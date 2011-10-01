// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.data.*;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author sk89q
 */
public class NoteBlock extends BaseBlock implements TileEntityBlock {
    /**
     * Stores the pitch.
     */
    private byte note;

    /**
     * Construct the note block.
     */
    public NoteBlock() {
        super(BlockID.NOTE_BLOCK);
        this.note = 0;
    }

    /**
     * Construct the note block.
     *
     * @param data
     */
    public NoteBlock(int data) {
        super(BlockID.NOTE_BLOCK, data);
        this.note = 0;
    }

    /**
     * Construct the note block.
     *
     * @param data 
     * @param note
     */
    public NoteBlock(int data, byte note) {
        super(BlockID.NOTE_BLOCK, data);
        this.note = note;
    }

    /**
     * @return the note
     */
    public byte getNote() {
        return note;
    }

    /**
     * @param note the note to set
     */
    public void setNote(byte note) {
        this.note = note;
    }

    /**
     * Return the name of the title entity ID.
     *
     * @return title entity ID
     */
    public String getTileEntityID() {
        return "Music";
    }
    
    /**
     * Store additional tile entity data. Returns true if the data is used.
     *
     * @return map of values
     * @throws DataException
     */
    public Map<String,Tag> toTileEntityNBT()
            throws DataException {
        Map<String,Tag> values = new HashMap<String,Tag>();
        values.put("note", new ByteTag("note", note));
        return values;
    }

    /**
     * Get additional information from the title entity data.
     *
     * @param values
     * @throws DataException
     */
    public void fromTileEntityNBT(Map<String,Tag> values)
            throws DataException  {
        if (values == null) {
            return;
        }
        
        Tag t;

        t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag)t).getValue().equals("Music")) {
            throw new DataException("'Music' tile entity expected");
        }

        t = values.get("note");
        if (t instanceof ByteTag) {
            note = ((ByteTag)t).getValue();
        }
    }
}
