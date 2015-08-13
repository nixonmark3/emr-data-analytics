import sys
import DVCH

from FunctionBlock import FunctionBlock


class LoadDVCH(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            dvch_ip = self.parameters['IP']
            dvch_port = int(self.parameters['Port'])
            query = str(self.parameters['Query'])
            query = query.replace("'", "\"")

            dvch = DVCH.Connection(dvch_ip, dvch_port)
            df = dvch.query_from_json(query)

            FunctionBlock.save_results(self, df=df, statistics=True, plot=False)
            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)