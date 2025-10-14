package com.mapp.intelligence.tracking;

import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import com.mapp.intelligence.tracking.core.MappIntelligenceHybrid;
import com.mapp.intelligence.tracking.core.MappIntelligenceTracking;
import com.mapp.intelligence.tracking.queue.MappIntelligenceQueue;
import com.mapp.intelligence.tracking.util.URLString;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unchecked")
public class MappIntelligenceUnitUtil {
    public static String getLongText() {
        String longText = "";
        longText += "Lorem%20ipsum%20dolor%20sit%20amet%2C%20consetetur%20sadipscing%20elitr%2C%20sed%20diam%20";
        longText += "nonumy%20eirmod%20tempor%20invidunt%20ut%20labore%20et%20dolore%20magna%20aliquyam%20erat%";
        longText += "2C%20sed%20diam%20voluptua.%20At%20vero%20eos%20et%20accusam%20et%20justo%20duo%20dolores%";
        longText += "20et%20ea%20rebum.%20Stet%20clita%20kasd%20gubergren%2C%20no%20sea%20takimata%20sanctus%20";
        longText += "est%20Lorem%20ipsum%20dolor%20sit%20amet.%20Lorem%20ipsum%20dolor%20sit%20amet%2C%20conset";
        longText += "etur%20sadipscing%20elitr%2C%20sed%20diam%20nonumy%20eirmod%20tempor%20invidunt%20ut%20lab";
        longText += "ore%20et%20dolore%20magna%20aliquyam%20erat%2C%20sed%20diam%20voluptua.%20At%20vero%20eos%";
        longText += "20et%20accusam%20et%20justo%20duo%20dolores%20et%20ea%20rebum.%20Stet%20clita%20kasd%20gub";
        longText += "ergren%2C%20no%20sea%20takimata%20sanctus%20est%20Lorem%20ipsum%20dolor%20sit%20amet.%20Lo";
        longText += "rem%20ipsum%20dolor%20sit%20amet%2C%20consetetur%20sadipscing%20elitr%2C%20sed%20diam%20no";
        longText += "numy%20eirmod%20tempor%20invidunt%20ut%20labore%20et%20dolore%20magna%20aliquyam%20erat%2C";
        longText += "%20sed%20diam%20voluptua.%20At%20vero%20eos%20et%20accusam%20et%20justo%20duo%20dolores%20";
        longText += "et%20ea%20rebum.%20Stet%20clita%20kasd%20gubergren%2C%20no%20sea%20takimata%20sanctus%20es";
        longText += "t%20Lorem%20ipsum%20dolor%20sit%20amet.%20Duis%20autem%20vel%20eum%20iriure%20dolor%20in%2";
        longText += "0hendrerit%20in%20vulputate%20velit%20esse%20molestie%20consequat%2C%20vel%20illum%20dolor";
        longText += "e%20eu%20feugiat%20nulla%20facilisis%20at%20vero%20eros%20et%20accumsan%20et%20iusto%20odi";
        longText += "o%20dignissim%20qui%20blandit%20praesent%20luptatum%20zzril%20delenit%20augue%20duis%20dol";
        longText += "ore%20te%20feugait%20nulla%20facilisi.%20Lorem%20ipsum%20dolor%20sit%20amet%2C%20consectet";
        longText += "uer%20adipiscing%20elit%2C%20sed%20diam%20nonummy%20nibh%20euismod%20tincidunt%20ut%20laor";
        longText += "eet%20dolore%20magna%20aliquam%20erat%20volutpat.%20Ut%20wisi%20enim%20ad%20minim%20veniam";
        longText += "%2C%20quis%20nostrud%20exerci%20tation%20ullamcorper%20suscipit%20lobortis%20nisl%20ut%20a";
        longText += "liquip%20ex%20ea%20commodo%20consequat.%20Duis%20autem%20vel%20eum%20iriure%20dolor%20in%2";
        longText += "0hendrerit%20in%20vulputate%20velit%20esse%20molestie%20consequat%2C%20vel%20illum%20dolor";
        longText += "e%20eu%20feugiat%20nulla%20facilisis%20at%20vero%20eros%20et%20accumsan%20et%20iusto%20odi";
        longText += "o%20dignissim%20qui%20blandit%20praesent%20luptatum%20zzril%20delenit%20augue%20duis%20dol";
        longText += "ore%20te%20feugait%20nulla%20facilisi.%20Nam%20liber%20tempor%20cum%20soluta%20nobis%20ele";
        longText += "ifend%20option%20congue%20nihil%20imperdiet%20doming%20id%20quod%20mazim%20placerat%20face";
        longText += "r%20possim%20assum.%20Lorem%20ipsum%20dolor%20sit%20amet%2C%20consectetuer%20adipiscing%20";
        longText += "elit%2C%20sed%20diam%20nonummy%20nibh%20euismod%20tincidunt%20ut%20laoreet%20dolore%20magn";
        longText += "a%20aliquam%20erat%20volutpat.%20Ut%20wisi%20enim%20ad%20minim%20veniam%2C%20quis%20nostru";
        longText += "d%20exerci%20tation%20ullamcorper%20suscipit%20lobortis%20nisl%20ut%20aliquip%20ex%20ea%20";
        longText += "commodo%20consequat.%20Duis%20autem%20vel%20eum%20iriure%20dolor%20in%20hendrerit%20in%20v";
        longText += "ulputate%20velit%20esse%20molestie%20consequat%2C%20vel%20illum%20dolore%20eu%20feugiat%20";
        longText += "nulla%20facilisis.%20At%20vero%20eos%20et%20accusam%20et%20justo%20duo%20dolores%20et%20ea";
        longText += "%20rebum.%20Stet%20clita%20kasd%20gubergren%2C%20no%20sea%20takimata%20sanctus%20est%20Lor";
        longText += "em%20ipsum%20dolor%20sit%20amet.%20Lorem%20ipsum%20dolor%20sit%20amet%2C%20consetetur%20sa";
        longText += "dipscing%20elitr%2C%20sed%20diam%20nonumy%20eirmod%20tempor%20invidunt%20ut%20labore%20et%";
        longText += "20dolore%20magna%20aliquyam%20erat%2C%20sed%20diam%20voluptua.%20At%20vero%20eos%20et%20ac";
        longText += "cusam%20et%20justo%20duo%20dolores%20et%20ea%20rebum.%20Stet%20clita%20kasd%20gubergren%2C";
        longText += "%20no%20sea%20takimata%20sanctus%20est%20Lorem%20ipsum%20dolor%20sit%20amet.%20Lorem%20ips";
        longText += "um%20dolor%20sit%20amet%2C%20consetetur%20sadipscing%20elitr%2C%20At%20accusam%20aliquyam%";
        longText += "20diam%20diam%20dolore%20dolores%20duo%20eirmod%20eos%20erat%2C%20et%20nonumy%20sed%20temp";
        longText += "or%20et%20et%20invidunt%20justo%20labore%20Stet%20clita%20ea%20et%20gubergren%2C%20kasd%20";
        longText += "magna%20no%20rebum.%20sanctus%20sea%20sed%20takimata%20ut%20vero%20voluptua.%20est%20Lorem";
        longText += "%20ipsum%20dolor%20sit%20amet.%20Lorem%20ipsum%20dolor%20sit%20ame.";

        return longText;
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

    /**
     * @param instance Mapp Intelligence instance
     * @param queueId Queue ID
     *
     * @return List<String>
     */
    public static Deque<String> getQueue(MappIntelligenceTracking instance, int queueId) {
        Deque<String> requests = new ConcurrentLinkedDeque<>();

        try {
            Field queueField = MappIntelligenceTracking.class.getSuperclass().getDeclaredField("queue");
            queueField.setAccessible(true);

            MappIntelligenceQueue queue = (MappIntelligenceQueue) queueField.get(instance);
            requests = queue.getQueue(queueId);

            queueField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // do nothing
            System.err.println(e);
        }

        return requests;
    }

    /**
     * @param instance Mapp Intelligence instance
     * @param queueId Queue ID
     *
     * @return List<String>
     */
    public static Queue<String> getQueue(MappIntelligenceHybrid instance, int queueId) {
        Queue<String> requests = new ConcurrentLinkedQueue<>();

        try {
            Field queueField = MappIntelligenceTracking.class.getSuperclass().getDeclaredField("queue");
            queueField.setAccessible(true);

            MappIntelligenceQueue queue = (MappIntelligenceQueue) queueField.get(instance);
            requests = queue.getQueue(queueId);

            queueField.setAccessible(false);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // do nothing
            System.err.println(e);
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

    /**
     * Blocks until all tasks have completed execution after a shutdown request, or the timeout occurs, or the current
     * thread is interrupted, whichever happens first.
     *
     * @param milliseconds The maximum time to wait in milliseconds
     */
    public static void await(int milliseconds) throws InterruptedException {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            // do nothing
        }, milliseconds, TimeUnit.MILLISECONDS);
        executor.shutdown();
        executor.awaitTermination(milliseconds, TimeUnit.MILLISECONDS);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
