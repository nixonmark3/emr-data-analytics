import sys
import numpy as np
import pymongo
import gridfs

from FunctionBlock import FunctionBlock


def prepare_data(df):
    df.index = df.index.astype(np.int64) / 10**9
    data = []
    features = df.columns.tolist()
    data.append(features)
    time_index = df.index.tolist()
    data.append(time_index)
    for feature in features:
        data.append(df[feature].tolist())
    return str(data).replace('\'', '"')


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

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            self.store_data(prepare_data(df))

            FunctionBlock.save_results(self, df=df, statistics=True)

            FunctionBlock.report_status_complete(self)

            return {'{0}'.format(self.unique_name): None}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

