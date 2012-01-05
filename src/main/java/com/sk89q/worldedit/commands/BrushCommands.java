// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
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
import com.sk89q.worldedit.tools.brushes.*;

/**
 * Brush shape commands.
 *
 * @author sk89q
 */
public class BrushCommands {
	private final WorldEdit we;
	
	public BrushCommands(WorldEdit we) {
		this.we = we;
	}

    @Command(
        aliases = { "sphere", "s" },
        usage = "<block> [radius]",
        flags = "h",
        desc = "Choose the sphere brush",
        help =
            "Chooses the sphere brush.\n" +
            "The -h flag creates hollow spheres instead.",
        min = 1,
        max = 2
    )
    @CommandPermissions("worldedit.brush.sphere")
    public void sphereBrush(CommandContext args, LocalSession session,
            LocalPlayer player, EditSession editSession) throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        double radius = args.argsLength() > 1 ? args.getDouble(1) : 2;
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
            tool.setBrush(new HollowSphereBrush(), "worldedit.brush.sphere");
        } else {
            tool.setBrush(new SphereBrush(), "worldedit.brush.sphere");
        }

        player.print(String.format("Sphere brush shape equipped (%.0f).",
                radius));
    }

    @Command(
        aliases = { "cylinder", "cyl", "c" },
        usage = "<block> [radius] [height]",
        flags = "h",
        desc = "Choose the cylinder brush",
        help =
            "Chooses the cylinder brush.\n" +
            "The -h flag creates hollow cylinders instead.",
        min = 1,
        max = 3
    )
    @CommandPermissions("worldedit.brush.cylinder")
    public void cylinderBrush(CommandContext args, LocalSession session,
            LocalPlayer player, EditSession editSession) throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        double radius = args.argsLength() > 1 ? args.getDouble(1) : 2;
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
            tool.setBrush(new HollowCylinderBrush(height), "worldedit.brush.cylinder");
        } else {
            tool.setBrush(new CylinderBrush(height), "worldedit.brush.cylinder");
        }

        player.print(String.format("Cylinder brush shape equipped (%.0f by %d).",
                radius, height));
    }

    @Command(
        aliases = { "clipboard", "copy" },
        usage = "",
        flags = "a",
        desc = "Choose the clipboard brush",
        help =
            "Chooses the clipboard brush.\n" +
            "The -a flag makes it not paste air.",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.brush.clipboard")
    public void clipboardBrush(CommandContext args, LocalSession session,
            LocalPlayer player, EditSession editSession) throws WorldEditException {
        
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
        aliases = { "smooth" },
        usage = "[size] [iterations]",
        flags = "n",
        desc = "Choose the terrain softener brush",
        help =
            "Chooses the terrain softener brush.\n" +
            "The -n flag makes it only consider naturally occuring blocks.",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.brush.smooth")
    public void smoothBrush(CommandContext args, LocalSession session,
            LocalPlayer player, EditSession editSession) throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        double radius = args.argsLength() > 0 ? args.getDouble(0) : 2;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }

        int iterations = args.argsLength() > 1 ? args.getInteger(1) : 4;

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        tool.setSize(radius);
        tool.setBrush(new SmoothBrush(iterations, args.hasFlag('n')), "worldedit.brush.smooth");

        player.print(String.format("Smooth brush equipped (%.0f x %dx, using " + (args.hasFlag('n') ? "natural blocks only" : "any block") + ").",
                radius, iterations));
    }

    @Command(
        aliases = { "ex", "extinguish" },
        usage = "[radius]",
        desc = "Shortcut fire extinguisher brush",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.brush.ex")
    public void extinguishBrush(CommandContext args, LocalSession session,
            LocalPlayer player, EditSession editSession) throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        double radius = args.argsLength() > 1 ? args.getDouble(1) : 5;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        Pattern fill = new SingleBlockPattern(new BaseBlock(0));
        tool.setFill(fill);
        tool.setSize(radius);
        tool.setMask(new BlockTypeMask(BlockID.FIRE));
        tool.setBrush(new SphereBrush(), "worldedit.brush.ex");

        player.print(String.format("Extinguisher equipped (%.0f).",
                radius));
    }

    @Command(
            aliases = { "gravity", "grav" },
            usage = "[radius]",
            flags = "h",
            desc = "Gravity brush",
            help =
                "This brush simulates the affect of gravity.\n" +
                "The -h flag makes it affect blocks starting at the world's max y, " +
                    "instead of the clicked block's y + radius.",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.brush.gravity")
    public void gravityBrush(CommandContext args, LocalSession session,
                                LocalPlayer player, EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        double radius = args.argsLength() > 0 ? args.getDouble(0) : 5;
        if (radius > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius: "
                    + config.maxBrushRadius);
            return;
        }

        BrushTool tool = session.getBrushTool(player.getItemInHand());
        tool.setSize(radius);
        tool.setBrush(new GravityBrush(args.hasFlag('h')), "worldedit.brush.gravity");

        player.print(String.format("Gravity brush equipped (%.0f).",
                radius));
    }
}
