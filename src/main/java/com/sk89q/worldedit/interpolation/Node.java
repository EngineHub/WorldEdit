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

import com.sk89q.worldedit.Vector;

/**
 * Represents a node for interpolation.<br />
 * The {@link #tension}, {@link #bias} and {@link #continuity} fields
 * are parameters for the Kochanek-Bartels interpolation algorithm.
 *
 * @author TomyLobo
 *
 */
public class Node {
    private Vector position;

    private double tension;
    private double bias;
    private double continuity;

    public Node() {
        this(new Vector(0, 0, 0));
    }

    public Node(Node other) {
        this.position = other.position;

        this.tension = other.tension;
        this.bias = other.bias;
        this.continuity = other.continuity;
    }

    public Node(Vector position) {
        this.position = position;
    }


    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
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
