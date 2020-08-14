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

package com.sk89q.bukkit.util;

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.util.StringUtil;
import com.sk89q.wepif.PermissionsResolverManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

/**
* An implementation of a dynamically registered {@link org.bukkit.command.Command} attached to a plugin.
*/
@SuppressWarnings("deprecation")
public class DynamicPluginCommand extends org.bukkit.command.Command implements PluginIdentifiableCommand {

    protected final CommandExecutor owner;
    protected final Object registeredWith;
    protected final Plugin owningPlugin;
    protected String[] permissions = new String[0];

    public DynamicPluginCommand(String[] aliases, String desc, String usage, CommandExecutor owner, Object registeredWith, Plugin plugin) {
        super(aliases[0], desc, usage, Arrays.asList(aliases));
        this.owner = owner;
        this.owningPlugin = plugin;
        this.registeredWith = registeredWith;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        return owner.onCommand(sender, this, label, args);
    }

    public Object getOwner() {
        return owner;
    }

    public Object getRegisteredWith() {
        return registeredWith;
    }

    public void setPermissions(String[] permissions) {
        this.permissions = permissions;
        if (permissions != null) {
            super.setPermission(StringUtil.joinString(permissions, ";"));
        }
    }

    public String[] getPermissions() {
        return permissions;
    }

    @Override
    public Plugin getPlugin() {
        return owningPlugin;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (registeredWith instanceof CommandInspector) {
            return ((TabCompleter) owner).onTabComplete(sender, this, alias, args);
        } else {
            return super.tabComplete(sender, alias, args);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean testPermissionSilent(CommandSender sender) {
        if (permissions == null || permissions.length == 0) {
            return true;
        }

        if (registeredWith instanceof CommandInspector) {
            CommandInspector resolver = (CommandInspector) registeredWith;
            return resolver.testPermission(sender, this);
        } else if (registeredWith instanceof CommandsManager<?>) {
            try {
                for (String permission : permissions) {
                    if (((CommandsManager<CommandSender>) registeredWith).hasPermission(sender, permission)) {
                        return true;
                    }
                }
                return false;
            } catch (Throwable ignored) {
            }
        } else if (PermissionsResolverManager.isInitialized() && sender instanceof OfflinePlayer) {
            for (String permission : permissions) {
                if (PermissionsResolverManager.getInstance().hasPermission((OfflinePlayer) sender, permission)) {
                    return true;
                }
            }
            return false;
        }
        return super.testPermissionSilent(sender);
    }
}
