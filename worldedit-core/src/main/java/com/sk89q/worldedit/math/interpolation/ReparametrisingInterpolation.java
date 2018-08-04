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

// $Id$

package com.sk89q.worldedit.math.interpolation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.Vector;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Reparametrises another interpolation function by arc length.
 *
 * <p>This is done so entities travel at roughly the same speed across
 * the whole route.</p>
 */
public class ReparametrisingInterpolation implements Interpolation {

    private static final Logger log = Logger.getLogger(ReparametrisingInterpolation.class.getCanonicalName());

    private final Interpolation baseInterpolation;
    private double totalArcLength;
    private final TreeMap<Double, Double> cache = new TreeMap<>();

    public ReparametrisingInterpolation(Interpolation baseInterpolation) {
        checkNotNull(baseInterpolation);
        
        this.baseInterpolation = baseInterpolation;
    }

    @Override
    public void setNodes(List<Node> nodes) {
        checkNotNull(nodes);

        baseInterpolation.setNodes(nodes);
        cache.clear();
        cache.put(0.0, 0.0);
        cache.put(totalArcLength = baseInterpolation.arcLength(0.0, 1.0), 1.0);
    }

    public Interpolation getBaseInterpolation() {
        return baseInterpolation;
    }

    @Override
    public Vector getPosition(double position) {
        if (position > 1)
            return null;

        return baseInterpolation.getPosition(arcToParameter(position));
    }

    @Override
    public Vector get1stDerivative(double position) {
        if (position > 1)
            return null;

        return baseInterpolation.get1stDerivative(arcToParameter(position)).normalize().multiply(totalArcLength);
    }

    @Override
    public double arcLength(double positionA, double positionB) {
        return baseInterpolation.arcLength(arcToParameter(positionA), arcToParameter(positionB));
    }

    private double arcToParameter(double arc) {
        if (cache.isEmpty())
            throw new IllegalStateException("Must call setNodes first.");

        if (arc > 1) arc = 1;
        arc *= totalArcLength;

        Entry<Double, Double> floorEntry = cache.floorEntry(arc);
        final double leftArc = floorEntry.getKey();
        final double leftParameter = floorEntry.getValue();

        if (leftArc == arc) {
            return leftParameter;
        }

        Entry<Double, Double> ceilingEntry = cache.ceilingEntry(arc);
        if (ceilingEntry == null) {
            log.warning("Error in arcToParameter: no ceiling entry for " + arc + " found!");
            return 0;
        }
        final double rightArc = ceilingEntry.getKey();
        final double rightParameter = ceilingEntry.getValue();

        if (rightArc == arc) {
            return rightParameter;
        }

        return evaluate(arc, leftArc, leftParameter, rightArc, rightParameter);
    }

    private double evaluate(double arc, double leftArc, double leftParameter, double rightArc, double rightParameter) {
        double midParameter = 0;
        for (int i = 0; i < 10; ++i) {
            midParameter = (leftParameter + rightParameter) * 0.5;
            //final double midArc = leftArc + baseInterpolation.arcLength(leftParameter, midParameter);
            final double midArc = baseInterpolation.arcLength(0, midParameter);
            cache.put(midArc, midParameter);

            if (midArc < leftArc) {
                return leftParameter;
            }

            if (midArc > rightArc) {
                return rightParameter;
            }

            if (Math.abs(midArc - arc) < 0.01) {
                return midParameter;
            }

            if (arc < midArc) {
                // search between left and mid
                rightArc = midArc;
                rightParameter = midParameter;
            }
            else {
                // search between mid and right
                leftArc = midArc;
                leftParameter = midParameter;
            }
        }
        return midParameter;
    }

    @Override
    public int getSegment(double position) {
        if (position > 1)
            return Integer.MAX_VALUE;

        return baseInterpolation.getSegment(arcToParameter(position));
    }

}
