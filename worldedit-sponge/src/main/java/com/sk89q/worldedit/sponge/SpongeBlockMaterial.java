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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.PassthroughBlockMaterial;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.MatterTypes;
import org.spongepowered.api.data.type.PushReactions;
import org.spongepowered.api.tag.BlockTypeTags;

import javax.annotation.Nullable;

/**
 * Sponge block material that pulls as much info as possible from the Minecraft
 * Material, and passes the rest to another implementation, typically the
 * bundled block info.
 */
public class SpongeBlockMaterial extends PassthroughBlockMaterial {

    private final BlockState block;

    public SpongeBlockMaterial(BlockState block, @Nullable BlockMaterial secondary) {
        super(secondary);
        this.block = block;
    }

    @Override
    public boolean isAir() {
        return block.type().is(BlockTypeTags.AIR) || super.isAir();
    }

    @Override
    public boolean isOpaque() {
        return ((net.minecraft.world.level.block.state.BlockState) block).canOcclude();
    }

    @Override
    public boolean isLiquid() {
        return block.require(Keys.MATTER_TYPE) == MatterTypes.LIQUID.get();
    }

    @Override
    public boolean isSolid() {
        return block.require(Keys.IS_SOLID);
    }

    @Override
    public boolean isFragileWhenPushed() {
        return block.require(Keys.PUSH_REACTION) == PushReactions.DESTROY.get();
    }

    @Override
    public boolean isUnpushable() {
        return block.require(Keys.PUSH_REACTION) == PushReactions.BLOCK.get();
    }

    @Override
    public boolean isMovementBlocker() {
        return !block.require(Keys.IS_PASSABLE);
    }

    @Override
    public boolean isBurnable() {
        return block.require(Keys.BURNABLE);
    }

    @Override
    public boolean isToolRequired() {
        return ((net.minecraft.world.level.block.state.BlockState) block).requiresCorrectToolForDrops();
    }

    @Override
    public boolean isReplacedDuringPlacement() {
        return block.require(Keys.IS_REPLACEABLE);
    }

}
