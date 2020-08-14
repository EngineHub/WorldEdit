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

package com.sk89q.worldedit.world.registry;

import javax.annotation.Nullable;

import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

public class PassthroughBlockMaterial implements BlockMaterial {

    private static final SimpleBlockMaterial DEFAULT_MATERIAL = new SimpleBlockMaterial();

    static {
        DEFAULT_MATERIAL.setFullCube(true);
        DEFAULT_MATERIAL.setOpaque(true);
        DEFAULT_MATERIAL.setSolid(true);
        DEFAULT_MATERIAL.setTicksRandomly(true);
        DEFAULT_MATERIAL.setMovementBlocker(true);
        DEFAULT_MATERIAL.setBurnable(true);
        DEFAULT_MATERIAL.setToolRequired(true);
    }

    private final BlockMaterial blockMaterial;

    public PassthroughBlockMaterial(@Nullable BlockMaterial material) {
        this.blockMaterial = firstNonNull(material, DEFAULT_MATERIAL);
    }

    @Override
    public boolean isAir() {
        return blockMaterial.isAir();
    }

    @Override
    public boolean isFullCube() {
        return blockMaterial.isFullCube();
    }

    @Override
    public boolean isOpaque() {
        return blockMaterial.isOpaque();
    }

    @Override
    public boolean isPowerSource() {
        return blockMaterial.isPowerSource();
    }

    @Override
    public boolean isLiquid() {
        return blockMaterial.isLiquid();
    }

    @Override
    public boolean isSolid() {
        return blockMaterial.isSolid();
    }

    @Override
    public float getHardness() {
        return blockMaterial.getHardness();
    }

    @Override
    public float getResistance() {
        return blockMaterial.getResistance();
    }

    @Override
    public float getSlipperiness() {
        return blockMaterial.getSlipperiness();
    }

    @Override
    public int getLightValue() {
        return blockMaterial.getLightValue();
    }

    @Override
    public boolean isFragileWhenPushed() {
        return blockMaterial.isFragileWhenPushed();
    }

    @Override
    public boolean isUnpushable() {
        return blockMaterial.isUnpushable();
    }

    @Override
    public boolean isTicksRandomly() {
        return blockMaterial.isTicksRandomly();
    }

    @Override
    public boolean isMovementBlocker() {
        return blockMaterial.isMovementBlocker();
    }

    @Override
    public boolean isBurnable() {
        return blockMaterial.isBurnable();
    }

    @Override
    public boolean isToolRequired() {
        return blockMaterial.isToolRequired();
    }

    @Override
    public boolean isReplacedDuringPlacement() {
        return blockMaterial.isReplacedDuringPlacement();
    }

    @Override
    public boolean isTranslucent() {
        return blockMaterial.isTranslucent();
    }

    @Override
    public boolean hasContainer() {
        return blockMaterial.hasContainer();
    }
}
