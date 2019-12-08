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

import com.google.common.collect.Collections2;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.tool.BlockDataCyler;
import com.sk89q.worldedit.command.tool.BlockReplacer;
import com.sk89q.worldedit.command.tool.DistanceWand;
import com.sk89q.worldedit.command.tool.FloatingTreeRemover;
import com.sk89q.worldedit.command.tool.FloodFillTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.LongRangeBuildTool;
import com.sk89q.worldedit.command.tool.NavigationWand;
import com.sk89q.worldedit.command.tool.QueryTool;
import com.sk89q.worldedit.command.tool.SelectionWand;
import com.sk89q.worldedit.command.tool.TreePlanter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.item.ItemType;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.CommandMetadata;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.part.SubCommandPart;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class ToolCommands {

    public static void register(CommandRegistrationHandler registration,
                                CommandManager commandManager,
                                CommandManagerService commandManagerService,
                                WorldEdit worldEdit) {
        // Collect the tool commands
        CommandManager collect = commandManagerService.newCommandManager();

        registration.register(
            collect,
            ToolCommandsRegistration.builder(),
            new ToolCommands(worldEdit)
        );

        // Register deprecated global commands
        Set<org.enginehub.piston.Command> commands = collect.getAllCommands()
            .collect(Collectors.toSet());
        for (org.enginehub.piston.Command command : commands) {
            if (command.getAliases().contains("unbind")) {
                // Don't register new /tool unbind alias
                command = command.toBuilder().aliases(
                    Collections2.filter(command.getAliases(), alias -> !"unbind".equals(alias))
                ).build();
            }
            commandManager.register(CommandUtil.deprecate(
                command, "Global tool names cause conflicts " +
                "and will be removed in WorldEdit 8", ToolCommands::asNonGlobal
            ));
        }

        // Remove aliases with / in them, since it doesn't make sense for sub-commands.
        Set<org.enginehub.piston.Command> nonGlobalCommands = commands.stream()
            .map(command ->
                command.toBuilder().aliases(
                    Collections2.filter(command.getAliases(), alias -> !alias.startsWith("/"))
                ).build()
            )
            .collect(Collectors.toSet());
        commandManager.register("tool", command -> {
            command.addPart(SubCommandPart.builder(
                TranslatableComponent.of("tool"),
                TextComponent.of("The tool to bind")
            )
                .withCommands(nonGlobalCommands)
                .required()
                .build());
            command.description(TextComponent.of("Binds a tool to the item in your hand"));
        });
    }

    private static String asNonGlobal(org.enginehub.piston.Command oldCommand,
                                      CommandParameters oldParameters) {
        String name = Optional.ofNullable(oldParameters.getMetadata())
            .map(CommandMetadata::getCalledName)
            .filter(n -> !n.startsWith("/"))
            .orElseGet(oldCommand::getName);
        return "/tool " + name;
    }

    static void setToolNone(Player player, LocalSession session, boolean isBrush)
        throws InvalidToolBindException {
        session.setTool(player.getItemInHand(HandSide.MAIN_HAND).getType(), null);
        player.printInfo(TranslatableComponent.of(isBrush ? "worldedit.brush.none.equip" : "worldedit.tool.none.equip"));
    }

    private final WorldEdit we;

    public ToolCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        name = "none",
        aliases = "unbind",
        desc = "Unbind a bound tool from your current item"
    )
    public void none(Player player, LocalSession session) throws WorldEditException {
        setToolNone(player, session, false);
    }

    @Command(
        name = "selwand",
        aliases = "/selwand",
        desc = "Selection wand tool"
    )
    @CommandPermissions("worldedit.setwand")
    public void selwand(Player player, LocalSession session) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new SelectionWand());
        player.printInfo(TranslatableComponent.of("worldedit.tool.selwand.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "navwand",
        aliases = "/navwand",
        desc = "Navigation wand tool"
    )
    @CommandPermissions("worldedit.setwand")
    public void navwand(Player player, LocalSession session) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new NavigationWand());
        player.printInfo(TranslatableComponent.of("worldedit.tool.navWand.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "info",
        desc = "Block information tool"
    )
    @CommandPermissions("worldedit.tool.info")
    public void info(Player player, LocalSession session) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new QueryTool());
        player.printInfo(TranslatableComponent.of("worldedit.tool.info.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "tree",
        desc = "Tree generator tool"
    )
    @CommandPermissions("worldedit.tool.tree")
    public void tree(Player player, LocalSession session,
                     @Arg(desc = "Type of tree to generate", def = "tree")
                     TreeGenerator.TreeType type) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new TreePlanter(type));
        player.printInfo(TranslatableComponent.of("worldedit.tool.tree.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "repl",
        desc = "Block replacer tool"
    )
    @CommandPermissions("worldedit.tool.replacer")
    public void repl(Player player, LocalSession session,
                     @Arg(desc = "The pattern of blocks to place")
                         Pattern pattern) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new BlockReplacer(pattern));
        player.printInfo(TranslatableComponent.of("worldedit.tool.repl.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "cycler",
        desc = "Block data cycler tool"
    )
    @CommandPermissions("worldedit.tool.data-cycler")
    public void cycler(Player player, LocalSession session) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new BlockDataCyler());
        player.printInfo(TranslatableComponent.of("worldedit.tool.data-cycler.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "floodfill",
        aliases = { "flood" },
        desc = "Flood fill tool"
    )
    @CommandPermissions("worldedit.tool.flood-fill")
    public void floodFill(Player player, LocalSession session,
                          @Arg(desc = "The pattern to flood fill")
                              Pattern pattern,
                          @Arg(desc = "The range to perform the fill")
                              int range) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (range > config.maxSuperPickaxeSize) {
            player.printError(TranslatableComponent.of("worldedit.superpickaxe.max-range", TextComponent.of(config.maxSuperPickaxeSize)));
            return;
        }

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new FloodFillTool(range, pattern));
        player.printInfo(TranslatableComponent.of("worldedit.tool.floodfill.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "deltree",
        desc = "Floating tree remover tool"
    )
    @CommandPermissions("worldedit.tool.deltree")
    public void deltree(Player player, LocalSession session) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new FloatingTreeRemover());
        player.printInfo(TranslatableComponent.of("worldedit.tool.deltree.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "farwand",
        desc = "Wand at a distance tool"
    )
    @CommandPermissions("worldedit.tool.farwand")
    public void farwand(Player player, LocalSession session) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new DistanceWand());
        player.printInfo(TranslatableComponent.of("worldedit.tool.farwand.equip", TextComponent.of(itemType.getName())));
    }

    @Command(
        name = "lrbuild",
        aliases = { "/lrbuild" },
        desc = "Long-range building tool"
    )
    @CommandPermissions("worldedit.tool.lrbuild")
    public void longrangebuildtool(Player player, LocalSession session,
                                   @Arg(desc = "Pattern to set on left-click")
                                       Pattern primary,
                                   @Arg(desc = "Pattern to set on right-click")
                                       Pattern secondary) throws WorldEditException {

        final ItemType itemType = player.getItemInHand(HandSide.MAIN_HAND).getType();
        session.setTool(itemType, new LongRangeBuildTool(primary, secondary));
        player.printInfo(TranslatableComponent.of("worldedit.tool.lrbuild.equip", TextComponent.of(itemType.getName())));
        String primaryName = "pattern";
        String secondaryName = "pattern";
        if (primary instanceof BlockStateHolder) {
            primaryName = ((BlockStateHolder<?>) primary).getBlockType().getName();
        }
        if (secondary instanceof BlockStateHolder) {
            secondaryName = ((BlockStateHolder<?>) secondary).getBlockType().getName();
        }
        player.printInfo(TranslatableComponent.of("worldedit.tool.lrbuild.set", TextComponent.of(primaryName), TextComponent.of(secondaryName)));
    }
}
