// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package org.enginehub.worldedit.bukkit;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.enginehub.auth.PermissionRequest;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.bukkit.BukkitPlayerBlockBag;
import com.sk89q.worldedit.cui.CUIEvent;
import com.sk89q.worldedit.foundation.World;

/**
 * An {@link org.enginehub.common.Actor} wrapper for Bukkit players.
 */
public class PlayerActor extends LocalPlayer {

    private final Player player;

    /**
     * Construct a player actor.
     *
     * @param player a Bukkit player
     */
    PlayerActor(Player player) {
        this.player = player;
    }

    @Override
    public Player getHandle() {
        return player;
    }

    @Override
    public boolean hasPermission(PermissionRequest request) {
        return true; // @TODO: Permissions
    }

    @Override
    public boolean hasPermission(String permission, World targetWorld) {
        return true; // @TODO: Permissions
    }

    @Override
    public boolean hasPermission(String permission) {
        return true; // @TODO: Permissions
    }

    @Override
    public int getItemInHand() {
        ItemStack itemStack = player.getItemInHand();
        return itemStack != null ? itemStack.getTypeId() : 0;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public WorldVector getPosition() {
        Location loc = player.getLocation();
        return new WorldVector(BukkitUtils.getLocalWorld(loc.getWorld()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double getPitch() {
        return player.getLocation().getPitch();
    }

    @Override
    public double getYaw() {
        return player.getLocation().getYaw();
    }

    @Override
    public void giveItem(int type, int amt) {
        player.getInventory().addItem(new ItemStack(type, amt));
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage(part);
        }
    }

    @Override
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("\u00A7d" + part);
        }
    }

    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("\u00A77" + part);
        }
    }

    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage("\u00A7c" + part);
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        player.teleport(new Location(player.getWorld(), pos.getX(), pos.getY(),
                pos.getZ(), yaw, pitch));
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return new BukkitPlayerBlockBag(player);
    }

    @Override
    public LocalWorld getWorld() {
        return BukkitUtils.getLocalWorld(player.getWorld());
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean hasCreativeMode() {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    @Override
    public String getStringId() {
        return getName();
    }
}
