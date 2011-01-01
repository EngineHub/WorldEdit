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

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bags.*;

public class HMPlayerInventoryBlockBag extends BlockBag {
    /**
     * Player instance.
     */
    private Player player;
    /**
     * The player's inventory;
     */
    private Item[] items;
    
    /**
     * Construct the object.
     * 
     * @param player
     */
    public HMPlayerInventoryBlockBag(Player player) {
        this.player = player;
    }
    
    /**
     * Loads inventory on first use.
     */
    private void loadInventory() {
        if (items == null) {
            items = player.getInventory().getContents();
        }
    }
    
    /**
     * Get the player.
     * 
     * @return
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Get a block.
     *
     * @param id
     */
    public void fetchBlock(int id) throws BlockBagException {
        if (id == 0) {
            throw new IllegalArgumentException("Can't fetch air block");
        }
        
        loadInventory();
        
        boolean found = false;
        
        for (int slot = 0; slot < items.length; slot++) {
            Item item = items[slot];
            
            if (item == null) continue;
            
            if (item.getItemId() == id) {
                int amount = item.getAmount();
                
                // Unlimited
                if (amount < 0) {
                    return;
                }
                
                if (amount > 1) {
                    item.setAmount(amount - 1);
                    found = true;
                } else {
                    items[slot] = null; 
                    found = true;
                }
                
                break;
            }
        }
        
        if (found) {
        } else {
            throw new OutOfBlocksException();
        }
    }
    
    /**
     * Store a block.
     * 
     * @param id
     */
    public void storeBlock(int id) throws BlockBagException {
        if (id == 0) {
            throw new IllegalArgumentException("Can't store air block");
        }
        
        loadInventory();
        
        boolean found = false;
        int freeSlot = -1;
        
        for (int slot = 0; slot < items.length; slot++) {
            Item item = items[slot];
            
            // Delay using up a free slot until we know there are no stacks
            // of this item to merge into
            if (item == null) {
                if (freeSlot == -1) {
                    freeSlot = slot;
                }
                continue;
            }
            
            if (item.getItemId() == id) {
                int amount = item.getAmount();
                
                // Unlimited
                if (amount < 0) {
                    return;
                }
                
                if (amount < 64) {
                    item.setAmount(amount + 1);
                    found = true;
                    break;
                }
            }
        }

        if (!found && freeSlot > -1) {
            items[freeSlot] = new Item(id, 1);
            found = true;
        }
        
        if (found) {
        } else {
            throw new OutOfSpaceException(id);
        }
    }
    
    /**
     * Flush any changes. This is called at the end.
     */
    public void flushChanges() {
        if (items != null) {
            setContents(player.getInventory(), items);
            items = null;
        }
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSourcePosition(Vector pos) {
    }
    /**
     * Adds a position to be used a source.
     *
     * @param pos
     * @return
     */
    public void addSingleSourcePosition(Vector pos) {
    }
    
    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    private static void setContents(Inventory itemArray, Item[] contents) {
        int size = itemArray.getContentsSize();

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(contents[i], i);
            }
        }
    }
}
