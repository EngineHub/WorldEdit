// $Id$
/*
 * WorldEdit
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

package com.sk89q.worldedit.commands;

import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.superpickaxe.*;

/**
 * Super pickaxe commands.
 * 
 * @author sk89q
 */
public class SuperPickaxeCommands {
    @Command(
        aliases = {"/", ","},
        usage = "",
        desc = "Toggle the super pickaxe pickaxe function",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.pickaxe"})
    public static void togglePickaxe(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        if (session.toggleSuperPickAxe()) {
            player.print("Super pick axe enabled.");
        } else {
            player.print("Super pick axe disabled.");
        }
    }
    
    @Command(
        aliases = {"single"},
        usage = "",
        desc = "Enable the single block super pickaxe mode",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.pickaxe"})
    public static void single(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setLeftClickMode(new SinglePickaxe());
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }
    
    @Command(
        aliases = {"area"},
        usage = "<radius>",
        desc = "Enable the area super pickaxe pickaxe mode",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.superpickaxe.pickaxe.area"})
    public static void area(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        int range = args.getInteger(0);
        
        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }
        
        session.setLeftClickMode(new AreaPickaxe(range));
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }
    
    @Command(
        aliases = {"recur"},
        usage = "<radius>",
        desc = "Enable the recursive super pickaxe pickaxe mode",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.superpickaxe.pickaxe.recursive"})
    public static void recursive(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        int range = args.getInteger(0);
        
        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }
        
        session.setLeftClickMode(new RecursivePickaxe(range));
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }

    @Command(
        aliases = {"none"},
        usage = "",
        desc = "Turn off all superpickaxe alternate modes",
        min = 0,
        max = 0
    )
    public static void none(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setArmSwingMode(null);
        session.setRightClickMode(null);
        player.print("Now no longer equipping a tool.");
    }

    @Command(
        aliases = {"info"},
        usage = "",
        desc = "Block information tool",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.info"})
    public static void info(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setArmSwingMode(null);
        session.setRightClickMode(new QueryTool());
        player.print("Info tool equipped. Right click with a pickaxe.");
    }

    @Command(
        aliases = {"tree"},
        usage = "",
        desc = "Tree generator tool",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.tree"})
    public static void tree(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setArmSwingMode(null);
        session.setRightClickMode(new TreePlanter());
        player.print("Tree tool equipped. Right click grass with a pickaxe.");
    }

    @Command(
        aliases = {"bigtree"},
        usage = "",
        desc = "Big tree generator tool",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.tree.big"})
    public static void bigTree(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setArmSwingMode(null);
        session.setRightClickMode(new BigTreePlanter());
        player.print("Big tree tool equipped. Right click grass with a pickaxe.");
    }

    @Command(
        aliases = {"pinetree"},
        usage = "",
        desc = "Pine tree generator tool",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.tree.pine"})
    public static void pineTree(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setArmSwingMode(null);
        session.setRightClickMode(new PineTreePlanter());
        player.print("Pine tree tool equipped. Right click a block with a pickaxe.");
    }

    @Command(
        aliases = {"repl"},
        usage = "<block>",
        desc = "Block replacer tool",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.superpickaxe.replacer"})
    public static void repl(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        session.setArmSwingMode(null);
        session.setRightClickMode(new BlockReplacer(targetBlock));
        player.print("Block replacer tool equipped. Right click with a pickaxe.");
    }

    @Command(
        aliases = {"cycler"},
        usage = "",
        desc = "Block data cycler tool",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.data-cycler"})
    public static void cycler(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setArmSwingMode(null);
        session.setRightClickMode(new BlockDataCyler());
        player.print("Block cycler tool equipped. Right click with a pickaxe.");
    }

    @Command(
        aliases = {"brush"},
        usage = "<block> [radius] [no-replace?]",
        desc = "Build spheres from far away",
        min = 1,
        max = 3
    )
    @CommandPermissions({"worldedit.superpickaxe.drawing.brush"})
    public static void brush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        int radius = args.argsLength() > 1 ? args.getInteger(1) : 2;
        boolean nonReplacing = args.argsLength() > 2
                ? (args.getString(2).equalsIgnoreCase("true")
                        || args.getString(2).equalsIgnoreCase("yes")) : false;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }
        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        session.setRightClickMode(null);
        session.setArmSwingMode(new SphereBrush(targetBlock, radius, nonReplacing));
        if (nonReplacing) {
            player.print("Non-replacing sphere brush tool equipped.");
        } else {
            player.print("Sphere brush tool equipped. Swing with a pickaxe.");
        }
    }
    
    @Command(
        aliases = {"rbrush"},
        usage = "<block> [radius] ",
        desc = "Brush tool that will only replace blocks",
        min = 1,
        max = 2
    )
    @CommandPermissions({"worldedit.superpickaxe.drawing.brush"})
    public static void rbrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        int radius = args.argsLength() > 1 ? args.getInteger(1) : 2;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }
        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        session.setRightClickMode(null);
        session.setArmSwingMode(new ReplacingSphereBrush(targetBlock, radius));
        player.print("Replacing sphere brush tool equipped. Swing with a pickaxe.");
    }
}
