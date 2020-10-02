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
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import com.google.common.io.MoreFiles;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.asset.holder.ImageHeightmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Class to store the various asset loaders.
 */
@Beta
public class AssetLoaders {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<AssetLoader<?>> assetLoaders = Lists.newArrayList();
    private final Table<Class<?>, String, AssetLoader<?>> assetLoaderRegistration = HashBasedTable.create();
    private final WorldEdit worldEdit;

    private Path assetsDir;

    /**
     * Creates a new AssetManager to load and cache custom assets.
     *
     * @param worldEdit WorldEdit instance
     */
    public AssetLoaders(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    public void init() {
        this.assetsDir = worldEdit.getWorkingDirectoryPath("assets");

        try {
            Files.createDirectories(this.assetsDir);
        } catch (IOException e) {
            logger.warn("Failed to create asset directory", e);
        }

        registerAssetLoader(new ImageHeightmapLoader(worldEdit, this.assetsDir), ImageHeightmap.class);
    }

    public <T> void registerAssetLoader(AssetLoader<T> loader, Class<T> assetClass) {
        assetLoaders.add(loader);
        for (String extension : loader.getAllowedExtensions()) {
            if (assetLoaderRegistration.contains(assetClass, extension)) {
                logger.warn(String.format(
                    "Tried to register asset loader '%s' with extension '%s' and asset class '%s', but it is already registered to '%s'",
                    loader.getClass().getName(),
                    extension,
                    assetClass.getName(),
                    assetLoaderRegistration.get(assetClass, extension).getClass().getName()
                ));
                continue;
            }

            assetLoaderRegistration.put(assetClass, extension, loader);
        }
    }

    /**
     * Gets the Asset Loader for the given file of the given type.
     *
     * @param assetClass The class to get a loader for
     * @param filename The filename to attempt to load
     * @param <T> The returned asset type
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<AssetLoader<T>> getAssetLoader(Class<T> assetClass, String filename) {
        if (!assetLoaderRegistration.containsRow(assetClass)) {
            return Optional.empty();
        }

        Path directPath = this.assetsDir.resolve(filename);

        String ext = MoreFiles.getFileExtension(directPath);
        if (Files.exists(directPath) && assetLoaderRegistration.contains(assetClass, ext)) {
            return Optional.ofNullable((AssetLoader<T>) assetLoaderRegistration.get(assetClass, ext));
        }

        for (Map.Entry<String, AssetLoader<?>> entry : assetLoaderRegistration.row(assetClass).entrySet()) {
            Path extensionPath = this.assetsDir.resolve(filename + "." + entry.getKey());
            if (Files.exists(extensionPath)) {
                return Optional.ofNullable((AssetLoader<T>) entry.getValue());
            }
        }

        return Optional.empty();
    }

    /**
     * Get the Asset Loaders for the given type.
     *
     * @param assetClass The class to get the loaders of
     * @return The list of asset loaders
     *
     * @param <T> The asset type
     */
    @SuppressWarnings("unchecked")
    public <T> List<AssetLoader<T>> getAssetLoaders(Class<T> assetClass) {
        if (!assetLoaderRegistration.containsRow(assetClass)) {
            return ImmutableList.of();
        }

        return ImmutableList.copyOf((Collection<AssetLoader<T>>) (Collection<?>) assetLoaderRegistration.row(assetClass).values());
    }

    /**
     * Gets an immutable list of all files that match a certain asset type.
     *
     * @param assetClass The asset class
     * @return The list of files
     */
    public List<Path> getFilesForAsset(Class<?> assetClass) {
        Set<String> extensions = this.assetLoaderRegistration.row(assetClass).keySet();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(this.assetsDir, entry -> extensions.contains(MoreFiles.getFileExtension(entry)))) {
            return ImmutableList.copyOf(stream);
        } catch (IOException e) {
            logger.warn("Failed to get files for asset type " + assetClass.getName(), e);
            return ImmutableList.of();
        }
    }

    /**
     * Gets an immutable copy of all registered asset loaders.
     *
     * @return The asset loaders
     */
    public List<AssetLoader<?>> getAssetLoaders() {
        return ImmutableList.copyOf(assetLoaders);
    }
}
