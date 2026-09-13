package org.neofabric.core;

import java.nio.file.Files;
import java.nio.file.Path;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        NeoFabricRuntime runtime = new NeoFabricRuntime();
        runtime.events().register(String.class, message -> System.out.println("[event] " + message));
        Path mods = args.length == 0 ? Path.of("examples/mods") : Path.of(args[0]);
        NeoFabricLoader loader = new NeoFabricLoader(MinecraftTarget.MC_26_2);
        if (Files.isDirectory(mods)) {
            loader.loadCatalog(mods).forEach(decision -> System.out.println(
                    "[NeoFabric] " + decision.mod().id() + ": "
                            + (decision.accepted() ? "accepted" : "rejected") + " - " + decision.reason()));
        }
        System.out.println("Target profile: Minecraft " + loader.target().runtimeVersion()
                + " (development coordinate " + loader.target().developmentCoordinate()
                + ", Java " + loader.target().javaRelease() + "+)");
        runtime.events().post("NeoFabric core initialized");
    }
}
