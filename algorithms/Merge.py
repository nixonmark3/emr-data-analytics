
import pandas as pd
import sys

from FunctionBlock import FunctionBlock


class Merge(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            dfs = []
            for input_wire in self.input_connectors['in']:
                dfs.append(results_table[input_wire])

            merge = dfs[0].copy(deep=True)

            for idx in range(1, len(dfs)):
                df = dfs[idx]
                df2 = df.reindex(dfs[0].index, method='ffill')
                for column in df.columns:
                    merge[column] = df2[column]

            FunctionBlock.save_results(self, df=merge, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): merge}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
