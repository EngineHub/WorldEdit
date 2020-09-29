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
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.asset.holder.ImageHeightmap;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

/**
 * Loads and caches image files from WorldEdit's assets directory.
 */
@Beta
public class ImageHeightmapLoader extends AssetLoader<ImageHeightmap> {

    public ImageHeightmapLoader(WorldEdit worldEdit, Path assetDir) {
        super(worldEdit, assetDir);
    }

    @Nullable
    public ImageHeightmap loadAssetFromPath(Path path) throws Exception {
        if (!Files.exists(path)) {
            return null;
        }
        return new ImageHeightmap(ImageIO.read(path.toFile()));
    }

    @Override
    public Set<String> getAllowedExtensions() {
        return ImmutableSet.copyOf(ImageIO.getReaderFileSuffixes());
    }
}
