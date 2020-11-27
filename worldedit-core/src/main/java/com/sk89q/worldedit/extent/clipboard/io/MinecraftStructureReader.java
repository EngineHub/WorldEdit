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
 *
 */

package com.sk89q.worldedit.extent.clipboard.io;

import com.google.common.collect.Maps;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.storage.NBTConversions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads Mincraft structure files.
 * {@link "https://minecraft.gamepedia.com/Structure_block_file_format"}
 */
public class MinecraftStructureReader extends NBTSchematicReader {
    private static final Logger log = Logger.getLogger(MinecraftStructureReader.class.getCanonicalName());
    private final NBTInputStream inputStream;

    /**
     * Create a new instance.
     *
     * @param inputStream the input stream to read from
     */
    public MinecraftStructureReader(NBTInputStream inputStream) {
        checkNotNull(inputStream);
        this.inputStream = inputStream;
    }

    @Override
    public Clipboard read() throws IOException {
        NamedTag rootTag = inputStream.readNamedTag();

        // MC structures are all unnamed, but this doesn't seem to be necessary? might remove this later
//        if (!rootTag.getName().isEmpty()) {
//            throw new IOException("Root tag has name - are you sure this is a structure?");
//        }

        CompoundTag structureTag = (CompoundTag) rootTag.getTag();
        Map<String, Tag> structure = structureTag.getValue();
        int version = requireTag(structure, "DataVersion", IntTag.class).getValue();

        // TODO DFU

        List<Tag> size = requireTag(structure, "size", ListTag.class).getValue();
        if (size.size() != 3) {
            throw new IOException("Invalid size list tag in structure.");
        }
        int width = ((IntTag) size.get(0)).getValue();
        int height = ((IntTag) size.get(1)).getValue();
        int length = ((IntTag) size.get(2)).getValue();

        if (structure.containsKey("palettes")) {
            throw new IOException("Structures with multiple palettes are not currently supported.");
        }
        List<CompoundTag> paletteObject = (List<CompoundTag>) (List<?>) requireTag(structure, "palette", ListTag.class).getValue();
        Map<Integer, BlockState> palette = readPalette(paletteObject);

        return getClipboardWithPalette(structure, palette, width, height, length);
    }

    private BlockArrayClipboard getClipboardWithPalette(Map<String, Tag> structure, Map<Integer, BlockState> palette,
                                                        int width, int height, int length) throws IOException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(
            new CuboidRegion(BlockVector3.ZERO, BlockVector3.at(width, height, length).subtract(BlockVector3.ONE)));

        for (Tag blockTag : requireTag(structure, "blocks", ListTag.class).getValue()) {
            CompoundTag block = (CompoundTag) blockTag;
            ListTag pos = requireTag(block.getValue(), "pos", ListTag.class);
            BlockVector3 vec = BlockVector3.at(pos.getInt(0), pos.getInt(1), pos.getInt(2));
            int state = requireTag(block.getValue(), "state", IntTag.class).getValue();
            BlockState blockState = palette.get(state);
            CompoundTag nbt = getTag(block.getValue(), "nbt", CompoundTag.class);
            try {
                if (nbt == null) {
                    clipboard.setBlock(vec, blockState);
                } else {
                    Map<String, Tag> values = Maps.newHashMap(nbt.getValue());
                    values.put("x", new IntTag(vec.getBlockX()));
                    values.put("y", new IntTag(vec.getBlockY()));
                    values.put("z", new IntTag(vec.getBlockZ()));
                    BaseBlock baseBlock = blockState.toBaseBlock(nbt);
                    clipboard.setBlock(vec, baseBlock);
                }
            } catch (WorldEditException e) {
                throw new IOException("Failed to load a block in the schematic");
            }
        }

        List<Tag> entityTags = requireTag(structure, "entities", ListTag.class).getValue();
        for (Tag tag : entityTags) {
            CompoundTag compound = (CompoundTag) tag;
            CompoundTag nbt = requireTag(compound.getValue(), "nbt", CompoundTag.class);
            Location location = NBTConversions.toLocation(clipboard, compound.getListTag("pos"),
                nbt.getListTag("Rotation"));
            String id = requireTag(nbt.getValue(), "id", StringTag.class).getValue();

            if (!id.isEmpty()) {
                EntityType entityType = EntityTypes.get(id.toLowerCase());
                if (entityType != null) {
                    BaseEntity state = new BaseEntity(entityType, compound);
                    clipboard.createEntity(location, state);
                } else {
                    log.warning("Unknown entity when loading structure: " + id.toLowerCase());
                }
            }
        }
        return clipboard;
    }

    private Map<Integer, BlockState> readPalette(List<CompoundTag> paletteTag) throws IOException {
        Map<Integer, BlockState> palette = new HashMap<>();
        ParserContext parserContext = new ParserContext();
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false);
        parserContext.setPreferringWildcard(false);

        for (int i = 0; i < paletteTag.size(); i++) {
            CompoundTag palettePart = paletteTag.get(i);
            String blockName = palettePart.getString("Name");
            if (blockName.isEmpty()) {
                throw new IOException("Palette block name empty.");
            }
            StringBuilder stateBuilder = new StringBuilder(blockName);
            Tag props = palettePart.getValue().getOrDefault("Properties", null);
            if (props instanceof CompoundTag) {
                CompoundTag properties = ((CompoundTag) props);
                if (!properties.getValue().isEmpty()) {
                    stateBuilder.append('[');
                    stateBuilder.append(properties.getValue().entrySet().stream().map(e ->
                        e.getKey() + "=" + e.getValue().getValue()).collect(Collectors.joining(",")));
                    stateBuilder.append(']');
                }
            }
            BlockState state;
            String stateString = stateBuilder.toString();
            try {
                state = WorldEdit.getInstance().getBlockFactory().parseFromInput(stateString, parserContext).toImmutableState();
            } catch (InputParseException e) {
                throw new IOException("Invalid BlockState in structure: " + stateString +
                    ". Are you missing a mod or using a structure made in a newer version of Minecraft?");
            }
            palette.put(i, state);
        }
        return palette;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}