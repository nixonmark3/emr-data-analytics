import numpy as np
import pandas as pd
import collections as coll
import sys

from sklearn.cross_decomposition import PLSRegression
from FunctionBlock import FunctionBlock


class PLS(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')
            x_df = results_table[self.input_connectors['x'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'y')
            y_df = results_table[self.input_connectors['y'][0]]

            pls_model = PLSRegression(n_components=x_df.shape[1])

            x_values = x_df.values

            y_values = y_df.values

            pls_model.fit(x_values, y_values)

            y_prediction = pls_model.predict(x_values)

            ss_error_total = sum(y_values**2)

            ss_error_residual = 0

            for x in range(len(y_values)):
                ss_error_residual += (y_values[x] - y_prediction[x])**2

            r2 = 1 - ss_error_residual/ss_error_total

            coefficients = [x[0] for x in pls_model.coefs]

            pls_coefficient = coll.OrderedDict(zip(x_df.columns.values, coefficients))

            model = list(pls_coefficient.items())

            pls_result = coll.OrderedDict()
            pls_result['coefficients'] = model
            pls_result['r squared'] = r2[0]

            data_dict = {'y vals': list(y_values[:, 0]), 'y pred': list(y_prediction[:, 0])}

            results_df = pd.DataFrame(data_dict)

            FunctionBlock.add_statistics_result(self, results_df)
            FunctionBlock.add_plot_result(self, results_df)
            FunctionBlock.add_general_results(self, pls_result)
            FunctionBlock.add_persisted_connector_result(self, 'model', model)
            FunctionBlock.save_all_results(self)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'model'): pls_model,
                    FunctionBlock.getFullPath(self, 'ycomp'): (pd.DataFrame(data_dict))}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
