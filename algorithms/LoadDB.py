import sys

from Projects import Project
from Projects import Dataset
from pymongo import MongoClient
from FunctionBlock import FunctionBlock


class LoadDB(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            project = self.parameters['Project']

            data_set = str(self.parameters['Data Set'])

            if (project == 'None') | (data_set == 'None'):
                FunctionBlock.report_status_configure(self)
                return {'{0}/{1}'.format(self.name, 'out'): None}

            connection = MongoClient()
            project = Project.Create(connection, name=project)

            ds = Dataset.Create(project, data_set)
            df = ds.Load_Data()

            connection.close()

            FunctionBlock.save_results(self, df=df, statistics=True, plot=True)
            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)