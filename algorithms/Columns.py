import sys
import pandas as pd

from FunctionBlock import FunctionBlock


class Columns(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            if len(self.input_connectors['in']) != 1:
                # this block can only have one wire connected
                FunctionBlock.report_status_failure(self)
                FunctionBlock.save_results(self)
                print("Too many wires connected to in connector!", file=sys.stderr)

            # get the incoming data frame
            df = results_table[self.input_connectors['in'][0]]

            # save available column information
            self.results['Columns'] = list(df.columns.values)

            # get the configured columns
            columns = self.parameters['Columns']

            if len(columns) == 0:
                # the columns have not been configured
                FunctionBlock.save_results(self)
                FunctionBlock.report_status_configure(self)
                return {'{0}/{1}'.format(self.name, 'out'): None}

            # Now we are ready to run the algorithm
            df = df[columns]

            # save block statistics
            self.results['Statistics'] = df.describe().to_dict()

            # save results and report block state is good
            FunctionBlock.save_results(self, plot_df=df, plot=True)
            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): df}

        except Exception as err:
            # save results and report block state is bad
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
