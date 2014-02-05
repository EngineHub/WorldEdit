package com.sk89q.worldedit.masks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;

public class BlockMask extends AbstractMask {
    private final List<MaskedBlockMask> blocks;

    public BlockMask() {
        blocks = new ArrayList<MaskedBlockMask>();
    }

    public BlockMask(List<MaskedBlockMask> types) {
        this.blocks = types;
    }

    public BlockMask(MaskedBlockMask block) {
        this(Collections.singletonList(block));
    }

    public BlockMask(BlockType type) {
        this(new MaskedBlockMask(new BaseBlock(type.getID())));
    }

    public void add(BaseBlock block) {
        blocks.add(new MaskedBlockMask(block));
    }

    public void add(MaskedBlockMask block) {
        blocks.add(block);
    }

    public void addAll(Collection<BaseBlock> blocks) {
        blocks.addAll(blocks);
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return MaskedBlockMask.containsFuzzy(blocks, editSession.getBlock(pos));
    }
}
