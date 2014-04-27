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

package com.sk89q.worldedit.entity;

import com.sk89q.worldedit.PlayerDirection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.WorldVectorFace;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;

/**
 * A reference to an instance of an entity that exists in an {@link Extent}
 * and thus would have position and similar details.
 * </p>
 * This object cannot be directly cloned because it represents a particular
 * instance of an entity, but a {@link BaseEntity} can be created from
 * this entity (or at least, it will be possible in the future), which
 * can then be used to spawn new instances of that particular entity
 * description.
 */
public interface Entity {

    /**
     * Find a position for the actor to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The actor will be teleported to
     * that free position.
     *
     * @param searchPos search position
     */
    void findFreePosition(WorldVector searchPos);

    /**
     * Set the actor on the ground.
     *
     * @param searchPos The location to start searching from
     */
    void setOnGround(WorldVector searchPos);

    /**
     * Find a position for the player to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The player will be teleported to
     * that free position.
     */
    void findFreePosition();

    /**
     * Go up one level to the next free space above.
     *
     * @return true if a spot was found
     */
    boolean ascendLevel();

    /**
     * Go up one level to the next free space above.
     *
     * @return true if a spot was found
     */
    boolean descendLevel();

    /**
     * Ascend to the ceiling above.
     *
     * @param clearance How many blocks to leave above the player's head
     * @return whether the player was moved
     */
    boolean ascendToCeiling(int clearance);

    /**
     * Ascend to the ceiling above.
     *
     * @param clearance How many blocks to leave above the player's head
     * @param alwaysGlass Always put glass under the player
     * @return whether the player was moved
     */
    boolean ascendToCeiling(int clearance, boolean alwaysGlass);

    /**
     * Just go up.
     *
     * @param distance How far up to teleport
     * @return whether the player was moved
     */
    boolean ascendUpwards(int distance);

    /**
     * Just go up.
     *
     * @param distance How far up to teleport
     * @param alwaysGlass Always put glass under the player
     * @return whether the player was moved
     */
    boolean ascendUpwards(int distance, boolean alwaysGlass);

    /**
     * Make the player float in the given blocks.
     *
     * @param x The X coordinate of the block to float in
     * @param y The Y coordinate of the block to float in
     * @param z The Z coordinate of the block to float in
     */
    void floatAt(int x, int y, int z, boolean alwaysGlass);

    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    WorldVector getBlockIn();

    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    WorldVector getBlockOn();

    /**
     * Get the point of the block being looked at. May return null.
     * Will return the farthest away air block if useLastBlock is true and no other block is found.
     *
     * @param range How far to checks for blocks
     * @param useLastBlock Try to return the last valid air block found.
     * @return point
     */
    WorldVector getBlockTrace(int range, boolean useLastBlock);

    WorldVectorFace getBlockTraceFace(int range, boolean useLastBlock);

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range How far to checks for blocks
     * @return point
     */
    WorldVector getBlockTrace(int range);

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range How far to checks for blocks
     * @return point
     */
    WorldVector getSolidBlockTrace(int range);

    /**
     * Get the player's cardinal direction (N, W, NW, etc.). May return null.
     *
     * @return the direction
     */
    PlayerDirection getCardinalDirection();

    /**
     * Get the location of this entity.
     *
     * @return the location of the entity
     */
    Location getLocation();

    /**
     * Get the actor's position.
     * </p>
     * If the actor has no permission, then return a dummy location.
     *
     * @return the actor's position
     */
    WorldVector getPosition();

    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    double getPitch();

    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    double getYaw();

    /**
     * Pass through the wall that you are looking at.
     *
     * @param range How far to checks for blocks
     * @return whether the player was pass through
     */
    boolean passThroughForwardWall(int range);

    /**
     * Move the player.
     *
     * @param pos Where to move them
     * @param pitch The pitch (up/down) of the player's view
     * @param yaw The yaw (left/right) of the player's view
     */
    void setPosition(Vector pos, float pitch, float yaw);

    /**
     * Move the player.
     *
     * @param pos Where to move them
     */
    void setPosition(Vector pos);

    /**
     * Get the world that this entity is on.
     *
     * @return the world
     */
    World getWorld();

}
