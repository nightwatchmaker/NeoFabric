package org.neofabric.forge;

import net.minecraftforge.fml.common.Mod;
import org.neofabric.core.MinecraftTarget;
import org.neofabric.core.ModDiscovery;
import org.neofabric.core.NeoFabricLoader;

import java.nio.file.Path;

/** Forge 26.2 host adapter using Forge's real FML @Mod bootstrap contract. */
@Mod(NeoFabricForgeAdapter.MOD_ID)
public final class NeoFabricForgeAdapter {
    public static final String MOD_ID = "neofabric-loader";

    public NeoFabricForgeAdapter() {
        Path modsDirectory = Path.of(System.getProperty("user.dir", "."), "mods");
        NeoFabricLoader loader = new NeoFabricLoader(MinecraftTarget.MC_26_2);
        try {
            var decisions = loader.loadCatalog(modsDirectory);
            System.out.println("[NeoFabric] Forge host adapter initialized for Minecraft 26.2");
            decisions.forEach(decision -> System.out.println("[NeoFabric] " + decision.mod().id()
                    + " -> " + (decision.accepted() ? "accepted" : "rejected")
                    + " (" + decision.reason() + ")"));
        } catch (Exception error) {
            System.err.println("[NeoFabric] compatibility scan failed: " + error.getMessage());
        }
    }
}
