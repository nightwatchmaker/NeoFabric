#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD="$ROOT/build"
rm -rf "$BUILD"
mkdir -p "$BUILD/classes" "$BUILD/adapter-classes" "$BUILD/neoforge-classes" "$BUILD/forge-classes" "$BUILD/test-classes" "$BUILD/libs"
find "$ROOT/src/main/java" -name '*.java' -print0 | xargs -0 javac --release 17 -d "$BUILD/classes"
find "$ROOT/src/adapter/java" -name 'NeoFabricFabricAdapter.java' -print0 | xargs -0 javac --release 25 -cp "$BUILD/classes:$ROOT/vendor/fabric-loader-0.19.5.jar" -d "$BUILD/adapter-classes"
find "$ROOT/src/adapter/java" -path '*/neoforge/*.java' -print0 | xargs -0 javac --release 25 -cp "$BUILD/classes:$ROOT/vendor/fml-loader-11.0.16.jar:$ROOT/vendor/neoforge-26.2.0.86.jar:/root/.gradle/caches/modules-2/files-2.1/net.neoforged/bus/8.0.5/5b2d33285ab5d1554e9798ad98c40d6ea3868bd5/bus-8.0.5.jar:/root/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.2/minecraft-merged-deobf-26.2.jar" -d "$BUILD/neoforge-classes"
find "$ROOT/src/test/java" -name '*.java' -print0 | xargs -0 javac --release 17 -cp "$BUILD/classes" -d "$BUILD/test-classes"
java -ea -cp "$BUILD/classes:$BUILD/test-classes" org.neofabric.core.NeoFabricRuntimeTest
java -ea -cp "$BUILD/classes:$BUILD/test-classes" org.neofabric.core.CompatibilityClassLoaderTest
java -ea -cp "$BUILD/classes:$BUILD/test-classes" org.neofabric.core.FabricEntrypointBridgeTest
jar --create --file "$BUILD/libs/neofabric-core-0.2.0-dev.jar" -C "$BUILD/classes" .
if [[ -f "$ROOT/forge-adapter/build/libs/examplemod-0.3.0-dev.jar" ]]; then
    (cd "$BUILD/forge-classes" && jar xf "$ROOT/forge-adapter/build/libs/examplemod-0.3.0-dev.jar" org/neofabric/forge/NeoFabricForgeAdapter.class)
else
    echo "Forge adapter build missing; run forge-adapter/gradlew build first" >&2
    exit 1
fi
cp -f "$ROOT/vendor/fancymodloader-src/loader/build/libs/loader-11.0.16-neofabric.1.jar" "$BUILD/libs/neofabric-loader-3.8.0-dev.jar"
jar uf "$BUILD/libs/neofabric-loader-3.8.0-dev.jar" \
    -C "$BUILD/classes" . \
    -C "$BUILD/adapter-classes" . \
    -C "$BUILD/neoforge-classes" . \
    -C "$BUILD/forge-classes" . \
    -C "$ROOT/src/adapter/resources" fabric.mod.json \
    -C "$ROOT/src/adapter/resources" META-INF/neoforge.mods.toml \
    -C "$ROOT/forge-adapter/src/main/resources" META-INF/mods.toml
chmod 777 "$BUILD/libs/neofabric-loader-3.8.0-dev.jar"
echo "Built $BUILD/libs/neofabric-loader-3.8.0-dev.jar"
java -cp "$BUILD/classes" org.neofabric.core.Main "$ROOT/examples/mods"
