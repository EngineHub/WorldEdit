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

import java.util.List;

import com.sk89q.worldedit.Vector;

/**
 * Represents an arbitrary function in &#8477; &rarr; &#8477;<sup>3</sup>
 *
 * @author TomyLobo
 *
 */
public interface Interpolation {
    /**
     * Sets nodes to be used by subsequent calls to
     * {@link #getPosition(double)} and the other methods.
     *
     * @param nodes
     */
    public void setNodes(List<Node> nodes);

    /**
     * Gets the result of f(position)
     *
     * @param position
     * @return
     */
    public Vector getPosition(double position);

    /**
     * Gets the result of f'(position).
     *
     * @param position
     * @return
     */
    public Vector get1stDerivative(double position);

    /**
     * Gets the result of &int;<sub>a</sub><sup style="position: relative; left: -1ex">b</sup>|f'(t)| dt.<br />
     * That means it calculates the arc length (in meters) between positionA
     * and positionB.
     *
     * @param positionA lower limit
     * @param positionB upper limit
     * @return
     */
    double arcLength(double positionA, double positionB);

    int getSegment(double position);
}
