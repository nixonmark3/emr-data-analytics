
import pickle
import pandas as pd
import json
import sys
import numpy as np

from pymongo import MongoClient
from datetime import datetime, timedelta
from operator import itemgetter
from Bricks import BricksDB
from dateutil.parser import parse
from FunctionBlock import FunctionBlock


class CalcOnCols(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            # ensure that parameters have been configured

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            calc_string = str(self.parameters['Calc'])

            if (calc_string == 'None'):
                FunctionBlock.report_status_configure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}

            # form of calc string is dataframe column name as variable like "newtest = In14 * 2 + In15 + .15"
            # column newtest is created for dataframe.  In14 and In15 are existing column names
            df.eval(calc_string)

            # save results and report block state is good
            FunctionBlock.save_results(self, df=df, statistics=True)

            # Report back good status as we are done
            FunctionBlock.report_status_complete(self)

            # Return data so that next block can consume it
            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            # save results and report block state is bad
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)


