package com.sk89q.worldedit.util;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector2D;

/**
 * Helper method for anything related to region calculations.
 */
public class RegionUtil {

    private RegionUtil() {
        
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

        final List<BlockVector2D> points = new ArrayList<BlockVector2D>(nPoints);
        for (int i = 0; i < nPoints; ++i) {
            double angle = i * (2.0 * Math.PI) / nPoints;
            final Vector2D pos = new Vector2D(Math.cos(angle), Math.sin(angle));
            final BlockVector2D blockVector2D = pos.multiply(radius).add(center).toBlockVector2D();
            points.add(blockVector2D);
        }

        return points;
    }
    
}
