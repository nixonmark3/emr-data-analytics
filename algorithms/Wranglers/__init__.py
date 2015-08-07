
import pandas as pd
import Exceptions
import numpy as np
import csv
import dateutil

from Bricks import BricksDB
from Bricks import Dataset
from pymongo import MongoClient
from datetime import datetime


def import_csv(filename, project_name, data_set_name):
    df = pd.read_csv(filename, parse_dates=True, index_col=0)
    connection = MongoClient()
    project = BricksDB.Create(connection, name=project_name)
    ds = Dataset.Create(project, data_set_name)
    ds.Store_Data(df)
    connection.close()
    return df


def import_ff3(project_name, parameters):

    connection = MongoClient()
    project = BricksDB.Create(connection, name=project_name)

    path = parameters["Path"]

    if "Name" in parameters:
        name = parameters["Name"]
    else:
        raise Exceptions.ParameterException

    if "Labels" in parameters:
        labels = parameters["Labels"]
    else:
        labels = None

    df, meta = read_ff3(path)
    datetime_from, datetime_to = time_range(df)

    try:
        data_set = Dataset(project, name=name)
        data_set.delete()
    except Exceptions.NotFoundException:
        pass

    data_set = Dataset.Create(project, name=name)
    meta.update({"imported": datetime.now()})
    data_set.update({"from": datetime_from, "to": datetime_to, "columns": list(df.columns), "rows": df.shape[0]})
    data_set.add_meta(meta)

    if labels is not None:
        for label in labels:
            data_set.add_label(label)

    total_size = data_set.Store_Data(df)
    data_set.add_meta({"total_size": total_size})
    return df


def read_ff3(path):

    with open(path) as csv_file:
        rows = csv.reader(csv_file, delimiter="\t")
        header = next(rows)
        num_cols = len(header)
        timestamps = []
        values_list = []
        for row in rows:
            ts = dateutil.parser.parse(row[1], yearfirst=True)
            timestamps.append(ts)
            values = []
            for x in row[2:num_cols]:
                try:
                    x = float(x)
                except:
                    x = np.nan
                values.append(x)
            values_list.append(values)
    meta = {"import_type": "FF3", "path": path}
    return pd.DataFrame(values_list, index=timestamps, columns=header[2:]), meta


def time_range(df):
    count = len(df)
    return df.index[0], df.index[count-1]