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

package com.sk89q.worldedit.sponge;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class SpongePlayer extends AbstractPlayerActor {

    private final Player player;

    protected SpongePlayer(SpongePlatform platform, Player player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public int getItemInHand() {
        Optional<ItemStack> is = this.player.getItemInHand(HandTypes.MAIN_HAND);
        return is.isPresent() ? SpongeWorldEdit.inst().getAdapter().resolve(is.get().getItem()) : 0;
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
        org.spongepowered.api.world.Location<World> entityLoc = this.player.getLocation();
        Vector3d entityRot = this.player.getRotation();

        return SpongeWorldEdit.inst().getAdapter().adapt(entityLoc, entityRot);
    }

    @Override
    public WorldVector getPosition() {
        Vector3d pos = this.player.getLocation().getPosition();
        return new WorldVector(LocalWorldAdapter.adapt(SpongeWorldEdit.inst().getAdapter().getWorld(this.player.getWorld())), pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public com.sk89q.worldedit.world.World getWorld() {
        return SpongeWorldEdit.inst().getAdapter().getWorld(player.getWorld());
    }

    @Override
    public double getPitch() {
        return getLocation().getPitch();
    }

    @Override
    public double getYaw() {
        return getLocation().getYaw();
    }

    @Override
    public void giveItem(int type, int amt) {
        this.player.getInventory().offer(ItemStack.of(SpongeWorldEdit.inst().getAdapter().resolveItem(type), amt));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }

        String finalData = send;
        CUIChannelHandler.getActiveChannel().sendTo(player, buffer -> buffer.writeBytes(finalData.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(part));
        }
    }

    @Override
    public void printDebug(String msg) {
        sendColorized(msg, TextColors.GRAY);
    }

    @Override
    public void print(String msg) {
        sendColorized(msg, TextColors.LIGHT_PURPLE);
    }

    @Override
    public void printError(String msg) {
        sendColorized(msg, TextColors.RED);
    }

    private void sendColorized(String msg, TextColor formatting) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(Text.of(formatting, TextSerializers.LEGACY_FORMATTING_CODE.deserialize(part)));
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        org.spongepowered.api.world.Location<World> loc = new org.spongepowered.api.world.Location<>(
                this.player.getWorld(), pos.getX(), pos.getY(), pos.getZ()
        );

        this.player.setLocationAndRotation(loc, new Vector3d(pitch, yaw, 0));
    }

    @Override
    public String[] getGroups() {
        return SpongeWorldEdit.inst().getPermissionsProvider().getGroups(this.player);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return null;
    }

    @Override
    public boolean hasPermission(String perm) {
        return SpongeWorldEdit.inst().getPermissionsProvider().hasPermission(player, perm);
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> cls) {
        return null;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(player.getUniqueId(), player.getName());
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
