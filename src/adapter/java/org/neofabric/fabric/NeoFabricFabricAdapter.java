package org.neofabric.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.neofabric.core.CompatibilityCoordinator;
import org.neofabric.core.MinecraftTarget;
import org.neofabric.core.ModDiscovery;
import org.neofabric.core.NeoFabricRuntime;

import java.nio.file.Path;

/** Fabric Loader 0.19.5+ bootstrap adapter for the Minecraft 26.2 target. */
public final class NeoFabricFabricAdapter implements ModInitializer {
    @Override
    public void onInitialize() {
        Path modsDirectory = FabricLoader.getInstance().getGameDir().resolve("mods");
        NeoFabricRuntime runtime = new NeoFabricRuntime();
        try {
            ModDiscovery.scanDirectory(modsDirectory).forEach(runtime::addMod);
            var decisions = new CompatibilityCoordinator(MinecraftTarget.MC_26_2).decide(runtime.mods());
            System.out.println("[NeoFabric] Fabric host adapter initialized for Minecraft 26.2");
            decisions.forEach(decision -> System.out.println("[NeoFabric] " + decision.mod().id()
                    + " -> " + (decision.accepted() ? "accepted" : "rejected")
                    + " (" + decision.reason() + ")"));
        } catch (Exception error) {
            System.err.println("[NeoFabric] compatibility scan failed: " + error.getMessage());
        }
    }
}
