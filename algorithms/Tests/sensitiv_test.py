
from Columns import Columns
from Sensitivity import Sensitivity
from DataBrick import DataBrick
from LoadDB import LoadDB


def driver():
    blocks_to_execute = []

#     DataBrick1 = DataBrick('DataBrick1', 'DataBrick1_Test')
#     DataBrick1.parameters = {'Project': 'PICKLE-20150401', 'Query': {
#   "query_name": "query1",
#   "docType": "json",
#   "version": "1.0",
#   "timeSelector": [
#     {
#       "startTime": "2015-02-09T16:30:00.000Z",
#       "endTime": "2015-02-11T02:16:30.000Z"
#     },
#     {
#       "startTime": "2015-02-24T015:30:00.000Z",
#       "endTime": "2015-02-26T10:30:00.000Z"
#     },
#     {
#       "startTime": "2015-03-06T08:30:00.000Z",
#       "endTime": "2015-03-10T16:30:00.000Z"
#     }
#   ],
#   "sampleRateSecs": 1,
#   "columns": [
#     {
#       "tag": "FT630B/DENS.CV",
#       "alias": "Flow",
#       "dataType": "Float",
#       "renderType": "VALUE",
#       "format": "0.###"
#     },
#     {
#       "tag": "PT615/WIRED_PSIA.CV",
#       "alias": "Pressure",
#       "dataType": "Float",
#       "renderType": "VALUE",
#       "format": "0.###"
#     },
#     {
#       "tag": "TT6079/INPUT_1.CV",
#       "alias": "Temperature",
#       "dataType": "Float",
#       "renderType": "VALUE",
#       "format": "0.###"
#     },
#     {
#       "tag": "630_mass_fraction_c5",
#       "alias": "630_MASS_FRAC_C5",
#       "dataType": "Float",
#       "renderType": "VALUE",
#       "format": "0.###"
#     }
#   ]
# }}
#     DataBrick1.input_connectors = {}
#     blocks_to_execute.append(DataBrick1)

    loaddb1 = LoadDB('LoadDB1', 'LoadDB_test1')
    loaddb1.parameters = {'Project': '7in1outData', 'Data Set': '7in1out', 'Plot': 'False'}
    loaddb1.input_connectors = {}
    blocks_to_execute.append(loaddb1)

    columns1 = Columns('Columns1','Columns1_test')
    columns1.parameters = {'Columns': ['x1', 'x2', 'x3', 'x4', 'x5', 'x6', 'x7']}
    columns1.input_connectors = {'in': ['LoadDB_test1/out']}
    blocks_to_execute.append(columns1)


    columns2 = Columns('Columns2', 'Columns2_test')
    columns2.parameters = {'Columns': ['y_noise']}
    columns2.input_connectors = {'in': ['LoadDB_test1/out']}
    blocks_to_execute.append(columns2)

    sensitivity = Sensitivity('Sensitivity1', 'Sensitivity1_test')
    sensitivity.input_connectors = {'x' : ['Columns1_test/out'], 'y' : ['Columns2_test/out']}
    blocks_to_execute.append(sensitivity)



    results_table = {}

    for block in blocks_to_execute:
        results_table.update(block.execute(results_table))


def main():
    driver()

if __name__ == '__main__':
    main()