package com.mapp.intelligence.tracking;

import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerFile;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;

/**
 * Several JVMs write into the same request logging files while the cron job takes them over. Afterwards
 * every request has to be on disk exactly once and intact.
 *
 * <p>A small run is part of the regular tests. The large run only starts with {@code -DstressTest=true}
 * ({@code make stress-test}); its size is set with {@code -Dstress.jvms}, {@code -Dstress.threads},
 * {@code -Dstress.requests} and {@code -Dstress.maxFileLines}.</p>
 */
public class MappIntelligenceStressTest {
    private static final String FILE_PREFIX = "MappIntelligenceStress";
    private static final String LINE_PATTERN = "^W\\d+-T\\d+-\\d+$";

    private Path directory;
    private Path archive;

    @Before
    public void setUp() throws IOException {
        this.directory = Files.createTempDirectory("mapp_intelligence_stress");
        this.archive = Files.createDirectory(this.directory.resolve("done"));
    }

    @After
    public void tearDown() throws IOException {
        for (Path dir : new Path[] {this.archive, this.directory}) {
            File[] files = dir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    Files.deleteIfExists(file.toPath());
                }
            }
        }

        Files.deleteIfExists(this.archive);
        Files.deleteIfExists(this.directory);
    }

    /**
     * One writer JVM: several threads, one consumer per request (like one tracking instance per request).
     *
     * @param args directory, writer id, threads, requests per thread, max file lines
     *
     * @throws InterruptedException Interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        String directory = args[0];
        String writer = args[1];
        int threads = Integer.parseInt(args[2]);
        int requests = Integer.parseInt(args[3]);
        int maxFileLines = Integer.parseInt(args[4]);

        Map<String, Object> config = new HashMap<>();
        config.put("filePath", directory);
        config.put("filePrefix", FILE_PREFIX);
        config.put("maxFileLines", maxFileLines);
        config.put("logLevel", MappIntelligenceLogLevel.ERROR);
        config.put("logger", null);

        AtomicLong notSent = new AtomicLong();
        List<Thread> workers = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            final int thread = t;
            Thread worker = new Thread(() -> {
                for (int i = 0; i < requests; i++) {
                    MappIntelligenceConsumerFile consumer = new MappIntelligenceConsumerFile(config);
                    String line = "W" + writer + "-T" + thread + "-" + i;
                    if (consumer.send(Collections.singletonList(line)) != MappIntelligenceConsumer.BatchResult.SENT) {
                        notSent.incrementAndGet();
                    }
                    consumer.close();
                }
            });
            worker.start();
            workers.add(worker);
        }

        for (Thread worker : workers) {
            worker.join();
        }

        System.exit(notSent.get() == 0 ? 0 : 1);
    }

    /**
     * @return Class path of the forked test JVM, also for the writer JVMs
     */
    private static String getClassPath() {
        String classPath = System.getProperty("surefire.test.class.path");
        return (classPath != null && !classPath.isEmpty()) ? classPath : System.getProperty("java.class.path");
    }

    /**
     * Takes the files over like the cron job: expired temporary files (immediately) become log files, which
     * are moved into the archive.
     */
    private void takeOver() throws IOException {
        MappIntelligenceFile.checkTemporaryFiles(this.directory + File.separator, FILE_PREFIX, 0);

        File[] logFiles = MappIntelligenceFile.getFiles(this.directory + File.separator, FILE_PREFIX, ".log");
        if (logFiles != null) {
            for (File logFile : logFiles) {
                Files.move(logFile.toPath(), this.archive.resolve(logFile.getName()), StandardCopyOption.ATOMIC_MOVE);
            }
        }
    }

    /**
     * @return All request lines in the directory and the archive
     */
    private List<String> readAllLines() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Path dir : new Path[] {this.directory, this.archive}) {
            File[] files = dir.toFile().listFiles((d, name) -> name.endsWith(".tmp") || name.endsWith(".log"));
            if (files != null) {
                for (File file : files) {
                    for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                        if (!line.isEmpty()) {
                            lines.add(line);
                        }
                    }
                }
            }
        }

        return lines;
    }

    /**
     * @param jvms Number of writer JVMs
     * @param threads Threads per JVM
     * @param requests Requests per thread
     * @param maxFileLines Max lines per temporary file (small values rotate often)
     */
    private void runWritersWithTakeover(int jvms, int threads, int requests, int maxFileLines) throws Exception {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        long start = System.currentTimeMillis();

        List<Process> processes = new ArrayList<>();
        for (int j = 0; j < jvms; j++) {
            processes.add(new ProcessBuilder(
                javaBin, "-cp", getClassPath(), MappIntelligenceStressTest.class.getName(),
                this.directory + File.separator, String.valueOf(j), String.valueOf(threads),
                String.valueOf(requests), String.valueOf(maxFileLines)
            ).inheritIO().start());
        }

        int failedWriters = 0;
        for (Process process : processes) {
            while (!process.waitFor(10, TimeUnit.MILLISECONDS)) {
                this.takeOver();
            }

            failedWriters += (process.exitValue() == 0) ? 0 : 1;
        }

        List<String> lines = this.readAllLines();
        Set<String> unique = new HashSet<>(lines);
        long intact = lines.stream().filter(line -> line.matches(LINE_PATTERN)).count();
        int expected = jvms * threads * requests;

        System.out.printf(
            "stress test: %d JVMs x %d threads x %d requests, maxFileLines=%d: expected=%d written=%d unique=%d "
                + "intact=%d failedWriters=%d (%d ms)%n",
            jvms, threads, requests, maxFileLines, expected, lines.size(), unique.size(), intact, failedWriters,
            System.currentTimeMillis() - start
        );

        assertEquals("failed writers", 0, failedWriters);
        assertEquals("written", expected, lines.size());
        assertEquals("unique", expected, unique.size());
        assertEquals("intact", expected, intact);
    }

    @Test
    public void testConcurrentJvmsWithCronJob() throws Exception {
        this.runWritersWithTakeover(2, 2, 200, 20);
    }

    @Test
    public void testStress() throws Exception {
        Assume.assumeTrue("only with -DstressTest=true", Boolean.getBoolean("stressTest"));

        this.runWritersWithTakeover(
            Integer.getInteger("stress.jvms", 4),
            Integer.getInteger("stress.threads", 4),
            Integer.getInteger("stress.requests", 3000),
            Integer.getInteger("stress.maxFileLines", 100)
        );
    }
}
