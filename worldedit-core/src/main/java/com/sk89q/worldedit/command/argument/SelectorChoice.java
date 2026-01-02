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

public enum SelectorChoice implements SelectorChoiceOrList {
    CUBOID("worldedit.select.cuboid.message") {
        @Override
        public RegionSelector createNewSelector(World world) {
            return new CuboidRegionSelector(world);
        }

        @Override
        public RegionSelector createNewSelector(RegionSelector oldSelector) {
            return new CuboidRegionSelector(oldSelector);
        }
    },
    EXTEND("worldedit.select.extend.message") {
        @Override
        public RegionSelector createNewSelector(World world) {
            return new ExtendingCuboidRegionSelector(world);
        }

        @Override
        public RegionSelector createNewSelector(RegionSelector oldSelector) {
            return new ExtendingCuboidRegionSelector(oldSelector);
        }
    },
    POLY("worldedit.select.poly.message") {
        @Override
        public RegionSelector createNewSelector(World world) {
            return new Polygonal2DRegionSelector(world);
        }

        @Override
        public RegionSelector createNewSelector(RegionSelector oldSelector) {
            return new Polygonal2DRegionSelector(oldSelector);
        }

        @Override
        public void explainNewSelector(Actor actor) {
            super.explainNewSelector(actor);
            Optional<Integer> limit = ActorSelectorLimits.forActor(actor).getPolygonVertexLimit();
            limit.ifPresent(integer -> actor.printInfo(TranslatableComponent.of(
                "worldedit.select.poly.limit-message", TextComponent.of(integer)
            )));
        }
    },
    ELLIPSOID("worldedit.select.ellipsoid.message") {
        @Override
        public RegionSelector createNewSelector(World world) {
            return new EllipsoidRegionSelector(world);
        }

        @Override
        public RegionSelector createNewSelector(RegionSelector oldSelector) {
            return new EllipsoidRegionSelector(oldSelector);
        }
    },
    SPHERE("worldedit.select.sphere.message") {
        @Override
        public RegionSelector createNewSelector(World world) {
            return new SphereRegionSelector(world);
        }

        @Override
        public RegionSelector createNewSelector(RegionSelector oldSelector) {
            return new SphereRegionSelector(oldSelector);
        }
    },
    CYL("worldedit.select.cyl.message") {
        @Override
        public RegionSelector createNewSelector(World world) {
            return new CylinderRegionSelector(world);
        }

        @Override
        public RegionSelector createNewSelector(RegionSelector oldSelector) {
            return new CylinderRegionSelector(oldSelector);
        }
    },
    CONVEX("worldedit.select.convex.message") {
        @Override
        public RegionSelector createNewSelector(World world) {
            return new ConvexPolyhedralRegionSelector(world);
        }

        @Override
        public RegionSelector createNewSelector(RegionSelector oldSelector) {
            return new ConvexPolyhedralRegionSelector(oldSelector);
        }

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

    // Suppress ImmutableEnumChecker: Component is immutable but not able to be marked as such
    @SuppressWarnings("ImmutableEnumChecker")
    private final Component messageComponent;

    SelectorChoice(String message) {
        this.messageComponent = TranslatableComponent.of(message);
    }

    public abstract RegionSelector createNewSelector(World world);

    public abstract RegionSelector createNewSelector(RegionSelector oldSelector);

    public void explainNewSelector(Actor actor) {
        actor.printInfo(messageComponent);
    }
}
