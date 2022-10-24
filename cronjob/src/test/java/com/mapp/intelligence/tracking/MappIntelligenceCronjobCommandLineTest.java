package com.mapp.intelligence.tracking;

import java.io.File;
import java.security.Permission;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MappIntelligenceCronjobCommandLineTest {
    private static final String FILE_PATH = System.getProperty("user.dir") + "/src/test/resources/";
    private static final String FILE_PREFIX = "MappIntelligenceRequests";
    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String CONFIG_FILE = FILE_PATH + "config_test.properties";

    protected static class ExitException extends SecurityException {
        public final int status;
        public ExitException(int status) {
            super("There is no escape!");
            this.status = status;
        }
    }

    private static class NoExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
            // allow anything.
        }

        @Override
        public void checkPermission(Permission perm, Object context) {
            // allow anything.
        }

        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setSecurityManager(new NoExitSecurityManager());
    }

    @After
    public void tearDown() throws Exception {
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, TEMPORARY_FILE_EXTENSION);
        MappIntelligenceUnitUtil.deleteFiles(FILE_PATH, FILE_PREFIX, LOG_FILE_EXTENSION);

        System.setSecurityManager(null); // or save and restore original
    }

    @Test
    public void testConstructor() {
        try {
            new MappIntelligenceCronjobCommandLine();
        } catch (ExitException e) {
            assertEquals(1, e.status);
        }
    }

    @Test
    public void testMappIntelligenceCronjobCommandLine1() {
        try {
            MappIntelligenceCronjobCommandLine.main(new String[0]);
        } catch (ExitException e) {
            assertEquals(1, e.status);
        }
    }

    @Test
    public void testMappIntelligenceCronjobCommandLine2() {
        String[] args = ("-c " + CONFIG_FILE + " -f " + FILE_PATH + " -p " + FILE_PREFIX).split(" ");

        try {
            MappIntelligenceCronjobCommandLine.main(args);
        } catch (ExitException e) {
            assertEquals(1, e.status);
        }
    }

    @Test
    public void testMappIntelligenceCronjobCommandLine3() {
        File file = MappIntelligenceUnitUtil.createFile(FILE_PATH + FILE_PREFIX + "-1400000000000" + LOG_FILE_EXTENSION);
        MappIntelligenceUnitUtil.writeToFile(file.getAbsolutePath(), "\n\np=400,0\n\n");

        String[] args = ("-c " + CONFIG_FILE + " -f " + FILE_PATH + " -p " + FILE_PREFIX).split(" ");

        try {
            MappIntelligenceCronjobCommandLine.main(args);
        } catch (ExitException e) {
            assertEquals(0, e.status);
        }
    }
}
