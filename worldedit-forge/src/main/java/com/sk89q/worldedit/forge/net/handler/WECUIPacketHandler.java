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

package com.sk89q.worldedit.forge.net.handler;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.forge.ForgePlayer;
import com.sk89q.worldedit.forge.ForgeWorldEdit;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent.ClientCustomPayloadEvent;
import net.minecraftforge.fml.network.event.EventNetworkChannel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static com.sk89q.worldedit.forge.ForgeAdapter.adaptPlayer;

public final class WECUIPacketHandler {
    private WECUIPacketHandler() {
    }

    private static final int PROTOCOL_VERSION = 1;
    private static final EventNetworkChannel HANDLER = PacketHandlerUtil
            .buildLenientHandler(ForgeWorldEdit.CUI_PLUGIN_CHANNEL, PROTOCOL_VERSION)
            .eventNetworkChannel();

    public static void init() {
        HANDLER.addListener(WECUIPacketHandler::onPacketData);
    }

    public static void onPacketData(ClientCustomPayloadEvent event) {
        ServerPlayerEntity player = event.getSource().get().getSender();
        LocalSession session = ForgeWorldEdit.inst.getSession(player);
        String text = event.getPayload().toString(StandardCharsets.UTF_8);
        final ForgePlayer actor = adaptPlayer(player);
        session.handleCUIInitializationMessage(text, actor);
    }

}
