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

package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.extent.inventory.OutOfBlocksException;
import com.sk89q.worldedit.extent.inventory.OutOfSpaceException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import org.spout.api.inventory.Inventory;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.Material;
import org.spout.api.entity.Player;
import org.spout.vanilla.plugin.component.inventory.PlayerInventory;
import org.spout.vanilla.plugin.component.living.neutral.Human;
import org.spout.vanilla.plugin.material.VanillaMaterials;

public class SpoutPlayerBlockBag extends BlockBag {
    /**
     * Player instance.
     */
    private Player player;
    /**
     * The player's inventory;
     */
    private ItemInfo items;

    private static class ItemInfo {
        ItemStack[] inventory;
        boolean includesFullInventory;

        public ItemInfo(ItemStack[] inventory, boolean full) {
            this.inventory = inventory;
            this.includesFullInventory = full;
        }
    }

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
            items = getViewableItems(player);
        }
    }

    private ItemInfo getViewableItems(Player player) {
        boolean includesFullInventory = true;
        ItemStack[] items;
        Human human = player.get(Human.class);
        PlayerInventory inv = player.get(PlayerInventory.class);
        if (human.isCreative()) {
            includesFullInventory = false;
            items = new ItemStack[inv.getQuickbar().size()];
        } else {
            items = new ItemStack[inv.getQuickbar().size() + inv.getMain().size()];
        }
        int index = 0;
        for (; index < inv.getQuickbar().size(); ++index) {
            items[index] = inv.getQuickbar().get(index);
        }

        if (!human.isCreative()) {
            for (int i = 0; i < inv.getMain().size() && index < items.length; ++i) {
                items[index++] = inv.getMain().get(i);
            }
        }
        return new ItemInfo(items, includesFullInventory);
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
        final short damage = item.getData();
        int amount = (item instanceof BaseItemStack) ? ((BaseItemStack) item).getAmount() : 1;
        assert(amount == 1);
        Material mat = VanillaMaterials.getMaterial(id, damage);

        if (mat == VanillaMaterials.AIR) {
            throw new IllegalArgumentException("Can't fetch air block");
        }

        loadInventory();

        boolean found = false;

        for (int slot = 0; slot < items.inventory.length; ++slot) {
            ItemStack spoutItem = items.inventory[slot];

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
                items.inventory[slot] = null;
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
        final short damage = item.getData();
        Material mat = VanillaMaterials.getMaterial(id, damage);
        int amount = (item instanceof BaseItemStack) ? ((BaseItemStack) item).getAmount() : 1;
        assert(amount <= mat.getMaxStackSize());

        if (mat == VanillaMaterials.AIR) {
            throw new IllegalArgumentException("Can't store air block");
        }

        loadInventory();

        int freeSlot = -1;

        for (int slot = 0; slot < items.inventory.length; ++slot) {
            ItemStack spoutItem = items.inventory[slot];

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
            items.inventory[freeSlot] = new ItemStack(mat, amount);
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
            PlayerInventory inv = player.get(PlayerInventory.class);
            for (int i = 0; i < inv.getQuickbar().size(); i++) {
                inv.getQuickbar().set(i, items.inventory[i]);
            }

            for (int i = 0; i < inv.getMain().size(); ++i) {
                inv.getMain().set(i, items.inventory[inv.getQuickbar().size() + i]);
            }
            items = null;
        }
    }

    /**
     * Adds a position to be used a source.
     * (TODO: Figure out what this does)
     * @param pos
     */
    @Override
    public void addSourcePosition(WorldVector pos) {
    }

    /**
     * Adds a position to be used a source.
     * (TODO: Figure out what this does)
     * @param pos
     */
    @Override
    public void addSingleSourcePosition(WorldVector pos) {
    }
}
