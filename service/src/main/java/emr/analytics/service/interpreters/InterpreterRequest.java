package emr.analytics.service.interpreters;

public class InterpreterRequest {

    private String statements;

    public InterpreterRequest(String statements){
        this.statements = statements;
    }

    public String getStatements(){ return this.statements; }
}
