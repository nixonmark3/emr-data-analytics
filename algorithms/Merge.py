import time
import pandas as pd

from FunctionBlock import FunctionBlock


class Merge(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        print('executing {0}'.format(self.name))

        dfs = []

        for input_wire in self.input_connectors['in']:
            dfs.append(results_table[input_wire])

        merge = dfs[0].copy(deep=True)

        for idx in range(1, len(dfs)):
            df = dfs[idx]
            df2 = df.reindex(dfs[0].index, method='ffill')
            for column in df.columns:
                merge[column] = df2[column]

        return merge
