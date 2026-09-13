package org.neofabric.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Minimal Fabric-style lifecycle callback surface backed by NeoFabric phases. */
public final class FabricLifecycleCallbacks {
    private static final CopyOnWriteArrayList<Consumer<Object>> SERVER_STARTED = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<Object>> SERVER_STOPPING = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<Object>> SERVER_STOPPED = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Consumer<Object>> CLIENT_STARTED = new CopyOnWriteArrayList<>();

    private FabricLifecycleCallbacks() {
    }

    public static void onServerStarted(Consumer<Object> callback) {
        SERVER_STARTED.add(callback);
    }

    public static void onServerStopping(Consumer<Object> callback) {
        SERVER_STOPPING.add(callback);
    }

    public static void onServerStopped(Consumer<Object> callback) {
        SERVER_STOPPED.add(callback);
    }

    public static void onClientStarted(Consumer<Object> callback) {
        CLIENT_STARTED.add(callback);
    }

    public static int serverStartedCount() {
        return SERVER_STARTED.size();
    }

    public static int clientStartedCount() {
        return CLIENT_STARTED.size();
    }

    public static void fireServerStarted(Object server) {
        SERVER_STARTED.forEach(callback -> callback.accept(server));
    }

    public static void fireServerStopping(Object server) {
        SERVER_STOPPING.forEach(callback -> callback.accept(server));
    }

    public static void fireServerStopped(Object server) {
        SERVER_STOPPED.forEach(callback -> callback.accept(server));
    }

    public static void fireClientStarted(Object client) {
        CLIENT_STARTED.forEach(callback -> callback.accept(client));
    }
}
