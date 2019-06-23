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

package com.sk89q.worldedit.fabric.net.handler;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class WECUIPacketHandler {
    private WECUIPacketHandler() {
    }

    public static final Charset UTF_8_CHARSET = StandardCharsets.UTF_8;
    private static final int PROTOCOL_VERSION = 1;
//    private static EventNetworkChannel HANDLER = PacketHandlerUtil
//            .buildLenientHandler(FabricWorldEdit.CUI_PLUGIN_CHANNEL, PROTOCOL_VERSION)
//            .eventNetworkChannel();
//
//    public static void init() {
//        HANDLER.addListener(WECUIPacketHandler::onPacketData);
//        HANDLER.addListener(WECUIPacketHandler::callProcessPacket);
//    }
//
//    public static void onPacketData(CustomPayloadS2CPacket event) {
//        ServerPlayerEntity player = event.getSource().get().getSender();
//        LocalSession session = FabricWorldEdit.inst.getSession(player);
//
//        if (session.hasCUISupport()) {
//            return;
//        }
//
//        String text = event.getData().toString(UTF_8_CHARSET);
//        final FabricPlayer actor = adaptPlayer(player);
//        session.handleCUIInitializationMessage(text, actor);
//        session.describeCUI(actor);
//    }
//
//    public static void callProcessPacket(CustomPayloadS2CPacket event) {
//        try {
//            new CustomPayloadC2SPacket(
//                    new Identifier(FabricWorldEdit.MOD_ID, FabricWorldEdit.CUI_PLUGIN_CHANNEL),
//                    event.getData()
//            ).(MinecraftClient.getInstance().player.networkHandler);
//        } catch (ThreadQuickExitException ignored) {
//        }
//    }

}