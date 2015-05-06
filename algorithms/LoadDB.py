import sys
import ast

from Projects import Project
from Projects import Dataset
from pymongo import MongoClient
from FunctionBlock import FunctionBlock


class LoadDB(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            project = self.parameters['Project']

            data_set = str(self.parameters['Data Set'])

            plot = ast.literal_eval(self.parameters['Plot'])

            if (project == 'None') | (data_set == 'None'):
                FunctionBlock.report_status_configure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}

            connection = MongoClient()
            project = Project.Create(connection, name=project)

            ds = Dataset.Create(project, data_set)
            df = ds.Load_Data()

            connection.close()

            FunctionBlock.save_results(self, df=df, statistics=True, plot=plot)
            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)