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

package com.sk89q.worldedit.spout;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIEvent;
import org.spout.api.geo.discrete.Point;
import org.spout.api.geo.discrete.atomic.Transform;
import org.spout.api.inventory.ItemStack;
import org.spout.api.material.MaterialData;
import org.spout.api.math.Quaternion;
import org.spout.api.math.Vector3;
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
        Point loc = player.getEntity().getTransform().getPosition();
        return new WorldVector(SpoutUtil.getLocalWorld(loc.getWorld()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double getPitch() {
        return player.getEntity().getTransform().getRotation().getAxisAngles().getY();
    }

    @Override
    public double getYaw() {
        return player.getEntity().getTransform().getRotation().getAxisAngles().getZ();
    }

    @Override
    public void giveItem(int type, int amt) {
        player.getEntity().getInventory().addItem(new ItemStack(MaterialData.getMaterial((short)type), amt));
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
        Transform t = player.getEntity().getTransform();
        t.setPosition(new Point(t.getPosition().getWorld(), (float) pos.getX(), (float) pos.getY(), (float) pos.getZ()));
        t.setRotation(new Quaternion(pitch, Vector3.UNIT_Z).rotate(yaw, Vector3.UNIT_Y));
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
        return SpoutUtil.getLocalWorld(player.getEntity().getTransform().getPosition().getWorld());
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
