import sys
import pandas as pd
import ast

from FunctionBlock import FunctionBlock


class Scale(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            scale_factor = self.parameters['Scale_Factor']
            scale_0to1 = ast.literal_eval(self.parameters['Scale_0to1'])

            scale_df = df.copy()

            if scale_0to1:
                for tag in scale_df.columns:
                    scale_df[tag] = scale_factor * ((scale_df[tag] - scale_df[tag].min()) / (scale_df[tag].max() - scale_df[tag].min()))
            else:
                for tag in scale_df.columns:
                    scale_df[tag] = scale_factor * scale_df[tag]

            FunctionBlock.save_results(self, df=scale_df, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): scale_df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

