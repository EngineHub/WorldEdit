/*
 * WorldEdit
 * Copyright (C) 2012 sk89q <http://www.sk89q.com> and contributors
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

// $Id$


package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bags.BlockBagException;
import com.sk89q.worldedit.bags.OutOfBlocksException;
import com.sk89q.worldedit.bags.OutOfSpaceException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockID;
import org.spout.api.inventory.InventoryBase;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.Material;
import org.spout.api.entity.Player;
import org.spout.vanilla.material.VanillaMaterials;
import org.spout.vanilla.util.VanillaPlayerUtil;

public class SpoutPlayerBlockBag extends BlockBag {
    /**
     * Player instance.
     */
    private Player player;
    /**
     * The player's inventory;
     */
    private ItemStack[] items;

    /**
     * Construct the object.
     *
     * @param player
     */
    public SpoutPlayerBlockBag(Player player) {
        this.player = player;
    }

    /**
     * Loads inventory on first use.
     */
    private void loadInventory() {
        if (items == null) {
            items = VanillaPlayerUtil.getInventory(player).getContents();
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
     * @param item
     */
    @Override
    public void fetchItem(BaseItem item) throws BlockBagException {
        final short id = (short)item.getType();
        final short damage = item.getDamage();
        int amount = (item instanceof BaseItemStack) ? ((BaseItemStack) item).getAmount() : 1;
        assert(amount == 1);
        Material mat = VanillaMaterials.getMaterial(id);
        if (mat.hasSubMaterials()) {
            mat = mat.getSubMaterial(damage);
        }

        if (id == BlockID.AIR) {
            throw new IllegalArgumentException("Can't fetch air block");
        }

        loadInventory();

        boolean found = false;

        for (int slot = 0; slot < items.length; ++slot) {
            ItemStack spoutItem = items[slot];

            if (spoutItem == null) {
                continue;
            }

            if (!spoutItem.getMaterial().equals(mat)) {
                // Type id or damage value doesn't fit
                continue;
            }

            int currentAmount = spoutItem.getAmount();
            if (currentAmount < 0) {
                // Unlimited
                return;
            }

            if (currentAmount > 1) {
                spoutItem.setAmount(currentAmount - 1);
                found = true;
            } else {
                items[slot] = null;
                found = true;
            }

            break;
        }

        if (!found) {
            throw new OutOfBlocksException();
        }
    }

    /**
     * Store a block.
     *
     * @param item
     */
    @Override
    public void storeItem(BaseItem item) throws BlockBagException {
        final short id = (short) item.getType();
        final short damage = item.getDamage();
        Material mat = VanillaMaterials.getMaterial(id);
        if (mat.hasSubMaterials()) {
            mat = mat.getSubMaterial(damage);
        }
        int amount = (item instanceof BaseItemStack) ? ((BaseItemStack) item).getAmount() : 1;
        assert(amount <= mat.getMaxStackSize());

        if (id == BlockID.AIR) {
            throw new IllegalArgumentException("Can't store air block");
        }

        loadInventory();

        int freeSlot = -1;

        for (int slot = 0; slot < items.length; ++slot) {
            ItemStack spoutItem = items[slot];

            if (spoutItem == null) {
                // Delay using up a free slot until we know there are no stacks
                // of this item to merge into

                if (freeSlot == -1) {
                    freeSlot = slot;
                }
                continue;
            }

            if (!spoutItem.getMaterial().equals(mat)) {
                // Type id or damage value doesn't fit
                continue;
            }

            int currentAmount = spoutItem.getAmount();
            if (currentAmount < 0) {
                // Unlimited
                return;
            }
            if (currentAmount >= mat.getMaxStackSize()) {
                // Full stack
                continue;
            }

            int spaceLeft = mat.getMaxStackSize() - currentAmount;
            if (spaceLeft >= amount) {
                spoutItem.setAmount(currentAmount + amount);
                return;
            }

            spoutItem.setAmount(mat.getMaxStackSize());
            amount -= spaceLeft;
        }

        if (freeSlot > -1) {
            items[freeSlot] = new ItemStack(mat, amount);
            return;
        }

        throw new OutOfSpaceException(id);
    }

    /**
     * Flush any changes. This is called at the end.
     */
    @Override
    public void flushChanges() {
        if (items != null) {
            InventoryBase inv = VanillaPlayerUtil.getInventory(player);
            for (int i = 0; i < items.length && i < inv.getSize(); ++i) {
                inv.setItem(i, items[i]);
            }
            items = null;
        }
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     */
    @Override
    public void addSourcePosition(WorldVector pos) {
    }

    /**
     * Adds a position to be used a source.
     *
     * @param pos
     */
    @Override
    public void addSingleSourcePosition(WorldVector pos) {
    }
}
