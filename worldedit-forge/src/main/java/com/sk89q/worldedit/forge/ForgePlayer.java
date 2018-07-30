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
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.item.ItemTypes;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.UUID;

import javax.annotation.Nullable;

public class ForgePlayer extends AbstractPlayerActor {

    private final EntityPlayerMP player;

    protected ForgePlayer(EntityPlayerMP player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueID();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        ItemStack is = this.player.getHeldItem(handSide == HandSide.MAIN_HAND ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
        return new BaseItemStack(ItemTypes.get(ForgeRegistries.ITEMS.getKey(is.getItem()).toString()));
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        Vector position = new Vector(this.player.posX, this.player.posY, this.player.posZ);
        return new Location(
                ForgeWorldEdit.inst.getWorld(this.player.world),
                position,
                this.player.rotationYaw,
                this.player.rotationPitch);
    }

    @Override
    public com.sk89q.worldedit.world.World getWorld() {
        return ForgeWorldEdit.inst.getWorld(this.player.world);
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        this.player.inventory.addItemStackToInventory(
                new ItemStack(Item.getByNameOrId(itemStack.getType().getId()), itemStack.getAmount(), 0));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        PacketBuffer buffer = new PacketBuffer(Unpooled.copiedBuffer(send.getBytes(WECUIPacketHandler.UTF_8_CHARSET)));
        SPacketCustomPayload packet = new SPacketCustomPayload(ForgeWorldEdit.CUI_PLUGIN_CHANNEL, buffer);
        this.player.connection.sendPacket(packet);
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(new TextComponentString(part));
        }
    }

    @Override
    public void printDebug(String msg) {
        sendColorized(msg, TextFormatting.GRAY);
    }

    @Override
    public void print(String msg) {
        sendColorized(msg, TextFormatting.LIGHT_PURPLE);
    }

    @Override
    public void printError(String msg) {
        sendColorized(msg, TextFormatting.RED);
    }

    private void sendColorized(String msg, TextFormatting formatting) {
        for (String part : msg.split("\n")) {
            TextComponentString component = new TextComponentString(part);
            component.getStyle().setColor(formatting);
            this.player.sendMessage(component);
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        this.player.connection.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
    }

    @Override
    public String[] getGroups() {
        return new String[]{}; // WorldEditMod.inst.getPermissionsResolver().getGroups(this.player.username);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public boolean hasPermission(String perm) {
        return ForgeWorldEdit.inst.getPermissionsProvider().hasPermission(player, perm);
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(player.getUniqueID(), player.getName());
    }

    private static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        private SessionKeyImpl(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        @Override
        public UUID getUniqueId() {
            return uuid;
        }

        @Nullable
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isActive() {
            // We can't directly check if the player is online because
            // the list of players is not thread safe
            return ThreadSafeCache.getInstance().getOnlineIds().contains(uuid);
        }

        @Override
        public boolean isPersistent() {
            return true;
        }

    }

}
