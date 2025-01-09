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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4;

import com.sk89q.worldedit.world.registry.BlockMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

public class PaperweightBlockMaterial implements BlockMaterial {

    private final BlockState block;

    public PaperweightBlockMaterial(BlockState block) {
        this.block = block;
    }

    @Override
    public boolean isAir() {
        return block.isAir();
    }

    @Override
    public boolean isFullCube() {
        return Block.isShapeFullBlock(block.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
    }

    @Override
    public boolean isOpaque() {
        return block.canOcclude();
    }

    @Override
    public boolean isPowerSource() {
        return block.isSignalSource();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isLiquid() {
        return block.liquid();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSolid() {
        return block.isSolid();
    }

    @Override
    public float getHardness() {
        return block.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    @Override
    @SuppressWarnings("deprecation")
    public float getResistance() {
        return block.getBlock().getExplosionResistance();
    }

    @Override
    public float getSlipperiness() {
        return block.getBlock().getFriction();
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getLightValue() {
        return block.getLightEmission();
    }

    @Override
    public boolean isFragileWhenPushed() {
        return block.getPistonPushReaction() == PushReaction.DESTROY;
    }

    @Override
    public boolean isUnpushable() {
        return block.getPistonPushReaction() == PushReaction.BLOCK;
    }

    @Override
    public boolean isTicksRandomly() {
        return block.isRandomlyTicking();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isMovementBlocker() {
        return block.blocksMotion();
    }

    @Override
    public boolean isBurnable() {
        return block.ignitedByLava();
    }

    @Override
    public boolean isToolRequired() {
        return block.requiresCorrectToolForDrops();
    }

    @Override
    public boolean isReplacedDuringPlacement() {
        return block.canBeReplaced();
    }

    @Override
    public boolean isTranslucent() {
        return !block.canOcclude();
    }

    @Override
    public boolean hasContainer() {
        return block.getBlock() instanceof EntityBlock entityBlock
                && entityBlock.newBlockEntity(BlockPos.ZERO, block) instanceof Clearable;
    }

}
