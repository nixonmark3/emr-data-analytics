import pandas as pd

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


class LoadDB(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        print('executing {0}'.format(self.name))
        df = pd.read_csv(self.parameters['Data Set'], parse_dates=True, index_col=0)
        return {'{0}/{1}'.format(self.name, 'out'): df}


class Columns(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        print('executing {0}'.format(self.name))
        df = results_table[self.input_connectors['in'][0]]  # a columns block can only have one wire in
        df = df[self.parameters['Columns']]
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

