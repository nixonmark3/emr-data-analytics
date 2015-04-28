import sys
import pandas as pd
import numpy as np

from scipy.stats import pearsonr
from FunctionBlock import FunctionBlock


class LagCorr(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            lag = self.parameters['Lag']

            df_result = pd.DataFrame()

            already_correlated = list()

            for x_tag in df.columns:
                for y_tag in df.columns:
                    if y_tag == x_tag:
                        continue
                    if y_tag in already_correlated:
                        continue
                    x = df[x_tag]
                    y = df[y_tag]
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
                    df_result['{0}x{1}'.format(x_tag, y_tag)] = [r[0] for r in result]
                already_correlated.append(x_tag)

            FunctionBlock.save_results(self, df=df_result, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): df_result}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


