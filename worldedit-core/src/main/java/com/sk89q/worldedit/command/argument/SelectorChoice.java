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

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
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
import com.sk89q.worldedit.world.World;

import java.util.Optional;
import java.util.function.Function;

public enum SelectorChoice implements SelectorChoiceOrList {
    CUBOID(CuboidRegionSelector::new, CuboidRegionSelector::new, "worldedit.select.cuboid.message"),
    EXTEND(ExtendingCuboidRegionSelector::new, ExtendingCuboidRegionSelector::new, "worldedit.select.extend.message"),
    POLY(Polygonal2DRegionSelector::new, Polygonal2DRegionSelector::new, "worldedit.select.poly.message") {
        @Override
        public void explainNewSelector(Actor actor) {
            super.explainNewSelector(actor);
            Optional<Integer> limit = ActorSelectorLimits.forActor(actor).getPolygonVertexLimit();
            limit.ifPresent(integer -> actor.printInfo(TranslatableComponent.of(
                "worldedit.select.poly.limit-message", TextComponent.of(integer)
            )));
        }
    },
    ELLIPSOID(EllipsoidRegionSelector::new, EllipsoidRegionSelector::new, "worldedit.select.ellipsoid.message"),
    SPHERE(SphereRegionSelector::new, SphereRegionSelector::new, "worldedit.select.sphere.message"),
    CYL(CylinderRegionSelector::new, CylinderRegionSelector::new, "worldedit.select.cyl.message"),
    CONVEX(ConvexPolyhedralRegionSelector::new, ConvexPolyhedralRegionSelector::new, "worldedit.select.convex.message") {
        @Override
        public void explainNewSelector(Actor actor) {
            super.explainNewSelector(actor);
            Optional<Integer> limit = ActorSelectorLimits.forActor(actor).getPolyhedronVertexLimit();
            limit.ifPresent(integer -> actor.printInfo(TranslatableComponent.of(
                "worldedit.select.convex.limit-message", TextComponent.of(integer)
            )));
        }
    },
    ;

    private final Function<World, RegionSelector> newFromWorld;
    private final Function<RegionSelector, RegionSelector> newFromOld;
    private final Component messageComponent;

    SelectorChoice(Function<World, RegionSelector> newFromWorld,
                   Function<RegionSelector, RegionSelector> newFromOld,
                   String message) {
        this.newFromWorld = newFromWorld;
        this.newFromOld = newFromOld;
        this.messageComponent = TranslatableComponent.of(message);
    }

    public RegionSelector createNewSelector(World world) {
        return this.newFromWorld.apply(world);
    }

    public RegionSelector createNewSelector(RegionSelector oldSelector) {
        return this.newFromOld.apply(oldSelector);
    }

    public void explainNewSelector(Actor actor) {
        actor.printInfo(messageComponent);
    }
}
