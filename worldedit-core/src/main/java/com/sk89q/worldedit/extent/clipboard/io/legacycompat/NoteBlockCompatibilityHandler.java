package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.registry.state.IntegerProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Map;

public class NoteBlockCompatibilityHandler implements NBTCompatibilityHandler {
    private static final IntegerProperty NoteProperty;

    static {
        IntegerProperty temp;
        try {
            temp = (IntegerProperty) (Property<?>) BlockTypes.NOTE_BLOCK.getProperty("note");
        } catch (NullPointerException | IllegalArgumentException | ClassCastException e) {
            temp = null;
        }
        NoteProperty = temp;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
        return NoteProperty != null && block.getBlockType() == BlockTypes.NOTE_BLOCK;
    }

    @Override
    public <B extends BlockStateHolder<B>> B updateNBT(B block, Map<String, Tag> values) {
        // note that instrument was note stored (in state or nbt) previously.
        // it will be updated to the block below when it gets set into the world for the first time
        Tag noteTag = values.get("note");
        if (noteTag instanceof ByteTag) {
            Byte note = ((ByteTag) noteTag).getValue();
            if (note != null) {
                return (B) block.with(NoteProperty, (int) note);
            }
        }
        return block;
    }
}
