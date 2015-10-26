# notify the client that the python script has been initialized
interpreter.onPythonScriptInitialized()

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

        interpreter.setStatementsComplete()

    except Py4JJavaError:
        interpreter.setStatementsFailed(traceback.format_exc())
        # interpreter.setStatementsFailed(traceback.format_exc() + str(sys.exc_info()))

    except:
        interpreter.setStatementsFailed(traceback.format_exc())