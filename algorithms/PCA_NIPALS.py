import numpy as np
import collections
import pandas as pd
from scipy.stats import f
from scipy.stats import norm
import sys
import ast

from FunctionBlock import FunctionBlock

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

            # mean center and scale the data columns to unit variance
            X = raw - raw.mean(axis=0)
            X = X / X.std(axis=0)

            original_data = X

            A = self.parameters['N Components']

            confidence_level = self.parameters['Confidence Level']

            calc_contributions = ast.literal_eval(self.parameters['Calculate Contributions'])

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
            spe = np.sum(X ** 2, axis=1)

            # Hotelling's T2, the directed distance from the model center to each data point.
            inv_covariance = np.linalg.inv(np.dot(scores.T, scores) / N)

            # Calculate variance captured
            eig_values, eig_vectors = np.linalg.eig(np.cov(original_data.T))
            eig_values_idx = eig_values.argsort()[::-1]
            eig_values = eig_values[eig_values_idx]
            eig_vectors = eig_vectors[:, eig_values_idx]

            # Calculate the variable contributions to score

            t2_lim = A * (N - 1) / (N - A) * f.ppf(confidence_level, A, N - A)

            t2 = np.zeros((N, 1))
            variable_contribution_t2 = np.zeros((N, K))
            variable_contribution_score = np.zeros((N, K))

            for i in range(N):
                t2[i] = np.dot(np.dot(scores[i, :], inv_covariance), scores[i, :].T)

                if calc_contributions:
                    for j in range(K):
                        # Calculate the variable contributions to T2
                        temp_x_score = original_data[i, j] * loadings[j, 0:A]
                        variable_contribution_t2[i, j] = np.dot(scores[i, 0:A] / eig_values[0:A], temp_x_score.T) / t2_lim

                        # Continue calculation of variable contributions to score
                        for a in range(A):
                            variable_contribution_score[i, j] = np.sum((original_data[i, j] * loadings[j, a] * eig_values[a]) / scores[i, a])

            if calc_contributions:
                # Calculate the variable contributions to Q
                eig_vector_inv = np.linalg.inv(eig_vectors)
                score_temp = np.matrix(scores)
                slice_temp = np.matrix(eig_vector_inv[:, 0:A].T)
                reconstructed = np.array(score_temp * slice_temp)
                variable_contribution_q = original_data - reconstructed
            else:
                variable_contribution_q = np.zeros((N, K))

            # Calculate Q limits
            q_lim = 0
            m = len(eig_values)
            if A >= m:
                q_lim = 0
            elif A == m-1:
                theta1 = eig_values[A]
                theta2 = eig_values[A]**2
                theta3 = eig_values[A]**3
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
                theta1 = sum(eig_values[A:m])
                theta2 = sum(eig_values[A:m]**2)
                theta3 = sum(eig_values[A:m]**3)
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

            sum_of_eig_values = sum(eig_values)
            variance_captured = [(e / sum_of_eig_values) * 100 for e in eig_values]

            results = collections.OrderedDict()
            results['N components'] = A
            results['Confidence Level'] = confidence_level
            results['Variance Captured'] = variance_captured

            FunctionBlock.save_results(self, df=None, statistics=False, plot=False, results=results)

            scores = pd.DataFrame(scores)
            scores.columns = [str('Score_{0}'.format(x+1)) for x in scores.columns.values.tolist()]
            scores.index = df.index

            loadings = pd.DataFrame(loadings)
            loadings.columns = [str('Loading_{0}'.format(x+1)) for x in loadings.columns.values.tolist()]

            q = pd.DataFrame({'Q': spe})
            q.index = df.index

            t2 = pd.DataFrame({'T2': t2[:, 0]})
            t2.index = df.index

            variable_contribution_score = pd.DataFrame(variable_contribution_score)
            variable_contribution_score.index = df.index
            variable_contribution_score.columns = df.columns

            variable_contribution_q = pd.DataFrame(variable_contribution_q)
            variable_contribution_q.index = df.index
            variable_contribution_q.columns = df.columns

            variable_contribution_t2 = pd.DataFrame(variable_contribution_t2)
            variable_contribution_t2.index = df.index
            variable_contribution_t2.columns = df.columns

            eig_values = pd.DataFrame(eig_values)
            eig_values.columns = [str('EVAL_{0}'.format(x+1)) for x in eig_values.columns.values.tolist()]

            eig_vectors = pd.DataFrame(eig_vectors)
            eig_vectors.columns = [str('EVEC_{0}'.format(x+1)) for x in eig_vectors.columns.values.tolist()]

            t2_limit = pd.DataFrame({'T2_Limit': [t2_lim for x in range(len(df.index.values))]})

            q_limit = pd.DataFrame({'Q_Limit': [q_lim for x in range(len(df.index.values))]})

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'Loadings'): loadings,
                    FunctionBlock.getFullPath(self, 'Scores'): scores,
                    FunctionBlock.getFullPath(self, 'ScoresCont'): variable_contribution_score,
                    FunctionBlock.getFullPath(self, 'T2'): t2,
                    FunctionBlock.getFullPath(self, 'T2Lim'): t2_limit,
                    FunctionBlock.getFullPath(self, 'T2Cont'): variable_contribution_t2,
                    FunctionBlock.getFullPath(self, 'Q'): q,
                    FunctionBlock.getFullPath(self, 'QLim'): q_limit,
                    FunctionBlock.getFullPath(self, 'QCont'): variable_contribution_q,
                    FunctionBlock.getFullPath(self, 'EigenValues'): eig_values,
                    FunctionBlock.getFullPath(self, 'EigenVectors'): eig_vectors}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


