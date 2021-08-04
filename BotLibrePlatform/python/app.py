# Copyright 2013-2020 Paphus Solutions Inc. - All rights reserved.
import os
import sys
from datetime import datetime
from flask import Flask, render_template, request, send_from_directory, Response
from workplace.image_recognition import label_image as image_recognition
from workplace.image_recognition import retrain as image_training

from workplace.speech_commands import label_wav as audio_recognition
from workplace.speech_commands import train as audio_training
from workplace.speech_commands import freeze as audio_freeze_graph

import database
import xml.etree.ElementTree as ET
import tensorflow as tf
import time

# Object Detection Imports #
from workplace.image_object_recognition.research.object_detection.utils import ops as utils_ops
from workplace.image_object_recognition.research.object_detection.utils import label_map_util
from workplace.image_object_recognition.research.object_detection.utils import visualization_utils as vis_util

from workplace.image_object_recognition import app_recognize_images as object_recognition

import custom_network
import numpy
import traceback
import time

app = Flask(__name__)

APP_ROOT = os.path.dirname(os.path.abspath(__file__))  # this is getting the file path for the uploaded image.

temp_folder = os.path.join(APP_ROOT, "temp/")
images_folder = os.path.join(APP_ROOT, "temp/images/")  # join is a method to join pathes ex usr/desktop/ + images/
graphs_folder = os.path.join(APP_ROOT, "temp/graphs/")
pbtxt_folder = os.path.join(APP_ROOT, "temp/pbtxt")  # for object_detection pbfiles needs to be saved before using.


@app.route("/")
def index():
    # database.setup_database(user, password)
    return ("<h1>botlibre server</h1>")


