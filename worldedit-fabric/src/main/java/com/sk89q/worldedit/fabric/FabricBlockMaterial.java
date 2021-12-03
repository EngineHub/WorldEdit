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

package com.sk89q.worldedit.fabric;

import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.PassthroughBlockMaterial;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;

import javax.annotation.Nullable;

// TODO Finish delegating all methods
/**
 * Fabric block material that pulls as much info as possible from the Minecraft
 * Material, and passes the rest to another implementation, typically the
 * bundled block info.
 */
public class FabricBlockMaterial extends PassthroughBlockMaterial {

    private final Material delegate;
    private final BlockState block;

    public FabricBlockMaterial(Material delegate, BlockState block, @Nullable BlockMaterial secondary) {
        super(secondary);
        this.delegate = delegate;
        this.block = block;
    }

    @Override
    public boolean isAir() {
        return delegate == Material.AIR || super.isAir();
    }

    @Override
    public boolean isFullCube() {
        // return block.isFullCube();
        return super.isFullCube();
    }

    @Override
    public boolean isOpaque() {
        return delegate.blocksLight();
    }

    @Override
    public boolean isPowerSource() {
        return block.emitsRedstonePower();
    }

    @Override
    public boolean isLiquid() {
        return delegate.isLiquid();
    }

    @Override
    public boolean isSolid() {
        return delegate.isSolid();
    }

    @Override
    public float getHardness() {
        return block.getBlock().getHardness();
    }

    @Override
    public float getResistance() {
        return block.getBlock().getBlastResistance();
    }

    @Override
    public float getSlipperiness() {
        return block.getBlock().getSlipperiness();
    }

    @Override
    public int getLightValue() {
        return block.getLuminance();
    }

    @Override
    public int getMapColor() {
        return delegate.getColor().color;
    }

    @Override
    public boolean isFragileWhenPushed() {
        return delegate.getPistonBehavior() == PistonBehavior.DESTROY;
    }

    @Override
    public boolean isUnpushable() {
        return delegate.getPistonBehavior() == PistonBehavior.BLOCK;
    }

    @Override
    public boolean isTicksRandomly() {
        return block.hasRandomTicks();
    }

    @Override
    public boolean isMovementBlocker() {
        return delegate.blocksMovement();
    }

    @Override
    public boolean isBurnable() {
        return delegate.isBurnable();
    }

    @Override
    public boolean isToolRequired() {
        return block.isToolRequired();
    }

    @Override
    public boolean isReplacedDuringPlacement() {
        return delegate.isReplaceable();
    }

    @Override
    public boolean isTranslucent() {
        // return block.isTranslucent();
        return super.isTranslucent();
    }

    @Override
    public boolean hasContainer() {
        return block.hasBlockEntity();
    }

}
