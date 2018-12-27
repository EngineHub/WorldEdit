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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;

import javax.annotation.Nullable;

/**
 * Represents a player
 */
public interface Player extends Entity, Actor {

    /**
     * Return the world that the player is on.
     *
     * @return the world
     */
    World getWorld();

    /**
     * Returns true if the entity is holding a pick axe.
     *
     * @return whether a pick axe is held
     */
    boolean isHoldingPickAxe();

    /**
     * Get the player's cardinal direction (N, W, NW, etc.) with an offset. May return null.
     * @param yawOffset offset that is added to the player's yaw before determining the cardinal direction
     *
     * @return the direction
     */
    Direction getCardinalDirection(int yawOffset);

    /**
     * Get the item that the player is holding.
     *
     * @return the item the player is holding
     */
    BaseItemStack getItemInHand(HandSide handSide);

    /**
     * Get the Block that the player is holding.
     *
     * @return the item id of the item the player is holding
     */
    BaseBlock getBlockInHand(HandSide handSide) throws WorldEditException;

    /**
     * Gives the player an item.
     *
     * @param itemStack The item to give
     */
    void giveItem(BaseItemStack itemStack);

    /**
     * Get this actor's block bag.
     *
     * @return the actor's block bag
     */
    BlockBag getInventoryBlockBag();

    /**
     * Return this actor's game mode.
     *
     * @return the game mode
     */
    GameMode getGameMode();

    /**
     * Sets the player to the given game mode.
     *
     * @param gameMode The game mode
     */
    void setGameMode(GameMode gameMode);

    /**
     * Find a position for the actor to stand that is not inside a block.
     * Blocks above the player will be iteratively tested until there is
     * a series of two free blocks. The actor will be teleported to
     * that free position.
     *
     * @param searchPos search position
     */
    void findFreePosition(Location searchPos);

    /**
     * Set the actor on the ground.
     *
     * @param searchPos The location to start searching from
     */
    void setOnGround(Location searchPos);

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
    Location getBlockIn();

    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    Location getBlockOn();

    /**
     * Get the point of the block being looked at. May return null.
     * Will return the farthest away air block if useLastBlock is true and no other block is found.
     *
     * @param range how far to checks for blocks
     * @param useLastBlock try to return the last valid air block found
     * @return point
     */
    Location getBlockTrace(int range, boolean useLastBlock);

    /**
     * Get the face that the player is looking at.
     *
     * @param range the range
     * @param useLastBlock try to return the last valid air block found
     * @return a face
     */
    Location getBlockTraceFace(int range, boolean useLastBlock);

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range How far to checks for blocks
     * @return point
     */
    Location getBlockTrace(int range);

    /**
     * Get the point of the block being looked at. May return null.
     *
     * @param range How far to checks for blocks
     * @return point
     */
    Location getSolidBlockTrace(int range);

    /**
     * Get the player's cardinal direction (N, W, NW, etc.). May return null.
     *
     * @return the direction
     */
    Direction getCardinalDirection();

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
     * @param pos where to move them
     * @param pitch the pitch (up/down) of the player's view in degrees
     * @param yaw the yaw (left/right) of the player's view in degrees
     */
    void setPosition(Vector3 pos, float pitch, float yaw);

    /**
     * Move the player.
     *
     * @param pos where to move them
     */
    void setPosition(Vector3 pos);

    /**
     * Sends a fake block to the client.
     *
     * <p>
     *     This block isn't real.
     * </p>
     *
     * @param pos The position of the block
     * @param block The block to send, null to reset
     */
    <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, @Nullable B block);
}
