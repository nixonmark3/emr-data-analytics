package emr.analytics;

public class Main {

    public static void main(String[] args) {
        new Thread(new listener()).start();
    }
}
