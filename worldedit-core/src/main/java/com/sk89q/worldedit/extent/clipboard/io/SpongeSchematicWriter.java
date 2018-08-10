/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extent.clipboard.io;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes schematic files using the Sponge schematic format.
 */
public class SpongeSchematicWriter implements ClipboardWriter {

    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;
    private final NBTOutputStream outputStream;

    /**
     * Create a new schematic writer.
     *
     * @param outputStream the output stream to write to
     */
    public SpongeSchematicWriter(NBTOutputStream outputStream) {
        checkNotNull(outputStream);
        this.outputStream = outputStream;
    }

    @Override
    public void write(Clipboard clipboard) throws IOException {
        // For now always write the latest version. Maybe provide support for earlier if more appear.
        outputStream.writeNamedTag("Schematic", new CompoundTag(write1(clipboard)));
    }

    /**
     * Writes a version 1 schematic file.
     *
     * @param clipboard The clipboard
     * @return The schematic map
     * @throws IOException If an error occurs
     */
    private Map<String, Tag> write1(Clipboard clipboard) throws IOException {
        Region region = clipboard.getRegion();
        Vector origin = clipboard.getOrigin();
        Vector min = region.getMinimumPoint();
        Vector offset = min.subtract(origin);
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
        schematic.put("Version", new IntTag(1));

        Map<String, Tag> metadata = new HashMap<>();
        metadata.put("WEOffsetX", new IntTag(offset.getBlockX()));
        metadata.put("WEOffsetY", new IntTag(offset.getBlockY()));
        metadata.put("WEOffsetZ", new IntTag(offset.getBlockZ()));

        schematic.put("Metadata", new CompoundTag(metadata));

        schematic.put("Width", new ShortTag((short) width));
        schematic.put("Height", new ShortTag((short) height));
        schematic.put("Length", new ShortTag((short) length));

        // The Sponge format Offset refers to the 'min' points location in the world. That's our 'Origin'
        schematic.put("Offset", new IntArrayTag(new int[]{
                min.getBlockX(),
                min.getBlockY(),
                min.getBlockZ(),
        }));

        int paletteMax = 0;
        Map<String, Integer> palette = new HashMap<>();

        List<CompoundTag> tileEntities = new ArrayList<>();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * height * length);

        for (int y = 0; y < height; y++) {
            int y0 = min.getBlockY() + y;
            for (int z = 0; z < length; z++) {
                int z0 = min.getBlockZ() + z;
                for (int x = 0; x < width; x++) {
                    int x0 = min.getBlockX() + x;
                    BlockVector point = new BlockVector(x0, y0, z0);
                    BaseBlock block = clipboard.getFullBlock(point);
                    if (block.getNbtData() != null) {
                        Map<String, Tag> values = new HashMap<>();
                        for (Map.Entry<String, Tag> entry : block.getNbtData().getValue().entrySet()) {
                            values.put(entry.getKey(), entry.getValue());
                        }

                        values.remove("id"); // Remove 'id' if it exists. We want 'Id'

                        // Positions are kept in NBT, we don't want that.
                        values.remove("x");
                        values.remove("y");
                        values.remove("z");

                        values.put("Id", new StringTag(block.getNbtId()));
                        values.put("Pos", new IntArrayTag(new int[]{
                                x,
                                y,
                                z
                        }));

                        tileEntities.add(new CompoundTag(values));
                    }

                    String blockKey = block.toImmutableState().getAsString();
                    int blockId;
                    if (palette.containsKey(blockKey)) {
                        blockId = palette.get(blockKey);
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

        schematic.put("PaletteMax", new IntTag(paletteMax));

        Map<String, Tag> paletteTag = new HashMap<>();
        palette.forEach((key, value) -> paletteTag.put(key, new IntTag(value)));

        schematic.put("Palette", new CompoundTag(paletteTag));
        schematic.put("BlockData", new ByteArrayTag(buffer.toByteArray()));
        schematic.put("TileEntities", new ListTag(CompoundTag.class, tileEntities));

        return schematic;
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
