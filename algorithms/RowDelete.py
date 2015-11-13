#Note: currently same as RowDelete


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

    exclude_time_ranges = []

    exclude_row_ranges = []
    print('in query')

    for range in query["excludeTime"]:
        start_time = range["exStartTime"]
        end_time = range["exEndTime"]
        if (start_time and end_time):
            exclude_time_ranges.append((start_time,end_time))

    for range in query["excludeRow"]:
        start_row = range["exStartRow"]
        end_row = range["exEndRow"]
        if (start_row and end_row):
            exclude_row_ranges.append((start_row, end_row))

    return exclude_time_ranges, exclude_row_ranges


class RowSelect(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            # ensure that parameters have been configured

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            query = str(self.parameters['Query'])

            if (query == 'None'):
                FunctionBlock.report_status_configure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}

            query = query.replace("'", "\"")

            exclude_time_ranges, exclude_row_ranges = json_to_query(query)

            # First remove row_ranges if any
            df.index = pd.to_datetime(df.index*1000000000)

            for x,y in exclude_row_ranges:
                df = df.drop(df.index[int(x):int(y)])

            # Second remove date_ranges
            # find index in dataframe that has closest time to specified times
            for x,y in exclude_time_ranges:
                i = np.argmin(np.abs(df.index.to_pydatetime() - pd.Timestamp(x)))
                j = np.argmin(np.abs(df.index.to_pydatetime() - pd.Timestamp(y)))
                df = df.drop(df.index[i:j])

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

# sample Query:
# {
#   "query_name": "Query1",
#   "docType": "json",
#   "excludeTime": [
#     {
#       "exStartTime": "2015-01-01 01:06:00",
#       "exEndTime": "2015-01-01 01:26:00"
#     },
#     {
#       "exStartTime": "",
#       "exEndTime": ""
#     }
#   ],
#   "excludeRow": [
#     {
#       "exStartRow": "100",
#       "exEndRow": "200"
#     },
#     {
#       "exStartRow": "",
#       "exEndRow": ""
#     }
#   ]
# }