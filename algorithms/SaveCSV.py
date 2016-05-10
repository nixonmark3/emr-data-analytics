import sys
import pandas as pd

from FunctionBlock import FunctionBlock


class SaveCSV(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df1 = results_table[self.input_connectors['in'][0]]

            #print('df in save csv......\n', df1)

            filename = self.parameters['Filename']

            df1.to_csv(filename)

            FunctionBlock.save_results(self, df=df1, statistics=True, plot=True)
            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df1}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

