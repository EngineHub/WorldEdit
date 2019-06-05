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
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.ApplyBrushCommands;
import com.sk89q.worldedit.command.BiomeCommands;
import com.sk89q.worldedit.command.BiomeCommandsRegistration;
import com.sk89q.worldedit.command.BrushCommands;
import com.sk89q.worldedit.command.BrushCommandsRegistration;
import com.sk89q.worldedit.command.ChunkCommands;
import com.sk89q.worldedit.command.ChunkCommandsRegistration;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.command.ClipboardCommandsRegistration;
import com.sk89q.worldedit.command.ExpandCommands;
import com.sk89q.worldedit.command.GeneralCommands;
import com.sk89q.worldedit.command.GeneralCommandsRegistration;
import com.sk89q.worldedit.command.GenerationCommands;
import com.sk89q.worldedit.command.GenerationCommandsRegistration;
import com.sk89q.worldedit.command.HistoryCommands;
import com.sk89q.worldedit.command.HistoryCommandsRegistration;
import com.sk89q.worldedit.command.NavigationCommands;
import com.sk89q.worldedit.command.NavigationCommandsRegistration;
import com.sk89q.worldedit.command.PaintBrushCommands;
import com.sk89q.worldedit.command.RegionCommands;
import com.sk89q.worldedit.command.RegionCommandsRegistration;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.command.SchematicCommandsRegistration;
import com.sk89q.worldedit.command.ScriptingCommands;
import com.sk89q.worldedit.command.ScriptingCommandsRegistration;
import com.sk89q.worldedit.command.SelectionCommands;
import com.sk89q.worldedit.command.SelectionCommandsRegistration;
import com.sk89q.worldedit.command.SnapshotCommands;
import com.sk89q.worldedit.command.SnapshotCommandsRegistration;
import com.sk89q.worldedit.command.SnapshotUtilCommands;
import com.sk89q.worldedit.command.SnapshotUtilCommandsRegistration;
import com.sk89q.worldedit.command.SuperPickaxeCommands;
import com.sk89q.worldedit.command.SuperPickaxeCommandsRegistration;
import com.sk89q.worldedit.command.ToolCommands;
import com.sk89q.worldedit.command.ToolCommandsRegistration;
import com.sk89q.worldedit.command.ToolUtilCommands;
import com.sk89q.worldedit.command.ToolUtilCommandsRegistration;
import com.sk89q.worldedit.command.UtilityCommands;
import com.sk89q.worldedit.command.UtilityCommandsRegistration;
import com.sk89q.worldedit.command.WorldEditCommands;
import com.sk89q.worldedit.command.WorldEditCommandsRegistration;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.serializer.ComponentSerializer;
import com.sk89q.worldedit.util.formatting.text.serializer.plain.PlainComponentSerializer;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.TextConfig;
import org.enginehub.piston.gen.CommandRegistration;
import org.enginehub.piston.impl.CommandManagerImpl;
import org.enginehub.piston.impl.CommandManagerServiceImpl;
import org.enginehub.piston.part.SubCommandPart;
import org.enginehub.piston.util.HelpGenerator;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DocumentationPrinter {

    /**
     * Generates documentation.
     *
     * @param args arguments
     */
    public static void main(String[] args) throws IOException {
        final DocumentationPrinter printer = new DocumentationPrinter();

        printer.writeAllCommands();
        writeOutput("commands.rst", printer.cmdOutput.toString());
        writeOutput("permissions.rst", printer.permsOutput.toString());
    }

    private static void writeOutput(String file, String output) throws IOException {
        File outfile = new File(file);
        Files.write(output, outfile, StandardCharsets.UTF_8);
    }

    private final ComponentSerializer<Component, TextComponent, String> serializer
            = new PlainComponentSerializer(kb -> "", TranslatableComponent::key);
    private final CommandRegistrationHandler registration;
    private final CommandManagerServiceImpl commandManagerService;
    private final WorldEdit worldEdit = WorldEdit.getInstance();
    private StringBuilder cmdOutput;
    private StringBuilder permsOutput;
    private Field mgrCmdField;

    private DocumentationPrinter() {
        this.cmdOutput = new StringBuilder();
        this.permsOutput = new StringBuilder();
        this.registration = new CommandRegistrationHandler(ImmutableList.of());
        this.commandManagerService = new CommandManagerServiceImpl();
        try {
            Field field = CommandManagerImpl.class.getDeclaredField("commands");
            field.setAccessible(true);
            this.mgrCmdField = field;
        } catch (NoSuchFieldException ignored) {
        }
    }

    private <CI> void registerSubCommands(CommandManager parent, String name, List<String> aliases, String desc,
                                          CommandRegistration<CI> registration, CI instance) {
        registerSubCommands(parent, name, aliases, desc, registration, instance, m -> {});
    }

    private <CI> void registerSubCommands(CommandManager parent, String name, List<String> aliases, String desc,
                                          CommandRegistration<CI> registration, CI instance,
                                          Consumer<CommandManager> additionalConfig) {
        parent.register(name, cmd -> {
            cmd.aliases(aliases);
            cmd.description(TextComponent.of(desc));
            cmd.action(Command.Action.NULL_ACTION);

            CommandManager manager = createManager();
            this.registration.register(
                    manager,
                    registration,
                    instance
            );
            additionalConfig.accept(manager);

            cmd.addPart(SubCommandPart.builder(TranslatableComponent.of("worldedit.argument.action"),
                    TextComponent.of("Sub-command to run."))
                    .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                    .required()
                    .build());
        });
    }

    private void writeAllCommands() {
        writeHeader();

        CommandManager manager;

        manager = createManager();
        registerSubCommands(
                manager,
                "worldedit",
                ImmutableList.of("we"),
                "WorldEdit commands",
                WorldEditCommandsRegistration.builder(),
                new WorldEditCommands(worldEdit)
        );
        this.registration.register(
                manager,
                HistoryCommandsRegistration.builder(),
                new HistoryCommands(worldEdit)
        );
        this.registration.register(
                manager,
                GeneralCommandsRegistration.builder(),
                new GeneralCommands(worldEdit)
        );
        dumpSection("General Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                NavigationCommandsRegistration.builder(),
                new NavigationCommands(worldEdit)
        );
        dumpSection("Navigation Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                SelectionCommandsRegistration.builder(),
                new SelectionCommands(worldEdit)
        );
        ExpandCommands.register(registration, manager, commandManagerService);
        dumpSection("Selection Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                RegionCommandsRegistration.builder(),
                new RegionCommands()
        );
        dumpSection("Region Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                GenerationCommandsRegistration.builder(),
                new GenerationCommands(worldEdit)
        );
        dumpSection("Generation Commands", manager);

        manager = createManager();
        registerSubCommands(
                manager,
                "schematic",
                ImmutableList.of("schem", "/schematic", "/schem"),
                "Schematic commands for saving/loading areas",
                SchematicCommandsRegistration.builder(),
                new SchematicCommands(worldEdit)
        );
        this.registration.register(
                manager,
                ClipboardCommandsRegistration.builder(),
                new ClipboardCommands()
        );
        dumpSection("Schematic and Clipboard Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                ToolCommandsRegistration.builder(),
                new ToolCommands(worldEdit)
        );
        this.registration.register(
                manager,
                ToolUtilCommandsRegistration.builder(),
                new ToolUtilCommands(worldEdit)
        );
        dumpSection("Tool Commands", manager);

        manager = createManager();
        registerSubCommands(
                manager,
                "superpickaxe",
                ImmutableList.of("pickaxe", "sp"),
                "Super-pickaxe commands",
                SuperPickaxeCommandsRegistration.builder(),
                new SuperPickaxeCommands(worldEdit)
        );
        dumpSection("Super Pickaxe Commands", manager);

        manager = createManager();
        registerSubCommands(
                manager,
                "brush",
                ImmutableList.of("br", "/brush", "/br"),
                "Brushing commands",
                BrushCommandsRegistration.builder(),
                new BrushCommands(worldEdit),
                mgr -> {
                    PaintBrushCommands.register(commandManagerService, mgr, registration);
                    ApplyBrushCommands.register(commandManagerService, mgr, registration);
                }
        );
        dumpSection("Brush Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                BiomeCommandsRegistration.builder(),
                new BiomeCommands()
        );
        dumpSection("Biome Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                ChunkCommandsRegistration.builder(),
                new ChunkCommands(worldEdit)
        );
        dumpSection("Chunk Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                SnapshotUtilCommandsRegistration.builder(),
                new SnapshotUtilCommands(worldEdit)
        );
        registerSubCommands(
                manager,
                "snapshot",
                ImmutableList.of("snap"),
                "Snapshot commands for restoring backups",
                SnapshotCommandsRegistration.builder(),
                new SnapshotCommands(worldEdit)
        );
        dumpSection("Snapshot Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                ScriptingCommandsRegistration.builder(),
                new ScriptingCommands(worldEdit)
        );
        dumpSection("Scripting Commands", manager);

        manager = createManager();
        this.registration.register(
                manager,
                UtilityCommandsRegistration.builder(),
                new UtilityCommands(worldEdit)
        );
        dumpSection("Utility Commands", manager);
    }

    private void writeHeader() {
        cmdOutput.append(
                "========\n" +
                "Commands\n" +
                "========\n" +
                "\n" +
                ".. contents::\n" +
                "    :local:\n" +
                "\n" +
                ".. tip::\n" +
                "\n" +
                "    Arguments enclosed in ``[ ]`` are optional, those enclosed in ``< >`` are required.\n\n");
    }

    private CommandManager createManager() {
        final CommandManager commandManager = commandManagerService.newCommandManager();
        if (mgrCmdField != null && commandManager instanceof CommandManagerImpl) {
            try {
                mgrCmdField.set(commandManager, new LinkedHashMap<>());
            } catch (IllegalAccessException ignored) {
            }
        }
        return commandManager;
    }

    private void dumpSection(String title, CommandManager manager) {
        cmdOutput.append("\n").append(title).append("\n").append(Strings.repeat("~", title.length())).append("\n");

        String prefix = TextConfig.getCommandPrefix();

//        permsOutput.append("\n------------\n\n");
        cmdsToPerms(manager.getAllCommands(), prefix);

        for (Command command : manager.getAllCommands().collect(Collectors.toList())) {
            cmdOutput.append("\n------------\n\n");
            writeCommandBlock(command, prefix, Stream.empty());
            command.getParts().stream().filter(p -> p instanceof SubCommandPart)
                    .flatMap(p -> ((SubCommandPart) p).getCommands().stream())
                    .forEach(sc -> {
                        cmdOutput.append("\n------------\n\n");
                        writeCommandBlock(sc, prefix + command.getName() + " ", Stream.of(command));
                    });
        }
    }

    private void cmdsToPerms(Stream<Command> cmds, String prefix) {
        cmds.forEach(c -> {
            permsOutput.append("    ").append(cmdToPerm(prefix, c)).append("\n");
            c.getParts().stream().filter(p -> p instanceof SubCommandPart).map(p -> (SubCommandPart) p)
                    .forEach(scp -> cmdsToPerms(scp.getCommands().stream(), prefix + c.getName() + " "));
        });
    }

    private String cmdToPerm(String prefix, Command c) {
        return prefix + c.getName() + ",\"" + (c.getCondition() instanceof PermissionCondition
                ? String.join(", ", ((PermissionCondition) c.getCondition()).getPermissions()) : "") + "\"";
    }

    private void writeCommandBlock(Command command, String prefix, Stream<Command> parents) {
        String name = prefix + command.getName();
        String desc = serializer.serialize(command.getDescription());
        cmdOutput.append(".. csv-table::\n    :widths: 8, 15\n\n");
        cmdOutput.append("    ").append(name).append(",\"").append(desc).append("\"\n");
        if (!command.getAliases().isEmpty()) {
            cmdOutput.append("    Aliases,\"").append(String.join(", ",
                        command.getAliases().stream().map(a -> prefix + a).collect(Collectors.toSet())))
                    .append("\"\n");
        }
        if (command.getCondition() instanceof PermissionCondition) {
            cmdOutput.append("    Permissions,\"").append(String.join(", ", ((PermissionCondition) command.getCondition()).getPermissions())).append("\"\n");
        }
        cmdOutput.append("    Usage,\"").append(serializer.serialize(
                HelpGenerator.create(Stream.concat(parents, Stream.of(command)).collect(Collectors.toList())).getUsage())).append("\"\n");
        command.getParts().stream().filter(part -> !(part instanceof SubCommandPart)).forEach(part ->
                cmdOutput.append("     \u2001\u2001").append(serializer.serialize(part.getTextRepresentation())).append(",\"")
                        .append(serializer.serialize(part.getDescription())).append("\"\n"));
        if (command.getFooter().isPresent()) {
            cmdOutput.append("    ,\"").append(serializer.serialize(command.getFooter().get()).replace("\n", " ")).append("\"\n");
        }
    }
}
