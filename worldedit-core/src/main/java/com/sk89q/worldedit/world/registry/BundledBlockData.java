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

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockMaterial;
import com.sk89q.worldedit.util.gson.VectorAdapter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides block data based on the built-in block database that is bundled
 * with WorldEdit.
 *
 * <p>A new instance cannot be created. Use {@link #getInstance()} to get
 * an instance.</p>
 *
 * <p>The data is read from a JSON file that is bundled with WorldEdit. If
 * reading fails (which occurs when this class is first instantiated), then
 * the methods will return {@code null}s for all blocks.</p>
 */
public class BundledBlockData {

    private static final Logger log = Logger.getLogger(BundledBlockData.class.getCanonicalName());
    private static final BundledBlockData INSTANCE = new BundledBlockData();

    private final Map<String, BlockEntry> idMap = new HashMap<String, BlockEntry>();
    private final Map<Integer, BlockEntry> legacyMap = new HashMap<Integer, BlockEntry>(); // Trove usage removed temporarily

    /**
     * Create a new instance.
     */
    private BundledBlockData() {
        try {
            loadFromResource();
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to load the built-in block registry", e);
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
        Gson gson = gsonBuilder.create();
        URL url = BundledBlockData.class.getResource("blocks.json");
        if (url == null) {
            throw new IOException("Could not find blocks.json");
        }
        String data = Resources.toString(url, Charset.defaultCharset());
        List<BlockEntry> entries = gson.fromJson(data, new TypeToken<List<BlockEntry>>() {}.getType());

        for (BlockEntry entry : entries) {
            entry.postDeserialization();
            idMap.put(entry.id, entry);
            legacyMap.put(entry.legacyId, entry);
        }
    }

    /**
     * Return the entry for the given block ID.
     *
     * @param id the ID
     * @return the entry, or null
     */
    @Nullable
    private BlockEntry findById(String id) {
        return idMap.get(id);
    }

    /**
     * Return the entry for the given block legacy numeric ID.
     *
     * @param id the ID
     * @return the entry, or null
     */
    @Nullable
    private BlockEntry findById(int id) {
        return legacyMap.get(id);
    }

    /**
     * Convert the given string ID to a legacy numeric ID.
     *
     * @param id the ID
     * @return the legacy ID, which may be null if the block does not have a legacy ID
     */
    @Nullable
    public Integer toLegacyId(String id) {
        BlockEntry entry = findById(id);
        if (entry != null) {
            return entry.legacyId;
        } else {
            return null;
        }
    }

    /**
     * Get the material properties for the given block.
     *
     * @param id the legacy numeric ID
     * @return the material's properties, or null
     */
    @Nullable
    public BlockMaterial getMaterialById(int id) {
        BlockEntry entry = findById(id);
        if (entry != null) {
            return entry.material;
        } else {
            return null;
        }
    }

    /**
     * Get the states for the given block.
     *
     * @param id the legacy numeric ID
     * @return the block's states, or null if no information is available
     */
    @Nullable
    public Map<String, ? extends State> getStatesById(int id) {
        BlockEntry entry = findById(id);
        if (entry != null) {
            return entry.states;
        } else {
            return null;
        }
    }

    /**
     * Get a singleton instance of this object.
     *
     * @return the instance
     */
    public static BundledBlockData getInstance() {
        return INSTANCE;
    }

    private static class BlockEntry {
        private int legacyId;
        private String id;
        private String unlocalizedName;
        private List<String> aliases;
        private Map<String, SimpleState> states = new HashMap<String, SimpleState>();
        private SimpleBlockMaterial material = new SimpleBlockMaterial();

        void postDeserialization() {
            for (SimpleState state : states.values()) {
                state.postDeserialization();
            }
        }
    }

}
