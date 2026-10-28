package com.mapp.intelligence.tracking;

import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.core.MappIntelligenceHybrid;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MappIntelligenceCronjobTest {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/test/resources/";
    private static final String FILE_PREFIX = "MappIntelligenceRequests";
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String DISCARDED_FILE_EXTENSION = ".discarded";
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

        // the run lock of a cron job stays in the directory, the tests with random prefixes leave one each
        File[] runLocks = new File(FILE_PATH).listFiles((dir, name) -> name.endsWith(".cronjob.lock"));
        for (File runLock : runLocks == null ? new File[0] : runLocks) {
            runLock.delete();
        }
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
            // no log files to send is not an error
            assertEquals(0, cronjob.run());
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
        mappIntelligenceConfig.setRequestQueues(0);

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
        mappIntelligenceConfig.setRequestQueues(0);

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
        mappIntelligenceConfig.setRequestQueues(0);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);
            assertEquals(0, cron.run());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testRequestLogFileNotExpired() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);
        mappIntelligenceConfig.setRequestQueues(0);

        MappIntelligenceHybrid tracking = new MappIntelligenceHybrid(mappIntelligenceConfig);
        tracking.setOnFlushComplete((success, queueId) -> {
            latch.countDown();
        });

        for (int i = 0; i < 100; i++) {
            tracking.track("https://sub.domain.tld/pix?p=400," + i);
        }

        tracking.flush();
        assertTrue(latch.await(3000, TimeUnit.MILLISECONDS));

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            // only a temporary file, nothing to send yet
            assertEquals(0, cron.run());
            assertEquals(1, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION).length);
            assertEquals(0, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION).length);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, ".lock");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSendBatchFail() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);
        mappIntelligenceConfig.setMaxFileLines(25);
        mappIntelligenceConfig.setRequestQueues(0);

        MappIntelligenceHybrid tracking = new MappIntelligenceHybrid(mappIntelligenceConfig);
        tracking.setOnFlushComplete((success, queueId) -> {
            latch.countDown();
        });

        for (int i = 0; i < 101; i++) {
            tracking.track("https://sub.domain.tld/pix?p=400," + i);
        }

        tracking.flush();
        assertTrue(latch.await(3000, TimeUnit.MILLISECONDS));

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            // the invalid track ID answers 404: the batches are discarded, so the requests of every file are
            // set aside instead of blocking the run and being sent again next time
            assertEquals(1, cron.run());
            assertEquals(1, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION).length);
            assertEquals(0, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION).length);
            assertEquals(4, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, DISCARDED_FILE_EXTENSION).length);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, ".lock");
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION);
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, DISCARDED_FILE_EXTENSION);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testSendBatchSuccess() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("123451234512345");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(FILE_PREFIX);
        mappIntelligenceConfig.setMaxFileLines(25);
        mappIntelligenceConfig.setRequestQueues(0);

        MappIntelligenceHybrid tracking = new MappIntelligenceHybrid(mappIntelligenceConfig);
        tracking.setOnFlushComplete((success, queueId) -> {
            latch.countDown();
        });

        for (int i = 0; i < 101; i++) {
            tracking.track("https://sub.domain.tld/pix?p=400," + i);
        }

        tracking.flush();
        assertTrue(latch.await(3000, TimeUnit.MILLISECONDS));

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            assertEquals(0, cron.run());
            assertEquals(1, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION).length);
            assertEquals(0, MappIntelligenceUnitUtil.getFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION).length);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, ".lock");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testEmptyFileLines() {
        UUID uuid = UUID.randomUUID();
        File file = MappIntelligenceUnitUtil.createFile(FILE_PATH + uuid + "-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), "\n\np=400,0\n\n");

        MappIntelligenceLogger l = MappIntelligenceUnitUtil.getCustomLogger();

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(uuid.toString());
        mappIntelligenceConfig.setLogger(l);
        mappIntelligenceConfig.setLogLevel(MappIntelligenceLogLevel.DEBUG);
        mappIntelligenceConfig.setRequestQueues(0);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            assertEquals(1, cron.run());

            int logFileCounter = 0;
            File[] logFiles = MappIntelligenceUnitUtil.getFiles(FILE_PATH, uuid.toString(), LOG_FILE_EXTENSION);
            for (File logFile : logFiles) {
                logFileCounter++;
                l.log(logFile.getAbsolutePath());
            }

            int tmpFileCounter = 0;
            File[] tmpFiles = MappIntelligenceUnitUtil.getFiles(FILE_PATH, uuid.toString(), TEMPORARY_FILE_EXTENSION);
            for (File tmpFile : tmpFiles) {
                tmpFileCounter++;
                l.log(tmpFile.getAbsolutePath());
            }

            // the empty lines are skipped, the one request is answered with 404 and discarded, so it is
            // kept in *.log.discarded instead of being sent again
            assertTrue(this.outContent.toString(), 0 == tmpFileCounter);
            assertTrue(this.outContent.toString(), 0 == logFileCounter);
            File[] discardedFiles = MappIntelligenceUnitUtil.getFiles(FILE_PATH, uuid.toString(), DISCARDED_FILE_EXTENSION);
            assertEquals(1, discardedFiles.length);
            assertEquals(Collections.singletonList("p=400,0"), readLines(discardedFiles[0]));
            assertTrue(this.outContent.toString().contains("Add the following request to queue -0- (1 req.): p=400,0"));

            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), LOG_FILE_EXTENSION);
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), DISCARDED_FILE_EXTENSION);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testLogFileIsKeptWhenAnotherQueueStillHasRequests() {
        // Several queues: the ones that received no requests report success immediately. With a
        // CountDownLatch(1) the very first of those reports deleted the file while the queue that
        // actually holds the requests was still sending.
        UUID uuid = UUID.randomUUID();
        File file = MappIntelligenceUnitUtil.createFile(
            FILE_PATH + uuid + "-1400000000000" + LOG_FILE_EXTENSION);
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            content.append("wt?p=400,").append(i).append("&eid=1").append(System.lineSeparator());
        }
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), content.toString());

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(uuid.toString());
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger());
        mappIntelligenceConfig.setLogLevel(MappIntelligenceLogLevel.DEBUG);
        // default is 2 -> 3 queues, all of which have to report before the file may go
        mappIntelligenceConfig.setRequestQueues(2);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            // the invalid track ID answers 404, so nothing was delivered
            assertEquals(1, cron.run());
            File discardedFile = new File(file.getAbsolutePath() + DISCARDED_FILE_EXTENSION);
            assertTrue("the requests must survive an unsuccessful run", discardedFile.exists());
            assertEquals(20, readLines(discardedFile).size());
        } catch (MappIntelligenceException | IOException e) {
            fail();
        } finally {
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), LOG_FILE_EXTENSION);
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), TEMPORARY_FILE_EXTENSION);
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), ".lock");
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), DISCARDED_FILE_EXTENSION);
        }
    }

    @Test
    public void testUndeliveredRequestsAreNotSentAgainOnClose() {
        // A connection error is retryable: the request stays in the queue and the file is kept for
        // the next run. Closing the queue used to send it once more - with every attempt again - so
        // a success there was followed by the same request from the kept file in the next run.
        UUID uuid = UUID.randomUUID();
        File file = MappIntelligenceUnitUtil.createFile(
            FILE_PATH + uuid + "-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), "wt?p=400,0&eid=1" + System.lineSeparator());

        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig(CONFIG_FILE);
        mappIntelligenceConfig.setTrackId("111111111111111");
        // nothing listens on the discard port, so every attempt fails with a connection error
        mappIntelligenceConfig.setTrackDomain("127.0.0.1:9");
        mappIntelligenceConfig.setForceSSL(false);
        mappIntelligenceConfig.setMaxAttempt(2);
        mappIntelligenceConfig.setAttemptTimeout(1);
        mappIntelligenceConfig.setFilePath(FILE_PATH);
        mappIntelligenceConfig.setFilePrefix(uuid.toString());
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger());
        mappIntelligenceConfig.setLogLevel(MappIntelligenceLogLevel.DEBUG);
        mappIntelligenceConfig.setRequestQueues(0);

        try {
            MappIntelligenceCronjob cron = new MappIntelligenceCronjob(mappIntelligenceConfig);

            assertEquals(1, cron.run());
            assertTrue("the file has to be kept for the next run", file.exists());

            String output = this.outContent.toString();
            int sendAttempts = output.split("Send batch data to ", -1).length - 1;
            assertEquals(output, 2, sendAttempts);
        } catch (MappIntelligenceException e) {
            fail();
        } finally {
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), LOG_FILE_EXTENSION);
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), TEMPORARY_FILE_EXTENSION);
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), ".lock");
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), DISCARDED_FILE_EXTENSION);
        }
    }

    @Test
    public void testALogFileThatCannotBeReadIsKept() throws IOException, MappIntelligenceException {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        // a directory with the name of a log file cannot be read as a file
        File logFile = new File(dir, "Shop-1400000000000" + LOG_FILE_EXTENSION);
        assertTrue(logFile.mkdir());

        try {
            MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(
                ("-i 111111111111111 -d analytics01.wt-eu02.net -f " + dir.getAbsolutePath() + " -p Shop").split(" "));

            assertEquals(1, cronjob.run());
            assertTrue(logFile.exists());
        } finally {
            assertTrue(logFile.delete());
            new File(dir, "Shop.lock").delete();
            new File(dir, "Shop.cronjob.lock").delete();
            assertTrue(dir.delete());
        }
    }

    @Test
    public void testThePrefixNeedsTheSeparator() throws IOException, MappIntelligenceException {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        File otherPrefix = new File(dir, "ShopDE-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(otherPrefix.getAbsolutePath(), "wt?p=400,0" + System.lineSeparator());
        File otherPrefixWithSeparator = new File(dir, "Shop-DE-1400000000000.abcdef01" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(otherPrefixWithSeparator.getAbsolutePath(), "wt?p=400,0" + System.lineSeparator());

        try {
            MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(
                ("-i 111111111111111 -d analytics01.wt-eu02.net -f " + dir.getAbsolutePath() + " -p Shop").split(" "));

            // nothing of the prefix "Shop": the files of "ShopDE" and "Shop-DE" are neither sent nor deleted
            assertEquals(0, cronjob.run());
            assertTrue(otherPrefix.exists());
            assertTrue(otherPrefixWithSeparator.exists());
        } finally {
            assertTrue(otherPrefix.delete());
            assertTrue(otherPrefixWithSeparator.delete());
            new File(dir, "Shop.lock").delete();
            new File(dir, "Shop.cronjob.lock").delete();
            assertTrue(dir.delete());
        }
    }

    @Test
    public void testOnlyOneRunAtATime() throws IOException, MappIntelligenceException {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        File logFile = new File(dir, "Shop-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(logFile.getAbsolutePath(), "wt?p=400,0" + System.lineSeparator());
        File lockFile = new File(dir, "Shop.cronjob.lock");

        try (java.nio.channels.FileChannel channel = java.nio.channels.FileChannel.open(lockFile.toPath(),
            java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.WRITE);
             java.nio.channels.FileLock lock = channel.lock()) {
            assertTrue(lock.isValid());

            MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(
                ("-i 111111111111111 -d analytics01.wt-eu02.net -f " + dir.getAbsolutePath() + " -p Shop").split(" "));

            // another run holds the lock: this run ends without sending, and it is not an error
            assertEquals(0, cronjob.run());
            // the file was neither sent nor deleted (the INFO message goes to the logger on stderr)
            assertTrue(logFile.exists());
        } finally {
            assertTrue(logFile.delete());
            assertTrue(lockFile.delete());
            assertTrue(dir.delete());
        }
    }

    @Test
    public void testAMissingFilePathFails() throws MappIntelligenceException {
        MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(
            "-i 111111111111111 -d analytics01.wt-eu02.net -f /nonexistent/path -p Shop".split(" "));

        assertEquals(1, cronjob.run());
    }

    @Test
    public void testAFileSentPartiallyKeepsOnlyTheUnsentRequests() throws Exception {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        File logFile = new File(dir, "Shop-1400000000000" + LOG_FILE_EXTENSION);
        String[] lines = {"wt?p=400,a", "wt?p=400,b", "", "wt?p=400,a", "wt?p=400,c"};

        try {
            MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(
                ("-i 111111111111111 -d analytics01.wt-eu02.net -f " + dir.getAbsolutePath() + " -p Shop").split(" "));
            java.lang.reflect.Method keep = MappIntelligenceCronjob.class.getDeclaredMethod(
                "keepUnsentRequests", File.class, String[].class, java.util.List.class);
            keep.setAccessible(true);

            // b and one of the two a were delivered, from different request queues
            MappIntelligenceUnitUtil.writeToFile(logFile.getAbsolutePath(), String.join(System.lineSeparator(), lines));
            keep.invoke(cronjob, logFile, lines, java.util.Arrays.asList("wt?p=400,c", "wt?p=400,a"));

            // in the order of the file
            assertEquals("wt?p=400,a" + System.lineSeparator() + "wt?p=400,c" + System.lineSeparator(),
                new String(java.nio.file.Files.readAllBytes(logFile.toPath()), java.nio.charset.StandardCharsets.UTF_8));

            // an unsent request that is not in the file (e.g. changed by the queue) is not lost
            MappIntelligenceUnitUtil.writeToFile(logFile.getAbsolutePath(), String.join(System.lineSeparator(), lines));
            keep.invoke(cronjob, logFile, lines, java.util.Collections.singletonList("wt?p=400,c&X-WT-IP=1.2.3.4"));
            assertEquals("wt?p=400,c&X-WT-IP=1.2.3.4" + System.lineSeparator(),
                new String(java.nio.file.Files.readAllBytes(logFile.toPath()), java.nio.charset.StandardCharsets.UTF_8));

            // nothing delivered: the file stays as it is
            MappIntelligenceUnitUtil.writeToFile(logFile.getAbsolutePath(), "wt?p=400,x");
            keep.invoke(cronjob, logFile, new String[]{"wt?p=400,x"}, java.util.Collections.singletonList("wt?p=400,x"));
            assertEquals("wt?p=400,x",
                new String(java.nio.file.Files.readAllBytes(logFile.toPath()), java.nio.charset.StandardCharsets.UTF_8));

            // the file cannot be replaced: the whole file is sent again with the next run
            File missing = new File(dir, "missing/Shop-1400000000001" + LOG_FILE_EXTENSION);
            keep.invoke(cronjob, missing, lines, java.util.Collections.singletonList("wt?p=400,c"));
            assertTrue(!missing.exists());
            String[] files = dir.list();
            assertEquals(1, files.length);
        } finally {
            logFile.delete();
            new File(dir, "Shop.cronjob.lock").delete();
            assertTrue(dir.delete());
        }
    }

    /**
     * Answers every batch by its first request, SENT if the request is not in the map.
     */
    private static class ResultConsumer implements MappIntelligenceConsumer {
        private final Map<String, BatchResult> results = new HashMap<>();
        private final List<String> requests = Collections.synchronizedList(new ArrayList<>());

        ResultConsumer(String... requestAndResult) {
            for (int i = 0; i < requestAndResult.length; i += 2) {
                this.results.put(requestAndResult[i], BatchResult.valueOf(requestAndResult[i + 1]));
            }
        }

        @Override
        public boolean sendBatch(List<String> batchContent) {
            return this.send(batchContent) != BatchResult.RETRY;
        }

        @Override
        public BatchResult send(List<String> batchContent) {
            this.requests.addAll(batchContent);

            return this.results.getOrDefault(batchContent.get(0), BatchResult.SENT);
        }
    }

    /**
     * @param dir          File path
     * @param consumer     Consumer
     * @param maxBatchSize Maximum batch size, the cron job itself uses 1000
     *
     * @return Cron job for the prefix "Shop" in the directory
     */
    private static MappIntelligenceCronjob createCronjob(File dir, MappIntelligenceConsumer consumer, int maxBatchSize)
        throws Exception {
        MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net");
        mappIntelligenceConfig.setFilePath(dir.getAbsolutePath());
        mappIntelligenceConfig.setFilePrefix("Shop");
        mappIntelligenceConfig.setConsumer(consumer);
        mappIntelligenceConfig.setLogger(MappIntelligenceUnitUtil.getCustomLogger());
        mappIntelligenceConfig.setMaxAttempt(1);
        mappIntelligenceConfig.setRequestQueues(0);

        MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(mappIntelligenceConfig);
        java.lang.reflect.Field mic = MappIntelligenceCronjob.class.getDeclaredField("mic");
        mic.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> config = (Map<String, Object>) mic.get(cronjob);
        config.put("maxBatchSize", maxBatchSize);

        return cronjob;
    }

    /**
     * @param file  File
     * @param lines Requests
     */
    private static void writeLines(File file, String... lines) {
        StringBuilder content = new StringBuilder();
        for (String line : lines) {
            content.append(line).append(System.lineSeparator());
        }
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), content.toString());
    }

    /**
     * @param file File
     *
     * @return The lines of the file
     */
    private static List<String> readLines(File file) throws IOException {
        return java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * @param dir Directory
     *
     * @return The file names in the directory, without the run lock, sorted
     */
    private static List<String> names(File dir) {
        List<String> names = new ArrayList<>();
        for (String name : dir.list()) {
            if (!name.endsWith(".lock")) {
                names.add(name);
            }
        }
        Collections.sort(names);

        return names;
    }

    /**
     * @param dir Directory, removed with everything in it
     */
    private static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        for (File file : files == null ? new File[0] : files) {
            if (file.isDirectory()) {
                deleteDirectory(file);
            } else {
                assertTrue(file.delete());
            }
        }
        assertTrue(dir.delete());
    }

    @Test
    public void testTheDiscardedRequestsOfAFileAreKeptTheNextFilesAreStillSent() throws Exception {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        try {
            writeLines(new File(dir, "Shop-1000000000001" + LOG_FILE_EXTENSION), "wt?p=300,a");
            writeLines(new File(dir, "Shop-1000000000002" + LOG_FILE_EXTENSION), "wt?p=300,b");
            ResultConsumer consumer = new ResultConsumer("wt?p=300,a", "DISCARDED");

            assertEquals(1, createCronjob(dir, consumer, 1000).run());
            assertEquals(Collections.singletonList("Shop-1000000000001.log.discarded"), names(dir));
            assertEquals(Collections.singletonList("wt?p=300,a"), readLines(new File(dir, "Shop-1000000000001.log.discarded")));
            assertEquals(Arrays.asList("wt?p=300,a", "wt?p=300,b"), consumer.requests);
            assertTrue(this.outContent.toString(), this.outContent.toString().contains(
                "Log file Shop-1000000000001.log contained 1 req. that can never be sent, they are kept in "
                    + "Shop-1000000000001.log.discarded and not sent again"));
        } finally {
            deleteDirectory(dir);
        }
    }

    @Test
    public void testOnlyTheDiscardedRequestsAreKeptNotTheDeliveredOnes() throws Exception {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        try {
            writeLines(new File(dir, "Shop-1000000000001" + LOG_FILE_EXTENSION), "wt?p=300,a", "wt?p=300,b", "wt?p=300,c");
            ResultConsumer consumer = new ResultConsumer("wt?p=300,b", "DISCARDED");

            assertEquals(1, createCronjob(dir, consumer, 1).run());
            assertEquals(Arrays.asList("wt?p=300,a", "wt?p=300,b", "wt?p=300,c"), consumer.requests);
            assertEquals(Collections.singletonList("Shop-1000000000001.log.discarded"), names(dir));
            assertEquals(Collections.singletonList("wt?p=300,b"), readLines(new File(dir, "Shop-1000000000001.log.discarded")));
        } finally {
            deleteDirectory(dir);
        }
    }

    @Test
    public void testDiscardedAndUnsentRequestsOfOneFileAreBothKept() throws Exception {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        File logFile = new File(dir, "Shop-1000000000001" + LOG_FILE_EXTENSION);
        File discardedFile = new File(dir, "Shop-1000000000001.log.discarded");
        try {
            writeLines(logFile, "wt?p=300,a", "wt?p=300,b", "wt?p=300,c", "wt?p=300,d");
            ResultConsumer consumer = new ResultConsumer("wt?p=300,b", "DISCARDED", "wt?p=300,c", "RETRY");

            assertEquals(1, createCronjob(dir, consumer, 1).run());
            assertEquals(Arrays.asList("Shop-1000000000001.log", "Shop-1000000000001.log.discarded"), names(dir));
            assertEquals(Arrays.asList("wt?p=300,c", "wt?p=300,d"), readLines(logFile));
            assertEquals(Collections.singletonList("wt?p=300,b"), readLines(discardedFile));

            // the next run adds its discarded requests to the same file
            ResultConsumer next = new ResultConsumer("wt?p=300,d", "DISCARDED");
            assertEquals(1, createCronjob(dir, next, 1).run());
            assertEquals(Collections.singletonList("Shop-1000000000001.log.discarded"), names(dir));
            assertEquals(Arrays.asList("wt?p=300,b", "wt?p=300,d"), readLines(discardedFile));
        } finally {
            deleteDirectory(dir);
        }
    }

    @Test
    public void testAFileWhoseDiscardedRequestsCannotBeWrittenIsKept() throws Exception {
        File dir = java.nio.file.Files.createTempDirectory("mapp-cronjob").toFile();
        File logFile = new File(dir, "Shop-1000000000001" + LOG_FILE_EXTENSION);
        try {
            writeLines(logFile, "wt?p=300,a", "wt?p=300,b");
            writeLines(new File(dir, "Shop-1000000000002" + LOG_FILE_EXTENSION), "wt?p=300,c");
            // a directory with the name of the discarded file cannot be written as a file
            assertTrue(new File(dir, "Shop-1000000000001.log.discarded").mkdir());
            ResultConsumer consumer = new ResultConsumer("wt?p=300,b", "DISCARDED");

            assertEquals(1, createCronjob(dir, consumer, 1).run());
            // unchanged, and the run stops: the next file is not sent
            assertEquals(Arrays.asList("wt?p=300,a", "wt?p=300,b"), readLines(logFile));
            assertEquals(Arrays.asList("wt?p=300,a", "wt?p=300,b"), consumer.requests);
            assertTrue(this.outContent.toString(), this.outContent.toString().contains(
                "The 1 req. of log file Shop-1000000000001.log that can never be sent could not be written to "
                    + "Shop-1000000000001.log.discarded, the file is sent again with the next run"));
        } finally {
            deleteDirectory(dir);
        }
    }

    @Test
    public void testLargeFileIsReadCompletely() throws IOException {
        // a single InputStream.read() is not guaranteed to fill the whole buffer
        UUID uuid = UUID.randomUUID();
        File file = MappIntelligenceUnitUtil.createFile(
            FILE_PATH + uuid + "-1400000000000" + LOG_FILE_EXTENSION);

        int lines = 20000;
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < lines; i++) {
            content.append("wt?p=400,").append(i).append("&cp1=").append(i).append(System.lineSeparator());
        }
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), content.toString());

        try {
            String[] fileLines = MappIntelligenceFile.getFileContent(file);

            assertEquals(lines, fileLines.length);
            assertTrue(fileLines[0].startsWith("wt?p=400,0&"));
            assertTrue(fileLines[lines - 1].startsWith("wt?p=400," + (lines - 1) + "&"));
        } finally {
            MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, uuid.toString(), LOG_FILE_EXTENSION);
        }
    }
}
