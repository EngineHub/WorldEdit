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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.util.TreeGenerator;

/**
 * Generates forests by searching for the ground starting from the given upper Y
 * coordinate for every column given.
 */
public class ForestGenerator extends GroundGenerator {

    private final TreeGenerator treeGenerator;
    private final EditSession editSession;

    /**
     * Create a new instance.
     *
     * @param editSession the edit session
     * @param treeGenerator a tree generator
     * @param lowerY the lower Y boundary to end the ground search from
     * @param upperY the upper Y boundary to start the ground search at
     */
    public ForestGenerator(EditSession editSession, TreeGenerator treeGenerator, int lowerY, int upperY) {
        super(editSession, lowerY, upperY);
        this.editSession = editSession;
        this.treeGenerator = treeGenerator;
    }

    @Override
    protected boolean apply(Vector pt, BaseBlock block) throws MaxChangedBlocksException {
        int t = block.getType();

        if (t == BlockID.GRASS || t == BlockID.DIRT) {
            treeGenerator.generate(editSession, pt.add(0, 1, 0));
            return true;
        } else if (t == BlockID.SNOW) {
            editSession.setBlock(pt, new BaseBlock(BlockID.AIR));
            return false;
        } else { // Trees won't grow on this!
            return false;
        }
    }


}
