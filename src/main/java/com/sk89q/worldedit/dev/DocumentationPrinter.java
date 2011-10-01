// $Id$
/*
 * WorldEditLibrary
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.dev;

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.commands.ChunkCommands;
import com.sk89q.worldedit.commands.ClipboardCommands;
import com.sk89q.worldedit.commands.GeneralCommands;
import com.sk89q.worldedit.commands.GenerationCommands;
import com.sk89q.worldedit.commands.HistoryCommands;
import com.sk89q.worldedit.commands.NavigationCommands;
import com.sk89q.worldedit.commands.RegionCommands;
import com.sk89q.worldedit.commands.ScriptingCommands;
import com.sk89q.worldedit.commands.SelectionCommands;
import com.sk89q.worldedit.commands.SnapshotUtilCommands;
import com.sk89q.worldedit.commands.ToolCommands;
import com.sk89q.worldedit.commands.ToolUtilCommands;
import com.sk89q.worldedit.commands.UtilityCommands;

public class DocumentationPrinter {
    public static void main(String[] args) throws IOException {
        File commandsDir = new File(args[0]);
        
        List<Class<?>> commandClasses = getCommandClasses(commandsDir);

        System.out.println("Writing permissions wiki table...");
        writePermissionsWikiTable(commandClasses);
        System.out.println("Writing Bukkit plugin.yml...");
        writeBukkitYAML(commandClasses);

        System.out.println("Done!");
    }
    
    private static List<Class<?>> getCommandClasses(File dir) {
        List<Class<?>> classes = new ArrayList<Class<?>>();

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
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream("wiki_permissions.txt");
            PrintStream print = new PrintStream(stream);
            _writePermissionsWikiTable(print, commandClasses, "/");
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
    
    private static void _writePermissionsWikiTable(PrintStream stream,
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
                    _writePermissionsWikiTable(stream,
                            Arrays.asList(nestedClasses),
                            prefix + cmd.aliases()[0] + " ");
                }
            }
        }
    }
    
    private static void writeBukkitYAML(List<Class<?>> commandClasses)
            throws IOException {
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream("plugin.yml");
            PrintStream print = new PrintStream(stream);
            _writeBukkitYAML(print, commandClasses);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }
    
    private static void _writeBukkitYAML(PrintStream stream,
            List<Class<?>> commandClasses) {

        stream.println("name: WorldEdit");
        stream.println("main: com.sk89q.worldedit.bukkit.WorldEditPlugin");
        stream.println("version: ${project.version}");
        stream.println("commands:");
        
        for (Class<?> cls : commandClasses) {
            for (Method method : cls.getMethods()) {
                if (!method.isAnnotationPresent(Command.class)) {
                    continue;
                }

                Command cmd = method.getAnnotation(Command.class);

                stream.println("    " + cmd.aliases()[0] + ":");
                stream.println("        description: " + cmd.desc());
                stream.println("        usage: /<command>"
                        + (cmd.flags().length() > 0 ? " [-" + cmd.flags() + "]" : "")
                        + (cmd.usage().length() > 0 ? " " + cmd.usage() : ""));
                if (cmd.aliases().length > 1) {
                    stream.println("        aliases: ["
                            + StringUtil.joinQuotedString(cmd.aliases(), ", ", 1, "'")
                            + "]");
                }
            }
        }

        stream.println();
        stream.println();
        stream.println("# Permissions aren't here. Read http://wiki.sk89q.com/wiki/WEPIF/DinnerPerms");
        stream.println("# for how WorldEdit permissions actually work.");
    }
}
