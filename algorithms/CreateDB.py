import sys
import Wranglers

from FunctionBlock import FunctionBlock


class CreateDB(FunctionBlock):

    def __init__(self, name, unique_name):
        FunctionBlock.__init__(self, name, unique_name)

    def execute(self, results_table):
        try:
            FunctionBlock.report_status_executing(self)

            filename = self.parameters['Filename']

            file_type = self.parameters['File Type']

            project_name = self.parameters['Project Name']

            data_set_name = str(self.parameters['Data Set Name'])

            if file_type == 'CSV':
                df = Wranglers.import_csv(filename, project_name, data_set_name)
            elif file_type == 'FF3':
                import_parameters = {
                    "Path": filename,
                    "Name": data_set_name
                }
                df = Wranglers.import_ff3(project_name, import_parameters)

            FunctionBlock.save_results(self, df=df, statistics=True, plot=False)
            FunctionBlock.report_status_complete(self)

            return {'{0}'.format(self.unique_name): None}

        except Exception as err:
            FunctionBlock.save_results(self)
            FunctionBlock.report_status_failure(self)
            print(err.args, file=sys.stderr)
