package com.mapp.intelligence.tracking.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for everything that has to release resources before the JVM terminates.
 *
 * <p>An instance registers the resource it owns - for a tracking instance that is its request queue -
 * via {@link #registerResource(AutoCloseable)}. The registry holds the <b>resource</b> strongly and the
 * <b>instance</b> only weakly:</p>
 * <ul>
 *     <li>All instances share a <b>single</b> JVM shutdown hook that closes every resource still
 *     registered. One hook per instance would pin every instance to the JVM forever, an unbounded
 *     leak as soon as instances are created per HTTP request.</li>
 *     <li>An instance that the caller abandoned without calling {@code close()} can be garbage
 *     collected. Its resource is then closed by a single background thread - which sends the
 *     requests that are still queued instead of dropping them, and stops the resource's threads.</li>
 * </ul>
 *
 * <p>This only works as long as the resource does not reference its owner. A flush callback that
 * captures the tracking instance keeps it reachable; the resource is then closed by the shutdown hook
 * at the latest.</p>
 *
 * <p>Implementations of {@link #close()} and of the resource's {@code close()} must be idempotent: a
 * resource can be reached both by an explicit close and by the shutdown hook.</p>
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public abstract class AbstractMappIntelligenceCleaner implements Runnable {
    /**
     * Name of the single shutdown hook thread.
     */
    private static final String SHUTDOWN_HOOK_NAME = "mapp-intelligence-shutdown";
    /**
     * Name of the single thread that closes the resources of collected instances.
     */
    private static final String REAPER_NAME = "mapp-intelligence-cleaner";

    /**
     * Receives the references of instances that were garbage collected.
     */
    private static final ReferenceQueue<AbstractMappIntelligenceCleaner> COLLECTED = new ReferenceQueue<>();

    /**
     * Resources that still have to be closed, keyed by the weak reference to their owner. A reference
     * has identity equality, which is exactly what is needed here.
     */
    private static final Map<Reference<?>, AutoCloseable> RESOURCES = new ConcurrentHashMap<>();

    /**
     * Makes sure the shutdown hook and the reaper thread exist exactly once for the whole JVM.
     */
    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    /**
     * Weak reference of this instance in the registry, null as long as nothing is registered.
     */
    private volatile Reference<AbstractMappIntelligenceCleaner> registration;

    /**
     * Default constructor.
     */
    protected AbstractMappIntelligenceCleaner() {
        // do nothing
    }

    /**
     * Starts the shutdown hook and the reaper thread on first use.
     */
    private static void start() {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    closeAll();
                }
            }, SHUTDOWN_HOOK_NAME));
        } catch (IllegalStateException e) {
            // the JVM is already shutting down, there is nothing left to register
        }

        Thread reaper = new Thread(new Runnable() {
            @Override
            public void run() {
                reap();
            }
        }, REAPER_NAME);
        reaper.setDaemon(true);
        reaper.start();
    }

    /**
     * Closes the resource of every instance that was garbage collected without being closed.
     */
    private static void reap() {
        while (true) {
            Reference<?> collected;
            try {
                collected = COLLECTED.remove();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            closeQuietly(RESOURCES.remove(collected));
        }
    }

    /**
     * Closes every resource that is still registered. A single failing resource must not stop the
     * others from being closed.
     */
    private static void closeAll() {
        List<Reference<?>> registered = new ArrayList<>(RESOURCES.keySet());

        for (Reference<?> reference : registered) {
            closeQuietly(RESOURCES.remove(reference));
        }
    }

    /**
     * @param resource Resource to close, may be null
     */
    private static void closeQuietly(AutoCloseable resource) {
        if (resource == null) {
            return;
        }

        try {
            resource.close();
        } catch (Exception e) {
            // nothing sensible left to do, the owner is gone or the JVM is going down
        }
    }

    /**
     * Registers the resource of this instance, so it is closed when this instance is garbage collected
     * or the JVM terminates, whichever comes first. The resource must not reference this instance.
     *
     * @param resource Resource owned by this instance
     */
    protected final void registerResource(AutoCloseable resource) {
        Reference<AbstractMappIntelligenceCleaner> reference = new WeakReference<>(this, COLLECTED);
        RESOURCES.put(reference, resource);
        this.registration = reference;

        start();
    }

    /**
     * Removes the resource of this instance from the registry, after the instance has closed it.
     */
    protected final void unregisterResource() {
        Reference<AbstractMappIntelligenceCleaner> reference = this.registration;
        if (reference != null) {
            RESOURCES.remove(reference);
        }
    }

    /**
     * Sends all requests in the queue.
     */
    @Override
    public void run() {
        this.close();
    }

    /**
     * Releases all resources held by this instance. Must be idempotent.
     */
    protected abstract void close();
}
