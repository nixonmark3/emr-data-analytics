import sys
import PI

from FunctionBlock import FunctionBlock


class LoadDVCH(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            pi_ip = self.parameters['IP']
            pi_port = int(self.parameters['Port'])
            query = str(self.parameters['Query'])
            query = query.replace("'", "\"")

            pi = PI.Connection(pi_ip, pi_port)
            df = pi.query_from_json(query)

            FunctionBlock.save_results(self, df=df, statistics=True, plot=False)
            FunctionBlock.report_status_complete(self)

            return {FunctionBlock.getFullPath(self, 'out'): df}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)