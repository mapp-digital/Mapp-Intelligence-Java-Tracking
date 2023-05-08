package com.mapp.intelligence.tracking.util;

import com.mapp.intelligence.tracking.MappIntelligenceLogLevel;
import com.mapp.intelligence.tracking.MappIntelligenceUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class MappIntelligenceDebugLoggerTest {
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
    public void testNewMappIntelligenceDebugLogger() {
        try {
            new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.DEBUG);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testNewMappIntelligenceDebugLogger2() {
        try {
            new MappIntelligenceDebugLogger(null, MappIntelligenceLogLevel.DEBUG);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testFatal1() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.FATAL);

        logger.fatal("fatal1");
        logger.fatal("%s %s", "fatal2", "fatal3");

        assertTrue(this.outContent.toString().contains("FATAL [Mapp Intelligence]: fatal1"));
        assertTrue(this.outContent.toString().contains("FATAL [Mapp Intelligence]: fatal2 fatal3"));
    }

    @Test
    public void testFatal2() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(null, MappIntelligenceLogLevel.FATAL);

        logger.fatal("fatal1");
        logger.fatal("%s %s", "fatal2", "fatal3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testFatal3() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.NONE);

        logger.fatal("fatal1");
        logger.fatal("%s %s", "fatal2", "fatal3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testFatal4() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.DEBUG);

        logger.fatal("fatal1");
        logger.fatal("%s %s", "fatal2", "fatal3");

        assertTrue(this.outContent.toString().contains("FATAL [Mapp Intelligence]: fatal1"));
        assertTrue(this.outContent.toString().contains("FATAL [Mapp Intelligence]: fatal2 fatal3"));
    }

    @Test
    public void testError1() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.ERROR);

        logger.error("error1");
        logger.error("%s %s", "error2", "error3");

        assertTrue(this.outContent.toString().contains("ERROR [Mapp Intelligence]: error1"));
        assertTrue(this.outContent.toString().contains("ERROR [Mapp Intelligence]: error2 error3"));
    }

    @Test
    public void testError2() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(null, MappIntelligenceLogLevel.ERROR);

        logger.error("error1");
        logger.error("%s %s", "error2", "error3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testError3() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.NONE);

        logger.error("error1");
        logger.error("%s %s", "error2", "error3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testError4() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.DEBUG);

        logger.error("error1");
        logger.error("%s %s", "error2", "error3");

        assertTrue(this.outContent.toString().contains("ERROR [Mapp Intelligence]: error1"));
        assertTrue(this.outContent.toString().contains("ERROR [Mapp Intelligence]: error2 error3"));
    }

    @Test
    public void testWarn1() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.WARN);

        logger.warn("warn1");
        logger.warn("%s %s", "warn2", "warn3");

        assertTrue(this.outContent.toString().contains("WARN [Mapp Intelligence]: warn1"));
        assertTrue(this.outContent.toString().contains("WARN [Mapp Intelligence]: warn2 warn3"));
    }

    @Test
    public void testWarn2() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(null, MappIntelligenceLogLevel.WARN);

        logger.warn("warn1");
        logger.warn("%s %s", "warn2", "warn3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testWarn3() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.NONE);

        logger.warn("warn1");
        logger.warn("%s %s", "warn2", "warn3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testWarn4() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.DEBUG);

        logger.warn("warn1");
        logger.warn("%s %s", "warn2", "warn3");

        assertTrue(this.outContent.toString().contains("WARN [Mapp Intelligence]: warn1"));
        assertTrue(this.outContent.toString().contains("WARN [Mapp Intelligence]: warn2 warn3"));
    }

    @Test
    public void testInfo1() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.INFO);

        logger.info("info1");
        logger.info("%s %s", "info2", "info3");

        assertTrue(this.outContent.toString().contains("INFO [Mapp Intelligence]: info1"));
        assertTrue(this.outContent.toString().contains("INFO [Mapp Intelligence]: info2 info3"));
    }

    @Test
    public void testInfo2() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(null, MappIntelligenceLogLevel.INFO);

        logger.info("info1");
        logger.info("%s %s", "info2", "info3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testInfo3() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.NONE);

        logger.info("info1");
        logger.info("%s %s", "info2", "info3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testInfo4() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.DEBUG);

        logger.info("info1");
        logger.info("%s %s", "info2", "info3");

        assertTrue(this.outContent.toString().contains("INFO [Mapp Intelligence]: info1"));
        assertTrue(this.outContent.toString().contains("INFO [Mapp Intelligence]: info2 info3"));
    }

    @Test
    public void testDebug1() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.DEBUG);

        logger.debug("debug1");
        logger.debug("%s %s", "debug2", "debug3");

        assertTrue(this.outContent.toString().contains("DEBUG [Mapp Intelligence]: debug1"));
        assertTrue(this.outContent.toString().contains("DEBUG [Mapp Intelligence]: debug2 debug3"));
    }

    @Test
    public void testDebug2() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(null, MappIntelligenceLogLevel.DEBUG);

        logger.debug("debug1");
        logger.debug("%s %s", "debug2", "debug3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testDebug3() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.NONE);

        logger.debug("debug1");
        logger.debug("%s %s", "debug2", "debug3");

        assertTrue(this.outContent.toString().isEmpty());
    }

    @Test
    public void testDebug4() {
        MappIntelligenceDebugLogger logger = new MappIntelligenceDebugLogger(MappIntelligenceUnitUtil.getCustomLogger(), MappIntelligenceLogLevel.DEBUG);

        logger.debug("debug1");
        logger.debug("%s %s", "debug2", "debug3");

        assertTrue(this.outContent.toString().contains("DEBUG [Mapp Intelligence]: debug1"));
        assertTrue(this.outContent.toString().contains("DEBUG [Mapp Intelligence]: debug2 debug3"));
    }
}
