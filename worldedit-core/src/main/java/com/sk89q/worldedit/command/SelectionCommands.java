/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.POSITION;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.ConvexPolyhedralRegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.CylinderRegionSelector;
import com.sk89q.worldedit.regions.selector.EllipsoidRegionSelector;
import com.sk89q.worldedit.regions.selector.ExtendingCuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.Polygonal2DRegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldedit.regions.selector.SphereRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.formatting.StyledFragment;
import com.sk89q.worldedit.util.formatting.component.CommandListBox;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.storage.ChunkStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Selection commands.
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
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.pos")
    public void pos1(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        Location pos;

        if (args.argsLength() == 1) {
            if (args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
                String[] coords = args.getString(0).split(",");
                pos = new Location(player.getWorld(), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
            } else {
                player.printError("Invalid coordinates " + args.getString(0));
                return;
            }
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos.toVector(), ActorSelectorLimits.forActor(player))) {
            player.printError("Position already set.");
            return;
        }

        session.getRegionSelector(player.getWorld())
                .explainPrimarySelection(player, session, pos.toVector());
    }

    @Command(
        aliases = { "/pos2" },
        usage = "[coordinates]",
        desc = "Set position 2",
        min = 0,
        max = 1
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.pos")
    public void pos2(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        Location pos;
        if (args.argsLength() == 1) {
            if (args.getString(0).matches("-?\\d+,-?\\d+,-?\\d+")) {
                String[] coords = args.getString(0).split(",");
                pos = new Location(player.getWorld(), Integer.parseInt(coords[0]),
                        Integer.parseInt(coords[1]),
                        Integer.parseInt(coords[2]));
            } else {
                player.printError("Invalid coordinates " + args.getString(0));
                return;
            }
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos.toVector(), ActorSelectorLimits.forActor(player))) {
            player.printError("Position already set.");
            return;
        }

        session.getRegionSelector(player.getWorld())
                .explainSecondarySelection(player, session, pos.toVector());
    }

    @Command(
        aliases = { "/hpos1" },
        usage = "",
        desc = "Set position 1 to targeted block",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos1(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        Location pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos.toVector(), ActorSelectorLimits.forActor(player))) {
                player.printError("Position already set.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainPrimarySelection(player, session, pos.toVector());
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
    public void hpos2(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        Location pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos.toVector(), ActorSelectorLimits.forActor(player))) {
                player.printError("Position already set.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainSecondarySelection(player, session, pos.toVector());
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        aliases = { "/chunk" },
        usage = "[x,z coordinates]",
        flags = "sc",
        desc = "Set the selection to your current chunk.",
        help =
            "Set the selection to the chunk you are currently in.\n" +
            "With the -s flag, your current selection is expanded\n" +
            "to encompass all chunks that are part of it.\n\n" +
            "Specifying coordinates will use those instead of your\n"+
            "current position. Use -c to specify chunk coordinates,\n" +
            "otherwise full coordinates will be implied.\n" +
            "(for example, the coordinates 5,5 are the same as -c 0,0)",
        min = 0,
        max = 1
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.chunk")
    public void chunk(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        final Vector min;
        final Vector max;
        final World world = player.getWorld();
        if (args.hasFlag('s')) {
            Region region = session.getSelection(world);

            final Vector2D min2D = ChunkStore.toChunk(region.getMinimumPoint());
            final Vector2D max2D = ChunkStore.toChunk(region.getMaximumPoint());

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = new Vector(max2D.getBlockX() * 16 + 15, world.getMaxY(), max2D.getBlockZ() * 16 + 15);

            player.print("Chunks selected: ("
                    + min2D.getBlockX() + ", " + min2D.getBlockZ() + ") - ("
                    + max2D.getBlockX() + ", " + max2D.getBlockZ() + ")");
        } else {
            final Vector2D min2D;
            if (args.argsLength() == 1) {
                // coords specified
                String[] coords = args.getString(0).split(",");
                if (coords.length != 2) {
                    throw new InsufficientArgumentsException("Invalid coordinates specified.");
                }
                int x = Integer.parseInt(coords[0]);
                int z = Integer.parseInt(coords[1]);
                Vector2D pos = new Vector2D(x, z);
                min2D = (args.hasFlag('c')) ? pos : ChunkStore.toChunk(pos.toVector());
            } else {
                // use player loc
                min2D = ChunkStore.toChunk(player.getBlockIn().toVector());
            }

            min = new Vector(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = min.add(15, world.getMaxY(), 15);

            player.print("Chunk selected: "
                    + min2D.getBlockX() + ", " + min2D.getBlockZ());
        }

        final CuboidRegionSelector selector;
        if (session.getRegionSelector(world) instanceof ExtendingCuboidRegionSelector) {
            selector = new ExtendingCuboidRegionSelector(world);
        } else {
            selector = new CuboidRegionSelector(world);
        }
        selector.selectPrimary(min, ActorSelectorLimits.forActor(player));
        selector.selectSecondary(max, ActorSelectorLimits.forActor(player));
        session.setRegionSelector(world, selector);

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
    public void wand(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        player.giveItem(new BaseItemStack(ItemTypes.get(we.getConfiguration().wandItem), 1));
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
    public void toggleWand(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        
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
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.expand")
    public void expand(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        // Special syntax (//expand vert) to expand the selection between
        // sky and bedrock.
        if (args.getString(0).equalsIgnoreCase("vert")
                || args.getString(0).equalsIgnoreCase("vertical")) {
            Region region = session.getSelection(player.getWorld());
            try {
                int oldSize = region.getArea();
                region.expand(
                        new Vector(0, (player.getWorld().getMaxY() + 1), 0),
                        new Vector(0, -(player.getWorld().getMaxY() + 1), 0));
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

        List<Vector> dirs = new ArrayList<>();
        int change = args.getInteger(0);
        int reverseChange = 0;

        switch (args.argsLength()) {
        case 2:
            // Either a reverse amount or a direction
            try {
                reverseChange = args.getInteger(1);
                dirs.add(we.getDirection(player, "me"));
            } catch (NumberFormatException e) {
                if (args.getString(1).contains(",")) {
                    String[] split = args.getString(1).split(",");
                    for (String s : split) {
                        dirs.add(we.getDirection(player, s.toLowerCase()));
                    }
                } else {
                    dirs.add(we.getDirection(player, args.getString(1).toLowerCase()));
                }
            }
            break;

        case 3:
            // Both reverse amount and direction
            reverseChange = args.getInteger(1);
            if (args.getString(2).contains(",")) {
                String[] split = args.getString(2).split(",");
                for (String s : split) {
                    dirs.add(we.getDirection(player, s.toLowerCase()));
                }
            } else {
                dirs.add(we.getDirection(player, args.getString(2).toLowerCase()));
            }
            break;

        default:
            dirs.add(we.getDirection(player, "me"));
            break;

        }

        Region region = session.getSelection(player.getWorld());
        int oldSize = region.getArea();

        if (reverseChange == 0) {
            for (Vector dir : dirs) {
                region.expand(dir.multiply(change));
            }
        } else {
            for (Vector dir : dirs) {
                region.expand(dir.multiply(change), dir.multiply(-reverseChange));
            }
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
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.contract")
    public void contract(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        List<Vector> dirs = new ArrayList<>();
        int change = args.getInteger(0);
        int reverseChange = 0;

        switch (args.argsLength()) {
        case 2:
            // Either a reverse amount or a direction
            try {
                reverseChange = args.getInteger(1);
                dirs.add(we.getDirection(player, "me"));
            } catch (NumberFormatException e) {
                if (args.getString(1).contains(",")) {
                    String[] split = args.getString(1).split(",");
                    for (String s : split) {
                        dirs.add(we.getDirection(player, s.toLowerCase()));
                    }
                } else {
                    dirs.add(we.getDirection(player, args.getString(1).toLowerCase()));
                }
            }
            break;

        case 3:
            // Both reverse amount and direction
            reverseChange = args.getInteger(1);
            if (args.getString(2).contains(",")) {
                String[] split = args.getString(2).split(",");
                for (String s : split) {
                    dirs.add(we.getDirection(player, s.toLowerCase()));
                }
            } else {
                dirs.add(we.getDirection(player, args.getString(2).toLowerCase()));
            }
            break;

        default:
            dirs.add(we.getDirection(player, "me"));
            break;
        }

        try {
            Region region = session.getSelection(player.getWorld());
            int oldSize = region.getArea();
            if (reverseChange == 0) {
                for (Vector dir : dirs) {
                    region.contract(dir.multiply(change));
                }
            } else {
                for (Vector dir : dirs) {
                    region.contract(dir.multiply(change), dir.multiply(-reverseChange));
                }
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
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.shift")
    public void shift(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        List<Vector> dirs = new ArrayList<>();
        int change = args.getInteger(0);
        if (args.argsLength() == 2) {
            if (args.getString(1).contains(",")) {
                for (String s : args.getString(1).split(",")) {
                    dirs.add(we.getDirection(player, s.toLowerCase()));
                }
            } else {
                dirs.add(we.getDirection(player, args.getString(1).toLowerCase()));
            }
        } else {
            dirs.add(we.getDirection(player, "me"));
        }

        try {
            Region region = session.getSelection(player.getWorld());

            for (Vector dir : dirs) {
                region.shift(dir.multiply(change));
            }

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
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.outset")
    public void outset(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        Region region = session.getSelection(player.getWorld());
        region.expand(getChangesForEachDir(args));
        session.getRegionSelector(player.getWorld()).learnChanges();
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
        player.print("Region outset.");
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
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.inset")
    public void inset(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        Region region = session.getSelection(player.getWorld());
        region.contract(getChangesForEachDir(args));
        session.getRegionSelector(player.getWorld()).learnChanges();
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
        player.print("Region inset.");
    }

    private Vector[] getChangesForEachDir(CommandContext args) {
        List<Vector> changes = new ArrayList<>(6);
        int change = args.getInteger(0);

        if (!args.hasFlag('h')) {
            changes.add((new Vector(0, 1, 0)).multiply(change));
            changes.add((new Vector(0, -1, 0)).multiply(change));
        }

        if (!args.hasFlag('v')) {
            changes.add((new Vector(1, 0, 0)).multiply(change));
            changes.add((new Vector(-1, 0, 0)).multiply(change));
            changes.add((new Vector(0, 0, 1)).multiply(change));
            changes.add((new Vector(0, 0, -1)).multiply(change));
        }

        return changes.toArray(new Vector[0]);
    }

    @Command(
        aliases = { "/size" },
        flags = "c",
        usage = "",
        desc = "Get information about the selection",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.selection.size")
    public void size(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        
        if (args.hasFlag('c')) {
            ClipboardHolder holder = session.getClipboard();
            Clipboard clipboard = holder.getClipboard();
            Region region = clipboard.getRegion();
            Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint());
            Vector origin = clipboard.getOrigin();

            player.print("Cuboid dimensions (max - min): " + size);
            player.print("Offset: " + origin);
            player.print("Cuboid distance: " + size.distance(Vector.ONE));
            player.print("# of blocks: " + (int) (size.getX() * size.getY() * size.getZ()));
            return;
        }
        
        Region region = session.getSelection(player.getWorld());
        Vector size = region.getMaximumPoint()
                .subtract(region.getMinimumPoint())
                .add(1, 1, 1);
        
        player.print("Type: " + session.getRegionSelector(player.getWorld())
                .getTypeName());
        
        for (String line : session.getRegionSelector(player.getWorld())
                .getInformationLines()) {
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
    public void count(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setExtent(player.getExtent());
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(false);

        Set<BlockStateHolder> searchBlocks = we.getBlockFactory().parseFromListInput(args.getString(0), context);
        int count = editSession.countBlocks(session.getSelection(player.getWorld()), searchBlocks);
        player.print("Counted: " + count);
    }

    @Command(
        aliases = { "/distr" },
        usage = "",
        desc = "Get the distribution of blocks in the selection",
        help =
            "Gets the distribution of blocks in the selection.\n" +
            "The -c flag gets the distribution of your clipboard.\n" +
            "The -d flag separates blocks by state",
        flags = "cd",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.analysis.distr")
    public void distr(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException, CommandException {

        int size;
        boolean useData = args.hasFlag('d');
        List<Countable<BlockType>> distribution = null;
        List<Countable<BlockStateHolder>> distributionData = null;

        if (args.hasFlag('c')) {
            // TODO: Update for new clipboard
            throw new CommandException("Needs to be re-written again");
        } else {
            if (useData) {
                distributionData = editSession.getBlockDistributionWithData(session.getSelection(player.getWorld()));
            } else {
                distribution = editSession.getBlockDistribution(session.getSelection(player.getWorld()));
            }
            size = session.getSelection(player.getWorld()).getArea();
        }

        if ((useData && distributionData.size() <= 0) || (!useData && distribution.size() <= 0)) {  // *Should* always be false
            player.printError("No blocks counted.");
            return;
        }

        player.print("# total blocks: " + size);

        if (useData) {
            for (Countable<BlockStateHolder> c : distributionData) {
                String name = c.getID().getBlockType().getName();
                String str = String.format("%-7s (%.3f%%) %s #%s%s",
                        String.valueOf(c.getAmount()),
                        c.getAmount() / (double) size * 100,
                        name,
                        c.getID().getBlockType().getId(),
                        c.getID().getStates());
                player.print(str);
            }
        } else {
            for (Countable<BlockType> c : distribution) {
                String name = c.getID().getName();
                String str = String.format("%-7s (%.3f%%) %s #%s",
                        String.valueOf(c.getAmount()),
                        c.getAmount() / (double) size * 100,
                        name,
                        c.getID().getId());
                player.print(str);
            }
        }
    }

    @Command(
        aliases = { "/sel", ";", "/desel", "/deselect" },
        flags = "d",
        usage = "[cuboid|extend|poly|ellipsoid|sphere|cyl|convex]",
        desc = "Choose a region selector",
        min = 0,
        max = 1
    )
    public void select(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        final World world = player.getWorld();
        if (args.argsLength() == 0) {
            session.getRegionSelector(world).clear();
            session.dispatchCUISelection(player);
            player.print("Selection cleared.");
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
            selector = new Polygonal2DRegionSelector(oldSelector);
            player.print("2D polygon selector: Left/right click to add a point.");
            Optional<Integer> limit = ActorSelectorLimits.forActor(player).getPolygonVertexLimit();
            limit.ifPresent(integer -> player.print(integer + " points maximum."));
        } else if (typeName.equalsIgnoreCase("ellipsoid")) {
            selector = new EllipsoidRegionSelector(oldSelector);
            player.print("Ellipsoid selector: left click=center, right click to extend");
        } else if (typeName.equalsIgnoreCase("sphere")) {
            selector = new SphereRegionSelector(oldSelector);
            player.print("Sphere selector: left click=center, right click to set radius");
        } else if (typeName.equalsIgnoreCase("cyl")) {
            selector = new CylinderRegionSelector(oldSelector);
            player.print("Cylindrical selector: Left click=center, right click to extend.");
        } else if (typeName.equalsIgnoreCase("convex") || typeName.equalsIgnoreCase("hull") || typeName.equalsIgnoreCase("polyhedron")) {
            selector = new ConvexPolyhedralRegionSelector(oldSelector);
            player.print("Convex polyhedral selector: Left click=First vertex, right click to add more.");
            Optional<Integer> limit = ActorSelectorLimits.forActor(player).getPolyhedronVertexLimit();
            limit.ifPresent(integer -> player.print(integer + " points maximum."));
        } else {
            CommandListBox box = new CommandListBox("Selection modes");
            StyledFragment contents = box.getContents();
            StyledFragment tip = contents.createFragment(Style.RED);
            tip.append("Select one of the modes below:").newLine();

            box.appendCommand("cuboid", "Select two corners of a cuboid");
            box.appendCommand("extend", "Fast cuboid selection mode");
            box.appendCommand("poly", "Select a 2D polygon with height");
            box.appendCommand("ellipsoid", "Select an ellipsoid");
            box.appendCommand("sphere", "Select a sphere");
            box.appendCommand("cyl", "Select a cylinder");
            box.appendCommand("convex", "Select a convex polyhedral");

            player.printRaw(ColorCodeBuilder.asColorCodes(box));
            return;
        }

        if (args.hasFlag('d')) {
            RegionSelectorType found = null;
            for (RegionSelectorType type : RegionSelectorType.values()) {
                if (type.getSelectorClass() == selector.getClass()) {
                    found = type;
                    break;
                }
            }

            if (found != null) {
                session.setDefaultRegionSelector(found);
                player.print("Your default region selector is now " + found.name() + ".");
            } else {
                throw new RuntimeException("Something unexpected happened. Please report this.");
            }
        }

        session.setRegionSelector(world, selector);
        session.dispatchCUISelection(player);
    }

}
