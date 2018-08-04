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

import com.sk89q.worldedit.forge.net.LeftClickAirEventMessage;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.nio.charset.Charset;

public class InternalPacketHandler {
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    public static SimpleNetworkWrapper CHANNEL;

    public static void init() {
         CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(ForgeWorldEdit.MOD_ID);
         CHANNEL.registerMessage(LeftClickAirEventMessage.Handler.class, LeftClickAirEventMessage.class, 0, Side.SERVER);
    }

    private InternalPacketHandler() {
    }
}
