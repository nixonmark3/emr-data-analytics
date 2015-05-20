__author__ = 'noelbell'

import sys
import pandas as pd
import collections
import numpy as np

from scipy.stats import pearsonr
from FunctionBlock import FunctionBlock


class TimeDelay(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')
            x_df = results_table[self.input_connectors['x'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'y')
            y_df = results_table[self.input_connectors['y'][0]]

            lag = self.parameters['Max Lag']

            df_result = pd.DataFrame()

            already_correlated = list()

            for x_tag in x_df.columns:
                for y_tag in y_df.columns:
                    if y_tag == x_tag:
                        continue
                    if y_tag in already_correlated:
                        continue
                    x = x_df[x_tag]
                    y = y_df[y_tag]
                    if len(x) != len(y):
                        raise 'Input variables of different lengths.'
                    if np.isscalar(lag):
                        if abs(lag) >= len(x):
                            raise 'Maximum lag equal or larger than array.'
                        if lag < 0:
                            lag = -np.arange(abs(lag) + 1)
                        elif lag == 0:
                            lag = [0, ]
                        else:
                            lag = np.arange(lag + 1)
                    elif lag is None:
                        lag = [0, ]
                    else:
                        lag = np.asarray(lag)
                    result = []
                    for ii in lag:
                        if ii < 0:
                            result.append(pearsonr(x[:ii], y[-ii:]))
                        elif ii == 0:
                            result.append(pearsonr(x, y))
                        elif ii > 0:
                            result.append(pearsonr(x[ii:], y[:-ii]))
                    df_result['{0}'.format(x_tag)] = [r[0] for r in result]
                already_correlated.append(x_tag)

            time_delays = collections.OrderedDict()

            for k, v in df_result.idxmax().items():
                time_delays[k] = int(v)

            time_delay_results = collections.OrderedDict()
            time_delay_results['Time Delays'] = list(time_delays.items())

            FunctionBlock.save_results(self, results=time_delay_results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): time_delays}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
