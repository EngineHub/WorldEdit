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

package com.sk89q.worldedit.neoforge;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.neoforge.internal.NBTConverter;
import com.sk89q.worldedit.neoforge.net.handler.WECUIPacketHandler;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.component.TextUtils;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class NeoForgePlayer extends AbstractPlayerActor {

    private final ServerPlayer player;

    protected NeoForgePlayer(ServerPlayer player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUUID();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        ItemStack is = this.player.getItemInHand(
            handSide == HandSide.MAIN_HAND
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND
        );
        return NeoForgeAdapter.adapt(is);
    }

    @Override
    public String getName() {
        return this.player.getName().getString();
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        Vector3 position = Vector3.at(this.player.getX(), this.player.getY(), this.player.getZ());
        return new Location(
            NeoForgeWorldEdit.inst.getWorld(this.player.serverLevel()),
            position,
            this.player.getYRot(),
            this.player.getXRot());
    }

    @Override
    public boolean setLocation(Location location) {
        ServerLevel level = NeoForgeAdapter.adapt((World) location.getExtent());
        this.player.teleportTo(
            level,
            location.getX(), location.getY(), location.getZ(),
            Set.of(),
            location.getYaw(), location.getPitch(),
            true
        );
        // This may be false if the teleport was cancelled by a mod
        return this.player.serverLevel() == level;
    }

    @Override
    public World getWorld() {
        return NeoForgeWorldEdit.inst.getWorld(this.player.serverLevel());
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        this.player.getInventory().add(NeoForgeAdapter.adapt(itemStack));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        PacketDistributor.sendToPlayer(this.player, new WECUIPacketHandler.CuiPacket(send));
    }

    private void sendMessage(net.minecraft.network.chat.Component textComponent) {
        this.player.sendSystemMessage(textComponent);
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sendMessage(net.minecraft.network.chat.Component.literal(part));
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        sendColorized(msg, ChatFormatting.GRAY);
    }

    @Override
    @Deprecated
    public void print(String msg) {
        sendColorized(msg, ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        sendColorized(msg, ChatFormatting.RED);
    }

    @Override
    public void print(Component component) {
        sendMessage(net.minecraft.network.chat.Component.Serializer.fromJson(
            GsonComponentSerializer.INSTANCE.serialize(WorldEditText.format(component, getLocale())),
            this.player.registryAccess()
        ));
    }

    private void sendColorized(String msg, ChatFormatting formatting) {
        for (String part : msg.split("\n")) {
            var component = net.minecraft.network.chat.Component.literal(part);
            component.withStyle(formatting);
            sendMessage(component);
        }
    }

    @Override
    public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
        this.player.connection.teleport(pos.x(), pos.y(), pos.z(), yaw, pitch);
        return true;
    }

    @Override
    public String[] getGroups() {
        return new String[] {}; // WorldEditMod.inst.getPermissionsResolver().getGroups(this.player.username);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public boolean hasPermission(String perm) {
        return NeoForgeWorldEdit.inst.getPermissionsProvider().hasPermission(player, perm);
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public boolean isAllowedToFly() {
        return player.mayFly();
    }

    @Override
    public void setFlying(boolean flying) {
        if (player.getAbilities().flying != flying) {
            player.getAbilities().flying = flying;
            player.onUpdateAbilities();
        }
    }

    @Override
    public Locale getLocale() {
        return TextUtils.getLocaleByMinecraftTag(player.getLanguage());
    }

    @Override
    public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, B block) {
        World world = getWorld();
        if (!(world instanceof NeoForgeWorld)) {
            return;
        }
        BlockPos loc = NeoForgeAdapter.toBlockPos(pos);
        if (block == null) {
            final ClientboundBlockUpdatePacket packetOut = new ClientboundBlockUpdatePacket(
                ((NeoForgeWorld) world).getWorld(),
                loc
            );
            player.connection.send(packetOut);
        } else {
            player.connection.send(new ClientboundBlockUpdatePacket(
                loc, NeoForgeAdapter.adapt(block.toImmutableState())
            ));
            if (block instanceof BaseBlock baseBlock && block.getBlockType().equals(BlockTypes.STRUCTURE_BLOCK)) {
                final LinCompoundTag nbtData = baseBlock.getNbt();
                if (nbtData != null) {
                    player.connection.send(new ClientboundBlockEntityDataPacket(
                        new BlockPos(pos.x(), pos.y(), pos.z()),
                        BlockEntityType.STRUCTURE_BLOCK,
                        NBTConverter.toNative(nbtData)
                    ));
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

        SessionKeyImpl(ServerPlayer player) {
            this.uuid = player.getUUID();
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
