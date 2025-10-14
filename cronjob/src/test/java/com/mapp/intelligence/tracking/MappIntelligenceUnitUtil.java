package com.mapp.intelligence.tracking;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MappIntelligenceUnitUtil {
    /**
     * @param path Path to file
     * @param prefix Path to file
     * @param extension Path to file
     */
    public static File[] getFiles(String path, String prefix, String extension) {
        File f = new File(path);
        FilenameFilter filter = (f1, name) -> name.startsWith(prefix) && name.endsWith(extension);

        return f.listFiles(filter);
    }

    /**
     * @param path Path to file
     * @param prefix Path to file
     * @param extension Path to file
     */
    public static void deleteFiles(String path, String prefix, String extension) {
        File f = new File(path);
        FilenameFilter filter = (f1, name) -> name.startsWith(prefix) && name.endsWith(extension);
        File[] files = f.listFiles(filter);

        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    /**
     * @param filePath Path to file
     * @return File
     */
    public static File createFile(String filePath) {
        File file = new File(filePath);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (IOException e) {
            // do nothing
        }

        return file;
    }

    /**
     * @param filePath Path to file
     * @param content File content
     */
    public static void writeToFile(String filePath, String content) {
        Path path = Paths.get(filePath);
        try {
            Files.write(path, content.getBytes());
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * @return MappIntelligenceLogger
     */
    public static MappIntelligenceLogger getCustomLogger() {
        class CustomLogger implements MappIntelligenceLogger {
            /**
             * @param msg Debug message
             */
            @Override
            public void log(String msg) {
                System.out.println(msg);
            }

            /**
             * @param format String format
             * @param args   Arguments
             */
            @Override
            public void log(String format, Object... args) {
                System.out.printf((format) + "%n", args);
            }
        }

        return new CustomLogger();
    }
}
