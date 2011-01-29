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
import com.sk89q.util.commands.Command;
import com.sk89q.worldedit.commands.CommandPermissions;

public class DocumentationPrinter {
    public static void main(String[] args) {
        File commandsDir = new File(args[0]);
        
        for (File f : commandsDir.listFiles()) {
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

            for (Method method : cls.getMethods()) {
                if (!method.isAnnotationPresent(Command.class)) {
                    continue;
                }

                Command cmd = method.getAnnotation(Command.class);

                System.out.println("|-");
                System.out.print("| " + cmd.aliases()[0]);
                System.out.print(" || ");

                if (method.isAnnotationPresent(CommandPermissions.class)) {
                    CommandPermissions perms =
                        method.getAnnotation(CommandPermissions.class);
                    
                    String[] permKeys = perms.value();
                    for (int i = 0; i < permKeys.length; i++) {
                        if (i > 0) {
                            System.out.print(", ");
                        }
                        System.out.print(permKeys[i]);
                    }
                }

                System.out.println();
            }
        }
    }
}
