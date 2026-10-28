package com.mapp.intelligence.tracking;

import org.junit.After;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class MappIntelligenceFileTest {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/test/resources/";
    private static final String FILE_PREFIX = "MappIntelligenceRequests";
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String TEST_TEMPORARY_FILE_1 = FILE_PATH + FILE_PREFIX + "-1400000000000" + TEMPORARY_FILE_EXTENSION;
    private static final String TEST_TEMPORARY_FILE_2 = FILE_PATH + FILE_PREFIX + "-14000000" + TEMPORARY_FILE_EXTENSION;

    @After
    public void restoreStreams() {
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, ".lock");
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION);
    }

    @Test
    public void testCheckTemporaryFiles1() {
        MappIntelligenceUnitUtil.createFile(TEST_TEMPORARY_FILE_1);

        assertTrue(MappIntelligenceFile.checkTemporaryFiles(FILE_PATH, FILE_PREFIX));
    }

    @Test
    public void testCheckTemporaryFiles2() {
        MappIntelligenceUnitUtil.createFile(TEST_TEMPORARY_FILE_2);

        assertTrue(MappIntelligenceFile.checkTemporaryFiles(FILE_PATH, FILE_PREFIX));
    }

    @Test
    public void testCheckTemporaryFiles3() {
        assertFalse(MappIntelligenceFile.checkTemporaryFiles(FILE_PATH, FILE_PREFIX + "_foo"));
    }

    @Test
    public void testCheckTemporaryFiles4() {
        assertFalse(MappIntelligenceFile.checkTemporaryFiles(FILE_PATH + "foo/", FILE_PREFIX));
    }

    @Test
    public void testGetLogFiles1() {
        for (int i = 0; i < 10; i++) {
            MappIntelligenceUnitUtil.createFile(FILE_PATH + FILE_PREFIX + "-" + i + LOG_FILE_EXTENSION);
        }

        try {
            assertEquals(10, MappIntelligenceFile.getLogFiles(FILE_PATH, FILE_PREFIX).length);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetLogFiles2() {
        try {
            File[] files = MappIntelligenceFile.getLogFiles(FILE_PATH, FILE_PREFIX);
            assertEquals(0, files.length);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetLogFiles3() {
        try {
            MappIntelligenceFile.getLogFiles(FILE_PATH + "foo/", FILE_PREFIX);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Request log files '" + FILE_PATH + "foo/' not found"));
        }
    }

    @Test
    public void testDeleteFile() {
        try {
            MappIntelligenceFile.deleteFile(null);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetFileContent() {
        // a file that cannot be read is no empty file: an empty file is deleted by the cron job
        try {
            MappIntelligenceFile.getFileContent(null);
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }

        try {
            MappIntelligenceFile.getFileContent(new File("/nonexistent/file.log"));
            fail();
        } catch (IOException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testCheckTemporaryFilesUsesMaxFileDuration() {
        String recentFile = FILE_PATH + FILE_PREFIX + "-" + (System.currentTimeMillis() - 60 * 1000) + TEMPORARY_FILE_EXTENSION;
        MappIntelligenceUnitUtil.createFile(recentFile);

        // one minute old: not expired with the default of 30 minutes, expired with 30 seconds
        assertFalse(MappIntelligenceFile.checkTemporaryFiles(FILE_PATH, FILE_PREFIX));
        assertTrue(MappIntelligenceFile.checkTemporaryFiles(FILE_PATH, FILE_PREFIX, 30 * 1000));
    }

    @Test
    public void testDeleteFileReportsResult() {
        String logFile = FILE_PATH + FILE_PREFIX + "-1400000000000" + LOG_FILE_EXTENSION;
        MappIntelligenceUnitUtil.createFile(logFile);

        assertTrue(MappIntelligenceFile.deleteFile(new File(logFile)));
        assertFalse(MappIntelligenceFile.deleteFile(new File(logFile)));
        assertFalse(MappIntelligenceFile.deleteFile(null));
    }

    @Test
    public void testMarkAsSent() throws Exception {
        String logFile = FILE_PATH + FILE_PREFIX + "-1400000000000" + LOG_FILE_EXTENSION;
        MappIntelligenceUnitUtil.createFile(logFile);

        File sentFile = MappIntelligenceFile.markAsSent(new File(logFile));
        try {
            assertEquals(FILE_PREFIX + "-1400000000000.log.sent", sentFile.getName());
            // a sent file is no longer picked up
            assertEquals(0, MappIntelligenceFile.getLogFiles(FILE_PATH, FILE_PREFIX).length);
        } finally {
            MappIntelligenceFile.deleteFile(sentFile);
        }
    }
}
