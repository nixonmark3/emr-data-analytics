from LoadDB import LoadDB
from FillNa import FillNa
from Split import Split
from Columns import Columns
from TimeDelay import TimeDelay
from Shift import Shift
from PLS import PLS

blocks_to_execute = []

_8ad8d928_6562_97da_f790_e9a60d6f5ffa = LoadDB('Load','8ad8d928-6562-97da-f790-e9a60d6f5ffa')
_8ad8d928_6562_97da_f790_e9a60d6f5ffa.parameters = {'Project': 'Huntsman05202015', 'Data Set': 'Evaporator', 'Plot': 'False'}
_8ad8d928_6562_97da_f790_e9a60d6f5ffa.input_connectors = {}
blocks_to_execute.append(_8ad8d928_6562_97da_f790_e9a60d6f5ffa)

_29300ef7_0377_0b40_5674_e0f05ee87592 = FillNa('Fill NaN','29300ef7-0377-0b40-5674-e0f05ee87592')
_29300ef7_0377_0b40_5674_e0f05ee87592.parameters = {}
_29300ef7_0377_0b40_5674_e0f05ee87592.input_connectors = {'in': ['8ad8d928-6562-97da-f790-e9a60d6f5ffa/out']}
blocks_to_execute.append(_29300ef7_0377_0b40_5674_e0f05ee87592)

_ab578af7_6f45_d3e1_af4a_c8162dad2562 = Split('Split Data','ab578af7-6f45-d3e1-af4a-c8162dad2562')
_ab578af7_6f45_d3e1_af4a_c8162dad2562.parameters = {'Split': 75}
_ab578af7_6f45_d3e1_af4a_c8162dad2562.input_connectors = {'in': ['29300ef7-0377-0b40-5674-e0f05ee87592/out']}
blocks_to_execute.append(_ab578af7_6f45_d3e1_af4a_c8162dad2562)

_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b = Columns('x train','e8ad529e-f3e7-0dfc-2d62-4ff565c2db5b')
_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b.parameters = {'Columns': ['Fd To Evap-Valve', 'Fd To Evap-Calc', 'Steam to EH250', 'Stm Vlv To EH250', 'EV250 Temp', 'Evap Systm Press', 'Rewk From D295', 'EV230 to CY240', 'EF250 to EV230', 'EV250 Press', 'EV230 Press', 'T255 Reflux', 'ER255 Stm Flow', 'DF260 to t260', 'DF260 to T260_SP']}
_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b.input_connectors = {'in': ['ab578af7-6f45-d3e1-af4a-c8162dad2562/train']}
blocks_to_execute.append(_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b)

_3f18e8a9_3ff9_d300_bb99_abca40684bf3 = Columns('y train','3f18e8a9-3ff9-d300-bb99-abca40684bf3')
_3f18e8a9_3ff9_d300_bb99_abca40684bf3.parameters = {'Columns': ['sample']}
_3f18e8a9_3ff9_d300_bb99_abca40684bf3.input_connectors = {'in': ['ab578af7-6f45-d3e1-af4a-c8162dad2562/train']}
blocks_to_execute.append(_3f18e8a9_3ff9_d300_bb99_abca40684bf3)

_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c = TimeDelay('Time Delay','9d1ed14b-9b73-76fe-bfe8-066c7f34e21c')
_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c.parameters = {'Max Lag': 1000}
_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c.input_connectors = {'x': ['e8ad529e-f3e7-0dfc-2d62-4ff565c2db5b/out'], 'y': ['3f18e8a9-3ff9-d300-bb99-abca40684bf3/out']}
blocks_to_execute.append(_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c)

_c3921ace_0e52_9a9c_1af8_91193629a389 = Shift('Shift Data','c3921ace-0e52-9a9c-1af8-91193629a389')
_c3921ace_0e52_9a9c_1af8_91193629a389.parameters = {}
_c3921ace_0e52_9a9c_1af8_91193629a389.input_connectors = {'delay': ['9d1ed14b-9b73-76fe-bfe8-066c7f34e21c/out'], 'in': ['e8ad529e-f3e7-0dfc-2d62-4ff565c2db5b/out']}
blocks_to_execute.append(_c3921ace_0e52_9a9c_1af8_91193629a389)

_5076011d_7e99_3c0c_37fc_06cd097d4395 = Split('Split2','5076011d-7e99-3c0c-37fc-06cd097d4395')
_5076011d_7e99_3c0c_37fc_06cd097d4395.parameters = {'Split': 2}
_5076011d_7e99_3c0c_37fc_06cd097d4395.input_connectors = {'in': ['c3921ace-0e52-9a9c-1af8-91193629a389/out']}
blocks_to_execute.append(_5076011d_7e99_3c0c_37fc_06cd097d4395)

_caa27033_5fa9_6f7c_e999_54693b626755 = Split('Split3','caa27033-5fa9-6f7c-e999-54693b626755')
_caa27033_5fa9_6f7c_e999_54693b626755.parameters = {'Split': 2}
_caa27033_5fa9_6f7c_e999_54693b626755.input_connectors = {'in': ['3f18e8a9-3ff9-d300-bb99-abca40684bf3/out']}
blocks_to_execute.append(_caa27033_5fa9_6f7c_e999_54693b626755)

_b4dcfaf5_0507_580d_64f8_4ac934259a79 = PLS('PLS1','b4dcfaf5-0507-580d-64f8-4ac934259a79')
_b4dcfaf5_0507_580d_64f8_4ac934259a79.parameters = {}
_b4dcfaf5_0507_580d_64f8_4ac934259a79.input_connectors = {'x': ['5076011d-7e99-3c0c-37fc-06cd097d4395/test'], 'y': ['caa27033-5fa9-6f7c-e999-54693b626755/test']}
blocks_to_execute.append(_b4dcfaf5_0507_580d_64f8_4ac934259a79)

results_table = {}

for block in blocks_to_execute:
    results_table.update(block.execute(results_table))