package emr.analytics.diagram.interpreter;

public class Package {

    private PackageType packageType;
    private String key;
    private String value;

    public Package(PackageType packageType, String key, String value){
        this.packageType = packageType;
        this.key = key;
        this.value = value;
    }

    public static enum PackageType{
        IMPORT, FUNCTION
    }

    public PackageType getPackageType() { return this.packageType; }

    public String getKey() { return this.key; }

    public String getValue() { return this.value; }
}
