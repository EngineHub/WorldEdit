// $Id$
/*
 * Copyright (c) 2011 toi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
*/

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.BlockWorldVector;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVectorFace;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * This class uses an inefficient method to figure out what block a player
 * is looking towards.
 * 
 * Originally written by toi. It was ported to WorldEdit and trimmed down by
 * sk89q. Thanks to Raphfrk for optimization of toi's original class.
 * 
 * @author toi
 */
public class TargetBlock {
    private LocalWorld world;
    private int maxDistance;
    private double checkDistance, curDistance;
    private Vector targetPos = new Vector();
    private Vector targetPosDouble = new Vector();
    private Vector prevPos = new Vector();
    private Vector offset = new Vector();

    /**
     * Constructor requiring a player, uses default values
     * 
     * @param player player to work with
     */
    public TargetBlock(LocalPlayer player) {
        this.world = player.getWorld();
        this.setValues(player.getPosition(), player.getYaw(), player.getPitch(),
                300, 1.65, 0.2);
    }

    /**
     * Constructor requiring a player, max distance and a checking distance
     * 
     * @param player LocalPlayer to work with
     * @param maxDistance how far it checks for blocks
     * @param checkDistance how often to check for blocks, the smaller the more precise
     */
    public TargetBlock(LocalPlayer player, int maxDistance, double checkDistance) {
        this.world = player.getWorld();
        this.setValues(player.getPosition(), player.getYaw(), player.getPitch(),
                maxDistance, 1.65, checkDistance);
    }

    /**
     * Set the values, all constructors uses this function
     * 
     * @param loc location of the view
     * @param xRotation
     * @param yRotation
     * @param maxDistance how far it checks for blocks
     * @param viewPos where the view is positioned in y-axis
     * @param checkDistance how often to check for blocks, the smaller the more precise
     */
    private void setValues(Vector loc, double xRotation, double yRotation,
            int maxDistance, double viewHeight, double checkDistance) {
        this.maxDistance = maxDistance;
        this.checkDistance = checkDistance;
        this.curDistance = 0;
        xRotation = (xRotation + 90) % 360;
        yRotation = yRotation * -1;

        double h = (checkDistance * Math.cos(Math.toRadians(yRotation)));
        
        offset = new Vector((h * Math.cos(Math.toRadians(xRotation))),
                            (checkDistance * Math.sin(Math.toRadians(yRotation))),
                            (h * Math.sin(Math.toRadians(xRotation))));

        targetPosDouble = loc.add(0, viewHeight, 0);
        targetPos = targetPosDouble.toBlockPoint();
        prevPos = targetPos;
    }

    /**
     * Returns any block at the sight. Returns null if out of range or if no
     * viable target was found. Will try to return the last valid air block it finds.
     * 
     * @return Block
     */
    public BlockWorldVector getAnyTargetBlock() {
        boolean searchForLastBlock = true;
        BlockWorldVector lastBlock = null;
        while (getNextBlock() != null) {
            if (world.getBlockType(getCurrentBlock()) == BlockID.AIR) {
                if(searchForLastBlock) {
                    lastBlock = getCurrentBlock();
                    if (lastBlock.getBlockY() <= 0 || lastBlock.getBlockY() >= 127) {
                        searchForLastBlock = false;
                    }
                }
            } else {
                break;
            }
        }
        BlockWorldVector currentBlock = getCurrentBlock();
        return (currentBlock != null ? currentBlock : lastBlock);
    }
    
    /**
     * Returns the block at the sight. Returns null if out of range or if no
     * viable target was found
     * 
     * @return Block
     */
    public BlockWorldVector getTargetBlock() {
        while ((getNextBlock() != null)
                && (world.getBlockType(getCurrentBlock()) == 0));
        return getCurrentBlock();
    }

    /**
     * Returns the block at the sight. Returns null if out of range or if no
     * viable target was found
     * 
     * @return Block
     */
    public BlockWorldVector getSolidTargetBlock() {
        while ((getNextBlock() != null)
                && BlockType.canPassThrough(world.getBlockType(getCurrentBlock())));
        return getCurrentBlock();
    }

    /**
     * Get next block
     * 
     * @return next block position
     */
    public BlockWorldVector getNextBlock() {
        prevPos = targetPos;
        do {
            curDistance += checkDistance;
            
            targetPosDouble = offset.add(targetPosDouble.getX(),
                                         targetPosDouble.getY(),
                                         targetPosDouble.getZ());
            targetPos = targetPosDouble.toBlockPoint();
        } while (curDistance <= maxDistance
                && targetPos.getBlockX() == prevPos.getBlockX()
                && targetPos.getBlockY() == prevPos.getBlockY()
                && targetPos.getBlockZ() == prevPos.getBlockZ());
        
        if (curDistance > maxDistance) {
            return null;
        }

        return new BlockWorldVector(world, targetPos);
    }

    /**
     * Returns the current block along the line of vision
     * 
     * @return block position
     */
    public BlockWorldVector getCurrentBlock() {
        if (curDistance > maxDistance) {
            return null;
        } else {
            return new BlockWorldVector(world, targetPos);
        }
    }

    /**
     * Returns the previous block in the aimed path
     * 
     * @return block position
     */
    public BlockWorldVector getPreviousBlock() {
        return new BlockWorldVector(world, prevPos);
    }

    public WorldVectorFace getAnyTargetBlockFace() {
        getAnyTargetBlock();
        return WorldVectorFace.getWorldVectorFace(world, getCurrentBlock(), getPreviousBlock());
    }

    public WorldVectorFace getTargetBlockFace() {
        getAnyTargetBlock();
        return WorldVectorFace.getWorldVectorFace(world, getCurrentBlock(), getPreviousBlock());
    }
}