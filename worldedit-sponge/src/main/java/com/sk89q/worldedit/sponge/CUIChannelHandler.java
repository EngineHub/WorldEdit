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

import com.sk89q.worldedit.LocalSession;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

import java.nio.charset.StandardCharsets;

public class CUIChannelHandler implements RawDataListener {
    public static final String CUI_PLUGIN_CHANNEL = "worldedit:cui";

    private static ChannelBinding.RawDataChannel channel;

    public static void init() {
        channel = Sponge.getChannelRegistrar().createRawChannel(SpongeWorldEdit.inst(), CUI_PLUGIN_CHANNEL);
        channel.addListener(Platform.Type.SERVER, new CUIChannelHandler());
    }


    public static ChannelBinding.RawDataChannel getActiveChannel() {
        return channel;
    }

    @Override
    public void handlePayload(ChannelBuf data, RemoteConnection connection, Platform.Type side) {
        if (connection instanceof PlayerConnection) {
            Player player = ((PlayerConnection) connection).getPlayer();

            LocalSession session = SpongeWorldEdit.inst().getSession(player);

            if (session.hasCUISupport()) {
                return;
            }

            final SpongePlayer actor = SpongeWorldEdit.inst().wrapPlayer(player);
            session.handleCUIInitializationMessage(new String(data.readBytes(data.available()), StandardCharsets.UTF_8),
                    actor);
            session.describeCUI(actor);
        }
    }
}
