package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.patterns.Pattern;

public class MaskedBlockMask extends AbstractMask implements Pattern {
    private final BaseBlock block;
    private final int mask;

    public MaskedBlockMask(BaseBlock block, int mask) {
        this.block = block;
        this.mask = mask;
    }

    public MaskedBlockMask(BaseBlock block) {
        this(block, ~0);
    }

    public static MaskedBlockMask fromValues(int blockId, int data, int mask) {
        return new MaskedBlockMask(new BaseBlock(blockId, data), mask);
    }

    public BaseBlock getBlock() {
        return block;
    }

    public int getMask() {
        return mask;
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {

        return matches(editSession.getBlock(pos));
    }

    private boolean matches(BaseBlock block) {
        if (block.getType() != this.block.getType()) {
            return false;
        }

        return (block.getData() & this.mask) == (this.block.getData() & this.mask);
    }

    @Override
    public BaseBlock next(Vector pos) {
        return block;
    }

    @Override
    public BaseBlock next(int x, int y, int z) {
        return block;
    }

    public static boolean containsFuzzy(Iterable<MaskedBlockMask> masks, BaseBlock block) {
        for (MaskedBlockMask mask : masks) {
            if (mask.matches(block)) {
                return true;
            }
        }

        return false;
    }
}
