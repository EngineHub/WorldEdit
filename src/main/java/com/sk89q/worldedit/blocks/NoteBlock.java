/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.blocks;

import java.util.HashMap;
import java.util.Map;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.DataException;

/**
 * A note block.
 * 
 * @author sk89q
 */
public class NoteBlock extends BaseBlock implements TileEntityBlock {

    private byte note;

    /**
     * Construct the note block with a data value of 0.
     */
    public NoteBlock() {
        super(BlockID.NOTE_BLOCK);
        this.note = 0;
    }

    /**
     * Construct the note block with a given data value.
     * 
     * @param data data value
     */
    public NoteBlock(int data) {
        super(BlockID.NOTE_BLOCK, data);
        this.note = 0;
    }

    /**
     * Construct the note block with a given data value and note.
     * 
     * @param data data value
     * @param note note
     */
    public NoteBlock(int data, byte note) {
        super(BlockID.NOTE_BLOCK, data);
        this.note = note;
    }

    /**
     * Get the note.
     * 
     * @return the note
     */
    public byte getNote() {
        return note;
    }

    /**
     * Set the note.
     * 
     * @param note the note to set
     */
    public void setNote(byte note) {
        this.note = note;
    }
    
    @Override
    public boolean hasNbtData() {
        return true;
    }

    @Override
    public String getNbtId() {
        return "Music";
    }

    @Override
    public CompoundTag getNbtData() {
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("note", new ByteTag("note", note));
        return new CompoundTag(getNbtId(), values);
    }

    @Override
    public void setNbtData(CompoundTag rootTag) throws DataException {
        if (rootTag == null) {
            return;
        }

        Map<String, Tag> values = rootTag.getValue();

        Tag t;

        t = values.get("id");
        if (!(t instanceof StringTag)
                || !((StringTag) t).getValue().equals("Music")) {
            throw new DataException("'Music' tile entity expected");
        }

        t = values.get("note");
        if (t instanceof ByteTag) {
            note = ((ByteTag) t).getValue();
        }
    }
}
