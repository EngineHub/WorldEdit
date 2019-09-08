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

package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.MultiDirection;
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
import org.enginehub.piston.part.SubCommandPart;

import java.util.List;

import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;
import static com.sk89q.worldedit.internal.command.CommandUtil.requireIV;

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
        return commandManager.newCommand("vert")
            .description(TextComponent.of("Vertically expand the selection to world limits."))
            .action(parameters -> {
                expandVert(
                    requireIV(Key.of(LocalSession.class), "localSession", parameters),
                    requireIV(Key.of(Player.class), "localSession", parameters)
                );
                return 1;
            })
            .build();
    }

    private static void expandVert(LocalSession session, Player player) throws IncompleteRegionException {
        Region region = session.getSelection(player.getWorld());
        try {
            int oldSize = region.getArea();
            region.expand(
                BlockVector3.at(0, (player.getWorld().getMaxY() + 1), 0),
                BlockVector3.at(0, -(player.getWorld().getMaxY() + 1), 0));
            session.getRegionSelector(player.getWorld()).learnChanges();
            int newSize = region.getArea();
            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
            player.print("Region expanded " + (newSize - oldSize)
                + " blocks [top-to-bottom].");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
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
        int oldSize = region.getArea();

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
        int newSize = region.getArea();

        session.getRegionSelector(world).explainRegionAdjust(actor, session);

        actor.print("Region expanded " + (newSize - oldSize) + " block(s).");
    }

}
