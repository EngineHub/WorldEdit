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

package com.sk89q.worldedit.math.geom;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper method for anything related to polygons.
 */
public final class Polygons {

    private Polygons() {
        
    }

    /**
     * Calculates the polygon shape of a cylinder which can then be used for e.g. intersection detection.
     * 
     * @param center the center point of the cylinder
     * @param radius the radius of the cylinder
     * @param maxPoints max points to be used for the calculation
     * @return a list of {@link BlockVector2D} which resemble the shape as a polygon
     */
    public static List<BlockVector2D> polygonizeCylinder(Vector2D center, Vector2D radius, int maxPoints) {
        int nPoints = (int) Math.ceil(Math.PI*radius.length());

        // These strange semantics for maxPoints are copied from the selectSecondary method.
        if (maxPoints >= 0 && nPoints >= maxPoints) {
            nPoints = maxPoints - 1;
        }

        final List<BlockVector2D> points = new ArrayList<>(nPoints);
        for (int i = 0; i < nPoints; ++i) {
            double angle = i * (2.0 * Math.PI) / nPoints;
            final Vector2D pos = new Vector2D(Math.cos(angle), Math.sin(angle));
            final BlockVector2D blockVector2D = pos.multiply(radius).add(center).toBlockVector2D();
            points.add(blockVector2D);
        }

        return points;
    }
    
}
