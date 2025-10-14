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

        assertTrue(this.outContent.toString().contains("java.io.IOException"));
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
        assertTrue(this.outContent.toString().contains("java.io.IOException"));
    }

    @Test
    public void testMaxBatchSize() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        assertFalse(consumer.sendBatch(contentMaxBatchSize));
        assertTrue(this.outContent.toString().contains("Batch size is larger than 10000 req. (11000 req.)"));
    }

    @Test
    public void testMaxPayloadSize() {
        Map<String, Object> config = new HashMap<>();
        config.put("filePath", this.tempFilePath);
        config.put("filePrefix", this.tempFilePrefix);
        config.put("logger", MappIntelligenceUnitUtil.getCustomLogger());
        config.put("logLevel", MappIntelligenceLogLevel.DEBUG);
        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);

        assertFalse(consumer.sendBatch(maxPayloadSize));
        assertTrue(this.outContent.toString().contains("Payload size is larger than 24MB (34.7MB)"));
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
            FileWriter fileWriter = MappIntelligenceUnitUtil.getFileWriter(consumer);
            fileWriter.close();
        } catch (IOException e) {
            // do nothing
        }

        List<String> data = new ArrayList<>();
        data.add("wt?p=300,0");

        assertTrue(consumer.sendBatch(data));
        assertTrue(this.outContent.toString().contains("IOException"));
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
}
