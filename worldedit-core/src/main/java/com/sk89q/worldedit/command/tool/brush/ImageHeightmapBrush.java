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
import com.sk89q.worldedit.util.asset.holder.ImageHeightmap;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.concurrent.ThreadLocalRandom;

public class ImageHeightmapBrush implements Brush {

    private final ImageHeightmap heightmap;
    private final double intensity;
    private final boolean erase;
    private final boolean flatten;
    private final boolean randomize;

    public ImageHeightmapBrush(ImageHeightmap heightmap, double intensity, boolean erase, boolean flatten, boolean randomize) {
        this.heightmap = heightmap;
        this.intensity = intensity;
        this.erase = erase;
        this.flatten = flatten;
        this.randomize = randomize;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double doubleSize) throws MaxChangedBlocksException {
        int size = (int) Math.ceil(doubleSize);

        double random = randomize ? ThreadLocalRandom.current().nextDouble() : 0;
        for (int offX = -size; offX <= size; offX++) {
            for (int offZ = -size; offZ <= size; offZ++) {
                int posX = position.getX() + offX;
                int posZ = position.getZ() + offZ;
                int posY = editSession.getHighestTerrainBlock(posX, posZ, 0, 255, editSession.getMask());
                BlockVector3 block = BlockVector3.at(posX, posY, posZ);
                if (editSession.getMask() != null && !editSession.getMask().test(block)) {
                    continue;
                }

                double height = heightmap.getHeightAt(offX + size, offZ + size, size * 2 + 1) * intensity;
                // Add a bit of variation
                if (randomize && random > 1 - height % 1) {
                    height += 1;
                }

                BlockState baseBlock = erase ? null : editSession.getBlock(block);
                for (int y = 0; y < height; y++) {
                    if (erase) {
                        // Remove blocks if using the erase flag
                        if (!flatten || block.getY() - y > position.getY()) {
                            editSession.setBlock(block.withY(block.getY() - y), BlockTypes.AIR.getDefaultState());
                        }
                    } else if (!flatten || block.getY() + y <= position.getY()) {
                        editSession.setBlock(block.withY(block.getY() + y), baseBlock);
                    }
                }
            }
        }
    }
}
