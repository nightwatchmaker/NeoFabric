package org.neofabric.launcher;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;

import org.neofabric.core.BytecodeTransformPipeline;
import org.neofabric.core.CompatibilityClassLoaderRegistry;
import org.neofabric.core.FabricEntrypointBridge;
import org.neofabric.core.FabricMetadataAdapter;
import org.neofabric.core.FabricMixinConfigDiscovery;
import org.neofabric.core.LoaderKind;
import org.neofabric.core.MinecraftTarget;
import org.neofabric.core.NeoFabricLoader;

/** Official-launcher entrypoint that delegates to Minecraft after loader setup. */
public final class NeoFabricLauncher {
    private static CompatibilityClassLoaderRegistry activeModLoaders;

    private NeoFabricLauncher() {
    }

    public static void main(String[] args) throws Exception {
        initializeCore(Path.of(System.getProperty("user.dir")));
        String target = System.getProperty("neofabric.targetMainClass", "net.minecraft.client.main.Main");
        launch(target, args);
    }

    public static NeoFabricLoader initializeCore(Path gameDirectory) throws java.io.IOException {
        NeoFabricLoader loader = new NeoFabricLoader(MinecraftTarget.MC_26_2);
        int externalBackends = loader.discoverBackends(NeoFabricLauncher.class.getClassLoader());
        var decisions = loader.loadCatalog(gameDirectory.resolve("mods"));
        activeModLoaders = new CompatibilityClassLoaderRegistry(NeoFabricLauncher.class.getClassLoader());
        activeModLoaders.prepare(decisions);
        String distribution = System.getProperty("neofabric.distribution", "CLIENT");
        for (var decision : decisions) {
            if (!decision.accepted() || decision.mod().loader() != LoaderKind.FABRIC) continue;
            Path modJar = Path.of(decision.mod().source());
            var metadata = FabricMetadataAdapter.read(modJar);
            var mixinConfigs = FabricMixinConfigDiscovery.validate(modJar, metadata);
            var modLoader = activeModLoaders.get(decision.mod().id());
            var initialized = FabricEntrypointBridge.initialize(metadata, modLoader, distribution);
            var registries = FabricEntrypointBridge.initializeRegistries(metadata, modLoader,
                    loader.registries(), loader.registryAdapter(LoaderKind.FABRIC));
            System.out.println("[NeoFabric] Fabric mod " + decision.mod().id() + " initialized: "
                    + initialized.size() + " entrypoints, " + registries.size() + " registry entrypoints, "
                    + mixinConfigs.size() + " validated Mixin configs");
        }
        System.out.println("[NeoFabric] external compatibility backends discovered: " + externalBackends);
        System.out.println("[NeoFabric] official-launcher bootstrap initialized for Minecraft 26.2 with "
                + activeModLoaders.size() + " accepted mod classloaders");
        return loader;
    }

    public static void launch(String targetMainClass, String[] args) throws Exception {
        URL[] runtimeUrls = Arrays.stream(System.getProperty("java.class.path", "").split(java.io.File.pathSeparator))
                .filter(entry -> !entry.isBlank())
                .map(Path::of)
                .filter(path -> java.nio.file.Files.exists(path))
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (java.net.MalformedURLException error) {
                        throw new IllegalArgumentException("Invalid runtime classpath entry: " + path, error);
                    }
                })
                .toArray(URL[]::new);
        BytecodeTransformPipeline transforms = new BytecodeTransformPipeline();
        try (var gameLoader = new NeoFabricGameClassLoader(runtimeUrls, NeoFabricLauncher.class.getClassLoader(), activeModLoaders, transforms)) {
            Class<?> target = Class.forName(targetMainClass, true, gameLoader);
            Method main = target.getMethod("main", String[].class);
            System.out.println("[NeoFabric] delegating official-launcher bootstrap to " + targetMainClass
                    + " through isolated game classloader with " + args.length + " arguments");
            main.invoke(null, (Object) Arrays.copyOf(args, args.length));
        }
    }
}
