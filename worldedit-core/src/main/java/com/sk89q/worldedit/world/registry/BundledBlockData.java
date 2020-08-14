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

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.gson.VectorAdapter;
import com.sk89q.worldedit.util.io.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

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
public final class BundledBlockData {

    private static final Logger log = LoggerFactory.getLogger(BundledBlockData.class);
    private static BundledBlockData INSTANCE;
    private final ResourceLoader resourceLoader;

    private final Map<String, BlockEntry> idMap = new HashMap<>();

    /**
     * Create a new instance.
     */
    private BundledBlockData() {
        this.resourceLoader = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION).getResourceLoader();

        try {
            loadFromResource();
        } catch (Throwable e) {
            log.warn("Failed to load the built-in block registry", e);
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
        Gson gson = gsonBuilder.create();
        URL url = null;
        final int dataVersion = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion();
        if (dataVersion >= Constants.DATA_VERSION_MC_1_16) {
            url = resourceLoader.getResource(BundledBlockData.class, "blocks.116.json");
        } else if (dataVersion >= Constants.DATA_VERSION_MC_1_15) {
            url = resourceLoader.getResource(BundledBlockData.class, "blocks.115.json");
        } else if (dataVersion >= Constants.DATA_VERSION_MC_1_14) {
            url = resourceLoader.getResource(BundledBlockData.class, "blocks.114.json");
        }
        if (url == null) {
            url = resourceLoader.getResource(BundledBlockData.class, "blocks.json");
        }
        if (url == null) {
            throw new IOException("Could not find blocks.json");
        }
        log.debug("Using {} for bundled block data.", url);
        String data = Resources.toString(url, Charset.defaultCharset());
        List<BlockEntry> entries = gson.fromJson(data, new TypeToken<List<BlockEntry>>() {}.getType());

        for (BlockEntry entry : entries) {
            idMap.put(entry.id, entry);
        }
    }

    /**
     * Return the entry for the given block ID.
     *
     * @param id the ID
     * @return the entry, or null
     */
    @Nullable
    public BlockEntry findById(String id) {
        // If it has no namespace, assume minecraft.
        if (!id.contains(":")) {
            id = "minecraft:" + id;
        }
        return idMap.get(id);
    }

    /**
     * Get the material properties for the given block.
     *
     * @param id the string ID
     * @return the material's properties, or null
     */
    @Nullable
    public BlockMaterial getMaterialById(String id) {
        BlockEntry entry = findById(id);
        if (entry != null) {
            return entry.material;
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
        if (INSTANCE == null) {
            INSTANCE = new BundledBlockData();
        }
        return INSTANCE;
    }

    public static class BlockEntry {
        private String id;
        public String localizedName;
        private final SimpleBlockMaterial material = new SimpleBlockMaterial();
    }

}
