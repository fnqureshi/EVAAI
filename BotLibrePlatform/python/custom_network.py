# Copyright 2020 Paphus Solutions Inc. - All rights reserved.
import tensorflow as tf
from tensorflow import keras
import numpy as np
import database
import os
import tempfile
import threading
from typing import List
import time


models_cache = dict()  # Cache of models stored as instances of Item class
cache_lock = threading.Lock()
max_cache_length = 3  # Maximum number of models stored in cache
min_cache_time = 10.0  # If an Item's time_last_used attribute is as least this many seconds old, it can be removed from cache


# Class for storing the graph, session, lock, and time since last use of a model
class Item:
    def __init__(self, model: keras.Model, graph: tf.Graph, session: tf.Session, lock: threading.Lock) -> None:
        self.model = model
        self.graph = graph
        self.session = session
        self.lock = lock
        self.time_last_used = time.time()

    def init_graph(self) -> None:
        if self.graph is None:
            self.graph = tf.Graph()
            if self.session is not None:
                self.session.close()
            self.session = tf.Session(graph=self.graph)


# Get the item from cache if it exists; otherwise, product a new item
def get_item_from_cache(media_id: int) -> Item:
    with cache_lock:
        if media_id in models_cache:
            item = models_cache[media_id]
            item.time_last_used = time.time()
            return item
        else:
            return _new_cache_item(media_id)


def _new_cache_item(new_media_id: int) -> Item:
    new_item = Item(model=None, graph=None, session=None, lock=threading.Lock())

    if len(models_cache) >= max_cache_length:
        purge_cache()

        if len(models_cache) >= max_cache_length:
            return new_item

    models_cache[new_media_id] = new_item
    return new_item


# Delete all items in cache that have not been used in the last min_cache_time seconds
def purge_cache() -> None:
    media_ids_to_be_removed = []
    cur_time = time.time()
    for media_id in models_cache:
        item = models_cache[media_id]
        if item.lock.acquire(blocking=False):
            if cur_time - item.time_last_used > min_cache_time:
                item.session.close()
                media_ids_to_be_removed.append(media_id)
            else:
                item.lock.release()

    for media_id in media_ids_to_be_removed:
        del models_cache[media_id]


def save_model_to_database(analytic_id: str, media_id: int, model: keras.Model) -> None:
    if not os.path.exists("temp/"):
        os.makedirs("temp/")
    if not os.path.exists("temp/custom_networks/"):
        os.makedirs("temp/custom_networks/")
    try:
        temp_network_file = tempfile.NamedTemporaryFile(prefix=str(media_id)+"_", suffix=".h5", dir="temp/custom_networks/", mode="rb", delete=False)
        model.save(temp_network_file.name)
        file_binary = temp_network_file.read()
    finally:
        temp_network_file.close()
        os.remove(temp_network_file.name)

    database.update_custom_network_binary(analytic_id, media_id, file_binary)


def get_model_from_database(media_id: int) -> keras.Model:
    file_binary = database.get_image_media(media_id)

    if not os.path.exists("temp/"):
        os.makedirs("temp/")
    if not os.path.exists("temp/custom_networks/"):
        os.makedirs("temp/custom_networks/")
    try:
        temp_network_file = tempfile.NamedTemporaryFile(prefix=str(media_id)+"_", suffix=".h5", dir="temp/custom_networks/", mode="wb", delete=False)
        temp_network_file.write(file_binary.tobytes())
        model = keras.models.load_model(temp_network_file.name)
        model._make_predict_function()
    finally:
        temp_network_file.close()
        os.remove(temp_network_file.name)

    return model


def reset_network(analytic_id: str) -> None:
    inputs, intermediates, outputs, layers, raw_activation_function, media_id = database.get_custom_network_details(analytic_id)

    if raw_activation_function == "Tanh":
        activation_function = "tanh"
    elif raw_activation_function == "Sigmoid":
        activation_function = "sigmoid"

    item = get_item_from_cache(media_id)

    with item.lock:
        item.init_graph()

        with item.graph.as_default():
            with item.session.as_default() as sess:
                sess.run(tf.global_variables_initializer())
                item.model = keras.Sequential()
                if layers < 1:
                    item.model.add(keras.layers.Dense(outputs, activation=activation_function, input_shape=(inputs,)))
                else:
                    item.model.add(keras.layers.Dense(intermediates, activation=activation_function, input_shape=(inputs,)))
                    for _ in range(1, layers):
                        item.model.add(keras.layers.Dense(intermediates, activation=activation_function))
                    item.model.add(keras.layers.Dense(outputs, activation=activation_function))

                item.model.compile(optimizer="Adam", loss="mse")
                item.model._make_predict_function()
                # model.summary()

                save_model_to_database(analytic_id, media_id, item.model)

        item.time_last_used = time.time()


def test_network(analytic_id: str, inputs_list: List[List[float]]) -> np.ndarray:
    media_id = database.get_media_id(analytic_id)

    item = get_item_from_cache(media_id)

    with item.lock:
        item.init_graph()

        with item.graph.as_default():
            with item.session.as_default() as sess:
                if item.model is None:
                    item.model = get_model_from_database(media_id)
                    sess.run(tf.global_variables_initializer())

                return item.model.predict(np.array(inputs_list))

        item.time_last_used = time.time()


def train_network(analytic_id: str, inputs_list: List[List[float]], outputs_list: List[List[float]]) -> None:
    media_id = database.get_media_id(analytic_id)

    item = get_item_from_cache(media_id)

    with item.lock:
        item.init_graph()

        with item.graph.as_default():
            with item.session.as_default() as sess:
                if item.model is None:
                    item.model = get_model_from_database(media_id)
                    sess.run(tf.global_variables_initializer())

                item.model.fit(np.array(inputs_list), np.array(outputs_list), epochs=10)
                save_model_to_database(analytic_id, media_id, item.model)

        item.time_last_used = time.time()
