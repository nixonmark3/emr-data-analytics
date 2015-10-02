import sys
import pandas as pd
import ast

from FunctionBlock import FunctionBlock


class FastSlowFilt(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            fast_filt = self.parameters['Fast_Filter']
            slow_filt = self.parameters['Slow_Filter']
            suffix = self.parameters['AddSuffix']

            filt_df = df


            len_df = len(df.index)
            for tag in df.columns:
                for i in range(1, len_df):
                    if df[tag].iloc[i] < filt_df[tag].iloc[i-1]:
                        filt = slow_filt
                    else:
                        filt = fast_filt
                    filt_df[tag].iloc[i] = df[tag].iloc[i] * (1-filt) + filt * filt_df[tag].iloc[i-1]

                new_tag = tag + suffix
                filt_df = filt_df.rename(columns={tag: new_tag})

            FunctionBlock.save_results(self, df=filt_df, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): filt_df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

