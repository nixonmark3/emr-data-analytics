import sys
import numpy as np
import ast

from FunctionBlock import FunctionBlock


class CenterNormal(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)
            
            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df_scale = results_table[self.input_connectors['in'][0]]

            subtract_mean = ast.literal_eval(self.parameters['Subtract Mean'])
            divide_by_std = ast.literal_eval(self.parameters['Divide Std'])

            df_mean = np.mean(df_scale, axis=0)
            df_std = np.std(df_scale, axis=0)

            if subtract_mean and divide_by_std:
                df_scale = (df_scale - df_mean) / df_std
            elif subtract_mean:
                df_scale = df_scale - df_mean
            elif divide_by_std:
                df_scale = df_scale / df_std

            FunctionBlock.save_results(self, df=df_scale, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.unique_name, 'out'): df_scale}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


