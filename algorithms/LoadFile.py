import sys
import pandas as pd
import ast
import Wranglers

from FunctionBlock import FunctionBlock


class LoadFile(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            filename = self.parameters['Filename']

            file_type = self.parameters['File Type']

            plot = ast.literal_eval(self.parameters['Plot'])

            time_series = ast.literal_eval(self.parameters['Time Series'])

            if file_type == 'CSV':
                df =  Wranglers.import_csv(filename, time_series)
            elif file_type == 'FF3':
                df = Wranglers.import_ff3(filename)
            elif file_type == 'CDA':
                df = Wranglers.import_cda(filename)
            else:
                FunctionBlock.report_status_failure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}

            FunctionBlock.save_results(self, df=df, statistics=True, plot=plot)
            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
