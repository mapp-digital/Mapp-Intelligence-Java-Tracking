package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceMessages;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceConsumerFile extends AbstractMappIntelligenceConsumer {
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    private static final String LOG_FILE_EXTENSION = ".log";

    /**
     * The path to your request logging file.
     */
    private final String filePath;
    /**
     * The prefix name for your request logging file.
     */
    private final String filePrefix;
    /**
     * The maximum number of maximal lines per file.
     */
    private final int maxFileLines;
    /**
     * The maximum number of maximal file duration.
     */
    private final int maxFileDuration;
    /**
     * The maximum number of maximal file size.
     */
    private final int maxFileSize;
    /**
     * File.
     */
    private File file;
    /**
     * FileWriter.
     */
    private FileWriter fileWriter;
    /**
     * FileWriter.
     */
    private BufferedWriter bufferedWriter;
    /**
     * The number of current lines in the file.
     */
    private long currentFileLines;
    /**
     * Timestamp for file creation.
     */
    private long timestamp;

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceConsumerFile(Map<String, Object> config) {
        super(config);

        this.filePath = (String) config.get("filePath");
        this.filePrefix = (String) config.get("filePrefix");
        this.maxFileLines = (int) config.getOrDefault("maxFileLines", DEFAULT_MAX_FILE_LINES);
        this.maxFileDuration = (int) config.getOrDefault("maxFileDuration", DEFAULT_MAX_FILE_DURATION);
        this.maxFileSize = (int) config.getOrDefault("maxFileSize", DEFAULT_MAX_FILE_SIZE);

        this.searchWriteableFile();
    }

    /**
     * @return long
     */
    private long getTimestamp() {
        return (new Date()).getTime();
    }

    /**
     * Create file writer.
     */
    private void createFileWriter() {
        if (this.bufferedWriter != null) {
            this.close();
        }

        try {
            this.fileWriter = new FileWriter(this.file.getAbsoluteFile(), true);
            this.bufferedWriter = new BufferedWriter(this.fileWriter);
        } catch (IOException e) {
            this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * Create new temporary file.
     */
    private void createNewTempFile() {
        this.currentFileLines = 0;
        this.timestamp = this.getTimestamp();

        String newTempFileName = String.format("%s%s-%s.tmp", this.filePath, this.filePrefix, this.timestamp);

        File newTempFile = new File(newTempFileName);
        try {
            if (!newTempFile.exists()) {
                this.logger.log(
                    MappIntelligenceMessages.CREATE_NEW_LOG_FILE,
                    newTempFile.getName(),
                    newTempFile.getAbsoluteFile().getPath(),
                    newTempFile.createNewFile()
                );
            } else {
                this.logger.log(
                    MappIntelligenceMessages.USE_EXISTING_LOG_FILE,
                    newTempFile.getName(),
                    newTempFile.getAbsoluteFile().getPath()
                );
            }

            this.file = newTempFile;
            this.createFileWriter();
        } catch (IOException e) {
            this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * @return Number of current lines.
     */
    private long getCurrentFileLines() {
        long lines = 0;

        try {
            try (BufferedReader reader = new BufferedReader(new FileReader(this.file.getAbsolutePath()))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    lines++;
                }
            }
        } catch (IOException e) {
            this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
        }

        return lines;
    }

    /**
     * @return Extract timestamp from file name.
     */
    private long extractTimestamp() {
        long defaultTimestamp = 0;
        String fileName = this.file.getName();
        Pattern pattern = Pattern.compile("^.+-(\\d{13})\\..+$");
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.find()) {
            defaultTimestamp = Long.parseLong(matcher.group(1));
        }

        return defaultTimestamp;
    }

    /**
     * Search for writeable file.
     */
    private void searchWriteableFile() {
        File f = new File(this.filePath);
        FilenameFilter filter = (f1, name) -> name.startsWith(this.filePrefix) && name.endsWith(TEMPORARY_FILE_EXTENSION);
        File[] files = f.listFiles(filter);

        if (files == null || files.length <= 0) {
            this.createNewTempFile();
        } else {
            this.file = files[0];
            this.currentFileLines = this.getCurrentFileLines();
            this.timestamp = this.extractTimestamp();

            this.createFileWriter();
        }
    }

    /**
     * Rename temporary file (.tmp to .log).
     */
    private void renameAndCreateNewTempFile() {
        int i = this.file.getName().lastIndexOf('.');
        String name = this.file.getName().substring(0, i);

        File renamedFile = new File(String.format("%s/%s%s", this.filePath, name, LOG_FILE_EXTENSION));
        if (this.file.renameTo(renamedFile)) {
            this.searchWriteableFile();
        } else {
            this.logger.log(MappIntelligenceMessages.CANNOT_RENAME_TEMPORARY_FILE);
            this.createNewTempFile();
        }
    }

    /**
     * @param batchContentSize Batch content size
     * @return Is file larger than 24MB or more than 10k lines or is older than 30min.
     */
    private boolean isFileLimitReached(int batchContentSize) {
        return this.currentFileLines + batchContentSize > this.maxFileLines
            || this.getTimestamp() > this.timestamp + this.maxFileDuration
            || this.file.length() > this.maxFileSize;
    }

    /**
     * Closed the FileWriter.
     */
    @Override
    protected void close() {
        if (this.bufferedWriter != null) {
            try {
                this.bufferedWriter.close();
                this.fileWriter.close();
            } catch (IOException e) {
                this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
            }
        }
    }

    /**
     * @param batchContent List of tracking requests
     * @return boolean
     */
    @Override
    public boolean sendBatch(List<String> batchContent) {
        if (this.bufferedWriter == null) {
            return false;
        }

        String payload = this.verifyPayload(batchContent);
        if (payload.isEmpty()) {
            return false;
        }

        payload += System.getProperty("line.separator");

        int bcs = batchContent.size();
        if (this.isFileLimitReached(bcs)) {
            this.renameAndCreateNewTempFile();
        }
        this.currentFileLines += bcs;

        try {
            this.bufferedWriter.write(payload);
            this.bufferedWriter.flush();

            long currentBatchSize = batchContent.size();
            this.logger.log(MappIntelligenceMessages.WRITE_BATCH_DATA, this.file.getName(), currentBatchSize);
        } catch (IOException e) {
            this.logger.log(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
        }

        return true;
    }
}
