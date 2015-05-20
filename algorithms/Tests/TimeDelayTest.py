__author__ = 'BigDataAnalytics'

from LoadDB import LoadDB
from Columns import Columns
from TimeDelay import TimeDelay

blocks_to_execute = []

_eec88527_2273_640c_4ea2_31227e3e4028 = LoadDB('LoadDB1','eec88527-2273-640c-4ea2-31227e3e4028')
_eec88527_2273_640c_4ea2_31227e3e4028.parameters = {'Project': 'TestData', 'Data Set': '7in_1out', 'Plot': 'False'}
_eec88527_2273_640c_4ea2_31227e3e4028.input_connectors = {}
blocks_to_execute.append(_eec88527_2273_640c_4ea2_31227e3e4028)

_98cc8ab9_5b87_642c_2c16_11032d6bcaa8 = Columns('Columns1','98cc8ab9-5b87-642c-2c16-11032d6bcaa8')
_98cc8ab9_5b87_642c_2c16_11032d6bcaa8.parameters = {'Columns': ['Tag1', 'Tag2', 'Tag3', 'Tag4', 'Tag5', 'Tag6', 'Tag7']}
_98cc8ab9_5b87_642c_2c16_11032d6bcaa8.input_connectors = {'in': ['eec88527-2273-640c-4ea2-31227e3e4028/out']}
blocks_to_execute.append(_98cc8ab9_5b87_642c_2c16_11032d6bcaa8)

_10aef685_4fbc_ee9a_b81b_bf3e534a3a69 = Columns('Columns2','10aef685-4fbc-ee9a-b81b-bf3e534a3a69')
_10aef685_4fbc_ee9a_b81b_bf3e534a3a69.parameters = {'Columns': ['Y']}
_10aef685_4fbc_ee9a_b81b_bf3e534a3a69.input_connectors = {'in': ['eec88527-2273-640c-4ea2-31227e3e4028/out']}
blocks_to_execute.append(_10aef685_4fbc_ee9a_b81b_bf3e534a3a69)

_a3ebc2a0_8dd8_385b_4020_5d41e9926263 = TimeDelay('TimeDelay1','a3ebc2a0-8dd8-385b-4020-5d41e9926263')
_a3ebc2a0_8dd8_385b_4020_5d41e9926263.parameters = {'Percent': 10}
_a3ebc2a0_8dd8_385b_4020_5d41e9926263.input_connectors = {'x': ['98cc8ab9-5b87-642c-2c16-11032d6bcaa8/out'], 'y': ['10aef685-4fbc-ee9a-b81b-bf3e534a3a69/out']}
blocks_to_execute.append(_a3ebc2a0_8dd8_385b_4020_5d41e9926263)

results_table = {}

for block in blocks_to_execute:
    results_table.update(block.execute(results_table))


