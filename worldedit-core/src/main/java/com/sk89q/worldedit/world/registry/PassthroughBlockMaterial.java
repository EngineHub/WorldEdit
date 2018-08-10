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

import javax.annotation.Nullable;

public class PassthroughBlockMaterial implements BlockMaterial {

    @Nullable private final BlockMaterial blockMaterial;

    public PassthroughBlockMaterial(@Nullable BlockMaterial material) {
        this.blockMaterial = material;
    }

    @Override
    public boolean isFullCube() {
        if (blockMaterial == null) {
            return true;
        } else {
            return blockMaterial.isFullCube();
        }
    }

    @Override
    public boolean isOpaque() {
        if (blockMaterial == null) {
            return true;
        } else {
            return blockMaterial.isOpaque();
        }
    }

    @Override
    public boolean isPowerSource() {
        if (blockMaterial == null) {
            return false;
        } else {
            return blockMaterial.isPowerSource();
        }
    }

    @Override
    public boolean isLiquid() {
        if (blockMaterial == null) {
            return false;
        } else {
            return blockMaterial.isLiquid();
        }
    }

    @Override
    public boolean isSolid() {
        if (blockMaterial == null) {
            return true;
        } else {
            return blockMaterial.isSolid();
        }
    }

    @Override
    public float getHardness() {
        if (blockMaterial == null) {
            return 0;
        } else {
            return blockMaterial.getHardness();
        }
    }

    @Override
    public float getResistance() {
        if (blockMaterial == null) {
            return 0;
        } else {
            return blockMaterial.getResistance();
        }
    }

    @Override
    public float getSlipperiness() {
        if (blockMaterial == null) {
            return 0;
        } else {
            return blockMaterial.getSlipperiness();
        }
    }

    @Override
    public int getLightValue() {
        if (blockMaterial == null) {
            return 0;
        } else {
            return blockMaterial.getLightValue();
        }
    }

    @Override
    public boolean isFragileWhenPushed() {
        if (blockMaterial == null) {
            return false;
        } else {
            return blockMaterial.isFragileWhenPushed();
        }
    }

    @Override
    public boolean isUnpushable() {
        if (blockMaterial == null) {
            return false;
        } else {
            return blockMaterial.isUnpushable();
        }
    }

    @Override
    public boolean isTicksRandomly() {
        if (blockMaterial == null) {
            return true;
        } else {
            return blockMaterial.isTicksRandomly();
        }
    }

    @Override
    public boolean isMovementBlocker() {
        if (blockMaterial == null) {
            return true;
        } else {
            return blockMaterial.isMovementBlocker();
        }
    }

    @Override
    public boolean isBurnable() {
        if (blockMaterial == null) {
            return true;
        } else {
            return blockMaterial.isOpaque();
        }
    }

    @Override
    public boolean isToolRequired() {
        if (blockMaterial == null) {
            return true;
        } else {
            return blockMaterial.isToolRequired();
        }
    }

    @Override
    public boolean isReplacedDuringPlacement() {
        if (blockMaterial == null) {
            return false;
        } else {
            return blockMaterial.isReplacedDuringPlacement();
        }
    }

    @Override
    public boolean isTranslucent() {
        if (blockMaterial == null) {
            return !isOpaque();
        } else {
            return blockMaterial.isTranslucent();
        }
    }

    @Override
    public boolean hasContainer() {
        if (blockMaterial == null) {
            return false;
        } else {
            return blockMaterial.hasContainer();
        }
    }
}
