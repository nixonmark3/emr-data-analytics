__author__ = 'noelbell'


import sys
import pandas as pd
import collections

from FunctionBlock import FunctionBlock


class RateOfChange(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            window_size = self.parameters['WindowSize']
            suffix = self.parameters['AddSuffix']

            df_rate_change = df.diff(periods=window_size, axis='rows')

            for tag in df_rate_change.columns:
                new_tag = tag + suffix
                df_rate_change = df_rate_change.rename(columns={tag: new_tag})

            results = collections.OrderedDict()
            results['WindowSize'] = window_size

            FunctionBlock.save_results(self, df=df_rate_change, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df_rate_change}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




