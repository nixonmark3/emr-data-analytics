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
