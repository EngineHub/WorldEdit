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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.command.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class DocumentationPrinter {

    private DocumentationPrinter() {
    }

    /**
     * Generates documentation.
     *
     * @param args arguments
     * @throws IOException thrown on I/O error
     */
    public static void main(String[] args) throws IOException {
        File commandsDir = new File(args[0]);

        List<Class<?>> commandClasses = getCommandClasses(commandsDir);

        System.out.println("Writing permissions wiki table...");
        writePermissionsWikiTable(commandClasses);
        System.out.println("Writing Bukkit plugin.yml...");
        writeBukkitYAML();

        System.out.println("Done!");
    }

    private static List<Class<?>> getCommandClasses(File dir) {
        List<Class<?>> classes = new ArrayList<>();

        classes.add(BiomeCommands.class);
        classes.add(ChunkCommands.class);
        classes.add(ClipboardCommands.class);
        classes.add(GeneralCommands.class);
        classes.add(GenerationCommands.class);
        classes.add(HistoryCommands.class);
        classes.add(NavigationCommands.class);
        classes.add(RegionCommands.class);
        classes.add(ScriptingCommands.class);
        classes.add(SelectionCommands.class);
        classes.add(SnapshotUtilCommands.class);
        classes.add(ToolUtilCommands.class);
        classes.add(ToolCommands.class);
        classes.add(UtilityCommands.class);

        /*for (File f : dir.listFiles()) {
            if (!f.getName().matches("^.*\\.java$")) {
                continue;
            }
            
            String className = "com.sk89q.worldedit.commands."
                + f.getName().substring(0, f.getName().lastIndexOf("."));
            
            Class<?> cls;
            try {
                cls = Class.forName(className, true,
                        Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException e) {
                continue;
            }
            
            classes.add(cls);
        }*/

        return classes;
    }

    private static void writePermissionsWikiTable(List<Class<?>> commandClasses)
            throws IOException {
        try (FileOutputStream stream = new FileOutputStream("wiki_permissions.txt")) {
            PrintStream print = new PrintStream(stream);
            writePermissionsWikiTable(print, commandClasses, "/");
        }
    }

    private static void writePermissionsWikiTable(PrintStream stream,
                                                  List<Class<?>> commandClasses, String prefix) {

        for (Class<?> cls : commandClasses) {
            for (Method method : cls.getMethods()) {
                if (!method.isAnnotationPresent(Command.class)) {
                    continue;
                }

                Command cmd = method.getAnnotation(Command.class);

                stream.println("|-");
                stream.print("| " + prefix + cmd.aliases()[0]);
                stream.print(" || ");

                if (method.isAnnotationPresent(CommandPermissions.class)) {
                    CommandPermissions perms =
                            method.getAnnotation(CommandPermissions.class);

                    String[] permKeys = perms.value();
                    for (int i = 0; i < permKeys.length; ++i) {
                        if (i > 0) {
                            stream.print(", ");
                        }
                        stream.print(permKeys[i]);
                    }
                }

                stream.print(" || ");

                boolean firstAlias = true;
                if (cmd.aliases().length != 0) {
                    for (String alias : cmd.aliases()) {
                        if (!firstAlias) stream.print("<br />");
                        stream.print(prefix + alias);
                        firstAlias = false;
                    }
                }

                stream.print(" || ");

                if (cmd.flags() != null && !cmd.flags().equals("")) {
                    stream.print(cmd.flags());
                }

                stream.print(" || ");

                if (cmd.desc() != null && !cmd.desc().equals("")) {
                    stream.print(cmd.desc());
                }

                stream.println();

                if (method.isAnnotationPresent(NestedCommand.class)) {
                    NestedCommand nested =
                            method.getAnnotation(NestedCommand.class);

                    Class<?>[] nestedClasses = nested.value();
                    writePermissionsWikiTable(stream,
                            Arrays.asList(nestedClasses),
                            prefix + cmd.aliases()[0] + " ");
                }
            }
        }
    }

    private static void writeBukkitYAML()
            throws IOException {
        try (FileOutputStream stream = new FileOutputStream("plugin.yml")) {
            PrintStream print = new PrintStream(stream);
            writeBukkitYAML(print);
        }
    }

    private static void writeBukkitYAML(PrintStream stream) {
        stream.println("name: WorldEdit");
        stream.println("main: com.sk89q.worldedit.bukkit.WorldEditPlugin");
        stream.println("version: ${project.version}");
        stream.println("softdepend: [Spout] #hack to fix trove errors");

        stream.println();
        stream.println();
        stream.println("# Permissions aren't here. Read http://wiki.sk89q.com/wiki/WEPIF/DinnerPerms");
        stream.println("# for how WorldEdit permissions actually work.");
    }

}
