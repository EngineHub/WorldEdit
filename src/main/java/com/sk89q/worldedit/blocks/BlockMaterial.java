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

package com.sk89q.worldedit.blocks;

/**
 * Describes the material for a block.
 */
public interface BlockMaterial {

    boolean isRenderedAsNormalBlock();

    boolean isFullCube();

    boolean isOpaque();

    boolean isPowerSource();

    boolean isLiquid();

    boolean isSolid();

    float getHardness();

    float getResistance();

    float getSlipperiness();

    boolean isGrassBlocking();

    float getAmbientOcclusionLightValue();

    int getLightOpacity();

    int getLightValue();

    boolean isFragileWhenPushed();

    boolean isUnpushable();

    boolean isAdventureModeExempt();

    boolean isTicksRandomly();

    boolean isUsingNeighborLight();

    boolean isMovementBlocker();

    boolean isBurnable();

    boolean isToolRequired();

    boolean isReplacedDuringPlacement();

}
