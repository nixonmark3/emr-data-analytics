__author__ = 'noelbell'

import sys
import pandas as pd
import numpy as np
import collections

from FunctionBlock import FunctionBlock


class SavGolay(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            nr = self.parameters['PointsToRight']
            nl = self.parameters['PointsToLeft']
            m = self.parameters['PolynomialOrder']

            b = np.mat([[kk**i for i in range(m+1)] for kk in range(-nl, nr+1)])  # m+1 to include m

            co_eff = np.ravel(np.linalg.pinv(b)[0])
            num_var = int(df.shape[1])
            smooth_data = df.copy()
            variable_names = df.columns.values

            for i in range(0, num_var):
                y = df[variable_names[i]]
                first_val = y[0] - abs(y[1:nl+1][::-1] - y[0])  # [::-1] to reverse the order
                last_val = y.iget(-1) + abs(y[-nr-1:-1][::-1] - y.iget(-1))   # last value of data frame cannot be got by y[-1]
                y_new = np.concatenate((first_val, y, last_val))
                smooth_data[variable_names[i]] = np.convolve(co_eff[::-1], y_new, mode='valid')

            results = collections.OrderedDict()
            results['PointsToRight'] = nr
            results['PointsToLeft'] = nl
            results['PolynomialOrder'] = m

            FunctionBlock.save_results(self, df=smooth_data, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): smooth_data}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




