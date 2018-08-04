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

import com.sk89q.worldedit.util.logging.LogFormat;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents WorldEdit's configuration.
 */
public abstract class LocalConfiguration {

    protected static final String[] defaultDisallowedBlocks = new String[] {
            // dangerous stuff (physics/drops items)
            BlockTypes.OAK_SAPLING.getId(),
            BlockTypes.JUNGLE_SAPLING.getId(),
            BlockTypes.DARK_OAK_SAPLING.getId(),
            BlockTypes.SPRUCE_SAPLING.getId(),
            BlockTypes.BIRCH_SAPLING.getId(),
            BlockTypes.ACACIA_SAPLING.getId(),
            BlockTypes.BLACK_BED.getId(),
            BlockTypes.BLUE_BED.getId(),
            BlockTypes.BROWN_BED.getId(),
            BlockTypes.CYAN_BED.getId(),
            BlockTypes.GRAY_BED.getId(),
            BlockTypes.GREEN_BED.getId(),
            BlockTypes.LIGHT_BLUE_BED.getId(),
            BlockTypes.LIGHT_GRAY_BED.getId(),
            BlockTypes.LIME_BED.getId(),
            BlockTypes.MAGENTA_BED.getId(),
            BlockTypes.ORANGE_BED.getId(),
            BlockTypes.PINK_BED.getId(),
            BlockTypes.PURPLE_BED.getId(),
            BlockTypes.RED_BED.getId(),
            BlockTypes.WHITE_BED.getId(),
            BlockTypes.YELLOW_BED.getId(),
            BlockTypes.POWERED_RAIL.getId(),
            BlockTypes.DETECTOR_RAIL.getId(),
            BlockTypes.GRASS.getId(),
            BlockTypes.DEAD_BUSH.getId(),
            BlockTypes.MOVING_PISTON.getId(),
            BlockTypes.PISTON_HEAD.getId(),
            BlockTypes.SUNFLOWER.getId(),
            BlockTypes.ROSE_BUSH.getId(),
            BlockTypes.DANDELION.getId(),
            BlockTypes.POPPY.getId(),
            BlockTypes.BROWN_MUSHROOM.getId(),
            BlockTypes.RED_MUSHROOM.getId(),
            BlockTypes.TNT.getId(),
            BlockTypes.TORCH.getId(),
            BlockTypes.FIRE.getId(),
            BlockTypes.REDSTONE_WIRE.getId(),
            BlockTypes.WHEAT.getId(),
            BlockTypes.POTATOES.getId(),
            BlockTypes.CARROTS.getId(),
            BlockTypes.MELON_STEM.getId(),
            BlockTypes.PUMPKIN_STEM.getId(),
            BlockTypes.BEETROOTS.getId(),
            BlockTypes.RAIL.getId(),
            BlockTypes.LEVER.getId(),
            BlockTypes.REDSTONE_TORCH.getId(),
            BlockTypes.REDSTONE_WALL_TORCH.getId(),
            BlockTypes.REPEATER.getId(),
            BlockTypes.COMPARATOR.getId(),
            BlockTypes.STONE_BUTTON.getId(),
            BlockTypes.BIRCH_BUTTON.getId(),
            BlockTypes.ACACIA_BUTTON.getId(),
            BlockTypes.DARK_OAK_BUTTON.getId(),
            BlockTypes.JUNGLE_BUTTON.getId(),
            BlockTypes.OAK_BUTTON.getId(),
            BlockTypes.SPRUCE_BUTTON.getId(),
            BlockTypes.CACTUS.getId(),
            BlockTypes.SUGAR_CANE.getId(),
            // ores and stuff
            BlockTypes.BEDROCK.getId(),
    };

    public boolean profile = false;
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
    public String wandItem = ItemTypes.WOODEN_AXE.getId();
    public boolean superPickaxeDrop = true;
    public boolean superPickaxeManyDrop = true;
    public boolean noDoubleSlash = false;
    public boolean useInventory = false;
    public boolean useInventoryOverride = false;
    public boolean useInventoryCreativeOverride = false;
    public boolean navigationUseGlass = true;
    public String navigationWand = ItemTypes.COMPASS.getId();
    public int navigationWandMaxDistance = 50;
    public int scriptTimeout = 3000;
    public Set<String> allowedDataCycleBlocks = new HashSet<>();
    public String saveDir = "schematics";
    public String scriptsDir = "craftscripts";
    public boolean showHelpInfo = true;
    public int butcherDefaultRadius = -1;
    public int butcherMaxRadius = -1;
    public boolean allowSymlinks = false;

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
