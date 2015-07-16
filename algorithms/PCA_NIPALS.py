import numpy as np
import collections
import pandas as pd
import sys

from FunctionBlock import FunctionBlock

# Limitations: does not handle missing data


class PCA_NIPALS(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            df = results_table[self.input_connectors['in'][0]]

            raw = df.values

            N, K = raw.shape

            # Pre-processing: mean center and scale the data columns to unit variance
            X = raw - raw.mean(axis=0)
            X = X / X.std(axis=0)

            # Verify the centering and scaling
            X.mean(axis=0)   # array([ -3.92198351e-17,  -1.74980803e-16, ...
            X.std(axis=0)    # [ 1.  1.  1.  1.  1.  1.  1.  1.  1.]

            A = self.parameters['N Components']

            scores = np.zeros((N, A))
            loadings = np.zeros((K, A))

            tolerance = 1E-10
            for a in range(A):

                t_a_guess = np.random.rand(N, 1)*2
                t_a = t_a_guess + 1.0
                itern = 0

                # Repeat until the score, t_a, converges, or until a maximum number of iterations has been reached
                while np.linalg.norm(t_a_guess - t_a) > tolerance or itern < 500:

                    # 0: starting point for convergence checking on next loop
                    t_a_guess = t_a

                    # 1: Regress the scores, t_a, onto every column in X; compute the
                    #    regression coefficient and store it in the loadings, p_a
                    #    i.e. p_a = (X' * t_a)/(t_a' * t_a)
                    p_a = np.dot(X.T, t_a) / np.dot(t_a.T, t_a)

                    # 2: Normalize loadings p_a to unit length
                    p_a = p_a / np.linalg.norm(p_a)

                    # 3: Now regress each row in X onto the loading vector; store the
                    #    regression coefficients in t_a.
                    #    i.e. t_a = X * p_a / (p_a.T * p_a)
                    t_a = np.dot(X, p_a) / np.dot(p_a.T, p_a)

                    itern += 1

                #  We've converged, or reached the limit on the number of iteration

                # Deflate the part of the data in X that we've explained with t_a and p_a
                X = X - np.dot(t_a, p_a.T)

                # Store result before computing the next component
                scores[:, a] = t_a.ravel()
                loadings[:, a] = p_a.ravel()

            # Squared Prediction Error to the X-space is the residual distance from the model to each data point.
            spe = np.sum(X**2, axis=1)

            # Hotelling's T2, the directed distance from the model center to each data point.
            inv_covariance = np.linalg.inv(np.dot(scores.T, scores)/N)
            t2 = np.zeros((N, 1))
            for n in range(N):
                t2[n] = np.dot(np.dot(scores[n,:], inv_covariance), scores[n,:].T)

            results = collections.OrderedDict()
            results['N components'] = A

            FunctionBlock.save_results(self, df=None, statistics=False, plot=False, results=results)

            scores = pd.DataFrame(scores)
            scores.columns = [str('scores_{0}'.format(x+1)) for x in scores.columns.values.tolist()]
            scores.index = df.index

            loadings = pd.DataFrame(loadings)
            loadings.columns = [str('loadings_{0}'.format(x+1)) for x in loadings.columns.values.tolist()]

            spe = pd.DataFrame(spe)
            spe.columns = [str('spe_{0}'.format(x+1)) for x in spe.columns.values.tolist()]
            spe.index = df.index

            t2 = pd.DataFrame(t2)
            t2.columns = [str('t2_{0}'.format(x+1)) for x in t2.columns.values.tolist()]
            t2.index = df.index

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'scores'): scores,
                    FunctionBlock.getFullPath(self, 'loadings'): loadings,
                    FunctionBlock.getFullPath(self, 'spe'): spe,
                    FunctionBlock.getFullPath(self, 't2'): t2}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


