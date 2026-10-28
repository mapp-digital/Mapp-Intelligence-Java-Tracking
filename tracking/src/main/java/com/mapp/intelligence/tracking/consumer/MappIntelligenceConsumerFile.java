package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceMessages;
import com.mapp.intelligence.tracking.util.MappIntelligenceDebugLogger;

import com.mapp.intelligence.tracking.util.MappIntelligenceFileLock;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Writes tracking requests to a log file.
 *
 * <p>All instances of one JVM that write to the same file path and file prefix share one
 * {@link SharedFile}: one temporary file, one writer, one line counter. With one tracking instance per
 * request there are many consumers at the same time, and each of them opening the same temporary file
 * with its own writer and its own line counter would rotate the file while the others keep writing into
 * it. The shared file is closed when the last consumer using it is closed.</p>
 *
 * <p>Every access to the file state is guarded by the {@link SharedFile} instance. Without that guard,
 * one thread could close the writer while another is writing into it, two threads could rotate the
 * same file concurrently, and the line counter could be lost or torn.</p>
 *
 * <p>Several JVMs (processes) and the cron job are coordinated with an exclusive lock on the lock file
 * {@code <prefix>.lock}: every write, rotation and takeover by the cron job happens under this lock, and
 * a file that was renamed in the meantime is detected by its identity (file key) and replaced. A file
 * that was found is never created again, new files are created exclusively with a unique name, and a
 * rename never replaces an existing file - so no request is overwritten.</p>
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceConsumerFile extends AbstractMappIntelligenceConsumer {
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    private static final String LOG_FILE_EXTENSION = ".log";

    /**
     * A file of a prefix is named {@code <prefix>-<timestamp>[.<random>]<ext>}. The timestamp after the separator
     * keeps the prefix "Shop" from picking up the files of the prefixes "ShopDE" and "Shop-DE".
     *
     * @param name File name
     * @param filePrefix Name of the file prefix
     * @param ext File extension
     *
     * @return The file belongs to the prefix
     */
    public static boolean isFileOfPrefix(String name, String filePrefix, String ext) {
        if (!name.startsWith(filePrefix + "-") || !name.endsWith(ext)) {
            return false;
        }

        String rest = name.substring(filePrefix.length() + 1);
        int digits = 0;
        while (digits < rest.length() && rest.charAt(digits) >= '0' && rest.charAt(digits) <= '9') {
            digits++;
        }

        return digits > 0 && digits < rest.length() && rest.charAt(digits) == '.';
    }

    /**
     * Files that are in use, keyed by file path and file prefix. Guarded by itself.
     */
    private static final Map<String, SharedFile> SHARED_FILES = new HashMap<>();

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
     * The file this instance writes to, shared with every other instance for the same path and prefix.
     */
    private final SharedFile sharedFile;
    /**
     * Whether this instance was closed. Guarded by {@code sharedFile}.
     */
    private boolean closed;

    /**
     * @param config Mapp Intelligence configuration
     */
    public MappIntelligenceConsumerFile(Map<String, Object> config) {
        super(config);

        String filePath = (String) config.get("filePath");
        String filePrefix = (String) config.get("filePrefix");
        this.maxFileLines = (int) config.getOrDefault("maxFileLines", DEFAULT_MAX_FILE_LINES);
        this.maxFileDuration = (int) config.getOrDefault("maxFileDuration", DEFAULT_MAX_FILE_DURATION);
        this.maxFileSize = (int) config.getOrDefault("maxFileSize", DEFAULT_MAX_FILE_SIZE);

        this.sharedFile = acquire(filePath, filePrefix, this.logger);
    }

    /**
     * @param filePath File path
     * @param filePrefix File prefix
     *
     * @return Key of the shared file
     */
    private static String getKey(String filePath, String filePrefix) {
        File directory = new File(filePath);
        String path;

        try {
            path = directory.getCanonicalPath();
        } catch (IOException e) {
            path = directory.getAbsolutePath();
        }

        return path + File.separator + filePrefix;
    }

    /**
     * Returns the shared file for the path and prefix and registers one more user of it.
     *
     * @param filePath File path
     * @param filePrefix File prefix
     * @param logger Mapp Intelligence debug logger
     *
     * @return Shared file
     */
    private static SharedFile acquire(String filePath, String filePrefix, MappIntelligenceDebugLogger logger) {
        String key = getKey(filePath, filePrefix);
        SharedFile sharedFile;

        synchronized (SHARED_FILES) {
            sharedFile = SHARED_FILES.get(key);
            if (sharedFile == null) {
                sharedFile = new SharedFile(key, filePath, filePrefix);
                SHARED_FILES.put(key, sharedFile);
            }

            sharedFile.users++;
        }

        synchronized (sharedFile) {
            sharedFile.open(logger);
        }

        return sharedFile;
    }

    /**
     * Unregisters one user of the shared file and closes it when it was the last one.
     *
     * @param sharedFile Shared file
     * @param logger Mapp Intelligence debug logger
     */
    private static void release(SharedFile sharedFile, MappIntelligenceDebugLogger logger) {
        boolean last;

        synchronized (SHARED_FILES) {
            sharedFile.users--;
            last = sharedFile.users <= 0;

            if (last && SHARED_FILES.get(sharedFile.key) == sharedFile) {
                SHARED_FILES.remove(sharedFile.key);
            }
        }

        if (last) {
            synchronized (sharedFile) {
                sharedFile.closeWriter(logger);
            }
        }
    }

    /**
     * Closes this consumer. The file itself is closed once no other consumer writes to it any more.
     * Idempotent - a consumer passed in through the config belongs to the caller, who may close it more
     * than once.
     */
    @Override
    public void close() {
        synchronized (this.sharedFile) {
            if (this.closed) {
                return;
            }

            this.closed = true;
        }

        release(this.sharedFile, this.logger);
    }

    /**
     * @param batchContent List of tracking requests
     *
     * @return Result of this batch
     */
    @Override
    protected BatchResult sendBatchContent(List<String> batchContent) {
        String payload = this.verifyPayload(batchContent);
        if (payload == null) {
            this.discardUnprocessableBatch(batchContent);

            // drop the batch instead of retrying it forever
            return BatchResult.DISCARDED;
        }

        if (payload.isEmpty()) {
            return BatchResult.RETRY;
        }

        payload += System.lineSeparator();

        int bcs = batchContent.size();
        SharedFile sf = this.sharedFile;

        // rotation and write have to be one atomic step, otherwise a concurrent rotation closes the
        // writer between the limit check and the write. The file lock does the same for other processes.
        synchronized (sf) {
            if (this.closed) {
                return BatchResult.RETRY;
            }

            try {
                if (!sf.lockCurrentFile(this.logger)) {
                    return BatchResult.RETRY;
                }

                try {
                    if (this.isFileLimitReached(sf, bcs)) {
                        sf.renameAndCreateNewTempFile(this.logger);
                    }

                    if (sf.channel == null) {
                        return BatchResult.RETRY;
                    }

                    sf.write(payload);
                    sf.currentFileLines += bcs;

                    this.logger.debug(MappIntelligenceMessages.WRITE_BATCH_DATA, sf.file.getFileName(), (long) bcs);
                } finally {
                    sf.fileLock.unlock();
                }
            } catch (IOException e) {
                // the batch was not written: report the failure, so the scheduler requeues the batch
                // instead of dropping it silently
                this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());

                return BatchResult.RETRY;
            }
        }

        return BatchResult.SENT;
    }

    /**
     * @param sf Shared file
     * @param batchContentSize Batch content size
     * @return Is file larger than 24MB or more than 10k lines or is older than 30min.
     */
    private boolean isFileLimitReached(SharedFile sf, int batchContentSize) throws IOException {
        return sf.currentFileLines + batchContentSize > this.maxFileLines
            || getTimestamp() > sf.timestamp + this.maxFileDuration
            || sf.channel.size() > this.maxFileSize;
    }

    /**
     * @return long
     */
    private static long getTimestamp() {
        return (new Date()).getTime();
    }

    /**
     * State of one temporary file, shared by every consumer for the same file path and file prefix.
     * All fields except the final ones and {@code users} are guarded by the instance itself;
     * {@code users} is guarded by {@link #SHARED_FILES}.
     */
    private static final class SharedFile {
        /**
         * Pattern of the timestamp in a file name ("<prefix>-<13 digits>.<random>.tmp").
         */
        private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("^.+-(\\d{13})\\..+$");
        /**
         * Maximum number of attempts to lock a current file (each attempt either writes or opens a newer file).
         */
        private static final int MAX_LOCK_ATTEMPTS = 100;

        /**
         * Key in {@link #SHARED_FILES}.
         */
        private final String key;
        /**
         * The directory of your request logging files.
         */
        private final Path directory;
        /**
         * The prefix name for your request logging file.
         */
        private final String filePrefix;
        /**
         * Number of consumers using this file.
         */
        private int users;
        /**
         * File.
         */
        private Path file;
        /**
         * Channel of the open file.
         */
        private FileChannel channel;
        /**
         * Identity of the open file, to detect a rename from outside.
         */
        private Object fileKey;
        /**
         * Exclusive lock shared with other processes and the cron job.
         */
        private MappIntelligenceFileLock fileLock;
        /**
         * The number of current lines in the file.
         */
        private long currentFileLines;
        /**
         * Timestamp for file creation.
         */
        private long timestamp;

        /**
         * @param key Key in the registry
         * @param filePath File path
         * @param filePrefix File prefix
         */
        private SharedFile(String key, String filePath, String filePrefix) {
            this.key = key;
            // "/tmp" and "/tmp/" are the same directory
            this.directory = Paths.get(filePath);
            this.filePrefix = filePrefix;
        }

        /**
         * Opens the file, unless it is already open and still current.
         *
         * @param logger Mapp Intelligence debug logger
         */
        private void open(MappIntelligenceDebugLogger logger) {
            if (this.channel == null || !this.isFileStillCurrent()) {
                this.searchWriteableFile(logger);
            }
        }

        /**
         * Locks the shared file lock and makes sure the open file is still the current temporary file. A
         * process can rotate the file between the check and the lock, so it is checked again after locking.
         *
         * @param logger Mapp Intelligence debug logger
         *
         * @return true if the lock is held and a current file is open, false if the lock is not held
         *
         * @throws IOException The lock cannot be acquired
         */
        private boolean lockCurrentFile(MappIntelligenceDebugLogger logger) throws IOException {
            if (this.fileLock == null) {
                this.fileLock = new MappIntelligenceFileLock(this.directory, this.filePrefix);
            }

            for (int attempt = 0; attempt < MAX_LOCK_ATTEMPTS; attempt++) {
                this.fileLock.lock();

                // the temporary file was renamed or deleted from outside, e.g. by the cron job
                if (this.channel == null || !this.isFileStillCurrent()) {
                    this.searchWriteableFile(logger);
                }

                if (this.channel != null && this.isFileStillCurrent()) {
                    return true;
                }

                this.fileLock.unlock();
            }

            return false;
        }

        /**
         * @return true if the open channel still belongs to the file at the expected path
         */
        private boolean isFileStillCurrent() {
            if (this.channel == null || this.file == null || !this.channel.isOpen()) {
                return false;
            }

            try {
                Object currentKey = Files.readAttributes(this.file, BasicFileAttributes.class).fileKey();

                // without file keys (e.g. Windows) only the existence can be checked
                return (currentKey == null || this.fileKey == null) || currentKey.equals(this.fileKey);
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * Writes the payload completely at the end of the file. Must be called while holding the lock.
         *
         * @param payload Payload
         *
         * @throws IOException The payload was not written completely
         */
        private void write(String payload) throws IOException {
            ByteBuffer buffer = ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8));
            try {
                this.channel.position(this.channel.size());
                while (buffer.hasRemaining()) {
                    this.channel.write(buffer);
                }
            } catch (NonWritableChannelException e) {
                throw new IOException("The file is not writable", e);
            }
        }

        /**
         * @param path File
         * @param create true creates a new file exclusively, false opens an existing one
         *
         * @return Channel, or null if the file cannot be opened (an existing one was removed in the meantime)
         *
         * @throws IOException Any other I/O error
         */
        private FileChannel openChannel(Path path, boolean create) throws IOException {
            // an existing file is never created again: if it was rotated after it was found, a new file
            // with the same name would be created, whose later rotation overwrites a log file
            if (create) {
                try {
                    return FileChannel.open(path, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                } catch (FileAlreadyExistsException e) {
                    return null;
                }
            }

            try {
                return FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
            } catch (NoSuchFileException e) {
                return null;
            }
        }

        /**
         * @param path File
         * @param newChannel Channel of the file
         *
         * @throws IOException The file attributes cannot be read
         */
        private void useFile(Path path, FileChannel newChannel) throws IOException {
            this.file = path;
            this.channel = newChannel;
            this.fileKey = Files.readAttributes(path, BasicFileAttributes.class).fileKey();
        }

        /**
         * Closes the channel. Idempotent.
         *
         * @param logger Mapp Intelligence debug logger
         */
        private void closeChannel(MappIntelligenceDebugLogger logger) {
            if (this.channel != null) {
                try {
                    this.channel.close();
                } catch (IOException e) {
                    logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
                }

                this.channel = null;
            }
        }

        /**
         * Closes the file and the lock file. Idempotent.
         *
         * @param logger Mapp Intelligence debug logger
         */
        private void closeWriter(MappIntelligenceDebugLogger logger) {
            this.closeChannel(logger);

            if (this.fileLock != null) {
                this.fileLock.close();
                this.fileLock = null;
            }
        }

        /**
         * Create new temporary file. The random part keeps the name unique: a file created in the same
         * millisecond in which another one was rotated would otherwise be renamed onto the same log file.
         *
         * @param logger Mapp Intelligence debug logger
         */
        private void createNewTempFile(MappIntelligenceDebugLogger logger) {
            this.closeChannel(logger);
            this.currentFileLines = 0;
            this.timestamp = getTimestamp();

            try {
                for (int attempt = 0; attempt < MAX_LOCK_ATTEMPTS; attempt++) {
                    String random = String.format("%08x", ThreadLocalRandom.current().nextInt());
                    Path newTempFile = this.directory.resolve(String.format(
                        "%s-%d.%s%s",
                        this.filePrefix,
                        this.timestamp,
                        random,
                        TEMPORARY_FILE_EXTENSION
                    ));

                    FileChannel newChannel = this.openChannel(newTempFile, true);
                    if (newChannel != null) {
                        logger.debug(
                            MappIntelligenceMessages.CREATE_NEW_LOG_FILE,
                            newTempFile.getFileName(),
                            newTempFile.toAbsolutePath(),
                            true
                        );
                        this.useFile(newTempFile, newChannel);
                        return;
                    }
                }
            } catch (IOException e) {
                logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
                this.closeChannel(logger);
            }
        }

        /**
         * Counts the lines through the open channel: the path may already belong to another file, if the
         * file was rotated after it was opened.
         *
         * @param fileChannel Channel of the file
         * @param logger Mapp Intelligence debug logger
         *
         * @return Number of current lines.
         */
        private static long getCurrentFileLines(FileChannel fileChannel, MappIntelligenceDebugLogger logger) {
            long lines = 0;
            ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);

            try {
                long position = 0;
                int read;
                while ((read = fileChannel.read(buffer, position)) > 0) {
                    for (int i = 0; i < read; i++) {
                        if (buffer.get(i) == '\n') {
                            lines++;
                        }
                    }

                    position += read;
                    buffer.clear();
                }
            } catch (IOException e) {
                logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
            }

            return lines;
        }

        /**
         * @param fileName File name
         *
         * @return Extract timestamp from file name.
         */
        private static long extractTimestamp(String fileName) {
            long defaultTimestamp = 0;
            Matcher matcher = TIMESTAMP_PATTERN.matcher(fileName);

            if (matcher.find()) {
                defaultTimestamp = Long.parseLong(matcher.group(1));
            }

            return defaultTimestamp;
        }

        /**
         * @return Temporary files of this prefix, the oldest first
         */
        private List<Path> getTemporaryFiles() {
            List<Path> files = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.directory, entry -> {
                String name = entry.getFileName().toString();
                return isFileOfPrefix(name, this.filePrefix, TEMPORARY_FILE_EXTENSION);
            })) {
                for (Path entry : stream) {
                    files.add(entry);
                }
            } catch (IOException e) {
                // no directory: nothing found
            }

            Collections.sort(files);
            return files;
        }

        /**
         * Search for writeable file.
         *
         * @param logger Mapp Intelligence debug logger
         */
        private void searchWriteableFile(MappIntelligenceDebugLogger logger) {
            this.closeChannel(logger);

            try {
                for (Path candidate : this.getTemporaryFiles()) {
                    FileChannel existingChannel = this.openChannel(candidate, false);

                    // the file was rotated or taken over in the meantime, try the next one
                    if (existingChannel != null) {
                        logger.debug(
                            MappIntelligenceMessages.USE_EXISTING_LOG_FILE,
                            candidate.getFileName(),
                            candidate.toAbsolutePath()
                        );
                        this.useFile(candidate, existingChannel);
                        this.currentFileLines = getCurrentFileLines(existingChannel, logger);
                        this.timestamp = extractTimestamp(candidate.getFileName().toString());
                        return;
                    }
                }
            } catch (IOException e) {
                logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
                this.closeChannel(logger);
            }

            this.createNewTempFile(logger);
        }

        /**
         * Rename temporary file (.tmp to .log) and continue with a new one. Must be called while holding
         * the lock.
         *
         * @param logger Mapp Intelligence debug logger
         */
        private void renameAndCreateNewTempFile(MappIntelligenceDebugLogger logger) {
            // close the channel first, some platforms cannot rename a file that is still open
            this.closeChannel(logger);

            String fileName = this.file.getFileName().toString();
            String name = fileName.substring(0, fileName.lastIndexOf('.'));
            Path renamedFile = this.directory.resolve(name + LOG_FILE_EXTENSION);

            try {
                // never replaces an existing log file
                Files.move(this.file, renamedFile);
            } catch (IOException e) {
                logger.error(MappIntelligenceMessages.CANNOT_RENAME_TEMPORARY_FILE);
            }

            this.createNewTempFile(logger);
        }
    }
}
