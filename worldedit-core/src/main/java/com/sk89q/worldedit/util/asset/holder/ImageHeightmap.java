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

package com.sk89q.worldedit.util.asset.holder;

import com.google.common.annotations.Beta;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Represents an image that acts as a heightmap.
 *
 * <p>
 * Height is determined by how light each pixel of the image is,
 * from black (0) to white (1). Lightness is determined by an
 * average of the 3 color channels.
 * </p>
 */
@Beta
public class ImageHeightmap {

    private final BufferedImage image;

    private BufferedImage resizedImage;
    private int lastSize = -1;

    /**
     * Create a new image heightmap from an image.
     *
     * @param image The image
     */
    public ImageHeightmap(BufferedImage image) {
        this.image = image;
    }

    /**
     * Gets the height at the given position with scaling applied.
     *
     * @param x The x position
     * @param y The y position
     * @param size The size to sample the image as
     * @return The height at the location
     */
    public double getHeightAt(int x, int y, int size) {
        if (size != lastSize || resizedImage == null) {
            resizedImage = new BufferedImage(size, size, 1);
            Graphics2D graphic = null;
            try {
                graphic = resizedImage.createGraphics();
                graphic.drawImage(this.image, 0, 0, size, size, null);
            } finally {
                if (graphic != null) {
                    graphic.dispose();
                }
            }
            lastSize = size;
        }

        // Flip the Y axis
        y = resizedImage.getHeight() - 1 - y;

        int rgb = resizedImage.getRGB(x, y);
        if (rgb == 0) {
            return 0;
        }

        int red = rgb >>> 16 & 0xFF;
        int green = rgb >>> 8 & 0xFF;
        int blue = rgb & 0xFF;

        return (red + blue + green) / 3D / 255D;
    }
}
