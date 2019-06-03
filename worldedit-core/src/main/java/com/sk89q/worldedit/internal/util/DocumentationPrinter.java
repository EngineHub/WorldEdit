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

package com.sk89q.worldedit.internal.util;

import com.google.common.base.Strings;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.serializer.plain.PlainComponentSerializer;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.TextConfig;
import org.enginehub.piston.part.SubCommandPart;

import java.util.stream.Stream;

public final class DocumentationPrinter {

    private DocumentationPrinter() {
    }

    /**
     * Generates documentation.
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        final PlatformManager platformManager = WorldEdit.getInstance().getPlatformManager();
        PlatformCommandManager mgr = platformManager.getPlatformCommandManager();
        final CommandManager commandManager = mgr.getCommandManager();
        dumpCommands(commandManager);
    }

    private static void dumpCommands(CommandManager commandManager) {
        final PlainComponentSerializer serializer = new PlainComponentSerializer(kbc -> "", TranslatableComponent::key);
        cmdToUsages(serializer, commandManager.getAllCommands(), TextConfig.getCommandPrefix(), 0);

        cmdsToPerms(commandManager.getAllCommands(), TextConfig.getCommandPrefix());
    }

    private static void cmdsToPerms(Stream<Command> cmds, String prefix) {
        cmds.forEach(c -> {
            System.out.println("    " + cmdToPerm(prefix, c));
            c.getParts().stream().filter(p -> p instanceof SubCommandPart).map(p -> (SubCommandPart) p)
                    .forEach(scp -> cmdsToPerms(scp.getCommands().stream(), prefix + c.getName() + " "));
        });
    }

    private static String cmdToPerm(String prefix, Command c) {
        return prefix + c.getName() + ",\"" + (c.getCondition() instanceof PermissionCondition
                ? String.join(", ", ((PermissionCondition) c.getCondition()).getPermissions()) : "") + "\"";
    }

    private static void cmdToUsages(PlainComponentSerializer serializer, Stream<Command> cmds, String prefix, int indent) {
        cmds.forEach(c -> {
            System.out.println(Strings.repeat("\t", indent) + cmdToString(serializer, prefix, c, indent));
            c.getParts().stream().filter(p -> p instanceof SubCommandPart).map(p -> (SubCommandPart) p)
                    .forEach(scp -> cmdToUsages(serializer, scp.getCommands().stream(), prefix + c.getName() + " ", indent + 1));
            System.out.println();
        });
    }

    private static String cmdToString(PlainComponentSerializer serializer, String prefix, Command c, int indent) {
        return serializer.serialize(TextComponent.of(prefix + c.getName()).append(TextComponent.newline())
            .append(TextComponent.of(c.getCondition() instanceof PermissionCondition
                    ? "Permissions: " + (String.join(", ", ((PermissionCondition) c.getCondition()).getPermissions())) + "\n"
                    : ""))
            .append(c.getFullHelp())).replace("\n", "\n" + Strings.repeat("\t", indent));
    }

}
