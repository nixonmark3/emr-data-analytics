import numpy as np
import collections
import pandas as pd
import sys
import traceback

from scipy.stats import f
from scipy.stats import norm

from FunctionBlock import FunctionBlock

class PCA_NIPALS(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')
            df = results_table[self.input_connectors['x'][0]]

            raw = df.values

            N, K = raw.shape
            #print('size of raw = ', N, K)

            # Pre-processing: mean center and scale the data columns to unit variance
            X = raw - raw.mean(axis=0)
            XDev = X.std(axis=0)
            for i in range(XDev.shape[0]):
                if(XDev[i] == 0):
                    XDev[i] = 1

            X=X/XDev

            original_data = X

            # Verify the centering and scaling
            X.mean(axis=0)   # array([ -3.92198351e-17,  -1.74980803e-16, ...
            X.std(axis=0)    # [ 1.  1.  1.  1.  1.  1.  1.  1.  1.]

            A = self.parameters['N Components']

            confidence_level = self.parameters['Confidence Level']

            scores = np.zeros((N, A))
            loadings = np.zeros((K, A))

            tolerance = 1E-6
            for a in range(A):

                t_a_guess = np.zeros((N,1))#np.random.rand(N, 1)*2
                t_a = t_a_guess + 1.0
                itern = 0

                # Repeat until the score, t_a, converges, or until a maximum number of iterations has been reached
                while np.linalg.norm(t_a_guess - t_a) > tolerance and itern < 10000:

                    # 0: starting point for convergence checking on next loop
                    t_a_guess = t_a

                    # 1: Regress the scores, t_a, onto every column in X; compute the
                    #    regression coefficient and store it in the loadings, p_a
                    #    i.e. p_a = (X' * t_a)/(t_a' * t_a)
                    ##print('....in PCA NIPALS..... X.T* t_a and t_a.T * T_a', shape(np.dot(X.T, t_a)))
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
            eig_value, eig_vector = np.linalg.eig(np.cov(original_data.T))
            #print('eig_value in PCA before argsort........\n', eig_value)
            eig_value_idx = eig_value.argsort()[::-1]
            #print('eig_value_idx in PCA......\n', eig_value_idx)
            eig_value = eig_value[eig_value_idx]
            #print('eig_value in PCA after argsort........\n', eig_value)
            eig_vector = eig_vector[:, eig_value_idx]

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
                    # #print(' temp_x_score in PCA..======\n', temp_x_score)
                    # #print('scores in varContrib=====\n', scores[i, 0:A])
                    #print('eigen values in PCA ========XXxxxxxxxxXXXXXXXXXXXXXXX=\n', eig_value[0:A], type(eig_value))
                    # #print('t2_lim========\n', t2_lim)
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

            scores = pd.DataFrame(scores)
            scores.columns = [str('Score_{0}'.format(x+1)) for x in scores.columns.values.tolist()]
            scores.index = df.index

            loadings_df = pd.DataFrame(loadings)
            loadings_df.columns = [str('Loading_{0}'.format(x+1)) for x in loadings_df.columns.values.tolist()]

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

            pca_results = collections.OrderedDict()
            pca_results['N components'] = A
            pca_results['Confidence Level'] = confidence_level
            pca_results['Q Limit'] = q_lim
            pca_results['T2 Limit'] = t2_lim

            mean = df.mean(axis=0)
            std = df.std(axis=0)

            FunctionBlock.add_general_results(self, pca_results)
            FunctionBlock.add_persisted_connector_result(self, 'Loadings', loadings.transpose().tolist())
            FunctionBlock.add_persisted_connector_result(self, 'x_mean', mean.tolist())
            FunctionBlock.add_persisted_connector_result(self, 'x_std', std.tolist())
            FunctionBlock.save_all_results(self)

            FunctionBlock.report_status_complete(self)

            # print(' before return call....PCA_Nipals..........')
            # print('scores type in PCA NIPALs = ', type(scores))
            # print('scores in PCA Nipals..................\n', scores)

            return {FunctionBlock.getFullPath(self, 'Loadings'): loadings_df,
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
                    FunctionBlock.getFullPath(self, 'Mean'): mean,
                    FunctionBlock.getFullPath(self, 'Std'): std}

        except:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            traceback.print_exc(file=sys.stderr)


