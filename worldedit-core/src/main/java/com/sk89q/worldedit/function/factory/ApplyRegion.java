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

package com.sk89q.worldedit.function.factory;

import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

public class ApplyRegion implements Contextual<Operation> {

    private final Region region;
    private final Contextual<? extends RegionFunction> function;

    public ApplyRegion(Contextual<? extends RegionFunction> function) {
        this(new NullRegion(), function);
    }

    public ApplyRegion(Region region, Contextual<? extends RegionFunction> function) {
        checkNotNull(region, "region");
        checkNotNull(function, "function");
        this.region = region;
        this.function = function;
    }

    @Override
    public Operation createFromContext(EditContext context) {
        return new RegionVisitor(firstNonNull(context.getRegion(), region), function.createFromContext(context));
    }

    @Override
    public String toString() {
        return "set " + function;
    }

}
