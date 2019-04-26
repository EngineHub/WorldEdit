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

package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.pattern.Pattern;

import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

public class ReplaceFactory implements Contextual<RegionFunction> {
    private final Pattern fill;

    public ReplaceFactory(Pattern fill) {
        this.fill = fill;
    }

    @Override
    public RegionFunction createFromContext(EditContext context) {
        return new BlockReplace(
            firstNonNull(context.getDestination(), new NullExtent()),
            firstNonNull(context.getFill(), fill));
    }

    @Override
    public String toString() {
        return "replace blocks";
    }
}
