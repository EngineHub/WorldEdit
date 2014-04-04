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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.DataException;

/**
 * Represents a sign block.
 * 
 * @author sk89q
 */
public class SignBlock extends BaseBlock implements TileEntityBlock {

    private String[] text;

    /**
     * Construct the sign without text.
     * 
     * @param type type ID
     * @param data data value (orientation)
     */
    public SignBlock(int type, int data) {
        super(type, data);
        this.text = new String[] { "", "", "", "" };
    }

    /**
     * Construct the sign with text.
     * 
     * @param type type ID
     * @param data data value (orientation)
     * @param text lines of text
     */
    public SignBlock(int type, int data, String[] text) {
        super(type, data);
        if (text == null) {
            this.text = new String[] { "", "", "", "" };
        }
        this.text = text;
    }

    /**
     * Get the text.
     * 
     * @return the text
     */
    public String[] getText() {
        return text;
    }

    /**
     * Set the text.
     * 
     * @param text the text to set
     */
    public void setText(String[] text) {
        if (text == null) {
            throw new IllegalArgumentException("Can't set null text for a sign");
        }
        this.text = text;
    }
    
    @Override
    public boolean hasNbtData() {
        return true;
    }

    @Override
    public String getNbtId() {
        return "Sign";
    }

    @Override
    public CompoundTag getNbtData() {
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("Text1", new StringTag("Text1", text[0]));
        values.put("Text2", new StringTag("Text2", text[1]));
        values.put("Text3", new StringTag("Text3", text[2]));
        values.put("Text4", new StringTag("Text4", text[3]));
        return new CompoundTag(getNbtId(), values);
    }

    @Override
    public void setNbtData(CompoundTag rootTag) throws DataException {
        if (rootTag == null) {
            return;
        }

        Map<String, Tag> values = rootTag.getValue();

        Tag t;

        text = new String[] { "", "", "", "" };

        t = values.get("id");
        if (!(t instanceof StringTag)
                || !((StringTag) t).getValue().equals("Sign")) {
            throw new DataException("'Sign' tile entity expected");
        }

        t = values.get("Text1");
        if (t instanceof StringTag) {
            text[0] = ((StringTag) t).getValue();
        }

        t = values.get("Text2");
        if (t instanceof StringTag) {
            text[1] = ((StringTag) t).getValue();
        }

        t = values.get("Text3");
        if (t instanceof StringTag) {
            text[2] = ((StringTag) t).getValue();
        }

        t = values.get("Text4");
        if (t instanceof StringTag) {
            text[3] = ((StringTag) t).getValue();
        }
    }
}
