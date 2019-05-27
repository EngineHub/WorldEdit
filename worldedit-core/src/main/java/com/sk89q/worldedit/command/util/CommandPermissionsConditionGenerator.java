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

package com.sk89q.worldedit.command.util;

import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.Command;
import org.enginehub.piston.gen.CommandConditionGenerator;
import org.enginehub.piston.util.NonnullByDefault;

import java.lang.reflect.Method;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@NonnullByDefault
public final class CommandPermissionsConditionGenerator implements CommandConditionGenerator {

    public interface Registration {
        Registration commandPermissionsConditionGenerator(CommandPermissionsConditionGenerator generator);
    }

    @Override
    public Command.Condition generateCondition(Method commandMethod) {
        CommandPermissions annotation = commandMethod.getAnnotation(CommandPermissions.class);
        checkNotNull(annotation, "Annotation is missing from commandMethod");
        Set<String> permissions = ImmutableSet.copyOf(annotation.value());
        return new PermissionCondition(permissions);
    }
}
