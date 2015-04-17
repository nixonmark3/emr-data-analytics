import pickle
import pandas as pd
import json

from pymongo import Connection
from datetime import datetime, timedelta
from operator import itemgetter
from Bricks import BricksDB
from dateutil.parser import parse
from FunctionBlock import FunctionBlock


class DataBrick(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def json_to_query(self, json_str):
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

    def execute(self, results_table):

        FunctionBlock.report_status_executing(self)

        s = str(self.parameters['Query']).replace("'", "\"")

        connection = Connection()
        bricks_db = BricksDB(connection, self.parameters['Project'])

        time_ranges, tags, aliases, sample_rate_secs, max_samples = self.json_to_query(s)
        df = bricks_db.query(tags=tags,time_ranges=time_ranges, aliases=aliases, period_secs=sample_rate_secs, max_samples=max_samples)

        FunctionBlock.report_status_complete(self)

        return {'{0}/{1}'.format(self.name, 'out'): df}
