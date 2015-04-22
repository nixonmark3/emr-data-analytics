import sys
import pymongo

from abc import ABCMeta, abstractmethod


class FunctionBlock():

    def __init__(self, name):
        __metaclass__ = ABCMeta
        self.name = name
        self.input_connectors = {}
        self.parameters = {}
        self.results = {'name': self.name}

    @abstractmethod
    def execute(self, results_table):
        pass

    def report_status_configure(self):
        print('{0},{1}'.format(self.name, '0'))
        sys.stdout.flush()

    def report_status_executing(self):
        print('{0},{1}'.format(self.name, '2'))
        sys.stdout.flush()

    def report_status_complete(self):
        print('{0},{1}'.format(self.name, '3'))
        sys.stdout.flush()

    def report_status_failure(self):
        print('{0},{1}'.format(self.name, '4'))
        sys.stdout.flush()

    def save_results(self):
        connection = pymongo.MongoClient()
        db = connection['emr-data-analytics-studio']
        results = db['results']
        results.update({'name': self.name}, self.results, upsert=True)