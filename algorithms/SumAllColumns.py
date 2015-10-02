# -*- coding: utf-8 -*-
"""
Created on Wed Jul 22 14:17:22 2015

@author: rw23722
"""
import pandas as pd
import numpy as np
from FunctionBlock import FunctionBlock
import sys
import collections


class SumAllColumns(FunctionBlock):
    def __init__(self,name,unique_name):
        FunctionBlock.__init__(self,name,unique_name)
        
    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            df_sum = pd.DataFrame()
            df_sum['sum'] = df.sum(axis=1)

            results = collections.OrderedDict()


            FunctionBlock.save_results(self, df=None, statistics=False, plot=False, results=None)

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'SumDF'): df_sum}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)