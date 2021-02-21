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

package com.sk89q.worldedit.sponge;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.Nullable;

public class SpongePlayer extends AbstractPlayerActor {

    private final ServerPlayer player;

    protected SpongePlayer(SpongePlatform platform, ServerPlayer player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        ItemStack is = this.player.getItemInHand(handSide == HandSide.MAIN_HAND
                ? HandTypes.MAIN_HAND.get() : HandTypes.OFF_HAND.get());
        return new BaseItemStack(SpongeAdapter.adapt(is.getType()));
    }

    @Override
    public String getName() {
        return this.player.getName();
    }

    @Override
    public String getDisplayName() {
        return player.get(Keys.DISPLAY_NAME).map(LegacyComponentSerializer.legacyAmpersand()::serialize).orElse(getName());
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        ServerLocation entityLoc = (ServerLocation) this.player.getLocation();
        Vector3d entityRot = this.player.getRotation();

        return SpongeAdapter.adapt(entityLoc, entityRot);
    }

    @Override
    public boolean setLocation(Location location) {
        return player.setLocation(SpongeAdapter.adapt(location));
    }

    @Override
    public com.sk89q.worldedit.world.World getWorld() {
        return SpongeAdapter.adapt(player.getWorld());
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        this.player.getInventory().offer(
                ItemStack.of(SpongeAdapter.adapt(itemStack.getType()),
                itemStack.getAmount())
        );
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }

        String finalData = send;
        CUIChannelHandler.getActiveChannel().play().sendTo(player, buffer -> buffer.writeBytes(finalData.getBytes(StandardCharsets.UTF_8)));
    }

    @Override
    public void print(Component component) {
        player.sendMessage(SpongeTextAdapter.convert(WorldEditText.format(component, getLocale())));
    }

    @Override
    public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
        ServerLocation loc = ServerLocation.of(
            this.player.getWorld(), pos.getX(), pos.getY(), pos.getZ()
        );

        return this.player.setLocationAndRotation(loc, new Vector3d(pitch, yaw, 0));
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
    public GameMode getGameMode() {
        return GameModes.get(player.get(Keys.GAME_MODE).get().key(RegistryTypes.GAME_MODE).getFormatted());
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        player.offer(
            Keys.GAME_MODE,
            RegistryTypes.GAME_MODE.get().findValue(ResourceKey.resolve(gameMode.getId())).get()
        );
    }

    @Override
    public boolean isAllowedToFly() {
        return player.get(Keys.CAN_FLY).orElse(super.isAllowedToFly());
    }

    @Override
    public void setFlying(boolean flying) {
        player.offer(Keys.IS_FLYING, flying);
    }

    @Override
    public <B extends BlockStateHolder<B>> void sendFakeBlock(BlockVector3 pos, B block) {
        ServerLocation loc = player.getWorld().getLocation(pos.getX(), pos.getY(), pos.getZ());
        if (block == null) {
            player.sendBlockChange(loc.getBlockPosition(), loc.getBlock());
        } else {
            // TODO via adapter
            //            player.sendBlockChange(loc, BukkitAdapter.adapt(block));
            //            if (block instanceof BaseBlock && ((BaseBlock) block).hasNbtData()) {
            //                BukkitImplAdapter adapter = WorldEditPlugin.getInstance().getBukkitImplAdapter();
            //                if (adapter != null) {
            //                    adapter.sendFakeNBT(player, pos, ((BaseBlock) block).getNbtData());
            //                }
            //            }
        }
    }

    @Override
    public Locale getLocale() {
        return player.getLocale();
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKeyImpl(player);
    }

    static class SessionKeyImpl implements SessionKey {
        // If not static, this will leak a reference

        private final UUID uuid;
        private final String name;

        SessionKeyImpl(Player player) {
            this.uuid = player.getUniqueId();
            this.name = player.getName();
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

    public Player getPlayer() {
        return player;
    }
}
