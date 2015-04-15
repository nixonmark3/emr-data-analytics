from LoadDB import LoadDB
from Columns import Columns


def driver():
    blocks_to_execute = []

    load = LoadDB('Load1')
    load.parameters = {'Project': '', 'Data Set': '/users/BigDataAnalytics/IdeaProjects/emr-data-analytics/algorithms/tests/df1.txt'}
    blocks_to_execute.append(load)

    columns = Columns('Columns1')
    columns.parameters = {'Columns': ['10L1019', '10P1019']}
    columns.input_connectors = {'in': ['Load1/out']}
    blocks_to_execute.append(columns)

    results_table = {}

    for block in blocks_to_execute:
        results_table.update(block.execute(results_table))


def main():
    driver()

if __name__ == '__main__':
    main()