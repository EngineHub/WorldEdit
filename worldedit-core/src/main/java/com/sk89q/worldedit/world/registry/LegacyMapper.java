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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.util.gson.VectorAdapter;

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
    private static final LegacyMapper INSTANCE = new LegacyMapper();

    private BiMap<String, BlockState> blockMap = HashBiMap.create();
    private BiMap<String, ItemType> itemMap = HashBiMap.create();

    /**
     * Create a new instance.
     */
    private LegacyMapper() {
        try {
            loadFromResource();
        } catch (IOException e) {
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

        for (Map.Entry<String, String> blockEntry : dataFile.blocks.entrySet()) {
            try {
                blockMap.put(blockEntry.getKey(),
                        (BlockState) WorldEdit.getInstance().getBlockFactory().parseFromInput(blockEntry.getValue(), parserContext));
            } catch (Exception e) {
                log.warning("Unknown block: " + blockEntry.getValue());
            }
        }

        for (Map.Entry<String, String> itemEntry : dataFile.items.entrySet()) {
            try {
                itemMap.put(itemEntry.getKey(), ItemTypes.get(itemEntry.getValue()));
            } catch (Exception e) {
                log.warning("Unknown item: " + itemEntry.getValue());
            }
        }
    }

    @Nullable
    public ItemType getItemFromLegacy(int legacyId) {
        return itemMap.get(legacyId + ":0");
    }

    @Nullable
    public ItemType getItemFromLegacy(int legacyId, int data) {
        return itemMap.get(legacyId + ":" + data);
    }

    @Nullable
    public int[] getLegacyFromItem(ItemType itemType) {
        if (!itemMap.inverse().containsKey(itemType)) {
            return null;
        } else {
            String value = itemMap.inverse().get(itemType);
            return Arrays.stream(value.split(":")).mapToInt(Integer::parseInt).toArray();
        }
    }

    @Nullable
    public BlockState getBlockFromLegacy(int legacyId) {
        return blockMap.get(legacyId + ":0");
    }

    @Nullable
    public BlockState getBlockFromLegacy(int legacyId, int data) {
        return blockMap.get(legacyId + ":" + data);
    }

    @Nullable
    public int[] getLegacyFromBlock(BlockState blockState) {
        if (!blockMap.inverse().containsKey(blockState)) {
            return null;
        } else {
            String value = blockMap.inverse().get(blockState);
            return Arrays.stream(value.split(":")).mapToInt(Integer::parseInt).toArray();
        }
    }

    public static LegacyMapper getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unused"})
    private static class LegacyDataFile {
        private Map<String, String> blocks;
        private Map<String, String> items;
    }
}
