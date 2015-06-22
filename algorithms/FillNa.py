__author__ = 'noelbell'

import sys
import pandas as pd
import numpy as np
import collections

from FunctionBlock import FunctionBlock


class FillNa(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            interpolation = self.parameters['Fill Method']

            if interpolation == 'mean':
                df_filled = df.fillna(value=df.mean())
            else:
                df_filled = df.fillna(method=interpolation)

            FunctionBlock.save_results(self, df=df_filled, statistics=True, plot=False, results=None)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df_filled}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




