import numpy as np
import collections as coll
import sys

from FunctionBlock import FunctionBlock

# Limitations: does not handle missing data


class PCA_SVD(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            df = results_table[self.input_connectors['in'][0]]

            raw = df.values

            # Pre-processing: mean center and scale the data columns to unit variance
            X = raw - raw.mean(axis=0)
            X = X / X.std(axis=0)

            # Verify the centering and scaling
            X.mean(axis=0)   # array([ -3.92198351e-17,  -1.74980803e-16, ...
            X.std(axis=0)    # [ 1.  1.  1.  1.  1.  1.  1.  1.  1.]

            A = self.parameters['N Components']

            # We could of course use SVD ...
            u, d, v = np.linalg.svd(X)

            # Transpose the "v" array from SVD, which contains the loadings, but retain only the first A columns
            loadings = v.T[:, range(0, A)]

            # Compute the scores from the loadings:
            scores = np.dot(X, loadings)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'scores'): scores,
                    FunctionBlock.getFullPath(self, 'loadings'): loadings}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

