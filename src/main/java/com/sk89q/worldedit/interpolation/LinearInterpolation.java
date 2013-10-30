// $Id$
/*
 * WorldEditLibrary
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sk89q.worldedit.interpolation;

import java.util.List;

import com.sk89q.worldedit.Vector;

/**
 * Simple linear interpolation. Mainly used for testing.
 *
 * @author TomyLobo
 *
 */
public class LinearInterpolation implements Interpolation {
    private List<Node> nodes;

    @Override
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Vector getPosition(double position) {
        if (nodes == null)
            throw new IllegalStateException("Must call setNodes first.");

        if (position > 1)
            return null;

        position *= nodes.size() - 1;

        final int index1 = (int) Math.floor(position);
        final double remainder = position - index1;

        final Vector position1 = nodes.get(index1).getPosition();
        final Vector position2 = nodes.get(index1 + 1).getPosition();

        return position1.multiply(1.0 - remainder).add(position2.multiply(remainder));
    }

    /*
    Formula for position:
        p1*(1-t) + p2*t
    Formula for position in Horner/monomial form:
        (p2-p1)*t + p1
    1st Derivative:
        p2-p1
    2nd Derivative:
        0
    Integral:
        (p2-p1)/2*t^2 + p1*t + constant
    Integral in Horner form:
        ((p2-p1)/2*t + p1)*t + constant
    */

    @Override
    public Vector get1stDerivative(double position) {
        if (nodes == null)
            throw new IllegalStateException("Must call setNodes first.");

        if (position > 1)
            return null;

        position *= nodes.size() - 1;

        final int index1 = (int) Math.floor(position);

        final Vector position1 = nodes.get(index1).getPosition();
        final Vector position2 = nodes.get(index1 + 1).getPosition();

        return position2.subtract(position1);
    }

    @Override
    public double arcLength(double positionA, double positionB) {
        if (nodes == null)
            throw new IllegalStateException("Must call setNodes first.");

        if (positionA > positionB)
            return arcLength(positionB, positionA);

        positionA *= nodes.size() - 1;
        positionB *= nodes.size() - 1;

        final int indexA = (int) Math.floor(positionA);
        final double remainderA = positionA - indexA;

        final int indexB = (int) Math.floor(positionB);
        final double remainderB = positionB - indexB;

        return arcLengthRecursive(indexA, remainderA, indexB, remainderB);
    }

    /**
     * Assumes a < b
     *
     * @param indexA
     * @param remainderA
     * @param indexB
     * @param remainderB
     * @return
     */
    private double arcLengthRecursive(int indexA, double remainderA, int indexB, double remainderB) {
        switch (indexB - indexA) {
        case 0:
            return arcLengthRecursive(indexA, remainderA, remainderB);

        case 1:
            // This case is merely a speed-up for a very common case
            return
                    arcLengthRecursive(indexA, remainderA, 1.0) +
                    arcLengthRecursive(indexB, 0.0, remainderB);

        default:
            return
                    arcLengthRecursive(indexA, remainderA, indexB - 1, 1.0) +
                    arcLengthRecursive(indexB, 0.0, remainderB);
        }
    }

    private double arcLengthRecursive(int index, double remainderA, double remainderB) {
        final Vector position1 = nodes.get(index).getPosition();
        final Vector position2 = nodes.get(index + 1).getPosition();

        return position1.distance(position2) * (remainderB - remainderA);
    }

    @Override
    public int getSegment(double position) {
        if (nodes == null)
            throw new IllegalStateException("Must call setNodes first.");

        if (position > 1)
            return Integer.MAX_VALUE;

        position *= nodes.size() - 1;

        final int index = (int) Math.floor(position);
        return index;
    }
}
