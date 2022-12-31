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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.fabric.internal.NBTConverter;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.fluid.FluidType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class FabricEditSessionDelegate implements WorldGenLevel {

    private final EditSession editSession;
    private final ServerLevel level;

    public FabricEditSessionDelegate(EditSession editSession, ServerLevel level) {
        this.editSession = editSession;
        this.level = level;
    }

    @Override
    public long getSeed() {
        return this.level.getSeed();
    }

    @Override
    public ServerLevel getLevel() {
        return this.level;
    }

    @Override
    public long nextSubTickCount() {
        return this.level.nextSubTickCount();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return this.level.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return this.level.getFluidTicks();
    }

    @Override
    public LevelData getLevelData() {
        return this.level.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos blockPos) {
        return this.level.getCurrentDifficultyAt(blockPos);
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.level.getServer();
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.level.getChunkSource();
    }

    @Override
    public RandomSource getRandom() {
        // TODO investigate overriding this for supplying seeds
        return this.level.getRandom();
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos blockPos, SoundEvent soundEvent, SoundSource soundSource, float f, float g) {
        this.level.playSound(player, blockPos, soundEvent, soundSource, f, g);
    }

    @Override
    public void addParticle(ParticleOptions particleOptions, double d, double e, double f, double g, double h, double i) {
        this.level.addParticle(particleOptions, d, e, f, g, h, i);
    }

    @Override
    public void levelEvent(@Nullable Player player, int i, BlockPos blockPos, int j) {
        this.level.levelEvent(player, i, blockPos, j);
    }

    @Override
    public void gameEvent(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context) {
        this.level.gameEvent(gameEvent, vec3, context);
    }

    @Override
    public float getShade(Direction direction, boolean bl) {
        return this.level.getShade(direction, bl);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos blockPos) {
        BlockEntity tileEntity = this.level.getChunkAt(blockPos).getBlockEntity(blockPos);
        if (tileEntity == null) {
            return null;
        }
        BlockEntity newEntity = tileEntity.getType().create(blockPos, getBlockState(blockPos));
        newEntity.load(NBTConverter.toNative(this.editSession.getFullBlock(FabricAdapter.adapt(blockPos)).getNbtReference().getValue()));

        return newEntity;
    }

    @Override
    public BlockState getBlockState(BlockPos blockPos) {
        return FabricAdapter.adapt(this.editSession.getBlock(FabricAdapter.adapt(blockPos)));
    }

    @Override
    public FluidState getFluidState(BlockPos blockPos) {
        return this.level.getFluidState(blockPos);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB aabb, Predicate<? super Entity> predicate) {
        return this.level.getEntities(entity, aabb, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB aabb, Predicate<? super T> predicate) {
        return this.level.getEntities(entityTypeTest, aabb, predicate);
    }

    @Override
    public List<? extends Player> players() {
        return this.level.players();
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int i, int j, ChunkStatus chunkStatus, boolean bl) {
        return this.level.getChunk(i, j, chunkStatus, bl);
    }

    @Override
    public int getHeight(Heightmap.Types types, int i, int j) {
        return this.level.getHeight(types, i, j);
    }

    @Override
    public int getSkyDarken() {
        return this.level.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.level.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int i, int j, int k) {
        return this.level.getUncachedNoiseBiome(i, j, k);
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return this.level.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return this.level.dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.level.enabledFeatures();
    }

    @Override
    public boolean isStateAtPosition(BlockPos blockPos, Predicate<BlockState> predicate) {
        return predicate.test(this.getBlockState(blockPos));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos blockPos, Predicate<FluidState> predicate) {
        return predicate.test(this.getFluidState(blockPos));
    }

    @Override
    public boolean setBlock(BlockPos blockPos, BlockState blockState, int i, int j) {
        try {
            return editSession.setBlock(FabricAdapter.adapt(blockPos), FabricAdapter.adapt(blockState));
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean removeBlock(BlockPos blockPos, boolean bl) {
        try {
            return editSession.setBlock(FabricAdapter.adapt(blockPos), BlockTypes.AIR.getDefaultState());
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean destroyBlock(BlockPos blockPos, boolean bl, @Nullable Entity entity, int i) {
        return removeBlock(blockPos, bl);
    }
}
