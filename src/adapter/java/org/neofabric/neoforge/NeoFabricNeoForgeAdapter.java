package org.neofabric.neoforge;

import java.nio.file.Path;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.neofabric.core.CompatibilityCoordinator;
import org.neofabric.core.MinecraftTarget;
import org.neofabric.core.ModDiscovery;
import org.neofabric.core.NeoFabricLoader;

/** NeoForge 26.2 host adapter using the real FML @Mod bootstrap contract. */
@Mod("neofabric-loader")
public final class NeoFabricNeoForgeAdapter {
    public NeoFabricNeoForgeAdapter(IEventBus modBus) {
        Path modsDirectory = Path.of(System.getProperty("user.dir", "."), "mods");
        NeoFabricLoader loader = new NeoFabricLoader(MinecraftTarget.MC_26_2);
        try {
            var mods = ModDiscovery.scanDirectory(modsDirectory);
            var decisions = mods.stream().map(loader::translate).toList();
            modBus.addListener(net.neoforged.neoforge.registries.RegisterEvent.class,
                    event -> NeoFabricNeoForgeRegistryBridge.registerMatching(event, loader));
            System.out.println("[NeoFabric] NeoForge compatibility adapter initialized for Minecraft 26.2");
            decisions.forEach(decision -> System.out.println("[NeoFabric] " + decision.mod().id()
                    + " -> " + (decision.accepted() ? "accepted" : "rejected")
                    + " (" + decision.reason() + ")"));
        } catch (Exception error) {
            System.err.println("[NeoFabric] compatibility scan failed: " + error.getMessage());
        }
    }
}
