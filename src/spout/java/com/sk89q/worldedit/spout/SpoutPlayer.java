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

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;

import org.spout.api.Client;
import org.spout.api.chat.style.ChatStyle;
import org.spout.api.component.impl.SceneComponent;
import org.spout.api.geo.discrete.Point;
import org.spout.api.inventory.ItemStack;
import org.spout.api.entity.Player;
import org.spout.api.math.QuaternionMath;
import org.spout.api.math.Vector3;
import org.spout.vanilla.api.inventory.Slot;
import org.spout.vanilla.plugin.component.inventory.PlayerInventory;
import org.spout.vanilla.plugin.component.living.neutral.Human;
import org.spout.vanilla.api.material.VanillaMaterial;
import org.spout.vanilla.plugin.material.VanillaMaterials;

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
        if (player.has(Human.class)) {
            Slot slot = player.get(PlayerInventory.class).getQuickbar().getSelectedSlot();
            if (slot.get() == null) {
                return 0;
            }
            return ((VanillaMaterial) slot.get().getMaterial()).getMinecraftId();
        } else {
            return 0;
        }
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public WorldVector getPosition() {
        Point loc = player.getScene().getPosition();
        return new WorldVector(SpoutUtil.getLocalWorld(loc.getWorld()),
                loc.getX(), loc.getY(), loc.getZ());
    }

    @Override
    public double getPitch() {
        return player.getScene().getRotation().getYaw();
    }

    @Override
    public double getYaw() {
        return player.getScene().getRotation().getYaw();
    }

    @Override
    public void giveItem(int type, int amt) {
        if (player.has(Human.class)) {
            player.get(PlayerInventory.class).add(new ItemStack(VanillaMaterials.getMaterial((short) type), amt));
        }
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
            player.sendMessage(ChatStyle.PINK, part);
        }
    }

    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage(ChatStyle.GRAY, part);
        }
    }

    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            player.sendMessage(ChatStyle.RED,  part);
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        SceneComponent component = player.getScene();
        player.teleport(SpoutUtil.toPoint(player.getWorld(), pos));
        component.setRotation(QuaternionMath.rotation(pitch, yaw, component.getRotation().getRoll()));
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
        return SpoutUtil.getLocalWorld(player.getWorld());
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        player.getSession().send(player.getSession().getEngine() instanceof Client, new WorldEditCUIMessage(event));
    }

    public Player getPlayer() {
        return player;
    }
}
