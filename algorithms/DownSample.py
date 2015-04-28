import sys
import pandas as pd

from FunctionBlock import FunctionBlock


class DownSample(FunctionBlock):

    def __init__(self, name):
        FunctionBlock.__init__(self, name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            FunctionBlock.check_connector_has_one_wire(self, 'in')

            df = results_table[self.input_connectors['in'][0]]

            sample_size = '{0}S'.format(self.parameters['SampleSize'])

            interpolation = self.parameters['Interpolation']

            df = df.resample(sample_size, how=interpolation.lower())

            FunctionBlock.save_results(self, df=df, statistics=True, plot=True)

            FunctionBlock.report_status_complete(self)

            return {'{0}/{1}'.format(self.name, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)

