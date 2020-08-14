/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;
import com.sk89q.worldedit.extent.inventory.OutOfBlocksException;
import com.sk89q.worldedit.extent.inventory.OutOfSpaceException;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BukkitPlayerBlockBag extends BlockBag {

    private final Player player;
    private ItemStack[] items;

    /**
     * Construct the object.
     *
     * @param player the player
     */
    public BukkitPlayerBlockBag(Player player) {
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
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public void fetchBlock(BlockState blockState) throws BlockBagException {
        if (blockState.getBlockType().getMaterial().isAir()) {
            throw new IllegalArgumentException("Can't fetch air block");
        }

        loadInventory();

        boolean found = false;

        for (int slot = 0; slot < items.length; ++slot) {
            ItemStack bukkitItem = items[slot];

            if (bukkitItem == null) {
                continue;
            }

            if (!BukkitAdapter.equals(blockState.getBlockType(), bukkitItem.getType())) {
                // Type id doesn't fit
                continue;
            }

            int currentAmount = bukkitItem.getAmount();
            if (currentAmount < 0) {
                // Unlimited
                return;
            }

            if (currentAmount > 1) {
                bukkitItem.setAmount(currentAmount - 1);
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

    @Override
    public void storeBlock(BlockState blockState, int amount) throws BlockBagException {
        if (blockState.getBlockType().getMaterial().isAir()) {
            throw new IllegalArgumentException("Can't store air block");
        }
        if (!blockState.getBlockType().hasItemType()) {
            throw new IllegalArgumentException("This block cannot be stored");
        }

        loadInventory();

        int freeSlot = -1;

        for (int slot = 0; slot < items.length; ++slot) {
            ItemStack bukkitItem = items[slot];

            if (bukkitItem == null) {
                // Delay using up a free slot until we know there are no stacks
                // of this item to merge into

                if (freeSlot == -1) {
                    freeSlot = slot;
                }
                continue;
            }

            if (!BukkitAdapter.equals(blockState.getBlockType(), bukkitItem.getType())) {
                // Type id doesn't fit
                continue;
            }

            int currentAmount = bukkitItem.getAmount();
            if (currentAmount < 0) {
                // Unlimited
                return;
            }
            if (currentAmount >= 64) {
                // Full stack
                continue;
            }

            int spaceLeft = 64 - currentAmount;
            if (spaceLeft >= amount) {
                bukkitItem.setAmount(currentAmount + amount);
                return;
            }

            bukkitItem.setAmount(64);
            amount -= spaceLeft;
        }

        if (freeSlot > -1) {
            items[freeSlot] = BukkitAdapter.adapt(new BaseItemStack(blockState.getBlockType().getItemType(), amount));
            return;
        }

        throw new OutOfSpaceException(blockState.getBlockType());
    }

    @Override
    public void flushChanges() {
        if (items != null) {
            player.getInventory().setContents(items);
            items = null;
        }
    }

    @Override
    public void addSourcePosition(Location pos) {
    }

    @Override
    public void addSingleSourcePosition(Location pos) {
    }

}
