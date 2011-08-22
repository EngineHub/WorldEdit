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
import java.util.logging.Logger;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.data.ChunkStore;
import com.sk89q.worldedit.regions.CuboidRegionSelector;
import com.sk89q.worldedit.regions.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.blocks.*;

/**
 * Selection commands.
 * 
 * @author sk89q
 */
public class SelectionCommands {
    @Command(
        aliases = {"/pos1"},
        usage = "[coordinates]",
        desc = "Set position 1",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.selection.pos"})
    public static void pos1(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Vector pos;

        if (args.argsLength() == 1) {
            if (args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
                String[] coords = args.getString(0).split(",");
                pos = new Vector(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
            } else {
                player.printError("Invalid coordinates " + args.getString(0));
                return;
            }
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos)) {
            player.printError("Position already set.");
            return;
        }

        session.getRegionSelector(player.getWorld())
                .explainPrimarySelection(player, session, pos);
    }

    @Command(
        aliases = {"/pos2"},
        usage = "[coordinates]",
        desc = "Set position 2",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.selection.pos"})
    public static void pos2(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Vector pos;
        if(args.argsLength() == 1) {
            if(args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
                String[] coords = args.getString(0).split(",");
                pos = new Vector(Integer.parseInt(coords[0]), 
                        Integer.parseInt(coords[1]), 
                        Integer.parseInt(coords[2]));
            } else {
                player.printError("Invalid coordinates " + args.getString(0));
                return;
            }
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos)) {
            player.printError("Position already set.");
            return;
        }


        session.getRegionSelector(player.getWorld())
                .explainSecondarySelection(player, session, pos);
    }

    @Command(
        aliases = {"/hpos1"},
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
            if (!session.getRegionSelector(player.getWorld())
                    .selectPrimary(pos)) {
                player.printError("Position already set.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainPrimarySelection(player, session, pos);
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        aliases = {"/hpos2"},
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
            if (!session.getRegionSelector(player.getWorld())
                    .selectSecondary(pos)) {
                player.printError("Position already set.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainSecondarySelection(player, session, pos);
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        aliases = {"/chunk"},
        usage = "",
        flags = "s",
        desc = "Set the selection to your current chunk. The -s flag extends your current selection to the encompassed chunks.",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.chunk"})
    public static void chunk(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        final Vector min;
        final Vector max;
        if (args.hasFlag('s')) {
            Region region = session.getSelection(player.getWorld());

            final Vector2D min2D = ChunkStore.toChunk(region.getMinimumPoint());
            final Vector2D max2D = ChunkStore.toChunk(region.getMaximumPoint());

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = new Vector(max2D.getBlockX() * 16 + 15, 127, max2D.getBlockZ() * 16 + 15);

            player.print("Chunks selected: ("
                    + min2D.getBlockX() + ", " + min2D.getBlockZ() + ") - ("
                    + max2D.getBlockX() + ", " + max2D.getBlockZ() + ")");
        }
        else {
            final Vector2D min2D = ChunkStore.toChunk(player.getBlockIn());

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = min.add(15, 127, 15);

            player.print("Chunk selected: "
                    + min2D.getBlockX() + ", " + min2D.getBlockZ());
        }

        CuboidRegionSelector selector = new CuboidRegionSelector();
        selector.selectPrimary(min);
        selector.selectSecondary(max);
        session.setRegionSelector(player.getWorld(), selector);
        
        session.dispatchCUISelection(player);

    }
    
    @Command(
        aliases = {"/wand"},
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
        aliases = {"toggleeditwand"},
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
        aliases = {"/expand"},
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
            Region region = session.getSelection(player.getWorld());
            try {
                int oldSize = region.getArea();
                region.expand(new Vector(0, 128, 0));
                region.expand(new Vector(0, -128, 0));
                session.getRegionSelector().learnChanges();
                int newSize = region.getArea();
                session.getRegionSelector().explainRegionAdjust(player, session);
                player.print("Region expanded " + (newSize - oldSize)
                        + " blocks [top-to-bottom].");
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }
            
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

        Region region = session.getSelection(player.getWorld());
        int oldSize = region.getArea();
        region.expand(dir.multiply(change));
        
        if (reverseChange != 0) {
            region.expand(dir.multiply(reverseChange));
        }

        session.getRegionSelector().learnChanges();
        int newSize = region.getArea();
        
        session.getRegionSelector().explainRegionAdjust(player, session);
        
        player.print("Region expanded " + (newSize - oldSize) + " blocks.");
    }

    @Command(
        aliases = {"/contract"},
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

        try {
            Region region = session.getSelection(player.getWorld());
            int oldSize = region.getArea();
            region.contract(dir.multiply(change));
            if (reverseChange != 0) {
                region.contract(dir.multiply(reverseChange));
            }
            session.getRegionSelector().learnChanges();
            int newSize = region.getArea();
            
            session.getRegionSelector().explainRegionAdjust(player, session);
            
            player.print("Region contracted " + (oldSize - newSize) + " blocks.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = {"/shift"},
        usage = "<amount> [direction]",
        desc = "Shift the selection area",
        min = 1,
        max = 2
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

        try {
            Region region = session.getSelection(player.getWorld());
            region.expand(dir.multiply(change));
            region.contract(dir.multiply(change));
            session.getRegionSelector().learnChanges();
            
            session.getRegionSelector().explainRegionAdjust(player, session);
            
            player.print("Region shifted.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = {"/outset"},
        usage = "<amount>",
        desc = "Outset the selection area",
        flags = "hv",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.selection.outset"})
    public static void outset(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        int change = args.getInteger(0);

        Region region = session.getSelection(player.getWorld());
        
        try {
            if (!args.hasFlag('h')) {
                region.expand((new Vector(0, 1, 0)).multiply(change));
                region.expand((new Vector(0, -1, 0)).multiply(change));
            }
            
            if (!args.hasFlag('v')) {
                region.expand((new Vector(1, 0, 0)).multiply(change));
                region.expand((new Vector(-1, 0, 0)).multiply(change));
                region.expand((new Vector(0, 0, 1)).multiply(change));
                region.expand((new Vector(0, 0, -1)).multiply(change));
            }

            session.getRegionSelector().learnChanges();
            
            session.getRegionSelector().explainRegionAdjust(player, session);
            
            player.print("Region outset.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = {"/inset"},
        usage = "<amount>",
        desc = "Inset the selection area",
        flags = "hv",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.selection.inset"})
    public static void inset(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        int change = args.getInteger(0);

        Region region = session.getSelection(player.getWorld());
        
        if (!args.hasFlag('h')) {
            region.contract((new Vector(0, 1, 0)).multiply(change));
            region.contract((new Vector(0, -1, 0)).multiply(change));
        }
        
        if (!args.hasFlag('v')) {
            region.contract((new Vector(1, 0, 0)).multiply(change));
            region.contract((new Vector(-1, 0, 0)).multiply(change));
            region.contract((new Vector(0, 0, 1)).multiply(change));
            region.contract((new Vector(0, 0, -1)).multiply(change));
        }

        session.getRegionSelector().learnChanges();
        
        session.getRegionSelector().explainRegionAdjust(player, session);
        
        player.print("Region inset.");
    }

    @Command(
        aliases = {"/size"},
        usage = "",
        desc = "Get information about the selection",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.selection.size"})
    public static void size(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Region region = session.getSelection(player.getWorld());
        Vector size = region.getMaximumPoint()
                .subtract(region.getMinimumPoint())
                .add(1, 1, 1);

        player.print("Type: " + session.getRegionSelector().getTypeName());
        
        for (String line : session.getRegionSelector().getInformationLines()) {
            player.print(line);
        }
        
        player.print("Size: " + size);
        player.print("Cuboid distance: " + region.getMaximumPoint().distance(region.getMinimumPoint()));
        player.print("# of blocks: " + region.getArea());
    }

    @Command(
        aliases = {"/count"},
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
                editSession.countBlocks(session.getSelection(player.getWorld()), searchIDs));
    }

    @Command(
        aliases = {"/distr"},
        usage = "",
        desc = "Get the distribution of blocks in the selection",
        flags = "c",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.analysis.distr"})
    public static void distr(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        List<Countable<Integer>> distribution =
            editSession.getBlockDistribution(session.getSelection(player.getWorld()));
        
        Logger logger = Logger.getLogger("Minecraft.WorldEdit");
        
        if (distribution.size() > 0) { // *Should* always be true
            int size = session.getSelection(player.getWorld()).getArea();
    
            player.print("# total blocks: " + size);
            
            if (args.hasFlag('c')) {
                logger.info("Block distribution (req. by " + player.getName() + "):");
                logger.info("# total blocks: " + size);
            }
            
            for (Countable<Integer> c : distribution) {
                String str = String.format("%-7s (%.3f%%) %s #%d",
                        String.valueOf(c.getAmount()),
                        c.getAmount() / (double)size * 100,
                        BlockType.fromID(c.getID()).getName(), c.getID());
                player.print(str);
                
                if (args.hasFlag('c')) {
                    logger.info(str);
                }
            }
        } else {
            player.printError("No blocks counted.");
        }
    }

    @Command(
        aliases = {"/sel", ","},
        usage = "[type]",
        desc = "Choose a region selector",
        min = 1,
        max = 1
    )
    public static void select(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        String typeName = args.getString(0);
        if (typeName.equalsIgnoreCase("cuboid")) {
            session.setRegionSelector(player.getWorld(), new CuboidRegionSelector());
            session.dispatchCUISelection(player);
            player.print("Cuboid: left click for point 1, right click for point 2");
        } else if (typeName.equalsIgnoreCase("poly")) {
            session.setRegionSelector(player.getWorld(), new Polygonal2DRegionSelector());
            session.dispatchCUISelection(player);
            player.print("2D polygon selector: Left/right click to add a point.");
        } else {
            player.printError("Only 'cuboid' and 'poly' are accepted.");
        }
    }
}
