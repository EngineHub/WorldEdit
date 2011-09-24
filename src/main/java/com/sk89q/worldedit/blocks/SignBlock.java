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
public class SignBlock extends BaseBlock implements TileEntityBlock {
    /**
     * Stores the sign's text.
     */
    private String[] text;

    /**
     * Construct the sign without text.
     *
     * @param type
     * @param data
     */
    public SignBlock(int type, int data) {
        super(type, data);
        this.text = new String[]{ "", "", "", "" };
    }

    /**
     * Construct the sign with text.
     *
     * @param type
     * @param data
     * @param text
     */
    public SignBlock(int type, int data, String[] text) {
        super(type, data);
        this.text = text;
    }

    /**
     * @return the text
     */
    public String[] getText() {
        return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String[] text) {
        this.text = text;
    }

    /**
     * Return the name of the title entity ID.
     *
     * @return title entity ID
     */
    public String getTileEntityID() {
        return "Sign";
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
        values.put("Text1", new StringTag("Text1", text[0]));
        values.put("Text2", new StringTag("Text2", text[1]));
        values.put("Text3", new StringTag("Text3", text[2]));
        values.put("Text4", new StringTag("Text4", text[3]));
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

        text = new String[]{ "", "", "", "" };

        t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag)t).getValue().equals("Sign")) {
            throw new DataException("'Sign' tile entity expected");
        }

        t = values.get("Text1");
        if (t instanceof StringTag) {
            text[0] = ((StringTag)t).getValue();
        }

        t = values.get("Text2");
        if (t instanceof StringTag) {
            text[1] = ((StringTag)t).getValue();
        }

        t = values.get("Text3");
        if (t instanceof StringTag) {
            text[2] = ((StringTag)t).getValue();
        }

        t = values.get("Text4");
        if (t instanceof StringTag) {
            text[3] = ((StringTag)t).getValue();
        }
    }
}
