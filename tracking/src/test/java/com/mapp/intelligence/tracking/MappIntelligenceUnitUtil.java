package com.mapp.intelligence.tracking;

import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import com.mapp.intelligence.tracking.core.MappIntelligenceHybrid;
import com.mapp.intelligence.tracking.core.MappIntelligenceTracking;
import com.mapp.intelligence.tracking.queue.MappIntelligenceQueue;
import com.mapp.intelligence.tracking.util.URLString;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MappIntelligenceUnitUtil {
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
                System.out.println(String.format(format, args));
            }
        }

        return new CustomLogger();
    }

    /**
     * @param instance Mapp Intelligence instance
     * @return List<String>
     */
    public static List<String> getQueue(MappIntelligenceTracking instance) {
        List<String> requests = new ArrayList<>();

        try {
            Field queueField = MappIntelligenceTracking.class.getSuperclass().getDeclaredField("queue");
            queueField.setAccessible(true);

            MappIntelligenceQueue queue = (MappIntelligenceQueue) queueField.get(instance);
            requests = queue.getQueue();

            queueField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // do nothing
            System.err.println(e);
        }

        return requests;
    }

    /**
     * @param instance Mapp Intelligence instance
     * @return List<String>
     */
    public static List<String> getQueue(MappIntelligenceHybrid instance) {
        List<String> requests = new ArrayList<>();

        try {
            Field queueField = MappIntelligenceTracking.class.getSuperclass().getDeclaredField("queue");
            queueField.setAccessible(true);

            MappIntelligenceQueue queue = (MappIntelligenceQueue) queueField.get(instance);
            requests = queue.getQueue();

            queueField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // do nothing
            System.err.println(e);
        }

        return requests;
    }

    /**
     * @param queue Mapp Intelligence queue
     * @return List<String>
     */
    public static List<String> getQueue(MappIntelligenceQueue queue) {
        List<String> requests = new ArrayList<>();

        try {
            Field queueField = MappIntelligenceQueue.class.getDeclaredField("queue");
            queueField.setAccessible(true);

            requests = (List<String>) queueField.get(queue);

            queueField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // do nothing
        }

        return requests;
    }

    public static boolean checkStatistics(String request, String pixelFeatures) {
        if (request.contains("pf=" + pixelFeatures)
            && request.contains("cs801=" + URLString.encode(MappIntelligence.VERSION))
            && request.contains("cs802=Java")) {
            return true;
        }

        System.out.println(request + " | pf=" + pixelFeatures);
        return false;
    }

    /**
     * @param consumer Mapp Intelligence consumer
     * @return FileWriter
     */
    public static FileWriter getFileWriter(MappIntelligenceConsumer consumer) {
        FileWriter fileWriter = null;

        try {
            Field fileWriterField = MappIntelligenceConsumerFile.class.getDeclaredField("fileWriter");
            fileWriterField.setAccessible(true);

            fileWriter = (FileWriter) fileWriterField.get(consumer);

            fileWriterField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // do nothing
        }

        return fileWriter;
    }

    /**
     * @param path Path to file
     * @param prefix Path to file
     * @param extension Path to file
     * @return String
     */
    public static String getFileContent(String path, String prefix, String extension) {
        File f = new File(path);
        FilenameFilter filter = (f1, name) -> name.startsWith(prefix) && name.endsWith(extension);
        File[] files = f.listFiles(filter);

        if (files != null) {
            try {
                File file = files[0];
                byte[] bt = new byte[(int) file.length()];
                FileInputStream fis = new FileInputStream(file);
                fis.read(bt);
                fis.close();

                return new String(bt, StandardCharsets.UTF_8);
            } catch (IOException e) {
                // do nothing
            }
        }

        return "";
    }

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
}
