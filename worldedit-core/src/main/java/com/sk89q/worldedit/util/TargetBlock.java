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

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

/**
 * This class uses an inefficient method to figure out what block a player
 * is looking towards.
 *
 * <p>Originally written by toi. It was ported to WorldEdit and trimmed down by
 * sk89q. Thanks to Raphfrk for optimization of toi's original class.</p>
 */
public class TargetBlock {

    private final World world;

    private int maxDistance;
    private double checkDistance;
    private double curDistance;
    private BlockVector3 targetPos = BlockVector3.ZERO;
    private Vector3 targetPosDouble = Vector3.ZERO;
    private BlockVector3 prevPos = BlockVector3.ZERO;
    private Vector3 offset = Vector3.ZERO;

    // the mask which dictates when to stop a trace - defaults to stopping at non-air blocks
    private Mask stopMask;
    // the mask which dictates when to stop a solid block trace - default to BlockMaterial#isMovementBlocker
    private Mask solidMask;

    /**
     * Constructor requiring a player, uses default values.
     *
     * @param player player to work with
     */
    public TargetBlock(Player player) {
        this.world = player.getWorld();
        this.setValues(player.getLocation().toVector(), player.getLocation().getYaw(), player.getLocation().getPitch(),
                300, 1.65, 0.2);
        this.stopMask = new ExistingBlockMask(world);
        this.solidMask = new SolidBlockMask(world);
    }

    /**
     * Constructor requiring a player, max distance and a checking distance.
     *
     * @param player Player to work with
     * @param maxDistance how far it checks for blocks
     * @param checkDistance how often to check for blocks, the smaller the more precise
     */
    public TargetBlock(Player player, int maxDistance, double checkDistance) {
        this.world = player.getWorld();
        this.setValues(player.getLocation().toVector(), player.getLocation().getYaw(), player.getLocation().getPitch(), maxDistance, 1.65, checkDistance);
        this.stopMask = new ExistingBlockMask(world);
        this.solidMask = new SolidBlockMask(world);
    }

    /**
     * Set the mask used for determine where to stop traces.
     * Setting to null will restore the default.
     *
     * @param stopMask the mask used to stop traces
     */
    public void setStopMask(@Nullable Mask stopMask) {
        if (stopMask == null) {
            this.stopMask = new ExistingBlockMask(world);
        } else {
            this.stopMask = stopMask;
        }
    }

    /**
     * Set the mask used for determine where to stop solid block traces.
     * Setting to null will restore the default.
     *
     * @param solidMask the mask used to stop solid block traces
     */
    public void setSolidMask(@Nullable Mask solidMask) {
        if (solidMask == null) {
            this.solidMask = new SolidBlockMask(world);
        } else {
            this.solidMask = solidMask;
        }
    }

    /**
     * Set the values, all constructors uses this function.
     *
     * @param loc location of the view
     * @param rotationX the X rotation
     * @param rotationY the Y rotation
     * @param maxDistance how far it checks for blocks
     * @param viewHeight where the view is positioned in y-axis
     * @param checkDistance how often to check for blocks, the smaller the more precise
     */
    private void setValues(Vector3 loc, double rotationX, double rotationY, int maxDistance, double viewHeight, double checkDistance) {
        this.maxDistance = maxDistance;
        this.checkDistance = checkDistance;
        this.curDistance = 0;
        rotationX = (rotationX + 90) % 360;
        rotationY *= -1;

        double h = (checkDistance * Math.cos(Math.toRadians(rotationY)));

        offset = Vector3.at((h * Math.cos(Math.toRadians(rotationX))),
                            (checkDistance * Math.sin(Math.toRadians(rotationY))),
                            (h * Math.sin(Math.toRadians(rotationX))));

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
    public Location getAnyTargetBlock() {
        boolean searchForLastBlock = true;
        Location lastBlock = null;
        while (getNextBlock() != null) {
            if (stopMask.test(targetPos)) {
                break;
            } else {
                if (searchForLastBlock) {
                    lastBlock = getCurrentBlock();
                    if (lastBlock.getBlockY() <= world.getMinY()
                        || lastBlock.getBlockY() >= world.getMaxY()) {
                        searchForLastBlock = false;
                    }
                }
            }
        }
        Location currentBlock = getCurrentBlock();
        return (currentBlock != null ? currentBlock : lastBlock);
    }

    /**
     * Returns the block at the sight. Returns null if out of range or if no
     * viable target was found
     *
     * @return Block
     */
    public Location getTargetBlock() {
        //noinspection StatementWithEmptyBody
        while (getNextBlock() != null && !stopMask.test(targetPos)) {
        }
        return getCurrentBlock();
    }

    /**
     * Returns the block at the sight. Returns null if out of range or if no
     * viable target was found
     *
     * @return Block
     */
    public Location getSolidTargetBlock() {
        //noinspection StatementWithEmptyBody
        while (getNextBlock() != null && !solidMask.test(targetPos)) {
        }
        return getCurrentBlock();
    }

    /**
     * Get next block.
     *
     * @return next block position
     */
    public Location getNextBlock() {
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

        return new Location(world, targetPos.toVector3());
    }

    /**
     * Returns the current block along the line of vision.
     *
     * @return block position
     */
    public Location getCurrentBlock() {
        if (curDistance > maxDistance) {
            return null;
        } else {
            return new Location(world, targetPos.toVector3());
        }
    }

    /**
     * Returns the previous block in the aimed path.
     *
     * @return block position
     */
    public Location getPreviousBlock() {
        return new Location(world, prevPos.toVector3());
    }

    public Location getAnyTargetBlockFace() {
        getAnyTargetBlock();
        Location current = getCurrentBlock();
        if (current != null) {
            return current.setDirection(current.toVector().subtract(getPreviousBlock().toVector()));
        } else {
            return new Location(world, targetPos.toVector3(), Float.NaN, Float.NaN);
        }
    }

    public Location getTargetBlockFace() {
        getTargetBlock();
        if (getCurrentBlock() == null) {
            return null;
        }
        return getCurrentBlock().setDirection(getCurrentBlock().toVector().subtract(getPreviousBlock().toVector()));
    }

}
