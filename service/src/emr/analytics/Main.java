package emr.analytics;

public class Main {

    public static void main(String[] args) {

        String source = "import sys\nprint sys.version_info";
        PythonTask task = new PythonTask(source);

        System.out.println(task.execute());
    }
}
