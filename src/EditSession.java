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

import java.util.Map;
import java.util.HashMap;
import com.sk89q.worldedit.*;

/**
 *
 * @author Albert
 */
public class EditSession {
    /**
     * Stores the original blocks before modification.
     */
    private HashMap<Point,Integer> original = new HashMap<Point,Integer>();
    private HashMap<Point,Integer> current = new HashMap<Point,Integer>();

    /**
     * Sets a block without changing history.
     * 
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return Whether the block changed
     */
    private boolean rawSetBlock(int x, int y, int z, int blockType) {
        return etc.getMCServer().e.d(x, y, z, blockType);
    }
    
    /**
     * Sets the block at position x, y, z with a block type.
     *
     * @param x
     * @param y
     * @param z
     * @param blockType
     * @return Whether the block changed
     */
    public boolean setBlock(int x, int y, int z, int blockType) {
        Point pt = new Point(x, y, z);
        if (!original.containsKey(pt)) {
            original.put(pt, getBlock(x, y, z));
        }
        current.put(pt, blockType);
        return rawSetBlock(x, y, z, blockType);
    }

    /**
     * Gets the block type at a position x, y, z.
     *
     * @param x
     * @param y
     * @param z
     * @return Block type
     */
    public int getBlock(int x, int y, int z) {
        return etc.getMCServer().e.a(x, y, z);
    }

    /**
     * Restores all blocks to their initial state.
     */
    public void undo() {
        for (Map.Entry<Point,Integer> entry : original.entrySet()) {
            Point pt = (Point)entry.getKey();
            rawSetBlock((int)pt.getX(), (int)pt.getY(),(int)pt.getZ(),
                        (int)entry.getValue());
        }
    }

    /**
     * Sets to new state.
     */
    public void redo() {
        for (Map.Entry<Point,Integer> entry : current.entrySet()) {
            Point pt = (Point)entry.getKey();
            rawSetBlock((int)pt.getX(), (int)pt.getY(),(int)pt.getZ(),
                        (int)entry.getValue());
        }
    }

    /**
     * Get the number of changed blocks.
     * 
     */
    public int size() {
        return original.size();
    }
}
