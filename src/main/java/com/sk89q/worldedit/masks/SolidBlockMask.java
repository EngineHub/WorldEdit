package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * Works like {@link ExistingBlockMask}, except also dealing with non-solid non-air blocks the same way as with air.
 */
public class SolidBlockMask extends AbstractMask {
    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return !BlockType.canPassThrough(editSession.getBlockType(pos), editSession.getBlockData(pos));
    }
}
