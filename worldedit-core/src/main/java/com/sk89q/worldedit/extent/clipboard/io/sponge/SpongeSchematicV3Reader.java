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
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;

import java.io.IOException;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Reads schematic files using the Sponge Schematic Specification.
 */
public class SpongeSchematicV3Reader implements ClipboardReader {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    private final LinStream rootStream;

    public SpongeSchematicV3Reader(LinStream rootStream) {
        this.rootStream = rootStream;
    }

    @Override
    public Clipboard read() throws IOException {
        LinCompoundTag schematicTag = getBaseTag();
        ReaderUtil.checkSchematicVersion(3, schematicTag);

        final Platform platform = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.WORLD_EDITING);
        int liveDataVersion = platform.getDataVersion();

        VersionedDataFixer fixer = ReaderUtil.getVersionedDataFixer(
            schematicTag, platform, liveDataVersion
        );
        return readVersion3(schematicTag, fixer);
    }

    @Override
    public OptionalInt getDataVersion() {
        try {
            LinCompoundTag schematicTag = getBaseTag();
            ReaderUtil.checkSchematicVersion(3, schematicTag);

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
        return LinRootEntry.readFrom(rootStream).value().getTag("Schematic", LinTagType.compoundTag());
    }

    private Clipboard readVersion3(LinCompoundTag schematicTag, VersionedDataFixer fixer) throws IOException {
        int width = schematicTag.getTag("Width", LinTagType.shortTag()).valueAsShort() & 0xFFFF;
        int height = schematicTag.getTag("Height", LinTagType.shortTag()).valueAsShort() & 0xFFFF;
        int length = schematicTag.getTag("Length", LinTagType.shortTag()).valueAsShort() & 0xFFFF;

        BlockVector3 offset = ReaderUtil.decodeBlockVector3(
            schematicTag.findTag("Offset", LinTagType.intArrayTag())
        );

        BlockVector3 origin = BlockVector3.ZERO;
        LinCompoundTag metadataTag = schematicTag.findTag("Metadata", LinTagType.compoundTag());
        if (metadataTag != null) {
            LinCompoundTag worldeditMeta = metadataTag.findTag("WorldEdit", LinTagType.compoundTag());
            if (worldeditMeta != null) {
                origin = ReaderUtil.decodeBlockVector3(
                    worldeditMeta.findTag("Origin", LinTagType.intArrayTag())
                );
            }
        }
        BlockVector3 min = offset.add(origin);

        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE))
        );
        clipboard.setOrigin(origin);

        decodeBlocksIntoClipboard(fixer, schematicTag, clipboard);

        LinCompoundTag biomeContainer = schematicTag.findTag("Biomes", LinTagType.compoundTag());
        if (biomeContainer != null) {
            readBiomes3(clipboard, biomeContainer, fixer);
        }

        LinListTag<LinCompoundTag> entities = schematicTag.findListTag("Entities", LinTagType.compoundTag());
        if (entities != null) {
            ReaderUtil.readEntities(clipboard, entities.value(), fixer, true);
        }

        return clipboard;
    }

    private void decodeBlocksIntoClipboard(VersionedDataFixer fixer, LinCompoundTag schematic,
                                           BlockArrayClipboard clipboard) throws IOException {
        LinCompoundTag blockContainer = schematic.getTag("Blocks", LinTagType.compoundTag());

        LinCompoundTag paletteObject = blockContainer.getTag("Palette", LinTagType.compoundTag());
        Map<Integer, BlockState> palette = ReaderUtil.decodePalette(
            paletteObject, fixer
        );

        byte[] blocks = blockContainer.getTag("Data", LinTagType.byteArrayTag()).value();
        LinListTag<LinCompoundTag> blockEntities = blockContainer.findListTag("BlockEntities", LinTagType.compoundTag());

        ReaderUtil.initializeClipboardFromBlocks(
            clipboard, palette, blocks, blockEntities, fixer, true
        );
    }

    private void readBiomes3(BlockArrayClipboard clipboard, LinCompoundTag biomeContainer,
                             VersionedDataFixer fixer) throws IOException {
        LinCompoundTag paletteTag = biomeContainer.getTag("Palette", LinTagType.compoundTag());

        Int2ObjectMap<BiomeType> palette = ReaderUtil.readBiomePalette(fixer, paletteTag, LOGGER);

        int width = clipboard.getRegion().getWidth();
        int length = clipboard.getRegion().getLength();

        byte[] biomes = biomeContainer.getTag("Data", LinTagType.byteArrayTag()).value();
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
    }
}
