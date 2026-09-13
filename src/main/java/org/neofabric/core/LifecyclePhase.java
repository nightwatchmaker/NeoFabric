package org.neofabric.core;

public enum LifecyclePhase {
    LOADER_READY,
    MODS_DISCOVERED,
    MODS_CONSTRUCTED,
    COMMON_SETUP,
    CLIENT_READY,
    SERVER_READY,
    SERVER_STOPPING,
    SERVER_STOPPED
}
