__author__ = 'noelbell'


import sys
import pandas as pd
import numpy as np
import collections

from FunctionBlock import FunctionBlock


class ExpFilter(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            alpha = self.parameters['Alpha']
            order = self.parameters['Order']

            variable_names = df.columns.values
            num_obs = int(df.shape[0])
            num_var = int(df.shape[1])
            smooth_data = df.copy()

            if order == 1:
                co_eff = np.array([1-alpha, alpha])
            else:
                co_eff = np.array([-(1-alpha)**2, 2*(1-alpha), alpha**2])

            co_eff_0 = co_eff[0]
            co_eff_1 = co_eff[1]

            for i in range(0, num_var):
                col = smooth_data[variable_names[i]]
                col_df = df[variable_names[i]]

                if order == 1:
                    for j in range(1, num_obs):
                        col.iat[j] = co_eff_0 * col.iat[j-1] + co_eff_1 * col_df.iat[j]
                else:
                    for j in range(2, num_obs):
                        col.iat[j] = co_eff[0] * col.iat[j-2] + co_eff[1] * col.iat[j-1] + co_eff[2] * col_df.iat[j]

                smooth_data[variable_names[i]] = col

            results = collections.OrderedDict()
            results['Alpha'] = alpha
            results['Order'] = order

            FunctionBlock.save_results(self, df=smooth_data, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): smooth_data}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




