package com.mapp.intelligence.tracking;

import org.junit.After;
import org.junit.Test;

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
            MappIntelligenceFile.getLogFiles(FILE_PATH, FILE_PREFIX);
            fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Request log files '" + FILE_PATH + "' not found"));
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
        try {
            MappIntelligenceFile.getFileContent(null);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }
}
