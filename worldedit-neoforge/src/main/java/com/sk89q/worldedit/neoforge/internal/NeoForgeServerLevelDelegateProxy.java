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

package com.sk89q.worldedit.neoforge.internal;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.neoforge.NeoForgeAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class NeoForgeServerLevelDelegateProxy implements InvocationHandler, AutoCloseable {

    private final EditSession editSession;
    private final ServerLevel serverLevel;
    private final Map<BlockVector3, BlockEntity> createdBlockEntities = new HashMap<>();

    private NeoForgeServerLevelDelegateProxy(EditSession editSession, ServerLevel serverLevel) {
        this.editSession = editSession;
        this.serverLevel = serverLevel;
    }

    public record LevelAndProxy(WorldGenLevel level, NeoForgeServerLevelDelegateProxy proxy) implements AutoCloseable {
        @Override
        public void close() throws MaxChangedBlocksException {
            proxy.close();
        }
    }

    public static LevelAndProxy newInstance(EditSession editSession, ServerLevel serverLevel) {
        NeoForgeServerLevelDelegateProxy proxy = new NeoForgeServerLevelDelegateProxy(editSession, serverLevel);
        return new LevelAndProxy(
            (WorldGenLevel) Proxy.newProxyInstance(
                serverLevel.getClass().getClassLoader(),
                serverLevel.getClass().getInterfaces(),
                proxy
            ),
            proxy
        );
    }

    @Nullable
    private BlockEntity getBlockEntity(BlockPos blockPos) {
        // This doesn't synthesize or load from world. I think editing existing block entities without setting the block
        // (in the context of features) should not be supported in the first place.
        BlockVector3 pos = NeoForgeAdapter.adapt(blockPos);
        return createdBlockEntities.get(pos);
    }

    private BlockState getBlockState(BlockPos blockPos) {
        return NeoForgeAdapter.adapt(this.editSession.getBlock(NeoForgeAdapter.adapt(blockPos)));
    }

    private boolean setBlock(BlockPos blockPos, BlockState blockState) {
        try {
            handleBlockEntity(blockPos, blockState);
            return editSession.setBlock(NeoForgeAdapter.adapt(blockPos), NeoForgeAdapter.adapt(blockState));
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    // For BlockEntity#setBlockState, not sure why it's deprecated
    @SuppressWarnings("deprecation")
    private void handleBlockEntity(BlockPos blockPos, BlockState blockState) {
        BlockVector3 pos = NeoForgeAdapter.adapt(blockPos);
        if (blockState.hasBlockEntity()) {
            if (!(blockState.getBlock() instanceof EntityBlock entityBlock)) {
                // This will probably never happen, as Mojang's own code assumes that
                // hasBlockEntity implies instanceof EntityBlock, but just to be safe...
                throw new AssertionError("BlockState has block entity but block is not an EntityBlock: " + blockState);
            }
            BlockEntity newEntity = entityBlock.newBlockEntity(blockPos, blockState);
            if (newEntity != null) {
                newEntity.setBlockState(blockState);
                createdBlockEntities.put(pos, newEntity);
                // Should we load existing NBT here? This is for feature / structure gen so it seems unnecessary.
                // But it would align with the behavior of the real setBlock method.
                return;
            }
        }
        // Discard any block entity that was previously created if new block is set without block entity
        createdBlockEntities.remove(pos);
    }

    private boolean removeBlock(BlockPos blockPos, boolean bl) {
        return setBlock(blockPos, Blocks.AIR.defaultBlockState());
    }

    private boolean addEntity(Entity entity) {
        Vector3 pos = NeoForgeAdapter.adapt(entity.getPosition(0.0f));
        Location location = new Location(NeoForgeAdapter.adapt(serverLevel), pos.x(), pos.y(), pos.z());
        BaseEntity baseEntity = new NeoForgeEntity(entity).getState();
        return editSession.createEntity(location, baseEntity) != null;
    }

    @Override
    public void close() throws MaxChangedBlocksException {
        for (Map.Entry<BlockVector3, BlockEntity> entry : createdBlockEntities.entrySet()) {
            BlockVector3 blockPos = entry.getKey();
            BlockEntity blockEntity = entry.getValue();
            net.minecraft.nbt.CompoundTag tag = blockEntity.saveWithId(serverLevel.registryAccess());
            editSession.setBlock(
                blockPos,
                NeoForgeAdapter.adapt(blockEntity.getBlockState())
                    .toBaseBlock(LazyReference.from(() -> NBTConverter.fromNative(tag)))
            );
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "getBlockState", "m_8055_" -> {
                if (args.length == 1 && args[0] instanceof BlockPos blockPos) {
                    return getBlockState(blockPos);
                }
            }
            case "getBlockEntity", "m_7702_" -> {
                if (args.length == 1 && args[0] instanceof BlockPos blockPos) {
                    return getBlockEntity(blockPos);
                }
            }
            case "setBlock", "m_7731_" -> {
                if (args.length >= 2 && args[0] instanceof BlockPos blockPos && args[1] instanceof BlockState blockState) {
                    return setBlock(blockPos, blockState);
                }
            }
            case "removeBlock", "destroyBlock", "m_7471_", "m_7740_" -> {
                if (args.length >= 2 && args[0] instanceof BlockPos blockPos && args[1] instanceof Boolean bl) {
                    return removeBlock(blockPos, bl);
                }
            }
            case "addEntity", "m_8872_", "addFreshEntityWithPassengers", "m_47205_" -> {
                if (args.length >= 1 && args[0] instanceof Entity entity) {
                    return addEntity(entity);
                }
            }
            default -> { }
        }

        return method.invoke(this.serverLevel, args);
    }

}
