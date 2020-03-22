package com.sk89q.worldedit.forge.internal;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.forge.ForgeAdapter;
import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.internal.wna.WorldNativeAccess;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerChunkProvider;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Objects;

public class ForgeWorldNativeAccess implements WorldNativeAccess<Chunk, BlockState, BlockPos> {
    private static final int UPDATE = 1, NOTIFY = 2;

    private final WeakReference<World> world;

    public ForgeWorldNativeAccess(WeakReference<World> world) {
        this.world = world;
    }

    private World getWorld() {
        return Objects.requireNonNull(world.get(), "The reference to the world was lost");
    }

    @Override
    public Chunk getChunk(int x, int z) {
        return getWorld().getChunk(x, z);
    }

    @Override
    public BlockState toNative(com.sk89q.worldedit.world.block.BlockState state) {
        int stateId = BlockStateIdAccess.getBlockStateId(state);
        return BlockStateIdAccess.isValidInternalId(stateId)
            ? Block.getStateById(stateId)
            : ForgeAdapter.adapt(state);
    }

    @Override
    public BlockState getBlockState(Chunk chunk, BlockPos position) {
        return chunk.getBlockState(position);
    }

    @Nullable
    @Override
    public BlockState setBlockState(Chunk chunk, BlockPos position, BlockState state) {
        return chunk.setBlockState(position, state, false);
    }

    @Override
    public BlockState getValidBlockForPosition(BlockState block, BlockPos position) {
        return Block.getValidBlockForPosition(block, getWorld(), position);
    }

    @Override
    public BlockPos getPosition(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    @Override
    public void updateLightingForBlock(BlockPos position) {
        getWorld().getChunkProvider().getLightManager().checkBlock(position);
    }

    @Override
    public boolean updateTileEntity(BlockPos position, CompoundTag tag) {
        CompoundNBT nativeTag = NBTConverter.toNative(tag);
        return TileEntityUtils.setTileEntity(getWorld(), position, nativeTag);
    }

    @Override
    public void markBlockRangeForRenderUpdate(BlockPos position, BlockState oldState, BlockState newState) {
        getWorld().markBlockRangeForRenderUpdate(position, oldState, newState);
    }

    @Override
    public void notifyBlockUpdate(BlockPos position, BlockState oldState, BlockState newState) {
        getWorld().notifyBlockUpdate(position, oldState, newState, UPDATE | NOTIFY);
    }

    @Override
    public boolean isChunkTicking(Chunk chunk) {
        return chunk.getLocationType().isAtLeast(ChunkHolder.LocationType.TICKING);
    }

    @Override
    public void markBlockChanged(BlockPos position) {
        ((ServerChunkProvider) getWorld().getChunkProvider()).markBlockChanged(position);
    }

    @Override
    public void notifyNeighbors(BlockPos pos, BlockState oldState, BlockState newState) {
        getWorld().notifyNeighbors(pos, oldState.getBlock());
        if (newState.hasComparatorInputOverride()) {
            getWorld().updateComparatorOutputLevel(pos, newState.getBlock());
        }
    }

    @Override
    public void updateNeighbors(BlockPos pos, BlockState oldState, BlockState newState) {
        World world = getWorld();
        oldState.updateDiagonalNeighbors(world, pos, NOTIFY);
        newState.updateNeighbors(world, pos, NOTIFY);
        newState.updateDiagonalNeighbors(world, pos, NOTIFY);
    }

    @Override
    public void onBlockStateChange(BlockPos pos, BlockState oldState, BlockState newState) {
        getWorld().onBlockStateChange(pos, oldState, newState);
    }
}
