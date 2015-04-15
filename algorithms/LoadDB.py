import time
import pandas as pd

from FunctionBlock import FunctionBlock


class LoadDB(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        FunctionBlock.report_status_executing(self)
        df = pd.read_csv(self.parameters['Data Set'], parse_dates=True, index_col=0)
        time.sleep(4)
        FunctionBlock.report_status_complete(self)
        return {'{0}/{1}'.format(self.name, 'out'): df}
