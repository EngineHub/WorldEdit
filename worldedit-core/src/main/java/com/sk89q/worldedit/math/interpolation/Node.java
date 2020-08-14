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

// $Id$

package com.sk89q.worldedit.math.interpolation;

import com.sk89q.worldedit.math.Vector3;

/**
 * Represents a node for interpolation.
 *
 * <p>The {@link #tension}, {@link #bias} and {@link #continuity} fields
 * are parameters for the Kochanek-Bartels interpolation algorithm.</p>
 */
public class Node {

    private Vector3 position;

    private double tension;
    private double bias;
    private double continuity;

    public Node() {
        this(Vector3.ZERO);
    }

    public Node(Node other) {
        this.position = other.position;

        this.tension = other.tension;
        this.bias = other.bias;
        this.continuity = other.continuity;
    }

    public Node(Vector3 position) {
        this.position = position;
    }


    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public double getTension() {
        return tension;
    }

    public void setTension(double tension) {
        this.tension = tension;
    }

    public double getBias() {
        return bias;
    }

    public void setBias(double bias) {
        this.bias = bias;
    }

    public double getContinuity() {
        return continuity;
    }

    public void setContinuity(double continuity) {
        this.continuity = continuity;
    }

}
