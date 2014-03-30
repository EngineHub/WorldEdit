package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Extent;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * Works like {@link ExistingBlockMask}, except also dealing with non-solid non-air blocks the same way as with air.
 */
public class SolidBlockMask extends ExtentAwareMask {
    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        Extent extent = getExtent(editSession);
        return !BlockType.canPassThrough(extent.getBlockType(pos), extent.getBlockData(pos));
    }
}
