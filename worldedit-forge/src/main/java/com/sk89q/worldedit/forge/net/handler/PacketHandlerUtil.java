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

import com.sk89q.worldedit.forge.ForgeWorldEdit;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;

final class PacketHandlerUtil {
    private PacketHandlerUtil() {
    }

    static ChannelBuilder buildLenientHandler(String id, int protocolVersion) {
        final Channel.VersionTest validator = validateLenient(protocolVersion);
        return ChannelBuilder
            .named(new ResourceLocation(ForgeWorldEdit.MOD_ID, id))
            .clientAcceptedVersions(validator)
            .serverAcceptedVersions(validator)
            .networkProtocolVersion(protocolVersion);
    }

    private static Channel.VersionTest validateLenient(int protocolVersion) {
        return (status, remoteVersion) ->
            protocolVersion == remoteVersion
                // These two ignore protocolVersion anyway so it doesn't matter what it is
                || Channel.VersionTest.ACCEPT_MISSING.accepts(status, protocolVersion)
                || Channel.VersionTest.ACCEPT_VANILLA.accepts(status, protocolVersion);
    }
}
