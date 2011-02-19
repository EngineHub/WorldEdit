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
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.tools.BrushTool;
import com.sk89q.worldedit.tools.brushes.ClipboardBrush;
import com.sk89q.worldedit.tools.brushes.CylinderBrush;
import com.sk89q.worldedit.tools.brushes.HollowCylinderBrush;
import com.sk89q.worldedit.tools.brushes.HollowSphereBrush;
import com.sk89q.worldedit.tools.brushes.SmoothBrush;
import com.sk89q.worldedit.tools.brushes.SphereBrush;

/**
 * Brush shape commands.
 *
 * @author sk89q
 */
public class BrushCommands {
    @Command(
        aliases = {"sphere", "s"},
        usage = "<block> [radius]",
        flags = "h",
        desc = "Choose the sphere brush",
        min = 1,
        max = 2
    )
    @CommandPermissions({"worldedit.brush.sphere"})
    public static void sphereBrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        int radius = args.argsLength() > 1 ? args.getInteger(1) : 2;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        Pattern fill = we.getBlockPattern(player, args.getString(0));
        tool.setFill(fill);
        tool.setSize(radius);
        
        if (args.hasFlag('h')) {
            tool.setBrush(new HollowSphereBrush());
        } else {
            tool.setBrush(new SphereBrush());
        }

        player.print(String.format("Sphere brush shape equipped (%d).",
                radius));
    }

    @Command(
        aliases = {"cylinder", "cyl", "c"},
        usage = "<block> [radius] [height]",
        flags = "h",
        desc = "Choose the cylinder brush",
        min = 1,
        max = 3
    )
    @CommandPermissions({"worldedit.brush.cylinder"})
    public static void cylinderBrush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        int radius = args.argsLength() > 1 ? args.getInteger(1) : 2;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }

        int height = args.argsLength() > 2 ? args.getInteger(2) : 1;
        if (height > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius/height: "
                    + config.maxBrushRadius);
            return;
        }

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        Pattern fill = we.getBlockPattern(player, args.getString(0));
        tool.setFill(fill);
        tool.setSize(radius);
        
        if (args.hasFlag('h')) {
            tool.setBrush(new HollowCylinderBrush(height));
        } else {
            tool.setBrush(new CylinderBrush(height));
        }
        
        player.print(String.format("Cylinder brush shape equipped (%d by %d).",
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
        tool.setBrush(new ClipboardBrush(clipboard, args.hasFlag('a')));
        
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

        int radius = args.argsLength() > 0 ? args.getInteger(0) : 2;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }

        int iterations = args.argsLength() > 1 ? args.getInteger(1) : 4;

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        tool.setSize(radius);
        tool.setBrush(new SmoothBrush(iterations));

        player.print(String.format("Smooth brush equipped (%d x %dx).",
                radius, iterations));
    }
}
