package org.neofabric.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deterministic compatibility coordinator. It deliberately does not load mod
 * classes; a future 26.2 bootstrap adapter consumes these decisions.
 */
public final class CompatibilityCoordinator {
    private final MinecraftTarget target;

    public CompatibilityCoordinator(MinecraftTarget target) {
        this.target = target;
    }

    public List<CompatibilityDecision> decide(List<ModDescriptor> discovered) {
        Map<String, List<ModDescriptor>> byId = new HashMap<>();
        discovered.forEach(mod -> byId.computeIfAbsent(mod.id(), ignored -> new ArrayList<>()).add(mod));
        List<CompatibilityDecision> decisions = new ArrayList<>();
        byId.values().forEach(group -> {
            group.sort(Comparator.comparing(ModDescriptor::source));
            boolean duplicate = group.size() > 1;
            for (ModDescriptor mod : group) {
                if (duplicate) {
                    decisions.add(new CompatibilityDecision(mod, false,
                            "duplicate mod id '" + mod.id() + "' in the 26.2 catalog"));
                } else if (mod.loader() == LoaderKind.UNKNOWN) {
                    decisions.add(new CompatibilityDecision(mod, false,
                            "no Fabric, NeoForge, or Forge metadata was found"));
                } else {
                    decisions.add(new CompatibilityDecision(mod, true,
                            adapterName(mod.loader()) + " adapter selected for Minecraft " + target.runtimeVersion()));
                }
            }
        });
        return decisions.stream().sorted(Comparator.comparing(decision -> decision.mod().id())).toList();
    }

    private static String adapterName(LoaderKind loader) {
        return switch (loader) {
            case FABRIC -> "Fabric";
            case NEOFORGE -> "NeoForge";
            case FORGE -> "Forge";
            case UNKNOWN -> "Unknown";
        };
    }
}
