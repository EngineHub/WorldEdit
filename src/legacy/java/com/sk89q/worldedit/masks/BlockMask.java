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

package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated Use {@link com.sk89q.worldedit.function.mask.BlockMask}
 */
@Deprecated
public class BlockMask extends AbstractMask {

    private final Set<BaseBlock> blocks;

    public BlockMask() {
        blocks = new HashSet<BaseBlock>();
    }

    public BlockMask(Set<BaseBlock> types) {
        this.blocks = types;
    }

    public BlockMask(BaseBlock... block) {
        blocks = new HashSet<BaseBlock>();
        for (BaseBlock b : block) {
            add(b);
        }
    }

    public BlockMask(BaseBlock block) {
        this();
        add(block);
    }

    public void add(BaseBlock block) {
        blocks.add(block);
    }

    public void addAll(Collection<BaseBlock> blocks) {
        blocks.addAll(blocks);
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        BaseBlock block = editSession.getBlock(pos);
        return blocks.contains(block)
                || blocks.contains(new BaseBlock(block.getType(), -1));
    }

}
