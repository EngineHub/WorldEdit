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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.coremc.CoreMcPermissionsProvider;
import net.fabricmc.fabric.api.permission.v1.PermissionNode;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class FabricPermissionsProvider implements CoreMcPermissionsProvider {
    private final CoreMcPermissionsProvider fallback;
    private final Map<String, PermissionNode<Boolean>> registeredPermissions = new ConcurrentHashMap<>();

    FabricPermissionsProvider(CoreMcPermissionsProvider fallback) {
        this.fallback = fallback;
    }

    @Override
    public boolean hasPermission(ServerPlayer player, String permission) {
        PermissionNode<Boolean> node = registeredPermissions.get(permission);
        if (node == null) {
            node = createNode(permission);
        }

        if (node == null) {
            // This permission node doesn't have the namespace/path format expected by the official Fabric perms API,
            // so delegate to the fallback in the hopes it can handle it.
            return fallback.hasPermission(player, permission);
        }

        Boolean result = player.checkPermission(node);
        return result != null ? result : fallback.hasPermission(player, permission);
    }

    @Override
    public void registerPermission(String permission) {
        PermissionNode<Boolean> node = createNode(permission);
        if (node != null) {
            registeredPermissions.putIfAbsent(permission, node);
        }
        fallback.registerPermission(permission);
    }

    private static @Nullable PermissionNode<Boolean> createNode(String permission) {
        Identifier identifier = identifierFor(permission);
        // It's theoretically possible some of our permission nodes don't have a namespace and path,
        // so return null here and allow a fallback later on.
        return identifier != null ? PermissionNode.of(identifier) : null;
    }

    static @Nullable Identifier identifierFor(String permission) {
        int namespaceSeparator = permission.indexOf('.');
        if (namespaceSeparator < 1 || namespaceSeparator == permission.length() - 1) {
            return null;
        }

        // Our permission nodes are in a.b.c.d format, Fabric expects modid:b.c.d, so we convert it here.
        return Identifier.fromNamespaceAndPath(
            encodeIdentifierPart(permission.substring(0, namespaceSeparator), false),
            encodeIdentifierPart(permission.substring(namespaceSeparator + 1), true)
        );
    }

    private static String encodeIdentifierPart(String value, boolean allowSlash) {
        // Fabric uses Identifier which has a stricter character set, so escape anything they cannot represent directly.
        byte[] bytes = value.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8);
        StringBuilder encoded = new StringBuilder(bytes.length);
        for (byte rawByte : bytes) {
            int valueByte = Byte.toUnsignedInt(rawByte);
            if ((valueByte >= 'a' && valueByte <= 'z')
                    || (valueByte >= '0' && valueByte <= '9')
                    || valueByte == '.' || valueByte == '-'
                    || (allowSlash && valueByte == '/')) {
                encoded.append((char) valueByte);
            } else if (valueByte == '_') {
                encoded.append("__");
            } else {
                encoded.append('_');
                encoded.append(Character.forDigit(valueByte >>> 4, 16));
                encoded.append(Character.forDigit(valueByte & 0x0F, 16));
            }
        }
        return encoded.toString();
    }
}
