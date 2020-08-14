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

package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.command.factory.ItemUseFactory;
import com.sk89q.worldedit.command.factory.ReplaceFactory;
import com.sk89q.worldedit.command.factory.TreeGeneratorFactory;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.factory.ApplyRegion;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.util.formatting.text.format.TextDecoration;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.SubCommandPart;

import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.enginehub.piston.part.CommandParts.arg;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class ApplyBrushCommands {

    private static final CommandArgument REGION_FACTORY = arg(TranslatableComponent.of("shape"), TranslatableComponent.of("worldedit.brush.apply.shape"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(Key.of(RegionFactory.class)))
        .build();

    private static final CommandArgument RADIUS = arg(TranslatableComponent.of("radius"), TranslatableComponent.of("worldedit.brush.apply.radius"))
        .defaultsTo(ImmutableList.of("5"))
        .ofTypes(ImmutableList.of(Key.of(double.class)))
        .build();

    public static void register(CommandManagerService service, CommandManager commandManager, CommandRegistrationHandler registration) {
        commandManager.register("apply", builder -> {
            builder.description(TranslatableComponent.of("worldedit.brush.apply.description"));
            builder.action(org.enginehub.piston.Command.Action.NULL_ACTION);

            CommandManager manager = service.newCommandManager();
            registration.register(
                manager,
                ApplyBrushCommandsRegistration.builder(),
                new ApplyBrushCommands()
            );

            builder.condition(new PermissionCondition(ImmutableSet.of("worldedit.brush.apply")));

            builder.addParts(REGION_FACTORY, RADIUS);
            builder.addPart(SubCommandPart.builder(TranslatableComponent.of("type"), TranslatableComponent.of("worldedit.brush.apply.type"))
                .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                .required()
                .build());
        });
    }

    private void setApplyBrush(CommandParameters parameters, Player player, LocalSession localSession,
                               Contextual<? extends RegionFunction> generatorFactory) throws WorldEditException {
        double radius = requireNonNull(RADIUS.value(parameters).asSingle(double.class));
        RegionFactory regionFactory = REGION_FACTORY.value(parameters).asSingle(RegionFactory.class);
        BrushCommands.setOperationBasedBrush(player, localSession, radius,
            new ApplyRegion(generatorFactory), regionFactory, "worldedit.brush.apply");
    }

    @Command(
        name = "forest",
        desc = "Plant trees"
    )
    public void forest(CommandParameters parameters,
                       Player player, LocalSession localSession,
                       @Arg(desc = "The type of tree to plant")
                           TreeGenerator.TreeType type) throws WorldEditException {
        setApplyBrush(parameters, player, localSession, new TreeGeneratorFactory(type));
    }

    @Command(
        name = "item",
        desc = "Use an item"
    )
    @CommandPermissions("worldedit.brush.item")
    public void item(CommandParameters parameters,
                     Player player, LocalSession localSession,
                     @Arg(desc = "The type of item to use")
                         BaseItem item,
                     @Arg(desc = "The direction in which the item will be applied", def = "up")
                     @Direction(includeDiagonals = true)
                         com.sk89q.worldedit.util.Direction direction) throws WorldEditException {
        player.print(TextComponent.builder().append("WARNING: ", TextColor.RED, TextDecoration.BOLD)
                .append(TranslatableComponent.of("worldedit.brush.apply.item.warning")).build());
        setApplyBrush(parameters, player, localSession, new ItemUseFactory(item, direction));
    }

    @Command(
        name = "set",
        desc = "Place a block"
    )
    public void set(CommandParameters parameters,
                    Player player, LocalSession localSession,
                    @Arg(desc = "The pattern of blocks to use")
                        Pattern pattern) throws WorldEditException {
        setApplyBrush(parameters, player, localSession, new ReplaceFactory(pattern));
    }

}
