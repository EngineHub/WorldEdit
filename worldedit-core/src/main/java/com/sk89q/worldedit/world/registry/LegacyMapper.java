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

package com.sk89q.worldedit.world.registry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.BlockFactory;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.gson.VectorAdapter;
import com.sk89q.worldedit.util.io.ResourceLoader;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public final class LegacyMapper {

    private static final Logger log = LoggerFactory.getLogger(LegacyMapper.class);
    private static LegacyMapper INSTANCE;
    private final ResourceLoader resourceLoader;

    private final Map<String, BlockState> stringToBlockMap = new HashMap<>();
    private final Multimap<BlockState, String> blockToStringMap = HashMultimap.create();
    private final Map<String, ItemType> stringToItemMap = new HashMap<>();
    private final Multimap<ItemType, String> itemToStringMap = HashMultimap.create();

    /**
     * Create a new instance.
     */
    private LegacyMapper() {
        this.resourceLoader = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION).getResourceLoader();

        try {
            loadFromResource();
        } catch (Throwable e) {
            log.warn("Failed to load the built-in legacy id registry", e);
        }
    }

    /**
     * Attempt to load the data from file.
     *
     * @throws IOException thrown on I/O error
     */
    private void loadFromResource() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Vector3.class, new VectorAdapter());
        Gson gson = gsonBuilder.disableHtmlEscaping().create();
        URL url = resourceLoader.getResource(LegacyMapper.class, "legacy.json");
        if (url == null) {
            throw new IOException("Could not find legacy.json");
        }
        String data = Resources.toString(url, Charset.defaultCharset());
        LegacyDataFile dataFile = gson.fromJson(data, new TypeToken<LegacyDataFile>() {}.getType());

        DataFixer fixer = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataFixer();
        ParserContext parserContext = new ParserContext();
        parserContext.setPreferringWildcard(false);
        parserContext.setRestricted(false);
        parserContext.setTryLegacy(false); // This is legacy. Don't match itself.

        for (Map.Entry<String, String> blockEntry : dataFile.blocks.entrySet()) {
            String id = blockEntry.getKey();
            final String value = blockEntry.getValue();

            BlockState state = null;
            BlockFactory blockFactory = WorldEdit.getInstance().getBlockFactory();

            // if fixer is available, try using that first, as some old blocks that were renamed share names with new blocks
            if (fixer != null) {
                try {
                    String newEntry = fixer.fixUp(DataFixer.FixTypes.BLOCK_STATE, value, Constants.DATA_VERSION_MC_1_13_2);
                    state = blockFactory.parseFromInput(newEntry, parserContext).toImmutableState();
                } catch (InputParseException ignored) {
                }
            }

            // if it's still null, the fixer was unavailable or failed
            if (state == null) {
                try {
                    state = blockFactory.parseFromInput(value, parserContext).toImmutableState();
                } catch (InputParseException ignored) {
                }
            }

            // if it's still null, both fixer and default failed
            if (state == null) {
                log.debug("Unknown block: " + value);
            } else {
                // it's not null so one of them succeeded, now use it
                blockToStringMap.put(state, id);
                stringToBlockMap.put(id, state);
            }
        }

        for (Map.Entry<String, String> itemEntry : dataFile.items.entrySet()) {
            String id = itemEntry.getKey();
            String value = itemEntry.getValue();
            ItemType type = ItemTypes.get(value);
            if (type == null && fixer != null) {
                value = fixer.fixUp(DataFixer.FixTypes.ITEM_TYPE, value, Constants.DATA_VERSION_MC_1_13_2);
                type = ItemTypes.get(value);
            }
            if (type == null) {
                log.debug("Unknown item: " + value);
            } else {
                itemToStringMap.put(type, id);
                stringToItemMap.put(id, type);
            }
        }
    }

    @Nullable
    public ItemType getItemFromLegacy(int legacyId) {
        return getItemFromLegacy(legacyId, 0);
    }

    @Nullable
    public ItemType getItemFromLegacy(int legacyId, int data) {
        return stringToItemMap.get(legacyId + ":" + data);
    }

    @Nullable
    public int[] getLegacyFromItem(ItemType itemType) {
        if (itemToStringMap.containsKey(itemType)) {
            String value = itemToStringMap.get(itemType).stream().findFirst().get();
            return Arrays.stream(value.split(":")).mapToInt(Integer::parseInt).toArray();
        } else {
            return null;
        }
    }

    @Nullable
    public BlockState getBlockFromLegacy(int legacyId) {
        return getBlockFromLegacy(legacyId, 0);
    }

    @Nullable
    public BlockState getBlockFromLegacy(int legacyId, int data) {
        return stringToBlockMap.get(legacyId + ":" + data);
    }

    @Nullable
    public int[] getLegacyFromBlock(BlockState blockState) {
        if (blockToStringMap.containsKey(blockState)) {
            String value = blockToStringMap.get(blockState).stream().findFirst().get();
            return Arrays.stream(value.split(":")).mapToInt(Integer::parseInt).toArray();
        } else {
            return null;
        }
    }

    public static LegacyMapper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LegacyMapper();
        }
        return INSTANCE;
    }

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private static class LegacyDataFile {
        private Map<String, String> blocks;
        private Map<String, String> items;
    }
}
