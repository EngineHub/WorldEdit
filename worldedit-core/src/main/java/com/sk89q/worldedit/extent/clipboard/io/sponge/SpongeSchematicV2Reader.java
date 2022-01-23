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
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.VarIntIterator;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads schematic files using the Sponge Schematic Specification (Version 2).
 */
public class SpongeSchematicV2Reader extends NBTSchematicReader {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final NBTInputStream inputStream;

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public SpongeSchematicV2Reader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    @Override
    public Clipboard read() throws IOException {
        CompoundTag schematicTag = getBaseTag();
        ReaderUtil.checkSchematicVersion(2, schematicTag);

        final Platform platform = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.WORLD_EDITING);
        int liveDataVersion = platform.getDataVersion();

        VersionedDataFixer fixer = ReaderUtil.getVersionedDataFixer(
            schematicTag.getValue(), platform, liveDataVersion
        );
        BlockArrayClipboard clip = SpongeSchematicV1Reader.readVersion1(schematicTag, fixer);
        return readVersion2(clip, schematicTag, fixer);
    }

    @Override
    public OptionalInt getDataVersion() {
        try {
            CompoundTag schematicTag = getBaseTag();
            ReaderUtil.checkSchematicVersion(2, schematicTag);

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

        return (CompoundTag) rootTag.getTag();
    }

    private Clipboard readVersion2(BlockArrayClipboard version1, CompoundTag schematicTag,
                                   VersionedDataFixer fixer) throws IOException {
        Map<String, Tag> schematic = schematicTag.getValue();
        if (schematic.containsKey("BiomeData")) {
            readBiomes2(version1, schematic, fixer);
        }
        ListTag entities = getTag(schematic, "Entities", ListTag.class);
        if (entities != null) {
            ReaderUtil.readEntities(
                version1, entities.getValue(), fixer, false
            );
        }
        return version1;
    }

    private void readBiomes2(BlockArrayClipboard clipboard, Map<String, Tag> schematic,
                             VersionedDataFixer fixer) throws IOException {
        ByteArrayTag dataTag = requireTag(schematic, "BiomeData", ByteArrayTag.class);
        IntTag maxTag = requireTag(schematic, "BiomePaletteMax", IntTag.class);
        CompoundTag paletteTag = requireTag(schematic, "BiomePalette", CompoundTag.class);

        Map<Integer, BiomeType> palette = new HashMap<>();
        if (maxTag.getValue() != paletteTag.getValue().size()) {
            throw new IOException("Biome palette size does not match expected size.");
        }

        for (Entry<String, Tag> palettePart : paletteTag.getValue().entrySet()) {
            String key = palettePart.getKey();
            key = fixer.fixUp(DataFixer.FixTypes.BIOME, key);
            BiomeType biome = BiomeTypes.get(key);
            if (biome == null) {
                LOGGER.warn("Unknown biome type :" + key
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
        BlockVector3 min = clipboard.getMinimumPoint();
        int index = 0;
        for (VarIntIterator iter = new VarIntIterator(biomes); iter.hasNext(); index++) {
            int nextBiomeId = iter.nextInt();
            BiomeType type = palette.get(nextBiomeId);
            // hack -- the x and y values from the 3d decode with length == 1 are equivalent
            BlockVector3 hackDecode = ReaderUtil.decodePositionFromDataIndex(
                width, 1, index
            );
            int x = hackDecode.getX();
            int z = hackDecode.getY();
            for (int y = 0; y < clipboard.getRegion().getHeight(); y++) {
                clipboard.setBiome(min.add(x, y, z), type);
            }
        }
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
