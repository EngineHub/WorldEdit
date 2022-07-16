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
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;

import java.io.IOException;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Reads schematic files using the Sponge Schematic Specification (Version 1).
 */
public class SpongeSchematicV1Reader implements ClipboardReader {

    private final LinStream rootStream;

    public SpongeSchematicV1Reader(LinStream rootStream) {
        this.rootStream = rootStream;
    }

    @Override
    public Clipboard read() throws IOException {
        LinCompoundTag schematicTag = getBaseTag();
        ReaderUtil.checkSchematicVersion(1, getBaseTag());

        return doRead(schematicTag);
    }

    // For legacy SpongeSchematicReader, can be inlined in WorldEdit 8
    public static BlockArrayClipboard doRead(LinCompoundTag schematicTag) throws IOException {
        final Platform platform = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.WORLD_EDITING);

        // this is a relatively safe assumption unless someone imports a schematic from 1.12
        // e.g. sponge 7.1-
        VersionedDataFixer fixer = new VersionedDataFixer(
            Constants.DATA_VERSION_MC_1_13_2, platform.getDataFixer()
        );
        return readVersion1(schematicTag, fixer);
    }

    @Override
    public OptionalInt getDataVersion() {
        try {
            // Validate schematic version to be sure
            ReaderUtil.checkSchematicVersion(1, getBaseTag());
            return OptionalInt.of(Constants.DATA_VERSION_MC_1_13_2);
        } catch (IOException e) {
            return OptionalInt.empty();
        }
    }

    private LinCompoundTag getBaseTag() throws IOException {
        return LinRootEntry.readFrom(rootStream).value();
    }

    static BlockArrayClipboard readVersion1(LinCompoundTag schematicTag, VersionedDataFixer fixer) throws IOException {
        int width = schematicTag.getTag("Width", LinTagType.shortTag()).valueAsShort() & 0xFFFF;
        int height = schematicTag.getTag("Height", LinTagType.shortTag()).valueAsShort() & 0xFFFF;
        int length = schematicTag.getTag("Length", LinTagType.shortTag()).valueAsShort() & 0xFFFF;

        BlockVector3 min = ReaderUtil.decodeBlockVector3(
            schematicTag.findTag("Offset", LinTagType.intArrayTag())
        );

        BlockVector3 offset = BlockVector3.ZERO;
        LinCompoundTag metadataTag = schematicTag.findTag("Metadata", LinTagType.compoundTag());
        if (metadataTag != null) {
            LinIntTag offsetX = metadataTag.findTag("WEOffsetX", LinTagType.intTag());
            if (offsetX != null) {
                int offsetY = metadataTag.getTag("WEOffsetY", LinTagType.intTag()).valueAsInt();
                int offsetZ = metadataTag.getTag("WEOffsetZ", LinTagType.intTag()).valueAsInt();
                offset = BlockVector3.at(offsetX.valueAsInt(), offsetY, offsetZ);
            }
        }

        BlockVector3 origin = min.subtract(offset);
        Region region = new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE));

        LinIntTag paletteMaxTag = schematicTag.findTag("PaletteMax", LinTagType.intTag());
        LinCompoundTag paletteObject = schematicTag.getTag("Palette", LinTagType.compoundTag());
        if (paletteMaxTag != null && paletteObject.value().size() != paletteMaxTag.valueAsInt()) {
            throw new IOException("Block palette size does not match expected size.");
        }

        Map<Integer, BlockState> palette = ReaderUtil.decodePalette(
            paletteObject, fixer
        );

        byte[] blocks = schematicTag.getTag("BlockData", LinTagType.byteArrayTag()).value();

        LinListTag<LinCompoundTag> tileEntities = schematicTag.findListTag("BlockEntities", LinTagType.compoundTag());
        if (tileEntities == null) {
            tileEntities = schematicTag.findListTag("TileEntities", LinTagType.compoundTag());
        }

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(origin);
        ReaderUtil.initializeClipboardFromBlocks(
            clipboard, palette, blocks, tileEntities, fixer, false
        );
        return clipboard;
    }

    @Override
    public void close() throws IOException {
    }
}
