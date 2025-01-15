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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;

public class SpongeWorldEditListener {

    private final SpongeWorldEdit plugin;

    public SpongeWorldEditListener(SpongeWorldEdit plugin) {
        this.plugin = plugin;
    }

    public boolean skipEvents() {
        return plugin.getInternalPlatform() == null || !plugin.getInternalPlatform().isHookingEvents();
    }

    private boolean skipInteractionEvent(InteractEvent event) {
        return skipEvents() || event.context().get(EventContextKeys.USED_HAND).orElse(null) != HandTypes.MAIN_HAND.get();
    }

    @Listener
    public void onPlayerInteractItemPrimary(InteractItemEvent.Primary event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        Optional<Boolean> previousResult = plugin.getDebouncer().getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            return;
        }

        boolean result = we.handleArmSwing(player);
        plugin.getDebouncer().setLastInteraction(player, result);
    }

    @Listener
    public void onPlayerInteractItemSecondary(InteractItemEvent.Secondary event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        Optional<Boolean> previousResult = plugin.getDebouncer().getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            if (previousResult.get()) {
                event.setCancelled(true);
            }
            return;
        }

        boolean result = we.handleRightClick(player);
        plugin.getDebouncer().setLastInteraction(player, result);

        if (result) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteractBlockPrimary(InteractBlockEvent.Primary.Start event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        BlockSnapshot targetBlock = event.block();
        Optional<ServerLocation> optLoc = targetBlock.location();

        boolean result = false;
        if (optLoc.isPresent()) {
            ServerLocation loc = optLoc.get();
            com.sk89q.worldedit.util.Location pos = SpongeAdapter.adapt(loc, Vector3d.ZERO);

            result = we.handleBlockLeftClick(player, pos, SpongeAdapter.adapt(event.targetSide()));
        }

        result = we.handleArmSwing(player) || result;
        plugin.getDebouncer().setLastInteraction(player, result);

        if (result) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteractBlockSecondary(InteractBlockEvent.Secondary.Pre event, @Root ServerPlayer spongePlayer) {
        if (skipInteractionEvent(event)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        SpongePlayer player = SpongeAdapter.adapt(spongePlayer);

        BlockSnapshot targetBlock = event.block();
        Optional<ServerLocation> optLoc = targetBlock.location();

        boolean result = false;
        if (optLoc.isPresent()) {
            ServerLocation loc = optLoc.get();
            com.sk89q.worldedit.util.Location pos = SpongeAdapter.adapt(loc, Vector3d.ZERO);

            result = we.handleBlockRightClick(player, pos, SpongeAdapter.adapt(event.targetSide()));
        }

        result = we.handleRightClick(player) || result;
        plugin.getDebouncer().setLastInteraction(player, result);

        if (result) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerQuit(ServerSideConnectionEvent.Disconnect event) {
        event.profile().ifPresent(profile -> {
            plugin.getDebouncer().clearInteraction(profile::uniqueId);

            WorldEdit.getInstance().getEventBus()
                    .post(new SessionIdleEvent(new SpongePlayer.SessionKeyImpl(profile.uniqueId(), profile.name().orElseThrow())));
        });
    }

}
