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
    for range in query["timeSelector"]:
        start_time = parse(range["startTime"])
        end_time = parse(range["endTime"])
        time_ranges.append((start_time,end_time,))
    tags = [x["tag"] for x in query["columns"]]
    aliases = {x["tag"]:x["alias"] for x in query["columns"] if "alias" in x}
    sample_rate_secs = query["sampleRateSecs"]
    max_samples = query["maxSamples"] if "maxSamples" in query else None
    return time_ranges, tags, aliases, sample_rate_secs, max_samples


class DataBrick(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            # ensure that parameters have been configured
            project = self.parameters['Project']

            query = str(self.parameters['Query'])

            if (project == 'None') | (query == 'None'):
                FunctionBlock.report_status_configure(self)
                return {'{0}/{1}'.format(self.name, 'out'): None}

            # Now we are ready to run the algorithm
            connection = MongoClient()
            bricks_db = BricksDB(connection, project)

            query = query.replace("'", "\"")

            time_ranges, tags, aliases, sample_rate_secs, max_samples = json_to_query(query)

            df = bricks_db.query(tags=tags, time_ranges=time_ranges, aliases=aliases, period_secs=sample_rate_secs, max_samples=max_samples)

            # save results and report block state is good
            FunctionBlock.save_results(self, df=df, statistics=True, plot=True)

            # Report back good status as we are done
            FunctionBlock.report_status_complete(self)

            # Close connection to database
            connection.close()

            # Return data so that next block can consume it
            return {'{0}/{1}'.format(self.name, 'out'): df}

        except Exception as err:
            # save results and report block state is bad
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
