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

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.NoiseFilter2D;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.regions.Regions.*;
import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

public class Paint implements Contextual<Operation> {

    private final Extent destination;
    private final Region region;
    private final Contextual<? extends RegionFunction> function;
    private final double density;

    public Paint(Contextual<? extends RegionFunction> function, double density) {
        this(new NullExtent(), new NullRegion(), function, density);
    }

    public Paint(Extent destination, Region region, Contextual<? extends RegionFunction> function,
                 double density) {
        checkNotNull(destination, "destination");
        checkNotNull(region, "region");
        checkNotNull(function, "function");
        checkNotNull(density, "density");
        this.destination = destination;
        this.region = region;
        this.function = function;
        this.density = density;
        new NoiseFilter2D(new RandomNoise(), density); // Check validity of the density argument
    }

    @Override
    public Operation createFromContext(EditContext context) {
        Extent destination = firstNonNull(context.getDestination(), this.destination);
        Region region = firstNonNull(context.getRegion(), this.region);
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(destination), function.createFromContext(context));
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
        return visitor;
    }

    @Override
    public String toString() {
        return "scatter " + function;
    }

}
