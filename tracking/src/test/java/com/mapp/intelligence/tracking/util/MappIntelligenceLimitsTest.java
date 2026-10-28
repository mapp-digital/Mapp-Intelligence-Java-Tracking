package com.mapp.intelligence.tracking.util;

import com.mapp.intelligence.tracking.MappIntelligenceConsumer;
import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Limits of the configuration and the request queue.
 */
public class MappIntelligenceLimitsTest {
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
    public void testTooLargeFileLimitsAreReducedWithWarning() {
        Map<String, Object> config = new MappIntelligenceConfig()
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.WARN)
            .setMaxFileLines(20000)
            .setMaxFileDuration(30 * 60 * 1000 + 1)
            .setMaxFileSize(100 * 1024 * 1024)
            .build();

        assertEquals(10000, config.get("maxFileLines"));
        assertEquals(30 * 60 * 1000, config.get("maxFileDuration"));
        assertEquals(24 * 1024 * 1024, config.get("maxFileSize"));

        String log = this.outContent.toString();
        assertTrue(log, log.contains("Value 20000 for maxFileLines is larger than the maximum, 10000 is used"));
        assertTrue(log, log.contains("for maxFileDuration is larger than the maximum, 1800000 is used"));
        assertTrue(log, log.contains("for maxFileSize is larger than the maximum, 25165824 is used"));
    }

    @Test
    public void testMaxBatchSizeOverwrittenForFileConsumerIsLogged() {
        Map<String, Object> config = new MappIntelligenceConfig()
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.DEBUG)
            .setConsumerType(MappIntelligenceConsumerType.FILE)
            .setMaxBatchSize(500)
            .build();

        assertEquals(1, config.get("maxBatchSize"));
        String log = this.outContent.toString();
        assertTrue(log, log.contains("maxBatchSize 500 is set to 1, the consumer FILE writes every request immediately"));
    }

    @Test
    public void testMaxQueueSizeDiscardsOldestRequests() throws InterruptedException {
        // a consumer that can never deliver: the queue cannot be emptied by sending
        MappIntelligenceConsumer consumer = batchContent -> false;
        Map<String, Object> config = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net")
            .setLogger(MappIntelligenceUnitUtil.getCustomLogger())
            .setLogLevel(MappIntelligenceLogLevel.WARN)
            .setRequestQueues(0)
            .setMaxQueueSize(3)
            .build();

        RequestScheduler scheduler = new RequestScheduler(config, consumer, false);
        // two flushes: the full queue (third request) and flush() - the queue is only complete again after both,
        // a failed batch is put back into the queue request by request
        CountDownLatch flushes = new CountDownLatch(2);
        scheduler.setOnFlushComplete((success, queueId) -> flushes.countDown());
        for (int i = 0; i < 4; i++) {
            scheduler.add("wt?p=300," + i);
        }
        scheduler.flush();

        assertTrue(flushes.await(3, TimeUnit.SECONDS));

        assertEquals(
            Arrays.asList("wt?p=300,1", "wt?p=300,2", "wt?p=300,3"),
            new ArrayList<>(scheduler.getQueue(0))
        );
        String log = this.outContent.toString();
        assertTrue(log, log.contains("is full (4 req.), the 1 oldest requests are discarded"));
        scheduler.close(false);
    }

    @Test
    public void testMaxQueueSizeZeroIsUnlimited() {
        MappIntelligenceConsumer consumer = batchContent -> false;
        Map<String, Object> config = new MappIntelligenceConfig("111111111111111", "analytics01.wt-eu02.net")
            .setRequestQueues(0)
            .setMaxQueueSize(0)
            .build();

        RequestScheduler scheduler = new RequestScheduler(config, consumer, false);
        for (int i = 0; i < 2000; i++) {
            scheduler.add("wt?p=300," + i);
        }

        assertEquals(2000, scheduler.getQueue(0).size());
        scheduler.close(false);
    }

    @Test
    public void testFilePathWithoutSeparator() throws IOException {
        Path directory = Files.createTempDirectory("mapp_intelligence_path");
        Map<String, Object> config = new MappIntelligenceConfig()
            .setConsumerType(MappIntelligenceConsumerType.FILE)
            // no trailing separator: the files still belong into the directory
            .setFilePath(directory.toString())
            .setFilePrefix("prefix")
            .build();

        MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);
        assertEquals(MappIntelligenceConsumer.BatchResult.SENT, consumer.send(Collections.singletonList("wt?p=300,0")));
        consumer.close();

        File[] files = directory.toFile().listFiles((d, name) -> name.startsWith("prefix") && name.endsWith(".tmp"));
        assertEquals(1, files.length);
        assertTrue(files[0].getName().matches("^prefix-\\d{13}\\.[0-9a-f]{8}\\.tmp$"));

        for (File file : directory.toFile().listFiles()) {
            Files.delete(file.toPath());
        }
        Files.delete(directory);
    }
}
