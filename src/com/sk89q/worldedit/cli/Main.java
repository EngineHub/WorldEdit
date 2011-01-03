// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.cli;

import static java.util.Arrays.*;
import java.util.List;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * 
 * @author sk89q
 */
public class Main {
    /**
     * @param args
     */
    public static void main(String[] _args) throws Throwable {
        OptionParser parser = new OptionParser();
        OptionSpec<String> world =
                parser.accepts("world").withRequiredArg().ofType(String.class)
                .describedAs("world directory").defaultsTo("world");
        parser.acceptsAll(asList("?", "h"), "show help");

        OptionSet options = parser.parse(_args);
        List<String> args = options.nonOptionArguments();
        
        if (args.size() != 1 || options.has("?")) {
            System.err.println("worldedit <action>");
            System.err.println();
            parser.printHelpOn(System.err);
            return;
        }

        System.err.println("WorldEdit v" + getVersion());
        System.err.println("Copyright (c) 2010-2011 sk89q <http://www.sk89q.com>");
        System.err.println();
        
        String act = args.get(0);
        String worldPath = options.valueOf(world);
        
        if (act.equalsIgnoreCase("check")) {
            new WorldChecker(worldPath);
        } else {
            System.err.println("Only valid action is 'check'.");
        }
    }

    /**
     * Get the version.
     * 
     * @return
     */
    public static String getVersion() {
        Package p = com.sk89q.worldedit.cli.Main.class.getPackage();

        if (p == null) {
            p = Package.getPackage("com.sk89q.worldedit");
        }
        
        String version;

        if (p == null) {
            return "(unknown)";
        } else {
            version = p.getImplementationVersion();

            if (version == null) {
                return "(unknown)";
            }
        }

        return version;
    }

}
