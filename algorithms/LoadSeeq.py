import sys
import pandas as pd

from Projects import Project
from pymongo import MongoClient
from FunctionBlock import FunctionBlock
from SEEQ import Connection
from SEEQ import Import_SEEQ_Capsule
from Exceptions import NotFoundException


class LoadSeeq(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            seeq_ip = self.parameters['Seeq IP']
            project_name = self.parameters['Project Name']
            capsule_name = self.parameters['Capsule Name']

            Connection(seeq_ip)

            pd.set_option('display.width', 1000)

            connection = MongoClient()

            try:
                project = Project(connection, name=project_name)
            except NotFoundException:
                project = Project.Create(connection, name=project_name)

            df = Import_SEEQ_Capsule(project, {"Name": capsule_name})

            connection.close()

            # FunctionBlock.save_results(self, df=df, statistics=True)
            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
