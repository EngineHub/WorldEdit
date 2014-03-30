package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockType;

/**
 * @deprecated See {@link com.sk89q.worldedit.function.mask.SolidBlockMask}
 */
@Deprecated
public class SolidBlockMask extends AbstractMask {
    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return !BlockType.canPassThrough(editSession.getBlockType(pos), editSession.getBlockData(pos));
    }
}
