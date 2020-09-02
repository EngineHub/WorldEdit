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

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class ImageBrush implements Brush {

    private static final Random RANDOM = new Random();
    private final BufferedImage image;
    private final double intensity;
    private final boolean erase;
    private final boolean flatten;
    private final boolean randomize;

    public ImageBrush(BufferedImage image, double intensity, boolean erase, boolean flatten, boolean randomize) {
        this.image = image;
        this.intensity = intensity;
        this.erase = erase;
        this.flatten = flatten;
        this.randomize = randomize;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double doubleSize) throws MaxChangedBlocksException {
        // Resize the image
        int size = (int) Math.ceil(doubleSize);
        int diameter = size * 2 + 1;
        BufferedImage resizedPattern = new BufferedImage(diameter, diameter, 1);
        Graphics2D graphic = null;
        try {
            graphic = resizedPattern.createGraphics();
            graphic.drawImage(this.image, 0, 0, diameter, diameter, null);
        } finally {
            graphic.dispose();
        }

        double random = randomize ? RANDOM.nextDouble() : 0;
        for (int offX = -size; offX <= size; offX++) {
            for (int offZ = -size; offZ <= size; offZ++) {
                int posX = position.getX() + offX;
                int posZ = position.getZ() + offZ;
                int posY = editSession.getHighestTerrainBlock(posX, posZ, 0, 255, editSession.getMask());
                BlockVector3 block = BlockVector3.at(posX, posY, posZ);
                if (editSession.getMask() != null && !editSession.getMask().test(block)) {
                    continue;
                }

                double height = getHeightAt(resizedPattern, offX + size, resizedPattern.getHeight() - 1 - (offZ + size));
                // Add a bit of variation
                if (randomize && random > 1 - height % 1) {
                    height += 1;
                }

                BaseBlock baseBlock = erase ? null : editSession.getBlock(block).toBaseBlock();
                for (int y = 0; y < height; y++) {
                    if (erase) {
                        // Remove blocks if using the erase flag
                        editSession.setBlock(block.withY(block.getY() - y), BlockTypes.AIR.getDefaultState());
                    } else if ((!flatten || block.getY() - y > position.getY()) && block.getY() - y >= 0) {
                        // Only go up to the origin's level if flat mode is enabled
                        if (!flatten || block.getY() + y <= position.getY()) {
                            editSession.setBlock(block.withY(block.getY() + y), baseBlock);
                        }
                    }
                }
            }
        }
    }

    private double getHeightAt(BufferedImage image, int x, int y) {
        int rgb = image.getRGB(x, y);
        if (rgb == 0) {
            return 0;
        }

        int red = rgb >>> 16 & 0xFF;
        int green = rgb >>> 8 & 0xFF;
        int blue = rgb & 0xFF;

        double scale = (red + blue + green) / 3D / 255D;
        return scale * intensity;
    }

}

