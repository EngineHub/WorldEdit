// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
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


package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class OverlayBrush implements Brush{
    private Set<BaseBlock> groundBlocks;
    private boolean replaceBlocks;

    public OverlayBrush(Set<BaseBlock> groundBlocks, boolean replaceExistingBlocks) {
        this.groundBlocks = groundBlocks;
        this.replaceBlocks = replaceExistingBlocks;
    }
    
    /**
     * 
     * @param pos The center of the sphere
     * @param radius The radius of the sphere
     * @return A set with all blocks in the sphere
     */
    private List<Vector> getSphereBlocks(Vector pos, double radius){
        final List<Vector> list = new ArrayList<Vector>();
        
        radius += 0.5;

        final double invRadius = 1 / radius;

        final int ceilRadius = (int) Math.ceil(radius);

        double nextXn = 0;
        forX:
        for (int x = 0; x <= ceilRadius; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadius;
            double nextYn = 0;
            forY:
            for (int y = 0; y <= ceilRadius; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadius;
                double nextZn = 0;
                forZ:
                for (int z = 0; z <= ceilRadius; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadius;

                    double distanceSq = xn * xn + yn * yn + zn * zn;
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    list.add(pos.add(x, y, z));
                    list.add(pos.add(-x, y, z));
                    list.add(pos.add(x, -y, z));
                    list.add(pos.add(x, y, -z));
                    list.add(pos.add(-x, -y, z));
                    list.add(pos.add(-x, y, -z));
                    list.add(pos.add(x, -y, -z));
                    list.add(pos.add(-x, -y, -z));

                }
            }
        }
        return list;
    }

    @Override
    public void build(EditSession editSession, Vector pos, Pattern mat, double size) throws MaxChangedBlocksException {
        List<Vector> blockVectors = this.getSphereBlocks(pos, size);
        
        Iterator<Vector> it = blockVectors.iterator();
        
        while(it.hasNext()){
            Vector blockVector = it.next();
            Vector aboveVector = blockVector.add(0, 1, 0);
            
            if(this.groundBlocks.contains(editSession.getBlock(blockVector))){
                
                if(replaceBlocks || editSession.getBlock(aboveVector).isAir()){
                    editSession.setBlock(aboveVector, mat.next(aboveVector));
                }
            }
        }
    }
    
}
