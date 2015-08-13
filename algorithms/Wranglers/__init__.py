import pandas as pd
import numpy as np
import csv
import dateutil


def import_csv(filename, time_series):
    if time_series:
        df = pd.read_csv(filename, parse_dates=True, index_col=0)
    else:
        df = pd.read_csv(filename, index_col=0)
    return df


def import_ff3(filename):
    df, meta = read_ff3(filename)
    return df


def import_cda(filename):
    df = read_cda(filename)
    return df


def read_ff3(filename):
    with open(filename) as csv_file:
        rows = csv.reader(csv_file, delimiter="\t")
        header = next(rows)
        num_cols = len(header)
        timestamps = []
        values_list = []
        for row in rows:
            ts = dateutil.parser.parse(row[0], yearfirst=True)
            timestamps.append(ts)
            values = []
            for x in row[2:num_cols]:
                try:
                    x = float(x)
                except:
                    x = np.nan
                values.append(x)
            values_list.append(values)
    meta = {"import_type": "FF3", "path": filename}
    return pd.DataFrame(values_list, index=timestamps, columns=header[2:]), meta


def time_range(df):
    count = len(df)
    return df.index[0], df.index[count-1]


def read_cda(filename):
    header, columns, rows = read_cda_config(filename)
    header = process_header(header)
    columns = process_columns(columns)
    rows = process_rows(rows)
    index = pd.date_range('1/1/2000', periods=header['periods'], freq=header['frequency'])
    df = pd.DataFrame(rows, index=index, columns=columns)
    return df


def read_cda_config(path):
    rows = []
    with open(path, "rt") as f:
        reader = csv.reader(f, delimiter="\t")
        header = next(reader)
        header.append(next(reader))
        header.append(next(reader))
        next(reader)
        columns = next(reader)
        next(reader)
        for row in reader:
            rows.append(row)
    return header, columns, rows


def process_header(header):
    name = header[0]
    inputs = header[1][0]
    outputs = header[1][1]
    periods = int(header[2][0])
    frequency = '{0}S'.format(int(float(header[2][1])))
    return {'name': name, 'inputs': inputs, 'outputs': outputs, 'periods': periods, 'frequency': frequency}


def process_columns(columns):
    modified_columns = []
    columns.pop(0)
    for index, item in enumerate(columns):
        item = item.replace(' ', '-')
        modified_columns.append(item)
    return modified_columns


def process_rows(rows):
    rows.pop()
    replace_text = ' 128 0'
    modified_rows = []
    for row in rows:
        row.pop(0) # remove the first column as it is the index
        for index, item in enumerate(row):
            item = item.replace(replace_text, '')
            item = float(item)
            row[index] = item
        modified_rows.append(row)
    return modified_rows