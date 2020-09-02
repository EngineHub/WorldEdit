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

package com.sk89q.worldedit.util.assets;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.file.FilenameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Abstract manager to lazy-load and temporarily cache custom assets.
 *
 * @param <T> type the be loaded
 */
public abstract class AssetManager<T> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private final Cache<String, T> assets = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private final WorldEdit worldEdit;
    private final String[] assetExtensions;
    private final String defaultExtension;
    private final String assetDirectoryName;

    /**
     * Creates a new AssetManager to load and cache custom assets.
     *
     * @param worldEdit          worldedit instance
     * @param assetDirectoryName subdirectory of the platform's WorldEdit dir
     * @param defaultExtension   standard file extension associated with the asset
     * @param assetExtensions    all possible file extentions associated with the asset
     */
    protected AssetManager(WorldEdit worldEdit, String assetDirectoryName, String defaultExtension, String... assetExtensions) {
        this.worldEdit = worldEdit;
        this.assetDirectoryName = assetDirectoryName;
        this.defaultExtension = defaultExtension;
        this.assetExtensions = assetExtensions;
    }

    /**
     * Loads an asset.
     *
     * @param path path in assets directory, can be with and without its file extension
     * @return asset if successfully loaded, null otherwise
     */
    @Nullable
    public T getAsset(String path) {
        // Remove file extension from mapped name
        int extensionIndex = path.lastIndexOf('.');
        String assetName = extensionIndex != -1 ? path.substring(0, extensionIndex) : path;
        assetName = assetName.toLowerCase(Locale.ROOT);

        T cached = assets.getIfPresent(assetName);
        if (cached != null) {
            return cached;
        }

        File assetsDir = new File(worldEdit.getPlatformManager().getConfiguration().getWorkingDirectory(), assetDirectoryName);
        if (!assetsDir.exists() || !assetsDir.isDirectory()) {
            return null;
        }

        File file;
        try {
            file = worldEdit.getSafeOpenFile(null, assetsDir, path, defaultExtension, assetExtensions);
        } catch (FilenameException e) {
            return null;
        }

        T asset = null;
        try {
            asset = loadAssetFromFile(file);
        } catch (Exception e) {
            logger.error("Error reading file from brushes directory", e);
        }
        if (asset == null) {
            return null;
        }

        assets.put(assetName, asset);
        return asset;
    }

    /**
     * Returns an immutable set of asset keys.
     *
     * @return immutable set of asset keys
     */
    public Set<String> getCachedAssetKeys() {
        return Collections.unmodifiableSet(assets.asMap().keySet());
    }

    /**
     * Loads an asset from the given file if possible.
     *
     * @param file file to load
     * @return loaded asset, or null otherwise
     */
    @Nullable
    protected abstract T loadAssetFromFile(File file) throws Exception;

}
