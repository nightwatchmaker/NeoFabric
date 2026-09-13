package org.neofabric.core;

/** Optional cancellation contract for events translated into the shared bus. */
public interface CancellableEvent {
    boolean isCanceled();

    void setCanceled(boolean canceled);
}
