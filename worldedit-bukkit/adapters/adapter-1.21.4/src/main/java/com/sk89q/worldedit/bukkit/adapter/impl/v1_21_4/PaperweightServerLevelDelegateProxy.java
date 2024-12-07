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

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
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
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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
        tileEntity.loadWithComponents(
            (CompoundTag) adapter.fromNative(this.editSession.getFullBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ())).getNbtReference().getValue()),
            this.serverLevel.registryAccess()
        );

        return tileEntity;
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

    private boolean removeBlock(BlockPos blockPos) {
        try {
            return editSession.setBlock(BlockVector3.at(blockPos.getX(), blockPos.getY(), blockPos.getZ()), BlockTypes.AIR.getDefaultState());
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean addEntity(Entity entity) {
        Vec3 pos = entity.getPosition(0.0f);
        Location location = new Location(BukkitAdapter.adapt(serverLevel.getWorld()), pos.x(), pos.y(), pos.z());

        ResourceLocation id = serverLevel.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).getKey(entity.getType());
        CompoundTag tag = new CompoundTag();
        entity.saveWithoutId(tag);
        BaseEntity baseEntity = new BaseEntity(EntityTypes.get(id.toString()), LazyReference.from(() -> (LinCompoundTag) adapter.toNative(tag)));

        return editSession.createEntity(location, baseEntity) != null;
    }

    private static void addMethodHandleToTable(
        ImmutableTable.Builder<String, MethodType, MethodHandle> table,
        String methodName,
        MethodHandle methodHandle
    ) {
        // We want to call these with Object[] args, not plain args
        // We skip the first argument, which is our receiver
        MethodHandle spreader = methodHandle.asSpreader(
            1, Object[].class, methodHandle.type().parameterCount() - 1
        );
        // We drop the first argument, which is our receiver
        // We also drop the return type, which is not important
        table.put(methodName, methodHandle.type().dropParameterTypes(0, 1).changeReturnType(void.class), spreader);
    }

    private static final Table<String, MethodType, MethodHandle> METHOD_MAP;

    static {
        var lookup = MethodHandles.lookup();
        var builder = ImmutableTable.<String, MethodType, MethodHandle>builder();
        try {
            addMethodHandleToTable(
                builder,
                StaticRefraction.GET_BLOCK_STATE,
                lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("getBlockState", BlockPos.class))
            );

            MethodHandle addEntity = lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("addEntity", Entity.class));
            addMethodHandleToTable(
                builder,
                StaticRefraction.ADD_FRESH_ENTITY_WITH_PASSENGERS_ENTITY,
                addEntity
            );
            addMethodHandleToTable(
                builder,
                StaticRefraction.ADD_FRESH_ENTITY_WITH_PASSENGERS_ENTITY_SPAWN_REASON,
                // 0 - this, 1 - entity, 2 - reason
                MethodHandles.dropArguments(addEntity, 2, CreatureSpawnEvent.SpawnReason.class)
            );
            addMethodHandleToTable(
                builder,
                StaticRefraction.ADD_FRESH_ENTITY,
                addEntity
            );
            addMethodHandleToTable(
                builder,
                StaticRefraction.ADD_FRESH_ENTITY_SPAWN_REASON,
                // 0 - this, 1 - entity, 2 - reason
                MethodHandles.dropArguments(addEntity, 2, CreatureSpawnEvent.SpawnReason.class)
            );

            addMethodHandleToTable(
                builder,
                StaticRefraction.GET_BLOCK_ENTITY,
                lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("getBlockEntity", BlockPos.class))
            );

            MethodHandle setBlock = lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("setBlock", BlockPos.class, BlockState.class));
            addMethodHandleToTable(
                builder,
                StaticRefraction.SET_BLOCK,
                // 0 - this, 1 - blockPos, 2 - blockState, 3 - flags
                MethodHandles.dropArguments(setBlock, 3, int.class)
            );
            addMethodHandleToTable(
                builder,
                StaticRefraction.SET_BLOCK_MAX_UPDATE,
                // 0 - this, 1 - blockPos, 2 - blockState, 3 - flags, 4 - maxUpdateDepth
                MethodHandles.dropArguments(setBlock, 3, int.class, int.class)
            );

            MethodHandle removeBlock = lookup.unreflect(PaperweightServerLevelDelegateProxy.class.getDeclaredMethod("removeBlock", BlockPos.class));
            addMethodHandleToTable(
                builder,
                StaticRefraction.REMOVE_BLOCK,
                // 0 - this, 1 - blockPos, 2 - move
                MethodHandles.dropArguments(removeBlock, 2, boolean.class)
            );
            addMethodHandleToTable(
                builder,
                StaticRefraction.DESTROY_BLOCK,
                // 0 - this, 1 - blockPos, 2 - drop
                MethodHandles.dropArguments(removeBlock, 2, boolean.class)
            );
            addMethodHandleToTable(
                builder,
                StaticRefraction.DESTROY_BLOCK_BREAKING_ENTITY,
                // 0 - this, 1 - blockPos, 2 - drop, 3 - breakingEntity
                MethodHandles.dropArguments(removeBlock, 2, boolean.class, Entity.class)
            );
            addMethodHandleToTable(
                builder,
                StaticRefraction.DESTROY_BLOCK_BREAKING_ENTITY_MAX_UPDATE,
                // 0 - this, 1 - blockPos, 2 - drop, 3 - breakingEntity, 4 - maxUpdateDepth
                MethodHandles.dropArguments(removeBlock, 2, boolean.class, Entity.class, int.class)
            );
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to bind to own methods", e);
        }
        METHOD_MAP = builder.build();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodHandle delegate = METHOD_MAP.get(
            // ignore return type, we only need the parameter types
            method.getName(), MethodType.methodType(void.class, method.getParameterTypes())
        );
        if (delegate != null) {
            return delegate.invoke(this, args);
        }
        return method.invoke(this.serverLevel, args);
    }

}
