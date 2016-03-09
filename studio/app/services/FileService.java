package services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileService {

    public static String stringToFile(String path, String name, String content) {

        File file = new File(path, name);

        try (FileOutputStream fop = new FileOutputStream(file)) {

            if (!file.exists())
                file.createNewFile();

            byte[] bytes = content.getBytes();

            fop.write(bytes);
            fop.flush();
            fop.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }
}
