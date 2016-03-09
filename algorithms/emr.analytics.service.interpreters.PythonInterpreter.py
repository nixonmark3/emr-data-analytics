import sys, getopt, traceback
import json
import numpy as np
import pandas as pd

from py4j.java_gateway import java_import, JavaGateway, GatewayClient
from py4j.protocol import Py4JJavaError

# reference the py4J client
client = GatewayClient(port=int(sys.argv[1]))
gateway = JavaGateway(client, auto_convert = True)

# reference interpreter
interpreter = gateway.entry_point# initialize the output dictionary
output = {}

# collection of methods
class DataGateway(object):

    # sends a dataframe's schema and data to the interpreter
    def collect(self, dataFrame):
        schema = json.dumps(self.schema(dataFrame))
        interpreter.collect(schema, dataFrame.to_json(orient='values'))

    #
    def columns(self, dataFrame):
        interpreter.onNotify('DATA', json.dumps(dataFrame.columns.values.tolist()))

    # sends a dataframe's schema and describe statistics to the interpreter
    def describe(self, name, dataFrame):
        # get schema
        schema = self.schema(dataFrame)
        # create histograms
        hist = self.histograms(schema, dataFrame)
        # capture statistics
        stats = dataFrame.describe(percentiles=None)
        # send stats to interpreter
        interpreter.describe(name, json.dumps(schema), json.dumps(hist), stats.to_json(orient='split'))
        return stats

    # build schema
    def schema(self, dataFrame):
        dataTypes = dataFrame.dtypes
        columns = dataTypes.index
        return [{'name': columns[i], 'type': dataTypes[i].name} for i in range(len(dataTypes))]

    # create a histogram for each dataframe column
    def histograms(self, schema, dataFrame):
        binCount = 10
        hist = []
        for item in schema:
            if item['type'] == 'float64':
                data, edges = np.histogram(dataFrame[item['name']], binCount)
                hist.append({ 'name': item['name'], 'data': data.tolist(), 'edges': edges.tolist() })
        return hist

    # select specified columns and collect
    def select(self, columns, dataFrame):
        features = dataFrame.loc[:, columns]
        self.collect(features)

dataGateway = DataGateway()
# notify the client that the python script has been initialized
interpreter.onPythonScriptInitialized()

while True:
    request = interpreter.getStatements()
    try:
        statements = request.getStatements().split("\n")
        source = None

        for statement in statements:
            if statement == None or len(statement.strip()) == 0:
                continue

            # skip comment
            if statement.strip().startswith("#"):
                continue

            if source:
                source += "\n" + statement
            else:
                source = statement

        if source:
            compiledCode = compile(source, "<string>", "exec")
            eval(compiledCode)

        interpreter.setStatementsComplete()

    except Py4JJavaError:
        interpreter.setStatementsFailed(traceback.format_exc())
        # interpreter.setStatementsFailed(traceback.format_exc() + str(sys.exc_info()))

    except:
        interpreter.setStatementsFailed(traceback.format_exc())