import sys
import pandas as pd

from FunctionBlock import FunctionBlock


class Columns(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            self.results['Columns'] = list(df.columns.values)

            columns = self.parameters['Columns']

            if len(columns) == 0:
                FunctionBlock.save_results(self)
                FunctionBlock.report_status_configure(self)
                return {'{0}/{1}'.format(self.unique_name, 'out'): None}

            df = df[columns]

            FunctionBlock.save_results(self, df=df, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.unique_name, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
