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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.forge.net.handler.WECUIPacketHandler;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Nullable;

public class ForgePlayer extends AbstractPlayerActor {

    // see ClientPlayNetHandler: search for "invalid update packet", lots of hardcoded consts
    private static final int STRUCTURE_BLOCK_PACKET_ID = 7;
    private final ServerPlayerEntity player;

    protected ForgePlayer(ServerPlayerEntity player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueID();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        ItemStack is = this.player.getHeldItem(handSide == HandSide.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND);
        return ForgeAdapter.adapt(is);
    }

    @Override
    public String getName() {
        return this.player.getName().getFormattedText();
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        Vector3 position = Vector3.at(this.player.posX, this.player.posY, this.player.posZ);
        return new Location(
                ForgeWorldEdit.inst.getWorld(this.player.world),
                position,
                this.player.rotationYaw,
                this.player.rotationPitch);
    }

    @Override
    public boolean setLocation(Location location) {
        // TODO
        return false;
    }

    @Override
    public com.sk89q.worldedit.world.World getWorld() {
        return ForgeWorldEdit.inst.getWorld(this.player.world);
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        this.player.inventory.addItemStackToInventory(ForgeAdapter.adapt(itemStack));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        PacketBuffer buffer = new PacketBuffer(Unpooled.copiedBuffer(send.getBytes(WECUIPacketHandler.UTF_8_CHARSET)));
        SCustomPayloadPlayPacket packet = new SCustomPayloadPlayPacket(new ResourceLocation(ForgeWorldEdit.MOD_ID, ForgeWorldEdit.CUI_PLUGIN_CHANNEL), buffer);
        this.player.connection.sendPacket(packet);
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(new StringTextComponent(part));
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

    @Override
    public void print(Component component) {
        this.player.sendMessage(ITextComponent.Serializer.fromJson(GsonComponentSerializer.INSTANCE.serialize(component)));
    }

    private void sendColorized(String msg, TextFormatting formatting) {
        for (String part : msg.split("\n")) {
            StringTextComponent component = new StringTextComponent(part);
            component.getStyle().setColor(formatting);
            this.player.sendMessage(component);
        }
    }

    @Override
    public void setPosition(Vector3 pos, float pitch, float yaw) {
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
    public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, B block) {
        World world = getWorld();
        if (!(world instanceof ForgeWorld)) {
            return;
        }
        BlockPos loc = ForgeAdapter.toBlockPos(pos);
        if (block == null) {
            final SChangeBlockPacket packetOut = new SChangeBlockPacket(((ForgeWorld) world).getWorld(), loc);
            player.connection.sendPacket(packetOut);
        } else {
            final SChangeBlockPacket packetOut = new SChangeBlockPacket();
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeBlockPos(loc);
            buf.writeVarInt(Block.getStateId(ForgeAdapter.adapt(block.toImmutableState())));
            try {
                packetOut.readPacketData(buf);
            } catch (IOException e) {
                return;
            }
            player.connection.sendPacket(packetOut);
            if (block instanceof BaseBlock && block.getBlockType().equals(BlockTypes.STRUCTURE_BLOCK)) {
                final BaseBlock baseBlock = (BaseBlock) block;
                final CompoundTag nbtData = baseBlock.getNbtData();
                if (nbtData != null) {
                    player.connection.sendPacket(new SUpdateTileEntityPacket(
                            new BlockPos(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()),
                            STRUCTURE_BLOCK_PACKET_ID,
                            NBTConverter.toNative(nbtData))
                    );
                }
            }
        }
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(player.getUniqueID(), player.getName().getString());
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
