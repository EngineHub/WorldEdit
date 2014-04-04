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

import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated See {@link com.sk89q.worldedit.function.mask.FuzzyBlockMask}
 */
@Deprecated
public class FuzzyBlockMask extends AbstractMask {

    private final Set<BaseBlock> filter;

    /**
     * Create a new fuzzy block mask.
     *
     * @param filter a list of block types to match
     */
    public FuzzyBlockMask(Set<BaseBlock> filter) {
        this.filter = filter;
    }

    /**
     * Create a new fuzzy block mask.
     *
     * @param block a list of block types to match
     */
    public FuzzyBlockMask(BaseBlock... block) {
        Set<BaseBlock> filter = new HashSet<BaseBlock>();
        for (BaseBlock b : block) {
            filter.add(b);
        }
        this.filter = filter;
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        BaseBlock compare = new BaseBlock(editSession.getBlockType(pos), editSession.getBlockData(pos));
        return BaseBlock.containsFuzzy(filter, compare);
    }
}
