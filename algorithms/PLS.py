import pandas as pd
import collections as coll
import sys
import traceback
import ast

from sklearn.cross_decomposition import PLSRegression
from FunctionBlock import FunctionBlock


def x_y_mean_std(x, y):
    x_mean = x.mean(axis=0)
    y_mean = y.mean(axis=0)

    x_std = x.std(axis=0, ddof=1)
    x_std[x_std == 0.0] = 1.0

    y_std = y.std(axis=0, ddof=1)
    y_std[y_std == 0.0] = 1.0

    return x_mean.tolist(), y_mean.tolist(), (x_std.tolist()), (y_std.tolist())


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

            n_components = int(self.parameters['NumberComponents'])
            print('number comp = ', n_components)
            scale = self.parameters['Scale']
            print('Scale = ', scale)
            print('scale tupe = ', type(scale))

            pls_model = PLSRegression(n_components, ast.literal_eval(scale))  #n_components=set as parameter in block definitionl
            print('scale_2 = ', scale)

            x_values = x_df.values

            y_values = y_df.values
            print('y_df mean = ', y_df.mean())

            pls_model.fit(x_values, y_values)

            y_prediction = pls_model.predict(x_values)
            print('y pred.....\n', y_prediction)

            r2 = pls_model.score(x_values, y_values)

            print('rsq from pls_model = ', r2)

            #ss_error_total = ((y_values - y_df.mean())**2).sum()

            ss_error_residual = 0

            for x in range(len(y_values)):
                ss_error_residual += (y_values[x] - y_prediction[x])**2


            #r2 = 1 - ss_error_residual/ss_error_total

            coefficients = [x[0] for x in pls_model.coefs]

            pls_coefficient = coll.OrderedDict(zip(x_df.columns.values, coefficients))

            model = list(pls_coefficient.items())
            x_mean, y_mean, x_std, y_std = x_y_mean_std(x_values, y_values)

            pls_result = coll.OrderedDict()
            pls_result['Coefficients'] = model
            pls_result['R-Squared'] = r2
            pls_result['MSE'] = float(ss_error_residual) / len(y_values)

            data_dict = {'Y-Values': list(y_values[:, 0]), 'Y-Prediction': list(y_prediction[:, 0])}

            results_df = pd.DataFrame(data_dict)

            model_values = [x[1] for x in model]

            FunctionBlock.add_statistics_result(self, results_df)
            FunctionBlock.add_plot_result(self, results_df)
            FunctionBlock.add_general_results(self, pls_result)
            FunctionBlock.add_persisted_connector_result(self, 'model', model_values)
            FunctionBlock.add_persisted_connector_result(self, 'x_mean', x_mean)
            FunctionBlock.add_persisted_connector_result(self, 'x_std', x_std)
            FunctionBlock.add_persisted_connector_result(self, 'y_mean', y_mean)
            FunctionBlock.add_persisted_connector_result(self, 'y_std', y_std)
            FunctionBlock.save_all_results(self)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'model'): model,
                    FunctionBlock.getFullPath(self, 'y_comp'): results_df}

        except:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            traceback.print_exc(file=sys.stderr)
