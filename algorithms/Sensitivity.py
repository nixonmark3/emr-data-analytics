__author__ = "Shu Xu, Noel Bell"
__date__ = "04/16/2015"
__version__ = "1.0"
__maintainer__ = "Noel Bell"
__email__ = "noel.bell@emerson.com"
__company__ = " Emerson process management inc."

##======================================================================================================================
# Main function
##======================================================================================================================

import numpy as np
import pandas as pd
import collections as coll
import sys

from sklearn.cross_decomposition import PLSRegression
from FunctionBlock import FunctionBlock


class Sensitivity(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        file_stderr = sys.stderr
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')

            FunctionBlock.check_connector_has_one_wire(self, 'y')

            x_data_frame = results_table[self.input_connectors['x'][0]]
            y_data_frame = results_table[self.input_connectors['y'][0]]

            x_data_frame = x_data_frame.sort()

            x_variable_name = x_data_frame.columns.values

            num_input = x_data_frame.shape[1]

            sensitivity_val_list = []
            Y_list = []
            Y_pred_list = []
            R2_list = []
            pls_model = PLSRegression(n_components=num_input)
            Ydf_filled = y_data_frame.fillna(method='ffill')
            Y = Ydf_filled.values
            Xdf_filled = x_data_frame.fillna(method='ffill')
            X = Xdf_filled.values

            pls_model.fit(X, Y)
            Y_list.append(Y)

            Y_pred_list.append(pls_model.predict(X))



            #R2_list.append(r2)
            R2_list = [.5]
            coeffs = pls_model.coefs

            scale_coeffs = coeffs # /sum(abs(coeffs))
            scoeff = [x[0] for x in scale_coeffs]

            sensitivity_val_list.append(scale_coeffs.T)

            sensitivity_val = coll.OrderedDict(zip(x_variable_name, scoeff))

            sensitivity_result = coll.OrderedDict()
            sensitivity_result['scaled_coef'] = list(sensitivity_val.items())
            sensitivity_result['sensitivity_R2'] = R2_list

            FunctionBlock.save_results(self, results=sensitivity_result)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'obj'): pls_model,
                    FunctionBlock.getFullPath(self, 'coef'): coeffs,
                    FunctionBlock.getFullPath(self, 'r2'): R2_list}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)