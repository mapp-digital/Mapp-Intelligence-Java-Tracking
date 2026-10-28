package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceCronjobCommandLine {
    /**
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.exit(execute(args));
    }

    /**
     * Runs the cron job without terminating the JVM, so it can be tested.
     *
     * @param args Command line arguments
     *
     * @return Exit status (0 = success, nothing to send, help or version; 1 = failure or invalid configuration)
     */
    static int execute(String[] args) {
        // help and version only print their information and exit successfully, nothing is sent
        if (MappIntelligenceCronjob.printInformation(args)) {
            return 0;
        }

        int status = 1;
        try {
            MappIntelligenceCronjob c = new MappIntelligenceCronjob(args);
            status = c.run();
        } catch (MappIntelligenceException e) {
            // do nothing
        }

        return status;
    }
}
