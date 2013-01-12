package com.sk89q.worldedit;

import com.sk89q.worldedit.bags.BlockBag;

public class EditSessionFactory {

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world
     * @param maxBlocks
     */
    public EditSession getEditSession(LocalWorld world, int maxBlocks) {
        return new EditSession(world, maxBlocks);
    }

    /**
     * Construct an edit session with a maximum number of blocks.
     *
     * @param world
     * @param maxBlocks
     * @param player
     */
    public EditSession getEditSession(LocalWorld world, int maxBlocks, LocalPlayer player) {
        return this.getEditSession(world, maxBlocks);
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world
     * @param maxBlocks
     * @param blockBag
     */
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag) {
        return new EditSession(world, maxBlocks, blockBag);
    }

    /**
     * Construct an edit session with a maximum number of blocks and a block bag.
     *
     * @param world
     * @param maxBlocks
     * @param blockBag
     * @param player
     */
    public EditSession getEditSession(LocalWorld world, int maxBlocks, BlockBag blockBag, LocalPlayer player) {
        return this.getEditSession(world, maxBlocks, blockBag);
    }

}
