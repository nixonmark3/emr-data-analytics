import sys
import pymongo
import gridfs
import matplotlib.pyplot as plt
import os

from abc import ABCMeta, abstractmethod


class FunctionBlock():

    def __init__(self, name):
        __metaclass__ = ABCMeta
        self.name = name
        self.input_connectors = {}
        self.parameters = {}
        self.results = {'name': self.name}
        self.blockResults = {}

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

    def save_results(self, plot_df=None, plot=False):


        connection = pymongo.MongoClient()
        db = connection['emr-data-analytics-studio']

        if plot:
            # Generate plot
            ax = plot_df.plot(legend=False)
            fig = ax.get_figure()
            fig.savefig('{0}.png'.format(self.name))
            plt.close(fig)

            # Save plot using GridFS
            with open('{0}.png'.format(self.name), 'rb') as f:
                png = f.read()

            fs = gridfs.GridFS(db)
            stored = fs.put(png, filename=self.name)
            print(stored)

            # Remove the generate plot file
            os.remove('{0}.png'.format(self.name))

            self.blockResults['Plot'] = { 'name': self.name }

        self.results['Results'] = self.blockResults

        results = db['results']
        results.update({'name': self.name}, self.results, upsert=True)