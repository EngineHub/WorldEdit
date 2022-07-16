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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.internal.util.VarIntIterator;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.tree.LinByteArrayTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Reads schematic files using the Sponge Schematic Specification (Version 2).
 */
public class SpongeSchematicV2Reader implements ClipboardReader {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final LinStream rootStream;

    public SpongeSchematicV2Reader(LinStream rootStream) {
        this.rootStream = rootStream;
    }

    @Override
    public Clipboard read() throws IOException {
        LinCompoundTag schematicTag = getBaseTag();
        ReaderUtil.checkSchematicVersion(2, schematicTag);

        return doRead(schematicTag);
    }

    // For legacy SpongeSchematicReader, can be inlined in WorldEdit 8
    public static Clipboard doRead(LinCompoundTag schematicTag) throws IOException {
        final Platform platform = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.WORLD_EDITING);
        int liveDataVersion = platform.getDataVersion();

        VersionedDataFixer fixer = ReaderUtil.getVersionedDataFixer(
            schematicTag, platform, liveDataVersion
        );
        BlockArrayClipboard clip = SpongeSchematicV1Reader.readVersion1(schematicTag, fixer);
        return readVersion2(clip, schematicTag, fixer);
    }

    @Override
    public OptionalInt getDataVersion() {
        try {
            LinCompoundTag schematicTag = getBaseTag();
            ReaderUtil.checkSchematicVersion(2, schematicTag);

            int dataVersion = schematicTag.getTag("DataVersion", LinTagType.intTag())
                .valueAsInt();
            if (dataVersion < 0) {
                return OptionalInt.empty();
            }
            return OptionalInt.of(dataVersion);
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }

    private LinCompoundTag getBaseTag() throws IOException {
        return LinRootEntry.readFrom(rootStream).value();
    }

    private static Clipboard readVersion2(BlockArrayClipboard version1, LinCompoundTag schematicTag,
                                          VersionedDataFixer fixer) throws IOException {
        if (schematicTag.value().containsKey("BiomeData")) {
            readBiomes2(version1, schematicTag, fixer);
        }
        LinListTag<LinCompoundTag> entities = schematicTag.findListTag("Entities", LinTagType.compoundTag());
        if (entities != null) {
            ReaderUtil.readEntities(
                version1, entities.value(), fixer, false
            );
        }
        return version1;
    }

    private static void readBiomes2(BlockArrayClipboard clipboard, LinCompoundTag schematic,
                                    VersionedDataFixer fixer) throws IOException {
        LinByteArrayTag dataTag = schematic.getTag("BiomeData", LinTagType.byteArrayTag());
        LinIntTag maxTag = schematic.getTag("BiomePaletteMax", LinTagType.intTag());
        LinCompoundTag paletteTag = schematic.getTag("BiomePalette", LinTagType.compoundTag());

        if (maxTag.valueAsInt() != paletteTag.value().size()) {
            throw new IOException("Biome palette size does not match expected size.");
        }

        Int2ObjectMap<BiomeType> palette = ReaderUtil.readBiomePalette(fixer, paletteTag, LOGGER);

        int width = clipboard.getDimensions().getX();

        byte[] biomes = dataTag.value();
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
    }
}
