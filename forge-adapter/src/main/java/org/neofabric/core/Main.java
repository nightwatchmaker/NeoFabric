package org.neofabric.core;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        NeoFabricRuntime runtime = new NeoFabricRuntime();
        runtime.events().register(String.class, message -> System.out.println("[event] " + message));
        Path mods = args.length == 0 ? Path.of("examples/mods") : Path.of(args[0]);
        if (Files.isDirectory(mods)) {
            ModDiscovery.scanDirectory(mods).forEach(runtime::addMod);
        }
        System.out.println("Target profile: Minecraft " + MinecraftTarget.MC_26_2.runtimeVersion()
                + " (development coordinate " + MinecraftTarget.MC_26_2.developmentCoordinate()
                + ", Java " + MinecraftTarget.MC_26_2.javaRelease() + "+)");
        System.out.println(runtime.compatibilityReport());
        runtime.mods().forEach(System.out::println);
        CompatibilityCoordinator coordinator = new CompatibilityCoordinator(MinecraftTarget.MC_26_2);
        coordinator.decide(runtime.mods()).forEach(decision -> System.out.println(
                "[compatibility] " + decision.mod().id() + ": "
                        + (decision.accepted() ? "accepted" : "rejected") + " - " + decision.reason()));
        runtime.events().post("NeoFabric core initialized");
    }
}
