#!/usr/bin/env python3
"""
Git textconv filter that normalizes platform-specific terms to platform$.

Replaces Fabric/NeoForge/CoreMc identifiers with neutral placeholders so that
git diff only shows code changes.

Setup:
    git config diff.core-mc.textconv ./worldedit-core-mc/textconv-core-mc.py
"""
import re
import sys


def normalize(content: str) -> str:
    """Replace all platform-specific terms with placeholders."""
    # Package names -- longest (with .internal) first to avoid partial matches
    content = content.replace("com.sk89q.worldedit.fabric.internal", "com.sk89q.worldedit._platform_.internal")
    content = content.replace("com.sk89q.worldedit.neoforge.internal", "com.sk89q.worldedit._platform_.internal")
    content = content.replace("com.sk89q.worldedit.coremc.internal", "com.sk89q.worldedit._platform_.internal")
    content = content.replace("com.sk89q.worldedit.fabric", "com.sk89q.worldedit._platform_")
    content = content.replace("com.sk89q.worldedit.neoforge", "com.sk89q.worldedit._platform_")
    content = content.replace("com.sk89q.worldedit.coremc", "com.sk89q.worldedit._platform_")

    # PascalCase class/type prefixes: FabricPlayer -> platform$Player
    content = re.sub(r"\bFabric(?=[A-Z])", "platform$", content)
    content = re.sub(r"\bNeoForge(?=[A-Z])", "platform$", content)
    content = re.sub(r"\bCoreMc(?=[A-Z])", "platform$", content)

    # camelCase variable prefixes: fabricWorld -> platform$World
    content = re.sub(r"\bfabric(?=[A-Z])", "platform$", content)
    content = re.sub(r"\bneoForge(?=[A-Z])", "platform$", content)
    content = re.sub(r"\bcoreMc(?=[A-Z])", "platform$", content)

    return content


def main() -> int:
    if len(sys.argv) != 2:
        print(f"Usage: {sys.argv[0]} <file>", file=sys.stderr)
        return 2

    with open(sys.argv[1]) as f:
        content = f.read()

    sys.stdout.write(normalize(content))
    return 0


if __name__ == "__main__":
    sys.exit(main())
