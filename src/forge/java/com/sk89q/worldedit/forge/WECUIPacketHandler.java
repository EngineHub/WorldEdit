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

import java.nio.charset.Charset;

import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

import com.sk89q.worldedit.LocalSession;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;

public class WECUIPacketHandler {
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    @SubscribeEvent
    public void onReceiveServer(ServerCustomPacketEvent evt) {
        if (evt.handler instanceof NetHandlerPlayServer) {
            LocalSession session = ForgeWorldEdit.inst.getSession(((NetHandlerPlayServer) evt.handler).playerEntity);

            if (session.hasCUISupport()) {
                return;
            }

            String text = new String(((C17PacketCustomPayload) evt.packet.toC17Packet()).func_149558_e(), UTF_8_CHARSET);
            session.handleCUIInitializationMessage(text);
        }
    }
}