package com.mapp.intelligence.tracking;

import java.io.File;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MappIntelligenceCronjobCommandLineTest {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/test/resources/";
    private static final String FILE_PREFIX = "MappIntelligenceRequests";
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String CONFIG_FILE = FILE_PATH + "config_test.properties";

    @After
    public void tearDown() throws Exception {
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, ".lock");
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION);
    }

    @Test
    public void testConstructor() {
        assertNotNull(new MappIntelligenceCronjobCommandLine());
    }

    @Test
    public void testMappIntelligenceCronjobCommandLine1() {
        // without track ID and track domain the configuration is invalid
        assertEquals(1, MappIntelligenceCronjobCommandLine.execute(new String[0]));
    }

    @Test
    public void testMappIntelligenceCronjobCommandLine2() {
        String[] args = ("-c " + CONFIG_FILE + " -f " + FILE_PATH + " -p " + FILE_PREFIX).split(" ");

        // no log files to send is not an error
        assertEquals(0, MappIntelligenceCronjobCommandLine.execute(args));
    }

    @Test
    public void testHelpExitsSuccessfully() {
        // without track ID and track domain, the help is printed and nothing else is done
        assertEquals(0, MappIntelligenceCronjobCommandLine.execute(new String[] {"--help"}));
    }

    @Test
    public void testVersionExitsSuccessfully() {
        assertEquals(0, MappIntelligenceCronjobCommandLine.execute(new String[] {"--version"}));
    }

    @Test
    public void testVersionDoesNotSend() {
        File file = MappIntelligenceUnitUtil.createFile(FILE_PATH + FILE_PREFIX + "-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), "p=400,0\n");

        String[] args = ("-c " + CONFIG_FILE + " -f " + FILE_PATH + " -p " + FILE_PREFIX + " --version").split(" ");

        assertEquals(0, MappIntelligenceCronjobCommandLine.execute(args));
        assertTrue(file.exists());
    }

    @Test
    public void testMappIntelligenceCronjobCommandLine3() {
        File file = MappIntelligenceUnitUtil.createFile(FILE_PATH + FILE_PREFIX + "-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), "\n\np=400,0\n\n");

        String[] args = ("-c " + CONFIG_FILE + " -f " + FILE_PATH + " -p " + FILE_PREFIX).split(" ");

        assertEquals(0, MappIntelligenceCronjobCommandLine.execute(args));
    }
}
