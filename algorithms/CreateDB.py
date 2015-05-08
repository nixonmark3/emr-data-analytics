import sys
import pandas as pd

from Projects import Project
from Projects import Dataset
from pymongo import MongoClient
from FunctionBlock import FunctionBlock


class CreateDB(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            filename = self.parameters['Filename']

            project_name = self.parameters['Project Name']

            data_set_name = str(self.parameters['Data Set Name'])

            df = pd.read_csv(filename, parse_dates=True, index_col=0)

            connection = MongoClient()
            project = Project.Create(connection, name=project_name)

            ds = Dataset.Create(project, data_set_name)
            ds.Store_Data(df)

            connection.close()

            FunctionBlock.save_results(self, df=df, statistics=True, plot=False)
            FunctionBlock.report_status_complete(self)

            return {'{0}'.format(self.unique_name): None}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
