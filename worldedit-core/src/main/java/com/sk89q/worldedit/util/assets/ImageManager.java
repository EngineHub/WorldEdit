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

import com.sk89q.worldedit.WorldEdit;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;

/**
 * Loads and caches image files from WorldEdit's assets directory.
 */
public class ImageManager extends AssetManager<BufferedImage> {

    public ImageManager(WorldEdit worldEdit) {
        super(worldEdit, "assets", "png", "png", "jpg", "jpeg");
    }

    @Nullable
    public BufferedImage loadAssetFromFile(File file) throws Exception {
        if (!file.exists()) {
            return null;
        }
        return ImageIO.read(file);
    }
}
