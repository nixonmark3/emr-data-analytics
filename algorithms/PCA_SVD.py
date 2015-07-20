import sys
import pandas as pd
import collections

from sklearn import decomposition
from FunctionBlock import FunctionBlock


class PCA_SVD(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            df = results_table[self.input_connectors['in'][0]]

            raw = df.values

            N = self.parameters['N Components']

            pca = decomposition.PCA(n_components=N)
            pca.fit(raw)
            scores = pca.transform(raw)

            results = collections.OrderedDict()
            results['N components'] = N
            results['Explained Variance'] = pca.explained_variance_.tolist()
            FunctionBlock.save_results(self, df=None, statistics=False, plot=False, results=results)

            scores = pd.DataFrame(scores)
            scores.columns = [str('Score_{0}'.format(x+1)) for x in scores.columns.values.tolist()]

            loadings = pd.DataFrame(pca.components_)
            loadings.columns = [str('Loading_{0}'.format(x+1)) for x in loadings.columns.values.tolist()]

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'scores'): scores,
                    FunctionBlock.getFullPath(self, 'loadings'): loadings}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

