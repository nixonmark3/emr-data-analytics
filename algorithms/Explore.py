import sys
import numpy as np
import json
import pymongo
import gridfs

from FunctionBlock import FunctionBlock
from collections import OrderedDict


class JsonConverter(dict):
        def __str__(self):
            return json.dumps(self)


class Explore(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def store_data(self, data_table):
        filename = '{0}_data'.format(self.unique_name)

        connection = pymongo.MongoClient()

        db = connection['emr-data-analytics-studio']

        fs = gridfs.GridFS(db)

        if fs.exists(filename=filename):
            fp = fs.get_last_version(filename)
            fs.delete(fp._id)

        fs.put(str(data_table).encode("UTF-8"), filename=filename)

        connection.close()

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            df.index = df.index.astype(np.int64) // 10**9

            df.index = df.index.astype(str)

            data_table = OrderedDict()

            for col in df.columns.values:
                col_data = df[col]
                data = []
                for i, v in col_data.iteritems():
                    data.append({"date": i, "value": v})
                data_table[col] = data

            data_table = JsonConverter(data_table)

            self.store_data(data_table=data_table)

            FunctionBlock.save_results(self, df=df, statistics=True)

            FunctionBlock.report_status_complete(self)

            return {'{0}'.format(self.unique_name): None}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

