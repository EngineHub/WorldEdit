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

package com.sk89q.worldedit.neoforge.net.handler;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.neoforge.NeoForgeAdapter;
import com.sk89q.worldedit.neoforge.NeoForgePlayer;
import com.sk89q.worldedit.neoforge.NeoForgeWorldEdit;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import java.nio.charset.StandardCharsets;

public final class WECUIPacketHandler {
    private WECUIPacketHandler() {
    }

    private static final String PROTOCOL_VERSION = "1";

    public static final ResourceLocation CUI_IDENTIFIER = new ResourceLocation(NeoForgeWorldEdit.MOD_ID, NeoForgeWorldEdit.CUI_PLUGIN_CHANNEL);

    public record CuiPacket(String text) implements CustomPacketPayload {
        public static final Type<CuiPacket> TYPE = new Type<>(CUI_IDENTIFIER);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(PROTOCOL_VERSION)
            .optional()
            .playBidirectional(
                CuiPacket.TYPE,
                CustomPacketPayload.codec(
                    (packet, buffer) -> buffer.writeCharSequence(packet.text(), StandardCharsets.UTF_8),
                    buffer -> new CuiPacket(buffer.readCharSequence(buffer.readableBytes(), StandardCharsets.UTF_8).toString())
                ),
                (payload, context) -> {
                    if (!(context.player() instanceof ServerPlayer player)) {
                        // Client-side packet, ignore (this is for WECUI to handle)
                        return;
                    }
                    LocalSession session = NeoForgeWorldEdit.inst.getSession(player);
                    NeoForgePlayer actor = NeoForgeAdapter.adaptPlayer(player);
                    session.handleCUIInitializationMessage(payload.text(), actor);
                }
            );
    }

}
