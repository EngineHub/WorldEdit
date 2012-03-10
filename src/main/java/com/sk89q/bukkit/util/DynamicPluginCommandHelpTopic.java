/*
 * WorldEdit
 * Copyright (C) 2012 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.bukkit.util;

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.wepif.WEPIFRuntimeException;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;

import java.util.Map;

/**
 * @author zml2008
 */
public class DynamicPluginCommandHelpTopic extends HelpTopic {
    private final DynamicPluginCommand cmd;

    public DynamicPluginCommandHelpTopic(DynamicPluginCommand cmd) {
        this.cmd = cmd;
        this.name = "/" + cmd.getName();

        if (cmd.getRegisteredWith() instanceof CommandsManager) {
            Map<String, String> helpText = ((CommandsManager<?>) cmd.getRegisteredWith()).getHelpMessages();
            final String lookupName = cmd.getName().replaceAll("/", "");
            if (helpText.containsKey(lookupName)) {
                this.fullText = helpText.get(lookupName);
            }
            helpText = ((CommandsManager<?>) cmd.getRegisteredWith()).getCommands();
            if (helpText.containsKey(cmd.getName())) {
                final String shortText = helpText.get(cmd.getName());
                if (this.fullText == null) {
                    this.fullText = this.name + " " + shortText;
                }
                this.shortText = shortText;
            }
        } else {
            this.shortText = cmd.getDescription();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean canSee(CommandSender player) {
        if (cmd.getPermissions() != null && cmd.getPermissions().length > 0) {
            if (cmd.getRegisteredWith() instanceof CommandsManager) {
                try {
                    for (String perm : cmd.getPermissions()) {
                        if (((CommandsManager<Object>) cmd.getRegisteredWith()).hasPermission(player, perm)) {
                            return true;
                        }
                    }
                } catch (Throwable t) {
                    // Doesn't take the CommandSender (Hooray for compile-time generics!), we have other methods at our disposal
                }
            }
            if (player instanceof OfflinePlayer) {
                try {
                    for (String perm : cmd.getPermissions()) {
                        if (PermissionsResolverManager.getInstance().hasPermission((OfflinePlayer) player, perm)) {
                            return true;
                        }
                    }
                } catch (WEPIFRuntimeException e) {
                    // PermissionsResolverManager not initialized, eat it
                }
            }
            for (String perm : cmd.getPermissions()) {
                if (player.hasPermission(perm)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public String getFullText(CommandSender forWho) {
        if (this.fullText == null || this.fullText.length() == 0) {
            return getShortText();
        } else {
            return this.fullText;
        }
    }

    public static class Factory implements HelpTopicFactory<DynamicPluginCommand> {

        @Override
        public HelpTopic createTopic(DynamicPluginCommand command) {
            return new DynamicPluginCommandHelpTopic(command);
        }
    }
}
