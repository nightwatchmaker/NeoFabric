package org.neofabric.core;

public final class NeoFabricRuntimeTest {
    public static final class BootstrapTarget {
        public static void main(String[] args) {
            if (args.length != 1 || !"forwarded".equals(args[0])) throw new AssertionError("bootstrap args were not forwarded");
            System.setProperty("neofabric.bootstrap", "ok");
        }
    }

    public static final class NoArgMod {
        static boolean constructed;
        public NoArgMod() { constructed = true; }
    }

    public static final class BusMod {
        static Object received;
        public BusMod(Object eventBus) { received = eventBus; }
    }

    public static void main(String[] args) throws Exception {
        NeoFabricRuntime runtime = new NeoFabricRuntime();
        int[] calls = {0};
        int[] interfaceCalls = {0};
        runtime.events().register(String.class, ignored -> calls[0]++);
        runtime.events().register(CharSequence.class, ignored -> interfaceCalls[0]++);
        runtime.addMod(new ModDescriptor("fabric-example", "1.0.0", LoaderKind.FABRIC, "test"));
        runtime.addMod(new ModDescriptor("forge-example", "1.0.0", LoaderKind.FORGE, "test"));
        runtime.events().post("hello");
        assert calls[0] == 1 : "event was not dispatched";
        assert interfaceCalls[0] == 1 : "assignable event listener was not dispatched";
        var ordered = new java.util.ArrayList<String>();
        runtime.events().register(String.class, EventPriority.LOW, ignored -> ordered.add("low"));
        runtime.events().register(String.class, EventPriority.HIGHEST, ignored -> ordered.add("high"));
        runtime.events().post("priority");
        assert ordered.equals(java.util.List.of("high", "low")) : "event priorities were not ordered";
        var canceled = new CancellableEvent() {
            private boolean value;
            public boolean isCanceled() { return value; }
            public void setCanceled(boolean canceled) { value = canceled; }
        };
        var cancellationOrder = new java.util.ArrayList<String>();
        runtime.events().register(CancellableEvent.class, EventPriority.HIGHEST, event -> {
            cancellationOrder.add("cancel");
            event.setCanceled(true);
        });
        runtime.events().register(CancellableEvent.class, EventPriority.LOWEST, event -> cancellationOrder.add("late"));
        runtime.events().post(canceled);
        assert cancellationOrder.equals(java.util.List.of("cancel")) : "cancellation did not stop dispatch";
        assert runtime.compatibilityReport().contains("Fabric=1, NeoForge=0, Forge=1") : "bad report";
        ForgeModInfo forgeInfo = ForgeMetadataAdapter.read(java.nio.file.Path.of("examples/mods/forge-example.jar"));
        assert forgeInfo.modIds().equals(java.util.List.of("forge-example"));
        assert forgeInfo.versions().equals(java.util.List.of("1.0.0"));
        ForgeModInfo neoForgeInfo = ForgeMetadataAdapter.read(java.nio.file.Path.of("examples/mods/neoforge-example.jar"));
        assert neoForgeInfo.modIds().equals(java.util.List.of("neoforge-example"));
        assert MinecraftTarget.MC_26_2.runtimeVersion().equals("26.2") : "wrong target profile";
        var dependencyResolver = new ModDependencyResolver();
        var dependencyGraph = new java.util.LinkedHashMap<String, java.util.List<ModDependency>>();
        dependencyGraph.put("base", java.util.List.of());
        dependencyGraph.put("addon", java.util.List.of(new ModDependency("base", "1.0.0")));
        assert dependencyResolver.resolve(dependencyGraph).equals(java.util.List.of("base", "addon"));
        Object bus = new Object();
        ModConstructorBridge.construct(NoArgMod.class, bus);
        assert NoArgMod.constructed : "no-argument mod constructor was not invoked";
        BusMod.received = null;
        ModConstructorBridge.construct(BusMod.class, bus);
        assert BusMod.received == bus : "event-bus mod constructor was not invoked";
        try {
            dependencyResolver.resolve(java.util.Map.of("addon", java.util.List.of(new ModDependency("missing", "1"))));
            throw new AssertionError("missing dependency accepted");
        } catch (IllegalArgumentException expected) {
        }
        var coordinator = new CompatibilityCoordinator(MinecraftTarget.MC_26_2);
        var decisions = coordinator.decide(java.util.List.of(
                new ModDescriptor("duplicate", "1", LoaderKind.FABRIC, "a.jar"),
                new ModDescriptor("duplicate", "1", LoaderKind.FORGE, "b.jar"),
                new ModDescriptor("unknown", "1", LoaderKind.UNKNOWN, "c.jar")));
        assert decisions.size() == 3 : "all decisions must be retained";
        assert decisions.stream().noneMatch(CompatibilityDecision::accepted) : "invalid catalog entry accepted";
        var loader = new NeoFabricLoader(MinecraftTarget.MC_26_2);
        assert loader.discoverBackends(NeoFabricRuntimeTest.class.getClassLoader()) == 0 : "unexpected test backend provider";
        var lifecycleEvents = new java.util.ArrayList<LifecyclePhase>();
        loader.lifecycle().register(LifecyclePhase.LOADER_READY, lifecycleEvents::add);
        loader.lifecycle().register(LifecyclePhase.MODS_DISCOVERED, lifecycleEvents::add);
        var callbackContexts = new java.util.ArrayList<>();
        loader.callbacks().register(LifecyclePhase.LOADER_READY, callbackContexts::add);
        loader.initialize();
        loader.initialize();
        assert lifecycleEvents.equals(java.util.List.of(LifecyclePhase.LOADER_READY)) : "loader initialization was not idempotent";
        assert callbackContexts.size() == 1 && callbackContexts.get(0) == loader : "callback context was not delivered";
        var constructed = new java.util.ArrayList<>();
        loader.callbacks().register(LifecyclePhase.MODS_CONSTRUCTED, constructed::add);
        loader.firePhase(LifecyclePhase.MODS_CONSTRUCTED, "constructed");
        assert constructed.equals(java.util.List.of("constructed")) : "constructed phase was not dispatched";
        var setupEvents = new java.util.ArrayList<LifecyclePhase>();
        loader.callbacks().register(LifecyclePhase.COMMON_SETUP, ignored -> setupEvents.add(LifecyclePhase.COMMON_SETUP));
        loader.callbacks().register(LifecyclePhase.CLIENT_READY, ignored -> setupEvents.add(LifecyclePhase.CLIENT_READY));
        loader.callbacks().register(LifecyclePhase.SERVER_READY, ignored -> setupEvents.add(LifecyclePhase.SERVER_READY));
        loader.firePhase(LifecyclePhase.COMMON_SETUP, "common");
        loader.firePhase(LifecyclePhase.CLIENT_READY, "client");
        loader.firePhase(LifecyclePhase.SERVER_READY, "server");
        assert setupEvents.equals(java.util.List.of(LifecyclePhase.COMMON_SETUP, LifecyclePhase.CLIENT_READY, LifecyclePhase.SERVER_READY)) : "setup phases were not dispatched";
        var fabricCallbacks = new java.util.ArrayList<>();
        FabricLifecycleCallbacks.onServerStarted(fabricCallbacks::add);
        FabricLifecycleCallbacks.onClientStarted(fabricCallbacks::add);
        Object serverContext = new Object();
        Object clientContext = new Object();
        FabricLifecycleCallbacks.fireServerStarted(serverContext);
        FabricLifecycleCallbacks.fireClientStarted(clientContext);
        assert fabricCallbacks.equals(java.util.List.of(serverContext, clientContext)) : "Fabric callback API did not preserve contexts";
        var shutdownCallbacks = new java.util.ArrayList<>();
        FabricLifecycleCallbacks.onServerStopping(ignored -> shutdownCallbacks.add("stopping"));
        FabricLifecycleCallbacks.onServerStopped(ignored -> shutdownCallbacks.add("stopped"));
        FabricLifecycleCallbacks.fireServerStopping(serverContext);
        FabricLifecycleCallbacks.fireServerStopped(serverContext);
        assert shutdownCallbacks.equals(java.util.List.of("stopping", "stopped")) : "shutdown callbacks were not ordered";
        var registry = new NeoRegistry<String>("test:items");
        var registrySources = new java.util.ArrayList<LoaderKind>();
        registry.onRegistered((id, value, source) -> registrySources.add(source));
        registry.register("first", "A");
        registry.register("second", "B");
        assert registrySources.equals(java.util.List.of(LoaderKind.UNKNOWN, LoaderKind.UNKNOWN)) : "direct registry callbacks missing";
        assert registry.values().equals(java.util.List.of("A", "B")) : "registry order was not deterministic";
        try {
            registry.register("first", "duplicate");
            throw new AssertionError("duplicate registry entry was accepted");
        } catch (IllegalArgumentException expected) {
        }
        registry.freeze();
        try {
            registry.register("third", "C");
            throw new AssertionError("frozen registry accepted a new entry");
        } catch (IllegalStateException expected) {
        }
        var catalog = loader.registries();
        catalog.create("test:items");
        try {
            catalog.create("test:items");
            throw new AssertionError("duplicate registry was accepted");
        } catch (IllegalArgumentException expected) {
        }
        var sharedRegistry = loader.registries().create("test:blocks");
        for (LoaderKind source : LoaderKind.values()) {
            loader.registryAdapter(source).register(sharedRegistry, "test:" + source.name().toLowerCase(), source);
            assert loader.registryAdapter(source).source() == source : "registry adapter source was not preserved";
        }
        try {
            loader.registryAdapter(LoaderKind.FABRIC).register(sharedRegistry, "not-namespaced", "bad");
            throw new AssertionError("invalid registry ID was accepted");
        } catch (IllegalArgumentException expected) {
        }
        assert loader.translate(new ModDescriptor("f", "1", LoaderKind.FABRIC, "f")).accepted();
        assert loader.translate(new ModDescriptor("n", "1", LoaderKind.NEOFORGE, "n")).accepted();
        assert loader.translate(new ModDescriptor("g", "1", LoaderKind.FORGE, "g")).accepted();
        var fabricInfo = FabricMetadataAdapter.read(java.nio.file.Path.of("examples/mods/fabric-example.jar"));
        assert fabricInfo.id().equals("fabric-example") : "Fabric metadata id was not read";
        assert fabricInfo.version().equals("1.0.0") : "Fabric metadata version was not read";
        java.nio.file.Path bootstrapGame = java.nio.file.Files.createTempDirectory("neofabric-bootstrap-game");
        java.nio.file.Files.createDirectories(bootstrapGame.resolve("mods"));
        assert org.neofabric.launcher.NeoFabricLauncher.initializeCore(bootstrapGame) != null;
        org.neofabric.launcher.NeoFabricLauncher.launch(BootstrapTarget.class.getName(), new String[]{"forwarded"});
        assert "ok".equals(System.getProperty("neofabric.bootstrap"));
        System.out.println("NeoFabricRuntimeTest: PASS");
    }
}
