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


def json_to_query(json_str):
    query = json.loads(json_str)
    time_ranges = []
    row_ranges = []
    for range in query["timeSelector"]:
        start_time = range["startTime"]
        end_time = range["endTime"]
        time_ranges.append((start_time,end_time))
    for range in query["rowSelector"]:
        start_row = range["startRow"]
        end_row = range["endRow"]
        row_ranges.append((start_row, end_row))
    return time_ranges, row_ranges


class RowSelect(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            # ensure that parameters have been configured

            FunctionBlock.check_connector_has_one_wire(self, 'in')
            #print('before df')
            df = results_table[self.input_connectors['in'][0]]
            #print(' df......\n', df)
            query = str(self.parameters['Query'])
            #print('query.......\n', query)
            if (query == 'None'):
                FunctionBlock.report_status_configure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}

            query = query.replace("'", "\"")

            time_ranges, row_ranges = json_to_query(query)

            # First remove row_ranges if any
            #df.index = pd.to_datetime(df.index)
            print('first df.....\n', df)

            for x,y in row_ranges:
                print('x,y.....', int(x), int(y), type(int(x)), type(int(y)))
                df = df.drop(df.index[int(x):int(y)])


            # Second remove date_ranges
            # for x,y in time_ranges:
            #     print('x,y.....', x, y, type(x), type(y))
            #     df.drop(df[np.datetime64(x):np.datetime64(y)], inplace=True)

            #print(time_ranges)
            #print(row_ranges)

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

