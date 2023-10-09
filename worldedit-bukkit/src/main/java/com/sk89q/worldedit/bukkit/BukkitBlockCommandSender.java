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

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.AbstractCommandBlockActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class BukkitBlockCommandSender extends AbstractCommandBlockActor {
    private final BlockCommandSender sender;
    private final WorldEditPlugin plugin;
    private final UUID uuid;

    public BukkitBlockCommandSender(WorldEditPlugin plugin, BlockCommandSender sender) {
        super(BukkitAdapter.adapt(checkNotNull(sender).getBlock().getLocation()));
        checkNotNull(plugin);

        this.plugin = plugin;
        this.sender = sender;
        this.uuid = UUID.nameUUIDFromBytes((UUID_PREFIX + sender.getName()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sender.sendMessage(part);
        }
    }

    @Override
    @Deprecated
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            print(Component.text(part, NamedTextColor.LIGHT_PURPLE));
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            print(Component.text(part, NamedTextColor.GRAY));
        }
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            print(Component.text(part, NamedTextColor.RED));
        }
    }

    @Override
    public void print(Component component) {
        plugin.getAudiences().sender(sender).sendMessage(WorldEditText.format(component, getLocale()));
    }

    @Override
    public Locale getLocale() {
        return WorldEdit.getInstance().getConfiguration().defaultLocale;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public void checkPermission(String permission) throws AuthorizationException {
        if (!hasPermission(permission)) {
            throw new AuthorizationException();
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return sender.hasPermission(permission);
    }

    public BlockCommandSender getSender() {
        return this.sender;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKey() {

            private volatile boolean active = true;

            private void updateActive() {
                Block block = sender.getBlock();
                if (!block.getWorld().isChunkLoaded(block.getX() >> 4, block.getZ() >> 4)) {
                    active = false;
                    return;
                }
                Material type = block.getType();
                active = type == Material.COMMAND_BLOCK
                    || type == Material.CHAIN_COMMAND_BLOCK
                    || type == Material.REPEATING_COMMAND_BLOCK;
            }

            @Override
            public String getName() {
                return sender.getName();
            }

            @Override
            public boolean isActive() {
                if (Bukkit.isPrimaryThread()) {
                    // we can update eagerly
                    updateActive();
                } else {
                    // we should update it eventually
                    Bukkit.getScheduler().callSyncMethod(plugin,
                        () -> {
                            updateActive();
                            return null;
                        });
                }
                return active;
            }

            @Override
            public boolean isPersistent() {
                return true;
            }

            @Override
            public UUID getUniqueId() {
                return uuid;
            }
        };
    }
}