@app.route("/setup-database", methods=["POST"])
def setup_database():
    try:
        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        host = root.get("host")
        port = root.get("port")
        dbname = root.get("dbname")
        user = root.get("user")
        password = root.get("password")
        database.setup_database(host, port, dbname, user, password)

        return "Done"
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/test-analytic", methods=["POST"])
def test_analytic():
    try:
        print("INFO - TEST ANALYTIC")

        if not os.path.exists(temp_folder):
            os.makedirs(temp_folder)
        if not os.path.exists(images_folder):
            os.makedirs(images_folder)
        if not os.path.exists(graphs_folder):
            os.makedirs(graphs_folder)

        # first file
        if "file" not in request.files:
            print("No test file attached.")
            return Response("No test file attached.", status=400)

        test_file = request.files["file"]
        test_file.filename = str(datetime.now().second + datetime.now().minute) + test_file.filename
        dest = "/".join([images_folder, test_file.filename])
        test_file.save(dest)

        # second file - connection to database.
        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analyticID = root.get("analyticId")

        labels_result = database.getLabels(analyticID)
        # checking if graph exists in graphs/...pb
        exists_graph = database.check_graph_exists(analyticID)

        # check if the graph is 0 kb
        if exists_graph:
            is_empty = database.is_file_at_zero(exists_graph)  # is the file is empty 0kb
            if is_empty:
                os.remove(exists_graph)
                exists_graph = False  # after removing the file - exists becomes false.

        if not exists_graph:
            print("INFO - OUTPUT GRAPH")
            graphs_result = database.get_media(analyticID)
            # Save Graph File
            destG = "/".join([graphs_folder, analyticID + ".pb"])
            f = open(destG, "wb")
            # check if the file is not 0 kb - empty
            try:
                f.write(graphs_result.tobytes())
            except:
                return "<analytic-response label=\"Missing binary network\" confidence=\"0\"></analytic-response>"
            finally:
                f.close()
            # Using downloeded Graphs
            graphFile = "{}/temp/graphs/{}.pb".format(os.getcwd(), analyticID)
        else:
            print("INFO - GRAPH EXSITS")
            graphFile = exists_graph

        graph = image_recognition.load_graph(graphFile)

        image_size = database.get_image_size(analyticID)
        feed = database.get_feed(analyticID)
        fetch = database.get_fetch(analyticID)

        input_height = image_size
        input_width = image_size
        input_mean = 128
        input_std = 128
        input_layer = feed
        output_layer = fetch

        # check outputs:
        print("INFO - OUTPUT")
        print("\t", "GRAPH: ", graph)
        print("\t", "LABELS: ", labels_result)
        print("\t", "FEED: " + input_layer)
        print("\t", "FETCH: " + output_layer)
        print("\t", "IMAGE_SIZE: " + str(input_height))

        image_file = "{}/temp/images/{}".format(os.getcwd(), test_file.filename)

        result = image_recognition.read_tensor_from_image_file(image_file, input_height, input_width, input_mean, input_std)
        img_results = image_recognition.getImageResults(result, graph, labels_result, input_layer, output_layer)

        # Clean temporary files
        database.removeTempFiles(5)
        return Response(img_results, mimetype="text/xml")
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/training-analytic", methods=["POST"])
def training_analytic():
    try:
        print("INFO - TRAINING ANALYTIC")

        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analyticID = root.get("analyticId")

        print(analyticID)

        analyticFolder = os.path.join(APP_ROOT, "analytics/")
        targetID = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/")
        targetIMG = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/tf_files/")  # img_dir
        target_bottlenecks = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/bottlenecks/")
        target_output = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/output/")
        target_model = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/model/")
        target_summaries = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/summaries/")
        target_summaries_time = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/summaries/" + str(datetime.now().hour) + str(datetime.now().minute) + str(datetime.now().second) + "/")

        # training steps = 4000
        training_steps = 4000

        # check dir exists
        if not os.path.exists(temp_folder):
            os.makedirs(temp_folder)
        if not os.path.exists(images_folder):
            os.makedirs(images_folder)
        if not os.path.exists(graphs_folder):
            os.makedirs(graphs_folder)
        if not os.path.exists(analyticFolder):
            os.makedirs(analyticFolder)
        if not os.path.exists(targetID):
            os.makedirs(targetID)
        if not os.path.exists(targetIMG):
            os.makedirs(targetIMG)
        if not os.path.exists(target_bottlenecks):
            os.makedirs(target_bottlenecks)
        if not os.path.exists(target_output):
            os.makedirs(target_output)
        if not os.path.exists(target_model):
            os.makedirs(target_model)
        if not os.path.exists(target_summaries):
            os.makedirs(target_summaries)
        if not os.path.exists(target_summaries_time):
            os.makedirs(target_summaries_time)

        database.update_analytic_status("in-progress : extracting images...", analyticID)
        result = database.prepare_network(analyticID)

        # getting files from database
        print("INFO - EXTRACTING IMAGES\n\tID: " + analyticID)
        for row in result:
            destImg = "/".join([targetIMG, row[1], str(row[0]) + ".jpg"])
            image = database.get_image_media(row[0])
            dirc = "/".join([targetIMG, row[1]])
            if not os.path.exists(dirc):
                os.makedirs(dirc)
            f = open(destImg, "wb")
            f.write(image.tobytes())
            f.close()
            # print(destImg + " *Downloaded")

        image_size = database.get_image_size(analyticID)
        # check for the image size:

        analytic_type = database.get_analytic_type(analyticID)
        if analytic_type == "inception_v3":
            resultOfAnalyticType = analytic_type
        else:
            resultOfAnalyticType = analytic_type + "_" + str(image_size)

        database.update_analytic_status("in-progress : training images...", analyticID)
        image_training.start_training(resultOfAnalyticType, targetIMG, target_model, target_output, target_bottlenecks, target_summaries_time, training_steps)

        # check if exists in cache - delete
        exists_graph_file = database.check_graph_exists(analyticID)
        if exists_graph_file:
            os.remove(exists_graph_file)

        # reading file from output folder -> updating analytic
        with open(target_output + "retrained_graph.pb", "rb") as graph:
            data = graph.read()
            database.update_analytic_binary(analyticID, data)

        with open(target_output + "retrained_labels.txt", "r") as labels:
            data = labels.read()
            print(data)
            database.update_analytic_labels(analyticID, data)

        database.removeTempAnalyticFiles(10)
        return "Done"
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/report-media-analytic", methods=["POST"])
def report_media_analytic():
    try:
        start_time = time.time()
        list_results_test_media = []
        # Dictionary to store file name and id of the media.
        file_name_and_media = {}

        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analyticID = root.get("analyticId")

        print(analyticID)

        analyticFolder = os.path.join(APP_ROOT, "analytics/")
        targetID = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/")
        targetIMG = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/tf_files/")  # img_dir
        target_output = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/output/")

        print("Report Media Analytic: ", analyticID)

        # Clear up used images while trianing if exists. Using the same dir to test images.
        print("INFO - REMOVING IMAGES...")
        try:
            database.remove_dir_files(targetIMG)
        except:
            print("INFO - REMOVING IMAGES - Error.")

        # check dir exists
        if not os.path.exists(temp_folder):
            os.makedirs(temp_folder)
        if not os.path.exists(images_folder):
            os.makedirs(images_folder)
        if not os.path.exists(graphs_folder):
            os.makedirs(graphs_folder)
        if not os.path.exists(analyticFolder):
            os.makedirs(analyticFolder)
        if not os.path.exists(targetID):
            os.makedirs(targetID)
        if not os.path.exists(targetIMG):
            os.makedirs(targetIMG)
        if not os.path.exists(target_output):
            os.makedirs(target_output)

        database.update_test_media_status("in-progress : extracting images...", analyticID)
        result = database.prepare_test_media(analyticID)

        # getting files from database
        print("INFO - EXTRACTING IMAGES\n\tID: " + analyticID)
        for row in result:
            destImg = "/".join([targetIMG, row[1], str(row[2])])
            image = database.get_image_media(row[0])
            dirc = "/".join([targetIMG, row[1]])
            if not os.path.exists(dirc):
                os.makedirs(dirc)
            f = open(destImg, "wb")
            f.write(image.tobytes())
            # file_name_and_media.update({row[2]: database.convert_image_to_base64(destImg)})  # Saved temporarly to be used in the xml later to display the images.
            file_name_and_media.update({row[2]: ""})  # do not encode the image due to memory issues
            f.close()
            # print(destImg + " *Downloaded")

        database.update_test_media_status("in-progress : testing images...", analyticID)

        # checking if graph exists in graphs/...pb
        labels_result = database.getLabels(analyticID)
        exists_graph = database.check_graph_exists(analyticID)
        # check if the graph is 0 kb
        if exists_graph:
            is_empty = database.is_file_at_zero(exists_graph)
            if is_empty:
                os.remove(exists_graph)
                exists_graph = False

        if not exists_graph:
            print("INFO - OUTPUT GRAPH")
            graphs_result = database.get_media(analyticID)
            # Save Graph File
            destG = "/".join([graphs_folder, analyticID + ".pb"])
            f = open(destG, "wb")
            # check if the file is not 0 kb - empty
            try:
                f.write(graphs_result.tobytes())
                f.close()
            except:
                return "<analytic-response label=\"Missing binary network\" confidence=\"0\"></analytic-response>"
            # Using downloeded Graphs
            graphFile = "{}/temp/graphs/{}.pb".format(os.getcwd(), analyticID)
        else:
            print("INFO - GRAPH EXSITS")
            graphFile = exists_graph
        graph = image_recognition.load_graph(graphFile)

        # loading settings
        image_size = database.get_image_size(analyticID)
        feed = database.get_feed(analyticID)
        fetch = database.get_fetch(analyticID)
        input_height = image_size
        input_width = image_size
        input_mean = 128
        input_std = 128
        input_layer = feed
        output_layer = fetch

        # count how many images there are
        number_of_images = 0
        for subdir, dirs, files in os.walk(targetIMG):
            for image_file in files:
                if image_file.endswith(".png") or image_file.endswith(".jpg") or image_file.endswith(".jpeg") or image_file.endswith(".PNG") or image_file.endswith(".JPG") or image_file.endswith(".JPEG"):
                    number_of_images += 1

        i = 0

        # loop through the images and test each
        for subdir, dirs, files in os.walk(targetIMG):
            for image_file in files:
                if image_file.endswith(".png") or image_file.endswith(".jpg") or image_file.endswith(".jpeg") or image_file.endswith(".PNG") or image_file.endswith(".JPG") or image_file.endswith(".JPEG"):

                    i += 1
                    print("INFO - testing image: {0} [{1}/{2}]".format(image_file, i, number_of_images))
                    if i % 10 == 9:
                        database.update_test_media_status("in-progress : testing images... ({0}/{1})".format(i, number_of_images), analyticID)

                    img = os.path.join(subdir, image_file)
                    # result of image recognition
                    result = image_recognition.read_tensor_from_image_file(img, input_height, input_width, input_mean, input_std)
                    file_id = file_name_and_media.get(image_file, "Non.")
                    img_results = image_recognition.getImageResultsForTest(os.path.basename(subdir), str(file_id), image_file, result, graph, labels_result, input_layer, output_layer)
                    # result saves in an array of strings
                    list_results_test_media.append(img_results)
                    # print("INFO - Image: " + image_file + " Result: " + img_results + "\n")

        end_time = time.time()
        elapsed_time = end_time - start_time
        start_time_str = time.strftime("%Y-%m-%d %H:%M:%S UTC", time.gmtime(start_time))
        end_time_str = time.strftime("%Y-%m-%d %H:%M:%S UTC", time.gmtime(end_time))

        with open(target_output + "test_media_result.xml", "w") as test_media:
            test_media.write("<test-media-result starttime=\"{0}\" endtime=\"{1}\" elapsedtime=\"{2:.0f}\">".format(start_time_str, end_time_str, elapsed_time))
            for result in list_results_test_media:
                test_media.write(result)
                test_media.write("\n")
            test_media.write("</test-media-result>")

        with open(target_output + "test_media_result.xml") as read_test_media:
            database.update_analytic_test_media_result(analyticID, read_test_media.read())

        return "done"  # Response(img_results, mimetype="text/xml")
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/report-media-analytic_object_detection", methods=["POST"])
def report_media_analytic_object_detection():
    try:
        start_time = time.time()

        label_colors = {}
        datebase_images = []
        # coordinates = []

        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analyticID = root.get("instance")

        threshold = root.get("threshold")

        if threshold is None:
            threshold = 0.5
        else:
            threshold = float(threshold)

        print("Threshold: " + str(threshold))

        print(analyticID)
        analyticFolder = os.path.join(APP_ROOT, "analytics/")
        targetID = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/")
        targetIMG = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/tf_files/")  # img_dir
        target_output = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/output/")

        print("Report Media Analytic Object Detection: ", analyticID)

        # Clear up used images while trinaing if exists. Using the same dir to test images.
        print("INFO - REMOVING IMAGES...")
        try:
            database.remove_dir_files(targetIMG)
        except Exception as e:
            print(e)
            print("INFO - REMOVING IMAGES - Error.")

        # check dir exists
        if not os.path.exists(temp_folder):
            os.makedirs(temp_folder)
        if not os.path.exists(images_folder):
            os.makedirs(images_folder)
        if not os.path.exists(graphs_folder):
            os.makedirs(graphs_folder)
        if not os.path.exists(analyticFolder):
            os.makedirs(analyticFolder)
        if not os.path.exists(targetID):
            os.makedirs(targetID)
        if not os.path.exists(targetIMG):
            os.makedirs(targetIMG)
        if not os.path.exists(target_output):
            os.makedirs(target_output)

        print("INFO - LOADING SETTINGS...")
        file_labels_path = database.get_object_detection_labels(analyticID)
        file_labels_path = os.path.join(APP_ROOT, file_labels_path)
        print(file_labels_path)

        file_properties_path = database.get_object_detection_Properties_labels(analyticID)
        file_properties_path = os.path.join(APP_ROOT, file_properties_path)
        print(file_properties_path)

        database.update_test_media_status("in-progress : extracting images...", analyticID)
        datebase_images = database.prepare_test_media(analyticID)

        # number of classes
        num_classes = 0
        with open(file_labels_path, "r") as ins:
            for line in ins:
                if "id" in line:
                    num_classes = num_classes + 1
        print("Number of classes: " + str(num_classes))

        with open(file_properties_path, "r") as ins:
            for line in ins:
                line = line.strip()
                if line != "":
                    list = line.split(":")
                    label_colors.update({list[0].strip(): list[1].strip()})

        if not label_colors:
            print("No colors.")
        else:
            print(label_colors)

        # getting files from database
        print("INFO - EXTRACTING IMAGES\n\tID: " + analyticID)
        for row in datebase_images:
            destImg = "/".join([targetIMG, row[1], str(row[2])])
            image = database.get_image_media(row[0])
            dirc = "/".join([targetIMG, row[1]])
            if not os.path.exists(dirc):
                os.makedirs(dirc)
            f = open(destImg, "wb")
            f.write(image.tobytes())
            f.close()

        database.update_test_media_status("in-progress : testing images...", analyticID)

        # checking if graph exists in graphs/...pb
        exists_graph = database.check_graph_exists(analyticID)

        # check if the graph is 0 kb
        if exists_graph:
            is_empty = database.is_file_at_zero(exists_graph)  # if file at 0kb
            if is_empty:
                os.remove(exists_graph)
                exists_graph = False  # after removing the file - exists becomes false.

        if not exists_graph:
            print("INFO - OUTPUT GRAPH")
            graphs_result = database.get_media(analyticID)
            # Save Graph File
            destG = "/".join([graphs_folder, analyticID + ".pb"])
            f = open(destG, "wb")
            try:
                f.write(graphs_result.tobytes())
                f.close()
            except:
                return "<analytic-response label=\"Missing binary network\" confidence=\"0\"></analytic-response>"
            # Using downloeded Graphs
            graphFile = "{}/temp/graphs/{}.pb".format(os.getcwd(), analyticID)
        else:
            print("INFO - GRAPH EXSITS")
            graphFile = exists_graph

        print("INFO - " + analyticID + " - Detecting Images...")
        # array of strings used while creating an xml file and return it to botlibre.
        list_test_media = []

        # count how many images there are
        number_of_images = 0
        for subdir, dirs, files in os.walk(targetIMG):
            for image_file in files:
                if image_file.endswith(".png") or image_file.endswith(".jpg") or image_file.endswith(".jpeg") or image_file.endswith(".PNG") or image_file.endswith(".JPG") or image_file.endswith(".JPEG"):
                    number_of_images += 1

        i = 0

        # loop through the images and test each
        for subdir, dirs, files in os.walk(targetIMG):
            for image_file in files:
                if image_file.endswith(".png") or image_file.endswith(".jpg") or image_file.endswith(".jpeg") or image_file.endswith(".PNG") or image_file.endswith(".JPG") or image_file.endswith(".JPEG"):

                    i += 1
                    print("INFO - testing image: {0} [{1}/{2}]".format(image_file, i, number_of_images))
                    database.update_test_media_status("in-progress : testing images... ({0}/{1})".format(i, number_of_images), analyticID)

                    img = os.path.join(subdir, image_file)
                    test_start_time = time.time()
                    coordinates, result, image_location = object_recognition.load_graph_and_labels(threshold, graphFile, file_labels_path, num_classes, img, label_colors)
                    test_end_time = time.time()
                    list_test_media.append(database.convert_image_to_base64_xml_test(image_file, os.path.basename(subdir), image_location, result, test_end_time - test_start_time))
                    print("INFO - Image: " + image_file + " Result: " + str(result) + "\n")

        end_time = time.time()
        elapsed_time = end_time - start_time
        start_time_str = time.strftime("%Y-%m-%d %H:%M:%S UTC", time.gmtime(start_time))
        end_time_str = time.strftime("%Y-%m-%d %H:%M:%S UTC", time.gmtime(end_time))

        with open(target_output + "test_media_result.xml", "w") as test_media:
            test_media.write("<test-media-result starttime=\"{0}\" endtime=\"{1}\" elapsedtime=\"{2:.0f}\">".format(start_time_str, end_time_str, elapsed_time))
            for result in list_test_media:
                test_media.write(result)
                test_media.write("\n")
            test_media.write("</test-media-result>")

        with open(target_output + "test_media_result.xml") as read_test_media:
            database.update_analytic_test_media_result(analyticID, read_test_media.read())
        # Clean temp images folder
        database.removeTempFiles(5)
        return "done"
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/test-analytic-detection", methods=["POST"])
def test_analytic_detection():
    try:
        print("INFO - TEST ANALYTIC OBJECT DETECTION")
        label_colors = {}
        result = []
        coordinates = []
        if not os.path.exists(temp_folder):
            os.makedirs(temp_folder)
        if not os.path.exists(images_folder):
            os.makedirs(images_folder)
        if not os.path.exists(graphs_folder):
            os.makedirs(graphs_folder)
        if not os.path.exists(pbtxt_folder):
            os.makedirs(pbtxt_folder)

        # first file
        if "file" not in request.files:
            print("No test file attached.")
            return Response("No test file attached.", status=400)
        test_file = request.files["file"]
        test_file.filename = str(datetime.now().second + datetime.now().minute) + test_file.filename
        dest = "/".join([images_folder, test_file.filename])
        test_file.save(dest)

        # second file - connection to database.
        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analyticID = root.get("instance")

        image_result = root.get("image-result")
        threshold = root.get("threshold")

        if threshold is None:
            threshold = 0.5
        else:
            threshold = float(threshold)

        if image_result == "false":
            image_result = False
        else:
            image_result = True

        print("Threshold: " + str(threshold))
        print("Image Result: " + str(image_result))

        file_labels_path = database.get_object_detection_labels(analyticID)
        file_labels_path = os.path.join(APP_ROOT, file_labels_path)
        # print(file_labels_path)

        file_properties_path = database.get_object_detection_Properties_labels(analyticID)
        file_properties_path = os.path.join(APP_ROOT, file_properties_path)
        # print(file_properties_path)

        # checking if graph exists in graphs/...pb
        exists_graph = database.check_graph_exists(analyticID)

        # number of classes
        num_classes = 0
        with open(file_labels_path, "r") as ins:
            for line in ins:
                if "id" in line:
                    num_classes = num_classes + 1
        print("Number of classes: " + str(num_classes))

        with open(file_properties_path, "r") as ins:
            for line in ins:
                line = line.strip()
                if line != "":
                    list = line.split(":")
                    label_colors.update({list[0].strip(): list[1].strip()})
        # print(label_colors)
        # image
        obj_image_file = "{}/temp/images/{}".format(os.getcwd(), test_file.filename)

        # check if the graph is 0 kb
        if exists_graph:
            is_empty = database.is_file_at_zero(exists_graph)
            if is_empty:
                os.remove(exists_graph)
                exists_graph = False  # after removing the file - exists becomes false.

        if not exists_graph:
            print("INFO - OUTPUT GRAPH")
            graphs_result = database.get_media(analyticID)
            # Save Graph File
            destination_graph = "/".join([graphs_folder, analyticID + ".pb"])
            f = open(destination_graph, "wb")
            # check if the file is not 0 kb - empty
            try:
                f.write(graphs_result.tobytes())
            except:
                return "<analytic-response label=\"Missing binary network\" confidence=\"0\"></analytic-response>"
            finally:
                f.close()
            # Using downloeded Graphs
            graphFile = "{}/temp/graphs/{}.pb".format(os.getcwd(), analyticID)
        else:
            print("INFO - GRAPH EXSITS")
            graphFile = exists_graph

        print("INFO - AnalyticID: " + analyticID + " Detecting image...")
        coordinates, result, image_path_result = object_recognition.load_graph_and_labels(threshold, graphFile, file_labels_path, num_classes, obj_image_file, label_colors)

        # Clean temporary files
        database.removeTempFiles(5)
        os.remove(obj_image_file)
        return database.convert_image_to_base64_xml(image_result, image_path_result, num_classes, result, coordinates)
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/test-audio-analytic", methods=["POST"])
def test_analytic_audio():
    try:
        print("INFO - TEST AUDIO")
        if not os.path.exists(temp_folder):
            os.makedirs(temp_folder)
        if not os.path.exists(images_folder):
            os.makedirs(images_folder)
        if not os.path.exists(graphs_folder):
            os.makedirs(graphs_folder)
        if not os.path.exists(pbtxt_folder):
            os.makedirs(pbtxt_folder)

        # first file
        if "file" not in request.files:
            print("No test file attached.")
            return Response("No test file attached.", status=400)
        file = request.files["file"]
        file.filename = str(datetime.now().second + datetime.now().minute) + file.filename
        dest = "/".join([images_folder, file.filename])
        file.save(dest)

        # second file - connection to database.
        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analyticID = root.get("analyticId")

        print("INFO - AnalyticID: " + analyticID)

        labels_result = database.getLabels(analyticID)
        # Labels must be saved in pbtxt as .txt
        destG = "/".join([pbtxt_folder, analyticID + ".txt"])
        with open(destG, "w") as f:
            for items in labels_result:
                f.write(items + "\n")
        labels_result = "{}/temp/pbtxt/{}".format(os.getcwd(), analyticID + ".txt")

        # checking if graph exists in graphs/...pb
        exists_graph = database.check_graph_exists(analyticID)

        if exists_graph:
            is_empty = database.is_file_at_zero(exists_graph)
            if is_empty:
                os.remove(exists_graph)
                exists_graph = False

        if not exists_graph:
            graphs_result = database.get_media(analyticID)
            # Save Graph File
            destG = "/".join([graphs_folder, analyticID + ".pb"])
            f = open(destG, "wb")
            try:
                f.write(graphs_result.tobytes())
            except:
                return "<analytic-audio-response label=\"Missing binary network\" confidence=\"0\"></analytic-audio-response>"
            finally:
                f.close()
        else:
            print("INFO - GRAPH EXSITS")

        graph = "{}/temp/graphs/{}.pb".format(os.getcwd(), analyticID)
        audioInputName = database.get_audio_input(analyticID)
        audioOutputName = database.get_audio_output(analyticID)

        # check outputs:
        print("INFO - OUTPUT")
        print("\t", "GRAPH: ", graph)
        print("\t", "LABELS: ", labels_result)
        print("\t", "audioInput: " + audioInputName)
        print("\t", "audioOutput: " + audioOutputName)

        audio_file = "{}/temp/images/{}".format(os.getcwd(), file.filename)

        print("INFO - Recognize Audio...")
        audio_results = audio_recognition.label_wav(audio_file, labels_result, graph, audioInputName, audioOutputName, 3)

        # Clean temporary files
        database.removeTempFiles(5)

        return Response(audio_results, mimetype="text/xml")
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/training-audio", methods=["POST"])
def training_audio():
    try:
        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)
        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analyticID = root.get("analyticId")

        print(analyticID)

        analyticFolder = os.path.join(APP_ROOT, "analytics/")
        targetID = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/")
        target_audio_data = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/tf_files/")  # folder contains files of audio, also needs to add background noise folder in there
        target_audio_summaries = os.path.join("analytics/analytic_" + str(analyticID) + "/summaries/" + str(datetime.now().hour) + str(datetime.now().minute) + str(datetime.now().second) + "/")
        target_audio_train = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/speech_commands_train/")  # checkpoints and data
        target_audio_output = os.path.join(APP_ROOT, "analytics/analytic_" + str(analyticID) + "/output/")  # result pb file.
        target_background_noise = os.path.join(APP_ROOT, "workplace/speech_commands/requirements/_background_noise_")

        # training total of steps are 5000
        # training_steps = "4000, 1000"

        # check dir exists
        if not os.path.exists(temp_folder):
            os.makedirs(temp_folder)
        if not os.path.exists(images_folder):
            os.makedirs(images_folder)
        if not os.path.exists(graphs_folder):
            os.makedirs(graphs_folder)
        if not os.path.exists(analyticFolder):
            os.makedirs(analyticFolder)
        if not os.path.exists(targetID):
            os.makedirs(targetID)
        if not os.path.exists(target_audio_data):
            os.makedirs(target_audio_data)
        if not os.path.exists(target_audio_output):
            os.makedirs(target_audio_output)
        if not os.path.exists(target_audio_train):
            os.makedirs(target_audio_train)
        if not os.path.exists(target_audio_summaries):
            os.makedirs(target_audio_summaries)

        database.update_analytic_status("in-progress : extracting audio file...", analyticID)
        result = database.prepare_network(analyticID)

        # getting files from database
        print("INFO - EXTRACTING AUDIO WAVS\n\tID: " + analyticID)
        for row in result:
            audio_dest = "/".join([target_audio_data, row[1], str(row[0]) + ".wav"])
            audio = database.get_image_media(row[0])
            directory_audio = "/".join([target_audio_data, row[1]])
            if not os.path.exists(directory_audio):
                os.makedirs(directory_audio)
            with open(audio_dest, "wb") as f:
                f.write(audio.tobytes())
            # print(audio_dest + " *Downloaded")"""

        # takeing all the labels (names of folders after extracting) as wanted words, is required for training.
        wanted_words = os.listdir(target_audio_data)
        wanted_words = ",".join(wanted_words)

        if "_background_noise_" in wanted_words:
            wanted_words = wanted_words.replace(", _background_noise_", "")

        # copy background noise contetns folder
        if not os.path.exists(target_audio_data + "/_background_noise_"):
            os.makedirs(target_audio_data + "/_background_noise_")

        print(wanted_words)

        database.copytree(target_background_noise, target_audio_data + "/_background_noise_")
        # updating status...
        database.update_analytic_status("in-progress : training images...", analyticID)

        # setting the model
        audio_model = database.get_analytic_type(analyticID)
        print(audio_model)

        # start audio training
        audio_training.train_audio(wanted_words, target_audio_data, target_audio_train, target_audio_summaries, audio_model)
        # it causes an error if it didn"t close
        """sess = tf.InteractiveSession()
        sess.close()"""
        tf.reset_default_graph()

        # check if graph exists in temp - delete for preparation to the new graph
        exists_graph_file = database.check_graph_exists(analyticID)
        if exists_graph_file:
            os.remove(exists_graph_file)

        # extracting new graph and txt files. freeze.
        print("freeze: " + target_audio_train + audio_model + ".ckpt-5000")
        audio_freeze_graph.freeze(audio_model, wanted_words, target_audio_output + "graph.pb", target_audio_train + audio_model + ".ckpt-5000")

        # raeding file from output folder -> updating analytic
        with open(target_audio_output + "graph.pb", "rb") as graph:
            data = graph.read()
            database.update_analytic_binary(analyticID, data)

        with open(target_audio_train + audio_model + "_labels.txt", "r") as labels:
            data = labels.read()
            database.update_analytic_labels(analyticID, data)

        # Clean temporary files
        database.removeTempAnalyticFiles(10)

        return "Done"
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/reset-data-analytic", methods=["POST"])
def reset_data_analytic():
    try:
        print("INFO - RESET CUSTOM NETWORK")

        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)

        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analytic_id = root.get("analyticId")
        print(analytic_id)
        try:
            custom_network.reset_network(analytic_id)
        except ValueError:
            traceback.print_exc()
            return Response("Bad inputs.", status=400)
        return "Done"
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/test-data-analytic", methods=["POST"])
def test_data_analytic():
    try:
        print("INFO - TEST CUSTOM NETWORK")

        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)

        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analytic_id = root.get("analyticId")
        print(analytic_id)

        try:
            # This is capable of testing multiple sets of inputs at a time, but AnalyticBean will always only send one
            analytic_inputs = []
            for input_element in root:
                if input_element.tag != "input":
                    raise ValueError("Expected <input> tag, got " + input_element.tag + " instead")
                input_element_text = input_element.text.rstrip(",")
                analytic_input = [float(val) for val in input_element_text.split(",")]
                analytic_inputs.append(analytic_input)
            # print("INFO - INPUTS:")
            # print(analytic_inputs)

            analytic_outputs = numpy.around(custom_network.test_network(analytic_id, analytic_inputs), 3)
            # print("INFO - OUTPUTS:")
            # print(analytic_outputs)
        except ValueError:
            traceback.print_exc()
            return Response("Bad inputs.", status=400)

        output_xml_list = ["<data-analytic-result>"]
        for output_list in analytic_outputs:
            output_xml_list.append("<output>")
            for output in output_list:
                output_xml_list.append(str(output))
                output_xml_list.append(",")
            if output_xml_list[-1] == ",":
                output_xml_list.pop()
            output_xml_list.append("</output>")
        output_xml_list.append("</data-analytic-result>")

        return Response("".join(output_xml_list), mimetype="text/xml")
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


