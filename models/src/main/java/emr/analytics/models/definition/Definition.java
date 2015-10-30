package emr.analytics.models.definition;

public class Definition implements Comparable<Definition> {

    private DefinitionType definitionType;
    private String name = null;
    private String description = null;
    private String category = null;
    private String friendlyName = null;
    private int w = 200;

    private ModeDefinition offlineDefinition = null;
    private ModeDefinition onlineDefinition = null;

    private Definition() {}

    public Definition(String name, String friendlyName, String category) {
        this(DefinitionType.GENERAL, name, friendlyName, category);
    }

    public Definition(DefinitionType definitionType, String name, String friendlyName, String category) {
        this.definitionType = definitionType;
        this.name = name;
        this.friendlyName = friendlyName;
        this.category = category;
    }

    public DefinitionType getDefinitionType() { return definitionType; }

    public int getW() { return w; }

    public void setW(int w) { this.w = w; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    public String getFriendlyName() { return friendlyName; }

    public void setFriendlyName(String friendlyName) { this.friendlyName = friendlyName; }

    public ModeDefinition getOfflineDefinition() { return this.offlineDefinition; }

    public void setOfflineDefinition(ModeDefinition modeDefinition) { this.offlineDefinition = modeDefinition; }

    public ModeDefinition getOnlineDefinition() { return this.onlineDefinition; }

    public void setOnlineDefinition(ModeDefinition modeDefinition) { this.onlineDefinition = modeDefinition; }

    public ModeDefinition getModel(Mode mode) {

        ModeDefinition modeDefinition;
        if (mode == Mode.OFFLINE && this.offlineDefinition != null)
            modeDefinition = this.offlineDefinition;
        else if (mode == Mode.ONLINE && this.onlineDefinition != null)
            modeDefinition = this.onlineDefinition;
        else if (this.offlineDefinition != null)
            modeDefinition = this.offlineDefinition;
        else if (this.onlineDefinition != null)
            modeDefinition = this.onlineDefinition;
        else
            modeDefinition = new ModeDefinition();

        return modeDefinition;
    }

    public void setModel(Mode mode, ModeDefinition modeDefinition){

        switch(mode){
            case OFFLINE:
                offlineDefinition = modeDefinition;
                break;
            case ONLINE:
                onlineDefinition = modeDefinition;
                break;
        }
    }

    public int compareTo(Definition definition) {

        // sort by category then name

        if (this.getCategory().equals(definition.getCategory()))
            return this.getName().compareTo(definition.getName());
        else
            return this.getCategory().compareTo(definition.getCategory());
    }
}
