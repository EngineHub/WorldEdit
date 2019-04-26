package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.command.factory.TreeGeneratorFactory;
import com.sk89q.worldedit.command.factory.ItemUseFactory;
import com.sk89q.worldedit.command.factory.ReplaceFactory;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.factory.Apply;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.command.CommandUtil;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.DefaultCommandManagerService;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.SubCommandPart;

import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.enginehub.piston.part.CommandParts.arg;

@CommandContainer
public class ApplyBrushCommands {

    private static final CommandArgument REGION_FACTORY = arg(TranslatableComponent.of("regionFactory"), TextComponent.of("The shape of the region"))
        .defaultsTo(ImmutableList.of())
        .ofTypes(ImmutableList.of(Key.of(RegionFactory.class)))
        .build();

    private static final CommandArgument RADIUS = arg(TranslatableComponent.of("radius"), TextComponent.of("The size of the brush"))
        .defaultsTo(ImmutableList.of("5"))
        .ofTypes(ImmutableList.of(Key.of(double.class)))
        .build();

    public static void register(CommandManager commandManager) {
        commandManager.register("apply", builder -> {
            builder.description(TextComponent.of("Apply brush, apply a function to every block"));
            builder.action(org.enginehub.piston.Command.Action.NULL_ACTION);

            CommandManager manager = DefaultCommandManagerService.getInstance()
                .newCommandManager();
            CommandUtil.register(
                manager,
                ApplyBrushCommandsRegistration.builder(),
                new ApplyBrushCommands()
            );

            builder.condition(new PermissionCondition(ImmutableSet.of("worldedit.brush.apply")));

            builder.addParts(REGION_FACTORY, RADIUS);
            builder.addPart(SubCommandPart.builder(TranslatableComponent.of("type"), TextComponent.of("Type of brush to use"))
                .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                .build());
        });
    }

    private void setApplyBrush(CommandParameters parameters, Player player, LocalSession localSession,
                               Contextual<? extends RegionFunction> generatorFactory) throws WorldEditException {
        double radius = requireNonNull(RADIUS.value(parameters).asSingle(double.class));
        RegionFactory regionFactory = REGION_FACTORY.value(parameters).asSingle(RegionFactory.class);
        BrushCommands.setOperationBasedBrush(player, localSession, radius,
            new Apply(generatorFactory), regionFactory, "worldedit.brush.apply");
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
    public void item(CommandParameters parameters,
                     Player player, LocalSession localSession,
                     @Arg(desc = "The type of item to use")
                         BaseItem item) throws WorldEditException {
        setApplyBrush(parameters, player, localSession, new ItemUseFactory(item));
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
