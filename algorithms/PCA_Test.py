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

            FunctionBlock.check_connector_has_one_wire(self, 'TestData')
            test_df = results_table[self.input_connectors['TestData'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'OrigData')
            original_df = results_table[self.input_connectors['OrigData'][0]]

            FunctionBlock.check_connector_has_one_wire(self, 'Loadings')
            loadings = results_table[self.input_connectors['Loadings'][0]]

            df = (test_df - original_df.mean(axis=0)) / original_df.std(axis=0)

            raw = df.values

            scores = np.dot(raw, loadings)

            scores = pd.DataFrame(scores)
            scores.columns = [str('Test_Score_{0}'.format(x+1)) for x in scores.columns.values.tolist()]
            scores.index = df.index

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'Scores'): scores}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)