@app.route("/train-data-analytic", methods=["POST"])
def train_data_analytic():
    try:
        print("INFO - TRAIN CUSTOM NETWORK")

        if "xml" not in request.form:
            print("No xml file attached.")
            return Response("No xml file attached.", status=400)

        xml = request.form["xml"]
        root = ET.fromstring(xml)
        analytic_id = root.get("analyticId")
        print(analytic_id)

        try:
            analytic_inputs = []
            analytic_outputs = []
            for data_element in root:
                if data_element[0].tag != "input":
                    raise ValueError("Expected <input> tag, got " + data_element[0].tag + " instead")
                if data_element[1].tag != "output":
                    raise ValueError("Expected <output> tag, got " + data_element[1].tag + " instead")

                input_element = data_element[0]
                output_element = data_element[1]

                input_element_text = input_element.text.rstrip(",")
                analytic_input = [float(val) for val in input_element_text.split(",")]
                analytic_inputs.append(analytic_input)

                output_element_text = output_element.text.rstrip(",")
                analytic_output = [float(val) for val in output_element_text.split(",")]
                analytic_outputs.append(analytic_output)

            # print("INFO - INPUTS:")
            # print(analytic_inputs)
            # print("INFO - OUTPUTS:")
            # print(analytic_outputs)

            custom_network.train_network(analytic_id, analytic_inputs, analytic_outputs)
        except ValueError:
            traceback.print_exc()
            return Response("Bad inputs.", status=400)

        return "Success"
    except Exception as e:
        traceback.print_exc()
        return Response("Error encountered when handling request: " + str(e), status=500)


if __name__ == "__main__":
    app.run(port=6777, debug=False, threaded=True)
