import time
import pandas as pd

from FunctionBlock import FunctionBlock


class Columns(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        FunctionBlock.report_status_executing(self)
        df = results_table[self.input_connectors['in'][0]]  # a columns block can only have one wire in
        df = df[self.parameters['Columns']]
        time.sleep(3)
        FunctionBlock.report_status_complete(self)
        return {'{0}/{1}'.format(self.name, 'out'): df}
