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
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.tools.Brush;
import com.sk89q.worldedit.tools.ReplacingBrush;

public class BrushModeCommands {
    @Command(
        aliases = {"normal", "s"},
        usage = "",
        desc = "Normal brush mode",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.brush.mode.normal"})
    public static void normal(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        session.setRightClickMode(null);
        session.setArmSwingMode(new Brush(true));
        player.print("Normal brush mode set.");
    }
    
    @Command(
        aliases = {"replace", "r"},
        usage = "",
        desc = "Replace existing blocks only",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.brush.mode.replace"})
    public static void replace(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        session.setRightClickMode(null);
        session.setArmSwingMode(new ReplacingBrush());
        player.print("Replacing brush mode equipped.");
    }
}
