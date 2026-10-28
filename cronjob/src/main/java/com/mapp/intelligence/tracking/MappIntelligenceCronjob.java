package com.mapp.intelligence.tracking;

import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.queue.MappIntelligenceQueue;
import com.mapp.intelligence.tracking.util.MappIntelligenceDebugLogger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceCronjob {
    /**
     * Constant for exit status successful.
     */
    private static final int EXIT_STATUS_SUCCESS = 0;
    /**
     * Constant for exit status fail.
     */
    private static final int EXIT_STATUS_FAIL = 1;
    /**
     * Lock of a cron job run: "<prefix>.cronjob.lock".
     */
    private static final String RUN_LOCK_EXTENSION = ".cronjob.lock";

    /**
     * Mapp Intelligence config map
     */
    private final Map<String, Object> mic;
    /**
     * The path to your request logging file.
     */
    private final String filePath;
    /**
     * The prefix name for your request logging file.
     */
    private final String filePrefix;
    /**
     * Deactivate the tracking functionality.
     */
    private final boolean deactivate;
    /**
     * Activates the debug mode.
     */
    private final MappIntelligenceDebugLogger logger;

    /**
     * @param config Mapp Intelligence config
     * @throws MappIntelligenceException Mapp Intelligence configuration exception
     */
    public MappIntelligenceCronjob(MappIntelligenceConfig config) throws MappIntelligenceException {
        this(config.build());
    }

    /**
     * @param args Command line arguments
     * @throws MappIntelligenceException Mapp Intelligence configuration exception
     */
    public MappIntelligenceCronjob(String[] args) throws MappIntelligenceException {
        this(getMappIntelligenceConfig(args));
    }

    /**
     * @param config Mapp Intelligence config map
     * @throws MappIntelligenceException Mapp Intelligence configuration exception
     */
    private MappIntelligenceCronjob(Map<String, Object> config) throws MappIntelligenceException {
        this.mic = config;

        this.validateOptions();

        this.filePath = (String) this.mic.get(MappIntelligenceOptions.FILE_PATH);
        this.filePrefix = (String) this.mic.get(MappIntelligenceOptions.FILE_PREFIX);
        this.deactivate = (boolean) this.mic.get(MappIntelligenceOptions.DEACTIVATE);

        int logLevel = (int) this.mic.get(MappIntelligenceOptions.LOG_LEVEL);
        MappIntelligenceLogger l = (MappIntelligenceLogger) this.mic.get(MappIntelligenceOptions.LOGGER);
        this.logger = new MappIntelligenceDebugLogger(l, logLevel);

        this.mic.put("consumerType", MappIntelligenceConsumerType.HTTP_CLIENT);
        this.mic.put("deactivate", this.deactivate);
        this.mic.put("maxBatchSize", 1000);
        // unlimited: the cron job must not discard requests of a log file, unsent requests stay in the file
        this.mic.put("maxQueueSize", 0);
        // no periodic flush: the result of every file is decided by the flush at its end
        this.mic.put("requestInterval", 0);
    }

    /**
     * @return options
     */
    private static Options getOptions() {
        Options options = new Options();

        options.addOption("i", MappIntelligenceOptions.TRACK_ID, true, MappIntelligenceMessages.OPTION_TRACK_ID);
        options.addOption("d", MappIntelligenceOptions.TRACK_DOMAIN, true, MappIntelligenceMessages.OPTION_TRACK_DOMAIN);
        options.addOption("c", MappIntelligenceOptions.CONFIG, true, MappIntelligenceMessages.OPTION_CONFIG);
        options.addOption("f", MappIntelligenceOptions.FILE_PATH, true, MappIntelligenceMessages.OPTION_FILE_PATH);
        options.addOption("p", MappIntelligenceOptions.FILE_PREFIX, true, MappIntelligenceMessages.OPTION_FILE_PREFIX);
        options.addOption("ll", MappIntelligenceOptions.LOG_LEVEL, true, MappIntelligenceMessages.OPTION_LOG_LEVEL);

        options.addOption(
            Option.builder().longOpt(MappIntelligenceOptions.HELP).desc(MappIntelligenceMessages.OPTION_HELP).build()
        );
        options.addOption(
            Option.builder().longOpt(MappIntelligenceOptions.DEBUG).desc(MappIntelligenceMessages.OPTION_DEBUG).build()
        );
        options.addOption(
            Option.builder().longOpt(MappIntelligenceOptions.VERSION).desc(MappIntelligenceMessages.OPTION_VERSION).build()
        );

        return options;
    }

    /**
     * @param options Command line options
     */
    private static void displayHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp(
            MappIntelligenceMessages.HELP_SYNTAX,
            MappIntelligenceMessages.HELP_HEADER,
            options,
            MappIntelligenceMessages.HELP_FOOTER,
            true
        );
    }

    /**
     * @param args Command line arguments
     */
    private static Map<String, Object> getMappIntelligenceConfig(String[] args) throws MappIntelligenceException {
        MappIntelligenceConfig mappConfig = new MappIntelligenceConfig();
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption(MappIntelligenceOptions.HELP)) {
                displayHelp(options);
            }

            if (line.hasOption(MappIntelligenceOptions.VERSION)) {
                MappIntelligenceCronjobLogger l = new MappIntelligenceCronjobLogger();
                l.log("v%s", MappIntelligence.VERSION);
            }

            if (line.hasOption(MappIntelligenceOptions.CONFIG)) {
                mappConfig = new MappIntelligenceConfig(line.getOptionValue(MappIntelligenceOptions.CONFIG));
            }

            if (line.hasOption(MappIntelligenceOptions.DEBUG)) {
                mappConfig.setLogger(new MappIntelligenceCronjobLogger());
            }

            if (line.hasOption(MappIntelligenceOptions.LOG_LEVEL)) {
                String logLevel = line.getOptionValue(MappIntelligenceOptions.LOG_LEVEL);
                mappConfig.setLogLevel(logLevel);
            }

            if (line.hasOption(MappIntelligenceOptions.TRACK_ID)) {
                String i = line.getOptionValue(MappIntelligenceOptions.TRACK_ID);
                mappConfig.setTrackId(i);
            }

            if (line.hasOption(MappIntelligenceOptions.TRACK_DOMAIN)) {
                mappConfig.setTrackDomain(line.getOptionValue(MappIntelligenceOptions.TRACK_DOMAIN));
            }

            if (line.hasOption(MappIntelligenceOptions.FILE_PATH)) {
                mappConfig.setFilePath(line.getOptionValue(MappIntelligenceOptions.FILE_PATH));
            }

            if (line.hasOption(MappIntelligenceOptions.FILE_PREFIX)) {
                mappConfig.setFilePrefix(line.getOptionValue(MappIntelligenceOptions.FILE_PREFIX));
            }
        } catch(ParseException e) {
            displayHelp(options);
            throw new MappIntelligenceException(e.getMessage());
        }

        return mappConfig.build();
    }

    /**
     * Prints the help or the version, if requested.
     *
     * @param args Command line arguments
     *
     * @return true if the help or the version was printed, then nothing else is done
     */
    static boolean printInformation(String[] args) {
        Options options = getOptions();
        CommandLine line;
        try {
            line = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            // reported by the regular configuration
            return false;
        }

        boolean printed = false;
        if (line.hasOption(MappIntelligenceOptions.HELP)) {
            displayHelp(options);
            printed = true;
        }

        if (line.hasOption(MappIntelligenceOptions.VERSION)) {
            MappIntelligenceCronjobLogger l = new MappIntelligenceCronjobLogger();
            l.log("v%s", MappIntelligence.VERSION);
            printed = true;
        }

        return printed;
    }

    /**
     * @param o Object
     * @return boolean
     */
    private boolean isOptionInvalid(String o) {
        return o.isEmpty();
    }

    /**
     * @throws MappIntelligenceException Mapp Intelligence configuration exception
     */
    private void validateOptions() throws MappIntelligenceException {
        if (this.isOptionInvalid((String) this.mic.get(MappIntelligenceOptions.TRACK_ID))) {
            throw new MappIntelligenceException(MappIntelligenceMessages.REQUIRED_TRACK_ID);
        }

        if (this.isOptionInvalid((String) this.mic.get(MappIntelligenceOptions.TRACK_DOMAIN))) {
            throw new MappIntelligenceException(MappIntelligenceMessages.REQUIRED_TRACK_DOMAIN);
        }

        if (this.isOptionInvalid((String) this.mic.get(MappIntelligenceOptions.FILE_PATH))) {
            this.mic.put(MappIntelligenceOptions.FILE_PATH, String.format("%s/", System.getProperty(MappIntelligenceOptions.TMP_DIR)));
        }

        if (this.isOptionInvalid((String) this.mic.get(MappIntelligenceOptions.FILE_PREFIX))) {
            this.mic.put(MappIntelligenceOptions.FILE_PREFIX, "MappIntelligenceRequests");
        }
    }

    /**
     * @param files List of files to be sent synchronously.
     *
     * @return exit status
     */
    private int processFiles(File[] files) {
        // flush() triggers every queue and the callback fires once per queue, so the latch has to
        // wait for all of them - otherwise the file is deleted while other queues still hold unsent
        // requests
        int numQueues = ((int) this.mic.getOrDefault("requestQueues", 0)) + 1;
        int exitStatus = EXIT_STATUS_SUCCESS;

        for (File file : files) {
            FileResult result = this.processFile(file, numQueues);
            if (result == FileResult.RETRY) {
                // keep this file and everything after it for the next run, in order
                return EXIT_STATUS_FAIL;
            }

            if (result == FileResult.DISCARDED) {
                // the file is out of the way, so the next files can still be sent - but the run
                // could not deliver everything and must not look successful
                exitStatus = EXIT_STATUS_FAIL;
            }
        }

        return exitStatus;
    }

    /**
     * Outcome of processing a single log file.
     */
    private enum FileResult {
        /**
         * Every request was delivered, the file is deleted.
         */
        SENT,
        /**
         * Requests are still undelivered but may succeed later, the file is kept for the next run.
         */
        RETRY,
        /**
         * Nothing is left to retry, but at least one batch can never be sent (permanent client error,
         * oversized payload). Its requests are kept in {@code <file>.discarded}, the log file is deleted:
         * sending them again would fail again, sending the delivered ones again would duplicate them.
         */
        DISCARDED
    }

    /**
     * @param file      File to be sent
     * @param numQueues Number of request queues that report back
     *
     * @return Result of this file: SENT (deleted), RETRY (kept for the next run, with the unsent requests only) or
     *         DISCARDED (deleted, the requests that can never be sent are kept in {@code <file>.discarded})
     */
    private FileResult processFile(File file, int numQueues) {
        String[] fileLines;
        try {
            fileLines = MappIntelligenceFile.getFileContent(file);
        } catch (IOException e) {
            this.logger.error(MappIntelligenceMessages.CANNOT_READ_LOG_FILE, file.getName(), e.getMessage());
            return FileResult.RETRY;
        }

        MappIntelligenceQueue requestQueue = new MappIntelligenceQueue(this.mic);
        List<String> discardedRequests = requestQueue.collectDiscardedRequests();
        List<String> unsent = new ArrayList<>();
        List<String> discarded;

        try {
            for (String fileLine : fileLines) {
                if (fileLine.trim().isEmpty()) {
                    continue;
                }

                requestQueue.add(fileLine);
            }

            CountDownLatch latch = new CountDownLatch(numQueues);
            requestQueue.setOnFlushComplete((success, queueId) -> latch.countDown());

            requestQueue.flush();

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return FileResult.RETRY;
            }

            for (int i = 0; i < numQueues; i++) {
                unsent.addAll(requestQueue.getQueue(i));
            }

            synchronized (discardedRequests) {
                discarded = new ArrayList<>(discardedRequests);
            }
        } finally {
            // release the thread pools of this file's queue before moving on to the next file.
            // Without a last flush: whatever is still queued stays in the log file and is sent by the
            // next run - sending it here as well would only deliver it twice, and would repeat all
            // attempts of the run that just failed
            requestQueue.close(false);
        }

        if (!discarded.isEmpty()) {
            // only the requests that can never be sent: sending them again would fail again, sending the
            // delivered ones again would duplicate them
            List<String> requests = inFileOrder(fileLines, discarded);
            File discardedFile;
            try {
                discardedFile = MappIntelligenceFile.appendDiscardedRequests(file, requests);
            } catch (IOException e) {
                // the file stays as it is: the next run sends it again, the discarded requests are not lost
                this.logger.error(
                    MappIntelligenceMessages.CANNOT_WRITE_DISCARDED_REQUESTS,
                    requests.size(),
                    file.getName(),
                    MappIntelligenceFile.getDiscardedFile(file).getName()
                );
                return FileResult.RETRY;
            }

            this.logger.error(
                MappIntelligenceMessages.LOG_FILE_WITH_DISCARDED_REQUESTS,
                file.getName(),
                requests.size(),
                discardedFile.getName()
            );
        }

        if (!unsent.isEmpty()) {
            return this.keepUnsentRequests(file, fileLines, unsent);
        }

        FileResult result = discarded.isEmpty() ? FileResult.SENT : FileResult.DISCARDED;

        // only now, after every queue has reported back and nothing is left to retry, is the file safe to remove
        if (MappIntelligenceFile.deleteFile(file)) {
            return result;
        }

        // a file that stays in place would be sent again on the next run, which duplicates the
        // delivered requests
        File sentFile = MappIntelligenceFile.markAsSent(file);
        if (sentFile != null) {
            this.logger.warn(MappIntelligenceMessages.CANNOT_DELETE_SENT_LOG_FILE, file.getName(), sentFile.getName());

            return result;
        }

        this.logger.error(MappIntelligenceMessages.CANNOT_REMOVE_SENT_LOG_FILE, file.getName());

        return FileResult.RETRY;
    }

    /**
     * A file that was sent partially keeps only the requests that were not delivered, in their order, so the next run
     * does not send the delivered ones again. The log files belong to the cron job alone (the writers only use the
     * temporary files), and only one run at a time processes them (see run()).
     *
     * @param file      Log file
     * @param fileLines Content of the log file
     * @param unsent    Requests that were not delivered, from all request queues
     *
     * @return FileResult.RETRY
     */
    private FileResult keepUnsentRequests(File file, String[] fileLines, List<String> unsent) {
        List<String> remaining = inFileOrder(fileLines, unsent);
        int requests = 0;
        for (String fileLine : fileLines) {
            if (!fileLine.trim().isEmpty()) {
                requests++;
            }
        }

        if (remaining.size() < requests) {
            try {
                MappIntelligenceFile.replaceFileContent(file, remaining);
                this.logger.info(
                    MappIntelligenceMessages.LOG_FILE_SENT_PARTIALLY,
                    file.getName(),
                    requests - remaining.size(),
                    remaining.size()
                );
            } catch (IOException e) {
                // the whole file is sent again with the next run
                this.logger.error(MappIntelligenceMessages.GENERIC_ERROR, e.getClass().getName(), e.getMessage());
            }
        }

        return FileResult.RETRY;
    }

    /**
     * Puts requests of a log file in the order of the file: the request queues do not keep the order between each
     * other.
     *
     * @param fileLines Content of the log file
     * @param requests  Requests of the log file, from all request queues
     *
     * @return The requests in the order of the file - or as they are, if not every request is found in the file
     */
    private static List<String> inFileOrder(String[] fileLines, List<String> requests) {
        Map<String, Integer> requestCount = new HashMap<>();
        for (String request : requests) {
            requestCount.merge(request, 1, Integer::sum);
        }

        List<String> ordered = new ArrayList<>();
        for (String fileLine : fileLines) {
            Integer count = requestCount.get(fileLine);
            if (count != null && count > 0) {
                ordered.add(fileLine);
                requestCount.put(fileLine, count - 1);
            }
        }

        // every request must be found in the file, otherwise the requests are kept as they are
        return (ordered.size() == requests.size()) ? ordered : new ArrayList<>(requests);
    }

    /**
     * Only one run at a time sends the files of a file path and prefix: a run that starts while another one is still
     * sending (e.g. with a slow track server) would send the same log files again. The lock is released by the
     * operating system when the process ends, also if it is killed.
     *
     * @return exit status
     */
    public int run() {
        if (this.deactivate) {
            this.logger.info(MappIntelligenceMessages.TRACKING_IS_DEACTIVATED);
            return EXIT_STATUS_SUCCESS;
        }

        File lockFile = new File(this.filePath, this.filePrefix + RUN_LOCK_EXTENSION);
        try (FileChannel channel = FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            FileLock runLock;
            try {
                runLock = channel.tryLock();
            } catch (OverlappingFileLockException e) {
                // another run in this JVM
                runLock = null;
            }

            if (runLock == null) {
                // not an error: the other run sends the files
                this.logger.info(MappIntelligenceMessages.CRONJOB_IS_RUNNING, this.filePath, this.filePrefix);
                return EXIT_STATUS_SUCCESS;
            }

            try {
                return this.sendFiles();
            } finally {
                runLock.release();
            }
        } catch (IOException e) {
            this.logger.error(MappIntelligenceMessages.REQUEST_LOG_FILES_NOT_FOUND, this.filePath);
            return EXIT_STATUS_FAIL;
        }
    }

    /**
     * @return exit status
     */
    private int sendFiles() {

        int maxFileDuration = (int) this.mic.getOrDefault("maxFileDuration", MappIntelligenceFile.DEFAULT_MAX_FILE_DURATION);
        if (MappIntelligenceFile.checkTemporaryFiles(this.filePath, this.filePrefix, maxFileDuration)) {
            this.logger.info(MappIntelligenceMessages.RENAME_EXPIRED_TEMPORARY_FILE);
        }

        File[] files;
        try {
            files = MappIntelligenceFile.getLogFiles(this.filePath, this.filePrefix);
            if (files.length == 0) {
                // nothing to do is not an error, otherwise monitoring raises an alarm on every quiet run
                this.logger.info(MappIntelligenceMessages.REQUEST_LOG_FILES_NOT_FOUND, this.filePath);
                return EXIT_STATUS_SUCCESS;
            }
        } catch (MappIntelligenceException e) {
            this.logger.error(e.getMessage());
            return EXIT_STATUS_FAIL;
        }

        return this.processFiles(files);
    }
}
