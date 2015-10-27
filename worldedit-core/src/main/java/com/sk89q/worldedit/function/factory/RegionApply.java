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

package com.sk89q.worldedit.function.factory;

import com.google.common.base.Function;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.visitor.RegionVisitor;

import static com.google.common.base.Preconditions.checkNotNull;

public class RegionApply implements OperationFactory {

    private final Function<EditContext, ? extends RegionFunction> regionFunctionFactory;

    public RegionApply(Function<EditContext, ? extends RegionFunction> regionFunctionFactory) {
        checkNotNull(regionFunctionFactory, "regionFunctionFactory");
        this.regionFunctionFactory = regionFunctionFactory;
    }

    @Override
    public Operation createOperation(EditContext context) {
        return new RegionVisitor(context.getRegion(), regionFunctionFactory.apply(context));
    }

    @Override
    public String toString() {
        return "set " + regionFunctionFactory;
    }

}
