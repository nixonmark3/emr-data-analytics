# notify the client that the python script has been initialized
interpreter.onPythonScriptInitialized()

class Logger(object):
    def __init__(self):
        self.out = ""

    def write(self, message):
        self.out = self.out + message

    def get(self):
        return self.out

    def reset(self):
        self.out = ""

output = Logger()
sys.stdout = output
sys.stderr = output

while True:
    request = interpreter.getStatements()
    try:
        statements = request.getStatements().split("\n")
        source = None

        for statement in statements:
            if statement == None or len(statement.strip()) == 0:
                continue

            # skip comment
            if statement.strip().startswith("#"):
                continue

            if source:
                source += "\n" + statement
            else:
                source = statement

        if source:
            compiledCode = compile(source, "<string>", "exec")
            eval(compiledCode)

        interpreter.setStatementsFinished(output.get(), False)

    except Py4JJavaError:
        interpreter.setStatementsFinished(traceback.format_exc(), True)

        #excInnerError = traceback.format_exc() # format_tb() does not return the inner exception
        #innerErrorStart = excInnerError.find("Py4JJavaError:")
        #if innerErrorStart > -1:
        #    excInnerError = excInnerError[innerErrorStart:]
        #interpreter.setStatementsFinished(excInnerError + str(sys.exc_info()), True)

    except:
        interpreter.setStatementsFinished(traceback.format_exc(), True)

    output.reset()