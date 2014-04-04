/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function.block;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.function.LayerFunction;
import com.sk89q.worldedit.masks.BlockMask;
import com.sk89q.worldedit.masks.Mask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Makes a layer of grass on top, three layers of dirt below, and smooth stone
 * only below that for all layers that originally consist of grass, dirt,
 * or smooth stone.
 */
public class Naturalizer implements LayerFunction {

    private final EditSession editSession;
    private final BaseBlock grass = new BaseBlock(BlockID.GRASS);
    private final BaseBlock dirt = new BaseBlock(BlockID.DIRT);
    private final BaseBlock stone = new BaseBlock(BlockID.STONE);
    private final Mask mask = new BlockMask(grass, dirt, stone);
    private int affected = 0;

    /**
     * Make a new naturalizer.
     *
     * @param editSession an edit session
     */
    public Naturalizer(EditSession editSession) {
        checkNotNull(editSession);
        this.editSession = editSession;
    }

    /**
     * Get the number of affected objects.
     *
     * @return the number of affected
     */
    public int getAffected() {
        return affected;
    }

    @Override
    public boolean isGround(Vector position) {
        return mask.matches(editSession, position);
    }

    @Override
    public boolean apply(Vector position, int depth) throws WorldEditException {
        if (mask.matches(editSession, position)) {
            affected++;
            switch (depth) {
                case 0:
                    editSession.setBlock(position, grass);
                    break;
                case 1:
                case 2:
                case 3:
                    editSession.setBlock(position, dirt);
                    break;
                default:
                    editSession.setBlock(position, stone);
            }
        }

        return true;
    }
}
