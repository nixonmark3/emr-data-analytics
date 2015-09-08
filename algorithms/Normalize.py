import numpy as np
import collections
import pandas as pd
import ast
from scipy.stats import f
from scipy.stats import norm
import sys

from FunctionBlock import FunctionBlock

class Normalize(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            df = results_table[self.input_connectors['in'][0]]

            mean_center = ast.literal_eval(self.parameters['Mean Center'])
            unit_var = ast.literal_eval(self.parameters['Unit Variance'])
            div_range = ast.literal_eval(self.parameters['DivideByRange'])

            min_allowed_std = 0

            X = df.copy()
            X_mean = X.mean()
            XDev = X.std(axis=0)
            Xrange = X.max() - X.min()

            # Pre-processing: mean center and scale the data columns to unit variance
            if mean_center:
                X =  X - X_mean

            if unit_var:
                for i in range(XDev.shape[0]):
                    if(XDev[i] <=  min_allowed_std):
                        XDev[i] = 1
                X=X/XDev
            elif div_range:
                for i in range(Xrange.shape[0]):
                    if(Xrange[i] <= min_allowed_std):
                        Xrange[i]=1
                X=X/Xrange

            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'Normalized'): X}  #,
                    # FunctionBlock.getFullPath(self, 'mean'): X_mean,
                    # FunctionBlock.getFullPath(self, 'Std'): XDev,
                    # FunctionBlock.getFullPath(self, 'Range'): Xrange}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


