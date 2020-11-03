package com.mapp.intelligence.tracking;

import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.core.MappIntelligenceHybrid;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MappIntelligenceCronjobTest {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/test/resources/";
    private static final String FILE_PREFIX = "MappIntelligenceRequests";
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String CONFIG_FILE = FILE_PATH + "config_test.properties";

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(this.originalOut);
    }

    @Test
    public void testHelp() {
        String[] args = "--help".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            fail();
        } catch (Exception e) {
            assertTrue(this.outContent.toString().length() > 500);
        }
    }

    @Test
    public void testVersion() {
        String[] args = "--version".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            fail();
        } catch (Exception e) {
            assertTrue(true);
        }
    }

    @Test
    public void testDebug() {
        String[] args = ("-c " + CONFIG_FILE + " --debug").split(" ");

        try {
            MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(args);
            assertEquals(1, cronjob.run());
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMappIntelligenceConfigRequiredTrackId() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();

        try {
            new MappIntelligenceCronjob(mappIntelligenceConfig);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-i\" or alternative \"--trackId\" are required", e.getMessage());
        }
    }

    @Test
    public void testMappIntelligenceConfigRequiredTrackDomain() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setTrackId("123451234512345");

        try {
            new MappIntelligenceCronjob(mappIntelligenceConfig);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-d\" or alternative \"--trackDomain\" are required", e.getMessage());
        }
    }

    @Test
    public void testCommandLineArgumentsRequiredTrackId() {
        try {
            new MappIntelligenceCronjob(new String[0]);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-i\" or alternative \"--trackId\" are required", e.getMessage());
        }
    }

    @Test
    public void testCommandLineArgumentsRequiredTrackDomain1() {
        String[] args = "-i 123451234512345".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-d\" or alternative \"--trackDomain\" are required", e.getMessage());
        }
    }

    @Test
    public void testCommandLineArgumentsRequiredTrackDomain2() {
        String[] args = "--trackId=123451234512345".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-d\" or alternative \"--trackDomain\" are required", e.getMessage());
        }
    }

    @Test
    public void testMappIntelligenceWithTrackIdAndTrackDomain() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setTrackId("123451234512345");
        mappIntelligenceConfig.setTrackDomain("q3.webtrekk.net");

        try {
            new MappIntelligenceCronjob(mappIntelligenceConfig);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCommandLineArgumentsWithTrackIdAndTrackDomain1() {
        String[] args = "-i 123451234512345 -d q3.webtrekk.net".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCommandLineArgumentsWithTrackIdAndTrackDomain2() {
        String[] args = "--trackId=123451234512345 --trackDomain=q3.webtrekk.net".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMappIntelligenceConfigAll() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
        mappIntelligenceConfig.setTrackId("123451234512345");
        mappIntelligenceConfig.setTrackDomain("q3.webtrekk.net");
        mappIntelligenceConfig.setFilePath("/tmp/");
        mappIntelligenceConfig.setFilePrefix("MappIntelligenceData");

        try {
            new MappIntelligenceCronjob(mappIntelligenceConfig);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCommandLineArgumentsConfigAll1() {
        String[] args = "-i 123451234512345 -d q3.webtrekk.net -f /tmp/ -p MappIntelligenceData".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCommandLineArgumentsConfigAll2() {
        String[] args = "--trackId=123451234512345 --trackDomain=q3.webtrekk.net --filePath=/tmp/ --filePrefix=MappIntelligenceData".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testMappIntelligenceInvalidConfigFile() {
        String configFile = System.getProperty("user.dir") + "/src/main/resources/foo.bar";
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(configFile);

        try {
            new MappIntelligenceCronjob(mappIntelligenceConfig);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-i\" or alternative \"--trackId\" are required", e.getMessage());
        }
    }

    @Test
    public void testCommandLineArgumentsInvalidConfigFile1() {
        String[] args = ("-c " + System.getProperty("user.dir") + "/src/main/resources/foo.bar").split(" ");

        try {
            new MappIntelligenceCronjob(args);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-i\" or alternative \"--trackId\" are required", e.getMessage());
        }
    }

    @Test
    public void testCommandLineArgumentsInvalidConfigFile2() {
        String[] args = ("--config=" + System.getProperty("user.dir") + "/src/main/resources/foo.bar").split(" ");

        try {
            new MappIntelligenceCronjob(args);
            fail();
        } catch (Exception e) {
            assertEquals("Argument \"-i\" or alternative \"--trackId\" are required", e.getMessage());
        }
    }

    @Test
    public void testMappIntelligenceConfigFile() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);

        try {
            new MappIntelligenceCronjob(mappIntelligenceConfig);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCommandLineArgumentsConfigFile1() {
        String[] args = ("-c " + CONFIG_FILE).split(" ");

        try {
            new MappIntelligenceCronjob(args);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCommandLineArgumentsConfigFile2() {
        String[] args = ("--config=" + CONFIG_FILE).split(" ");

        try {
            new MappIntelligenceCronjob(args);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCommandLineArgumentsUnrecognizedOptionException() {
        String[] args = "--foo=123451234512345 --bar=q3.webtrekk.net".split(" ");

        try {
            new MappIntelligenceCronjob(args);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("--foo=123451234512345"));
            assertEquals("com.mapp.intelligence.tracking.MappIntelligenceException", e.getClass().getName());
        }
    }

    @Test
    public void testRunDeactivated() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setDeactivate(true);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);
            assertEquals(0, cron.run());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testRenameTemporaryFiles() {
        long time = Long.parseLong("1400000000000");
        for (int i = 0; i < 10; i++) {
            time += i * 10;
            MappIntelligenceUnitUtil.createFile(FILE_PATH + FILE_PREFIX + "-" + time + TEMPORARY_FILE_EXTENSION);
        }

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);
            assertEquals(0, cron.run());

            File[] logFiles = MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION);
            assertEquals(0, logFiles.length);

            File[] temporaryFiles = MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);
            assertEquals(0, temporaryFiles.length);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testEmptyDirectory() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);
            assertEquals(1, cron.run());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testRequestLogFileNotExpired() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);

        MappIntelligenceHybrid tracking = new MappIntelligenceHybrid(mappIntelligenceConfig);
        for (int i = 0; i < 100; i++) {
            tracking.track("https://sub.domain.tld/pix?p=400," + i);
        }

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            assertEquals(1, cron.run());
            assertEquals(1, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION).length);
            assertEquals(0, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION).length);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSendBatchFail() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);
        mappIntelligenceConfig.setMaxFileLines(25);

        MappIntelligenceHybrid tracking = new MappIntelligenceHybrid(mappIntelligenceConfig);
        for (int i = 0; i < 101; i++) {
            tracking.track("https://sub.domain.tld/pix?p=400," + i);
        }

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            assertEquals(1, cron.run());
            assertEquals(1, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION).length);
            assertEquals(4, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION).length);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSendBatchSuccess() {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("123451234512345");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);
        mappIntelligenceConfig.setMaxFileLines(25);

        MappIntelligenceHybrid tracking = new MappIntelligenceHybrid(mappIntelligenceConfig);
        for (int i = 0; i < 101; i++) {
            tracking.track("https://sub.domain.tld/pix?p=400," + i);
        }

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            assertEquals(0, cron.run());
            assertEquals(1, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION).length);
            assertEquals(0, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION).length);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testEmptyFileLines() {
        File file = MappIntelligenceUnitUtil.createFile(FILE_PATH + FILE_PREFIX + "-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), "\n\np=400,0\n\n");

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            assertEquals(1, cron.run());
            assertEquals(0, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION).length);
            assertEquals(1, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION).length);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION);
        } catch (Exception e) {
            fail();
        }
    }
}
