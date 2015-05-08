__author__ = 'noelbell'

import sys
import pandas as pd
import collections

from FunctionBlock import FunctionBlock


class RollingStdDev(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            window_size = self.parameters['WindowSize']

            df_roll_std_dev = pd.rolling_std(df, int(window_size))

            results = collections.OrderedDict()
            results['WindowSize'] = window_size

            FunctionBlock.save_results(self, df=df_roll_std_dev, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df_roll_std_dev}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)





