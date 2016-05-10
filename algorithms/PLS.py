import pandas as pd
import numpy as np
import collections as coll
import sys
import traceback
import ast

from sklearn.cross_decomposition import PLSRegression
from FunctionBlock import FunctionBlock

def concat_dfs(dfs):

    all_timestamp = True

    for df in dfs:
        if df.index.values.dtype != np.dtype('datetime64[ns]'):
            all_timestamp = False
            break

    if not all_timestamp:
        for i, df in enumerate(dfs):
            dfs[i] = df.reset_index(drop=True)

    return pd.concat(dfs, axis=1), all_timestamp

def unique_col_name(dfs):

    num_of_df = len(dfs)
    if num_of_df > 1:

        for i in range(0,num_of_df-1):
            j = 0
            col_list_1 = dfs[i].columns
            for icnt in range(0,num_of_df):
                for tag in col_list_1:
                    tagcnt = 0


                    if icnt == i:
                        continue
                    else:
                        listnames = dfs[icnt].columns
                        if tag in listnames:
                            tagcnt+=i
                            new_tag = tag + "_" + str(tagcnt)
                            dfs[i] = dfs[i].rename(columns={tag: new_tag})
    return dfs

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



            if 'x_test' in self.input_connectors:
                x_test_df = results_table[self.input_connectors['x_test'][0]]
            else:
                x_test_df = []

            if 'y_test' in self.input_connectors:
                y_test_df = results_table[self.input_connectors['y_test'][0]]
            else:
                y_test_df = []

            n_components = int(self.parameters['NumberComponents'])

            scale = self.parameters['Scale']

            pls_model = PLSRegression(n_components, ast.literal_eval(scale))  #n_components=set as parameter in block definitionl

            x_values = x_df.values


            y_values = y_df.values


            pls_model.fit(x_values, y_values)


            y_prediction = pls_model.predict(x_values)


            r2 = pls_model.score(x_values, y_values)

            ss_error_residual = 0


            for x in range(len(y_values)):
                ss_error_residual += (y_values[x] - y_prediction[x])**2

            pls_result = coll.OrderedDict()

            if ('x_test' and 'y_test') in self.input_connectors:
                x_test_values = x_test_df.values
                y_test_values = y_test_df.values
                y_test_pred = pls_model.predict(x_test_values)
                r2_test = pls_model.score(x_test_values, y_test_values)
                ss_error_residual_test = 0
                for x in range(len(y_test_values)):
                    ss_error_residual_test += (y_test_values[x] - y_test_pred[x])**2
                df_y_test_pred = pd.DataFrame(y_test_pred, columns=['y_test_pred'])
                pls_result['MSE-test'] = float(ss_error_residual_test) / len(y_test_values)
            else:
                r2_test = 0
                ss_error_residual_test = 0
                df_y_test_pred = []
                pls_result['MSE-test'] = 0



            coefficients = [x[0] for x in pls_model.coefs]

            pls_coefficient = coll.OrderedDict(zip(x_df.columns.values, coefficients))

            model = list(pls_coefficient.items())

            x_mean, y_mean, x_std, y_std = x_y_mean_std(x_values, y_values)


            df_y_prediction = pd.DataFrame(y_prediction, columns=['y_pred'])

            #print('df y test pred.......\n', df_y_test_pred)
            #print('df y prediction.......\n', df_y_prediction)


            #pls_result = coll.OrderedDict()
            pls_result['Coefficients'] = model
            pls_result['R-Squared'] = r2
            pls_result['R-Squared-test'] = r2_test
            pls_result['MSE'] = float(ss_error_residual) / len(y_values)

            dfs = []
            dfs.append(y_df)
            dfs.append(df_y_prediction)
            if ('x_test' and 'y_test') in self.input_connectors:
                dfs.append(y_test_df)
                dfs.append(df_y_test_pred)

            dfs2 = unique_col_name(dfs)
            merge = concat_dfs(dfs2)

            #print('dfs........\n', dfs)

            merge2 = merge[0].copy(deep=True)

            for idx in range(1, len(dfs)):
                df = dfs[idx]
                df2 = df.reindex(dfs[0].index) #, method='ffill')
                for column in df.columns:
                    merge2[column] = df2[column]

            #print('merged......\n', merge)




            results_df = merge2

            # print('model.....', model)
            # print('x_mean....\n', x_mean)
            # print('y_mean....\n', y_mean)
            # print('x_std....\n', x_std)
            # print('y_std....\n', y_std)
            model_values = [x[1] for x in model]
            #coef_list = [x[0] for x in model]
            #print('model_values = \n',  model_values)
            model_values_df = pd.DataFrame(model_values, columns=['coef'])
            #model_values_df.append(pd.DataFrame(coef_list, columns=['param']))

            #print('model values df......\n', model_values_df)

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

            return {FunctionBlock.getFullPath(self, 'model'): model_values_df,
                    FunctionBlock.getFullPath(self, 'y_comp'): results_df}

        except:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            traceback.print_exc(file=sys.stderr)
