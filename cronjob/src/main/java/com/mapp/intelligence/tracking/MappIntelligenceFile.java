package com.mapp.intelligence.tracking;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
class MappIntelligenceFile {
    /**
     * Constant for temporary file extension name.
     */
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    /**
     * Constant for logfile extension name.
     */
    private static final String LOG_FILE_EXTENSION = ".log";
    /**
     * Constant for an empty string array.
     */
    private static final String[] EMPTY_ARRAY = new String[0];
    /**
     * Constant for the default value of max file duration (30 min).
     */
    private static final int DEFAULT_MAX_FILE_DURATION = 30 * 60 * 1000;

    /**
     * Default constructor.
     */
    private MappIntelligenceFile() {
        // do nothing
    }

    /**
     * @return long
     */
    private static long getTimestamp() {
        return (new Date()).getTime();
    }

    /**
     * @param file File
     * @return Extract timestamp from file name.
     */
    private static long extractTimestamp(File file) {
        long defaultTimestamp = 0;
        String fileName = file.getName();
        Pattern pattern = Pattern.compile("^.+-(\\d{13})\\..+$");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            defaultTimestamp = Long.parseLong(matcher.group(1));
        }

        return defaultTimestamp;
    }

    /**
     * @param filePath Path to the file directory
     * @param filePrefix Name of the file prefix
     * @param ext Name of the file extension
     * @return File[]
     */
    public static File[] getFiles(String filePath, String filePrefix, String ext) {
        File f = new File(filePath);
        FilenameFilter filter = (f1, name) -> name.startsWith(filePrefix) && name.endsWith(ext);
        return f.listFiles(filter);
    }

    /**
     * @param file File reference
     * @return String
     */
    public static String[] getFileContent(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bt = new byte[(int) file.length()];
            if (fis.read(bt) > 0) {
                return new String(bt, StandardCharsets.UTF_8).split(System.getProperty("line.separator"));
            }
        } catch (IOException | NullPointerException e) {
            // do nothing
        }

        return EMPTY_ARRAY;
    }

    /**
     * @param file File reference
     * @return Rename status
     */
    public static boolean renameFile(File file) {
        int i = file.getName().lastIndexOf(".");
        String name = file.getName().substring(0, i);

        File renamedFile = new File(String.format("%s/%s%s", file.getParent(), name, LOG_FILE_EXTENSION));

        return file.renameTo(renamedFile);
    }

    /**
     * @param filePath Path to the file directory
     * @param filePrefix Name of the file prefix
     */
    public static boolean checkTemporaryFiles(String filePath, String filePrefix) {
        boolean renameStatus = false;
        File[] tmpFiles = getFiles(filePath, filePrefix, TEMPORARY_FILE_EXTENSION);
        if (tmpFiles != null && tmpFiles.length > 0) {
            for (File file : tmpFiles) {
                if (getTimestamp() > extractTimestamp(file) + DEFAULT_MAX_FILE_DURATION) {
                    renameStatus = renameFile(file);
                }
            }
        }

        return renameStatus;
    }

    /**
     * @param filePath Path to the file directory
     * @param filePrefix Name of the file prefix
     * @return File[]
     * @throws MappIntelligenceException Request log files not found
     */
    public static File[] getLogFiles(String filePath, String filePrefix) throws MappIntelligenceException {
        File[] files = getFiles(filePath, filePrefix, LOG_FILE_EXTENSION);
        if (files == null) {
            throw new MappIntelligenceException(
                String.format(MappIntelligenceMessages.REQUEST_LOG_FILES_NOT_FOUND, filePath)
            );
        } else {
            Arrays.sort(files, Comparator.comparing(File::getName));
        }

        return files;
    }

    /**
     * @param file File which should be deleted
     */
    public static void deleteFile(File file) {
        try {
            Files.delete(Paths.get(file.getAbsolutePath()));
        } catch (IOException | NullPointerException e) {
            // do nothing
        }
    }
}
