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

package com.sk89q.worldedit.util.asset;

import com.google.common.annotations.Beta;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.io.file.FilenameException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@Beta
public abstract class AssetLoader<T> {

    private final Cache<String, T> assets = CacheBuilder.newBuilder()
        .expireAfterAccess(5, TimeUnit.MINUTES)
        .build();

    private final WorldEdit worldEdit;
    private final Path assetDir;

    public AssetLoader(WorldEdit worldEdit, Path assetDir) {
        this.worldEdit = worldEdit;
        this.assetDir = assetDir;
    }

    /**
     * Loads an asset.
     *
     * @param path path in assets directory, can be with and without its file extension
     * @return asset if successfully loaded, null otherwise
     */
    @Nullable
    public T getAsset(String path) {
        T cached = assets.getIfPresent(path);
        if (cached != null) {
            return cached;
        }

        if (!Files.isDirectory(this.assetDir)) {
            return null;
        }

        String[] extensions = this.getAllowedExtensions().toArray(new String[0]);

        Path file;
        try {
            file = worldEdit.getSafeOpenFile(
                null,
                this.assetDir.toFile(),
                path,
                extensions[0],
                extensions
            ).toPath();
        } catch (FilenameException e) {
            return null;
        }

        T asset;
        try {
            asset = loadAssetFromPath(file);
            if (asset == null) {
                return null;
            }
        } catch (Exception e) {
            WorldEdit.logger.error("Error reading asset file directory", e);
            return null;
        }

        assets.put(path, asset);
        return asset;
    }

    /**
     * Loads an asset from the given file if possible.
     *
     * @param path The file to load
     * @return loaded asset, or null otherwise
     */
    @Nullable
    protected abstract T loadAssetFromPath(Path path) throws Exception;

    /**
     * The extensions that this asset loader supports.
     *
     * @return The supported extensions
     */
    public abstract Set<String> getAllowedExtensions();
}
