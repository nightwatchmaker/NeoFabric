package org.neofabric.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Thread-safe callback registry used by Fabric lifecycle compatibility shims. */
public final class LifecycleCallbackRegistry {
    private final Map<LifecyclePhase, CopyOnWriteArrayList<Consumer<Object>>> callbacks = new EnumMap<>(LifecyclePhase.class);

    public LifecycleCallbackRegistry() {
        for (LifecyclePhase phase : LifecyclePhase.values()) callbacks.put(phase, new CopyOnWriteArrayList<>());
    }

    public void register(LifecyclePhase phase, Consumer<Object> callback) {
        callbacks.get(phase).add(callback);
    }

    public void fire(LifecyclePhase phase, Object context) {
        callbacks.get(phase).forEach(callback -> callback.accept(context));
    }

    public int callbackCount(LifecyclePhase phase) {
        return callbacks.get(phase).size();
    }
}
