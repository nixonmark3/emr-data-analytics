__author__ = 'noelbell'


import sys
import pandas as pd
import numpy as np
import collections

from FunctionBlock import FunctionBlock


class BoxcarAve(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            window_size = self.parameters['WindowSize']

            variable_names = df.columns.values
            num_obs = int(df.shape[0])
            num_vars = int(df.shape[1])
            input_index = 0
            index_size = num_obs // window_size
            index = np.linspace(0, index_size, index_size + 1) * window_size

            filtered_df = df.copy()

            for i in range(input_index, num_vars):
                for j in range(index_size):
                    start = int(index[j])
                    end = int(index[j+1])
                    filtered_df[variable_names[i]][start:end] = np.tile(np.mean(filtered_df[variable_names[i]][start:end]), window_size)

            # for col in df.columns.values:
            #     filtered_df[col] = pd.rolling_window(df[col], window_size, 'boxcar')

            results = collections.OrderedDict()
            results['WindowSize'] = window_size

            FunctionBlock.save_results(self, df=filtered_df, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): filtered_df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




