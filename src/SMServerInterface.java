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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseItem;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author sk89q
 */
public class SMServerInterface implements ServerInterface {
    /**
     * Set block type.
     *
     * @param pt
     * @param type
     * @return
     */
    public boolean setBlockType(Vector pt, int type) {
        // Can't set colored cloth or crash
        if ((type >= 21 && type <= 34) || type == 36) {
            return false;
        }
        return etc.getMCServer().e.d(pt.getBlockX(), pt.getBlockY(),
                pt.getBlockZ(), type);
    }
    
    /**
     * Get block type.
     *
     * @param pt
     * @return
     */
    public int getBlockType(Vector pt) {
        return etc.getMCServer().e.a(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }

    /**
     * Set block data.
     *
     * @param pt
     * @param data
     * @return
     */
    public void setBlockData(Vector pt, int data) {
        etc.getServer().setBlockData(pt.getBlockX(), pt.getBlockY(),
                        pt.getBlockZ(), data);
    }

    /**
     * Get block data.
     *
     * @param pt
     * @return
     */
    public int getBlockData(Vector pt) {
        return etc.getMCServer().e.b(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
    }
    
    /**
     * Set sign text.
     *
     * @param pt
     * @param text
     */
    public void setSignText(Vector pt, String[] text) {
        Sign signData = (Sign)etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return;
        }
        for (byte i = 0; i < 4; i++) {
            signData.setText(i, text[i]);
        }
        signData.update();
    }
    
    /**
     * Get sign text.
     *
     * @param pt
     * @return
     */
    public String[] getSignText(Vector pt) {
        Sign signData = (Sign)etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
        if (signData == null) {
            return new String[]{"", "", "", ""};
        }
        String[] text = new String[4];
        for (byte i = 0; i < 4; i++) {
            text[i] = signData.getText(i);
        }
        return text;
    }

    /**
     * Gets the contents of chests.
     *
     * @param pt
     * @return
     */
    public Map<Byte,Countable<BaseItem>> getChestContents(Vector pt) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (!(cblock instanceof Chest)) {
            return null;
        }

        Chest chest = (Chest)cblock;
        Map<Byte,Countable<BaseItem>> items =
                new HashMap<Byte,Countable<BaseItem>>();

        for (byte i = 0; i <= 26; i++) {
            Item item = chest.getItemFromSlot(i);
            if (item != null) {
                items.put(i, new Countable(new BaseItem((short)item.getItemId()), item.getAmount()));
            }
        }

        return items;
    }

    /**
     * Sets a chest slot.
     *
     * @param pt
     * @param slot
     * @param item
     * @param amount
     * @return
     */
    public boolean setChestSlot(Vector pt, byte slot, BaseItem item, int amount) {
        ComplexBlock cblock = etc.getServer().getComplexBlock(
                pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());

        if (!(cblock instanceof Chest)) {
            return false;
        }

        Chest chest = (Chest)cblock;
        chest.addItem(new Item(item.getID(), amount, slot));
        chest.update();
        return true;
    }
}
