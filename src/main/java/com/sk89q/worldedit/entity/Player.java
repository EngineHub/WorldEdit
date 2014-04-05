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
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.inventory.BlockBag;

/**
 * A player.
 */
public interface Player extends Entity {

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
    PlayerDirection getCardinalDirection(int yawOffset);

    /**
     * Get the ID of the item that the player is holding.
     *
     * @return the item id of the item the player is holding
     */
    int getItemInHand();

    /**
     * Get the Block that the player is holding.
     *
     * @return the item id of the item the player is holding
     */
    BaseBlock getBlockInHand() throws WorldEditException;

    /**
     * Gives the player an item.
     *
     * @param type The item id of the item to be given to the player
     * @param amount How many items in the stack
     */
    void giveItem(int type, int amount);

    /**
     * Get this actor's block bag.
     *
     * @return the actor's block bag
     */
    BlockBag getInventoryBlockBag();

    /**
     * Return whether this actor has creative mode.
     *
     * @return true if creative mode is enabled
     */
    boolean hasCreativeMode();

}
