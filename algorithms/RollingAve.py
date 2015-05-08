__author__ = 'noelbell'


import sys
import pandas as pd
import collections

from FunctionBlock import FunctionBlock


class RollingAve(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            window_size = self.parameters['WindowSize']

            df_rolling_ave = pd.rolling_mean(df, int(window_size))

            results = collections.OrderedDict()
            results['WindowSize'] = window_size

            FunctionBlock.save_results(self, df=df_rolling_ave, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df_rolling_ave}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




