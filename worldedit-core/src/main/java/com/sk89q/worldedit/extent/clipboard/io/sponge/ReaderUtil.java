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

package com.sk89q.worldedit.extent.clipboard.io.sponge;

import com.google.common.collect.Maps;
import com.sk89q.jnbt.AdventureNBTConverter;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.SchematicNbtUtil;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.VarIntIterator;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.storage.NBTConversions;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;
import static com.sk89q.worldedit.extent.clipboard.io.SchematicNbtUtil.requireTag;

/**
 * Common code shared between schematic readers.
 */
class ReaderUtil {
    private static final Logger LOGGER = LogManagerCompat.getLogger();

    static void checkSchematicVersion(int version, CompoundTag schematicTag) throws IOException {
        int schematicVersion = requireTag(schematicTag.getValue(), "Version", IntTag.class)
            .getValue();

        checkState(
            version == schematicVersion,
            "Schematic is not version %s, but %s", version, schematicVersion
        );
    }

    static VersionedDataFixer getVersionedDataFixer(Map<String, Tag> schematic, Platform platform,
                                                    int liveDataVersion) throws IOException {
        DataFixer fixer = null;
        int dataVersion = requireTag(schematic, "DataVersion", IntTag.class).getValue();
        if (dataVersion < 0) {
            LOGGER.warn(
                "Schematic has an unknown data version ({}). Data may be incompatible.",
                dataVersion
            );
        } else if (dataVersion > liveDataVersion) {
            LOGGER.warn(
                "Schematic was made in a newer Minecraft version ({} > {})."
                    + " Data may be incompatible.",
                dataVersion, liveDataVersion
            );
        } else if (dataVersion < liveDataVersion) {
            fixer = platform.getDataFixer();
            if (fixer != null) {
                LOGGER.debug(
                    "Schematic was made in an older Minecraft version ({} < {}),"
                        + " will attempt DFU.",
                    dataVersion, liveDataVersion
                );
            } else {
                LOGGER.info(
                    "Schematic was made in an older Minecraft version ({} < {}),"
                        + " but DFU is not available. Data may be incompatible.",
                    dataVersion, liveDataVersion
                );
            }
        }

        return new VersionedDataFixer(dataVersion, fixer);
    }

    static Map<Integer, BlockState> decodePalette(
        Map<String, Tag> paletteObject, VersionedDataFixer fixer
    ) throws IOException {
        Map<Integer, BlockState> palette = new HashMap<>();

        ParserContext parserContext = new ParserContext();
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false);
        parserContext.setPreferringWildcard(false);

