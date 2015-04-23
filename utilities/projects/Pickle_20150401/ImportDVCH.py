__author__ = 'paulmuston'

import re
from datetime import datetime
import pandas as pd
import pickle
import os.path
from pymongo import MongoClient
from Bricks import BricksDB, Dataset


def extract_dvch_header(infile):
    max_lines = 10
    sample_type=None
    node=None
    tag=None
    for line in infile:
        line = line.strip()
        max_lines -= 1
        if(max_lines<0):
            break
        if(len(line)==0):
            continue
        if(line=="RAW HISTORY SAMPLES"):
            sample_type="raw"
        else:
            matchObj = re.search(r'(DeltaV=\S+) ([^\']+)', line)
            if(matchObj):
                node = matchObj.group(1)
                tag = matchObj.group(2)
                break
    return(node, tag, sample_type)


def unpack_dvch_line(line, fields):
    result = []
    pos = 0
    for i in fields:
        if i>0:
            result.append(line[pos:pos+i])
            pos += i
        else:
            pos += abs(i)
    return result


def _read_DvCH(path):
    time_format="%Y/%m/%d %H:%M:%S.%f"
    fields = [24,-2,1,-2,3,-2,3,-2,9999]

    with open(path, 'r') as infile:
        node, tag, sample_type = extract_dvch_header(infile)
        timestamps = []
        values = []
        for line in infile:
            try:
                line = line.strip()
                if(len(line)==0):
                    continue
                if line.startswith("Raw samples returned"):
                    break
                cols = unpack_dvch_line(line, fields)
                dt_str, type, status, misc1, val = cols
                ts = datetime.strptime(dt_str+"00",time_format)
                if int(status.lstrip()) < 128:
                    val = float("NaN")
                if type=="F":
                    val = float(val)
                elif type=="I":
                    val = int(val)
                timestamps.append(ts)
                values.append(val)
            except Exception as e:
                print("Problem: %s:", e)
                print(cols)
    meta = {"import_type":"DvCH", "tag":tag, "path":path}
    return (pd.DataFrame({tag :values},index=timestamps), meta)

connection = MongoClient()
bricks_db = BricksDB(connection, "PICKLE-20150401")

file_list = """TT60712__INPUT_1.txt
TT6079__INPUT_1.txt
TT6076B__INPUT_1.txt
TT6070__INPUT_1.txt
PT615__WIRED_PSIA.txt
FT640B__DENS.txt
FT630B__DENS.txt
FT603__DENSITY__OUT.txt
FT601__DENSITY__OUT.txt
FT600_FB__DENSITY__PV.txt
FT600__DEN__OUT.txt"""

source_dir = '/volumes/alper/Data/Pickle/DWC/2015-04-01'

file_names = [x for x in file_list.split("\n")]

for filename in file_names:
    print("importing", filename)
    df, meta = _read_DvCH(os.path.join(source_dir, filename))
    data_set = Dataset.Create(bricks_db, name=filename.replace(".txt", ""), tags=[meta["tag"]], labels=["DWC"])
    data_set.Store_Data(df)