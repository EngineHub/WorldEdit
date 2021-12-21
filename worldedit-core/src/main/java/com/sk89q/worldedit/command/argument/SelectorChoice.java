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

package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.ConvexPolyhedralRegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.CylinderRegionSelector;
import com.sk89q.worldedit.regions.selector.EllipsoidRegionSelector;
import com.sk89q.worldedit.regions.selector.ExtendingCuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.selector.SphereRegionSelector;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import java.util.function.Function;
import javax.annotation.Nullable;

public enum SelectorChoice {
    CUBOID(CuboidRegionSelector::new, "worldedit.select.cuboid.message"),
    EXTEND(ExtendingCuboidRegionSelector::new, "worldedit.select.extend.message"),
    POLY(Polygonal2DRegionSelector::new, "worldedit.select.poly.message"),
    ELLIPSOID(EllipsoidRegionSelector::new, "worldedit.select.ellipsoid.message"),
    SPHERE(SphereRegionSelector::new, "worldedit.select.sphere.message"),
    CYL(CylinderRegionSelector::new, "worldedit.select.cyl.message"),
    CONVEX(ConvexPolyhedralRegionSelector::new, "worldedit.select.convex.message"),
    HULL(ConvexPolyhedralRegionSelector::new, "worldedit.select.convex.message"),
    POLYHEDRON(ConvexPolyhedralRegionSelector::new, "worldedit.select.convex.message"),
    LIST(Function.identity(), null);

    private final Function<RegionSelector, RegionSelector> selectorFunction;
    private final Component messageComponent;

    SelectorChoice(Function<RegionSelector, RegionSelector> selectorFunction, @Nullable String message) {
        this.selectorFunction = selectorFunction;
        if (message != null) {
            this.messageComponent = TranslatableComponent.of(message);
        } else {
            this.messageComponent = TextComponent.empty();
        }
    }

    public Function<RegionSelector, RegionSelector> getSelectorFunction() {
        return this.selectorFunction;
    }

    public Component getMessage() {
        return this.messageComponent;
    }
}
