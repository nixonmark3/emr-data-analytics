__author__ = 'noelbell'


import sys
import pandas as pd
import numpy as np
import collections

from FunctionBlock import FunctionBlock


class DropNa(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            df_dropped = df.dropna(how='any')

            FunctionBlock.save_results(self, df=df_dropped, statistics=True, plot=True, results=None)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df_dropped}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)




