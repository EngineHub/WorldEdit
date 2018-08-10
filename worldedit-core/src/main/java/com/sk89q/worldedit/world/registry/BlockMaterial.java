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

package com.sk89q.worldedit.world.registry;

/**
 * Describes the material for a block.
 */
public interface BlockMaterial {

    /**
     * Get whether this block is a full sized cube.
     *
     * @return the value of the test
     */
    boolean isFullCube();

    /**
     * Get whether this block is opaque.
     *
     * @return the value of the test
     */
    boolean isOpaque();

    /**
     * Get whether this block emits a Redstone signal.
     *
     * @return the value of the test
     */
    boolean isPowerSource();

    /**
     * Get whether this block is a liquid.
     *
     * @return the value of the test
     */
    boolean isLiquid();

    /**
     * Get whether this block is a solid.
     *
     * @return the value of the test
     */
    boolean isSolid();

    /**
     * Get the hardness factor for this block.
     *
     * @return the hardness factor
     */
    float getHardness();

    /**
     * Get the resistance factor for this block.
     *
     * @return the resistance factor
     */
    float getResistance();

    /**
     * Get the slipperiness factor for this block.
     *
     * @return the slipperiness factor
     */
    float getSlipperiness();

    /**
     * Get the light value for this block.
     *
     * @return the light value
     */
    int getLightValue();

    /**
     * Get whether this block breaks when it is pushed by a piston.
     *
     * @return true if the block breaks
     */
    boolean isFragileWhenPushed();

    /**
     * Get whether this block can be pushed by a piston.
     *
     * @return true if the block cannot be pushed
     */
    boolean isUnpushable();

    /**
     * Get whether this block is ticked randomly.
     *
     * @return true if this block is ticked randomly
     */
    boolean isTicksRandomly();

    /**
     * Get whether this block prevents movement.
     *
     * @return true if this block blocks movement
     */
    boolean isMovementBlocker();

    /**
     * Get whether this block will burn.
     *
     * @return true if this block will burn
     */
    boolean isBurnable();

    /**
     * Get whether this block needs to be broken by a tool for maximum
     * speed.
     *
     * @return true if a tool is required
     */
    boolean isToolRequired();

    /**
     * Get whether this block is replaced when a block is placed over it
     * (for example, tall grass).
     *
     * @return true if the block is replaced
     */
    boolean isReplacedDuringPlacement();

    /**
     * Get whether this block is translucent.
     *
     * @return true if the block is translucent
     */
    boolean isTranslucent();

    /**
     * Gets whether the block has a container (Item container)
     *
     * @return If it has a container
     */
    boolean hasContainer();
}
