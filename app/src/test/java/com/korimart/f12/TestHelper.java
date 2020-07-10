package com.korimart.f12;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

public enum TestHelper {
    INSTANCE;

    public String loadDocument(String doc, String encoding){
        try {
            File file = new File(
                    Paths.get(System.getProperty("user.dir"), "src/test/java/com/korimart/f12", doc)
                            .toString());
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            return new String(data, encoding);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
