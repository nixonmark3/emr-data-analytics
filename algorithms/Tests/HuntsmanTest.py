from LoadDB import LoadDB
from FillNa import FillNa
from Split import Split
from Columns import Columns
from TimeDelay import TimeDelay
from Shift import Shift
from PLS import PLS
from PLSTest import PLSTest

blocks_to_execute = []

_8ad8d928_6562_97da_f790_e9a60d6f5ffa = LoadDB('Load','8ad8d928-6562-97da-f790-e9a60d6f5ffa')
_8ad8d928_6562_97da_f790_e9a60d6f5ffa.parameters = {'Project': 'Huntsman05202015', 'Data Set': 'Evaporator', 'Plot': 'False'}
_8ad8d928_6562_97da_f790_e9a60d6f5ffa.input_connectors = {}
blocks_to_execute.append(_8ad8d928_6562_97da_f790_e9a60d6f5ffa)

_29300ef7_0377_0b40_5674_e0f05ee87592 = FillNa('Fill NaN','29300ef7-0377-0b40-5674-e0f05ee87592')
_29300ef7_0377_0b40_5674_e0f05ee87592.parameters = {}
_29300ef7_0377_0b40_5674_e0f05ee87592.input_connectors = {'in': ['8ad8d928-6562-97da-f790-e9a60d6f5ffa/out']}
blocks_to_execute.append(_29300ef7_0377_0b40_5674_e0f05ee87592)

_d04aef8e_fc51_d2b5_4d30_84989e8c8ca2 = Split('Split5','d04aef8e-fc51-d2b5-4d30-84989e8c8ca2')
_d04aef8e_fc51_d2b5_4d30_84989e8c8ca2.parameters = {'Split': 75}
_d04aef8e_fc51_d2b5_4d30_84989e8c8ca2.input_connectors = {'in': ['29300ef7-0377-0b40-5674-e0f05ee87592/out']}
blocks_to_execute.append(_d04aef8e_fc51_d2b5_4d30_84989e8c8ca2)

_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b = Columns('x train','e8ad529e-f3e7-0dfc-2d62-4ff565c2db5b')
_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b.parameters = {'Columns': ['Fd To Evap-Valve', 'Fd To Evap-Calc', 'Steam to EH250', 'Stm Vlv To EH250', 'EV250 Temp', 'Evap Systm Press', 'Rewk From D295', 'EV230 to CY240', 'EF250 to EV230', 'EV250 Press', 'EV230 Press', 'T255 Reflux', 'ER255 Stm Flow', 'DF260 to t260', 'DF260 to T260_SP']}
_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b.input_connectors = {'in': ['d04aef8e-fc51-d2b5-4d30-84989e8c8ca2/out1']}
blocks_to_execute.append(_e8ad529e_f3e7_0dfc_2d62_4ff565c2db5b)

_3f18e8a9_3ff9_d300_bb99_abca40684bf3 = Columns('y train','3f18e8a9-3ff9-d300-bb99-abca40684bf3')
_3f18e8a9_3ff9_d300_bb99_abca40684bf3.parameters = {'Columns': ['sample']}
_3f18e8a9_3ff9_d300_bb99_abca40684bf3.input_connectors = {'in': ['d04aef8e-fc51-d2b5-4d30-84989e8c8ca2/out1']}
blocks_to_execute.append(_3f18e8a9_3ff9_d300_bb99_abca40684bf3)

_744d395d_33f5_610e_964f_4aa55a405c09 = Columns('x test','744d395d-33f5-610e-964f-4aa55a405c09')
_744d395d_33f5_610e_964f_4aa55a405c09.parameters = {'Columns': ['Fd To Evap-Valve', 'Fd To Evap-Calc', 'Steam to EH250', 'Stm Vlv To EH250', 'EV250 Temp', 'Evap Systm Press', 'Rewk From D295', 'EV230 to CY240', 'EF250 to EV230', 'EV250 Press', 'EV230 Press', 'T255 Reflux', 'ER255 Stm Flow', 'DF260 to t260', 'DF260 to T260_SP']}
_744d395d_33f5_610e_964f_4aa55a405c09.input_connectors = {'in': ['d04aef8e-fc51-d2b5-4d30-84989e8c8ca2/out2']}
blocks_to_execute.append(_744d395d_33f5_610e_964f_4aa55a405c09)

_1ab23dce_e9dc_b588_70c6_cb05b92cd0c2 = Columns('y test','1ab23dce-e9dc-b588-70c6-cb05b92cd0c2')
_1ab23dce_e9dc_b588_70c6_cb05b92cd0c2.parameters = {'Columns': ['sample']}
_1ab23dce_e9dc_b588_70c6_cb05b92cd0c2.input_connectors = {'in': ['d04aef8e-fc51-d2b5-4d30-84989e8c8ca2/out2']}
blocks_to_execute.append(_1ab23dce_e9dc_b588_70c6_cb05b92cd0c2)

