# Copyright 2013-2020 Paphus Solutions Inc. - All rights reserved.
import psycopg2
import psycopg2.pool
import os
import shutil
import base64
import time
import traceback

exception_msg = "INFO - UNABLE TO CONNECT TO DATABASE"

connections_pool = None


def setup_database(host, port, dbname, user, password):
    global connections_pool
    if connections_pool is not None:
        connections_pool.closeall()

    host = deobfuscate(host)
    port = deobfuscate(port)
    dbname = deobfuscate(dbname)
    user = deobfuscate(user)
    password = deobfuscate(password)

    try:
        connections_pool = psycopg2.pool.ThreadedConnectionPool(8, 16, host=host, port=port, dbname=dbname, user=user, password=password)
        print("INFO - DATABASE CONNECTED")
    except Exception as e:
        print("INFO - DATABASE FAILED TO CONNECT.\n\tCheck user and password")
        raise e


def get_connection():
    global connections_pool
    if connections_pool is None:
        print("INFO - TRIED TO GET CONNECTION BEFORE CALLING setup-database")
        raise ValueError("Tried to get database connection before calling setup-database")

    start_time = time.time()
    while time.time() - start_time <= 30:
        try:
            return connections_pool.getconn()
        except psycopg2.pool.PoolError:
            print("INFO - POOL OUT OF CONNECTIONS. WAITING FOR A CONNECTION TO BE RELEASED.")
            time.sleep(1)  # 1 second
    print("INFO - POOL OUT OF CONNECTIONS.")
    raise psycopg2.pool.PoolError()


def release_connection(connection, close=False):
    global connections_pool
    connections_pool.putconn(connection, close=close)


def deobfuscate(de_str):
    de_str = bytearray.fromhex(de_str).decode()
    de_str = de_str[::-1]
    return de_str


def getLabels(analyticId):
    label = []
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT networklabels FROM analytic WHERE id = " + analyticId
    cur.execute(ex)
    rows = cur.fetchall()
    for row in rows:
        label.append(row[0])
    cur.close()
    release_connection(conn)
    label = label[0].splitlines()

    if not label:
        return []
    
    # delete the last empty item
    if not label[-1]:
        del label[-1]

    return label


def get_object_detection_labels(analyticId):  # retun file name
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT networklabels FROM analytic WHERE id = " + analyticId
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)
    # save file
    file_name = "temp/pbtxt/" + analyticId + ".pbtxt"
    with open(file_name, "w+") as f:
        f.write(rows[0][0])

    return file_name


def get_object_detection_Properties_labels(analyticId):  # retun file name
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT propertiesLabels FROM analytic WHERE id = " + analyticId
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)
    # save file
    file_name = "temp/pbtxt/" + analyticId + ".txt"
    with open(file_name, "w+") as f:
        if rows[0][0] is not None:
            f.write(rows[0][0])

    return file_name


