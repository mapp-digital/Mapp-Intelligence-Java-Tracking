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
        this.mic.put("maxQueueSize", 100000);
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
        for (File file : files) {
            String[] fileLines = MappIntelligenceFile.getFileContent(file);

            MappIntelligenceQueue requestQueue = new MappIntelligenceQueue(this.mic);
            for (String fileLine : fileLines) {
                if (fileLine.trim().isEmpty()) {
                    continue;
                }

                requestQueue.add(fileLine);
            }

            CountDownLatch latch = new CountDownLatch(1);
            final boolean[] flushSuccess = {false};

            requestQueue.setOnFlushComplete((success, queueId) -> {
                flushSuccess[0] = success;

                if (flushSuccess[0]) {
                    MappIntelligenceFile.deleteFile(file);
                }

                latch.countDown();
            });

            requestQueue.flush();

            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return EXIT_STATUS_FAIL;
            }

            if (!flushSuccess[0]) {
                return EXIT_STATUS_FAIL;
            }
        }

        return EXIT_STATUS_SUCCESS;
    }

    /**
     * @return exit status
     */
    public int run() {
        if (this.deactivate) {
            this.logger.info(MappIntelligenceMessages.TRACKING_IS_DEACTIVATED);
            return EXIT_STATUS_SUCCESS;
        }

        if (MappIntelligenceFile.checkTemporaryFiles(this.filePath, this.filePrefix)) {
            this.logger.error(MappIntelligenceMessages.RENAME_EXPIRED_TEMPORARY_FILE);
        }

        File[] files;
        try {
            files = MappIntelligenceFile.getLogFiles(this.filePath, this.filePrefix);
            if (files.length == 0) {
                this.logger.info(MappIntelligenceMessages.REQUEST_LOG_FILES_NOT_FOUND, this.filePath);
                return EXIT_STATUS_FAIL;
            }
        } catch (MappIntelligenceException e) {
            this.logger.error(e.getMessage());
            return EXIT_STATUS_FAIL;
        }

        return this.processFiles(files);
    }
}
