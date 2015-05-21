import collections as coll
import pandas as pd

from FunctionBlock import FunctionBlock


class PLSTest(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')
            x_test_df = results_table[self.input_connectors['x'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'y')
            y_test_df = results_table[self.input_connectors['y'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'model')
            pls_model = results_table[self.input_connectors['model'][0]]

            y_test_values = y_test_df.values

            y_prediction = pls_model.predict(x_test_df.values)

            ss_error_total = sum(y_test_values**2)

            ss_error_residual = 0

            for x in range(len(y_test_values)):
                ss_error_residual += (y_test_values[x] - y_prediction[x])**2

            r2 = 1 - ss_error_residual/ss_error_total

            pls_test_result = coll.OrderedDict()
            pls_test_result['r squared'] = r2[0]

            data_dict = {'y vals': list(y_test_values[:, 0]), 'y pred': list(y_prediction[:, 0])}

            FunctionBlock.save_results(self, df=(pd.DataFrame(data_dict)), statistics=True, plot=True, results=pls_test_result)

            FunctionBlock.report_status_complete(self)

            return {'{0}'.format(self.unique_name): None}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
