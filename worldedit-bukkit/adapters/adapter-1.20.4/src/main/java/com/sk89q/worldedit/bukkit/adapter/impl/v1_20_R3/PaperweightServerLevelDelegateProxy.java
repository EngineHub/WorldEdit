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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_20_R3;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.entity.EntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class PaperweightServerLevelDelegateProxy implements InvocationHandler {

    private final EditSession editSession;
    private final ServerLevel serverLevel;
    private final PaperweightAdapter adapter;

    private PaperweightServerLevelDelegateProxy(EditSession editSession, ServerLevel serverLevel, PaperweightAdapter adapter) {
        this.editSession = editSession;
        this.serverLevel = serverLevel;
        this.adapter = adapter;
    }

    public static WorldGenLevel newInstance(EditSession editSession, ServerLevel serverLevel, PaperweightAdapter adapter) {
        return (WorldGenLevel) Proxy.newProxyInstance(
            serverLevel.getClass().getClassLoader(),
            serverLevel.getClass().getInterfaces(),
            new PaperweightServerLevelDelegateProxy(editSession, serverLevel, adapter)
        );
    }

    @Nullable
    private BlockEntity getBlockEntity(BlockPos blockPos) {
        BlockEntity tileEntity = this.serverLevel.getChunkAt(blockPos).getBlockEntity(blockPos);
        if (tileEntity == null) {
            return null;
        }
        BlockEntity newEntity = tileEntity.getType().create(blockPos, getBlockState(blockPos));
        newEntity.load((CompoundTag) adapter.fromNative(this.editSession.getFullBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ())).getNbtReference().getValue()));

        return newEntity;
    }

    private BlockState getBlockState(BlockPos blockPos) {
        return adapter.adapt(this.editSession.getBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
    }

    private boolean setBlock(BlockPos blockPos, BlockState blockState) {
        try {
            return editSession.setBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ()), adapter.adapt(blockState));
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean removeBlock(BlockPos blockPos, boolean bl) {
        try {
            return editSession.setBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ()), BlockTypes.AIR.getDefaultState());
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean addEntity(Entity entity) {
        Vec3 pos = entity.getPosition(0.0f);
        Location location = new Location(BukkitAdapter.adapt(serverLevel.getWorld()), pos.x(), pos.y(), pos.z());

        ResourceLocation id = serverLevel.registryAccess().registryOrThrow(Registries.ENTITY_TYPE).getKey(entity.getType());
        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        BaseEntity baseEntity = new BaseEntity(EntityTypes.get(id.toString()), LazyReference.from(() -> (LinCompoundTag) adapter.toNative(tag)));

        return editSession.createEntity(location, baseEntity) != null;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "a_", "getBlockState", "addFreshEntityWithPassengers" -> {
                if (args.length == 1 && args[0] instanceof BlockPos blockPos) {
                    // getBlockState
                    return getBlockState(blockPos);
                } else if (args.length >= 1 && args[0] instanceof Entity entity) {
                    // addFreshEntityWithPassengers
                    return addEntity(entity);
                }
            }
            case "c_", "getBlockEntity" -> {
                if (args.length == 1 && args[0] instanceof BlockPos blockPos) {
                    // getBlockEntity
                    return getBlockEntity(blockPos);
                }
            }
            case "a", "setBlock", "removeBlock", "destroyBlock" -> {
                if (args.length >= 2 && args[0] instanceof BlockPos blockPos && args[1] instanceof BlockState blockState) {
                    // setBlock
                    return setBlock(blockPos, blockState);
                } else if (args.length >= 2 && args[0] instanceof BlockPos blockPos && args[1] instanceof Boolean bl) {
                    // removeBlock (and also matches destroyBlock)
                    return removeBlock(blockPos, bl);
                }
            }
            case "j", "addEntity", "addFreshEntity" -> {
                if (args.length >= 1 && args[0] instanceof Entity entity) {
                    return addEntity(entity);
                }
            }
            default -> { }
        }

        return method.invoke(this.serverLevel, args);
    }

}
