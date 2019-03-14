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

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.sk89q.worldedit.util.command.CommandMapping;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.function.Predicate;

import static net.minecraft.command.Commands.literal;

public final class CommandWrapper {
    private CommandWrapper() {
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher, CommandMapping command) {
        for (String alias : command.getAllAliases()) {
            LiteralArgumentBuilder<CommandSource> base = literal(alias)
                .executes(FAKE_COMMAND);
            if (command.getDescription().getPermissions().size() > 0) {
                base.requires(requirementsFor(command));
            }
            dispatcher.register(base);
        }
    }

    public static final Command<CommandSource> FAKE_COMMAND = ctx -> {
        EntityPlayerMP player = ctx.getSource().asPlayer();
        if (player.world.isRemote()) {
            return 0;
        }
        return 1;
    };

    private static Predicate<CommandSource> requirementsFor(CommandMapping mapping) {
        return ctx -> {
            ForgePermissionsProvider permsProvider = ForgeWorldEdit.inst.getPermissionsProvider();
            return ctx.getEntity() instanceof EntityPlayerMP &&
                mapping.getDescription().getPermissions().stream()
                    .allMatch(perm -> permsProvider.hasPermission(
                        (EntityPlayerMP) ctx.getEntity(), perm
                    ));
        };
    }

}
