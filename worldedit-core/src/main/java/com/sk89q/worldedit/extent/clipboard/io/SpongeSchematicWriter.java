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

package com.sk89q.worldedit.extent.clipboard.io;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Writes schematic files using the Sponge schematic format.
 */
public class SpongeSchematicWriter implements ClipboardWriter {

    private static final int CURRENT_VERSION = 2;

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
        outputStream.writeNamedTag("Schematic", new CompoundTag(write2(clipboard)));
    }

    /**
     * Writes a version 2 schematic file.
     *
     * @param clipboard The clipboard
     * @return The schematic map
     */
    private Map<String, Tag> write2(Clipboard clipboard) {
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
        metadata.put("WEOffsetX", new IntTag(offset.getBlockX()));
        metadata.put("WEOffsetY", new IntTag(offset.getBlockY()));
        metadata.put("WEOffsetZ", new IntTag(offset.getBlockZ()));

        Map<String, Tag> worldEditSection = new HashMap<>();
        worldEditSection.put("Version", new StringTag(WorldEdit.getVersion()));
        worldEditSection.put("EditingPlatform", new StringTag(WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getId()));
        worldEditSection.put("Offset", new IntArrayTag(new int[]{offset.getBlockX(), offset.getBlockY(), offset.getBlockZ()}));

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
                    BlockVector3 point = BlockVector3.at(x0, y0, z0);
                    BaseBlock block = clipboard.getFullBlock(point);
                    if (block.getNbtData() != null) {
                        Map<String, Tag> values = new HashMap<>(block.getNbtData().getValue());

                        values.remove("id"); // Remove 'id' if it exists. We want 'Id'

                        // Positions are kept in NBT, we don't want that.
                        values.remove("x");
                        values.remove("y");
                        values.remove("z");

                        values.put("Id", new StringTag(block.getNbtId()));
                        values.put("Pos", new IntArrayTag(new int[] { x, y, z }));

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
        schematic.put("BlockEntities", new ListTag(CompoundTag.class, tileEntities));

        // version 2 stuff
        if (clipboard.hasBiomes()) {
            writeBiomes(clipboard, schematic);
        }

        if (!clipboard.getEntities().isEmpty()) {
            writeEntities(clipboard, schematic);
        }

        return schematic;
    }

    private void writeBiomes(Clipboard clipboard, Map<String, Tag> schematic) {
        BlockVector3 min = clipboard.getMinimumPoint();
        int width = clipboard.getRegion().getWidth();
        int length = clipboard.getRegion().getLength();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(width * length);

        int paletteMax = 0;
        Map<String, Integer> palette = new HashMap<>();

        for (int z = 0; z < length; z++) {
            int z0 = min.getBlockZ() + z;
            for (int x = 0; x < width; x++) {
                int x0 = min.getBlockX() + x;
                BlockVector3 pt = BlockVector3.at(x0, min.getBlockY(), z0);
                BiomeType biome = clipboard.getBiome(pt);

                String biomeKey = biome.getId();
                int biomeId;
                if (palette.containsKey(biomeKey)) {
                    biomeId = palette.get(biomeKey);
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

        schematic.put("BiomePaletteMax", new IntTag(paletteMax));

        Map<String, Tag> paletteTag = new HashMap<>();
        palette.forEach((key, value) -> paletteTag.put(key, new IntTag(value)));

        schematic.put("BiomePalette", new CompoundTag(paletteTag));
        schematic.put("BiomeData", new ByteArrayTag(buffer.toByteArray()));
    }

    private void writeEntities(Clipboard clipboard, Map<String, Tag> schematic) {
        List<CompoundTag> entities = clipboard.getEntities().stream().map(e -> {
            BaseEntity state = e.getState();
            if (state == null) {
                return null;
            }
            Map<String, Tag> values = Maps.newHashMap();
            CompoundTag rawData = state.getNbtData();
            if (rawData != null) {
                values.putAll(rawData.getValue());
            }
            values.remove("id");
            values.put("Id", new StringTag(state.getType().getId()));
            final Location location = e.getLocation();
            values.put("Pos", writeVector(location.toVector()));
            values.put("Rotation", writeRotation(location));

            return new CompoundTag(values);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (entities.isEmpty()) {
            return;
        }
        schematic.put("Entities", new ListTag(CompoundTag.class, entities));
    }

    private Tag writeVector(Vector3 vector) {
        List<DoubleTag> list = new ArrayList<>();
        list.add(new DoubleTag(vector.getX()));
        list.add(new DoubleTag(vector.getY()));
        list.add(new DoubleTag(vector.getZ()));
        return new ListTag(DoubleTag.class, list);
    }

    private Tag writeRotation(Location location) {
        List<FloatTag> list = new ArrayList<>();
        list.add(new FloatTag(location.getYaw()));
        list.add(new FloatTag(location.getPitch()));
        return new ListTag(FloatTag.class, list);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
