package com.mapp.intelligence.tracking.consumer;

import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;

import java.io.*;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class MappIntelligenceTrackingConsumerFileTest {
    private static final List<String> contentMaxBatchSize = new ArrayList<>();
    private static final List<String> maxPayloadSize = new ArrayList<>();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final String tempFilePath = System.getProperty("user.dir") + "/src/test/resources/";
    private final String tempFilePathFail = System.getProperty("user.dir") + "src/test/resources/foo/";
    private final String tempFilePrefix = "mapp_intelligence_test";

    /**
     * @return long
     */
    private long getTimestamp() {
        return (new Date()).getTime();
    }

    @BeforeClass
    public static void createTestData() {
        String longText = MappIntelligenceUnitUtil.getLongText();

        for (int i = 0; i < 11 * 1000; i++) {
            contentMaxBatchSize.add("wt?p=300,0");
        }

        for (int i = 0; i < 9 * 1000; i++) {
            maxPayloadSize.add("wt?p=300,0&cp1=" + longText);
        }
    }

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(this.originalOut);

        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");

        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".lock");
        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".log");
    }

    @Test
    public void testNewConsumerFile() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePathFail);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        new MappIntelligenceConsumerFile(config);

        assertTrue(this.outContent.toString().contains("NoSuchFileException"));
    }

    @Test
    public void testFailedToSendBatch() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePathFail);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        assertFalse(consumer.sendBatch(data));
        assertTrue(this.outContent.toString().contains("NoSuchFileException"));
    }

    @Test
    public void testMaxBatchSize() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        // an oversized batch can never be sent: it is dropped instead of blocking the queue forever
        assertTrue(consumer.sendBatch(contentMaxBatchSize));
        assertTrue(this.outContent.toString().contains("Batch size is larger than 10000 req. (11000 req.)"));
        assertTrue(this.outContent.toString().contains("Discarding a batch of 11000 req. that can never be sent"));
    }

    @Test
    public void testMaxPayloadSize() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        // an oversized payload can never be sent: it is dropped instead of blocking the queue forever
        assertTrue(consumer.sendBatch(maxPayloadSize));
        assertTrue(this.outContent.toString().contains("Payload size is larger than 24MB (34.7MB)"));
        assertTrue(this.outContent.toString().contains("Discarding a batch of 9000 req. that can never be sent"));
    }

    @Test
    public void testClosedFile() {
        MappIntelligenceUnitUtil.createFile(this.tempFilePath + this.tempFilePrefix + "-" + this.getTimestamp() + ".tmp");

        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        try {
            MappIntelligenceUnitUtil.getFileChannel(consumer).close();
        } catch (IOException e) {
            // do nothing
        }

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");

        // the closed file is opened again before the next write, nothing is lost
        assertTrue(consumer.sendBatch(data));
        assertTrue(MappIntelligenceUnitUtil
            .getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
            .contains("wt?p=300,0"));
        consumer.close();
    }

    @Test
    public void testWriteBatchRequestInNewFile() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        assertTrue(consumer.sendBatch(data));

        assertTrue(this.outContent.toString().contains("Create new file mapp_intelligence_test"));
        assertTrue(this.outContent.toString().contains("Write batch data in mapp_intelligence_test"));
        assertEquals(
            "wt?p=300,0\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
        );
    }

    @Test
    public void testWriteBatchRequestInExistingNewFile1() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        data.add("wt?p=300,1");
        data.add("wt?p=300,2");
        assertTrue(consumer.sendBatch(data));

        data = new ArrayList<>();
        data.add("wt?p=300,3");
        data.add("wt?p=300,4");
        assertTrue(consumer.sendBatch(data));

        assertTrue(this.outContent.toString().contains("Write batch data in mapp_intelligence_test"));
        assertEquals(
            "wt?p=300,0\nwt?p=300,1\nwt?p=300,2\nwt?p=300,3\nwt?p=300,4\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
        );
    }

    @Test
    public void testWriteBatchRequestInExistingNewFile2() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        data.add("wt?p=300,1");
        data.add("wt?p=300,2");
        assertTrue(consumer.sendBatch(data));

        consumer = new MappIntelligenceConsumerFile(config);
        data = new ArrayList<>();
        data.add("wt?p=300,3");
        data.add("wt?p=300,4");
        assertTrue(consumer.sendBatch(data));

        assertTrue(this.outContent.toString().contains("Write batch data in mapp_intelligence_test"));
        assertEquals(
                "wt?p=300,0\nwt?p=300,1\nwt?p=300,2\nwt?p=300,3\nwt?p=300,4\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
        );
    }

    @Test
    public void testFileLimitReachedFileLines() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("maxFileLines", 5);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        for (int i = 0; i < 10; i++) {
            List<String> data = new ArrayList<>();
            data.add("wt?p=300," + i);

            consumer.sendBatch(data);
        }

        File[] tmp = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        File[] log = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".log");

        assertEquals(1, tmp.length);
        assertEquals(1, log.length);
        assertEquals(
            "wt?p=300,0\nwt?p=300,1\nwt?p=300,2\nwt?p=300,3\nwt?p=300,4\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".log")
        );
        assertEquals(
            "wt?p=300,5\nwt?p=300,6\nwt?p=300,7\nwt?p=300,8\nwt?p=300,9\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
        );
    }

    @Test
    public void testFileLimitReachedFileDuration() throws InterruptedException {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("maxFileDuration", 1000);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        data.add("wt?p=300,1");
        consumer.sendBatch(data);

        MappIntelligenceUnitUtil.await(2000);

        data = new ArrayList<>();
        data.add("wt?p=300,2");
        data.add("wt?p=300,3");
        consumer.sendBatch(data);

        File[] tmp = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        File[] log = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".log");

        assertEquals(1, tmp.length);
        assertEquals(1, log.length);
        assertEquals(
            "wt?p=300,0\nwt?p=300,1\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".log")
        );
        assertEquals(
            "wt?p=300,2\nwt?p=300,3\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
        );
    }

    @Test
    public void testFileLimitReachedFileSize() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("maxFileSize", 10);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        data.add("wt?p=300,1");
        consumer.sendBatch(data);

        data = new ArrayList<>();
        data.add("wt?p=300,2");
        data.add("wt?p=300,3");
        consumer.sendBatch(data);

        File[] tmp = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        File[] log = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".log");

        assertEquals(1, tmp.length);
        assertEquals(1, log.length);
        assertEquals(
            "wt?p=300,0\nwt?p=300,1\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".log")
        );
        assertEquals(
            "wt?p=300,2\nwt?p=300,3\n",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
        );
    }

    @Test
    @Ignore("Disabled on macOS")
    public void testCreateFileWriterFailed() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return;
        }

        String fileName = this.tempFilePath + this.tempFilePrefix + "-" + this.getTimestamp() + ".tmp";
        File file = MappIntelligenceUnitUtil.createFile(fileName);
        file.setReadOnly();

        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("maxFileLines", 5);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        data.add("wt?p=300,1");
        consumer.sendBatch(data);

        File[] tmp = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        File[] log = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".log");

        assertEquals(1, tmp.length);
        assertEquals(0, log.length);
        assertEquals(
            "",
            MappIntelligenceUnitUtil.getFileContent(this.tempFilePath, this.tempFilePrefix, ".tmp")
        );
    }

    @Test
    public void testExtractTimestamp() {
        String fileName = this.tempFilePath + this.tempFilePrefix + "-5" + this.getTimestamp() + ".tmp";
        MappIntelligenceUnitUtil.createFile(fileName);

        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("maxFileLines", 5);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        data.add("wt?p=300,1");
        consumer.sendBatch(data);

        File[] tmp = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        File[] log = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".log");

        assertEquals(1, tmp.length);
        assertEquals(1, log.length);
    }

    @Test
    @Ignore("Disabled on macOS")
    public void testCurrentFileLines() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            return;
        }

        String fileName = this.tempFilePath + this.tempFilePrefix + "-" + this.getTimestamp() + ".tmp";
        File file = MappIntelligenceUnitUtil.createFile(fileName);
        file.setReadable(false, false);

        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("maxFileLines", 5);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");
        data.add("wt?p=300,1");
        consumer.sendBatch(data);

        // java.io.FileNotFoundException (Permission denied)
        assertTrue(this.outContent.toString().contains("java.io.FileNotFoundException"));
    }

    @Test
    public void testFileLimitReached() throws InterruptedException {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("maxFileSize", 10);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        for (int i = 0; i < 25; i++) {
            List<String> data = new ArrayList<>();
            data.add("wt?p=300," + i);
            consumer.sendBatch(data);

            MappIntelligenceUnitUtil.await(5);
        }

        File[] tmp = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        File[] log = MappIntelligenceUnitUtil.getFiles(this.tempFilePath, this.tempFilePrefix, ".log");

        assertEquals(1, tmp.length);
        assertEquals(24, log.length);
    }

    @Test
    public void testConcurrentSendBatchFromMultipleQueueThreads() throws Exception {
        final int threads = 8;
        final int batchesPerThread = 250;
        final int expectedLines = threads * batchesPerThread;

        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.ERROR);
        // low line limit so the threads race on file rotation, not just on the writer
        config.put("maxFileLines", 100);
        config.put("maxFileDuration", 30 * 60 * 1000);
        config.put("maxFileSize", 24 * 1024 * 1024);

        final MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(threads);
        final List<Throwable> failures = java.util.Collections.synchronizedList(new ArrayList<Throwable>());

        for (int t = 0; t < threads; t++) {
            final int threadId = t;
            Thread worker = new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < batchesPerThread; i++) {
                        List<String> batch = new ArrayList<>();
                        batch.add(String.format("wt?p=300,0&thread=%d&i=%d", threadId, i));
                        assertTrue(consumer.sendBatch(batch));
                    }
                } catch (Throwable e) {
                    failures.add(e);
                } finally {
                    done.countDown();
                }
            });
            worker.start();
        }

        start.countDown();
        assertTrue("workers did not finish in time", done.await(60, java.util.concurrent.TimeUnit.SECONDS));
        consumer.close();

        assertEquals("worker threads reported failures: " + failures, 0, failures.size());

        List<String> written = new ArrayList<>();
        written.addAll(this.readAllLines(".tmp"));
        written.addAll(this.readAllLines(".log"));

        Set<String> expected = new HashSet<>();
        for (int t = 0; t < threads; t++) {
            for (int i = 0; i < batchesPerThread; i++) {
                expected.add(String.format("wt?p=300,0&thread=%d&i=%d", t, i));
            }
        }

        // no line may be lost, duplicated or interleaved with another thread's line
        assertEquals(expectedLines, written.size());
        assertEquals(expected, new HashSet<>(written));
    }

    /**
     * @param extension File extension to read
     * @return All non-empty lines of all matching files
     */
    private List<String> readAllLines(String extension) throws IOException {
        return this.readAllLines(this.tempFilePrefix, extension);
    }

    /**
     * @param prefix File prefix
     * @param extension File extension to read
     * @return All non-empty lines of all matching files
     */
    private List<String> readAllLines(String prefix, String extension) throws IOException {
        List<String> lines = new ArrayList<>();
        File dir = new File(this.tempFilePath);
        FilenameFilter filter = (f, name) -> name.startsWith(prefix) && name.endsWith(extension);
        File[] files = dir.listFiles(filter);

        if (files == null) {
            return lines;
        }

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        lines.add(line);
                    }
                }
            }
        }

        return lines;
    }

    @Test
    public void testWriteErrorIsReportedAsFailure() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        // replace the channel with one of the same file that cannot be written, like a full disk
        Object sharedFile = MappIntelligenceUnitUtil.getSharedFile(consumer);
        java.lang.reflect.Field fileField = sharedFile.getClass().getDeclaredField("file");
        fileField.setAccessible(true);
        java.lang.reflect.Field channelField = sharedFile.getClass().getDeclaredField("channel");
        channelField.setAccessible(true);
        ((java.nio.channels.FileChannel) channelField.get(sharedFile)).close();
        channelField.set(sharedFile, java.nio.channels.FileChannel.open(
            (java.nio.file.Path) fileField.get(sharedFile),
            java.nio.file.StandardOpenOption.READ
        ));

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");

        // the batch is lost unless sendBatch reports the failure, so the scheduler can requeue it
        assertFalse(consumer.sendBatch(data));
        assertTrue(this.outContent.toString().contains("The file is not writable"));

        // a failed write must not count towards the rotation limit
        java.lang.reflect.Field currentFileLinesField = sharedFile.getClass().getDeclaredField("currentFileLines");
        currentFileLinesField.setAccessible(true);
        assertEquals(0L, currentFileLinesField.getLong(sharedFile));

        consumer.close();
    }

    @Test
    public void testConsumersForSameFileShareOneFile() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        // own prefix: consumers of other tests that were not closed would share the file otherwise
        String prefix = this.tempFilePrefix + "_shared";
        config.put("filePrefix", prefix);
        config.put("maxFileLines", 4);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);

        // one tracking instance per request: several consumers for the same file at the same time
        MappIntelligenceConsumerFile first = new MappIntelligenceConsumerFile(config);
        MappIntelligenceConsumerFile second = new MappIntelligenceConsumerFile(config);
        assertTrue(MappIntelligenceUnitUtil.getSharedFile(first) == MappIntelligenceUnitUtil.getSharedFile(second));

        for (int i = 0; i < 3; i++) {
            assertTrue(first.sendBatch(Collections.singletonList("wt?p=300,first" + i)));
            assertTrue(second.sendBatch(Collections.singletonList("wt?p=300,second" + i)));
        }

        // closing one consumer must not close the file for the other one
        first.close();
        first.close();
        assertFalse(first.sendBatch(Collections.singletonList("wt?p=300,closed")));
        assertTrue(second.sendBatch(Collections.singletonList("wt?p=300,last")));
        assertTrue(MappIntelligenceUnitUtil.getFileChannel(second) != null);

        second.close();
        assertTrue(MappIntelligenceUnitUtil.getFileChannel(second) == null);

        // one line counter for all consumers: the limit of 4 lines per file is kept, nothing is lost
        List<String> logLines = this.readAllLines(prefix, ".log");
        List<String> tmpLines = this.readAllLines(prefix, ".tmp");
        assertEquals(4, logLines.size());
        assertEquals(3, tmpLines.size());
        assertTrue(tmpLines.contains("wt?p=300,last"));
        assertFalse(logLines.contains("wt?p=300,closed") || tmpLines.contains("wt?p=300,closed"));
    }

    @Test
    public void testConsumerReopensFileRemovedFromOutside() throws Exception {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        assertTrue(consumer.sendBatch(Collections.singletonList("wt?p=300,before")));

        // the cron job renames an expired temporary file into a log file and deletes it after sending
        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".tmp");
        MappIntelligenceUnitUtil.deleteFiles(this.tempFilePath, this.tempFilePrefix, ".lock");

        assertTrue(consumer.sendBatch(Collections.singletonList("wt?p=300,after")));
        assertEquals(Collections.singletonList("wt?p=300,after"), this.readAllLines(".tmp"));

        consumer.close();
    }

    @Test
    public void testAFileBelongsToAPrefixWithTheTimestampAfterTheSeparator() {
        assertTrue(MappIntelligenceConsumerFile.isFileOfPrefix("Shop-1000.abcdef01.log", "Shop", ".log"));
        assertTrue(MappIntelligenceConsumerFile.isFileOfPrefix("Shop-1000.log", "Shop", ".log"));
        assertFalse(MappIntelligenceConsumerFile.isFileOfPrefix("Shop-1000.abcdef01.tmp", "Shop", ".log"));
        assertFalse(MappIntelligenceConsumerFile.isFileOfPrefix("ShopDE-1000.abcdef01.log", "Shop", ".log"));
        assertFalse(MappIntelligenceConsumerFile.isFileOfPrefix("Shop-DE-1000.abcdef01.log", "Shop", ".log"));
        assertFalse(MappIntelligenceConsumerFile.isFileOfPrefix("Shop-2024-1000.abcdef01.log", "Shop", ".log"));
        assertFalse(MappIntelligenceConsumerFile.isFileOfPrefix("Shop-.log", "Shop", ".log"));
        assertTrue(MappIntelligenceConsumerFile.isFileOfPrefix("Shop-DE-1000.abcdef01.log", "Shop-DE", ".log"));
    }
}
