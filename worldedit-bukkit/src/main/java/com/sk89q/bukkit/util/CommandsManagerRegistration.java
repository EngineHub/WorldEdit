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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Deprecated
@SuppressWarnings("deprecation")
public class CommandsManagerRegistration extends CommandRegistration {

    protected CommandsManager<?> commands;

    public CommandsManagerRegistration(Plugin plugin, CommandsManager<?> commands) {
        super(plugin);
        this.commands = commands;
    }

    public CommandsManagerRegistration(Plugin plugin, CommandExecutor executor, CommandsManager<?> commands) {
        super(plugin, executor);
        this.commands = commands;
    }

    public boolean register(Class<?> clazz) {
        return registerAll(commands.registerAndReturn(clazz));
    }

    public boolean registerAll(List<Command> registered) {
        List<CommandInfo> toRegister = new ArrayList<>();
        for (Command command : registered) {
            List<String> permissions = null;
            Method cmdMethod = commands.getMethods().get(null).get(command.aliases()[0]);
            Map<String, Method> childMethods = commands.getMethods().get(cmdMethod);

            if (cmdMethod != null && cmdMethod.isAnnotationPresent(CommandPermissions.class)) {
                permissions = Arrays.asList(cmdMethod.getAnnotation(CommandPermissions.class).value());
            } else if (cmdMethod != null && childMethods != null && !childMethods.isEmpty()) {
                permissions = new ArrayList<>();
                for (Method m : childMethods.values()) {
                    if (m.isAnnotationPresent(CommandPermissions.class)) {
                        permissions.addAll(Arrays.asList(m.getAnnotation(CommandPermissions.class).value()));
                    }
                }
            }

            toRegister.add(new CommandInfo(command.usage(), command.desc(), command.aliases(), commands, permissions == null ? null : permissions.toArray(new String[permissions.size()])));
        }

        return register(toRegister);
    }

}
