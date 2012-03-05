package com.sk89q.worldedit.data;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

public interface Chunk {

    /**
     * Get the block ID of a block.
     *
     * @param pos
     * @return
     * @throws DataException
     */
    public int getBlockID(Vector pos) throws DataException;
    
    /**
     * Get the block data of a block.
     *
     * @param pos
     * @return
     * @throws DataException
     */
    public int getBlockData(Vector pos) throws DataException;
    
    
    /**
     * Get a block;
     *
     * @param pos
     * @return block
     * @throws DataException
     */
    public BaseBlock getBlock(Vector pos) throws DataException;
}