def get_audio_input(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT audioinputname FROM analytic WHERE id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def get_audio_output(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT audiooutputname FROM analytic WHERE id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def get_feed(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT analyticfeed FROM analytic WHERE id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def get_fetch(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT analyticfetch FROM analytic WHERE id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def get_image_size(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT imagesize FROM analytic WHERE id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def prepare_network(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT mediaid, label FROM analyticmedia WHERE analytic_id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows


def prepare_test_media(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT mediaid, label, name FROM analytictestmedia WHERE analytic_id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows


def get_media_id(AnalyticId):
    conn = get_connection()
    cur = conn.cursor()
    ex = "select networkbinary_mediaid from Analytic where id = " + AnalyticId
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def get_media(analyticID):
    media_id = get_media_id(analyticID)
    print("MediaID: " + str(media_id))
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT media FROM media WHERE id = " + str(media_id)
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def get_image_media(media_id):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT media FROM media WHERE id = " + str(media_id)
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def get_analytic_type(analyticID):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT analytictype FROM analytic WHERE id = " + analyticID
    cur.execute(ex)
    rows = cur.fetchall()
    cur.close()
    release_connection(conn)

    return rows[0][0]


def update_analytic_binary(analytic_id, file_binary):
    media_id = get_media_id(analytic_id)
    conn = get_connection()
    cur = conn.cursor()
    ex = "UPDATE media SET media = %s WHERE id = %s"
    cur.execute(ex, ((psycopg2.Binary(file_binary)), str(media_id)))
    q = "UPDATE mediafile SET name = %s WHERE mediaid = %s"
    cur.execute(q, ("retrained_graph.pb", media_id))
    q = "UPDATE mediafile SET type = %s WHERE mediaid = %s"
    cur.execute(q, ("application/octet-stream", media_id))
    f = "UPDATE analytic SET binaryfilename = %s WHERE id = %s"
    cur.execute(f, ("retrained_graph.pb", analytic_id))
    conn.commit()
    cur.close()
    release_connection(conn)


def update_analytic_labels(analytic_id, file_labels):
    conn = get_connection()
    cur = conn.cursor()
    ex = "UPDATE analytic SET networklabels = %s WHERE id = %s"
    cur.execute(ex, (file_labels, analytic_id))
    q = "UPDATE analytic SET labelsfilename = %s WHERE id = %s"
    cur.execute(q, ("retrained_labels.txt", analytic_id))
    conn.commit()
    cur.close()
    release_connection(conn)


def update_analytic_test_media_result(analytic_id, test_media_result):
    conn = get_connection()
    cur = conn.cursor()
    ex = "UPDATE analytic SET testmediaresult = %s WHERE id = %s"
    cur.execute(ex, (test_media_result, analytic_id))
    conn.commit()
    cur.close()
    release_connection(conn)


def update_analytic_status(status, analytic_id):
    conn = get_connection()
    cur = conn.cursor()
    ex = "UPDATE analytic SET trainingstatus = %s WHERE id = %s"
    cur.execute(ex, (status, analytic_id))
    conn.commit()
    cur.close()
    release_connection(conn)


def update_test_media_status(status, analytic_id):
    conn = get_connection()
    cur = conn.cursor()
    ex = "UPDATE analytic SET processingtestmediastatus = %s WHERE id = %s"
    cur.execute(ex, (status, analytic_id))
    conn.commit()
    cur.close()
    release_connection(conn)


def remove_dir_files(folder):
    for the_file in os.listdir(folder):
        file_path = os.path.join(folder, the_file)
        try:
            if os.path.isfile(file_path):
                os.unlink(file_path)
            elif os.path.isdir(file_path):
                shutil.rmtree(file_path)
        except:
            traceback.print_exc()


def is_file_at_zero(fpath):
    return os.stat(fpath).st_size == 0


def check_graph_exists(mediaID):
    result = False
    for graph_file in os.listdir("temp/graphs"):
        if graph_file.endswith(mediaID + ".pb"):
            print("File: " + os.path.join("temp/graphs", graph_file))
            result = os.path.join("temp/graphs", graph_file)
    return result


def convert_image_to_base64_xml(image_result, path, numOfClasses, result, coordinates):
    # c = ['ymin', 'xmin', 'ymax', 'xmax']
    with open(path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
        xml_code = "<analytic-object-detection-response>"
        if image_result:
            xml_code = xml_code + "<image>" + encoded_string.decode("utf-8") + "</image>"
        xml_code = xml_code + "<numberOfClasses>" + str(numOfClasses) + "</numberOfClasses>"
        for index, res in enumerate(result):
            xml_code = xml_code + "<result label=\"" + res.label + "\" confidence=\"" + res.score + "\" color=\"" + res.color + "\">"
            xml_code = xml_code + "<box bottom=\"" + str(coordinates[index].ymax) + "\" left=\"" + str(coordinates[index].xmin) + "\" top=\"" + str(coordinates[index].ymin) + "\" right=\"" + str(coordinates[index].xmax) + "\"></box>"
            xml_code = xml_code + "</result>"
        xml_code = xml_code + "</analytic-object-detection-response>"
        # print(xml_code)
        return xml_code


def convert_image_to_base64_xml_test(img, current_label, path, results, test_time=0):
    xml_code = ""
    with open(path, "rb") as image_file:
        # encoded_string = base64.b64encode(image_file.read())
        encoded_string = bytes([])  # do not encode the image due to memory issues
        xml_code = xml_code + "<analytic-object-detection-response name=\"" + img + "\" image=\"" + encoded_string.decode("utf-8") + "\""
        xml_code = xml_code + " testtime=\"{0:.2f}\"".format(test_time)
        if len(results) == 0:
            xml_code = xml_code + " expectedlabel=\"" + current_label + "\" expectedconfidence=\"0.0\" "
            xml_code = xml_code + " actuallabel=\"\" actualconfidence=\"\" "
        else:
            first_result = results[0]
            expected_result = None
            for index, result in enumerate(results):
                if current_label in result.label:
                    expected_result = result
                    break
            if expected_result is None:
                xml_code = xml_code + " expectedlabel=\"" + current_label + "\" expectedconfidence=\"0.0\" "
            else:
                xml_code = xml_code + " expectedlabel=\"" + expected_result.label + "\" expectedconfidence=\"0." + expected_result.score + "\" "
            xml_code = xml_code + " actuallabel=\"" + first_result.label + "\" actualconfidence=\"0." + first_result.score + "\" "
        xml_code = xml_code + "></analytic-object-detection-response>"
        # print("INFO - XML: ", xml_code)
        return xml_code


def convert_image_to_base64(path):
    with open(path, "rb") as image_file:
        encoded_string = base64.b64encode(image_file.read())
    return encoded_string.decode("utf-8")


def copytree(src, dst, symlinks=False, ignore=None):
    for item in os.listdir(src):
        s = os.path.join(src, item)
        d = os.path.join(dst, item)
        if os.path.isdir(s):
            shutil.copytree(s, d, symlinks, ignore)
        else:
            shutil.copy2(s, d)


def removeTempFiles(limitNumberOfFiles):
    list_of_pb_files = os.listdir("{}/temp/graphs/".format(os.getcwd()))
    num_of_pb_files = len(list_of_pb_files)
    list_of_temp_images = os.listdir("{}/temp/graphs/".format(os.getcwd()))
    num_of_temp_images = len(list_of_temp_images)
    print("INFO - TEMP FILES : " + str(num_of_pb_files))
    print("INFO - CLEAN TEMP")
    if num_of_pb_files > limitNumberOfFiles:
        print("Remove temp graphs...")
        remove_dir_files("{}/temp/graphs/".format(os.getcwd()))
    if num_of_temp_images > limitNumberOfFiles:
        print("Remove temp images...")
        remove_dir_files("{}/temp/images/".format(os.getcwd()))


def removeTempAnalyticFiles(limitNumberOfFiles):
    list_of_analytics_files = os.listdir("{}/analytics/".format(os.getcwd()))
    num_of_analytics_files = len(list_of_analytics_files)
    print("INFO - TEMP FILES : " + str(num_of_analytics_files))
    print("INFO - CLEAN TEMP")
    if num_of_analytics_files > limitNumberOfFiles:
        print("Remove analytics directory...")
        remove_dir_files("{}/analytics/".format(os.getcwd()))


def get_custom_network_details(analytic_id):
    conn = get_connection()
    cur = conn.cursor()
    ex = "SELECT inputs, intermediates, outputs, layers, activationfunction, networkbinary_mediaid FROM analytic WHERE id = " + analytic_id
    cur.execute(ex)
    # returns a list formatted like so: [inputs, intermediates, outputs, layers, activation_function, media_id]
    result = cur.fetchall()[0]
    cur.close()
    release_connection(conn)
    return result


def update_custom_network_binary(analytic_id, media_id, file_binary):
    conn = get_connection()
    cur = conn.cursor()
    ex = "UPDATE media SET media = %s WHERE id = %s"
    cur.execute(ex, ((psycopg2.Binary(file_binary)), media_id))
    q = "UPDATE mediafile SET name = %s WHERE mediaid = %s"
    cur.execute(q, ("custom_network.h5", media_id))
    q = "UPDATE mediafile SET type = %s WHERE mediaid = %s"
    cur.execute(q, ("application/octet-stream", media_id))
    f = "UPDATE analytic SET binaryfilename = %s WHERE id = %s"
    cur.execute(f, ("custom_network.h5", analytic_id))
    conn.commit()
    cur.close()
    release_connection(conn)
