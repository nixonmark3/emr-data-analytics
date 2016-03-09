
# collection of methods
class DataGateway(object):

    # sends a dataframe's schema and data to the interpreter
    def collect(self, dataFrame):
        # get schema
        schema = json.dumps(self.schema(dataFrame))
        interpreter.collect(schema, dataFrame.toPandas().to_json(orient='values'))

    # sends a dataframe's schema and describe statistics to the interpreter
    def describe(self, dataFrame):
        # get schema
        schema = json.dumps(self.schema(dataFrame))
        # execute describe and convert to pandas dataframe
        stats = dataFrame.describe().toPandas()
        interpreter.describe(schema, stats.to_json(orient='split'))

    def schema(self, dataFrame):
        dataTypes = dataFrame.schema.fields
        return [{'name': dataTypes[i].name, 'type': dataTypes[i].dataType.typeName()} for i in range(len(dataTypes))]

    # select specified columns and collect
    def select(self, columns, dataFrame):
        features = dataFrame.select(columns)
        self.collect(features)

dataGateway = DataGateway()
