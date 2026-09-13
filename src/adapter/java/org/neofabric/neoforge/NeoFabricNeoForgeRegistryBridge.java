package org.neofabric.neoforge;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.neofabric.core.NeoFabricLoader;
import org.neofabric.core.NeoRegistry;

/** NeoForge API-specific registry bridge kept outside dependency-free NeoFabric Core. */
public final class NeoFabricNeoForgeRegistryBridge {
    private NeoFabricNeoForgeRegistryBridge() {
    }

    public static void registerMatching(RegisterEvent event, NeoFabricLoader loader) {
        String registryId = event.getRegistryKey().identifier().toString();
        NeoRegistry<?> shared = loader.registries().get(registryId);
        if (shared == null) return;
        registerEntries(event, shared);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void registerEntries(RegisterEvent event, NeoRegistry<?> shared) {
        for (Map.Entry<String, ?> entry : shared.entries().entrySet()) {
            event.register((net.minecraft.resources.ResourceKey) event.getRegistryKey(),
                    Identifier.parse(entry.getKey()), (java.util.function.Supplier) () -> entry.getValue());
        }
    }
}
