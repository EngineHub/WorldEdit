package com.sk89q.worldedit.canarymod;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Inventory;
import net.canarymod.api.inventory.Item;

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bags.BlockBagException;
import com.sk89q.worldedit.bags.OutOfBlocksException;
import com.sk89q.worldedit.bags.OutOfSpaceException;

public class CanaryBlockBag extends BlockBag {
    /**
     * Player instance.
     */
    private Player player;
    /**
     * The player's inventory;
     */
    private Item[] items;

    /**
     * Construct a new BlockBag for the given {@link Player}
     * 
     * @param player
     */
    public CanaryBlockBag(Player player) {
        this.player = player;
    }

    /**
     * Get the player for this {@link BlockBag}
     * 
     * @return
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Load or update the inventory data from the {@link Player} object
     */
    private void loadInventory() {
        if (items == null) {
            items = player.getInventory().getContents();
        }
    }

    @Override
    public void flushChanges() {
        if (items != null) {
            setContents(player.getInventory(), items);
            items = null;
        }
    }

    @Override
    public void addSourcePosition(WorldVector pos) {
    }

    @Override
    public void addSingleSourcePosition(WorldVector pos) {
    }

    @Override
    public void fetchBlock(int id) throws BlockBagException {
        if (id == 0) {
            throw new IllegalArgumentException("Can't fetch air block");
        }

        loadInventory();

        boolean found = false;

        for (int slot = 0; slot < items.length; slot++) {
            Item item = items[slot];

            if (item == null)
                continue;

            if (item.getId() == id) {
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

    @Override
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

            if (item.getId() == id) {
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
            items[freeSlot] = Canary.factory().getItemFactory().newItem(id, 0, 1);
            found = true;
        }

        if (found) {
        } else {
            throw new OutOfSpaceException(id);
        }
    }

    /**
     * Set the contents of an ItemArray.
     * 
     * @param itemArray
     * @param contents
     */
    private static void setContents(Inventory itemArray, Item[] contents) {
        int size = itemArray.getSize();

        for (int i = 0; i < size; i++) {
            if (contents[i] == null) {
                itemArray.removeItem(i);
            } else {
                itemArray.setSlot(i, contents[i]);
            }
        }
    }

}
