/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.util.formatting.component.TextUtils;
import com.sk89q.worldedit.util.io.file.ArchiveNioSupports;
import com.sk89q.worldedit.util.logging.LogFormat;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.snapshot.SnapshotRepository;
import com.sk89q.worldedit.world.snapshot.experimental.SnapshotDatabase;
import com.sk89q.worldedit.world.snapshot.experimental.fs.FileSystemSnapshotDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Represents WorldEdit's configuration.
 */
public abstract class LocalConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalConfiguration.class);

    public boolean profile = false;
    public boolean traceUnflushedSessions = false;
    public Set<String> disallowedBlocks = new HashSet<>();
    public int defaultChangeLimit = -1;
    public int maxChangeLimit = -1;
    public int defaultVerticalHeight = 256;
    public int defaultMaxPolygonalPoints = -1;
    public int maxPolygonalPoints = 20;
    public int defaultMaxPolyhedronPoints = -1;
    public int maxPolyhedronPoints = 20;
    public String shellSaveType = "";
    public boolean snapshotsConfigured = false;
    public SnapshotRepository snapshotRepo = null;
    public SnapshotDatabase snapshotDatabase = null;
    public int maxRadius = -1;
    public int maxSuperPickaxeSize = 5;
    public int maxBrushRadius = 6;
    public boolean logCommands = false;
    public String logFile = "";
    public String logFormat = LogFormat.DEFAULT_FORMAT;
    public boolean registerHelp = true; // unused
    public String wandItem = "minecraft:wooden_axe";
    public boolean superPickaxeDrop = true;
    public boolean superPickaxeManyDrop = true;
    public boolean useInventory = false;
    public boolean useInventoryOverride = false;
    public boolean useInventoryCreativeOverride = false;
    public boolean navigationUseGlass = true;
    public String navigationWand = "minecraft:compass";
    public int navigationWandMaxDistance = 50;
    public int scriptTimeout = 3000;
    public int calculationTimeout = 100;
    public int maxCalculationTimeout = 300;
    public Set<String> allowedDataCycleBlocks = new HashSet<>();
    public String saveDir = "schematics";
    public String scriptsDir = "craftscripts";
    public boolean showHelpInfo = true; // unused
    public int butcherDefaultRadius = -1;
    public int butcherMaxRadius = -1;
    public boolean allowSymlinks = false;
    public boolean serverSideCUI = true;
    public boolean extendedYLimit = false;
    public String defaultLocaleName = "default";
    public Locale defaultLocale = Locale.getDefault();

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
     * @deprecated Use {@link LocalConfiguration#getWorkingDirectoryPath()}
     */
    @Deprecated
    public File getWorkingDirectory() {
        return getWorkingDirectoryPath().toFile();
    }

    /**
     * Get the working directory to work from.
     *
     * @return a working directory
     */
    public Path getWorkingDirectoryPath() {
        return Paths.get(".");
    }

    public void initializeSnapshotConfiguration(String directory, boolean experimental) {
        // Reset for reload
        snapshotRepo = null;
        snapshotDatabase = null;
        snapshotsConfigured = false;
        if (!directory.isEmpty()) {
            if (experimental) {
                try {
                    snapshotDatabase = FileSystemSnapshotDatabase.maybeCreate(
                        Paths.get(directory),
                        ArchiveNioSupports.combined()
                    );
                    snapshotsConfigured = true;
                } catch (IOException e) {
                    LOGGER.warn("Failed to open snapshotDatabase", e);
                }
            } else {
                snapshotRepo = new SnapshotRepository(directory);
                snapshotsConfigured = true;
            }
        }
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
        } catch (Throwable ignored) {
        }

        return item;
    }

    public void setDefaultLocaleName(String localeName) {
        this.defaultLocaleName = localeName;
        if (localeName.equals("default")) {
            this.defaultLocale = Locale.getDefault();
        } else {
            this.defaultLocale = TextUtils.getLocaleByMinecraftTag(localeName);
        }
    }
}
