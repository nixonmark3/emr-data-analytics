import sys
import collections

from Projects import Project
from Projects import Dataset
from pymongo import MongoClient
from FunctionBlock import FunctionBlock


class SaveDB(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            project = self.parameters['Project']

            data_set = str(self.parameters['Data Set Name'])

            connection = MongoClient()
            project = Project.Create(connection, name=project)

            ds = Dataset.Create(project, data_set)
            size = ds.Store_Data(df)

            connection.close()

            results = collections.OrderedDict()
            results['Size'] = size

            FunctionBlock.save_results(self, df=df, statistics=True, plot=True, results=results)
            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
