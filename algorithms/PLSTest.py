import collections as coll
import pandas as pd
import numpy as np
import sklearn.preprocessing as skpre
import sys

from FunctionBlock import FunctionBlock

# convert a dictionary of features to an array of doubles (in order)
def dictionaryToArray(dict, keys):
    result = []
    print('dict type = ', type(dict))
    print('keys in routine \n', keys)
    #print('tpe of keyes = ', keys.)
    for key in keys:   #.split(","):
        print('single key = ', key)
        print('dict = ', key, dict[key])
        result.append(dict[key])
    return result

def dotProduct(v1, v2):
    print('size v1 =', len(v1), )
    print('size v2 =', len(v2))
    print('v1.......\n', v1)
    print('v2.......\n', v2)
    print('zip.....\n', list(zip(v1,v2)))
    print('sum = ', sum([a*b for a,b in zip(v1, v2)]) )
    return [sum([a*b for a,b in zip(v1, v2)])]

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
            #print('pls_model = \n', pls_model)
            coef_dict = {}
            for items in pls_model:
                coef_dict[items[0]] = items[1]

            #print('type coef_dict = ', type(coef_dict))
            #print('coef_dict \n', coef_dict)


            key_list = x_test_df.columns.values.tolist()
            #print('key_list \n', key_list)

            coef = dictionaryToArray(coef_dict, key_list)
            #print('coef = \n', coef)

            y_test_values = y_test_df.values
            # print('y test values........\n', y_test_values)
            # print('size of y test = ', y_test_values.size)

            x_test_values = x_test_df.values
            print('x test values.....\n', x_test_values)

            x_scaled = skpre.scale(x_test_values)
            y_scaled = skpre.scale(y_test_values)

            y_prediction = np.dot(coef, x_scaled.T)
            # print('y_prediction = ', y_prediction)
            # print('y_std = ', y_test_df.std())
            # print('y_mean = ', y_test_df.mean())

            # ss_error_total = sum((y_test_values - y_test_values.mean())**2)
            #
            # ss_error_residual = 0
            #
            # for x in range(len(y_test_values)):
            #     ss_error_residual += (y_test_values[x] - y_prediction[x])**2
            #
            # r2 = 1 - ss_error_residual/ss_error_total
            #
            # pls_test_result = coll.OrderedDict()
            # pls_test_result['r squared'] = r2[0]
            #
            # data_dict = {'y vals': list(y_test_values[:, 0]), 'y pred': list(y_prediction[:, 0])}

            #FunctionBlock.save_results(self, df=(pd.DataFrame(data_dict)), statistics=True, plot=True, results=pls_test_result)

            FunctionBlock.report_status_complete(self)

            return #{FunctionBlock.getFullPath(self, 'ycomp'): (pd.DataFrame(data_dict))}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
