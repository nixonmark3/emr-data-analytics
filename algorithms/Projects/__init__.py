import pandas as pd
import pickle
import sys

from Exceptions import NotFoundException
from pymongo import ASCENDING
from pymongo.database import Database
from datetime import datetime
from bson import ObjectId

PROJECT_ID = 1
PICKLE_VERSION = 4
rows_per_chunk = 10000


class Project:
    project_preamble = "das-"

    def __init__(self, connection, name):
        self.name = name
        self.db = Database(connection, Project.project_preamble+name)
        self.id = PROJECT_ID

    @classmethod
    def all(cls, connection):
        return [d[len(cls.project_preamble):] for d in connection.database_names() if d.startswith(cls.project_preamble)]

    def __repr__(self):
        return 'Project(connection, %s)' % self.name

    @classmethod
    def Create(cls, connection, name):
        project = Project(connection, name)
        db = project.db
        if not db.project.find_one():
            db.project.insert({"created":datetime.now()})
        return project


class DataBase:
    def Store_Data(self, df):
        db = self.project.db
        db.chunk.remove({"dataset_id":self.id})
        row_count = df.shape[0]
        pages = int(row_count/rows_per_chunk)+1
        total = 0
        for page in range(pages):
            chunk = df[page*rows_per_chunk:(page+1)*rows_per_chunk]
            print("store columns", chunk.columns)
            p = pickle.dumps(chunk, PICKLE_VERSION)
            print(sys.getsizeof(p))
            size = sys.getsizeof(p)
            db.chunk.insert({"dataset_id":self.id, "page":page, "data":p, "size":size})
            total += size
        return total

    def Load_Data(self):
        db = self.project.db
        db.chunk.ensure_index([("dataset_id", ASCENDING),("page", ASCENDING)])
        #db.chunk.ensure_index([("name", ASCENDING),("page", ASCENDING)])
        chunks = []
        #store in chunks of n rows per page
        for page in db.chunk.find({"dataset_id": self.id}).sort("page", ASCENDING):
            p = page["data"]
            chunk = pickle.loads(p)
            chunks.append(chunk)
        return pd.concat(chunks)


class Dataset(DataBase):
    def __init__(self, project, name=None, id=None):
        db = project.db
        self.name = name
        self.id = id
        self.project = project
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
    def all(cls, project):
        db = project.db
        return [(d['name'],d['_id']) for d in db.dataset.find()]

    def _db_read(self):
        db = self.project.db
        return db.dataset.find_one({"_id": ObjectId(self.id)})

    @property
    def meta(self):
        item = self._db_read()
        return item["meta_data"]

    @meta.setter
    def meta(self, value):
        db = self.project.db
        db.dataset.update({"_id": ObjectId(self.id)},{'$set':{'meta':value}})

    def add_meta(self, data):
        db = self.project.db
        update_dict = {"meta_data."+key : val for key,val in data.items()}
        db.dataset.update({"_id": ObjectId(self.id)},{ "$set": update_dict })

    def update(self, data):
        db = self.project.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$set": data })

    @classmethod
    def Create(cls, project, name):
        db = project.db
        data_set = db.dataset.find_one({"name":name})
        if not data_set:
            db.dataset.insert({"name":name, "meta_data":{"created":datetime.now()}, "labels":[]})
        return Dataset(project, name=name)

    @property
    def labels(self):
        item = self._db_read()
        return item["labels"]

    def add_label(self, tag):
        db = self.project.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$addToSet": { "labels": tag } })

    def remove_label(self, tag):
        db = self.project.db
        db.dataset.update({"_id": ObjectId(self.id)},{ "$pull": { "labels": tag } })

    def delete(self):
        db = self.project.db
        db.dataset.remove({"_id": ObjectId(self.id)})
        db.chunk.remove({"dataset_id":self.id})

    def __repr__(self):
        return 'Dataset(project, %s, %s)' % (self.name, self.id)

