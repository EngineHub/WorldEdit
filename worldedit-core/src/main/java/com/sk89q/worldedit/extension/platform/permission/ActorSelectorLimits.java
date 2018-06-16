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

package com.sk89q.worldedit.extension.platform.permission;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;

import java.util.Optional;

public class ActorSelectorLimits implements SelectorLimits {

    private final LocalConfiguration configuration;
    private final Actor actor;

    public ActorSelectorLimits(LocalConfiguration configuration, Actor actor) {
        checkNotNull(configuration);
        checkNotNull(actor);

        this.configuration = configuration;
        this.actor = actor;
    }

    @Override
    public Optional<Integer> getPolygonVertexLimit() {
        int limit;

        if (actor.hasPermission(OverridePermissions.NO_LIMITS) || configuration.maxPolygonalPoints < 0) {
            limit = configuration.defaultMaxPolygonalPoints;
        } else if (configuration.defaultMaxPolygonalPoints < 0) {
            limit = configuration.maxPolygonalPoints;
        } else {
            limit = Math.min(configuration.defaultMaxPolygonalPoints, configuration.maxPolygonalPoints);
        }

        if (limit > 0) {
            return Optional.of(limit);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Integer> getPolyhedronVertexLimit() {
        int limit;

        if (actor.hasPermission(OverridePermissions.NO_LIMITS) || configuration.maxPolyhedronPoints < 0) {
            limit = configuration.defaultMaxPolyhedronPoints;
        } else if (configuration.defaultMaxPolyhedronPoints < 0) {
            limit = configuration.maxPolyhedronPoints;
        } else {
            limit = Math.min(configuration.defaultMaxPolyhedronPoints, configuration.maxPolyhedronPoints);
        }

        if (limit > 0) {
            return Optional.of(limit);
        } else {
            return Optional.empty();
        }
    }

    public static ActorSelectorLimits forActor(Actor actor) {
        return new ActorSelectorLimits(WorldEdit.getInstance().getConfiguration(), actor);
    }

}
