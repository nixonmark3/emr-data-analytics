package emr.analytics.service.interpreters;

public class InterpreterFlags {

    private int flags;

    public InterpreterFlags(){
        flags = 0;
    }

    public void clearFlag(InterpreterFlag flag){
        if (hasFlag(flag))
            flags ^= flag.getValue();
    }

    public boolean hasFlag(InterpreterFlag flag){
        return ((flags & flag.getValue()) == flag.getValue());
    }

    public void setFlag(InterpreterFlag flag){
        flags |= flag.getValue();
    }

    public enum InterpreterFlag {
        STARTED(1), INITIALIZED(2), RUNNING(4), FAILED(8);

        private final int value;
        private InterpreterFlag(int value){
            this.value = value;
        }

        public int getValue(){
            return this.value;
        }
    }
}