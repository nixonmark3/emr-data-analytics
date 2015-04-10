import pandas as pd
import time
import sys

from abc import ABCMeta, abstractmethod


class FunctionBlock():

    def __init__(self, name):
        __metaclass__ = ABCMeta
        self.name = name
        self.input_connectors = {}
        self.parameters = {}

    @abstractmethod
    def execute(self, results_table):
        pass

    def report_status_executing(self):
        print('{0},{1}'.format(self.name, '2'))
        sys.stdout.flush()

    def report_status_complete(self):
        print('{0},{1}'.format(self.name, '3'))
        sys.stdout.flush()

class LoadDB(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        FunctionBlock.report_status_executing(self)
        df = pd.read_csv(self.parameters['Data Set'], parse_dates=True, index_col=0)
        time.sleep(4)
        FunctionBlock.report_status_complete(self)
        return {'{0}/{1}'.format(self.name, 'out'): df}


class Columns(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        FunctionBlock.report_status_executing(self)
        df = results_table[self.input_connectors['in'][0]]  # a columns block can only have one wire in
        df = df[self.parameters['Columns']]
        time.sleep(3)
        FunctionBlock.report_status_complete(self)
        return {'{0}/{1}'.format(self.name, 'out'): df}


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

