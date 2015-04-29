import sys
import pymongo
import gridfs
import matplotlib.pyplot as plt
import os
import collections

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

    def save_results(self, df=None, statistics=False, plot=False, results=None):
        connection = pymongo.MongoClient()
        db = connection['emr-data-analytics-studio']

        block_results = collections.OrderedDict()

        if statistics:
            block_results['Statistics'] = df.describe().to_dict()

        if plot:
            # get a connection to GridFS
            fs = gridfs.GridFS(db)

            # delete the old plot if it exists
            if fs.exists(filename=self.name):
                fp = fs.get_last_version(self.name)
                fs.delete(fp._id)

            # Generate plot
            ax = df.plot(legend=True)
            ax.legend(loc='best', fancybox=True, shadow=True, prop={'size': 7})
            ax.tick_params(axis='both', which='major', labelsize=8)
            fig = ax.get_figure()
            fig.savefig('{0}.png'.format(self.name), dpi=100)
            plt.close(fig)

            # Save plot using GridFS
            with open('{0}.png'.format(self.name), 'rb') as f:
                png = f.read()

            stored = fs.put(png, filename=self.name)

            # Remove the generate plot file
            os.remove('{0}.png'.format(self.name))

            block_results['Plot'] = {'name': self.name, 'ID': stored}

        if results:
            block_results['Results'] = results

        self.results['Results'] = block_results

        results = db['results']
        results.update({'name': self.name}, self.results, upsert=True)

        connection.close()

    def check_connector_has_one_wire(self, connector_name):
        if len(self.input_connectors[connector_name]) != 1:
            FunctionBlock.report_status_failure(self)
            FunctionBlock.save_results(self)
            print("Too many wires connected to in connector!", file=sys.stderr)
            return {'{0}/{1}'.format(self.name, 'out'): None}
