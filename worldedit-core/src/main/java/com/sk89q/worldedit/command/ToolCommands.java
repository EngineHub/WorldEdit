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

import com.google.common.collect.Collections2;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
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
import com.sk89q.worldedit.command.tool.StackTool;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.tool.TreePlanter;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.SubCommandPermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
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

    private static final Component UNBIND_COMMAND_COMPONENT = TextComponent.builder("/tool unbind", TextColor.AQUA)
                                                                   .clickEvent(ClickEvent.suggestCommand("/tool unbind"))
                                                                   .build();

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
                // Don't register new /tool <whatever> alias
                command = command.toBuilder().aliases(
                    Collections2.filter(command.getAliases(), alias -> !"unbind".equals(alias))
                ).build();
            }
            if (command.getName().equals("stacker")) {
                // Don't register /stacker
                continue;
            }
            commandManager.register(CommandUtil.deprecate(
                command, "Global tool names cause conflicts "
                    + "and will be removed in WorldEdit 8",
                CommandUtil.ReplacementMessageGenerator.forNewCommand(ToolCommands::asNonGlobal)
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

            command.condition(new SubCommandPermissionCondition.Generator(nonGlobalCommands).build());
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
        ItemType type = player.getItemInHand(HandSide.MAIN_HAND).getType();
        boolean set = session.getTool(type) != null
            || type.getId().equals(session.getWandItem())
            || type.getId().equals(session.getNavWandItem());
        if (set) {
            session.setTool(type, null);
            player.printInfo(TranslatableComponent.of(isBrush ? "worldedit.brush.none.equip" : "worldedit.tool.none.equip"));
        } else {
            player.printInfo(TranslatableComponent.of("worldedit.tool.none.to.unequip"));
        }
    }

    static void sendUnbindInstruction(Player sender, Component commandComponent) {
        sender.printDebug(TranslatableComponent.of("worldedit.tool.unbind-instruction", commandComponent));
    }

    private static void setTool(Player player, LocalSession session, Tool tool,
                                String translationKey) throws InvalidToolBindException {
        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), tool);
        player.printInfo(TranslatableComponent.of(translationKey, itemStack.getRichName()));
        sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
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
        setTool(player, session, new SelectionWand(), "worldedit.tool.selwand.equip");
    }

    @Command(
        name = "navwand",
        aliases = "/navwand",
        desc = "Navigation wand tool"
    )
    @CommandPermissions("worldedit.setwand")
    public void navwand(Player player, LocalSession session) throws WorldEditException {
        setTool(player, session, new NavigationWand(), "worldedit.tool.navwand.equip");
    }

    @Command(
        name = "info",
        desc = "Block information tool"
    )
    @CommandPermissions("worldedit.tool.info")
    public void info(Player player, LocalSession session) throws WorldEditException {
        setTool(player, session, new QueryTool(), "worldedit.tool.info.equip");
    }

    @Command(
        name = "tree",
        desc = "Tree generator tool"
    )
    @CommandPermissions("worldedit.tool.tree")
    public void tree(Player player, LocalSession session,
                     @Arg(desc = "Type of tree to generate", def = "tree")
                     TreeGenerator.TreeType type) throws WorldEditException {
        setTool(player, session, new TreePlanter(type), "worldedit.tool.tree.equip");
    }

    @Command(
        name = "stacker",
        desc = "Block stacker tool"
    )
    @CommandPermissions("worldedit.tool.stack")
    public void stacker(Player player, LocalSession session,
                        @Arg(desc = "The max range of the stack", def = "10")
                            int range,
                        @Arg(desc = "The mask to stack until", def = "!#existing")
                            Mask mask) throws WorldEditException {
        setTool(player, session, new StackTool(range, mask), "worldedit.tool.stack.equip");
    }

    @Command(
        name = "repl",
        desc = "Block replacer tool"
    )
    @CommandPermissions("worldedit.tool.replacer")
    public void repl(Player player, LocalSession session,
                     @Arg(desc = "The pattern of blocks to place")
                         Pattern pattern) throws WorldEditException {
        setTool(player, session, new BlockReplacer(pattern), "worldedit.tool.repl.equip");
    }

    @Command(
        name = "cycler",
        desc = "Block data cycler tool"
    )
    @CommandPermissions("worldedit.tool.data-cycler")
    public void cycler(Player player, LocalSession session) throws WorldEditException {
        setTool(player, session, new BlockDataCyler(), "worldedit.tool.data-cycler.equip");
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
            player.printError(TranslatableComponent.of("worldedit.tool.superpickaxe.max-range", TextComponent.of(config.maxSuperPickaxeSize)));
            return;
        }
        setTool(player, session, new FloodFillTool(range, pattern), "worldedit.tool.floodfill.equip");
    }

    @Command(
        name = "deltree",
        desc = "Floating tree remover tool"
    )
    @CommandPermissions("worldedit.tool.deltree")
    public void deltree(Player player, LocalSession session) throws WorldEditException {
        setTool(player, session, new FloatingTreeRemover(), "worldedit.tool.deltree.equip");
    }

    @Command(
        name = "farwand",
        desc = "Wand at a distance tool"
    )
    @CommandPermissions("worldedit.tool.farwand")
    public void farwand(Player player, LocalSession session) throws WorldEditException {
        setTool(player, session, new DistanceWand(), "worldedit.tool.farwand.equip");
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
        setTool(player, session, new LongRangeBuildTool(primary, secondary), "worldedit.tool.lrbuild.equip");
        Component primaryName;
        Component secondaryName;
        if (primary instanceof BlockStateHolder) {
            primaryName = ((BlockStateHolder<?>) primary).getBlockType().getRichName();
        } else {
            primaryName = TextComponent.of("pattern");
        }
        if (secondary instanceof BlockStateHolder) {
            secondaryName = ((BlockStateHolder<?>) secondary).getBlockType().getRichName();
        } else {
            secondaryName = TextComponent.of("pattern");
        }
        player.printInfo(TranslatableComponent.of("worldedit.tool.lrbuild.set", primaryName, secondaryName));
    }
}
