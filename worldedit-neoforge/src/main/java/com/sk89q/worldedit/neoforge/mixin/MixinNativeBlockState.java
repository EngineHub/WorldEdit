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

package com.sk89q.worldedit.neoforge.mixin;

import com.mojang.serialization.MapCodec;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativePosition;
import com.sk89q.worldedit.internal.wna.NativeWorld;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
@Implements(@Interface(iface = NativeBlockState.class, prefix = "nbs$"))
public abstract class MixinNativeBlockState extends BlockBehaviour.BlockStateBase {
    protected MixinNativeBlockState(Block owner, Reference2ObjectArrayMap<Property<?>, Comparable<?>> values, MapCodec<BlockState> propertiesCodec) {
        super(owner, values, propertiesCodec);
    }

    public boolean nbs$isSame(NativeBlockState other) {
        return this == other;
    }

    public boolean nbs$isSameBlockType(NativeBlockState other) {
        return this.getBlock() == ((BlockState) other).getBlock();
    }

    public boolean nbs$hasBlockEntity() {
        return super.hasBlockEntity();
    }

    public NativeBlockState nbs$updateFromNeighbourShapes(
        NativeWorld world, NativePosition position
    ) {
        return (NativeBlockState) Block.updateFromNeighbourShapes(
            (BlockState) (Object) this, (LevelAccessor) world, (BlockPos) position
        );
    }
}
