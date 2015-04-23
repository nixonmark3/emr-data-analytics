__author__ = 'paulmuston'

import csv
from datetime import datetime
import pandas as pd
import pickle
from os import walk
import os.path
from pymongo import MongoClient
from Bricks import BricksDB, Dataset

connection = MongoClient()
bricks_db = BricksDB(connection, "PICKLE-20150401")

samples_dir = "/volumes/alper/Data/Pickle/DWC/2015-04-01/Lab Sample Export"

filenames = []

for (path, dir_names, file_names) in walk(samples_dir):
    filenames.extend([os.path.join(path, x) for x in file_names])
    break

for filename in filenames:
    if filename.find('/._') == -1:
        basename = os.path.basename(filename).split(".")[0]
        print(basename)
        with open(filename) as csvfile:
            rows = csv.reader(csvfile, delimiter=",")
            timestamps = []
            values = []
            for row in rows:
                ts, val = row
                timestamps.append(datetime.fromtimestamp(int(row[0])/1000000.0))
                values.append(float(row[1]))
        df = pd.DataFrame(values,index=timestamps, columns=[basename])
        data_set = Dataset.Create(bricks_db, name=basename, tags=list(df.columns), labels=["2015-04-01", "Lab Sample"])
        data_set.Store_Data(df)
