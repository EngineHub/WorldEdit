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
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockState;

import java.io.IOException;
import java.util.Map;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads schematic files using the Sponge Schematic Specification (Version 1).
 */
public class SpongeSchematicV1Reader extends NBTSchematicReader {

    private final NBTInputStream inputStream;

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public SpongeSchematicV1Reader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    @Override
    public Clipboard read() throws IOException {
        CompoundTag schematicTag = getBaseTag();
        ReaderUtil.checkSchematicVersion(1, getBaseTag());

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

    private CompoundTag getBaseTag() throws IOException {
        NamedTag rootTag = inputStream.readNamedTag();

        return (CompoundTag) rootTag.getTag();
    }

    static BlockArrayClipboard readVersion1(CompoundTag schematicTag, VersionedDataFixer fixer) throws IOException {
        Map<String, Tag> schematic = schematicTag.getValue();

        int width = requireTag(schematic, "Width", ShortTag.class).getValue() & 0xFFFF;
        int height = requireTag(schematic, "Height", ShortTag.class).getValue() & 0xFFFF;
        int length = requireTag(schematic, "Length", ShortTag.class).getValue() & 0xFFFF;

        BlockVector3 min = ReaderUtil.decodeBlockVector3(
            getTag(schematic, "Offset", IntArrayTag.class)
        );

        BlockVector3 offset = BlockVector3.ZERO;
        CompoundTag metadataTag = getTag(schematic, "Metadata", CompoundTag.class);
        if (metadataTag != null && metadataTag.containsKey("WEOffsetX")) {
            // We appear to have WorldEdit Metadata
            Map<String, Tag> metadata = metadataTag.getValue();
            int offsetX = requireTag(metadata, "WEOffsetX", IntTag.class).getValue();
            int offsetY = requireTag(metadata, "WEOffsetY", IntTag.class).getValue();
            int offsetZ = requireTag(metadata, "WEOffsetZ", IntTag.class).getValue();
            offset = BlockVector3.at(offsetX, offsetY, offsetZ);
        }

        BlockVector3 origin = min.subtract(offset);
        Region region = new CuboidRegion(min, min.add(width, height, length).subtract(BlockVector3.ONE));

        IntTag paletteMaxTag = getTag(schematic, "PaletteMax", IntTag.class);
        Map<String, Tag> paletteObject = requireTag(schematic, "Palette", CompoundTag.class).getValue();
        if (paletteMaxTag != null && paletteObject.size() != paletteMaxTag.getValue()) {
            throw new IOException("Block palette size does not match expected size.");
        }

        Map<Integer, BlockState> palette = ReaderUtil.decodePalette(
            paletteObject, fixer
        );

        byte[] blocks = requireTag(schematic, "BlockData", ByteArrayTag.class).getValue();

        ListTag tileEntities = getTag(schematic, "BlockEntities", ListTag.class);
        if (tileEntities == null) {
            tileEntities = getTag(schematic, "TileEntities", ListTag.class);
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
        inputStream.close();
    }
}
