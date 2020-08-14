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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;

import java.util.function.Predicate;

final class PacketHandlerUtil {
    private PacketHandlerUtil() {
    }

    static NetworkRegistry.ChannelBuilder buildLenientHandler(String id, int protocolVersion) {
        final String verStr = Integer.toString(protocolVersion);
        final Predicate<String> validator = validateLenient(verStr);
        return NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ForgeWorldEdit.MOD_ID, id))
                .clientAcceptedVersions(validator)
                .serverAcceptedVersions(validator)
                .networkProtocolVersion(() -> verStr);
    }

    private static Predicate<String> validateLenient(String protocolVersion) {
        return remoteVersion ->
                protocolVersion.equals(remoteVersion)
                || NetworkRegistry.ABSENT.equals(remoteVersion)
                || NetworkRegistry.ACCEPTVANILLA.equals(remoteVersion);
    }
}
