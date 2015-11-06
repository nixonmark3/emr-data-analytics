import pickle
import pandas as pd
import json
import sys

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
        start_time = parse(range["startTime"])
        end_time = parse(range["endTime"])
        time_ranges.append((start_time,end_time,))
    for range in query["rowSelector"]:
        start_row = parse(range["startRow"])
        end_row = parse(range["endRow"])
        row_ranges.append((start_row, end_row))
    return time_ranges, row_ranges


class RowSelect(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            # ensure that parameters have been configured

            FunctionBlock.check_connector_has_one_wire(self, 'In')

            df = results_table[self.input_connectors['In'][0]]

            query = str(self.parameters['Query'])

            if (query == 'None'):
                FunctionBlock.report_status_configure(self)
                return {FunctionBlock.getFullPath(self, 'out'): None}

            query = query.replace("'", "\"")

            time_ranges, row_ranges = json_to_query(query)

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

