__author__ = 'noelbell'

import sys
import pandas as pd
import numpy as np
import collections

from FunctionBlock import FunctionBlock

def rolling_mad(data_frame, mw):
    """
    :param data_frame: n*1 pandas data_frame
    :param mw: integer; moving window size
    :return robust estimation of median absolute deviation
    """
    df = pd.DataFrame(data_frame)
    n = data_frame.shape[0]
    initial = np.zeros((n, 1))
    initial[:] = np.nan


    mad = pd.DataFrame(data=initial, columns=['value'])

    mad.index = data_frame.index


    for i in range(mw, n):
        med = df[i-mw+1:i].median()
        diff = df[i-mw+1:i] - med
        mad.value[i] = np.median(abs(diff - med))
    return mad

class Hampel(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)


    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            mw = self.parameters['MovingWindow']
            g = self.parameters['g-Sigma']

            variable_name = df.columns.values
            clean_data = df.copy()
            temp_index = df.index

            if mw <= 0 or g <= 0:
                # warnings.simplefilter("always", ImportWarning)
                # warnings.warn('Wrong parameter setting! Reset: mw=20, g =3 ', ImportWarning)
                mw = 20
                g = 3

            num_obs = int(clean_data.shape[0])
            num_var = int(clean_data.shape[1])

            temp_data_list = []

            for j in range(0, num_var):

                temp_data = pd.DataFrame(index=temp_index, columns=['value','y0','s0','upper_bound','lower_bound','indicator','outliers'])

                current_col = variable_name[j]

                temp_data['value'] = pd.DataFrame(df[current_col])
                temp_data['y0'] = pd.rolling_median(temp_data.value, mw)

                temp_mad = rolling_mad(temp_data.value, mw)

                temp_data['s0'] = pd.rolling_std(temp_data, mw) #.value, mw)

                temp_data['upper_bound'] = temp_data.y0 + g*temp_mad.value
                temp_data['lower_bound'] = temp_data.y0 - g*temp_mad.value

                temp_data['indicator'] = ((temp_data.value > temp_data.upper_bound) | (temp_data.value < temp_data.lower_bound))

                temp_data['outliers'] = temp_data.apply(lambda x: x.value if not x.indicator else np.NaN, axis=1)

                clean_data[current_col] = temp_data['outliers']

                temp_data_list.append(temp_data)

            results = collections.OrderedDict()
            results['MovingWindow'] = mw
            results['g-Sigma'] = g

            FunctionBlock.save_results(self, df=clean_data, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): clean_data}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)





