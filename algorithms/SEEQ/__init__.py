__author__ = 'paulmuston'

import requests
import json
from dateutil.parser import parse
import pandas as pd
from datetime import datetime


def seeq_format(dt):
    iso = dt.isoformat()
    if iso.endswith("+00:00"):
        iso = iso.split("+")[0]
    return iso+"Z"

connection = None
class Connection():
    def __init__(self, ip="localhost", port=7777):
        global connection
        self.ip = ip
        self.port = port
        connection = self

    @property
    def endpoint(self):
        return "http://%s:%d" % (self.ip, self.port)

class Series():
    def __init__(self, name):
        self.name = name
        self.href = None
        self.type = None
        self.description = None
        self.interpolationMaxGap = None
        self.interpolationMethod = None
        self.unitOfMeasure = None

    @classmethod
    def Create(cls, name, type, href):
        series = Series(name)
        series.href = href
        series.type = type
        return series

    def __repr__(self):
        return "Series(name=%s, type=%s, href=%s, description=%s, interpolationMaxGap=%s, interpolationMethod=%s, unitOfMeasure=%s)" %\
               (self.name, self.type, self.href, self.description, self.interpolationMaxGap, self.interpolationMethod, self.unitOfMeasure)

    def get(self, offset=0, limit=1000):
        url = "%s%s" % (connection.endpoint, self.href)
        # print(url)
        x = requests.get(url, params={'offset':offset, 'limit':limit})
        result = json.loads(x.content.decode("utf-8"))
        self.description = result['description'] if 'description' in result else None
        self.interpolationMaxGap = result['interpolationMaxGap'] if 'interpolationMaxGap' in result else None
        self.interpolationMethod = result['interpolationMethod'] if 'interpolationMethod' in result else None
        self.unitOfMeasure = result['unitOfMeasure'] if 'unitOfMeasure' in result else None

    def create(self):
        url = "%s/series" % connection.endpoint
        payload = {'name':self.name}
        if not self.description is None:
            payload['description'] = self.description
        if not self.interpolationMaxGap is None:
            payload['interpolationMaxGap'] = self.interpolationMaxGap
        if not self.interpolationMethod is None:
            payload['interpolationMethod'] = self.interpolationMethod
        if not self.unitOfMeasure is None:
            payload['unitOfMeasure'] = self.unitOfMeasure
        # print("posting", url, payload)
        headers = {'content-type': 'application/json'}
        response = requests.post(url, data=json.dumps(payload), headers=headers)
        response_dict = json.loads(response.content.decode("utf-8"))
        if "href" in response_dict:
            self.href = response_dict["href"]
            self.get()
        # print(response_dict)

    def delete(self, offset=0, limit=1000):
        url = "%s%s" % (connection.endpoint, self.href)
        x = requests.delete(url)

    @classmethod
    def get_all(cls, offset=0, limit=1000):
        x = requests.get("%s/series?offset=%d&limit=%d" % (connection.endpoint, offset, limit))
        return [Series.Create(x['name'], x['type'], href=x['href']) for x in json.loads(x.content.decode("utf-8"))['items']]

    def get_values(self, offset=0, limit=1000, startTime=None, endTime=None):
        url = "%s%s/values" % (connection.endpoint, self.href)
        x = requests.get(url, params={'offset':offset, 'limit':limit, 'start':seeq_format(startTime), 'end':seeq_format(endTime)})
        result = json.loads(x.content.decode("utf-8"))
        # print(result)
        dataPoints = result['dataPoints'] if 'dataPoints' in result else None
        vals = []
        ts = []
        if dataPoints:
            for dataPoint in dataPoints:
                if "value" in dataPoint:
                    vals.append(dataPoint["value"])
                    ts.append(parse(dataPoint["index"]))
            return pd.DataFrame(vals,columns=[self.name], index=ts)
        return None

    def write_values(self, df):
        url = "%s%s/values" % (connection.endpoint, self.href)
        col = df.columns[0]
        items = []
        for index, row in df.iterrows():
            item = {'index':seeq_format(index),'value':row[col]}
            items.append(item)
        payload = {'dataPoints':items}
        # print("posting", url, payload)
        headers = {'content-type': 'application/json'}
        response = requests.post(url, data=json.dumps(payload), headers=headers)
        # print(response)
        # response_dict = json.loads(response.content.decode("utf-8"))
        # print(response_dict)

class Asset():
    def __init__(self, name, type, href=None):
        self.name = name
        self.href = href
        self.type = type

    def __repr__(self):
        return "Asset(name=%s, type=%s, href=%s)" % (self.name, self.type, self.href)

    @classmethod
    def get_all(cls, offset=0, limit=1000):
        x = requests.get("%s/assets?offset=%d&limit=%d" % (connection.endpoint, offset, limit))
        return [Asset(x['name'], x['type'], href=x['href']) for x in json.loads(x.content.decode("utf-8"))['items']]

