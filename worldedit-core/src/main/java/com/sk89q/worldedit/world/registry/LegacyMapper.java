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

package com.sk89q.worldedit.world.registry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.util.gson.VectorAdapter;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

public class LegacyMapper {

    private static final Logger log = Logger.getLogger(LegacyMapper.class.getCanonicalName());
    private static LegacyMapper INSTANCE;

    private Multimap<String, BlockState> stringToBlockMap = HashMultimap.create();
    private Multimap<BlockState, String> blockToStringMap = HashMultimap.create();
    private Multimap<String, ItemType> stringToItemMap = HashMultimap.create();
    private Multimap<ItemType, String> itemToStringMap = HashMultimap.create();

    /**
     * Create a new instance.
     */
    private LegacyMapper() {
        try {
            loadFromResource();
        } catch (Throwable e) {
            log.log(Level.WARNING, "Failed to load the built-in legacy id registry", e);
        }
    }

    /**
     * Attempt to load the data from file.
     *
     * @throws IOException thrown on I/O error
     */
    private void loadFromResource() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Vector.class, new VectorAdapter());
        Gson gson = gsonBuilder.disableHtmlEscaping().create();
        URL url = LegacyMapper.class.getResource("legacy.json");
        if (url == null) {
            throw new IOException("Could not find legacy.json");
        }
        String data = Resources.toString(url, Charset.defaultCharset());
        LegacyDataFile dataFile = gson.fromJson(data, new TypeToken<LegacyDataFile>() {}.getType());

        ParserContext parserContext = new ParserContext();
        parserContext.setPreferringWildcard(false);
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false); // This is legacy. Don't match itself.

        for (Map.Entry<String, String> blockEntry : dataFile.blocks.entrySet()) {
            try {
                String id = blockEntry.getKey();
                BlockState state = WorldEdit.getInstance().getBlockFactory().parseFromInput(blockEntry.getValue(), parserContext).toImmutableState();
                blockToStringMap.put(state, id);
                stringToBlockMap.put(id, state);
            } catch (Exception e) {
                log.warning("Unknown block: " + blockEntry.getValue());
            }
        }

        for (Map.Entry<String, String> itemEntry : dataFile.items.entrySet()) {
            try {
                String id = itemEntry.getKey();
                ItemType type = ItemTypes.get(itemEntry.getValue());
                itemToStringMap.put(type, id);
                stringToItemMap.put(id, type);
            } catch (Exception e) {
                log.warning("Unknown item: " + itemEntry.getValue());
            }
        }
    }

    @Nullable
    public ItemType getItemFromLegacy(int legacyId) {
        return getItemFromLegacy(legacyId, 0);
    }

    @Nullable
    public ItemType getItemFromLegacy(int legacyId, int data) {
        return stringToItemMap.get(legacyId + ":" + data).stream().findFirst().orElse(null);
    }

    @Nullable
    public int[] getLegacyFromItem(ItemType itemType) {
        if (!itemToStringMap.containsKey(itemType)) {
            return null;
        } else {
            String value = itemToStringMap.get(itemType).stream().findFirst().get();
            return Arrays.stream(value.split(":")).mapToInt(Integer::parseInt).toArray();
        }
    }

    @Nullable
    public BlockState getBlockFromLegacy(int legacyId) {
        return getBlockFromLegacy(legacyId, 0);
    }

    @Nullable
    public BlockState getBlockFromLegacy(int legacyId, int data) {
        return stringToBlockMap.get(legacyId + ":" + data).stream().findFirst().orElse(null);
    }

    @Nullable
    public int[] getLegacyFromBlock(BlockState blockState) {
        if (!blockToStringMap.containsKey(blockState)) {
            return null;
        } else {
            String value = blockToStringMap.get(blockState).stream().findFirst().get();
            return Arrays.stream(value.split(":")).mapToInt(Integer::parseInt).toArray();
        }
    }

    public static LegacyMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LegacyMapper();
        }
        return INSTANCE;
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
    private static class LegacyDataFile {
        private Map<String, String> blocks;
        private Map<String, String> items;
    }
}
