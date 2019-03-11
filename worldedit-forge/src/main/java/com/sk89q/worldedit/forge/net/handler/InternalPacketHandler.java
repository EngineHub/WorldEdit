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

package com.sk89q.worldedit.forge.net.handler;

import com.sk89q.worldedit.forge.ForgeWorldEdit;
import com.sk89q.worldedit.forge.net.packet.LeftClickAirEventMessage;
import com.sk89q.worldedit.forge.net.packet.LeftClickAirEventMessage.Handler;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public final class InternalPacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    public static SimpleChannel HANDLER = ChannelBuilder
            .named(new ResourceLocation(ForgeWorldEdit.MOD_ID, "internal"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private InternalPacketHandler() {
    }

    public static void init() {
        HANDLER.registerMessage(0, LeftClickAirEventMessage.class,
                LeftClickAirEventMessage::encode, LeftClickAirEventMessage::decode, Handler::handle);
    }
}
