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

package com.sk89q.worldedit.command.factory;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.ItemUseFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.World;

public final class ItemUseFactory implements Contextual<RegionFunction> {
    private final BaseItem item;
    private final Direction dir;

    public ItemUseFactory(BaseItem item) {
        this(item, Direction.UP);
    }

    public ItemUseFactory(BaseItem item, Direction dir) {
        this.item = item;
        this.dir = dir;
    }

    @Override
    public RegionFunction createFromContext(EditContext input) {
        World world = ((EditSession) input.getDestination()).getWorld();
        return new ItemUseFunction(world, item, dir);
    }

    @Override
    public String toString() {
        return "application of the item " + item.getType();
    }
}
