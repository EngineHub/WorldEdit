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
import java.util.function.Function;

/**
 * Writes schematic files using the Sponge Schematic Specification (Version 3).
 */
public class SpongeSchematicV3Writer implements ClipboardWriter {

    private static final int CURRENT_VERSION = 3;

    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;
    private final DataOutputStream outputStream;

    public SpongeSchematicV3Writer(DataOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    public void write(Clipboard clipboard) throws IOException {
        LinBinaryIO.write(
            outputStream,
            new LinRootEntry(
                "",
                LinCompoundTag.builder()
                .put("Schematic", write3(clipboard))
                .build()
            )
        );
    }

    /**
     * Writes a version 3 schematic file.
     *
     * @param clipboard The clipboard
     * @return The schematic map
     */
    private LinCompoundTag write3(Clipboard clipboard) {
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
        metadata.putLong("Date", System.currentTimeMillis());

        LinCompoundTag.Builder worldEditSection = LinCompoundTag.builder();
        worldEditSection.putString("Version", WorldEdit.getVersion());
        worldEditSection.putString(
            "EditingPlatform",
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getId()
        );
        worldEditSection.putIntArray("Origin", new int[] {
            origin.getBlockX(), origin.getBlockY(), origin.getBlockZ()
        });

        LinCompoundTag.Builder platformsSection = LinCompoundTag.builder();
        for (Platform platform : WorldEdit.getInstance().getPlatformManager().getPlatforms()) {
            platformsSection.put(
                platform.getId(),
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

        schematic.putIntArray("Offset", new int[] {
            offset.getBlockX(),
            offset.getBlockY(),
            offset.getBlockZ(),
        });

        schematic.put("Blocks", encodeBlocks(clipboard));

        if (clipboard.hasBiomes()) {
            schematic.put("Biomes", encodeBiomes(clipboard));
        }

        if (!clipboard.getEntities().isEmpty()) {
            LinListTag<LinCompoundTag> value = WriterUtil.encodeEntities(clipboard, true);
            if (value != null) {
                schematic.put("Entities", value);
            }
        }

        return schematic.build();
    }

    private static final class PaletteMap {
        private final Object2IntMap<String> contents = new Object2IntLinkedOpenHashMap<>();
        private int nextId = 0;

        public int getId(String key) {
            int result = contents.getOrDefault(key, -1);
            if (result != -1) {
                return result;
            }
            int newValue = nextId;
            nextId++;
            contents.put(key, newValue);
            return newValue;
        }

        public LinCompoundTag toNbt() {
            LinCompoundTag.Builder result = LinCompoundTag.builder();
            Object2IntMaps.fastForEach(contents, e -> result.putInt(e.getKey(), e.getIntValue()));
            return result.build();
        }
    }

    private LinCompoundTag encodeBlocks(Clipboard clipboard) {
        LinListTag.Builder<LinCompoundTag> blockEntities = LinListTag.builder(LinTagType.compoundTag());
        LinCompoundTag.Builder result = encodePalettedData(clipboard, point -> {
            BaseBlock block = clipboard.getFullBlock(point);
            // Also compute block entity side-effect here
            LinCompoundTag nbt = block.getNbt();
            if (nbt != null) {
                LinCompoundTag.Builder builder = LinCompoundTag.builder();

                builder.putString("Id", block.getNbtId());
                BlockVector3 adjustedPos = point.subtract(clipboard.getMinimumPoint());
                builder.putIntArray("Pos", new int[] {
                    adjustedPos.getBlockX(),
                    adjustedPos.getBlockY(),
                    adjustedPos.getBlockZ()
                });
                builder.put("Data", nbt);

                blockEntities.add(builder.build());
            }
            return block.toImmutableState().getAsString();
        });

        return result
            .put("BlockEntities", blockEntities.build())
            .build();
    }

    private LinCompoundTag encodeBiomes(Clipboard clipboard) {
        return encodePalettedData(clipboard, point -> clipboard.getBiome(point).getId()).build();
    }

    private LinCompoundTag.Builder encodePalettedData(Clipboard clipboard,
                                           Function<BlockVector3, String> keyFunction) {
        BlockVector3 min = clipboard.getMinimumPoint();
        int width = clipboard.getRegion().getWidth();
        int height = clipboard.getRegion().getHeight();
        int length = clipboard.getRegion().getLength();

        PaletteMap paletteMap = new PaletteMap();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);

        for (int y = 0; y < height; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    BlockVector3 point = min.add(x, y, z);

                    String key = keyFunction.apply(point);
                    int id = paletteMap.getId(key);

                    while ((id & -128) != 0) {
                        buffer.write(id & 127 | 128);
                        id >>>= 7;
                    }
                    buffer.write(id);
                }
            }
        }

        return LinCompoundTag.builder()
            .put("Palette", paletteMap.toNbt())
            .putByteArray("Data", buffer.toByteArray());
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
