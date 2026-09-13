package org.neofabric.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Canonical lifecycle dispatcher shared by all compatibility backends. */
public final class LifecycleDispatcher {
    private final Map<LifecyclePhase, CopyOnWriteArrayList<Consumer<LifecyclePhase>>> listeners = new EnumMap<>(LifecyclePhase.class);

    public LifecycleDispatcher() {
        for (LifecyclePhase phase : LifecyclePhase.values()) listeners.put(phase, new CopyOnWriteArrayList<>());
    }

    public void register(LifecyclePhase phase, Consumer<LifecyclePhase> listener) {
        listeners.get(phase).add(listener);
    }

    public void fire(LifecyclePhase phase) {
        listeners.get(phase).forEach(listener -> listener.accept(phase));
    }
}
