__author__ = 'noelbell'

import sys
import pandas as pd
import collections

from FunctionBlock import FunctionBlock


class EWMStDev(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            weight = self.parameters['Weight']

            df_ewmstdev = pd.ewmstd(df, weight)

            results = collections.OrderedDict()
            results['Weight'] = weight

            FunctionBlock.save_results(self, df=df_ewmstdev, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self,  'out'): df_ewmstdev}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




