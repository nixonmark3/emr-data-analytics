import sys
import pandas as pd

from FunctionBlock import FunctionBlock


class Shift(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            df = results_table[self.input_connectors['in'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'delay')
            time_delays = results_table[self.input_connectors['delay'][0]]

            saved_index = df.index
            shifted_df = time_shift(df, time_delays)
            shifted_df.index = saved_index

            FunctionBlock.save_results(self, df=shifted_df, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): shifted_df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


def time_shift(df, delays):
    df_out = df.copy(deep=True)
    for key in df.columns.values:
        df_out[key] = df_out[key].shift(delays[key].values)
    return df_out