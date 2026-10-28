package com.mapp.intelligence.tracking.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Exclusive lock for the request logging files of one file path and file prefix, shared by every process
 * that writes into them (several JVMs, the cron job).
 *
 * <p>The lock is taken on a separate lock file ({@code <prefix>.lock}) instead of the data file: a POSIX
 * lock is released as soon as any descriptor of the locked file is closed in the process, and the data
 * file is opened and closed for reading as well.</p>
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public final class MappIntelligenceFileLock implements AutoCloseable {
    /**
     * Extension of the lock file.
     */
    public static final String LOCK_FILE_EXTENSION = ".lock";
    /**
     * Pause between two attempts if the lock is held by another channel of the same JVM.
     */
    private static final long OVERLAPPING_LOCK_PAUSE = 5;
    /**
     * Maximum number of attempts if the lock is held by another channel of the same JVM (about 30 seconds).
     */
    private static final int MAX_OVERLAPPING_LOCK_ATTEMPTS = 6000;

    /**
     * Channel of the lock file.
     */
    private final FileChannel channel;
    /**
     * Current lock, null if not locked.
     */
    private FileLock lock;

    /**
     * @param directory Directory of the request logging files
     * @param filePrefix File prefix of the request logging files
     *
     * @throws IOException The lock file cannot be opened
     */
    public MappIntelligenceFileLock(Path directory, String filePrefix) throws IOException {
        this.channel = FileChannel.open(
            directory.resolve(filePrefix + LOCK_FILE_EXTENSION),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE
        );
    }

    /**
     * Waits until the lock is acquired.
     *
     * @throws IOException The lock cannot be acquired
     */
    public void lock() throws IOException {
        if (this.lock != null) {
            return;
        }

        for (int attempt = 0; ; attempt++) {
            try {
                this.lock = this.channel.lock();
                return;
            } catch (OverlappingFileLockException e) {
                // another channel of this JVM (e.g. another class loader) holds the lock
                if (attempt >= MAX_OVERLAPPING_LOCK_ATTEMPTS) {
                    throw new IOException("Cannot acquire the lock, it is held by this JVM", e);
                }

                pause();
            }
        }
    }

    /**
     * Releases the lock, if it is held.
     */
    public void unlock() {
        if (this.lock != null) {
            try {
                this.lock.release();
            } catch (IOException e) {
                // the lock is released with the channel at the latest
            }

            this.lock = null;
        }
    }

    /**
     * Releases the lock and closes the lock file.
     */
    @Override
    public void close() {
        this.unlock();

        try {
            this.channel.close();
        } catch (IOException e) {
            // nothing to do
        }
    }

    /**
     * Waits a moment before the next attempt.
     *
     * @throws IOException The thread was interrupted
     */
    private static void pause() throws IOException {
        try {
            Thread.sleep(OVERLAPPING_LOCK_PAUSE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for the lock", e);
        }
    }
}
