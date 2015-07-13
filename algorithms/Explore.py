import sys
import numpy as np
import pymongo
import gridfs
import ast
import pandas as pd

from FunctionBlock import FunctionBlock


def prepare_data(df, fill_nan, time_series, reindex):
    if reindex:
        df = df.reset_index(drop=True)
        time_series = False
    if time_series:
        if df.index.values.dtype == np.dtype('datetime64[ns]'):
            df.index = df.index.astype(np.int64) / 10**9
    data = []
    features = df.columns.tolist()
    data.append(features)
    time_index = df.index.tolist()
    data.append(time_index)
    for feature in features:
        col = df[feature].tolist()
        if fill_nan:
            col = ['null' if np.isnan(x) else x for x in col]
        data.append(col)
    return str(data).replace('\'', '"')


def concat_dfs(dfs):

    all_timestamp = True

    for df in dfs:
        if df.index.values.dtype != np.dtype('datetime64[ns]'):
            all_timestamp = False
            break

    if not all_timestamp:
        for i, df in enumerate(dfs):
            dfs[i] = df.reset_index(drop=True)

    return pd.concat(dfs, axis=1), all_timestamp


class Explore(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def store_data(self, data):
        filename = '{0}_data'.format(self.unique_name)
        connection = pymongo.MongoClient()
        db = connection['emr-data-analytics-studio']
        fs = gridfs.GridFS(db)
        if fs.exists(filename=filename):
            fp = fs.get_last_version(filename)
            fs.delete(fp._id)
        fs.put(data.encode("UTF-8"), filename=filename)
        connection.close()

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            dfs = []
            for input_wire in self.input_connectors['in']:
                dfs.append(results_table[input_wire])

            fill_nan = ast.literal_eval(self.parameters['Fill NaN'])
            time_series = ast.literal_eval(self.parameters['Time Series'])
            reindex = ast.literal_eval(self.parameters['Reindex'])

            if len(dfs) == 1:
                df = dfs[0]
            else:
                df, time_series = concat_dfs(dfs)
                fill_nan = True

            FunctionBlock.add_statistics_result(self, df)

            self.store_data(prepare_data(df, fill_nan, time_series, reindex))

            FunctionBlock.save_all_results(self)

            FunctionBlock.report_status_complete(self)

            return {'{0}'.format(self.unique_name): None}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

