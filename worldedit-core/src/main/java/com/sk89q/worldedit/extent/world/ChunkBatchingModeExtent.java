package com.sk89q.worldedit.extent.world;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.world.World;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class ChunkBatchingModeExtent extends AbstractDelegateExtent {

    private static BlockVector2D chunkLocation(Vector location) {
        return new BlockVector2D(location.getBlockX() >> 4,
                location.getBlockZ() >> 4);
    }

    // TODO: we can totally write a more efficient version of this
    // i.e. without a new BlockVector2D per block being set/retrieved
    private final Multimap<BlockVector2D, BlockChange> chunkChanges =
            HashMultimap.create();
    private final World world;
    private boolean enabled = true;

    /**
     * Create a new instance with fast mode enabled.
     *
     * @param delegate
     *            - the extent to delegate to
     * @param world
     *            - the world
     */
    public ChunkBatchingModeExtent(Extent delegate, World world) {
        this(delegate, world, true);
    }

    /**
     * Create a new instance.
     *
     * @param delegate
     *            - the extent to delegate to
     * @param world
     *            - the world
     * @param enabled
     *            - true to enable fast mode
     */
    public ChunkBatchingModeExtent(Extent delegate, World world, boolean enabled) {
        super(delegate);
        checkNotNull(world);
        this.world = world;
        this.enabled = enabled;
    }

    /**
     * Return whether chunk batching mode is enabled.
     *
     * @return true if chunk batching mode is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set chunk batching mode enable status.
     *
     * @param enabled
     *            - true to enable chunk batching mode
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            this.chunkChanges.clear();
        }
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block)
            throws WorldEditException {
        if (enabled) {
            // Assume it is in position and will be set.
            return this.chunkChanges.put(chunkLocation(location),
                    new BlockChange(new BlockVector(location), block, block));
        } else {
            return super.setBlock(location, block);
        }
    }

    @Override
    protected Operation commitBefore() {
        return new Operation() {
            
            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                if (chunkChanges.isEmpty()) {
                    return null;
                }
                BlockVector2D minimumChunk = chunkLocation(getMinimumPoint());
                BlockVector2D maximumChunk = chunkLocation(getMaximumPoint());
                for (int z = minimumChunk.getBlockZ(); z < maximumChunk.getBlockZ(); z++) {
                    for (int x = minimumChunk.getBlockX(); x < maximumChunk.getBlockX(); x++) {
                        BlockVector2D chunkPos = new BlockVector2D(x, z);
                        world.checkLoadedChunk(chunkPos.toVector());
                        for (BlockChange change : chunkChanges.get(chunkPos)) {
                            getExtent().setBlock(change.getPosition(), change.getCurrent());
                        }
                    }
                }
                chunkChanges.clear();
                return null;
            }
            
            @Override
            public void cancel() {
            }
            
            @Override
            public void addStatusMessages(List<String> messages) {
            }
        };
    }

}
