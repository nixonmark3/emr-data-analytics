__author__ = 'noelbell'

import sys
import pandas as pd
import collections

from FunctionBlock import FunctionBlock


class Split(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:

            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            split = self.parameters['Split']

            group = int(split/100 * len(df))

            df_train = df[:group]
            df_test = df[group:]

            results = collections.OrderedDict()
            results['Split'] = split

            FunctionBlock.report_status_complete(self)

            FunctionBlock.save_results(self, df=df_train, statistics=True, plot=True, results=results)

            return {FunctionBlock.getFullPath(self, 'train'): df_train, FunctionBlock.getFullPath(self, 'test'): df_test}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)






