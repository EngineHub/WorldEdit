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


import java.util.Set;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.filtering.GaussianKernel;
import com.sk89q.worldedit.filtering.HeightMapFilter;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.*;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;

/**
 * Region related commands.
 * 
 * @author sk89q
 */
public class RegionCommands {
    @Command(
        aliases = {"/set"},
        usage = "<block>",
        desc = "Set all the blocks inside the selection to a block",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.set"})
    @Logging(REGION)
    public static void set(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        
        int affected;
        
        if (pattern instanceof SingleBlockPattern) {
            affected = editSession.setBlocks(session.getSelection(player.getWorld()),
                    ((SingleBlockPattern)pattern).getBlock());
        } else {
            affected = editSession.setBlocks(session.getSelection(player.getWorld()), pattern);
        }
        
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = {"/replace"},
        usage = "[from-block] <to-block>",
        desc = "Replace all blocks in the selection with another",
        min = 1,
        max = 2
    )
    @CommandPermissions({"worldedit.region.replace"})
    @Logging(REGION)
    public static void replace(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Set<BaseBlock> from;
        Pattern to;
        if (args.argsLength() == 1) {
            from = null;
            to = we.getBlockPattern(player, args.getString(0));
        } else {
            from = we.getBlocks(player, args.getString(0), true);
            to = we.getBlockPattern(player, args.getString(1));
        }

        int affected = 0;
        if (to instanceof SingleBlockPattern) {
            affected = editSession.replaceBlocks(session.getSelection(player.getWorld()), from,
                    ((SingleBlockPattern)to).getBlock());
        } else {
            affected = editSession.replaceBlocks(session.getSelection(player.getWorld()), from, to);
        }
        
        player.print(affected + " block(s) have been replaced.");
    }
    
    @Command(
        aliases = {"/overlay"},
        usage = "<block>",
        desc = "Set a block on top of blocks in the region",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.overlay"})
    @Logging(REGION)
    public static void overlay(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern pat = we.getBlockPattern(player, args.getString(0));

        Region region = session.getSelection(player.getWorld());
        int affected = 0;
        if (pat instanceof SingleBlockPattern) {
            affected = editSession.overlayCuboidBlocks(region,
                    ((SingleBlockPattern)pat).getBlock());
        } else {
            affected = editSession.overlayCuboidBlocks(region, pat);
        }
        player.print(affected + " block(s) have been overlayed.");
    }

    @Command(
        aliases = {"/naturalize"},
        usage = "",
        desc = "3 layers of dirt on top then rock below",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.region.naturalize"})
    @Logging(REGION)
    public static void naturalize(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Region region = session.getSelection(player.getWorld());
        int affected = editSession.naturalizeCuboidBlocks(region);
        player.print(affected + " block(s) have been naturalized.");
    }

    @Command(
        aliases = {"/walls"},
        usage = "<block>",
        desc = "Build the four sides of the selection",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.walls"})
    @Logging(REGION)
    public static void walls(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        int affected;
        if (pattern instanceof SingleBlockPattern) {
            affected = editSession.makeCuboidWalls(session.getSelection(player.getWorld()), ((SingleBlockPattern) pattern).getBlock());
        } else {
            affected = editSession.makeCuboidWalls(session.getSelection(player.getWorld()), pattern);
        }
        
        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = {"/faces", "/outline"},
        usage = "<block>",
        desc = "Build the walls, ceiling, and floor of a selection",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.region.faces"})
    @Logging(REGION)
    public static void faces(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        int affected;
        if (pattern instanceof SingleBlockPattern) {
            affected = editSession.makeCuboidFaces(session.getSelection(player.getWorld()), ((SingleBlockPattern) pattern).getBlock());
        } else {
            affected = editSession.makeCuboidFaces(session.getSelection(player.getWorld()), pattern);
        }

        player.print(affected + " block(s) have been changed.");
    }

    @Command(
        aliases = {"/smooth"},
        usage = "[iterations]",
        flags = "n",
        desc = "Smooth the elevation in the selection",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.region.smooth"})
    @Logging(REGION)
    public static void smooth(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        int iterations = 1;
        if (args.argsLength() > 0) {
            iterations = args.getInteger(0);
        }

        HeightMap heightMap = new HeightMap(editSession, session.getSelection(player.getWorld()), args.hasFlag('n'));
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        int affected = heightMap.applyFilter(filter, iterations);
        player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");
    
    }

    @Command(
        aliases = {"/move"},
        usage = "[count] [direction] [leave-id]",
        flags = "s",
        desc = "Move the contents of the selection",
        min = 0,
        max = 3
    )
    @CommandPermissions({"worldedit.region.move"})
    @Logging(ORIENTATION_REGION)
    public static void move(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        Vector dir = we.getDirection(player,
                args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");
        BaseBlock replace;

        // Replacement block argument
        if (args.argsLength() > 2) {
            replace = we.getBlock(player, args.getString(2));
        } else {
            replace = new BaseBlock(BlockID.AIR);
        }

        int affected = editSession.moveCuboidRegion(session.getSelection(player.getWorld()),
                dir, count, true, replace);

        if (args.hasFlag('s')) {
            try {
                Region region = session.getSelection(player.getWorld());
                region.expand(dir.multiply(count));
                region.contract(dir.multiply(count));

                session.getRegionSelector().learnChanges();
                session.getRegionSelector().explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " blocks moved.");
    }
    

    @Command(
        aliases = {"/stack"},
        usage = "[count] [direction]",
        flags = "sa",
        desc = "Repeat the contents of the selection",
        min = 0,
        max = 2
    )
    @CommandPermissions({"worldedit.region.stack"})
    @Logging(ORIENTATION_REGION)
    public static void stack(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        int count = args.argsLength() > 0 ? Math.max(1, args.getInteger(0)) : 1;
        Vector dir = we.getDiagonalDirection(player,
                args.argsLength() > 1 ? args.getString(1).toLowerCase() : "me");

        int affected = editSession.stackCuboidRegion(session.getSelection(player.getWorld()),
                dir, count, !args.hasFlag('a'));

        if (args.hasFlag('s')) {
            try {
                Region region = session.getSelection(player.getWorld());
                region.expand(dir.multiply(count));
                region.contract(dir.multiply(count));

                session.getRegionSelector().learnChanges();
                session.getRegionSelector().explainRegionAdjust(player, session);
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
        }

        player.print(affected + " blocks changed. Undo with //undo");
    }

    @Command(
        aliases = {"/regen"},
        usage = "",
        desc = "Regenerates the contents of the selection",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.regen"})
    @Logging(REGION)
    public static void regenerateChunk(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Region region = session.getSelection(player.getWorld());
        Mask mask = session.getMask();
        session.setMask(null);
        player.getWorld().regenerate(region, editSession);
        session.setMask(mask);
        player.print("Region regenerated.");
    }
}
