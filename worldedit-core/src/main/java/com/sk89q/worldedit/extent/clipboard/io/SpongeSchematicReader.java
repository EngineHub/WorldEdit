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

package com.sk89q.worldedit.extent.clipboard.io;

import com.google.common.collect.Maps;
import com.sk89q.jnbt.AdventureNBTConverter;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.storage.NBTConversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads schematic files using the Sponge Schematic Specification.
 */
public class SpongeSchematicReader extends NBTSchematicReader {

    private static final Logger log = LoggerFactory.getLogger(SpongeSchematicReader.class);
    private final NBTInputStream inputStream;
    private DataFixer fixer = null;
    private int schematicVersion = -1;
    private int dataVersion = -1;

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public SpongeSchematicReader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    @Override
    public Clipboard read() throws IOException {
        CompoundTag schematicTag = getBaseTag();
        Map<String, Tag> schematic = schematicTag.getValue();

        final Platform platform = WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.WORLD_EDITING);
        int liveDataVersion = platform.getDataVersion();

        if (schematicVersion == 1) {
            dataVersion = Constants.DATA_VERSION_MC_1_13_2; // this is a relatively safe assumption unless someone imports a schematic from 1.12, e.g. sponge 7.1-
            fixer = platform.getDataFixer();
            return readVersion1(schematicTag);
        } else if (schematicVersion == 2) {
            dataVersion = requireTag(schematic, "DataVersion", IntTag.class).getValue();
            if (dataVersion < 0) {
                log.warn("Schematic has an unknown data version ({}). Data may be incompatible.",
                    dataVersion);
                // Do not DFU unknown data
                dataVersion = liveDataVersion;
            }
            if (dataVersion > liveDataVersion) {
                log.warn("Schematic was made in a newer Minecraft version ({} > {}). Data may be incompatible.",
                        dataVersion, liveDataVersion);
            } else if (dataVersion < liveDataVersion) {
                fixer = platform.getDataFixer();
                if (fixer != null) {
                    log.debug("Schematic was made in an older Minecraft version ({} < {}), will attempt DFU.",
                            dataVersion, liveDataVersion);
                } else {
                    log.info("Schematic was made in an older Minecraft version ({} < {}), but DFU is not available. Data may be incompatible.",
                            dataVersion, liveDataVersion);
                }
            }

            BlockArrayClipboard clip = readVersion1(schematicTag);
            return readVersion2(clip, schematicTag);
        }
        throw new IOException("This schematic version is currently not supported");
    }

    @Override
    public OptionalInt getDataVersion() {
        try {
            CompoundTag schematicTag = getBaseTag();
            Map<String, Tag> schematic = schematicTag.getValue();
            if (schematicVersion == 1) {
                return OptionalInt.of(Constants.DATA_VERSION_MC_1_13_2);
            } else if (schematicVersion == 2) {
                int dataVersion = requireTag(schematic, "DataVersion", IntTag.class).getValue();
                if (dataVersion < 0) {
                    return OptionalInt.empty();
                }
                return OptionalInt.of(dataVersion);
            }
            return OptionalInt.empty();
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }

    private CompoundTag getBaseTag() throws IOException {
        NamedTag rootTag = inputStream.readNamedTag();
        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();

        // Check
        Map<String, Tag> schematic = schematicTag.getValue();

        schematicVersion = requireTag(schematic, "Version", IntTag.class).getValue();
        return schematicTag;
    }

    private BlockArrayClipboard readVersion1(CompoundTag schematicTag) throws IOException {
        BlockVector3 origin;
        Region region;
        Map<String, Tag> schematic = schematicTag.getValue();

        int width = requireTag(schematic, "Width", ShortTag.class).getValue();
        int height = requireTag(schematic, "Height", ShortTag.class).getValue();
        int length = requireTag(schematic, "Length", ShortTag.class).getValue();

        IntArrayTag offsetTag = getTag(schematic, "Offset", IntArrayTag.class);
        int[] offsetParts;
        if (offsetTag != null) {
            offsetParts = offsetTag.getValue();
            if  (offsetParts.length != 3) {
                throw new IOException("Invalid offset specified in schematic.");
            }
        } else {
            offsetParts = new int[] {0, 0, 0};
        }

        BlockVector3 min = BlockVector3.at(offsetParts[0], offsetParts[1], offsetParts[2]);

        CompoundTag metadataTag = getTag(schematic, "Metadata", CompoundTag.class);
        if (metadataTag != null && metadataTag.containsKey("WEOffsetX")) {
            // We appear to have WorldEdit Metadata
            Map<String, Tag> metadata = metadataTag.getValue();
            int offsetX = requireTag(metadata, "WEOffsetX", IntTag.class).getValue();
            int offsetY = requireTag(metadata, "WEOffsetY", IntTag.class).getValue();
            int offsetZ = requireTag(metadata, "WEOffsetZ", IntTag.class).getValue();
            BlockVector3 offset = BlockVector3.at(offsetX, offsetY, offsetZ);
            origin = min.subtract(offset);
            region = new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE));
        } else {
            origin = min;
            region = new CuboidRegion(origin, origin.add(width, height, length).subtract(BlockVector3.ONE));
        }

        IntTag paletteMaxTag = getTag(schematic, "PaletteMax", IntTag.class);
        Map<String, Tag> paletteObject = requireTag(schematic, "Palette", CompoundTag.class).getValue();
        if (paletteMaxTag != null && paletteObject.size() != paletteMaxTag.getValue()) {
            throw new IOException("Block palette size does not match expected size.");
        }

        Map<Integer, BlockState> palette = new HashMap<>();

        ParserContext parserContext = new ParserContext();
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false);
        parserContext.setPreferringWildcard(false);

        for (String palettePart : paletteObject.keySet()) {
            int id = requireTag(paletteObject, palettePart, IntTag.class).getValue();
            if (fixer != null) {
                palettePart = fixer.fixUp(DataFixer.FixTypes.BLOCK_STATE, palettePart, dataVersion);
            }
            BlockState state;
            try {
                state = WorldEdit.getInstance().getBlockFactory().parseFromInput(palettePart, parserContext).toImmutableState();
            } catch (InputParseException e) {
                log.warn("Invalid BlockState in palette: " + palettePart + ". Block will be replaced with air.");
                state = BlockTypes.AIR.getDefaultState();
            }
            palette.put(id, state);
        }

        byte[] blocks = requireTag(schematic, "BlockData", ByteArrayTag.class).getValue();

        Map<BlockVector3, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
        ListTag tileEntities = getTag(schematic, "BlockEntities", ListTag.class);
        if (tileEntities == null) {
            tileEntities = getTag(schematic, "TileEntities", ListTag.class);
        }
        if (tileEntities != null) {
            List<Map<String, Tag>> tileEntityTags = tileEntities.getValue().stream()
                    .map(tag -> (CompoundTag) tag)
                    .map(CompoundTag::getValue)
                    .collect(Collectors.toList());

            for (Map<String, Tag> tileEntity : tileEntityTags) {
                int[] pos = requireTag(tileEntity, "Pos", IntArrayTag.class).getValue();
                final BlockVector3 pt = BlockVector3.at(pos[0], pos[1], pos[2]);
                Map<String, Tag> values = Maps.newHashMap(tileEntity);
                values.put("x", new IntTag(pt.getBlockX()));
                values.put("y", new IntTag(pt.getBlockY()));
                values.put("z", new IntTag(pt.getBlockZ()));
                values.put("id", values.get("Id"));
                values.remove("Id");
                values.remove("Pos");
                if (fixer != null) {
                    tileEntity = ((CompoundTag) AdventureNBTConverter.fromAdventure(fixer.fixUp(DataFixer.FixTypes.BLOCK_ENTITY, new CompoundTag(values).asBinaryTag(), dataVersion))).getValue();
                } else {
                    tileEntity = values;
                }
                tileEntitiesMap.put(pt, tileEntity);
            }
        }

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(origin);

        int index = 0;
        int i = 0;
        int value;
        int varintLength;
        while (i < blocks.length) {
            value = 0;
            varintLength = 0;

            while (true) {
                value |= (blocks[i] & 127) << (varintLength++ * 7);
                if (varintLength > 5) {
                    throw new IOException("VarInt too big (probably corrupted data)");
                }
                if ((blocks[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }
            // index = (y * length * width) + (z * width) + x
            int y = index / (width * length);
            int z = (index % (width * length)) / width;
            int x = (index % (width * length)) % width;
            BlockState state = palette.get(value);
            BlockVector3 pt = BlockVector3.at(x, y, z);
            try {
                if (tileEntitiesMap.containsKey(pt)) {
                    clipboard.setBlock(clipboard.getMinimumPoint().add(pt), state.toBaseBlock(new CompoundTag(tileEntitiesMap.get(pt))));
                } else {
                    clipboard.setBlock(clipboard.getMinimumPoint().add(pt), state);
                }
            } catch (WorldEditException e) {
                throw new IOException("Failed to load a block in the schematic");
            }

            index++;
        }

        return clipboard;
    }

    private Clipboard readVersion2(BlockArrayClipboard version1, CompoundTag schematicTag) throws IOException {
        Map<String, Tag> schematic = schematicTag.getValue();
        if (schematic.containsKey("BiomeData")) {
            readBiomes(version1, schematic);
        }
        if (schematic.containsKey("Entities")) {
            readEntities(version1, schematic);
        }
        return version1;
    }

    private void readBiomes(BlockArrayClipboard clipboard, Map<String, Tag> schematic) throws IOException {
        ByteArrayTag dataTag = requireTag(schematic, "BiomeData", ByteArrayTag.class);
        IntTag maxTag = requireTag(schematic, "BiomePaletteMax", IntTag.class);
        CompoundTag paletteTag = requireTag(schematic, "BiomePalette", CompoundTag.class);

        Map<Integer, BiomeType> palette = new HashMap<>();
        if (maxTag.getValue() != paletteTag.getValue().size()) {
            throw new IOException("Biome palette size does not match expected size.");
        }

        for (Entry<String, Tag> palettePart : paletteTag.getValue().entrySet()) {
            String key = palettePart.getKey();
            if (fixer != null) {
                key = fixer.fixUp(DataFixer.FixTypes.BIOME, key, dataVersion);
            }
            BiomeType biome = BiomeTypes.get(key);
            if (biome == null) {
                log.warn("Unknown biome type :" + key
                    + " in palette. Are you missing a mod or using a schematic made in a newer version of Minecraft?");
            }
            Tag idTag = palettePart.getValue();
            if (!(idTag instanceof IntTag)) {
                throw new IOException("Biome mapped to non-Int tag.");
            }
            palette.put(((IntTag) idTag).getValue(), biome);
        }

        int width = clipboard.getDimensions().getX();

        byte[] biomes = dataTag.getValue();
        int biomeIndex = 0;
        int biomeJ = 0;
        int bVal;
        int varIntLength;
        BlockVector3 min = clipboard.getMinimumPoint();
        while (biomeJ < biomes.length) {
            bVal = 0;
            varIntLength = 0;

            while (true) {
                bVal |= (biomes[biomeJ] & 127) << (varIntLength++ * 7);
                if (varIntLength > 5) {
                    throw new IOException("VarInt too big (probably corrupted data)");
                }
                if (((biomes[biomeJ] & 128) != 128)) {
                    biomeJ++;
                    break;
                }
                biomeJ++;
            }
            int z = biomeIndex / width;
            int x = biomeIndex % width;
            BiomeType type = palette.get(bVal);
            for (int y = 0; y < clipboard.getRegion().getHeight(); y++) {
                clipboard.setBiome(min.add(x, y, z), type);
            }
            biomeIndex++;
        }
    }

    private void readEntities(BlockArrayClipboard clipboard, Map<String, Tag> schematic) throws IOException {
        List<Tag> entList = requireTag(schematic, "Entities", ListTag.class).getValue();
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

            if (fixer != null) {
                entityTag = (CompoundTag) AdventureNBTConverter.fromAdventure(fixer.fixUp(DataFixer.FixTypes.ENTITY, entityTag.asBinaryTag(), dataVersion));
            }

            EntityType entityType = EntityTypes.get(id);
            if (entityType != null) {
                Location location = NBTConversions.toLocation(clipboard,
                        requireTag(tags, "Pos", ListTag.class),
                        requireTag(tags, "Rotation", ListTag.class));
                BaseEntity state = new BaseEntity(entityType, entityTag);
                clipboard.createEntity(location, state);
            } else {
                log.warn("Unknown entity when pasting schematic: " + id);
            }
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
