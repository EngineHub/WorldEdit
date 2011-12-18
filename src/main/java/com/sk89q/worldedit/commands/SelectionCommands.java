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
import com.sk89q.worldedit.regions.ExtendingCuboidRegionSelector;
import com.sk89q.worldedit.regions.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.blocks.*;

/**
 * Selection commands.
 * 
 * @author sk89q
 */
public class SelectionCommands {
    private final WorldEdit we;
    
    public SelectionCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "/pos1" },
        usage = "[coordinates]",
        desc = "Set position 1",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.selection.pos")
    public void pos1(CommandContext args, LocalSession session,  LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

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
        aliases = { "/pos2" },
        usage = "[coordinates]",
        desc = "Set position 2",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.selection.pos")
    public void pos2(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        Vector pos;
        if (args.argsLength() == 1) {
            if (args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
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
        aliases = { "/hpos1" },
        usage = "",
        desc = "Set position 1 to targeted block",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos1(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {
        
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
        aliases = { "/hpos2" },
        usage = "",
        desc = "Set position 2 to targeted block",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos2(CommandContext args, LocalSession session, LocalPlayer player,
                      EditSession editSession) throws WorldEditException {
        
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
        aliases = { "/chunk" },
        usage = "",
        flags = "s",
        desc = "Set the selection to your current chunk.",
        help =
            "Set the selection to the chunk you are currently in.\n" +
            "With the -s flag, your current selection is expanded\n" +
            "to encompass all chunks that are part of it.",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.selection.chunk")
    public void chunk(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final Vector min;
        final Vector max;
        if (args.hasFlag('s')) {
            Region region = session.getSelection(player.getWorld());

            final Vector2D min2D = ChunkStore.toChunk(region.getMinimumPoint());
            final Vector2D max2D = ChunkStore.toChunk(region.getMaximumPoint());

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = new Vector(max2D.getBlockX() * 16 + 15, player.getWorld().getMaxY(), max2D.getBlockZ() * 16 + 15);

            player.print("Chunks selected: ("
                    + min2D.getBlockX() + ", " + min2D.getBlockZ() + ") - ("
                    + max2D.getBlockX() + ", " + max2D.getBlockZ() + ")");
        } else {
            final Vector2D min2D = ChunkStore.toChunk(player.getBlockIn());

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = min.add(15, player.getWorld().getMaxY(), 15);

            player.print("Chunk selected: "
                    + min2D.getBlockX() + ", " + min2D.getBlockZ());
        }

        CuboidRegionSelector selector = new CuboidRegionSelector(player.getWorld());
        selector.selectPrimary(min);
        selector.selectSecondary(max);
        session.setRegionSelector(player.getWorld(), selector);

        session.dispatchCUISelection(player);

    }

    @Command(
        aliases = { "/wand" },
        usage = "",
        desc = "Get the wand object",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.wand")
    public void wand(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        player.giveItem(we.getConfiguration().wandItem, 1);
        player.print("Left click: select pos #1; Right click: select pos #2");
    }

    @Command(
        aliases = { "toggleeditwand" },
        usage = "",
        desc = "Toggle functionality of the edit wand",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.wand.toggle")
    public void toggleWand(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
        session.setToolControl(!session.isToolControlEnabled());

        if (session.isToolControlEnabled()) {
            player.print("Edit wand enabled.");
        } else {
            player.print("Edit wand disabled.");
        }
    }

    @Command(
        aliases = { "/expand" },
        usage = "<amount> [reverse-amount] <direction>",
        desc = "Expand the selection area",
        min = 1,
        max = 3
    )
    @CommandPermissions("worldedit.selection.expand")
    public void expand(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Vector dir;

        // Special syntax (//expand vert) to expand the selection between
        // sky and bedrock.
        if (args.getString(0).equalsIgnoreCase("vert")
                || args.getString(0).equalsIgnoreCase("vertical")) {
            Region region = session.getSelection(player.getWorld());
            try {
                int oldSize = region.getArea();
                region.expand(new Vector(0, (player.getWorld().getMaxY() + 1), 0));
                region.expand(new Vector(0, -(player.getWorld().getMaxY() + 1), 0));
                session.getRegionSelector(player.getWorld()).learnChanges();
                int newSize = region.getArea();
                session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
                player.print("Region expanded " + (newSize - oldSize)
                        + " blocks [top-to-bottom].");
            } catch (RegionOperationException e) {
                player.printError(e.getMessage());
            }

            return;
        }

        int change = args.getInteger(0);
        int reverseChange = 0;

        switch (args.argsLength()) {
        case 2:
            // Either a reverse amount or a direction
            try {
                reverseChange = args.getInteger(1) * -1;
                dir = we.getDirection(player, "me");
            } catch (NumberFormatException e) {
                dir = we.getDirection(player,
                        args.getString(1).toLowerCase());
            }
            break;

        case 3:
            // Both reverse amount and direction
            reverseChange = args.getInteger(1) * -1;
            dir = we.getDirection(player,
                    args.getString(2).toLowerCase());
            break;
        default:
            dir = we.getDirection(player, "me");
        }

        Region region = session.getSelection(player.getWorld());
        int oldSize = region.getArea();
        region.expand(dir.multiply(change));

        if (reverseChange != 0) {
            region.expand(dir.multiply(reverseChange));
        }

        session.getRegionSelector(player.getWorld()).learnChanges();
        int newSize = region.getArea();
        
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);

        player.print("Region expanded " + (newSize - oldSize) + " blocks.");
    }

    @Command(
        aliases = { "/contract" },
        usage = "<amount> [reverse-amount] [direction]",
        desc = "Contract the selection area",
        min = 1,
        max = 3
    )
    @CommandPermissions("worldedit.selection.contract")
    public void contract(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Vector dir;
        int change = args.getInteger(0);
        int reverseChange = 0;

        switch (args.argsLength()) {
        case 2:
            // Either a reverse amount or a direction
            try {
                reverseChange = args.getInteger(1) * -1;
                dir = we.getDirection(player, "me");
            } catch (NumberFormatException e) {
                dir = we.getDirection(player, args.getString(1).toLowerCase());
            }
            break;

        case 3:
            // Both reverse amount and direction
            reverseChange = args.getInteger(1) * -1;
            dir = we.getDirection(player, args.getString(2).toLowerCase());
            break;
        default:
            dir = we.getDirection(player, "me");
        }

        try {
            Region region = session.getSelection(player.getWorld());
            int oldSize = region.getArea();
            region.contract(dir.multiply(change));
            if (reverseChange != 0) {
                region.contract(dir.multiply(reverseChange));
            }
            session.getRegionSelector(player.getWorld()).learnChanges();
            int newSize = region.getArea();
            
            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);


            player.print("Region contracted " + (oldSize - newSize) + " blocks.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = { "/shift" },
        usage = "<amount> [direction]",
        desc = "Shift the selection area",
        min = 1,
        max = 2
    )
    @CommandPermissions("worldedit.selection.shift")
    public void shift(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        Vector dir;

        int change = args.getInteger(0);
        if (args.argsLength() == 2) {
            dir = we.getDirection(player, args.getString(1).toLowerCase());
        } else {
            dir = we.getDirection(player, "me");
        }

        try {
            Region region = session.getSelection(player.getWorld());
            region.expand(dir.multiply(change));
            region.contract(dir.multiply(change));
            session.getRegionSelector(player.getWorld()).learnChanges();
            
            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);

            player.print("Region shifted.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = { "/outset" },
        usage = "<amount>",
        desc = "Outset the selection area",
        help =
            "Expands the selection by the given amount in all directions.\n" +
            "Flags:\n" +
            "  -h only expand horizontally\n" +
            "  -v only expand vertically\n",
        flags = "hv",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.selection.outset")
    public void outset(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
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

            session.getRegionSelector(player.getWorld()).learnChanges();
            
            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);

            player.print("Region outset.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        aliases = { "/inset" },
        usage = "<amount>",
        desc = "Inset the selection area",
        help =
            "Contracts the selection by the given amount in all directions.\n" +
            "Flags:\n" +
            "  -h only contract horizontally\n" +
            "  -v only contract vertically\n",
        flags = "hv",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.selection.inset")
    public void inset(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
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

        session.getRegionSelector(player.getWorld()).learnChanges();
        
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);

        player.print("Region inset.");
    }

    @Command(
        aliases = { "/size" },
        usage = "",
        desc = "Get information about the selection",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.selection.size")
    public void size(CommandContext args, LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        Region region = session.getSelection(player.getWorld());
        Vector size = region.getMaximumPoint()
                .subtract(region.getMinimumPoint())
                .add(1, 1, 1);

        player.print("Type: " + session.getRegionSelector(player.getWorld()).getTypeName());
        
        for (String line : session.getRegionSelector(player.getWorld()).getInformationLines()) {
            player.print(line);
        }

        player.print("Size: " + size);
        player.print("Cuboid distance: " + region.getMaximumPoint().distance(region.getMinimumPoint()));
        player.print("# of blocks: " + region.getArea());
    }

    @Command(
        aliases = { "/count" },
        usage = "<block>",
        desc = "Counts the number of a certain type of block",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.analysis.count")
    public void count(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
        Set<Integer> searchIDs = we.getBlockIDs(player,
                args.getString(0), true);
        player.print("Counted: " +
                editSession.countBlocks(session.getSelection(player.getWorld()), searchIDs));
    }

    @Command(
        aliases = { "/distr" },
        usage = "",
        desc = "Get the distribution of blocks in the selection",
        help =
            "Gets the distribution of blocks in the selection.\n" +
            "The -c flag makes it print to the console as well.",
        flags = "c",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.analysis.distr")
    public void distr(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
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
                BlockType block = BlockType.fromID(c.getID());
                String str = String.format("%-7s (%.3f%%) %s #%d",
                        String.valueOf(c.getAmount()),
                        c.getAmount() / (double) size * 100,
                        block == null ? "Unknown" : block.getName(), c.getID());
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
        aliases = { "/sel", ";" },
        usage = "[cuboid|extend|poly]",
        desc = "Choose a region selector",
        min = 0,
        max = 1
    )
    public void select(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        final LocalWorld world = player.getWorld();
        if (args.argsLength() == 0) {
            session.getRegionSelector(world).clear();
            return;
        }

        final String typeName = args.getString(0);
        final RegionSelector oldSelector = session.getRegionSelector(world);

        final RegionSelector selector;
        if (typeName.equalsIgnoreCase("cuboid")) {
            selector = new CuboidRegionSelector(oldSelector);
            player.print("Cuboid: left click for point 1, right click for point 2");
        } else if (typeName.equalsIgnoreCase("extend")) {
            selector = new ExtendingCuboidRegionSelector(oldSelector);
            player.print("Cuboid: left click for a starting point, right click to extend");
        } else if (typeName.equalsIgnoreCase("poly")) {
            selector = new Polygonal2DRegionSelector(world);
            player.print("2D polygon selector: Left/right click to add a point.");
        } else {
            player.printError("Only 'cuboid', 'extend' and 'poly' are accepted.");
            return;
        }
        session.setRegionSelector(world, selector);
        session.dispatchCUISelection(player);
    }
}
