import pandas as pd
import collections as coll
import sys

from sklearn.lda import LDA
from FunctionBlock import FunctionBlock




class LinDiscA(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')
            x_df = results_table[self.input_connectors['x'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'y')
            y_df = results_table[self.input_connectors['y'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'test_x')
            test_x_df = results_table[self.input_connectors['test_x'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'test_y')
            test_y_df = results_table[self.input_connectors['test_y'][0]]

            lda_model = LDA()

            x_values = x_df.values

            y_values = y_df.values

            x_test_values = test_x_df.values

            y_test_values = test_y_df.values


            lda_model.fit(x_values, y_values)


            y_train_prediction = lda_model.predict(x_values) * 1.1

            y_test_prediction = lda_model.predict(x_test_values) * 1.1

            mean_accuracy_train = lda_model.score(x_values, y_values)

            mean_accuracy_test = lda_model.score(x_test_values, y_test_values)



            lda_result = coll.OrderedDict()
            lda_result['Mean Accuracy Train'] = mean_accuracy_train
            lda_result['Mean Accuracy Test'] = mean_accuracy_test
            # lda_result['Coefficients'] = model


            data_dict = {'Y-Values': list(y_values[:,0]), 'Y-Prediction': list(y_train_prediction)}


            data_dict2 = {'Y_test_values': list(y_test_values[:,0]), 'Y_test_pred': list(y_test_prediction)}


            results_df = pd.DataFrame(data_dict)

            results_df2 = pd.DataFrame(data_dict2)

            #FunctionBlock.add_statistics_result(self, results_df)
            #.add_plot_result(self, results_df)
            FunctionBlock.add_general_results(self, mean_accuracy_train)
            FunctionBlock.add_general_results(self, mean_accuracy_test)
            #FunctionBlock.add_persisted_connector_result(self, 'y_train', y_train_prediction)
            #FunctionBlock.add_persisted_connector_result(self, 'y_test', y_test_prediction)
            # FunctionBlock.add_persisted_connector_result(self, 'x_std', x_std)
            # FunctionBlock.add_persisted_connector_result(self, 'y_mean', y_mean)
            # FunctionBlock.add_persisted_connector_result(self, 'y_std', y_std)
            FunctionBlock.save_all_results(self)

            FunctionBlock.report_status_complete(self)
            print('after complete')

            return {FunctionBlock.getFullPath(self, 'y_train_comp'): results_df,
                    FunctionBlock.getFullPath(self, 'y_test_comp'): results_df2}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