class Marker():
    def __init__(self, name):
        self.name = name
        self.href = None
        self.type = None

    @classmethod
    def Create(cls, name, type, href):
        marker = Marker(name)
        marker.href = href
        marker.type = type
        return marker

    def __repr__(self):
        return "Marker(name=%s, type=%s, href=%s)" % (self.name, self.type, self.href)

def _merge(tags, dfs):
    column = dfs[0].columns[0]
    merge = pd.DataFrame({tags[0]:dfs[0][column]},index=dfs[0].index)
    for idx in range(1,len(tags)):
        column = dfs[idx].columns[0]
        dfs[idx].reindex(dfs[0].index, method='ffill')
        merge[tags[idx]] = dfs[idx][column]
    return merge

class Capsule():
    def __init__(self, name):
        self.name = name
        self.href = None
        self.type = None
        self.interests = None
        self.description = None
        self.startTime = None
        self.endTime = None
        self.startMarker = None
        self.endMarker = None

    @classmethod
    def Create(cls, name, type, href):
        capsule = Capsule(name)
        capsule.href = href
        capsule.type = type
        return capsule

    def __repr__(self):
        return "Capsule(name=%s, type=%s, href=%s, description=%s, startTime=%s, endTime=%s)" % (self.name, self.type, self.href, self.description, self.startTime, self.endTime)

    @classmethod
    def get_all(cls, offset=0, limit=1000):
        x = requests.get("%s/capsules?offset=%d&limit=%d" % (connection.endpoint, offset, limit))
        return [Capsule.Create(x['name'], x['type'], x['href']) for x in json.loads(x.content.decode("utf-8"))['items']]

    def create(self):
        url = "%s/capsules" % connection.endpoint
        payload = {'name':self.name}
        if not self.description is None:
            payload['description'] = self.description
        if not self.startTime is None:
            payload['startTime'] = seeq_format(self.startTime)
        if not self.endTime is None:
            payload['endTime'] = seeq_format(self.endTime)
        if not self.interests is None:
            payload['interests'] = [x.href.split('/')[-1] for x in self.interests]
        # print("posting", url, payload)
        headers = {'content-type': 'application/json'}
        response = requests.post(url, data=json.dumps(payload), headers=headers)
        response_dict = json.loads(response.content.decode("utf-8"))
        if "href" in response_dict:
            self.href = response_dict["href"]
            self.get()
        # print(response_dict)

    def delete(self, offset=0, limit=1000):
        url = "%s%s" % (connection.endpoint, self.href)
        x = requests.delete(url)

    def get(self, offset=0, limit=1000):
        url = "%s%s" % (connection.endpoint, self.href)
        # print(url)
        x = requests.get(url, params={'offset':offset, 'limit':limit})
        result = json.loads(x.content.decode("utf-8"))
        # print("json", result)
        self.raw_interests = result['interests']
        self.interests = []
        for x in self.raw_interests:
            if x['type'] == 'Capsule':
                capsule = Capsule.Create(x['name'], x['type'], x['href'])
                self.interests.append(capsule)
            elif x['type'] in ['PersistedTimeSeries','StoredTimeSeries']:
                series = Series.Create(x['name'], x['type'], href=x['href'])
                self.interests.append(series)
        self.description = result['description'] if 'description' in result else None
        self.startTime = parse(result['startTime'], yearfirst=True) if 'startTime' in result else None
        self.endTime = parse(result['endTime'], yearfirst=True) if 'endTime' in result else None
        if 'startMarker' in result:
            x = result['startMarker']
            self.startMarker = Marker.Create(x['name'], x['href'], x['type'])
        if 'endMarker' in result:
            x = result['endMarker']
            self.endMarker = Marker.Create(x['name'], x['href'], x['type'])

    def get_series(self):
        series = []
        self.get(connection)
        for interest in self.interests:
            if isinstance(interest, Series):
                series.append((interest, self.startTime, self.endTime,))
            if isinstance(interest, Capsule):
                sub_series = interest.get_series()
                series.extend(sub_series)
        return series

    def get_items(self, offset=0, limit=1000, startTime=None, endTime=None):
        series = self.get_series()
        names = {x[0].name for x in series}
        # print(names)
        named_dfs = []
        for name in names:
            tag_items = [x for x in series if x[0].name==name]
            time_sorted_items = sorted(tag_items, key=lambda x: x[1])
            # print(time_sorted_items)
            dfs = [x[0].get_values(offset=offset, limit=limit, startTime=x[1], endTime=x[2]) for x in time_sorted_items]
            actual_dfs = [x for x in dfs if not x is None]
            # print(actual_dfs)
            if len(actual_dfs) > 0:
                concat_df = pd.concat(dfs)
                named_dfs.append((name,concat_df,))
        if len(named_dfs)>0:
            return _merge([x[0] for x in named_dfs], [x[1] for x in named_dfs])
        else:
            return None


def Import_SEEQ_Capsule(capsule_name):
    capsule_dict = {x.name: x for x in Capsule.get_all()}
    return capsule_dict[capsule_name].get_items()