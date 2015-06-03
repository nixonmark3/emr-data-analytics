package emr.analytics.models.definition;

public enum Category {
    DATA_SOURCES("Data Sources"),
    TRANSFORMERS("Transformers"),
    FILTERS("Filters"),
    CLEANERS("Cleaners"),
    VISUALIZATIONS("Visualizations");

    private final String stringValue;

    /**
     * @param stringValue
     */
    private Category(final String stringValue) {
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
