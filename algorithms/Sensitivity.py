import collections as coll
import sys
import pandas as pd

from sklearn.cross_decomposition import PLSRegression
from FunctionBlock import FunctionBlock


class Sensitivity(FunctionBlock):

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

            Y_pred = pls_model.predict(x_values)

            ss_error_total = sum(y_values**2)

            ss_error_residual = 0

            for x in range(len(y_values)):
                ss_error_residual += (y_values[x] - Y_pred[x])**2

            r2 = 1 - ss_error_residual/ss_error_total

            coefficients = [x[0] for x in pls_model.coefs]

            model_scale_coefficients = pls_model.coefs/sum(abs(pls_model.coefs))

            scale_coefficients = [x[0] for x in model_scale_coefficients]

            sensitivity_val = coll.OrderedDict(zip(x_df.columns.values, scale_coefficients))

            sensitivity_coefficients = coll.OrderedDict(zip(x_df.columns.values, coefficients))

            sensitivity_result = coll.OrderedDict()
            sensitivity_result['scaled coefficients'] = list(sensitivity_val.items())
            sensitivity_result['coefficients'] = list(sensitivity_coefficients.items())
            sensitivity_result['r squared'] = r2[0]

            data_dict = {'y vals': list(y_values[:, 0]), 'y pred': list(Y_pred[:, 0])}

            FunctionBlock.save_results(self, df=(pd.DataFrame(data_dict)), statistics=True, plot=True, results=sensitivity_result)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'model'): pls_model,
                    FunctionBlock.getFullPath(self, 'coefs'): coefficients,
                    FunctionBlock.getFullPath(self, 'r2'): r2[0]}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)