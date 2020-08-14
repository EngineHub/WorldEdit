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
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.HeightConverter;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.MultiDirection;
import com.sk89q.worldedit.internal.annotation.VertHeight;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.SubCommandPart;

import java.util.List;

import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;
import static com.sk89q.worldedit.internal.command.CommandUtil.requireIV;
import static java.util.Objects.requireNonNull;
import static org.enginehub.piston.part.CommandParts.arg;

/**
 * Extracted from {@link SelectionCommands} to allow importing of {@link Command}.
 */
@CommandContainer
public class ExpandCommands {

    public static void register(CommandRegistrationHandler registration,
                                CommandManager commandManager,
                                CommandManagerService commandManagerService) {
        // Collect the general expand command
        CommandManager collect = commandManagerService.newCommandManager();

        registration.register(
            collect,
            ExpandCommandsRegistration.builder(),
            new ExpandCommands()
        );

        Command expandBaseCommand = collect.getCommand("/expand")
            .orElseThrow(() -> new IllegalStateException("No /expand command"));

        commandManager.register("/expand", command -> {
            command.condition(new PermissionCondition(ImmutableSet.of("worldedit.selection.expand")));

            command.addPart(SubCommandPart.builder(
                TranslatableComponent.of("vert"),
                TextComponent.of("Vertical expansion sub-command")
            )
                .withCommands(ImmutableSet.of(createVertCommand(commandManager)))
                .optional()
                .build());

            command.addParts(expandBaseCommand.getParts());
            command.action(expandBaseCommand.getAction());
            command.description(expandBaseCommand.getDescription());
        });
    }

    private static Command createVertCommand(CommandManager commandManager) {
        CommandArgument heightPart = arg(
            TranslatableComponent.of("height"),
            TextComponent.of("The height to expand both upwards and downwards")
        )
            .defaultsTo(ImmutableList.of(HeightConverter.DEFAULT_VALUE))
            .build();
        return commandManager.newCommand("vert")
            .addPart(heightPart)
            .description(TranslatableComponent.of("worldedit.expand.description.vert"))
            .action(parameters -> {
                int height = requireNonNull(parameters.valueOf(heightPart)
                    .asSingle(Key.of(int.class, VertHeight.class)));
                expandVert(
                    requireIV(Key.of(LocalSession.class), "localSession", parameters),
                    requireIV(Key.of(Actor.class), "actor", parameters),
                    requireIV(Key.of(World.class), "world", parameters),
                    height
                );
                return 1;
            })
            .build();
    }

    private static void expandVert(LocalSession session, Actor actor, World world,
                                   int height) throws IncompleteRegionException {
        Region region = session.getSelection(world);
        try {
            long oldSize = region.getVolume();
            region.expand(
                BlockVector3.at(0, height, 0),
                BlockVector3.at(0, -height, 0));
            session.getRegionSelector(world).learnChanges();
            long newSize = region.getVolume();
            session.getRegionSelector(world).explainRegionAdjust(actor, session);
            long changeSize = newSize - oldSize;
            actor.printInfo(
                TranslatableComponent.of("worldedit.expand.expanded.vert", TextComponent.of(changeSize))
            );
        } catch (RegionOperationException e) {
            actor.printError(TextComponent.of(e.getMessage()));
        }
    }

    @org.enginehub.piston.annotation.Command(
        name = "/expand",
        desc = "Expand the selection area"
    )
    @Logging(REGION)
    public void expand(Actor actor, World world, LocalSession session,
                       @Arg(desc = "Amount to expand the selection by, can be `vert` to expand to the whole vertical column")
                           int amount,
                       @Arg(desc = "Amount to expand the selection by in the other direction", def = "0")
                           int reverseAmount,
                       @Arg(desc = "Direction to expand", def = Direction.AIM)
                       @MultiDirection
                           List<BlockVector3> direction) throws WorldEditException {
        Region region = session.getSelection(world);
        long oldSize = region.getVolume();

        if (reverseAmount == 0) {
            for (BlockVector3 dir : direction) {
                region.expand(dir.multiply(amount));
            }
        } else {
            for (BlockVector3 dir : direction) {
                region.expand(dir.multiply(amount), dir.multiply(-reverseAmount));
            }
        }

        session.getRegionSelector(world).learnChanges();
        long newSize = region.getVolume();

        session.getRegionSelector(world).explainRegionAdjust(actor, session);

        long changeSize = newSize - oldSize;
        actor.printInfo(TranslatableComponent.of("worldedit.expand.expanded", TextComponent.of(changeSize)));
    }

}
