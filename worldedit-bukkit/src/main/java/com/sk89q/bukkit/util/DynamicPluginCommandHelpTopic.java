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
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.wepif.WEPIFRuntimeException;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.HelpTopicFactory;

import java.util.Map;

@SuppressWarnings("deprecation")
public class DynamicPluginCommandHelpTopic extends HelpTopic {

    private final DynamicPluginCommand cmd;

    public DynamicPluginCommandHelpTopic(DynamicPluginCommand cmd) {
        this.cmd = cmd;
        this.name = "/" + cmd.getName();

        if (cmd.getRegisteredWith() instanceof CommandInspector) {
            CommandInspector resolver = (CommandInspector) cmd.getRegisteredWith();
            this.shortText = resolver.getShortText(cmd);
            this.fullText = resolver.getFullText(cmd);
        } else {
            String fullTextTemp = null;
            StringBuilder fullText = new StringBuilder();

            if (cmd.getRegisteredWith() instanceof CommandsManager) {
                Map<String, String> helpText = ((CommandsManager<?>) cmd.getRegisteredWith()).getHelpMessages();
                final String lookupName = cmd.getName().replaceAll("/", "");
                if (helpText.containsKey(lookupName)) { // We have full help text for this command
                    fullTextTemp = helpText.get(lookupName);
                }
                // No full help text, assemble help text from info
                helpText = ((CommandsManager<?>) cmd.getRegisteredWith()).getCommands();
                if (helpText.containsKey(cmd.getName())) {
                    final String shortText = helpText.get(cmd.getName());
                    if (fullTextTemp == null) {
                        fullTextTemp = this.name + " " + shortText;
                    }
                    this.shortText = shortText;
                }
            } else {
                this.shortText = cmd.getDescription();
            }

            // Put the usage in the format: Usage string (newline) Aliases (newline) Help text
            String[] split = fullTextTemp == null ? new String[2] : fullTextTemp.split("\n", 2);
            fullText.append(ChatColor.BOLD).append(ChatColor.GOLD).append("Usage: ").append(ChatColor.WHITE);
            fullText.append(split[0]).append("\n");

            if (!cmd.getAliases().isEmpty()) {
                fullText.append(ChatColor.BOLD).append(ChatColor.GOLD).append("Aliases: ").append(ChatColor.WHITE);
                boolean first = true;
                for (String alias : cmd.getAliases()) {
                    if (!first) {
                        fullText.append(", ");
                    }
                    fullText.append(alias);
                    first = false;
                }
                fullText.append("\n");
            }
            if (split.length > 1) {
                fullText.append(split[1]);
            }
            this.fullText = fullText.toString();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean canSee(CommandSender player) {
        if (cmd.getRegisteredWith() instanceof CommandInspector) {
            CommandInspector resolver = (CommandInspector) cmd.getRegisteredWith();
            return resolver.testPermission(player, cmd);
        } else if (cmd.getPermissions() != null && cmd.getPermissions().length > 0) {
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
        if (this.fullText == null || this.fullText.isEmpty()) {
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
