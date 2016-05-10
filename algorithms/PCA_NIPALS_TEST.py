import numpy as np
import collections
import pandas as pd
from scipy.stats import f
from scipy.stats import norm
from numpy.linalg import inv
import sys
import PCA_calcdefs

from FunctionBlock import FunctionBlock

class PCA_NIPALS_TEST(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            df = results_table[self.input_connectors['in'][0]]
            #print(' after in load....PCA_NIPALA_TEST..........', flush=True)
            loadings = results_table[self.input_connectors['Loadings'][0]]
            #print(' after loadings load....PCA_NIPALA_TEST..........', flush=True)
            eig_value_df = results_table[self.input_connectors['EigenValues'][0]]
            #print(' after eigenvalues load....PCA_NIPALA_TEST..........', flush=True)
            eig_vector_df = results_table[self.input_connectors['EigenVectors'][0]]
            #print(' after eigenvectors load....PCA_NIPALA_TEST..........', flush=True)
            mean_df = results_table[self.input_connectors['Mean'][0]]
            #print(' after mean load....PCA_NIPALA_TEST..........', flush=True)
            std_df = results_table[self.input_connectors['Std'][0]]
            #print(' after std load....PCA_NIPALA_TEST..........', flush=True)

            confidence_level = self.parameters['Confidence Level']
            #print(' after confidence level read load....PCA_NIPALA_TEST..........', flush=True)

            raw = df.values
            #print(' after RAW....PCA_NIPALA_TEST..........', flush=True)
            P_a = loadings.values
            #print(' after P_a....PCA_NIPALA_TEST..........', flush=True)
            eig_value = []
            eig_value = np.array([np.array(xi) for xi in eig_value_df.values])
            #print('eigen dataframe in nipals test ******* =======\n', eig_value_df)
            #print('eigen values after load *********    =========\n', eig_value, type(eig_value))
            eig_vector = eig_vector_df.values

            N, K = raw.shape
            #print(' after in raw-shape....PCA_NIPALA_TEST.......  N= ', N, '  K = ', K, flush=True)

            # Pre-processing: mean center and scale the data columns to unit variance
            #print(' before mean subtraction..PCA_NIPALA_TEST..........', flush=True)
            #print('sizes', np.shape(raw), np.shape(mean_df), 'raw..............\n', raw)
            #print('types....raww...', type(raw), 'type.....df_mean', type(mean_df))
            #print('mean_df............\n', mean_df)

            mean = np.array(mean_df, dtype=pd.Series)
            X = raw - mean   #(axis=0)
            #print(' after mean subtraction....PCA_NIPALA_TEST..........', flush=True)

            std = np.array(std_df, dtype=pd.Series)
            #print('std dev.........\n', std_df)
            X = X / std #(axis=0)
            #print(' after scaling....PCA_NIPALA_TEST..........', flush=True)

            #print('  again.......')

            original_data = X

            # Verify the centering and scaling
            #X.mean(axis=0)   # array([ -3.92198351e-17,  -1.74980803e-16, ...
            #following causes error ("'float' object has no attribute 'sqrt'",)
            #X.std(axis=0)    # [ 1.  1.  1.  1.  1.  1.  1.  1.  1.]

            #print(' after verify..............', flush=True)

            # get number of principal components
            A = loadings.shape[1]
            #print('A = ', A)
            #print('shape X and P_a......', X.shape, P_a.shape, np.dot(X,P_a).shape, np.dot(P_a.T, P_a).shape)

             #          Ainv = linalg.pinv(dot(temp_loading.T, temp_loading))   # num_comp*num_comp
            #           temp_score = dot(dot(temp_X_test_no_i, temp_loading), Ainv)  #n_tes

            scores = np.dot(np.dot(X, P_a),np.linalg.pinv(np.dot(P_a.T, P_a)))

            #print(' after scores calc....PCA_NIPALA_TEST..........', flush=True)

            # Squared Prediction Error to the X-space is the residual distance from the model to each data point.
            X_error = X - np.dot(scores, P_a.T)
            spe = np.sum(X_error ** 2, axis=1)

            # Hotelling's T2, the directed distance from the model center to each data point.
            #print('before inv_covariance................')
            inv_covariance = 1 / (np.dot(scores.T, scores) / N)
            #print('after inv_covariance..................\n', scores.shape)


            # Calculate variance captured
            #eig_value_df, eig_vector_df = np.linalg.eig(np.cov(original_data.T))
            #eig_value_idx = eig_value.argsort()[::-1]
            #eig_value = eig_value[eig_value_idx]
            #eig_vector = eig_vector[:, eig_value_idx]

            # Calculate the variable contributions to score

            t2_lim = A * (N - 1) / (N - A) * f.ppf(confidence_level, A, N - A)
            #print('after t2_lim............')

            t2 = np.zeros((N, 1))
            varContribT2 = np.zeros((N, K))
            varContrib = np.zeros((N, K))

            #print('after varContrib..................')

            #eig_value = eig_value_df.values

            t2 =  PCA_calcdefs.t2_cal(A, scores, eig_value)

            #print('after t2 calc call................ t2 = ', t2)

                # :param k: integer; number of PCs selected
                # :param score_f: n*m array; scores
                # :param eigen_val: 1*m array; eigenvalues
                # :return: hotelling's T2: 1*n array

            for i in range(N):
                #t2[i] = np.dot(np.dot(scores[i, :], inv_covariance), scores[i, :].T)
                ##print('in var contribT2...... i = ', i)
                for j in range(K):
                    ##print('in var contribT2...... i,j = ', i, j, A)
                    # Calculate the variable contributions to T2
                    temp_x_score = original_data[i, j] * P_a[j, 0:A]
                    # #print('after temp_x_score.=========..............\n',temp_x_score)
                    # #print(' temp_x_score in PCA..======\n', temp_x_score)
                    # #print('scores in varContrib=====\n', scores[i, 0:A])
                    ##print('eigen values=========\n', np.ravel(eig_value[0:A]), type(eig_value))
                    # #print('t2_lim========\n', t2_lim)
                    varContribT2[i, j] = np.dot(scores[i, 0:A] / np.ravel(eig_value[0:A]), temp_x_score.T) / t2_lim
                    ##print('after varContribT2 ..................   i, j = ', i, j)
                    # Continue calculation of variable contributions to score
                    for a in range(A):
                        #print('before varContrib..........i, j, a', i, j, a)
                        varContrib[i, j] = np.sum((original_data[i, j] * P_a[j, a] * eig_value[a]) / scores[i, a])
            #print('varContrib[i,j]....*************************************........\n', varContrib.size, varContrib)
            # Calculate the variable contributions to Q
            #print('before eig_vector_inv..................')
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
                #print('inside A>=m.................................................................')
            elif A == m-1:
                theta1 = eig_value[A]
                theta2 = eig_value[A]**2
                #print(' in theta calcs....type theta2 = ', np.type(theta2), flush=True)
                theta3 = eig_value[A]**3
                if theta1 == 0:
                    q_lim = 0
                else:
                    h0 = 1-2*theta1*theta3/3/(theta2**2)
                    if h0 < 0.001:
                        h0 = 0.001
                    else:
                        ca = norm.ppf(confidence_level)
                        #print(' in theta calcs....type theta2 = ', type(theta2), flush=True)
                        h1 = ca*h0* np.sqrt(2*theta2)/theta1
                        h2 = theta2*h0*(h0-1)/(theta1**2)
                        q_lim = theta1*(1+h1+h2)**(1/h0)
            else:
                #print('inside last theta calc.........')
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

            #print(' before save results....PCA_NIPALA_TEST..........', flush=True)

            FunctionBlock.save_results(self, df=None, statistics=False, plot=False, results=results)

            scores = pd.DataFrame(scores)
            #print('before scores colujmns names..............................XXXXXXXXXXXXXXX')
            scores.columns = [str('Score_{0}'.format(x+1)) for x in scores.columns.values.tolist()]
            scores.index = df.index
            #print('before loadings colujmns names..............................XXXXXXXXXXXXXXX')
            # loadings = pd.DataFrame(loadings)
            # loadings.columns = [str('Loading_{0}'.format(x+1)) for x in loadings.columns.values.tolist()]

            # loadings already known from input

            #print('before squared pred errors colujmns names..............................XXXXXXXXXXXXXXX')
            q = pd.DataFrame(spe)
            q.columns = [str('Q_{0}'.format(x+1)) for x in q.columns.values.tolist()]
            q.index = df.index
            #print('before t2 colujmns names..............................XXXXXXXXXXXXXXX')
            t2 = pd.DataFrame(t2)
            t2.columns = [str('T2_{0}'.format(x+1)) for x in t2.columns.values.tolist()]
            t2.index = df.index

            #print('varContrib before Dataframe........$$$$$$$$$$$$$$$$$......\n', varContrib.size, varContrib.shape,  varContrib)
            varContribScore = pd.DataFrame(varContrib)

            varContribScore.index = df.index
            varContribScore.columns = df.columns
            #print('varContribScore DataFrame..........\n', varContribScore)

            varContribQ = pd.DataFrame(varContribQ)
            varContribQ.index = df.index
            varContribQ.columns = df.columns

            varContribT2 = pd.DataFrame(varContribT2)
            varContribT2.index = df.index
            varContribT2.columns = df.columns

            eig_value = pd.DataFrame(eig_value)
            eig_value.columns = [str('EVAL_{0}'.format(x+1)) for x in eig_value.columns.values.tolist()]

            eig_vector_df = pd.DataFrame(eig_vector)
            eig_vector_df.columns = [str('EVEC_{0}'.format(x+1)) for x in eig_vector_df.columns.values.tolist()]

            FunctionBlock.report_status_complete(self)

            # print('N, K, A = ', N,K, A)
            # print(' before return call....PCA_NIPALA_TEST..........')
            # print('scores type in PCA TEST = ', type(scores))
            # print('scores in PCA TEST..................\n', scores)

            return {FunctionBlock.getFullPath(self, 'Loadings'): loadings,
                    FunctionBlock.getFullPath(self, 'Scores'): scores,
                    FunctionBlock.getFullPath(self, 'ScoresCont'): varContribScore,
                    FunctionBlock.getFullPath(self, 'T2'): t2,
                    FunctionBlock.getFullPath(self, 'T2Lim'): t2_lim,
                    FunctionBlock.getFullPath(self, 'T2Cont'): varContribT2,
                    FunctionBlock.getFullPath(self, 'Q'): q,
                    FunctionBlock.getFullPath(self, 'QLim'): q_lim,
                    FunctionBlock.getFullPath(self, 'QCont'): varContribQ,
                    FunctionBlock.getFullPath(self, 'EigenValues'): eig_value_df,
                    FunctionBlock.getFullPath(self, 'EigenVectors'): eig_vector_df,
                    FunctionBlock.getFullPath(self, 'Mean'): df.mean(axis=1),
                    FunctionBlock.getFullPath(self, 'Std'): df.std(axis=1)}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


