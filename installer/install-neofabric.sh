#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ARTIFACT="$ROOT/build/libs/neofabric-loader-3.8.0-dev.jar"
MC_DIR="${MINECRAFT_DIR:-$HOME/.minecraft}"
INSTALL_DIR="$MC_DIR/neofabric"

usage() {
    printf 'Usage: %s [--minecraft-dir DIR] [--artifact JAR]\n' "$0"
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --minecraft-dir) MC_DIR="$2"; INSTALL_DIR="$MC_DIR/neofabric"; shift 2 ;;
        --artifact) ARTIFACT="$2"; shift 2 ;;
        -h|--help) usage; exit 0 ;;
        *) printf 'Unknown option: %s\n' "$1" >&2; usage >&2; exit 2 ;;
    esac
done

[[ -f "$ARTIFACT" ]] || { printf 'Artifact not found: %s\nBuild NeoFabric first.\n' "$ARTIFACT" >&2; exit 1; }
command -v unzip >/dev/null || { printf 'unzip is required\n' >&2; exit 1; }
unzip -tq "$ARTIFACT"
mkdir -p "$INSTALL_DIR"
install -m 0644 "$ARTIFACT" "$INSTALL_DIR/neofabric-loader-dev.jar"
cat > "$INSTALL_DIR/INSTALL-MANIFEST.txt" <<EOF
NeoFabric development installation
Loader artifact: neofabric-loader-dev.jar
Source artifact: $ARTIFACT

This is a loader staging install. It does not yet create an official Minecraft
Launcher version profile because NeoFabric still needs its version JSON,
Minecraft libraries, mappings, and launcher bootstrap integration.
EOF
printf 'Installed NeoFabric staging artifact to %s\n' "$INSTALL_DIR"
