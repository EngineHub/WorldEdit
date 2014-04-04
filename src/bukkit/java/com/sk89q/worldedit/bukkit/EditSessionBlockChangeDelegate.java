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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.BlockChangeDelegate;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Proxy class to catch calls to set blocks.
 *
 * @author sk89q
 */
public class EditSessionBlockChangeDelegate implements BlockChangeDelegate {
    private EditSession editSession;

    public EditSessionBlockChangeDelegate(EditSession editSession) {
        this.editSession = editSession;
    }

    public boolean setRawTypeId(int x, int y, int z, int typeId) {
        try {
            return editSession.setBlock(new Vector(x, y, z), new BaseBlock(typeId));
        } catch (MaxChangedBlocksException ex) {
            return false;
        }
    }

    public boolean setRawTypeIdAndData(int x, int y, int z, int typeId, int data) {
        try {
            return editSession.setBlock(new Vector(x, y, z), new BaseBlock(typeId, data));
        } catch (MaxChangedBlocksException ex) {
            return false;
        }
    }

    public boolean setTypeId(int x, int y, int z, int typeId) {
        return setRawTypeId(x, y, z, typeId);
    }

    public boolean setTypeIdAndData(int x, int y, int z, int typeId, int data) {
        return setRawTypeIdAndData(x, y, z, typeId, data);
    }

    public int getTypeId(int x, int y, int z) {
        return editSession.getBlockType(new Vector(x, y, z));
    }

    public int getHeight() {
        return editSession.getWorld().getMaxY() + 1;
    }

    public boolean isEmpty(int x, int y, int z) {
        return editSession.getBlockType(new Vector(x, y, z)) == BlockID.AIR;
    }
}
