# Diff Setup

Diffing against old `worldedit-fabric` or `worldedit-neoforge` code is best done with the following setup:
1. Add the following to `.git/info/attributes`:
    ```
    worldedit-core-mc/src/main/java/com/sk89q/worldedit/coremc/**/*.java diff=core-mc
    worldedit-fabric/src/main/java/com/sk89q/worldedit/fabric/**/*.java diff=core-mc
    worldedit-neoforge/src/main/java/com/sk89q/worldedit/neoforge/**/*.java diff=core-mc
    ```
2. Run `git config diff.core-mc.textconv ./worldedit-core-mc/textconv-core-mc.py`.
3. Run `git config diff.renames copy`

This will give you diffs that replace platform-specific names with `platform$`,
providing a clearer picture of the core logic changes without noise from the renames.

Make sure to remove the `.gitattributes` changes after you're done to avoid affecting future diffs.
