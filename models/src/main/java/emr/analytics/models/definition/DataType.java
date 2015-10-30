package emr.analytics.models.definition;

public enum DataType {
    EDITABLE_QUERY("editableQuery"),
    FLOAT("float"),
    FRAME("frame"),
    INT("int"),
    JSON("json"),
    LIST("list"),
    MULTI_SELECT_LIST("multiSelectList"),
    QUERY("query"),
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
