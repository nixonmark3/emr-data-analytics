import sys
import collections

from Bricks import BricksDB
from Bricks import Dataset
from pymongo import MongoClient
from FunctionBlock import FunctionBlock


class SaveQueryDb(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            brick = self.parameters['Brick']

            create_new_name = self.parameters['New Brick Name']

            if brick == '' and create_new_name == '':
                FunctionBlock.report_status_failure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}

            data_set = str(self.parameters['Data Set Name'])

            if len(create_new_name) == '':
                brick_name = brick
            else:
                brick_name = create_new_name

            connection = MongoClient()
            project = BricksDB.Create(connection, name=brick_name)

            columns = df.columns.values.tolist()

            ds = Dataset.Create(project, data_set, tags=columns, labels=columns)
            size = ds.Store_Data(df)

            connection.close()

            results = collections.OrderedDict()
            results['Size'] = size

            FunctionBlock.save_results(self, df=df, statistics=True, plot=False, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
