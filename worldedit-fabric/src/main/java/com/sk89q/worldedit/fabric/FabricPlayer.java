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

package com.sk89q.worldedit.fabric;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.fabric.internal.ExtendedPlayerEntity;
import com.sk89q.worldedit.fabric.internal.NBTConverter;
import com.sk89q.worldedit.fabric.net.handler.WECUIPacketHandler;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.component.TextUtils;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;

public class FabricPlayer extends AbstractPlayerActor {

    // see ClientPlayNetHandler: search for "invalid update packet", lots of hardcoded consts
    private static final int STRUCTURE_BLOCK_PACKET_ID = 7;
    private final ServerPlayerEntity player;

    protected FabricPlayer(ServerPlayerEntity player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUuid();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        ItemStack is = this.player.getStackInHand(handSide == HandSide.MAIN_HAND ? Hand.MAIN_HAND : Hand.OFF_HAND);
        return FabricAdapter.adapt(is);
    }

    @Override
    public String getName() {
        return this.player.getName().asString();
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        Vector3 position = Vector3.at(this.player.getX(), this.player.getY(), this.player.getZ());
        return new Location(
                FabricWorldEdit.inst.getWorld(this.player.world),
                position,
                this.player.yaw,
                this.player.pitch);
    }

    @Override
    public boolean setLocation(Location location) {
        // TODO
        return false;
    }

    @Override
    public World getWorld() {
        return FabricWorldEdit.inst.getWorld(this.player.world);
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        this.player.inventory.insertStack(FabricAdapter.adapt(itemStack));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        ServerPlayNetworking.send(
                this.player,
                WECUIPacketHandler.CUI_IDENTIFIER,
                new PacketByteBuf(Unpooled.copiedBuffer(send, StandardCharsets.UTF_8))
        );
    }

    @Override
    public Locale getLocale() {
        return TextUtils.getLocaleByMinecraftTag(((ExtendedPlayerEntity) this.player).getLanguage());
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(new LiteralText(part), false);
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        sendColorized(msg, Formatting.GRAY);
    }

    @Override
    @Deprecated
    public void print(String msg) {
        sendColorized(msg, Formatting.LIGHT_PURPLE);
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        sendColorized(msg, Formatting.RED);
    }

    @Override
    public void print(Component component) {
        this.player.sendMessage(Text.Serializer.fromJson(GsonComponentSerializer.INSTANCE.serialize(WorldEditText.format(component, getLocale()))), false);
    }

    private void sendColorized(String msg, Formatting formatting) {
        for (String part : msg.split("\n")) {
            MutableText component = new LiteralText(part)
                .styled(style -> style.withColor(formatting));
            this.player.sendMessage(component, false);
        }
    }

    @Override
    public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
        this.player.networkHandler.requestTeleport(pos.getX(), pos.getY(), pos.getZ(), yaw, pitch);
        return true;
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
        return FabricWorldEdit.inst.getPermissionsProvider().hasPermission(player, perm);
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public boolean isAllowedToFly() {
        return player.abilities.allowFlying;
    }

    @Override
    public void setFlying(boolean flying) {
        if (player.abilities.flying != flying) {
            player.abilities.flying = flying;
            player.sendAbilitiesUpdate();
        }
    }

    @Override
    public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, B block) {
        World world = getWorld();
        if (!(world instanceof FabricWorld)) {
            return;
        }
        BlockPos loc = FabricAdapter.toBlockPos(pos);
        if (block == null) {
            final BlockUpdateS2CPacket packetOut = new BlockUpdateS2CPacket(((FabricWorld) world).getWorld(), loc);
            player.networkHandler.sendPacket(packetOut);
        } else {
            final BlockUpdateS2CPacket packetOut = new BlockUpdateS2CPacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeBlockPos(loc);
            buf.writeVarInt(Block.getRawIdFromState(FabricAdapter.adapt(block.toImmutableState())));
            try {
                packetOut.read(buf);
            } catch (IOException e) {
                return;
            }
            player.networkHandler.sendPacket(packetOut);
            if (block instanceof BaseBlock && block.getBlockType().equals(BlockTypes.STRUCTURE_BLOCK)) {
                final BaseBlock baseBlock = (BaseBlock) block;
                final CompoundBinaryTag nbtData = baseBlock.getNbt();
                if (nbtData != null) {
                    player.networkHandler.sendPacket(new BlockEntityUpdateS2CPacket(
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
        return new SessionKeyImpl(player);
    }

    static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        SessionKeyImpl(ServerPlayerEntity player) {
            this.uuid = player.getUuid();
            this.name = player.getName().getString();
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
