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

package com.sk89q.worldedit.fabric.net.handler;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.fabric.FabricPlayer;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.nio.charset.StandardCharsets;

public final class WECUIPacketHandler {
    private WECUIPacketHandler() {
    }

    public static final ResourceLocation CUI_IDENTIFIER = new ResourceLocation(FabricWorldEdit.MOD_ID, FabricWorldEdit.CUI_PLUGIN_CHANNEL);

    public record CuiInitializationPacket(String text) implements CustomPacketPayload {
        public static final Type<CuiInitializationPacket> TYPE = new Type<>(CUI_IDENTIFIER);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void init() {
        PayloadTypeRegistry.playC2S().register(
            CuiInitializationPacket.TYPE,
            CustomPacketPayload.codec(
                (packet, buffer) -> buffer.writeCharSequence(packet.text(), StandardCharsets.UTF_8),
                buffer -> new CuiInitializationPacket(buffer.toString(StandardCharsets.UTF_8))
            )
        );
        ServerPlayNetworking.registerGlobalReceiver(CuiInitializationPacket.TYPE, (payload, context) -> {
            LocalSession session = FabricWorldEdit.inst.getSession(context.player());
            FabricPlayer actor = FabricAdapter.adaptPlayer(context.player());
            session.handleCUIInitializationMessage(payload.text(), actor);
        });
    }
}
