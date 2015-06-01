import sys

from FunctionBlock import FunctionBlock
from SEEQ import Connection
from SEEQ import Import_SEEQ_Capsule


class LoadSeeq(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            seeq_ip = self.parameters['Seeq IP']

            capsule_name = self.parameters['Capsule Name']

            Connection(seeq_ip)

            df = Import_SEEQ_Capsule(capsule_name)

            FunctionBlock.save_results(self, df=df, statistics=True, plot=False)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
