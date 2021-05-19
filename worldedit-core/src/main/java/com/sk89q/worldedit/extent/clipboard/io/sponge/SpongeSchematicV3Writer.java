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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Writes schematic files using the Sponge Schematic Specification (Version 3).
 */
public class SpongeSchematicV3Writer implements ClipboardWriter {

    private static final int CURRENT_VERSION = 3;

    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;
    private final NBTOutputStream outputStream;

    /**
     * Create a new schematic writer.
     *
     * @param outputStream the output stream to write to
     */
    public SpongeSchematicV3Writer(NBTOutputStream outputStream) {
        checkNotNull(outputStream);
        this.outputStream = outputStream;
    }

    @Override
    public void write(Clipboard clipboard) throws IOException {
        // For now always write the latest version. Maybe provide support for earlier if more appear.
        outputStream.writeNamedTag("",
            new CompoundTag(ImmutableMap.of("Schematic", new CompoundTag(write3(clipboard))))
        );
    }

    /**
     * Writes a version 3 schematic file.
     *
     * @param clipboard The clipboard
     * @return The schematic map
     */
    private Map<String, Tag> write3(Clipboard clipboard) {
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

        Map<String, Tag> schematic = new HashMap<>();
        schematic.put("Version", new IntTag(CURRENT_VERSION));
        schematic.put("DataVersion", new IntTag(
            WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion()));

        Map<String, Tag> metadata = new HashMap<>();
        metadata.put("Date", new LongTag(System.currentTimeMillis()));

        Map<String, Tag> worldEditSection = new HashMap<>();
        worldEditSection.put("Version", new StringTag(WorldEdit.getVersion()));
        worldEditSection.put("EditingPlatform", new StringTag(WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getId()));
        worldEditSection.put("Origin", new IntArrayTag(new int[] {
            origin.getBlockX(), origin.getBlockY(), origin.getBlockZ()
        }));

        Map<String, Tag> platformsSection = new HashMap<>();
        for (Platform platform : WorldEdit.getInstance().getPlatformManager().getPlatforms()) {
            platformsSection.put(platform.getId(), new CompoundTag(ImmutableMap.of(
                "Name", new StringTag(platform.getPlatformName()),
                "Version", new StringTag(platform.getPlatformVersion())
            )));
        }
        worldEditSection.put("Platforms", new CompoundTag(platformsSection));

        metadata.put("WorldEdit", new CompoundTag(worldEditSection));

        schematic.put("Metadata", new CompoundTag(metadata));

        schematic.put("Width", new ShortTag((short) width));
        schematic.put("Height", new ShortTag((short) height));
        schematic.put("Length", new ShortTag((short) length));

        schematic.put("Offset", new IntArrayTag(new int[] {
            offset.getBlockX(),
            offset.getBlockY(),
            offset.getBlockZ(),
        }));

        schematic.put("Blocks", encodeBlocks(clipboard));

        if (clipboard.hasBiomes()) {
            schematic.put("Biomes", encodeBiomes(clipboard));
        }

        if (!clipboard.getEntities().isEmpty()) {
            ListTag value = WriterUtil.encodeEntities(clipboard, true);
            if (value != null) {
                schematic.put("Entities", value);
            }
        }

        return schematic;
    }

    private static final class PaletteMap {
        private final Map<String, Integer> contents = new LinkedHashMap<>();
        private int nextId = 0;

        public int getId(String key) {
            Integer result = contents.get(key);
            if (result != null) {
                return result;
            }
            int newValue = nextId;
            nextId++;
            contents.put(key, newValue);
            return newValue;
        }

        public CompoundTag toNbt() {
            return new CompoundTag(ImmutableMap.copyOf(Maps.transformValues(
                contents, IntTag::new
            )));
        }
    }

    private CompoundTag encodeBlocks(Clipboard clipboard) {
        List<CompoundTag> blockEntities = new ArrayList<>();
        CompoundTag result = encodePalettedData(clipboard, point -> {
            BaseBlock block = clipboard.getFullBlock(point);
            // Also compute block entity side-effect here
            if (block.getNbtData() != null) {
                Map<String, Tag> values = new HashMap<>(block.getNbtData().getValue());

                values.remove("id"); // Remove 'id' if it exists. We want 'Id'

                // Positions are kept in NBT, we don't want that.
                values.remove("x");
                values.remove("y");
                values.remove("z");

                values.put("Id", new StringTag(block.getNbtId()));
                BlockVector3 adjustedPos = point.subtract(clipboard.getMinimumPoint());
                values.put("Pos", new IntArrayTag(new int[] {
                    adjustedPos.getBlockX(),
                    adjustedPos.getBlockY(),
                    adjustedPos.getBlockZ()
                }));

                blockEntities.add(new CompoundTag(values));
            }
            return block.toImmutableState().getAsString();
        });

        return result.createBuilder()
            .put("BlockEntities", new ListTag(CompoundTag.class, blockEntities))
            .build();
    }

    private CompoundTag encodeBiomes(Clipboard clipboard) {
        return encodePalettedData(clipboard, point -> clipboard.getBiome(point).getId());
    }

    private CompoundTag encodePalettedData(Clipboard clipboard,
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

        return new CompoundTag(ImmutableMap.of(
            "Palette", paletteMap.toNbt(),
            "Data", new ByteArrayTag(buffer.toByteArray())
        ));
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
