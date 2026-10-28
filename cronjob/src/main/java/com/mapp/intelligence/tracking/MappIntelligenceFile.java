package com.mapp.intelligence.tracking;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import com.mapp.intelligence.tracking.util.MappIntelligenceFileLock;

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
     * Extension of the file that keeps the requests of a log file which can never be sent.
     */
    private static final String DISCARDED_FILE_EXTENSION = ".discarded";
    /**
     * Extension of a log file that was sent completely but could not be deleted.
     */
    private static final String SENT_FILE_EXTENSION = ".sent";
    /**
     * Constant for an empty string array.
     */
    private static final String[] EMPTY_ARRAY = new String[0];
    /**
     * Constant for the default value of max file duration (30 min).
     */
    public static final int DEFAULT_MAX_FILE_DURATION = 30 * 60 * 1000;

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
        FilenameFilter filter = (f1, name) -> MappIntelligenceConsumerFile.isFileOfPrefix(name, filePrefix, ext);
        return f.listFiles(filter);
    }

    /**
     * @param file File reference
     * @return The lines of the file
     * @throws IOException The file cannot be read - it must not be treated as empty, it would be deleted
     */
    public static String[] getFileContent(File file) throws IOException {
        if (file == null) {
            throw new IOException("No file");
        }

        // a single InputStream.read() may return fewer bytes than the file holds, which silently
        // dropped lines - and the file is deleted afterwards
        byte[] content = Files.readAllBytes(file.toPath());
        if (content.length > 0) {
            return new String(content, StandardCharsets.UTF_8).split(System.lineSeparator());
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

        return move(file, new File(file.getParentFile(), name + LOG_FILE_EXTENSION));
    }

    /**
     * Renames a file, but never replaces an existing one (File.renameTo would silently overwrite it).
     *
     * @param from Source
     * @param to Target
     * @return Rename status
     */
    private static boolean move(File from, File to) {
        try {
            Files.move(from.toPath(), to.toPath());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * @param file Log file
     * @return The file that keeps the requests of the log file that can never be sent: {@code <file>.discarded}
     */
    public static File getDiscardedFile(File file) {
        return new File(file.getParentFile(), file.getName() + DISCARDED_FILE_EXTENSION);
    }

    /**
     * Appends the requests of a log file that can never be sent to {@code <file>.discarded}, so they are kept for
     * inspection but no longer picked up by the cron job. The file is created if it does not exist yet.
     *
     * @param file     Log file
     * @param requests Requests that can never be sent
     * @return The file with the discarded requests
     * @throws IOException The requests cannot be written
     */
    public static File appendDiscardedRequests(File file, List<String> requests) throws IOException {
        File discardedFile = getDiscardedFile(file);

        StringBuilder content = new StringBuilder();
        for (String request : requests) {
            content.append(request).append(System.lineSeparator());
        }

        Files.write(
            discardedFile.toPath(),
            content.toString().getBytes(StandardCharsets.UTF_8),
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );

        return discardedFile;
    }

    /**
     * @param filePath Path to the file directory
     * @param filePrefix Name of the file prefix
     * @return Whether at least one expired temporary file was renamed
     */
    public static boolean checkTemporaryFiles(String filePath, String filePrefix) {
        return checkTemporaryFiles(filePath, filePrefix, DEFAULT_MAX_FILE_DURATION);
    }

    /**
     * Renames every temporary file that is older than the maximum file duration into a log file.
     *
     * @param filePath Path to the file directory
     * @param filePrefix Name of the file prefix
     * @param maxFileDuration Maximum file duration in milliseconds, as configured for the file consumer
     * @return Whether at least one expired temporary file was renamed
     */
    public static boolean checkTemporaryFiles(String filePath, String filePrefix, int maxFileDuration) {
        boolean renameStatus = false;
        File[] tmpFiles = getFiles(filePath, filePrefix, TEMPORARY_FILE_EXTENSION);
        if (tmpFiles == null || tmpFiles.length == 0) {
            return false;
        }

        // the writers hold the same lock while they write: no request is written into a file that is
        // renamed at the same moment, and a writer detects the rename and continues with a new file
        try (MappIntelligenceFileLock fileLock = new MappIntelligenceFileLock(Paths.get(filePath), filePrefix)) {
            fileLock.lock();

            for (File file : tmpFiles) {
                if (getTimestamp() > extractTimestamp(file) + maxFileDuration && renameFile(file)) {
                    renameStatus = true;
                }
            }
        } catch (IOException e) {
            // the files are renamed with the next run
            return renameStatus;
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
     * Replaces the content of a file: written into a new file first, which then replaces the file - so an interruption
     * leaves either the old or the new content, never a part of it.
     *
     * @param file  File
     * @param lines New content
     *
     * @throws IOException The content cannot be written
     */
    public static void replaceFileContent(File file, List<String> lines) throws IOException {
        String random = String.format("%08x", ThreadLocalRandom.current().nextInt());
        Path newFile = new File(file.getParentFile(), file.getName() + "." + random + ".part").toPath();

        StringBuilder content = new StringBuilder();
        for (String line : lines) {
            content.append(line).append(System.lineSeparator());
        }

        try {
            Files.write(newFile, content.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
            try {
                Files.move(newFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(newFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(newFile);
        }
    }

    /**
     * @param file File which should be deleted
     * @return Whether the file was deleted
     */
    public static boolean deleteFile(File file) {
        try {
            Files.delete(Paths.get(file.getAbsolutePath()));
            return true;
        } catch (IOException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Renames a log file that was sent completely but could not be deleted, so it is not sent again.
     *
     * @param file Log file
     * @return The renamed file, or null if renaming failed
     */
    public static File markAsSent(File file) {
        File sentFile = new File(file.getParentFile(), file.getName() + SENT_FILE_EXTENSION);

        return move(file, sentFile) ? sentFile : null;
    }
}
