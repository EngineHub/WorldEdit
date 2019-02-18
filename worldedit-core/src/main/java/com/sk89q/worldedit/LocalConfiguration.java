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

import com.google.common.collect.Lists;
import com.sk89q.worldedit.util.logging.LogFormat;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents WorldEdit's configuration.
 */
public abstract class LocalConfiguration {

    public boolean profile = false;
    public boolean traceUnflushedSessions = false;
    public Set<String> disallowedBlocks = new HashSet<>();
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
    public String logFormat = LogFormat.DEFAULT_FORMAT;
    public boolean registerHelp = true; // what is the point of this, it's not even used
    public String wandItem = "minecraft:wooden_axe";
    public boolean superPickaxeDrop = true;
    public boolean superPickaxeManyDrop = true;
    public boolean noDoubleSlash = false;
    public boolean useInventory = false;
    public boolean useInventoryOverride = false;
    public boolean useInventoryCreativeOverride = false;
    public boolean navigationUseGlass = true;
    public String navigationWand = "minecraft:compass";
    public int navigationWandMaxDistance = 50;
    public int scriptTimeout = 3000;
    public int calculationTimeout = 100;
    public Set<String> allowedDataCycleBlocks = new HashSet<>();
    public String saveDir = "schematics";
    public String scriptsDir = "craftscripts";
    public boolean showHelpInfo = true;
    public int butcherDefaultRadius = -1;
    public int butcherMaxRadius = -1;
    public boolean allowSymlinks = false;
    public boolean serverSideCUI = true;

    protected String[] getDefaultDisallowedBlocks() {
        List<BlockType> blockTypes = Lists.newArrayList(
                BlockTypes.OAK_SAPLING,
                BlockTypes.JUNGLE_SAPLING,
                BlockTypes.DARK_OAK_SAPLING,
                BlockTypes.SPRUCE_SAPLING,
                BlockTypes.BIRCH_SAPLING,
                BlockTypes.ACACIA_SAPLING,
                BlockTypes.BLACK_BED,
                BlockTypes.BLUE_BED,
                BlockTypes.BROWN_BED,
                BlockTypes.CYAN_BED,
                BlockTypes.GRAY_BED,
                BlockTypes.GREEN_BED,
                BlockTypes.LIGHT_BLUE_BED,
                BlockTypes.LIGHT_GRAY_BED,
                BlockTypes.LIME_BED,
                BlockTypes.MAGENTA_BED,
                BlockTypes.ORANGE_BED,
                BlockTypes.PINK_BED,
                BlockTypes.PURPLE_BED,
                BlockTypes.RED_BED,
                BlockTypes.WHITE_BED,
                BlockTypes.YELLOW_BED,
                BlockTypes.POWERED_RAIL,
                BlockTypes.DETECTOR_RAIL,
                BlockTypes.GRASS,
                BlockTypes.DEAD_BUSH,
                BlockTypes.MOVING_PISTON,
                BlockTypes.PISTON_HEAD,
                BlockTypes.SUNFLOWER,
                BlockTypes.ROSE_BUSH,
                BlockTypes.DANDELION,
                BlockTypes.POPPY,
                BlockTypes.BROWN_MUSHROOM,
                BlockTypes.RED_MUSHROOM,
                BlockTypes.TNT,
                BlockTypes.TORCH,
                BlockTypes.FIRE,
                BlockTypes.REDSTONE_WIRE,
                BlockTypes.WHEAT,
                BlockTypes.POTATOES,
                BlockTypes.CARROTS,
                BlockTypes.MELON_STEM,
                BlockTypes.PUMPKIN_STEM,
                BlockTypes.BEETROOTS,
                BlockTypes.RAIL,
                BlockTypes.LEVER,
                BlockTypes.REDSTONE_TORCH,
                BlockTypes.REDSTONE_WALL_TORCH,
                BlockTypes.REPEATER,
                BlockTypes.COMPARATOR,
                BlockTypes.STONE_BUTTON,
                BlockTypes.BIRCH_BUTTON,
                BlockTypes.ACACIA_BUTTON,
                BlockTypes.DARK_OAK_BUTTON,
                BlockTypes.JUNGLE_BUTTON,
                BlockTypes.OAK_BUTTON,
                BlockTypes.SPRUCE_BUTTON,
                BlockTypes.CACTUS,
                BlockTypes.SUGAR_CANE,
                // ores and stuff
                BlockTypes.BEDROCK
        );
        return blockTypes.stream().filter(Objects::nonNull).map(BlockType::getId).toArray(String[]::new);
    }

    /**
     * Load the configuration.
     */
    public abstract void load();

    /**
     * Get the working directory to work from.
     *
     * @return a working directory
     */
    public File getWorkingDirectory() {
        return new File(".");
    }

    public String convertLegacyItem(String legacy) {
        String item = legacy;
        try {
            String[] splitter = item.split(":", 2);
            int id = 0;
            byte data = 0;
            if (splitter.length == 1) {
                id = Integer.parseInt(item);
            } else {
                id = Integer.parseInt(splitter[0]);
                data = Byte.parseByte(splitter[1]);
            }
            item = LegacyMapper.getInstance().getItemFromLegacy(id, data).getId();
        } catch (Throwable e) {
        }

        return item;
    }

}
