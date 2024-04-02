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
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import org.enginehub.linbus.stream.LinBinaryIO;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinRootEntry;
import org.enginehub.linbus.tree.LinTagType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Writes schematic files using the Sponge Schematic Specification (Version 2).
 */
public class SpongeSchematicV2Writer implements ClipboardWriter {

    private static final int CURRENT_VERSION = 2;

    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;
    private final DataOutputStream outputStream;

    public SpongeSchematicV2Writer(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void write(Clipboard clipboard) throws IOException {
        LinBinaryIO.write(
            outputStream,
            new LinRootEntry("Schematic", write2(clipboard))
        );
    }

    /**
     * Writes a version 2 schematic file.
     *
     * @param clipboard The clipboard
     * @return the schematic tag
     */
    private LinCompoundTag write2(Clipboard clipboard) {
        Region region = clipboard.getRegion();
        BlockVector3 origin = clipboard.getOrigin();
        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 offset = min.subtract(origin);
        int width = region.getWidth();
        int height = region.getHeight();
        int length = region.getLength();

        if (width > MAX_SIZE) {
            throw new IllegalArgumentException("Width of region too large for a .schematic");
        }
        if (height > MAX_SIZE) {
            throw new IllegalArgumentException("Height of region too large for a .schematic");
        }
        if (length > MAX_SIZE) {
            throw new IllegalArgumentException("Length of region too large for a .schematic");
        }

        LinCompoundTag.Builder schematic = LinCompoundTag.builder();
        schematic.putInt("Version", CURRENT_VERSION);
        schematic.putInt(
            "DataVersion",
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion()
        );

        LinCompoundTag.Builder metadata = LinCompoundTag.builder();
        metadata.putInt("WEOffsetX", offset.x());
        metadata.putInt("WEOffsetY", offset.y());
        metadata.putInt("WEOffsetZ", offset.z());

        LinCompoundTag.Builder worldEditSection = LinCompoundTag.builder();
        worldEditSection.putString("Version", WorldEdit.getVersion());
        worldEditSection.putString(
            "EditingPlatform",
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).id()
        );
        worldEditSection.putIntArray("Offset", new int[] {
            offset.x(), offset.y(), offset.z()
        });

        LinCompoundTag.Builder platformsSection = LinCompoundTag.builder();
        for (Platform platform : WorldEdit.getInstance().getPlatformManager().getPlatforms()) {
            platformsSection.put(
                platform.id(),
                LinCompoundTag.builder()
                    .putString("Name", platform.getPlatformName())
                    .putString("Version", platform.getPlatformVersion())
                    .build()
            );
        }
        worldEditSection.put("Platforms", platformsSection.build());

        metadata.put("WorldEdit", worldEditSection.build());

        schematic.put("Metadata", metadata.build());

        schematic.putShort("Width", (short) width);
        schematic.putShort("Height", (short) height);
        schematic.putShort("Length", (short) length);

        // The Sponge format Offset refers to the 'min' points location in the world. That's our 'Origin'
        schematic.putIntArray("Offset", new int[] {
            min.x(),
            min.y(),
            min.z(),
        });

        int paletteMax = 0;
        Object2IntMap<String> palette = new Object2IntLinkedOpenHashMap<>();

        LinListTag.Builder<LinCompoundTag> tileEntities = LinListTag.builder(LinTagType.compoundTag());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);

        for (int y = 0; y < height; y++) {
            int y0 = min.y() + y;
            for (int z = 0; z < length; z++) {
                int z0 = min.z() + z;
                for (int x = 0; x < width; x++) {
                    int x0 = min.x() + x;
                    BlockVector3 point = BlockVector3.at(x0, y0, z0);
                    BaseBlock block = clipboard.getFullBlock(point);
                    LinCompoundTag nbt = block.getNbt();
                    if (nbt != null) {
                        LinCompoundTag.Builder values = nbt.toBuilder();

                        values.remove("id"); // Remove 'id' if it exists. We want 'Id'

                        // Positions are kept in NBT, we don't want that.
                        values.remove("x");
                        values.remove("y");
                        values.remove("z");

                        values.putString("Id", block.getNbtId());
                        values.putIntArray("Pos", new int[] { x, y, z });

                        tileEntities.add(values.build());
                    }

                    String blockKey = block.toImmutableState().getAsString();
                    int blockId;
                    if (palette.containsKey(blockKey)) {
                        blockId = palette.getInt(blockKey);
                    } else {
                        blockId = paletteMax;
                        palette.put(blockKey, blockId);
                        paletteMax++;
                    }

                    while ((blockId & -128) != 0) {
                        buffer.write(blockId & 127 | 128);
                        blockId >>>= 7;
                    }
                    buffer.write(blockId);
                }
            }
        }

        schematic.putInt("PaletteMax", paletteMax);

        LinCompoundTag.Builder paletteTag = LinCompoundTag.builder();
        Object2IntMaps.fastForEach(palette, e -> paletteTag.putInt(e.getKey(), e.getIntValue()));

        schematic.put("Palette", paletteTag.build());
        schematic.putByteArray("BlockData", buffer.toByteArray());
        schematic.put("BlockEntities", tileEntities.build());

        // version 2 stuff
        if (clipboard.hasBiomes()) {
            writeBiomes(clipboard, schematic);
        }

        if (!clipboard.getEntities().isEmpty()) {
            LinListTag<LinCompoundTag> value = WriterUtil.encodeEntities(clipboard, false);
            if (value != null) {
                schematic.put("Entities", value);
            }
        }

        return schematic.build();
    }

    private void writeBiomes(Clipboard clipboard, LinCompoundTag.Builder schematic) {
        BlockVector3 min = clipboard.getMinimumPoint();
        int width = clipboard.getRegion().getWidth();
        int length = clipboard.getRegion().getLength();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * length);

        int paletteMax = 0;
        Object2IntMap<String> palette = new Object2IntLinkedOpenHashMap<>();

        for (int z = 0; z < length; z++) {
            int z0 = min.z() + z;
            for (int x = 0; x < width; x++) {
                int x0 = min.x() + x;
                BlockVector3 pt = BlockVector3.at(x0, min.y(), z0);
                BiomeType biome = clipboard.getBiome(pt);

                String biomeKey = biome.id();
                int biomeId;
                if (palette.containsKey(biomeKey)) {
                    biomeId = palette.getInt(biomeKey);
                } else {
                    biomeId = paletteMax;
                    palette.put(biomeKey, biomeId);
                    paletteMax++;
                }

                while ((biomeId & -128) != 0) {
                    buffer.write(biomeId & 127 | 128);
                    biomeId >>>= 7;
                }
                buffer.write(biomeId);
            }
        }

        schematic.putInt("BiomePaletteMax", paletteMax);

        LinCompoundTag.Builder paletteTag = LinCompoundTag.builder();
        Object2IntMaps.fastForEach(palette, e -> paletteTag.putInt(e.getKey(), e.getIntValue()));

        schematic.put("BiomePalette", paletteTag.build());
        schematic.putByteArray("BiomeData", buffer.toByteArray());
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
