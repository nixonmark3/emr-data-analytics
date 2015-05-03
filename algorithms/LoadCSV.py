import sys
import pandas as pd

from FunctionBlock import FunctionBlock


class LoadCSV(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            filename = self.parameters['Filename']

            df = pd.read_csv(filename, parse_dates=True, index_col=0)

            FunctionBlock.save_results(self, df=df, statistics=True, plot=True)
            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
