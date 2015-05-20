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

            shifted_df = pd.DataFrame()

            # FunctionBlock.save_results(self, df=shifted_df, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): shifted_df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

