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

import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.superpickaxe.brushes.ClipboardBrushShape;
import com.sk89q.worldedit.superpickaxe.brushes.CylinderBrushShape;
import com.sk89q.worldedit.superpickaxe.brushes.HollowCylinderBrushShape;
import com.sk89q.worldedit.superpickaxe.brushes.SphereBrushShape;
import com.sk89q.worldedit.superpickaxe.brushes.HollowSphereBrushShape;

/**
 * Brush shape commands.
 *
 * @author sk89q
 */
public class BrushShapeCommands {
    @Command(
        aliases = {"/sb", "/sphereb"},
        usage = "<block> [radius]",
        flags = "h",
        desc = "Choose the sphere brush",
        min = 1,
        max = 2
    )
    @CommandPermissions({"worldedit.superpickaxe.drawing.brush.sphere"})
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
        
        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        
        if (args.hasFlag('h')) {
            session.setBrushShape(new HollowSphereBrushShape(targetBlock, radius));
        } else {
            session.setBrushShape(new SphereBrushShape(targetBlock, radius));
        }
        
        player.print("Sphere brush shape equipped.");
    }

    @Command(
        aliases = {"/cb", "/cylb"},
        usage = "<block> [radius] [height]",
        flags = "h",
        desc = "Choose the cylinder brush",
        min = 1,
        max = 2
    )
    @CommandPermissions({"worldedit.superpickaxe.drawing.brush.cylinder"})
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

        int height = args.argsLength() > 1 ? args.getInteger(1) : 1;
        if (height > config.maxBrushRadius) {
            player.printError("Maximum allowed brush radius/height: "
                    + config.maxBrushRadius);
            return;
        }
        
        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        
        if (args.hasFlag('h')) {
            session.setBrushShape(new HollowCylinderBrushShape(targetBlock, radius, height));
        } else {
            session.setBrushShape(new CylinderBrushShape(targetBlock, radius, height));
        }
        
        player.print("Cylinder brush shape equipped.");
    }

    @Command(
        aliases = {"/cbb", "/copyb"},
        usage = "",
        flags = "a",
        desc = "Choose the clipboard brush",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe.drawing.brush.clipboard"})
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
        
        session.setBrushShape(new ClipboardBrushShape(clipboard, args.hasFlag('a')));
        
        player.print("Clipboard brush shape equipped.");
    }
}
