package emr.analytics.service.interpreters;

public class InterpreterResult {

    private State state;
    private String message;

    public InterpreterResult(State state, String message){
        this.state = state;
        this.message = message;
    }

    public static enum State {
        SUCCESS,
        FAILURE
    }

    public State getState(){ return this.state; }

    public String getMessage() { return this.message; }

    @Override
    public String toString(){
        return String.format("%s: %s.", this.getState().toString(), this.getMessage());
    }
}
