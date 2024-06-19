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
import com.sk89q.worldedit.sponge.internal.NbtAdapter;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.gamemode.GameModes;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
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
    private static final int STRUCTURE_BLOCK_PACKET_ID = 7;

    private final ServerPlayer player;

    protected SpongePlayer(ServerPlayer player) {
        this.player = player;
        ThreadSafeCache.getInstance().getOnlineIds().add(getUniqueId());
    }

    @Override
    public UUID getUniqueId() {
        return player.uniqueId();
    }

    @Override
    public BaseItemStack getItemInHand(HandSide handSide) {
        ItemStack is = this.player.itemInHand(
            handSide == HandSide.MAIN_HAND ? HandTypes.MAIN_HAND : HandTypes.OFF_HAND
        );
        return SpongeAdapter.adapt(is);
    }

    @Override
    public String getName() {
        return this.player.name();
    }

    @Override
    public String getDisplayName() {
        return LegacyComponentSerializer.legacySection().serialize(player.displayName().get());
    }

    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException("Cannot create a state from this object");
    }

    @Override
    public Location getLocation() {
        ServerLocation entityLoc = this.player.serverLocation();
        Vector3d entityRot = this.player.rotation();

        return SpongeAdapter.adapt(entityLoc, entityRot);
    }

    @Override
    public boolean setLocation(Location location) {
        return player.setLocation(SpongeAdapter.adapt(location));
    }

    @Override
    public com.sk89q.worldedit.world.World getWorld() {
        return SpongeAdapter.adapt(player.serverLocation().world());
    }

    @Override
    public void giveItem(BaseItemStack itemStack) {
        this.player.inventory().offer(SpongeAdapter.adapt(itemStack));
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }

        String finalData = send;
        CUIChannelHandler.channel().play().sendTo(
            player,
            buffer -> buffer.writeBytes(finalData.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(LegacyComponentSerializer.legacySection().deserialize(part));
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        sendColorized(msg, NamedTextColor.GRAY);
    }

    @Override
    @Deprecated
    public void print(String msg) {
        sendColorized(msg, NamedTextColor.LIGHT_PURPLE);
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        sendColorized(msg, NamedTextColor.RED);
    }

    @Override
    public void print(Component component) {
        player.sendMessage(SpongeTextAdapter.convert(component, getLocale()));
    }

    private void sendColorized(String msg, TextColor formatting) {
        for (String part : msg.split("\n")) {
            this.player.sendMessage(
                LegacyComponentSerializer.legacySection().deserialize(part).color(formatting)
            );
        }
    }

    @Override
    public boolean trySetPosition(Vector3 pos, float pitch, float yaw) {
        ServerLocation loc = ServerLocation.of(
            this.player.world(), pos.x(), pos.y(), pos.z()
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
        return GameModes.get(player.gameMode().get().key(RegistryTypes.GAME_MODE).asString());
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        player.gameMode().set(
            Sponge.game().registry(RegistryTypes.GAME_MODE).value(
                ResourceKey.resolve(gameMode.id())
            )
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
        if (block == null) {
            player.resetBlockChange(pos.x(), pos.y(), pos.z());
        } else {
            BlockState spongeBlock = SpongeAdapter.adapt(block.toImmutableState());
            player.sendBlockChange(pos.x(), pos.y(), pos.z(), spongeBlock);
            if (block instanceof final BaseBlock baseBlock
                && block.getBlockType().equals(com.sk89q.worldedit.world.block.BlockTypes.STRUCTURE_BLOCK)) {
                final LinCompoundTag nbtData = baseBlock.getNbt();
                if (nbtData != null) {
                    net.minecraft.world.level.block.state.BlockState nativeBlock =
                        (net.minecraft.world.level.block.state.BlockState) spongeBlock;
                    net.minecraft.nbt.CompoundTag nativeNbtData = NbtAdapter.adaptNMSToWorldEdit(nbtData);
                    net.minecraft.server.level.ServerPlayer nativePlayer =
                        ((net.minecraft.server.level.ServerPlayer) player);

                    StructureBlockEntity structureBlockEntity =
                        new StructureBlockEntity(new BlockPos(pos.x(), pos.y(), pos.z()), nativeBlock);
                    structureBlockEntity.loadWithComponents(nativeNbtData, nativePlayer.level().registryAccess());
                    nativePlayer.connection.send(
                        ClientboundBlockEntityDataPacket.create(structureBlockEntity, (be, ra) -> nativeNbtData));
                }
            }
        }
    }

    @Override
    public Locale getLocale() {
        return player.locale();
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
            this.uuid = player.uniqueId();
            this.name = player.name();
        }

        SessionKeyImpl(UUID uuid, String name) {
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

    public Player getPlayer() {
        return player;
    }
}
