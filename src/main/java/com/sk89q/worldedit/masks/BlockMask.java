// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.masks;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * A mask that matches a block in a given list of blocks.
 */
public class BlockMask implements Mask {

    private final Set<BaseBlock> blocks;

    /**
     * Create a new block mask with nothing in the list.
     */
    public BlockMask() {
        blocks = new HashSet<BaseBlock>();
    }

    /**
     * Create a new block mask with one block type.
     * 
     * @param block the block
     */
    public BlockMask(BaseBlock block) {
        this();
        add(block);
    }

    /**
     * Create a new block mask with a list of block types.
     * 
     * @param types a list of block types
     */
    public BlockMask(Set<BaseBlock> types) {
        this.blocks = types;
    }

    /**
     * Get the list of blocks.
     * 
     * @return the list of blocks
     */
    public Set<BaseBlock> getBlocks() {
        return blocks;
    }

    /**
     * Add to the list of blocks.
     * 
     * @param block the block
     */
    public void add(BaseBlock block) {
        blocks.add(block);
    }

    /**
     * Add several blocks to the list.
     * 
     * @param blocks a list of blocks
     */
    public void addAll(Collection<BaseBlock> blocks) {
        blocks.addAll(blocks);
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        BaseBlock block = editSession.getBlock(pos);
        return  blocks.contains(block)
                || blocks.contains(new BaseBlock(block.getType(), -1));
    }

    @Override
    public String toString() {
        return String.format("BlockMask(blocks=%s)", blocks);
    }
    
}
