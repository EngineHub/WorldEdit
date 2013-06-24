// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.transform;

import java.util.Random;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.operation.ChangeCountable;
import com.sk89q.worldedit.operation.ColumnVisitor;
import com.sk89q.worldedit.operation.ExecutionHint;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.structure.Structure;

/**
 * Scatter structures on the surface of {@link Region} with a variable density.
 * Surface is considered the first block exposed to the skylight, including
 * transparent blocks.
 */
public class ScatterStructures extends ColumnVisitor implements ChangeCountable {
    
    private final Random random = new Random();
    
    private final EditSession context;
    private final Structure structure;

    private int affected = 0;
    private double density = 1;
    private int minY;
    
    /**
     * Create a structure scattering operation.
     * 
     * @param context to apply changes to
     * @param region area to apply changes to
     * @param structure structure to generate
     */
    public ScatterStructures(EditSession context, FlatRegion region, Structure structure) {
        super(region);
        
        this.context = context;
        this.structure = structure;

        minY = region.getMinimumPoint().getBlockY();
    }

    /**
     * Get the density of the scattering as a number between 0 and 1, inclusive.
     * <p>
     * A value of 1  indicates that every visited block will have the structure generated
     * at it (if possible). A value of 0 indicates that no visited block will have a 
     * structure generated at it.
     * 
     * @return the density, as 0 to 1
     */
    public double getDensity() {
        return density;
    }

    /**
     * Set the density of the scattering as a number between 0 and 1, inclusive.
     * 
     * @see #getDensity()
     * @param density the density, as 0 to 1
     */
    public void setDensity(double density) {
        this.density = density;
    }

    @Override
    public void visitColumn(ExecutionHint opt, Vector columnPt) throws WorldEditException {
        if (random.nextDouble() >= density) {
            for (int y = columnPt.getBlockY(); y >= minY; --y) {
                int type = context.getBlock(columnPt.setY(y)).getType();
                if (isSurfaceBlock(type)) {
                    if (structure.generate(context, columnPt.setY(y + 1))) {
                        affected++;
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Indicates whether the surface block should be the given block. By default,
     * the method will return true for any non-air block.
     * 
     * @param type type ID of the block
     * @return true if it is the surface block.
     */
    public boolean isSurfaceBlock(int type) {
        return type != 0;
    }

    @Override
    public void cancel() {
        // Nothing to clean up
    }

    @Override
    public int getChangeCount() {
        return affected;
    }
    
    @Override
    public String toString() {
        return String.format("ScatterStructures(region=%s, structure=%s)", 
                getRegion(), structure);
    }

}