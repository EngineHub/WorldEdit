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

package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.masks.BlockTypeMask;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.tools.BrushTool;
import com.sk89q.worldedit.tools.brushes.ClipboardBrush;
import com.sk89q.worldedit.tools.brushes.CylinderBrush;
import com.sk89q.worldedit.tools.brushes.SmoothBrush;
import com.sk89q.worldedit.tools.brushes.SphereBrush;
import com.sk89q.worldedit.tools.enums.ToolFlag;

/**
 * Brush shape commands.
 *
 * @author sk89q
 */
public class BrushCommands {
    @Command(
        aliases = {"sphere", "s"},
        usage = "[pattern] [radius]",
        flags = "h",
        desc = "Choose the sphere brush",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.brush.sphere"})
    public static void sphereBrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        double radius = 2;

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        
        SphereBrush brush = new SphereBrush();
        
        if (args.argsLength() > 0) {
            Pattern pattern = we.getBlockPattern(player, args.getString(0));
            brush.pattern().set(pattern);
            if(args.argsLength() > 1) {
                radius =  args.getDouble(1);
            }
        }
        
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }
                
        if (args.hasFlag('h')) {
            brush.flags().add(ToolFlag.HOLLOW);
        }
        
        tool.setBrush(brush, "worldedit.brush.sphere");
        
        player.print(String.format("Sphere brush shape equipped (%.0f).",
                radius));
    }

    @Command(
        aliases = {"cylinder", "cyl", "c"},
        usage = "[block] [radius] [height]",
        flags = "h",
        desc = "Choose the cylinder brush",
        min = 0,
        max = 3
    )
    @CommandPermissions({"worldedit.brush.cylinder"})
    public static void cylinderBrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        long radius = 2;
        long height = 1;
        
        BrushTool tool = session.getBrushTool(player.getItemInHand());
        CylinderBrush brush = new CylinderBrush();
                
        if(args.argsLength() > 0) {
            Pattern pattern = we.getBlockPattern(player, args.getString(0));
            brush.pattern().set(pattern);
            if(args.argsLength() > 1) {
                radius =  args.getLong(1);
                if(args.argsLength() > 2) {
                    height = args.getInteger(2);
                }
            }
        }
        
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }
        
        if (height > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius/height: "
                    + config.maxBrushRadius);
            return;
        }
        
        brush.size().setY(height);
        brush.size().setX(radius);

        if (args.hasFlag('h')) {
            brush.flags().add(ToolFlag.HOLLOW);
        }
        
        tool.setBrush(brush, "worldedit.brush.cylinder");
        
        player.print(String.format("Cylinder brush shape equipped (%.0f by %d).",
                radius, height));
    }

    @Command(
        aliases = {"clipboard", "copy"},
        usage = "",
        flags = "a",
        desc = "Choose the clipboard brush",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.brush.clipboard"})
    public static void clipboardBrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();
        
        CuboidClipboard clipboard = session.getClipboard();
        
        if (clipboard == null) {
            player.printError("Copy something first.");
            return;
        }
        
        Vector size = clipboard.getSize();

        if (size.getBlockX() > config.maxBrushRadius
                || size.getBlockY() > config.maxBrushRadius
                || size.getBlockZ() > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius/height: "
                    + config.maxBrushRadius);
            return;
        }

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        tool.setBrush(new ClipboardBrush(clipboard, args.hasFlag('a')), "worldedit.brush.clipboard");
        
        player.print("Clipboard brush shape equipped.");
    }

    @Command(
        aliases = {"smooth"},
        usage = "[size] [iterations]",
        desc = "Choose the terrain softener brush",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.brush.smooth"})
    public static void smoothBrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        long radius = 2;
        int iterations = 4;
        
        if (args.argsLength() > 0) {
            radius = args.getLong(0);
            if(args.argsLength() > 1) {
                iterations = args.getInteger(1);
            }
        }
        
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        SmoothBrush brush = new SmoothBrush();

        brush.iterations().set(iterations);
        brush.size().setX(radius);
        
        tool.setBrush(brush, "worldedit.brush.smooth");

        player.print(String.format("Smooth brush equipped (%.0f x %dx).",
                radius, iterations));
    }
    
    @Command(
        aliases = {"ex", "extinguish"},
        usage = "[radius]",
        desc = "Shortcut fire extinguisher brush",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.brush.ex"})
    public static void extinguishBrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        double radius = 2;

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        
        SphereBrush brush = new SphereBrush();
        
        if(args.argsLength() > 1) {
            radius =  args.getDouble(1);
        }
        
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }
        
        brush.pattern().set(new SingleBlockPattern(new BaseBlock(0)));
        tool.setMask(new BlockTypeMask(BlockID.FIRE));
        
        tool.setBrush(brush, "worldedit.brush.ex");
        
        player.print(String.format("Extinguisher equipped (%.0f).",
                radius));
    }
}
