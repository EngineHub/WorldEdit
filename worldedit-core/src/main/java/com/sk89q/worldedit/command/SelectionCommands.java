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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.command.argument.SelectorChoice;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.block.BlockDistributionCounter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.MultiDirection;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
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
import com.sk89q.worldedit.util.formatting.component.CommandListBox;
import com.sk89q.worldedit.util.formatting.component.SubtleFormat;
import com.sk89q.worldedit.util.formatting.component.TextComponentProducer;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.storage.ChunkStore;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.sk89q.worldedit.command.util.Logging.LogMode.POSITION;
import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;

/**
 * Selection commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SelectionCommands {

    private final WorldEdit we;

    public SelectionCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        name = "/pos1",
        desc = "Set position 1"
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.pos")
    public void pos1(Player player, LocalSession session,
                     @Arg(desc = "Coordinates to set position 1 to", def = "")
                         BlockVector3 coordinates) throws WorldEditException {
        Location pos;
        if (coordinates != null) {
            pos = new Location(player.getWorld(), coordinates.toVector3());
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos.toVector().toBlockPoint(), ActorSelectorLimits.forActor(player))) {
            player.printError("Position already set.");
            return;
        }

        session.getRegionSelector(player.getWorld())
                .explainPrimarySelection(player, session, pos.toVector().toBlockPoint());
    }

    @Command(
        name = "/pos2",
        desc = "Set position 2"
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.pos")
    public void pos2(Player player, LocalSession session,
                     @Arg(desc = "Coordinates to set position 2 to", def = "")
                         BlockVector3 coordinates) throws WorldEditException {
        Location pos;
        if (coordinates != null) {
            pos = new Location(player.getWorld(), coordinates.toVector3());
        } else {
            pos = player.getBlockIn();
        }

        if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos.toVector().toBlockPoint(), ActorSelectorLimits.forActor(player))) {
            player.printError("Position already set.");
            return;
        }

        session.getRegionSelector(player.getWorld())
                .explainSecondarySelection(player, session, pos.toVector().toBlockPoint());
    }

    @Command(
        name = "/hpos1",
        desc = "Set position 1 to targeted block"
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos1(Player player, LocalSession session) throws WorldEditException {

        Location pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos.toVector().toBlockPoint(), ActorSelectorLimits.forActor(player))) {
                player.printError("Position already set.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainPrimarySelection(player, session, pos.toVector().toBlockPoint());
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        name = "/hpos2",
        desc = "Set position 2 to targeted block"
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos2(Player player, LocalSession session) throws WorldEditException {

        Location pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos.toVector().toBlockPoint(), ActorSelectorLimits.forActor(player))) {
                player.printError("Position already set.");
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainSecondarySelection(player, session, pos.toVector().toBlockPoint());
        } else {
            player.printError("No block in sight!");
        }
    }

    @Command(
        name = "/chunk",
        desc = "Set the selection to your current chunk."
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.chunk")
    public void chunk(Player player, LocalSession session,
                      @Arg(desc = "The chunk to select", def = "")
                          BlockVector2 coordinates,
                      @Switch(name = 's', desc = "Expand your selection to encompass all chunks that are part of it")
                          boolean expandSelection,
                      @Switch(name = 'c', desc = "Use chunk coordinates instead of block coordinates")
                          boolean useChunkCoordinates) throws WorldEditException {
        final BlockVector3 min;
        final BlockVector3 max;
        final World world = player.getWorld();
        if (expandSelection) {
            Region region = session.getSelection(world);

            final BlockVector2 min2D = ChunkStore.toChunk(region.getMinimumPoint());
            final BlockVector2 max2D = ChunkStore.toChunk(region.getMaximumPoint());

            min = BlockVector3.at(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
            max = BlockVector3.at(max2D.getBlockX() * 16 + 15, world.getMaxY(), max2D.getBlockZ() * 16 + 15);

            player.print("Chunks selected: ("
                    + min2D.getBlockX() + ", " + min2D.getBlockZ() + ") - ("
                    + max2D.getBlockX() + ", " + max2D.getBlockZ() + ")");
        } else {
            final BlockVector2 min2D;
            if (coordinates != null) {
                // coords specified
                min2D = useChunkCoordinates
                    ? coordinates
                    : ChunkStore.toChunk(coordinates.toBlockVector3());
            } else {
                // use player loc
                min2D = ChunkStore.toChunk(player.getBlockIn().toVector().toBlockPoint());
            }

            min = BlockVector3.at(min2D.getBlockX() * 16, 0, min2D.getBlockZ() * 16);
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
        name = "/wand",
        desc = "Get the wand object"
    )
    @CommandPermissions("worldedit.wand")
    public void wand(Player player) throws WorldEditException {
        player.giveItem(new BaseItemStack(ItemTypes.get(we.getConfiguration().wandItem), 1));
        player.print("Left click: select pos #1; Right click: select pos #2");
    }

    @Command(
        name = "toggleeditwand",
        desc = "Toggle functionality of the edit wand"
    )
    @CommandPermissions("worldedit.wand.toggle")
    public void toggleWand(Player player, LocalSession session) throws WorldEditException {
        session.setToolControl(!session.isToolControlEnabled());

        if (session.isToolControlEnabled()) {
            player.print("Edit wand enabled.");
        } else {
            player.print("Edit wand disabled.");
        }
    }

    @Command(
        name = "/contract",
        desc = "Contract the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.contract")
    public void contract(Player player, LocalSession session,
                         @Arg(desc = "Amount to contract the selection by")
                             int amount,
                         @Arg(desc = "Amount to contract the selection by in the other direction", def = "0")
                             int reverseAmount,
                         @Arg(desc = "Direction to contract", def = Direction.AIM)
                         @MultiDirection
                             List<BlockVector3> direction) throws WorldEditException {
        try {
            Region region = session.getSelection(player.getWorld());
            int oldSize = region.getArea();
            if (reverseAmount == 0) {
                for (BlockVector3 dir : direction) {
                    region.contract(dir.multiply(amount));
                }
            } else {
                for (BlockVector3 dir : direction) {
                    region.contract(dir.multiply(amount), dir.multiply(-reverseAmount));
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
        name = "/shift",
        desc = "Shift the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.shift")
    public void shift(Player player, LocalSession session,
                      @Arg(desc = "Amount to shift the selection by")
                          int amount,
                      @Arg(desc = "Direction to contract", def = Direction.AIM)
                      @MultiDirection
                          List<BlockVector3> direction) throws WorldEditException {
        try {
            Region region = session.getSelection(player.getWorld());

            for (BlockVector3 dir : direction) {
                region.shift(dir.multiply(amount));
            }

            session.getRegionSelector(player.getWorld()).learnChanges();

            session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);

            player.print("Region shifted.");
        } catch (RegionOperationException e) {
            player.printError(e.getMessage());
        }
    }

    @Command(
        name = "/outset",
        desc = "Outset the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.outset")
    public void outset(Player player, LocalSession session,
                       @Arg(desc = "Amount to expand the selection by in all directions")
                           int amount,
                       @Switch(name = 'h', desc = "Only expand horizontally")
                           boolean onlyHorizontal,
                       @Switch(name = 'v', desc = "Only expand vertically")
                           boolean onlyVertical) throws WorldEditException {
        Region region = session.getSelection(player.getWorld());
        region.expand(getChangesForEachDir(amount, onlyHorizontal, onlyVertical));
        session.getRegionSelector(player.getWorld()).learnChanges();
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
        player.print("Region outset.");
    }

    @Command(
        name = "/inset",
        desc = "Inset the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.inset")
    public void inset(Player player, LocalSession session,
                      @Arg(desc = "Amount to contract the selection by in all directions")
                          int amount,
                      @Switch(name = 'h', desc = "Only contract horizontally")
                          boolean onlyHorizontal,
                      @Switch(name = 'v', desc = "Only contract vertically")
                          boolean onlyVertical) throws WorldEditException {
        Region region = session.getSelection(player.getWorld());
        region.contract(getChangesForEachDir(amount, onlyHorizontal, onlyVertical));
        session.getRegionSelector(player.getWorld()).learnChanges();
        session.getRegionSelector(player.getWorld()).explainRegionAdjust(player, session);
        player.print("Region inset.");
    }

    private BlockVector3[] getChangesForEachDir(int amount, boolean onlyHorizontal, boolean onlyVertical) {
        Stream.Builder<BlockVector3> changes = Stream.builder();

        if (!onlyHorizontal) {
            changes.add(BlockVector3.UNIT_Y);
            changes.add(BlockVector3.UNIT_MINUS_Y);
        }

        if (!onlyVertical) {
            changes.add(BlockVector3.UNIT_X);
            changes.add(BlockVector3.UNIT_MINUS_X);
            changes.add(BlockVector3.UNIT_Z);
            changes.add(BlockVector3.UNIT_MINUS_Z);
        }

        return changes.build().map(v -> v.multiply(amount)).toArray(BlockVector3[]::new);
    }

    @Command(
        name = "/size",
        desc = "Get information about the selection"
    )
    @CommandPermissions("worldedit.selection.size")
    public void size(Player player, LocalSession session,
                     @Switch(name = 'c', desc = "Get clipboard info instead")
                         boolean clipboardInfo) throws WorldEditException {
        Region region;
        if (clipboardInfo) {
            ClipboardHolder holder = session.getClipboard();
            Clipboard clipboard = holder.getClipboard();
            region = clipboard.getRegion();

            BlockVector3 origin = clipboard.getOrigin();
            player.print("Offset: " + origin);
        } else {
            region = session.getSelection(player.getWorld());

            player.print("Type: " + session.getRegionSelector(player.getWorld()).getTypeName());

            for (String line : session.getRegionSelector(player.getWorld()).getInformationLines()) {
                player.print(line);
            }
        }
        BlockVector3 size = region.getMaximumPoint()
                .subtract(region.getMinimumPoint())
                .add(1, 1, 1);

        player.print("Size: " + size);
        player.print("Cuboid distance: " + region.getMaximumPoint().distance(region.getMinimumPoint()));
        player.print("# of blocks: " + region.getArea());
    }


    @Command(
        name = "/count",
        desc = "Counts the number of a certain type of block"
    )
    @CommandPermissions("worldedit.analysis.count")
    public void count(Player player, LocalSession session, EditSession editSession,
                      @Arg(desc = "The block type(s) to count")
                          String blocks,
                      @Switch(name = 'f', desc = "Fuzzy, match states using a wildcard")
                          boolean fuzzy) throws WorldEditException {
        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setExtent(player.getExtent());
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(false);
        context.setPreferringWildcard(fuzzy);

        Set<BaseBlock> searchBlocks = we.getBlockFactory().parseFromListInput(blocks, context);
        int count = editSession.countBlocks(session.getSelection(player.getWorld()), searchBlocks);
        player.print("Counted: " + count);
    }

    @Command(
        name = "/distr",
        desc = "Get the distribution of blocks in the selection"
    )
    @CommandPermissions("worldedit.analysis.distr")
    public void distr(Player player, LocalSession session, EditSession editSession,
                      @Switch(name = 'c', desc = "Get the distribution of the clipboard instead")
                          boolean clipboardDistr,
                      @Switch(name = 'd', desc = "Separate blocks by state")
                          boolean separateStates) throws WorldEditException {
        List<Countable<BlockState>> distribution;

        if (clipboardDistr) {
            Clipboard clipboard = session.getClipboard().getClipboard(); // throws if missing
            BlockDistributionCounter count = new BlockDistributionCounter(clipboard, separateStates);
            RegionVisitor visitor = new RegionVisitor(clipboard.getRegion(), count);
            Operations.completeBlindly(visitor);
            distribution = count.getDistribution();
        } else {
            distribution = editSession.getBlockDistribution(session.getSelection(player.getWorld()), separateStates);
        }

        if (distribution.isEmpty()) {  // *Should* always be false
            player.printError("No blocks counted.");
            return;
        }

        // note: doing things like region.getArea is inaccurate for non-cuboids.
        int size = distribution.stream().mapToInt(Countable::getAmount).sum();
        player.print("# total blocks: " + size);

        for (Countable<BlockState> c : distribution) {
            String name = c.getID().getBlockType().getName();
            String str;
            if (separateStates) {
                str = String.format("%-7s (%.3f%%) %s (%s)",
                        String.valueOf(c.getAmount()),
                        c.getAmount() / (double) size * 100,
                        name,
                        c.getID().getAsString());
            } else {
                str = String.format("%-7s (%.3f%%) %s (%s)",
                        String.valueOf(c.getAmount()),
                        c.getAmount() / (double) size * 100,
                        name,
                        c.getID().getBlockType().getId());
            }
            player.print(str);
        }
    }

    @Command(
        name = "/sel",
        aliases = { ";", "/desel", "/deselect" },
        desc = "Choose a region selector"
    )
    public void select(Player player, LocalSession session,
                       @Arg(desc = "Selector to switch to", def = "")
                           SelectorChoice selector,
                       @Switch(name = 'd', desc = "Set default selector")
                           boolean setDefaultSelector) throws WorldEditException {
        final World world = player.getWorld();
        if (selector == null) {
            session.getRegionSelector(world).clear();
            session.dispatchCUISelection(player);
            player.print("Selection cleared.");
            return;
        }

        final RegionSelector oldSelector = session.getRegionSelector(world);

        final RegionSelector newSelector;
        switch (selector) {
            case CUBOID:
                newSelector = new CuboidRegionSelector(oldSelector);
                player.print("Cuboid: left click for point 1, right click for point 2");
                break;
            case EXTEND:
                newSelector = new ExtendingCuboidRegionSelector(oldSelector);
                player.print("Cuboid: left click for a starting point, right click to extend");
                break;
            case POLY: {
                newSelector = new Polygonal2DRegionSelector(oldSelector);
                player.print("2D polygon selector: Left/right click to add a point.");
                Optional<Integer> limit = ActorSelectorLimits.forActor(player).getPolygonVertexLimit();
                limit.ifPresent(integer -> player.print(integer + " points maximum."));
                break;
            }
            case ELLIPSOID:
                newSelector = new EllipsoidRegionSelector(oldSelector);
                player.print("Ellipsoid selector: left click=center, right click to extend");
                break;
            case SPHERE:
                newSelector = new SphereRegionSelector(oldSelector);
                player.print("Sphere selector: left click=center, right click to set radius");
                break;
            case CYL:
                newSelector = new CylinderRegionSelector(oldSelector);
                player.print("Cylindrical selector: Left click=center, right click to extend.");
                break;
            case CONVEX:
            case HULL:
            case POLYHEDRON: {
                newSelector = new ConvexPolyhedralRegionSelector(oldSelector);
                player.print("Convex polyhedral selector: Left click=First vertex, right click to add more.");
                Optional<Integer> limit = ActorSelectorLimits.forActor(player).getPolyhedronVertexLimit();
                limit.ifPresent(integer -> player.print(integer + " points maximum."));
                break;
            }
            case UNKNOWN:
            default:
                CommandListBox box = new CommandListBox("Selection modes", null);
                box.setHidingHelp(true);
                TextComponentProducer contents = box.getContents();
                contents.append(SubtleFormat.wrap("Select one of the modes below:")).newline();

                box.appendCommand("cuboid", "Select two corners of a cuboid", "//sel cuboid");
                box.appendCommand("extend", "Fast cuboid selection mode", "//sel extend");
                box.appendCommand("poly", "Select a 2D polygon with height", "//sel poly");
                box.appendCommand("ellipsoid", "Select an ellipsoid", "//sel ellipsoid");
                box.appendCommand("sphere", "Select a sphere", "//sel sphere");
                box.appendCommand("cyl", "Select a cylinder", "//sel cyl");
                box.appendCommand("convex", "Select a convex polyhedral", "//sel convex");

                player.print(box.create(1));
                return;
        }

        if (setDefaultSelector) {
            RegionSelectorType found = null;
            for (RegionSelectorType type : RegionSelectorType.values()) {
                if (type.getSelectorClass() == newSelector.getClass()) {
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

        session.setRegionSelector(world, newSelector);
        session.dispatchCUISelection(player);
    }

}