_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c = TimeDelay('Time Delay','9d1ed14b-9b73-76fe-bfe8-066c7f34e21c')
_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c.parameters = {'Max Lag': 1000}
_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c.input_connectors = {'x': ['e8ad529e-f3e7-0dfc-2d62-4ff565c2db5b/out'], 'y': ['3f18e8a9-3ff9-d300-bb99-abca40684bf3/out']}
blocks_to_execute.append(_9d1ed14b_9b73_76fe_bfe8_066c7f34e21c)

_c3921ace_0e52_9a9c_1af8_91193629a389 = Shift('Shift Data','c3921ace-0e52-9a9c-1af8-91193629a389')
_c3921ace_0e52_9a9c_1af8_91193629a389.parameters = {}
_c3921ace_0e52_9a9c_1af8_91193629a389.input_connectors = {'delay': ['9d1ed14b-9b73-76fe-bfe8-066c7f34e21c/out'], 'in': ['e8ad529e-f3e7-0dfc-2d62-4ff565c2db5b/out']}
blocks_to_execute.append(_c3921ace_0e52_9a9c_1af8_91193629a389)

_add50b60_5313_e74b_fb14_9a391f9160bc = Split('Split9','add50b60-5313-e74b-fb14-9a391f9160bc')
_add50b60_5313_e74b_fb14_9a391f9160bc.parameters = {'Split': 5}
_add50b60_5313_e74b_fb14_9a391f9160bc.input_connectors = {'in': ['3f18e8a9-3ff9-d300-bb99-abca40684bf3/out']}
blocks_to_execute.append(_add50b60_5313_e74b_fb14_9a391f9160bc)

_ed012e45_00da_0667_ea7e_37e629579cc3 = Shift('Shift1','ed012e45-00da-0667-ea7e-37e629579cc3')
_ed012e45_00da_0667_ea7e_37e629579cc3.parameters = {}
_ed012e45_00da_0667_ea7e_37e629579cc3.input_connectors = {'delay': ['9d1ed14b-9b73-76fe-bfe8-066c7f34e21c/out'], 'in': ['744d395d-33f5-610e-964f-4aa55a405c09/out']}
blocks_to_execute.append(_ed012e45_00da_0667_ea7e_37e629579cc3)

_788f28cf_441d_3db0_b45d_feeecfe2653e = Split('Split8','788f28cf-441d-3db0-b45d-feeecfe2653e')
_788f28cf_441d_3db0_b45d_feeecfe2653e.parameters = {'Split': 5}
_788f28cf_441d_3db0_b45d_feeecfe2653e.input_connectors = {'in': ['1ab23dce-e9dc-b588-70c6-cb05b92cd0c2/out']}
blocks_to_execute.append(_788f28cf_441d_3db0_b45d_feeecfe2653e)

_e53a819c_9818_0df8_fa68_0c8790e62a46 = Split('Split6','e53a819c-9818-0df8-fa68-0c8790e62a46')
_e53a819c_9818_0df8_fa68_0c8790e62a46.parameters = {'Split': 5}
_e53a819c_9818_0df8_fa68_0c8790e62a46.input_connectors = {'in': ['c3921ace-0e52-9a9c-1af8-91193629a389/out']}
blocks_to_execute.append(_e53a819c_9818_0df8_fa68_0c8790e62a46)

_b4dcfaf5_0507_580d_64f8_4ac934259a79 = PLS('PLS1','b4dcfaf5-0507-580d-64f8-4ac934259a79')
_b4dcfaf5_0507_580d_64f8_4ac934259a79.parameters = {}
_b4dcfaf5_0507_580d_64f8_4ac934259a79.input_connectors = {'x': ['e53a819c-9818-0df8-fa68-0c8790e62a46/out2'], 'y': ['add50b60-5313-e74b-fb14-9a391f9160bc/out2']}
blocks_to_execute.append(_b4dcfaf5_0507_580d_64f8_4ac934259a79)

_75f19f2f_60a4_8a43_d560_15495612ed97 = Split('Split7','75f19f2f-60a4-8a43-d560-15495612ed97')
_75f19f2f_60a4_8a43_d560_15495612ed97.parameters = {'Split': 5}
_75f19f2f_60a4_8a43_d560_15495612ed97.input_connectors = {'in': ['ed012e45-00da-0667-ea7e-37e629579cc3/out']}
blocks_to_execute.append(_75f19f2f_60a4_8a43_d560_15495612ed97)

_a536ce91_95c1_57c2_ea8f_fb965efbb711 = PLSTest('PLSTest3','a536ce91-95c1-57c2-ea8f-fb965efbb711')
_a536ce91_95c1_57c2_ea8f_fb965efbb711.parameters = {}
_a536ce91_95c1_57c2_ea8f_fb965efbb711.input_connectors = {'x': ['75f19f2f-60a4-8a43-d560-15495612ed97/out2'], 'y': ['788f28cf-441d-3db0-b45d-feeecfe2653e/out2'], 'model': ['b4dcfaf5-0507-580d-64f8-4ac934259a79/model']}
blocks_to_execute.append(_a536ce91_95c1_57c2_ea8f_fb965efbb711)

results_table = {}

for block in blocks_to_execute:
    results_table.update(block.execute(results_table))