__author__ = 'paulmuston'

import sys
import pickle
import pandas as pd
import numpy as np
from pymongo import Connection, ASCENDING
from pymongo.database import Database
from bson import ObjectId
from datetime import datetime, timedelta
from operator import itemgetter
#import Pipes.Misc

class NotFoundException(Exception):
    pass

class ParameterException(Exception):
    pass

PICKLE_VERSION = 4
rows_per_brick = 10000

class BricksDB:
    bricks_preamble = "bricks-"
    def __init__(self, connection, name):
        self.name = name
        self.db = Database(connection, BricksDB.bricks_preamble+name)

    @classmethod
    def all(cls, connection):
        return [d[len(cls.bricks_preamble):] for d in connection.database_names() if d.startswith(cls.bricks_preamble)]

    def __repr__(self):
        return 'BricksDB(connection, %s)' % (self.name)

    @classmethod
    def Create(cls, connection, name):
        bricks_db = BricksDB(connection, name)
        db = bricks_db.db
        if not db.project.find_one():
            db.bricksDB.insert({"created":datetime.now()})
        return bricks_db

    def query(self, tags=None, time_range=None, period_secs=1):
        bricks = DataBrick.query(self, tags, time_range, data=True)
        datasets = {x["dataset_id"] for x in bricks}
        dfs = []
        for dataset in datasets:
            sub_frames = []
            ds_bricks = [x for x in bricks if x["dataset_id"] == dataset]
            ds_bricks = sorted(ds_bricks, key=itemgetter("page"))
            for ds_brick in ds_bricks:
                b = ds_brick["data"]
                sub_frame = pickle.loads(b)
                sub_frames.append(sub_frame)
            df = pd.concat(sub_frames)
            dfs.append(df)
            second = timedelta(seconds=period_secs)
        dr = pd.date_range(time_range[0], time_range[1], freq='1s')
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


class Dataset:
    def __init__(self, bricks_db, name=None, id=None):
        db = bricks_db.db
        self.name = name
        self.id = id
        self.bricks_db = bricks_db
        if id:
            data_set = db.dataset.find_one({"_id": ObjectId(id)})
            if not data_set:
                raise NotFoundException
            self.name = data_set["name"]
        elif name:
            data_set = db.dataset.find_one({"name": name})
            if not data_set:
                raise NotFoundException
            self.id = str(data_set["_id"])

    @classmethod
    def all(cls, bricks_db):
        db = bricks_db.db
        return [(d['name'],d['_id']) for d in db.dataset.find()]

    @classmethod
    def all_tags(cls, bricks_db):
        db = bricks_db.db
        return [d for d in db.dataset.find().distinct("tags")]

    @classmethod
    def all_labels(cls, bricks_db):
        db = bricks_db.db
        return [d for d in db.dataset.find().distinct("labels")]

    def _db_read(self):
        db = self.bricks_db.db
        return db.dataset.find_one({"_id": ObjectId(self.id)})

    @property
    def meta(self):
        item = self._db_read()
        return item["meta_data"]

    @meta.setter
    def meta(self, value):
        db = self.bricks_db.db
        db.dataset.update({"_id": ObjectId(self.id)},{'$set':{'meta':value}})

    def add_meta(self, data):
        db = self.bricks_db.db
        update_dict = {"meta_data."+key : val for key,val in data.items()}
        db.dataset.update({"_id": ObjectId(self.id)},{ "$set": update_dict })

    def update(self, data):
        db = self.bricks_db.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$set": data })

    @classmethod
    def Create(cls, bricks_db, name, tags=[], labels=[]):
        db = bricks_db.db
        data_set = db.dataset.find_one({"name":name})
        if not data_set:
            db.dataset.insert({"name":name, "meta_data":{"created":datetime.now()}, "labels":labels, "tags":tags})
        return Dataset(bricks_db, name=name)

    @property
    def labels(self):
        item = self._db_read()
        return item["labels"]

    def add_label(self, label):
        db = self.bricks_db.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$addToSet": { "labels": label } })

    def remove_label(self, label):
        db = self.bricks_db.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$pull": { "labels": label } })

    @property
    def tags(self):
        item = self._db_read()
        return item["tags"]

    def add_tag(self, tag):
        db = self.bricks_db.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$addToSet": { "tags": tag } })

    def remove_tag(self, tag):
        db = self.bricks_db.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$pull": { "tags": tag } })

    def delete(self):
        db = self.bricks_db.db
        db.dataset.remove({"_id": ObjectId(self.id)})
        db.chunk.remove({"dataset_id":self.id})

    def __repr__(self):
        return 'Dataset(project, %s, %s)' % (self.name, self.id)

    def Store_Data(self, df):
        db = self.bricks_db.db
        db.brick.remove({"dataset_id":self.id})
        row_count = df.shape[0]
        pages = int(row_count/rows_per_brick)+1
        total = 0
        for page in range(pages):
            brick = df[page*rows_per_brick:(page+1)*rows_per_brick]
            print("store columns", brick.columns)
            p = pickle.dumps(brick, PICKLE_VERSION)
            start = brick.index[0]
            end = brick.index[-1]
            print(sys.getsizeof(p))
            size = sys.getsizeof(p)
            db.brick.insert({"dataset_id":self.id, "page":page, "data":p, "size":size, "start":start, "end":end, "tags":self.tags})
            total += size
        return total

    def Load_Data(self):
        db = self.bricks_db.db
        db.brick.ensure_index([("name", ASCENDING),("page", ASCENDING)])
        bricks = []
        #store in chunks of n rows per page
        for page in db.chunk.find({"dataset_id":self.id}).sort("page",ASCENDING):
            p = page["data"]
            brick = pickle.loads(p)
            bricks.append(brick)
        return pd.concat(bricks)


class DataBrick:
    @classmethod
    def all_with_tag(cls, bricks_db, tag=None, data=False):
        db = bricks_db.db
        projection = None if data else {"data":0}
        if not tag is None:
            return [d for d in db.brick.find({'tags':tag},projection)]

    @classmethod
    def query(cls, bricks_db, tags=None, time_range=None, data=False):
        db = bricks_db.db
        projection = None if data else {"data":0}
        if not tags is None:
            if not time_range is None:
                time_query_within = {'$and':[{'start':{'$gte': time_range[0]}}, {'end':{'$lte': time_range[1]}}]}
                time_query_straddles_start = {'$and':[{'start':{'$lt': time_range[0]}}, {'end':{'$gt': time_range[0]}}]}
                time_query_straddles_end = {'$and':[{'start':{'$lt': time_range[1]}}, {'end':{'$gt': time_range[1]}}]}
                time_query = {'$or':[time_query_within, time_query_straddles_start, time_query_straddles_end]}
                tags_query = {'$or':[{'tags':x} for x in tags]}
                query = {'$and':[time_query, tags_query]}
                return [d for d in db.brick.find(query,projection)]
            else:
                tags_query = [{'tags':x} for x in tags]
                return [d for d in db.brick.find({'$or':tags_query},projection)]
        else:
            if not time_range is None:
                time_query_within = {'$and':[{'start':{'$gte': time_range[0]}}, {'end':{'$lte': time_range[1]}}]}
                time_query_straddles_start = {'$and':[{'start':{'$lt': time_range[0]}}, {'end':{'$gt': time_range[0]}}]}
                time_query_straddles_end = {'$and':[{'start':{'$lt': time_range[1]}}, {'end':{'$gt': time_range[1]}}]}
                time_query = {'$or':[time_query_within, time_query_straddles_start, time_query_straddles_end]}
                return [d for d in db.brick.find(time_query,projection)]
            else:
                return []

