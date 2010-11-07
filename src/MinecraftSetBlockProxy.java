// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Proxy class to catch calls to set blocks.
 *
 * @author sk89q
 */
public class MinecraftSetBlockProxy extends el {
    /**
     * Edit session.
     */
    private EditSession editSession;

    /**
     * Constructor that should NOT be called.
     * 
     * @param editSession
     */
    public MinecraftSetBlockProxy(EditSession editSession) {
        super(null, "", (long)0, null);
        throw new IllegalStateException("MinecraftSetBlockProxy constructor called (BAD)");
    }

    /**
     * Called to set a block.
     * 
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return
     */
    @Override
    public boolean a(int x, int y, int z, int blockType) {
        try {
            return editSession.setBlock(new Vector(x, y, z), new BaseBlock(blockType));
        } catch (MaxChangedBlocksException ex) {
            return false;
        }
    }

    /**
     * Called to get a block.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    @Override
    public int a(int x, int y, int z) {
        return editSession.getBlock(new Vector(x, y, z)).getID();
    }

    /**
     * @return
     */
    public EditSession getEditSession() {
        return editSession;
    }

    /**
     * @param editSession
     */
    public void setEditSession(EditSession editSession) {
        this.editSession = editSession;
    }
}
