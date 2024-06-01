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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterChannelEvent;
import org.spongepowered.api.network.ServerConnectionState;
import org.spongepowered.api.network.channel.ChannelBuf;
import org.spongepowered.api.network.channel.raw.RawDataChannel;
import org.spongepowered.api.network.channel.raw.play.RawPlayDataHandler;

import java.nio.charset.StandardCharsets;

public class CUIChannelHandler implements RawPlayDataHandler<ServerConnectionState.Game> {
    public static final ResourceKey CUI_PLUGIN_CHANNEL = ResourceKey.of("worldedit", "cui");
    private static final SimpleLifecycled<RawDataChannel> CHANNEL = SimpleLifecycled.invalid();

    public static final class RegistrationHandler {
        @Listener
        public void onChannelRegistration(RegisterChannelEvent event) {
            RawDataChannel channel = event.register(CUI_PLUGIN_CHANNEL, RawDataChannel.class);
            channel.play().addHandler(ServerConnectionState.Game.class, new CUIChannelHandler());
            CHANNEL.newValue(channel);
        }
    }

    public static RawDataChannel channel() {
        return CHANNEL.valueOrThrow();
    }

    @Override
    public void handlePayload(ChannelBuf data, ServerConnectionState.Game connection) {
        ServerPlayer player = connection.player();

        SpongePlayer spongePlayer = SpongeAdapter.adapt(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(
            spongePlayer
        );

        session.handleCUIInitializationMessage(
            new String(data.readBytes(data.available()), StandardCharsets.UTF_8),
            spongePlayer
        );
    }
}
