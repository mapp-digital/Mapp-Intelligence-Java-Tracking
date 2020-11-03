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
        int status = 1;
        try {
            MappIntelligenceCronjob c = new MappIntelligenceCronjob(args);
            status = c.run();
        } catch (MappIntelligenceException e) {
            // do nothing
        }

        System.exit(status);
    }
}
