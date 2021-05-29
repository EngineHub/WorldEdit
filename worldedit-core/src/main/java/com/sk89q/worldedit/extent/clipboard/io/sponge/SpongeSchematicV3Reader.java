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

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;
import com.sk89q.worldedit.extent.clipboard.io.SchematicNbtUtil;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.VarIntIterator;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads schematic files using the Sponge Schematic Specification.
 */
public class SpongeSchematicV3Reader extends NBTSchematicReader {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final NBTInputStream inputStream;

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public SpongeSchematicV3Reader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    @Override
    public Clipboard read() throws IOException {
        CompoundTag schematicTag = getBaseTag();
        ReaderUtil.checkSchematicVersion(3, schematicTag);

        final Platform platform = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.WORLD_EDITING);
        int liveDataVersion = platform.getDataVersion();

        VersionedDataFixer fixer = ReaderUtil.getVersionedDataFixer(
            schematicTag.getValue(), platform, liveDataVersion
        );
        return readVersion3(schematicTag, fixer);
    }

    @Override
    public OptionalInt getDataVersion() {
        try {
            CompoundTag schematicTag = getBaseTag();
            ReaderUtil.checkSchematicVersion(3, schematicTag);

            int dataVersion = requireTag(schematicTag.getValue(), "DataVersion", IntTag.class)
                .getValue();
            if (dataVersion < 0) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(dataVersion);
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }

    private CompoundTag getBaseTag() throws IOException {
        NamedTag rootTag = inputStream.readNamedTag();
        CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
        // Nested inside the root tag
        return requireTag(schematicTag.getValue(), "Schematic", CompoundTag.class);
    }

    private Clipboard readVersion3(CompoundTag schematicTag, VersionedDataFixer fixer) throws IOException {
        Map<String, Tag> schematic = schematicTag.getValue();

        int width = requireTag(schematic, "Width", ShortTag.class).getValue() & 0xFFFF;
        int height = requireTag(schematic, "Height", ShortTag.class).getValue() & 0xFFFF;
        int length = requireTag(schematic, "Length", ShortTag.class).getValue() & 0xFFFF;

        BlockVector3 offset = ReaderUtil.decodeBlockVector3(
            SchematicNbtUtil.getTag(schematic, "Offset", IntArrayTag.class)
        );

        BlockVector3 origin = BlockVector3.ZERO;
        CompoundTag metadataTag = getTag(schematic, "Metadata", CompoundTag.class);
        if (metadataTag != null && metadataTag.containsKey("WorldEdit")) {
            // We appear to have WorldEdit Metadata
            Map<String, Tag> worldedit =
                requireTag(metadataTag.getValue(), "WorldEdit", CompoundTag.class).getValue();
            origin = ReaderUtil.decodeBlockVector3(
                SchematicNbtUtil.getTag(worldedit, "Origin", IntArrayTag.class)
            );
        }
        BlockVector3 min = offset.add(origin);

        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE))
        );
        clipboard.setOrigin(origin);

        decodeBlocksIntoClipboard(fixer, schematic, clipboard);

        CompoundTag biomeContainer = getTag(schematic, "Biomes", CompoundTag.class);
        if (biomeContainer != null) {
            readBiomes3(clipboard, biomeContainer.getValue(), fixer);
        }

        ListTag entities = getTag(schematic, "Entities", ListTag.class);
        if (entities != null) {
            ReaderUtil.readEntities(clipboard, entities.getValue(), fixer, true);
        }

        return clipboard;
    }

    private void decodeBlocksIntoClipboard(VersionedDataFixer fixer, Map<String, Tag> schematic,
                                           BlockArrayClipboard clipboard) throws IOException {
        Map<String, Tag> blockContainer = requireTag(schematic, "Blocks", CompoundTag.class).getValue();

        Map<String, Tag> paletteObject = requireTag(blockContainer, "Palette", CompoundTag.class).getValue();
        Map<Integer, BlockState> palette = ReaderUtil.decodePalette(
            paletteObject, fixer
        );

        byte[] blocks = requireTag(blockContainer, "Data", ByteArrayTag.class).getValue();
        ListTag tileEntities = getTag(blockContainer, "BlockEntities", ListTag.class);

        ReaderUtil.initializeClipboardFromBlocks(
            clipboard, palette, blocks, tileEntities, fixer, true
        );
    }

    private void readBiomes3(BlockArrayClipboard clipboard, Map<String, Tag> biomeContainer,
                             VersionedDataFixer fixer) throws IOException {
        CompoundTag paletteTag = requireTag(biomeContainer, "Palette", CompoundTag.class);

        Map<Integer, BiomeType> palette = new HashMap<>();

        for (Entry<String, Tag> palettePart : paletteTag.getValue().entrySet()) {
            String key = palettePart.getKey();
            key = fixer.fixUp(DataFixer.FixTypes.BIOME, key);
            BiomeType biome = BiomeTypes.get(key);
            if (biome == null) {
                LOGGER.warn("Unknown biome type `" + key + "` in palette."
                    + " Are you missing a mod or using a schematic made in a newer version of Minecraft?");
            }
            Tag idTag = palettePart.getValue();
            if (!(idTag instanceof IntTag)) {
                throw new IOException("Biome mapped to non-Int tag.");
            }
            palette.put(((IntTag) idTag).getValue(), biome);
        }

        int width = clipboard.getRegion().getWidth();
        int length = clipboard.getRegion().getLength();

        byte[] biomes = requireTag(biomeContainer, "Data", ByteArrayTag.class).getValue();
        BlockVector3 min = clipboard.getMinimumPoint();
        int index = 0;
        for (VarIntIterator iter = new VarIntIterator(biomes); iter.hasNext(); index++) {
            int nextBiomeId = iter.nextInt();
            BiomeType type = palette.get(nextBiomeId);
            BlockVector3 pos = ReaderUtil.decodePositionFromDataIndex(
                width, length, index
            );
            clipboard.setBiome(min.add(pos), type);
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
