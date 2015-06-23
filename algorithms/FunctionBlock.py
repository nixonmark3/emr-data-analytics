import sys
import pymongo
import gridfs
import matplotlib.pyplot as plt
import os
import collections

from abc import ABCMeta, abstractmethod

studio_db_name = 'emr-data-analytics-studio'

class FunctionBlock():

    def __init__(self, name, unique_name):
        __metaclass__ = ABCMeta
        self.name = name
        self.unique_name = unique_name
        self.input_connectors = {}
        self.parameters = {}
        self.results = {'name': self.unique_name, 'friendly_name': self.name, 'Results': collections.OrderedDict()}

    @abstractmethod
    def execute(self, results_table):
        pass

    def report_status_configure(self):
        print('{0},{1}'.format(self.unique_name, '0'))
        sys.stdout.flush()

    def report_status_executing(self):
        print('{0},{1}'.format(self.unique_name, '2'))
        sys.stdout.flush()

    def report_status_complete(self):
        print('{0},{1}'.format(self.unique_name, '3'))
        sys.stdout.flush()

    def report_status_failure(self):
        print('{0},{1}'.format(self.unique_name, '4'))
        sys.stdout.flush()

    def save_results(self, df=None, statistics=False, plot=False, results=None):
        connection = pymongo.MongoClient()
        db = connection[studio_db_name]

        block_results = collections.OrderedDict()

        if statistics:
            block_results['Statistics'] = list(df.describe().to_dict().items())

        if plot:
            # get a connection to GridFS
            fs = gridfs.GridFS(db)

            # delete the old plot if it exists
            if fs.exists(filename=self.unique_name):
                fp = fs.get_last_version(self.unique_name)
                fs.delete(fp._id)

            # Generate plot
            ax = df.plot(legend=True)
            ax.legend(loc='best', fancybox=True, shadow=True, prop={'size': 7})
            ax.tick_params(axis='both', which='major', labelsize=8)
            fig = ax.get_figure()
            fig.savefig('{0}.png'.format(self.unique_name), dpi=100)
            plt.close(fig)

            # Save plot using GridFS
            with open('{0}.png'.format(self.unique_name), 'rb') as f:
                png = f.read()

            stored = fs.put(png, filename=self.unique_name)

            # Remove the generate plot file
            os.remove('{0}.png'.format(self.unique_name))

            block_results['Plot'] = {'name': self.unique_name, 'ID': stored}

        if results:
            block_results['Results'] = results

        self.results['Results'] = block_results

        results = db['results']
        results.update({'name': self.unique_name}, self.results, upsert=True)

        connection.close()

    def check_connector_has_one_wire(self, connector_name):
        if len(self.input_connectors[connector_name]) != 1:
            FunctionBlock.report_status_failure(self)
            FunctionBlock.save_results(self)
            print("Too many wires connected to in connector!", file=sys.stderr)
            return {'{0}'.format(self.unique_name): None}

    def getFullPath(self, parameter_name):
        return '{0}/{1}'.format(self.unique_name, parameter_name)

    def add_persisted_connector_result(self, name, value):
        self.results['Results'][name] = value

    def add_general_results(self, results):
        self.results['Results']['Results'] = results

    def add_statistics_result(self, df):
        self.results['Results']['Statistics'] = list(df.describe().to_dict().items())

    def add_plot_result(self, df):
        connection = pymongo.MongoClient()
        db = connection[studio_db_name]

        fs = gridfs.GridFS(db)

        if fs.exists(filename=self.unique_name):
            fp = fs.get_last_version(self.unique_name)
            fs.delete(fp._id)

        ax = df.plot(legend=True)
        ax.legend(loc='best', fancybox=True, shadow=True, prop={'size': 7})
        ax.tick_params(axis='both', which='major', labelsize=8)
        fig = ax.get_figure()
        fig.savefig('{0}.png'.format(self.unique_name), dpi=100)
        plt.close(fig)

        with open('{0}.png'.format(self.unique_name), 'rb') as f:
            png = f.read()

        stored = fs.put(png, filename=self.unique_name)

        os.remove('{0}.png'.format(self.unique_name))

        self.results['Results']['Plot'] = {'name': self.unique_name, 'ID': stored}

        connection.close()

    def save_all_results(self):
        connection = pymongo.MongoClient()
        db = connection[studio_db_name]
        results = db['results']
        results.update({'name': self.unique_name}, self.results, upsert=True)
        connection.close()
