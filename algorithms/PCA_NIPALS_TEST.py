import numpy as np
import collections
import pandas as pd
from scipy.stats import f
from scipy.stats import norm
import sys

from FunctionBlock import FunctionBlock

class PCA_NIPALS_TEST(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            df = results_table[self.input_connectors['in'][0]]
            loadings = results_table[self.input_connectors['Loadings'][0]]
            eig_value = results_table[self.input_connectors['EigenValues'][0]]
            eig_vector = results_table[self.input_connectors['EigenVectors'][0]]
            mean_df = results_table[self.input_connectors['Mean'][0]]
            std_df = results_table[self.input_connectors['Std'][0]]

            confidence_level = self.parameters['Confidence Level']

            raw = df.values
            P_a = loadings.values

            N, K = raw.shape

            # Pre-processing: mean center and scale the data columns to unit variance
            X = raw - mean_df(axis=0)
            X = X / std_df(axis=0)

            original_data = X

            # Verify the centering and scaling
            X.mean(axis=0)   # array([ -3.92198351e-17,  -1.74980803e-16, ...
            X.std(axis=0)    # [ 1.  1.  1.  1.  1.  1.  1.  1.  1.]

            # get number of principal components
            A = loadings.shape[1]

            scores = np.dot(X, P_a) / np.dot(P_a.T, P_a)

            # Squared Prediction Error to the X-space is the residual distance from the model to each data point.
            X_error = X - np.dot(scores, P_a.T)
            spe = np.sum(X_error ** 2, axis=1)

            # Hotelling's T2, the directed distance from the model center to each data point.
            inv_covariance = np.linalg.inv(np.dot(scores.T, scores) / N)

            # Calculate variance captured
            # eig_value, eig_vector = np.linalg.eig(np.cov(original_data.T))
            # eig_value_idx = eig_value.argsort()[::-1]
            # eig_value = eig_value[eig_value_idx]
            # eig_vector = eig_vector[:, eig_value_idx]

            # Calculate the variable contributions to score

            t2_lim = A * (N - 1) / (N - A) * f.ppf(confidence_level, A, N - A)

            t2 = np.zeros((N, 1))
            varContribT2 = np.zeros((N, K))
            varContrib = np.zeros((N, K))

            for i in range(N):
                t2[i] = np.dot(np.dot(scores[i, :], inv_covariance), scores[i, :].T)

                for j in range(K):
                    # Calculate the variable contributions to T2
                    temp_x_score = original_data[i, j] * loadings[j, 0:A]
                    varContribT2[i, j] = np.dot(scores[i, 0:A] / eig_value[0:A], temp_x_score.T) / t2_lim

                    # Continue calculation of variable contributions to score
                    for a in range(A):
                        varContrib[i, j] = np.sum((original_data[i, j] * loadings[j, a] * eig_value[a]) / scores[i, a])

            # Calculate the variable contributions to Q
            eig_vector_inv = np.linalg.inv(eig_vector)

            scoreTemp = np.matrix(scores)
            sliceTemp = np.matrix(eig_vector_inv[:, 0:A].T)
            reconstructed = np.array(scoreTemp * sliceTemp)

            varContribQ = original_data - reconstructed

            # Calculate Q limits
            q_lim = 0
            m = len(eig_value)
            if A >= m:
                q_lim = 0
            elif A == m-1:
                theta1 = eig_value[A]
                theta2 = eig_value[A]**2
                theta3 = eig_value[A]**3
                if theta1 == 0:
                    q_lim = 0
                else:
                    h0 = 1-2*theta1*theta3/3/(theta2**2)
                    if h0 < 0.001:
                        h0 = 0.001
                    else:
                        ca = norm.ppf(confidence_level)
                        h1 = ca*h0*np.sqrt(2*theta2)/theta1
                        h2 = theta2*h0*(h0-1)/(theta1**2)
                        q_lim = theta1*(1+h1+h2)**(1/h0)
            else:
                theta1 = sum(eig_value[A:m])
                theta2 = sum(eig_value[A:m]**2)
                theta3 = sum(eig_value[A:m]**3)
                if theta1 == 0:
                    q_lim = 0
                else:
                    h0 = 1-2*theta1*theta3/3/(theta2**2)
                    if h0 < 0.001:
                        h0 = 0.001
                    else:
                        ca = norm.ppf(confidence_level)
                        h1 = ca*h0*np.sqrt(2*theta2)/theta1
                        h2 = theta2*h0*(h0-1)/(theta1**2)
                        q_lim = theta1*(1+h1+h2)**(1/h0)

            results = collections.OrderedDict()
            results['N components'] = A
            results['Confidence Level'] = confidence_level

            FunctionBlock.save_results(self, df=None, statistics=False, plot=False, results=results)

            scores = pd.DataFrame(scores)
            scores.columns = [str('Score_{0}'.format(x+1)) for x in scores.columns.values.tolist()]
            scores.index = df.index

            loadings = pd.DataFrame(loadings)
            loadings.columns = [str('Loading_{0}'.format(x+1)) for x in loadings.columns.values.tolist()]

            q = pd.DataFrame(spe)
            q.columns = [str('Q_{0}'.format(x+1)) for x in q.columns.values.tolist()]
            q.index = df.index

            t2 = pd.DataFrame(t2)
            t2.columns = [str('T2_{0}'.format(x+1)) for x in t2.columns.values.tolist()]
            t2.index = df.index

            varContribScore = pd.DataFrame(varContrib)
            varContribScore.index = df.index
            varContribScore.columns = df.columns

            varContribQ = pd.DataFrame(varContribQ)
            varContribQ.index = df.index
            varContribQ.columns = df.columns

            varContribT2 = pd.DataFrame(varContribT2)
            varContribT2.index = df.index
            varContribT2.columns = df.columns

            eig_value = pd.DataFrame(eig_value)
            eig_value.columns = [str('EVAL_{0}'.format(x+1)) for x in eig_value.columns.values.tolist()]

            eig_vector = pd.DataFrame(eig_vector)
            eig_vector.columns = [str('EVEC_{0}'.format(x+1)) for x in eig_vector.columns.values.tolist()]

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'Loadings'): loadings,
                    FunctionBlock.getFullPath(self, 'Scores'): scores,
                    FunctionBlock.getFullPath(self, 'ScoresCont'): varContribScore,
                    FunctionBlock.getFullPath(self, 'T2'): t2,
                    FunctionBlock.getFullPath(self, 'T2Lim'): t2_lim,
                    FunctionBlock.getFullPath(self, 'T2Cont'): varContribT2,
                    FunctionBlock.getFullPath(self, 'Q'): q,
                    FunctionBlock.getFullPath(self, 'QLim'): q_lim,
                    FunctionBlock.getFullPath(self, 'QCont'): varContribQ,
                    FunctionBlock.getFullPath(self, 'EigenValues'): eig_value,
                    FunctionBlock.getFullPath(self, 'EigenVectors'): eig_vector,
                    FunctionBlock.getFullPath(self, 'Mean'): df.mean(axis=1),
                    FunctionBlock.getFullPath(self, 'Std'): df.std(axis=1)}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


