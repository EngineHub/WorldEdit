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

package com.sk89q.worldedit.internal.util;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Identity;
import com.sk89q.worldedit.math.transform.ScaleAndTranslateTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;

/**
 * Various internal utility methods related to {@link Transform}s.
 */
public final class TransformUtil {

    private TransformUtil() {
    }

    /**
     * Creates a {@link Transform} for various expression commands.
     *
     * @param actor           Actor that ran the command
     * @param session         Session that the command was run in
     * @param region          Selection that the command was run in
     * @param useRawCoords    Use the game's coordinate origin
     * @param offsetPlacement Use the placement's coordinate origin
     * @param offsetCenter    Use the selection's center as origin
     * @return                A transform from the expression coordinate system to the raw coordinate system
     */
    public static Transform createTransformForExpressionCommand(Actor actor, LocalSession session, Region region, boolean useRawCoords, boolean offsetPlacement, boolean offsetCenter) throws IncompleteRegionException {
        final Vector3 placement = session.getPlacementPosition(actor).toVector3();
        final Vector3 min = region.getMinimumPoint().toVector3();
        final Vector3 max = region.getMaximumPoint().toVector3();

        return createTransformForExpressionCommand(useRawCoords, offsetPlacement, offsetCenter, min, max, placement);
    }

    /**
     * Creates a {@link Transform} for various expression commands from raw min/max/placement values.
     *
     * @param useRawCoords    Use the game's coordinate origin
     * @param offsetPlacement Use the placement's coordinate origin
     * @param offsetCenter    Use the selection's center as origin
     * @param min             Minimum of the selection/clipboard
     * @param max             Maximum of the selection/clipboard
     * @param placement       Placement position
     * @return                A transform from the expression coordinate system to the world/clipboard coordinate system
     */
    public static Transform createTransformForExpressionCommand(boolean useRawCoords, boolean offsetPlacement, boolean offsetCenter, Vector3 min, Vector3 max, Vector3 placement) {
        if (useRawCoords) {
            return new Identity();
        }

        if (offsetPlacement) {
            return new ScaleAndTranslateTransform(placement, Vector3.ONE);
        }

        final Vector3 center = max.add(min).multiply(0.5);

        if (offsetCenter) {
            return new ScaleAndTranslateTransform(center, Vector3.ONE);
        }

        Vector3 scale = max.subtract(center);

        if (scale.x() == 0) {
            scale = scale.withX(1.0);
        }
        if (scale.y() == 0) {
            scale = scale.withY(1.0);
        }
        if (scale.z() == 0) {
            scale = scale.withZ(1.0);
        }
        return new ScaleAndTranslateTransform(center, scale);
    }
}
