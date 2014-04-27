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

package com.sk89q.worldedit.forge;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.Vectors;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;

public class ForgePlayer extends LocalPlayer {
    private EntityPlayerMP player;

    protected ForgePlayer(EntityPlayerMP player) {
        super((ServerInterface) ForgeWorldEdit.inst.getPlatform());
        this.player = player;
    }

    public int getItemInHand() {
        ItemStack is = this.player.getCurrentEquippedItem();
        return is == null ? 0 : is.itemID;
    }

    public String getName() {
        return this.player.username;
    }

    @Override
    public Location getLocation() {
        Vector position = new Vector(this.player.posX, this.player.posY, this.player.posZ);
        Vector direction = Vectors.fromEulerDeg(this.player.cameraYaw, this.player.cameraPitch);
        return new Location(ForgeWorldEdit.inst.getWorld(this.player.worldObj), position, direction);
    }

    public WorldVector getPosition() {
        return new WorldVector(LocalWorldAdapter.wrap(ForgeWorldEdit.inst.getWorld(this.player.worldObj)), this.player.posX, this.player.posY, this.player.posZ);
    }

    public com.sk89q.worldedit.world.World getWorld() {
        return ForgeWorldEdit.inst.getWorld(this.player.worldObj);
    }

    public double getPitch() {
        return this.player.rotationPitch;
    }

    public double getYaw() {
        return this.player.rotationYaw;
    }

    public void giveItem(int type, int amt) {
        this.player.inventory.addItemStackToInventory(new ItemStack(type, amt, 0));
    }

    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        Packet250CustomPayload packet = new Packet250CustomPayload(ForgeWorldEdit.CUI_PLUGIN_CHANNEL, send.getBytes(WECUIPacketHandler.UTF_8_CHARSET));
        this.player.playerNetServerHandler.sendPacketToPlayer(packet);
    }

    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText(part));
        }
    }

    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a77" + part));
        }
    }

    public void print(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a7d" + part));
        }
    }

    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a7c" + part));
        }
    }

    public void setPosition(Vector pos, float pitch, float yaw) {
        this.player.playerNetServerHandler.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), pitch, yaw);
    }

    public String[] getGroups() {
        return new String[]{}; // WorldEditMod.inst.getPermissionsResolver().getGroups(this.player.username);
    }

    public BlockBag getInventoryBlockBag() {
        return null;
    }

    public boolean hasPermission(String perm) {
        return ForgeUtil.hasPermission(this.player, perm);
    }
}