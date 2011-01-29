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

import java.util.List;
import java.util.Set;
import com.sk89q.util.commands.Command;
import com.sk89q.util.commands.CommandContext;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.data.ChunkStore;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.blocks.*;

/**
 * Selection commands.
 * 
 * @author sk89q
 */
public class SelectionCommands {
    @Command(
        aliases = {"//pos1"},
        usage = "",
        desc = "Set position 1",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.pos"})
    public static void pos1(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        session.setPos1(player.getBlockIn());
        
        if (session.isRegionDefined()) {
            player.print("First position set to " + player.getBlockIn()
                    + " (" + session.getRegion().getSize() + ").");
        } else {
            player.print("First position set to " + player.getBlockIn() + ".");
        }
    }

    @Command(
        aliases = {"//pos2"},
        usage = "",
        desc = "Set position 2",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.pos"})
    public static void pos2(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        session.setPos2(player.getBlockIn());
        
        if (session.isRegionDefined()) {
            player.print("Second position set to " + player.getBlockIn()
                    + " (" + session.getRegion().getSize() + ").");
        } else {
            player.print("Second position set to " + player.getBlockIn() + ".");
        }
    }

    @Command(
        aliases = {"//hpos1"},
        usage = "",
        desc = "Set position 1 to targeted block",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.hpos"})
    public static void hpos1(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Vector pos = player.getBlockTrace(300);
        
        if (pos != null) {
            session.setPos1(pos);
            if (session.isRegionDefined()) {
                player.print("First position set to " + pos
                        + " (" + session.getRegion().getSize() + ").");
            } else {
                player.print("First position set to " + pos.toString() + " .");
            }
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        aliases = {"//hpos2"},
        usage = "",
        desc = "Set position 2 to targeted block",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.hpos"})
    public static void hpos2(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Vector pos = player.getBlockTrace(300);
        
        if (pos != null) {
            session.setPos2(pos);
            
            if (session.isRegionDefined()) {
                player.print("Second position set to " + pos
                        + " (" + session.getRegion().getSize() + ").");
            } else {
                player.print("Second position set to " + pos.toString() + " .");
            }
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        aliases = {"//chunk"},
        usage = "",
        desc = "Set the selection to your current chunk",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.chunk"})
    public static void chunk(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Vector2D min2D = ChunkStore.toChunk(player.getBlockIn());
        Vector min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
        Vector max = min.add(15, 127, 15);

        session.setPos1(min);
        session.setPos2(max);

        player.print("Chunk selected: "
                + min2D.getBlockX() + ", " + min2D.getBlockZ());
    }
    
    @Command(
        aliases = {"//wand"},
        usage = "",
        desc = "Get the wand object",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.wand"})
    public static void wand(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        player.giveItem(we.getConfiguration().wandItem, 1);
        player.print("Left click: select pos #1; Right click: select pos #2");
    }
    
    @Command(
        aliases = {"/toggleeditwand"},
        usage = "",
        desc = "Toggle functionality of the edit wand",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.wand.toggle"})
    public static void toggleWand(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        session.setToolControl(!session.isToolControlEnabled());
        
        if (session.isToolControlEnabled()) {
            player.print("Edit wand enabled.");
        } else {
            player.print("Edit wand disabled.");
        }
    }

    @Command(
        aliases = {"//expand"},
        usage = "<amount> [reverse-amount] <direction>",
        desc = "Expand the selection area",
        min = 1,
        max = 3
    )
    @CommandPermissions({"worldedit.selection.expand"})
    public static void expand(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Vector dir;

        // Special syntax (//expand vert) to expand the selection between
        // sky and bedrock.
        if (args.getString(0).equalsIgnoreCase("vert")
                || args.getString(0).equalsIgnoreCase("vertical")) {
            Region region = session.getRegion();
            int oldSize = region.getSize();
            region.expand(new Vector(0, 128, 0));
            region.expand(new Vector(0, -128, 0));
            session.learnRegionChanges();
            int newSize = region.getSize();
            player.print("Region expanded " + (newSize - oldSize)
                    + " blocks [top-to-bottom].");
            
            return;
        }

        int change = args.getInteger(0);
        int reverseChange = 0;
        
        // Specifying a direction
        if (args.argsLength() == 2) {
            try {
                reverseChange = args.getInteger(1) * -1;
                dir = we.getDirection(player, "me");
            } catch (NumberFormatException e) {
                dir = we.getDirection(player,
                        args.getString(1).toLowerCase());
            }
        // Specifying a direction and a reverse amount
        } else if (args.argsLength() == 3) {
            reverseChange = args.getInteger(1) * -1;
            dir = we.getDirection(player,
                    args.getString(2).toLowerCase());
        } else {
            dir = we.getDirection(player, "me");
        }

        Region region = session.getRegion();
        int oldSize = region.getSize();
        region.expand(dir.multiply(change));
        
        if (reverseChange != 0) {
            region.expand(dir.multiply(reverseChange));
        }
        
        session.learnRegionChanges();
        int newSize = region.getSize();
        
        player.print("Region expanded " + (newSize - oldSize) + " blocks.");
    }

    @Command(
        aliases = {"//contract"},
        usage = "<amount> [reverse-amount] [direction]",
        desc = "Contract the selection area",
        min = 1,
        max = 3
    )
    @CommandPermissions({"worldedit.selection.contract"})
    public static void contract(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Vector dir;
        int change = args.getInteger(0);
        int reverseChange = 0;

        // Either a reverse amount or a direction
        if (args.argsLength() == 2) {
            try {
                reverseChange = args.getInteger(1) * -1;
                dir = we.getDirection(player, "me");
            } catch (NumberFormatException e) {
                dir = we.getDirection(player,
                        args.getString(1).toLowerCase());
            }
        // Both reverse amount and direction
        } else if (args.argsLength() == 3) {
            reverseChange = args.getInteger(1) * -1;
            dir = we.getDirection(player,
                    args.getString(2).toLowerCase());
        } else {
            dir = we.getDirection(player, "me");
        }

        Region region = session.getRegion();
        int oldSize = region.getSize();
        region.contract(dir.multiply(change));
        if (reverseChange != 0) {
            region.contract(dir.multiply(reverseChange));
        }
        session.learnRegionChanges();
        int newSize = region.getSize();
        
        player.print("Region contracted " + (oldSize - newSize) + " blocks.");
    }

    @Command(
        aliases = {"//shift"},
        usage = "<amount> [direction]",
        desc = "Shift the selection area",
        min = 1,
        max = 3
    )
    @CommandPermissions({"worldedit.selection.shift"})
    public static void shift(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        Vector dir;
        
        int change = args.getInteger(0);
        if (args.argsLength() == 2) {
            dir = we.getDirection(player,
                    args.getString(1).toLowerCase());
        } else {
            dir = we.getDirection(player, "me");
        }

        Region region = session.getRegion();
        region.expand(dir.multiply(change));
        region.contract(dir.multiply(change));
        session.learnRegionChanges();
        
        player.print("Region shifted.");
    }

    @Command(
        aliases = {"//size"},
        usage = "",
        desc = "Get information about the selection",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.size"})
    public static void size(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Region region = session.getRegion();
        Vector size = region.getMaximumPoint()
                .subtract(region.getMinimumPoint())
                .add(1, 1, 1);
        
        player.print("First position: " + session.getPos1());
        player.print("Second position: " + session.getPos2());
        player.print("Size: " + size);
        player.print("# of blocks: " + region.getSize());
    }

    @Command(
        aliases = {"//count"},
        usage = "<block>",
        desc = "Counts the number of a certain type of block",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.analysis.count"})
    public static void count(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        Set<Integer> searchIDs = we.getBlockIDs(player,
                args.getString(0), true);
        player.print("Counted: " +
                editSession.countBlocks(session.getRegion(), searchIDs));
    }

    @Command(
        aliases = {"//distr"},
        usage = "",
        desc = "Get the distribution of blocks in the selection",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.analysis.distr"})
    public static void distr(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        List<Countable<Integer>> distribution =
            editSession.getBlockDistribution(session.getRegion());
        
        if (distribution.size() > 0) { // *Should* always be true
            int size = session.getRegion().getSize();
    
            player.print("# total blocks: " + size);
            
            for (Countable<Integer> c : distribution) {
                player.print(String.format("%-7s (%.3f%%) %s #%d",
                        String.valueOf(c.getAmount()),
                        c.getAmount() / (double)size * 100,
                        BlockType.fromID(c.getID()).getName(), c.getID()));
            }
        } else {
            player.printError("No blocks counted.");
        }
    }
}
