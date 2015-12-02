import sys
import pandas as pd
import numpy as np

from scipy.stats import pearsonr
from FunctionBlock import FunctionBlock


class LagCorr(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            lag = self.parameters['MaxLag']

            df_result = pd.DataFrame()
            df_result_all = pd.DataFrame()

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
                        factor = 1
                        if lag < 0:
                            factor = -1
                            #lag = -np.arange(abs(lag) + 1)
                        if lag == 0:
                            lag = [0, ]
                        if abs(lag) >= len(x):

                            #raise 'Maximum lag equal or larger than array.'
                            lag = factor * np.arange(max(0,len(x)))

                        else:
                            lag = factor * np.arange(lag + 1)
                    elif lag is None:
                        lag = [0, ]
                    else:
                        lag = np.asarray(lag)
                    result = []
                    #print('lag ................=   ', lag)
                    for ii in lag:

                        if ii < 0:
                            result.append(pearsonr(x[:ii], y[-ii:]))
                        elif ii == 0:
                            result.append(pearsonr(x, y))
                        elif ii > 0:
                            result.append(pearsonr(x[ii:], y[:-ii]))

                    df_result['{0}x{1}'.format(x_tag, y_tag)] = [r[0] for r in result]
                    col_name = x_tag + '_x_' + y_tag
                    df_result_all[col_name] = [r[0] for r in result]

                already_correlated.append(x_tag)
            df_summary = pd.DataFrame()
            # index = df_result_all.columns.names
            # cols = ['lag', 'correlation']
            # df_summary.columns = cols
            # df_summary.index = index
            #print('max.....\n', [df_result_all.max(axis=0), df_result_all.idxmax(axis=0)])
            df_summary['lag'] = df_result_all.idxmax(axis=0)
            df_summary['correlation'] = df_result_all.max(axis=0)
            #print(df_summary)


            #print(df_result_all)

            FunctionBlock.save_results(self, df=df_result_all, statistics=True, plot=False)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df_result_all,
                    FunctionBlock.getFullPath(self, 'summary_lag_corr'): df_summary}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


