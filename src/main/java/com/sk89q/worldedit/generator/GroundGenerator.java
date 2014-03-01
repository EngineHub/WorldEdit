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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.operation.GroundFindingFunction;

/**
 * An abstract implementation for generators.
 */
public abstract class GroundGenerator extends GroundFindingFunction {

    private double density;

    /**
     * Create a new instance.
     *
     * @param editSession the edit session
     * @param lowerY the lower Y
     * @param upperY the upper Y (lowerY <= upperY)
     */
    protected GroundGenerator(EditSession editSession, int lowerY, int upperY) {
        super(editSession, lowerY, upperY);
    }

    /**
     * Set the density (0 <= density <= 1) which indicates the percentage chance
     * that an object will spawn in each column.
     *
     * @return the density
     */
    public double getDensity() {
        return density;
    }

    /**
     * Get the density (0 <= density <= 1) which indicates the percentage chance
     * that an object will spawn in each column.
     *
     * @param density the density
     */
    public void setDensity(double density) {
        this.density = density;
    }

    @Override
    protected boolean shouldContinue(Vector2D pt) {
        return Math.random() < density;
    }

}
