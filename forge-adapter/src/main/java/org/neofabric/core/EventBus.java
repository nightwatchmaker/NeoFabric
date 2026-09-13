package org.neofabric.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Small thread-safe event hub shared by compatibility adapters. */
public final class EventBus {
    private final Map<Class<?>, CopyOnWriteArrayList<Consumer<Object>>> handlers = new ConcurrentHashMap<>();

    public <T> void register(Class<T> eventType, Consumer<T> handler) {
        handlers.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>())
                .add(event -> handler.accept(eventType.cast(event)));
    }

    public void post(Object event) {
        List<Consumer<Object>> listeners = handlers.get(event.getClass());
        if (listeners != null) {
            listeners.forEach(listener -> listener.accept(event));
        }
    }
}
