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

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIEvent;

import org.spout.api.entity.Entity;
import org.spout.api.geo.discrete.Point;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.MaterialRegistry;
import org.spout.api.player.Player;

public class SpoutPlayer extends LocalPlayer {
    private Player player;
    @SuppressWarnings("unused")
    private WorldEditPlugin plugin;

    public SpoutPlayer(WorldEditPlugin plugin, ServerInterface server, Player player) {
        super(server);
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public int getItemInHand() {
        ItemStack itemStack = player.getEntity().getInventory().getCurrentItem();
        return itemStack != null ? itemStack.getMaterial().getId() : 0;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public WorldVector getPosition() {
        Point loc = player.getEntity().getPosition();
        return new WorldVector(SpoutUtil.getLocalWorld(loc.getWorld()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double getPitch() {
        return player.getEntity().getPitch();
    }

    @Override
    public double getYaw() {
        return player.getEntity().getYaw();
    }

    @Override
    public void giveItem(int type, int amt) {
        player.getEntity().getInventory().addItem(new ItemStack(MaterialRegistry.get((short) type), amt), false);
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
        final Entity entity = player.getEntity();
        entity.setPosition(SpoutUtil.toPoint(entity.getWorld(), pos));
        entity.setPitch(pitch);
        entity.setYaw(yaw);
        player.getNetworkSynchronizer().setPositionDirty();
    }

    @Override
    public String[] getGroups() {
        return player.getGroups();
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return new SpoutPlayerBlockBag(player);
    }

    @Override
    public boolean hasPermission(String perm) {
        return player.hasPermission(perm);
    }

    @Override
    public LocalWorld getWorld() {
        return SpoutUtil.getLocalWorld(player.getEntity().getWorld());
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();

        if (params.length > 0) {
            player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75" + event.getTypeId()
                    + "|" + StringUtil.joinString(params, "|"));
        } else {
            player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75" + event.getTypeId());
        }
    }

    @Override
    public void dispatchCUIHandshake() {
        player.sendRawMessage("\u00A75\u00A76\u00A74\u00A75");
    }

    public Player getPlayer() {
        return player;
    }
}