        for (String palettePart : paletteObject.keySet()) {
            int id = requireTag(paletteObject, palettePart, IntTag.class).getValue();
            palettePart = fixer.fixUp(DataFixer.FixTypes.BLOCK_STATE, palettePart);
            BlockState state;
            try {
                state = WorldEdit.getInstance().getBlockFactory().parseFromInput(palettePart, parserContext).toImmutableState();
            } catch (InputParseException e) {
                LOGGER.warn("Invalid BlockState in palette: " + palettePart + ". Block will be replaced with air.");
                state = BlockTypes.AIR.getDefaultState();
            }
            palette.put(id, state);
        }
        return palette;
    }

    static void initializeClipboardFromBlocks(
        Clipboard clipboard, Map<Integer, BlockState> palette, byte[] blocks, ListTag tileEntities,
        VersionedDataFixer fixer
    ) throws IOException {
        Map<BlockVector3, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
        if (tileEntities != null) {
            List<Map<String, Tag>> tileEntityTags = tileEntities.getValue().stream()
                .map(tag -> (CompoundTag) tag)
                .map(CompoundTag::getValue)
                .collect(Collectors.toList());

            for (Map<String, Tag> tileEntity : tileEntityTags) {
                int[] pos = requireTag(tileEntity, "Pos", IntArrayTag.class).getValue();
                final BlockVector3 pt = clipboard.getMinimumPoint().add(pos[0], pos[1], pos[2]);
                Map<String, Tag> values = Maps.newHashMap(tileEntity);
                values.put("x", new IntTag(pt.getBlockX()));
                values.put("y", new IntTag(pt.getBlockY()));
                values.put("z", new IntTag(pt.getBlockZ()));
                values.put("id", values.get("Id"));
                values.remove("Id");
                values.remove("Pos");
                if (fixer.isActive()) {
                    tileEntity = ((CompoundTag) AdventureNBTConverter.fromAdventure(fixer.fixUp(
                        DataFixer.FixTypes.BLOCK_ENTITY,
                        new CompoundTag(values).asBinaryTag()
                    ))).getValue();
                } else {
                    tileEntity = values;
                }
                tileEntitiesMap.put(pt, tileEntity);
            }
        }

        int width = clipboard.getRegion().getWidth();
        int length = clipboard.getRegion().getLength();

        int index = 0;
        for (VarIntIterator iter = new VarIntIterator(blocks); iter.hasNext(); index++) {
            int nextBlockId = iter.nextInt();
            BlockState state = palette.get(nextBlockId);
            BlockVector3 rawPos = decodePositionFromDataIndex(width, length, index);
            try {
                BlockVector3 offsetPos = clipboard.getMinimumPoint().add(rawPos);
                Map<String, Tag> tileEntity = tileEntitiesMap.get(offsetPos);
                if (tileEntity != null) {
                    clipboard.setBlock(
                        offsetPos, state.toBaseBlock(new CompoundTag(tileEntity))
                    );
                } else {
                    clipboard.setBlock(offsetPos, state);
                }
            } catch (WorldEditException e) {
                throw new IOException("Failed to load a block in the schematic", e);
            }
        }
    }

    static BlockVector3 decodePositionFromDataIndex(int width, int length, int index) {
        // index = (y * width * length) + (z * width) + x
        int y = index / (width * length);
        int remainder = index - (y * width * length);
        int z = remainder / width;
        int x = remainder - z * width;
        return BlockVector3.at(x, y, z);
    }

    static BlockVector3 decodeBlockVector3(@Nullable IntArrayTag tag) throws IOException {
        if (tag == null) {
            return BlockVector3.ZERO;
        }
        int[] parts = tag.getValue();
        if (parts.length != 3) {
            throw new IOException("Invalid block vector specified in schematic.");
        }
        return BlockVector3.at(parts[0], parts[1], parts[2]);
    }

    static void readEntities(BlockArrayClipboard clipboard, List<Tag> entList,
                             VersionedDataFixer fixer, boolean positionIsRelative) throws IOException {
        if (entList.isEmpty()) {
            return;
        }
        for (Tag et : entList) {
            if (!(et instanceof CompoundTag)) {
                continue;
            }
            CompoundTag entityTag = (CompoundTag) et;
            Map<String, Tag> tags = entityTag.getValue();
            String id = requireTag(tags, "Id", StringTag.class).getValue();
            entityTag = entityTag.createBuilder().putString("id", id).remove("Id").build();
            entityTag = ((CompoundTag) AdventureNBTConverter.fromAdventure(fixer.fixUp(
                DataFixer.FixTypes.ENTITY,
                entityTag.asBinaryTag()
            )));

            EntityType entityType = EntityTypes.get(id);
            if (entityType != null) {
                Location location = NBTConversions.toLocation(clipboard,
                    requireTag(tags, "Pos", ListTag.class),
                    requireTag(tags, "Rotation", ListTag.class));
                BaseEntity state = new BaseEntity(entityType, entityTag);
                if (positionIsRelative) {
                    location = location.setPosition(
                        location.toVector().add(clipboard.getMinimumPoint().toVector3())
                    );
                }
                clipboard.createEntity(location, state);
            } else {
                LOGGER.warn("Unknown entity when pasting schematic: " + id);
            }
        }
    }

    private ReaderUtil() {
    }
}
