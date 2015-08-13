__author__ = 'paulmuston'

from datetime import datetime
import requests
import pytz
from dateutil import parser
import json
from urllib import parse
import pandas as pd
import numpy as np

class Connection():
    def __init__(self, ip="localhost", port=8001):
        self.ip = ip
        self.port = port

    @property
    def endpoint(self):
        return "http://%s:%d" % (self.ip, self.port)

    def read_values(self, tag, start_time, end_time):
        start_ts = int(start_time.timestamp() * 1000.0)
        end_ts = int(end_time.timestamp() * 1000.0)
        url = "%s/rawhistory/%d/%d?tag=%s" % (self.endpoint, start_ts, end_ts,parse.quote(tag))
        r = requests.get(url)
        return json.loads(r.text)

    def query(self, tags=None, time_range=None, period_secs=1):
        dfs = []
        for tag in tags:
            ret = self.read_values(tag, time_range[0], time_range[1])
            data = ret['data']
            times = [datetime.utcfromtimestamp(x[0]/1000) for x in data]
            values = [x[1] for x in data]
            df = pd.DataFrame(values, index=times, columns=[tag])
            dfs.append(df)
        dr = pd.date_range(time_range[0], time_range[1], freq='%ds' % period_secs)
        for i in range(len(dfs)):
            if not dfs[i].index.is_unique:
                dfs[i] = dfs[i].groupby(level=0).first()
            dfs[i] = dfs[i].reindex(index=dr, method='ffill', limit=2)
        merge = dfs[0]
        for i in range(len(dfs)):
            df = dfs[i]
            for col in df.columns:
                merge[col] = df[col]
        return merge

    def _json_to_query(self, json_str):
        query = json.loads(json_str)
        time_ranges = []
        for range in query["timeSelector"]:
            start_time = parser.parse(range["startTime"], yearfirst=True)
            end_time = parser.parse(range["endTime"], yearfirst=True)
            time_ranges.append((start_time,end_time,))
        tags = [x["tag"] for x in query["columns"]]
        aliases = {x["tag"]:x["alias"] for x in query["columns"] if "alias" in x}
        sample_rate_secs = query["sampleRateSecs"]
        max_samples = query["maxSamples"] if "maxSamples" in query else None
        return time_ranges, tags, aliases, sample_rate_secs, max_samples

    def query2(self, tags=None, time_ranges=None, period_secs=1):
        merged_dfs = []
        for time_range in time_ranges:
            dfs = []
            for tag in tags:
                ret = self.read_values(tag, time_range[0], time_range[1])
                data = ret['data']
                if len(data) == 0:
                    df = pd.DataFrame([np.NaN], index=[time_range[0]], columns=[tag])
                else:
                    times = [datetime.utcfromtimestamp(x[0]/1000) for x in data]
                    values = [x[1] for x in data]
                    df = pd.DataFrame(values, index=times, columns=[tag])
                dfs.append(df)
            dr = pd.date_range(time_range[0], time_range[1], freq='%ds' % period_secs)
            for i in range(len(dfs)):
                if not dfs[i].index.is_unique:
                    dfs[i] = dfs[i].groupby(level=0).first()
                dfs[i] = dfs[i].reindex(index=dr, method='ffill', limit=2)
            merge = dfs[0]
            for i in range(len(dfs)):
                df = dfs[i]
                for col in df.columns:
                    merge[col] = df[col]
            merged_dfs.append(merge)
        return pd.concat(merged_dfs)

    def query_from_json(self, json_str):
        time_ranges, tags, aliases, sample_rate_secs, max_samples = self._json_to_query(json_str)
        return self.query2(tags, time_ranges, sample_rate_secs)

