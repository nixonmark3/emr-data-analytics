package models.definition;

public enum DataType {
    FRAME("frame"),
    INT("int"),
    FLOAT("float"),
    STRING("string"),
    TIMESTAMP("timestamp");

    private final String stringValue;

    /**
     * @param stringValue
     */
    private DataType(final String stringValue) {
        this.stringValue = stringValue;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return stringValue;
    }
}
