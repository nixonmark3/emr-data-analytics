import collections as coll
import pandas as pd
import sys

from FunctionBlock import FunctionBlock


class LDATest(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')
            x_test_df = results_table[self.input_connectors['x'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'y')
            y_test_df = results_table[self.input_connectors['y'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'LDAmodel')
            lda_model = results_table[self.input_connectors['LDAmodel'][0]]

            y_test_values = y_test_df.values

            y_prediction = lda_model.predict(x_test_df.values)

            ss_error_total = sum(y_test_values**2)

            ss_error_residual = 0

            for x in range(len(y_test_values)):
                ss_error_residual += (y_test_values[x] - y_prediction[x])**2

            r2 = 1 - ss_error_residual/ss_error_total

            lda_test_result = coll.OrderedDict()
            lda_test_result['r squared'] = r2[0]

            data_dict = {'y vals': list(y_test_values[:, 0]), 'y pred': list(y_prediction[:, 0])}

            FunctionBlock.save_results(self, df=(pd.DataFrame(data_dict)), statistics=True, plot=True, results=lda_test_result)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'ycomp'): (pd.DataFrame(data_dict))}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
