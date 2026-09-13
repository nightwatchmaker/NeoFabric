package org.neofabric.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class CompatibilityClassLoaderTest {
    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("neofabric-classloader-test");
        Path source = root.resolve("ModEntry.java");
        Files.writeString(source, "package fixture; public final class ModEntry { public static String id() { return \"fixture\"; } }");
        var compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null || compiler.run(null, null, null, "-d", root.toString(), source.toString()) != 0) {
            throw new AssertionError("fixture compilation failed");
        }
        Path jar = root.resolve("fixture.jar");
        try (var output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("fixture/ModEntry.class"));
            output.write(Files.readAllBytes(root.resolve("fixture/ModEntry.class")));
            output.closeEntry();
        }
        var registry = new CompatibilityClassLoaderRegistry(CompatibilityClassLoaderTest.class.getClassLoader());
        registry.prepare(java.util.List.of(new CompatibilityDecision(
                new ModDescriptor("fixture", "1", LoaderKind.FABRIC, jar.toString()), true, "test")));
        assert registry.loadModClass("fixture.ModEntry") != null : "registry did not resolve mod class";
        registry.close();
        try (var loader = new CompatibilityClassLoader(jar, CompatibilityClassLoaderTest.class.getClassLoader())) {
            Class<?> mod = loader.loadClass("fixture.ModEntry");
            Class<?> core = loader.loadClass("org.neofabric.core.NeoFabricLoader");
            assert mod.getClassLoader() == loader : "mod implementation was not isolated";
            assert core.getClassLoader() != loader : "shared NeoFabric API was not parent-first";
            assert mod.getMethod("id").invoke(null).equals("fixture");
        }
        int[] transformations = {0};
        var pipeline = new BytecodeTransformPipeline();
        pipeline.add((name, bytes) -> {
            transformations[0]++;
            return bytes;
        });
        try (var gameLoader = new org.neofabric.launcher.NeoFabricGameClassLoader(
                new java.net.URL[]{root.toUri().toURL()}, CompatibilityClassLoaderTest.class.getClassLoader(), null, pipeline)) {
            gameLoader.loadClass("fixture.ModEntry");
        }
        assert transformations[0] == 1 : "game classloader did not apply bytecode pipeline";
        System.out.println("CompatibilityClassLoaderTest: PASS");
    }
}
