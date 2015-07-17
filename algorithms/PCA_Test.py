__author__ = 'noelbell'

import numpy as np
import collections
import pandas as pd
from scipy.stats import f
from scipy.stats import norm
import sys

from FunctionBlock import FunctionBlock

class PCA_Test(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'data')
            df = results_table[self.input_connectors['data'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'Loadings')
            loadings = results_table[self.input_connectors['Loadings'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'OrigMean')
            mean = results_table[self.input_connectors['OrigMean'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'OrigSTD')
            std = results_table[self.input_connectors['OrigSTD'][0]]

            rawdf = (df - mean)/std
            raw = rawdf.values

            N, K = raw.shape

            L_n, num_PC = loadings.shape

            # Pre-processing: mean center and scale the data columns to unit variance
            # X = raw - raw.mean(axis=0)
            #X = X / X.std(axis=0)

            # scores = np.zeros((N, num_PC))

            scores = np.dot(raw, loadings)

            results = collections.OrderedDict()

            FunctionBlock.save_results(self, df=None, statistics=False, plot=False, results=results)

            scores = pd.DataFrame(scores)
            scores.columns = [str('Score_{0}'.format(x+1)) for x in scores.columns.values.tolist()]
            scores.index = df.index

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'Scores'): scores}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)