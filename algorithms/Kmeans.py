import pandas as pd
import collections as coll
import sys
import ast

from sklearn.cluster import KMeans
from FunctionBlock import FunctionBlock




class Kmeans(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'x')
            x_df = results_table[self.input_connectors['x'][0]]

            n_clusters = self.parameters['NumClusters']

            n_rows = self.parameters['NumRowsSample']

            ts_results = ast.literal_eval(self.parameters['TimeStampResults'])

            x_values = x_df.values
            col_names = list(x_df.columns.values)


            kmeans_model = KMeans((n_clusters))

            length_data = len(x_values)
            labellist = []
            time_list = []

            start = 0
            while start+n_rows <= length_data:

                kmeans_model.fit(x_values[start:start+n_rows,:].T)

                labels = kmeans_model.predict(x_values[start:start+n_rows,:].T)

                time_end = x_df.index[start+n_rows]

                labellist.append(list(labels))

                time_list.append(time_end.value)

                start += n_rows - 1

            if ts_results:
                time_list = pd.to_datetime(time_list)
                df_labels = pd.DataFrame(data=labellist, columns=col_names, index=time_list)
            else:
                df_labels = pd.DataFrame(data=labellist, columns=col_names)

            results_df = pd.DataFrame(df_labels)


            FunctionBlock.save_all_results(self)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'labels'): results_df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
