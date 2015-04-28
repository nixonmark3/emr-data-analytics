__author__ = 'noelbell'

import sys
import pandas as pd
import collections

from FunctionBlock import FunctionBlock


class EWMA(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            weight = self.parameters['Weight']

            df_ewma = pd.ewma(df, weight)

            results = collections.OrderedDict()
            results['Weight'] = weight

            FunctionBlock.save_results(self, df=df_ewma, statistics=True, plot=True, results=results)

            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): df_ewma}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




