/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ItemID;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;

/**
 * Represents WorldEdit's configuration.
 *
 * @author sk89q
 */
public abstract class LocalConfiguration {
    protected static final int[] defaultDisallowedBlocks = new int[] {
                // dangerous stuff (physics/drops items)
                BlockID.SAPLING,
                BlockID.BED,
                BlockID.POWERED_RAIL,
                BlockID.DETECTOR_RAIL,
                BlockID.LONG_GRASS,
                BlockID.DEAD_BUSH,
                BlockID.PISTON_EXTENSION,
                BlockID.PISTON_MOVING_PIECE,
                BlockID.YELLOW_FLOWER,
                BlockID.RED_FLOWER,
                BlockID.BROWN_MUSHROOM,
                BlockID.RED_MUSHROOM,
                BlockID.TNT,
                BlockID.TORCH,
                BlockID.FIRE,
                BlockID.REDSTONE_WIRE,
                BlockID.CROPS,
                BlockID.MINECART_TRACKS,
                BlockID.LEVER,
                BlockID.REDSTONE_TORCH_OFF,
                BlockID.REDSTONE_TORCH_ON,
                BlockID.REDSTONE_REPEATER_OFF,
                BlockID.REDSTONE_REPEATER_ON,
                BlockID.STONE_BUTTON,
                BlockID.CACTUS,
                BlockID.REED,
                // ores and stuff
                BlockID.BEDROCK,
                BlockID.GOLD_ORE,
                BlockID.IRON_ORE,
                BlockID.COAL_ORE,
                BlockID.DIAMOND_ORE,

                // @TODO rethink what should be disallowed by default
                // Gold and iron can be legitimately obtained, but were set to disallowed by
                // default. Diamond and coal can't be legitimately obtained. Sponges,
                // portals, snow, and locked chests also can't, but are allowed. None of
                // these blocks poses any immediate threat. Most of the blocks (in the first
                // section) are disallowed because people will accidentally set a huge area
                // of them, triggering physics and a million item drops, lagging the server.
                // Doors also have this effect, but are not disallowed.
            };

    public boolean profile = false;
    public Set<Integer> disallowedBlocks = new HashSet<Integer>();
    public int defaultChangeLimit = -1;
    public int maxChangeLimit = -1;
    public int defaultMaxPolygonalPoints = -1;
    public int maxPolygonalPoints = 20;
    public int defaultMaxPolyhedronPoints = -1;
    public int maxPolyhedronPoints = 20;
    public String shellSaveType = "";
    public SnapshotRepository snapshotRepo = null;
    public int maxRadius = -1;
    public int maxSuperPickaxeSize = 5;
    public int maxBrushRadius = 6;
    public boolean logCommands = false;
    public String logFile = "";
    public boolean registerHelp = true; // what is the point of this, it's not even used
    public int wandItem = ItemID.WOOD_AXE;
    public boolean superPickaxeDrop = true;
    public boolean superPickaxeManyDrop = true;
    public boolean noDoubleSlash = false;
    public boolean useInventory = false;
    public boolean useInventoryOverride = false;
    public boolean useInventoryCreativeOverride = false;
    public boolean navigationUseGlass = true;
    public int navigationWand = ItemID.COMPASS;
    public int navigationWandMaxDistance = 50;
    public int scriptTimeout = 3000;
    public Set<Integer> allowedDataCycleBlocks = new HashSet<Integer>();
    public String saveDir = "schematics";
    public String scriptsDir = "craftscripts";
    public boolean showFirstUseVersion = true;
    public int butcherDefaultRadius = -1;
    public int butcherMaxRadius = -1;
    public boolean allowSymlinks = false;

    /**
     * Loads the configuration.
     */
    public abstract void load();

    /**
     * Get the working directory to work from.
     *
     * @return
     */
    public File getWorkingDirectory() {
        return new File(".");
    }
}
