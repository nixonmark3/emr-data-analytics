__author__ = 'noelbell'

import sys
import pandas as pd
import numpy as np
import collections

from FunctionBlock import FunctionBlock


class ThreeSigma(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            mw = self.parameters['MovingWindow']
            g = self.parameters['g-Sigma']

            variable_name = df.columns.values
            clean_data = df.copy()

            if mw <= 0 or g <= 0:
                # warnings.simplefilter("always", ImportWarning)
                # warnings.warn('Wrong parameter setting! Reset: mw=20, g =3 ', ImportWarning)
                mw = 20
                g = 3

            num_obs = int(clean_data.shape[0])
            num_var = int(clean_data.shape[1])

            temp_data_list = []

            for j in range(0, num_var):
                temp_data = pd.DataFrame()
                temp_data['value'] = pd.DataFrame(df[variable_name[j]])
                temp_data['y0'] = pd.rolling_mean(temp_data.value, mw)
                temp_data['s0'] = pd.rolling_std(temp_data.value, mw)
                temp_data['upper_bound'] = temp_data.y0 + g*temp_data.s0
                temp_data['lower_bound'] = temp_data.y0 - g*temp_data.s0
                temp_data['indicator'] = ((temp_data.value > temp_data.upper_bound) | (temp_data.value < temp_data.lower_bound))
                temp_data['outliers'] = temp_data.apply(lambda x: x.value if x.indicator else np.NaN, axis=1)

                col = clean_data[variable_name[j]]

                for i in range(num_obs):
                    if temp_data.indicator[i]:
                        col.iat[i] = temp_data.y0[i]

                clean_data[variable_name[j]] = col

                temp_data_list.append(temp_data)

            results = collections.OrderedDict()
            results['MovingWindow'] = mw
            results['g-Sigma'] = g

            FunctionBlock.save_results(self, df=clean_data, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): clean_data}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




