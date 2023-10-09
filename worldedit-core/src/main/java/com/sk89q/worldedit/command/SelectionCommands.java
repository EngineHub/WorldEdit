/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import com.google.common.base.Strings;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.command.argument.SelectorChoice;
import com.sk89q.worldedit.command.argument.SelectorChoiceOrList;
import com.sk89q.worldedit.command.tool.NavigationWand;
import com.sk89q.worldedit.command.tool.SelectionWand;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.command.util.WorldEditAsyncCommandBuilder;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.block.BlockDistributionCounter;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.internal.annotation.Chunk3d;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.MultiDirection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.ExtendingCuboidRegionSelector;
import com.sk89q.worldedit.regions.selector.RegionSelectorType;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.TextComponent;
import com.sk89q.worldedit.util.adventure.text.event.ClickEvent;
import com.sk89q.worldedit.util.adventure.text.event.HoverEvent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.formatting.adventure.CommandListBox;
import com.sk89q.worldedit.util.formatting.adventure.InvalidComponentException;
import com.sk89q.worldedit.util.formatting.adventure.PaginationBox;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.storage.ChunkStore;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;
import org.enginehub.piston.exception.StopExecutionException;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import static com.sk89q.worldedit.command.util.Logging.LogMode.POSITION;
import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;
import static com.sk89q.worldedit.world.storage.ChunkStore.CHUNK_SHIFTS;
import static com.sk89q.worldedit.world.storage.ChunkStore.CHUNK_SHIFTS_Y;

/**
 * Selection commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SelectionCommands {

    private final WorldEdit we;

    public SelectionCommands(WorldEdit we) {
        this.we = we;
    }

    @Nullable
    private BlockVector3 getLocatablePosition(Actor actor) {
        if (actor instanceof Locatable locatable) {
            return locatable.getLocation().toVector().toBlockPoint();
        }
        actor.printError(Component.translatable("worldedit.pos.console-require-coords"));
        return null;
    }

    @Command(
        name = "/pos",
        desc = "Set positions"
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.pos")
    public void pos(Actor actor, World world, LocalSession session,
                    @Arg(desc = "Coordinates to set the primary position to. Defaults to the player position if not passed.", def = "")
                        BlockVector3 pos1,
                    @Arg(desc = "Coordinates to add as secondary positions. Defaults to the player position if not passed.", def = "", variable = true)
                        List<BlockVector3> pos2,
                    @ArgFlag(name = 's', desc = "Selector to switch to")
                        SelectorChoice selectorChoice) throws WorldEditException {
        if (pos1 == null) {
            pos1 = getLocatablePosition(actor);
            if (pos1 == null) {
                return;
            }
        }

        if (pos2.isEmpty()) {
            var newPos2 = getLocatablePosition(actor);
            if (newPos2 == null) {
                return;
            }
            pos2 = List.of(newPos2);
        }

        RegionSelector regionSelector = session.getRegionSelector(world);

        if (selectorChoice != null) {
            var newSelector = selectorChoice.createNewSelector(world);
            session.setRegionSelector(world, newSelector);
            selectorChoice.explainNewSelector(actor);
            regionSelector = newSelector;
        }

        regionSelector.selectPrimary(pos1, ActorSelectorLimits.forActor(actor));

        for (BlockVector3 vector : pos2) {
            regionSelector.selectSecondary(vector, ActorSelectorLimits.forActor(actor));
        }

        session.dispatchCUISelection(actor);

        for (Component line : regionSelector.getSelectionInfoLines()) {
            actor.printInfo(line);
        }
        actor.printInfo(Component.translatable("worldedit.selection.updated"));
    }

    @Command(
        name = "/pos1",
        desc = "Set position 1"
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.pos")
    public void pos1(Actor actor, World world, LocalSession session,
                     @Arg(desc = "Coordinates to set position 1 to", def = "")
                         BlockVector3 coordinates) {
        if (coordinates == null) {
            coordinates = getLocatablePosition(actor);
            if (coordinates == null) {
                return;
            }
        }

        if (!session.getRegionSelector(world).selectPrimary(coordinates, ActorSelectorLimits.forActor(actor))) {
            actor.printError(Component.translatable("worldedit.pos.already-set"));
            return;
        }

        session.getRegionSelector(world).explainPrimarySelection(actor, session, coordinates);
    }

    @Command(
        name = "/pos2",
        desc = "Set position 2"
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.pos")
    public void pos2(Actor actor, World world, LocalSession session,
                     @Arg(desc = "Coordinates to set position 2 to", def = "")
                         BlockVector3 coordinates) {
        if (coordinates == null) {
            coordinates = getLocatablePosition(actor);
            if (coordinates == null) {
                return;
            }
        }

        if (!session.getRegionSelector(world).selectSecondary(coordinates, ActorSelectorLimits.forActor(actor))) {
            actor.printError(Component.translatable("worldedit.pos.already-set"));
            return;
        }

        session.getRegionSelector(world).explainSecondarySelection(actor, session, coordinates);
    }

    @Command(
        name = "/hpos1",
        desc = "Set position 1 to targeted block"
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos1(Player player, LocalSession session) {

        Location pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld()).selectPrimary(pos.toVector().toBlockPoint(), ActorSelectorLimits.forActor(player))) {
                player.printError(Component.translatable("worldedit.hpos.already-set"));
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainPrimarySelection(player, session, pos.toVector().toBlockPoint());
        } else {
            player.printError(Component.translatable("worldedit.hpos.no-block"));
        }
    }

    @Command(
        name = "/hpos2",
        desc = "Set position 2 to targeted block"
    )
    @CommandPermissions("worldedit.selection.hpos")
    public void hpos2(Player player, LocalSession session) {

        Location pos = player.getBlockTrace(300);

        if (pos != null) {
            if (!session.getRegionSelector(player.getWorld()).selectSecondary(pos.toVector().toBlockPoint(), ActorSelectorLimits.forActor(player))) {
                player.printError(Component.translatable("worldedit.hpos.already-set"));
                return;
            }

            session.getRegionSelector(player.getWorld())
                    .explainSecondarySelection(player, session, pos.toVector().toBlockPoint());
        } else {
            player.printError(Component.translatable("worldedit.hpos.no-block"));
        }
    }

    @Command(
        name = "/chunk",
        desc = "Set the selection to your current chunk.",
        descFooter = "This command selects 256-block-tall areas,\nwhich can be specified by the y-coordinate.\nE.g. -c x,1,z will select from y=256 to y=511."
    )
    @Logging(POSITION)
    @CommandPermissions("worldedit.selection.chunk")
    public void chunk(Actor actor, World world, LocalSession session,
                      @Arg(desc = "The chunk to select", def = "")
                      @Chunk3d
                          BlockVector3 coordinates,
                      @Switch(name = 's', desc = "Expand your selection to encompass all chunks that are part of it")
                          boolean expandSelection,
                      @Switch(name = 'c', desc = "Use chunk coordinates instead of block coordinates")
                          boolean useChunkCoordinates) throws WorldEditException {
        final BlockVector3 min;
        final BlockVector3 max;
        if (expandSelection) {
            Region region = session.getSelection(world);

            int minChunkY = world.getMinY() >> CHUNK_SHIFTS_Y;
            int maxChunkY = world.getMaxY() >> CHUNK_SHIFTS_Y;

            BlockVector3 minChunk = ChunkStore.toChunk3d(region.getMinimumPoint())
                .clampY(minChunkY, maxChunkY);
            BlockVector3 maxChunk = ChunkStore.toChunk3d(region.getMaximumPoint())
                .clampY(minChunkY, maxChunkY);

            min = minChunk.shl(CHUNK_SHIFTS, CHUNK_SHIFTS_Y, CHUNK_SHIFTS);
            max = maxChunk.shl(CHUNK_SHIFTS, CHUNK_SHIFTS_Y, CHUNK_SHIFTS).add(15, 255, 15);

            actor.printInfo(Component.translatable(
                "worldedit.chunk.selected-multiple",
                Component.text(minChunk.x()),
                Component.text(minChunk.y()),
                Component.text(minChunk.z()),
                Component.text(maxChunk.x()),
                Component.text(maxChunk.y()),
                Component.text(maxChunk.z())
            ));
        } else {
            BlockVector3 minChunk;
            if (coordinates != null) {
                // coords specified
                minChunk = useChunkCoordinates
                    ? coordinates
                    : ChunkStore.toChunk3d(coordinates);
            } else {
                // use player loc
                if (actor instanceof Locatable) {
                    minChunk = ChunkStore.toChunk3d(((Locatable) actor).getBlockLocation().toVector().toBlockPoint());
                } else {
                    throw new StopExecutionException(Component.text("A player or coordinates are required."));
                }
            }

            min = minChunk.shl(CHUNK_SHIFTS, CHUNK_SHIFTS_Y, CHUNK_SHIFTS);
            max = min.add(15, 255, 15);

            actor.printInfo(Component.translatable("worldedit.chunk.selected",
                Component.text(minChunk.x()),
                Component.text(minChunk.y()),
                Component.text(minChunk.z())));
        }

        final CuboidRegionSelector selector;
        if (session.getRegionSelector(world) instanceof ExtendingCuboidRegionSelector) {
            selector = new ExtendingCuboidRegionSelector(world);
        } else {
            selector = new CuboidRegionSelector(world);
        }
        selector.selectPrimary(min, ActorSelectorLimits.forActor(actor));
        selector.selectSecondary(max, ActorSelectorLimits.forActor(actor));
        session.setRegionSelector(world, selector);

        session.dispatchCUISelection(actor);

    }

    @Command(
        name = "/wand",
        desc = "Get the wand item",
        descFooter = "You must have also have permission to use at least one of the"
            + " features of the requested wand."
    )
    @CommandPermissions("worldedit.wand")
    public void wand(Player player, LocalSession session,
                     @Switch(name = 'n', desc = "Get a navigation wand")
                         boolean navWand) throws WorldEditException {
        Tool tool = navWand ? new NavigationWand() : new SelectionWand();
        if (!tool.canUse(player)) {
            player.printError(Component.translatable("worldedit.command.permissions"));
            return;
        }
        String wandId = navWand ? session.getNavWandItem() : session.getWandItem();
        if (wandId == null) {
            wandId = navWand ? we.getConfiguration().navigationWand : we.getConfiguration().wandItem;
        }
        ItemType itemType = ItemTypes.get(wandId);
        if (itemType == null) {
            player.printError(Component.translatable("worldedit.wand.invalid"));
            return;
        }
        player.giveItem(new BaseItemStack(itemType, 1));
        session.setTool(itemType, tool);
        player.printInfo(Component.translatable("worldedit.wand." + (navWand ? "nav" : "sel" ) + "wand.info"));
    }

    @Command(
        name = "toggleeditwand",
        desc = "Remind the user that the wand is now a tool and can be unbound with /tool none."
    )
    @CommandPermissions("worldedit.wand.toggle")
    public void toggleWand(Player player) {
        player.printInfo(
            Component.translatable(
                "worldedit.wand.selwand.now.tool",
                Component.text("/tool none", NamedTextColor.AQUA).clickEvent(
                    ClickEvent.runCommand("/tool none")),
                Component.text("/tool selwand", NamedTextColor.AQUA).clickEvent(
                    ClickEvent.runCommand("/tool selwand")),
                Component.text("//wand", NamedTextColor.AQUA).clickEvent(
                    ClickEvent.runCommand("//wand"))
            )
        );
    }

    @Command(
        name = "/contract",
        desc = "Contract the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.contract")
    public void contract(Actor actor, World world, LocalSession session,
                         @Arg(desc = "Amount to contract the selection by")
                             int amount,
                         @Arg(desc = "Amount to contract the selection by in the other direction", def = "0")
                             int reverseAmount,
                         @Arg(desc = "Direction to contract", def = Direction.AIM)
                         @MultiDirection
                             List<BlockVector3> direction) throws WorldEditException {
        try {
            Region region = session.getSelection(world);
            long oldSize = region.getVolume();
            if (reverseAmount == 0) {
                for (BlockVector3 dir : direction) {
                    region.contract(dir.multiply(amount));
                }
            } else {
                for (BlockVector3 dir : direction) {
                    region.contract(dir.multiply(amount), dir.multiply(-reverseAmount));
                }
            }
            session.getRegionSelector(world).learnChanges();
            long newSize = region.getVolume();

            session.getRegionSelector(world).explainRegionAdjust(actor, session);

            actor.printInfo(Component.translatable("worldedit.contract.contracted", Component.text(oldSize - newSize)));
        } catch (RegionOperationException e) {
            actor.printError(Component.text(e.getMessage()));
        }
    }

    @Command(
        name = "/shift",
        desc = "Shift the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.shift")
    public void shift(Actor actor, World world, LocalSession session,
                      @Arg(desc = "Amount to shift the selection by")
                          int amount,
                      @Arg(desc = "Direction to contract", def = Direction.AIM)
                      @MultiDirection
                          List<BlockVector3> direction) throws WorldEditException {
        try {
            Region region = session.getSelection(world);

            for (BlockVector3 dir : direction) {
                region.shift(dir.multiply(amount));
            }

            session.getRegionSelector(world).learnChanges();

            session.getRegionSelector(world).explainRegionAdjust(actor, session);

            actor.printInfo(Component.translatable("worldedit.shift.shifted"));
        } catch (RegionOperationException e) {
            actor.printError(Component.text(e.getMessage()));
        }
    }

    @Command(
        name = "/outset",
        desc = "Outset the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.outset")
    public void outset(Actor actor, World world, LocalSession session,
                       @Arg(desc = "Amount to expand the selection by in all directions")
                           int amount,
                       @Switch(name = 'h', desc = "Only expand horizontally")
                           boolean onlyHorizontal,
                       @Switch(name = 'v', desc = "Only expand vertically")
                           boolean onlyVertical) throws WorldEditException {
        Region region = session.getSelection(world);
        region.expand(getChangesForEachDir(amount, onlyHorizontal, onlyVertical));
        session.getRegionSelector(world).learnChanges();
        session.getRegionSelector(world).explainRegionAdjust(actor, session);
        actor.printInfo(Component.translatable("worldedit.outset.outset"));
    }

    @Command(
        name = "/inset",
        desc = "Inset the selection area"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.inset")
    public void inset(Actor actor, World world, LocalSession session,
                      @Arg(desc = "Amount to contract the selection by in all directions")
                          int amount,
                      @Switch(name = 'h', desc = "Only contract horizontally")
                          boolean onlyHorizontal,
                      @Switch(name = 'v', desc = "Only contract vertically")
                          boolean onlyVertical) throws WorldEditException {
        Region region = session.getSelection(world);
        region.contract(getChangesForEachDir(amount, onlyHorizontal, onlyVertical));
        session.getRegionSelector(world).learnChanges();
        session.getRegionSelector(world).explainRegionAdjust(actor, session);
        actor.printInfo(Component.translatable("worldedit.inset.inset"));
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
        name = "/trim",
        desc = "Minimize the selection to encompass matching blocks"
    )
    @Logging(REGION)
    @CommandPermissions("worldedit.selection.trim")
    public void trim(Actor actor, World world, LocalSession session,
                     @Arg(desc = "Mask of blocks to keep within the selection", def = "#existing")
                         Mask mask) throws WorldEditException {
        // Avoid checking blocks outside the original region but within the cuboid region
        Region originalRegion = session.getSelection(world);
        if (!(originalRegion instanceof CuboidRegion)) {
            mask = new MaskIntersection(new RegionMask(originalRegion), mask);
        }
        // Memoize the mask to reduce duplicated lookups
        mask = Masks.memoize(mask);

        // Result region will be cuboid
        CuboidRegion region = originalRegion.getBoundingBox();

        BlockVector3 min = region.getMinimumPoint();
        BlockVector3 max = region.getMaximumPoint();

        int minY = 0;
        boolean found = false;

        outer: for (int y = min.y(); y <= max.y(); y++) {
            for (int x = min.x(); x <= max.x(); x++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    BlockVector3 vec = BlockVector3.at(x, y, z);

                    if (mask.test(vec)) {
                        found = true;
                        minY = y;

                        break outer;
                    }
                }
            }
        }

        // If anything was found in the first pass, then the remaining variables are guaranteed to be set
        if (!found) {
            throw new StopExecutionException(Component.translatable(
                        "worldedit.trim.no-blocks"));
        }

        int maxY = minY;

        outer: for (int y = max.y(); y > minY; y--) {
            for (int x = min.x(); x <= max.x(); x++) {
                for (int z = min.z(); z <= max.z(); z++) {
                    BlockVector3 vec = BlockVector3.at(x, y, z);

                    if (mask.test(vec)) {
                        maxY = y;
                        break outer;
                    }
                }
            }
        }

        int minX = 0;

        outer: for (int x = min.x(); x <= max.x(); x++) {
            for (int z = min.z(); z <= max.z(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockVector3 vec = BlockVector3.at(x, y, z);

                    if (mask.test(vec)) {
                        minX = x;
                        break outer;
                    }
                }
            }
        }

        int maxX = minX;

        outer: for (int x = max.x(); x > minX; x--) {
            for (int z = min.z(); z <= max.z(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockVector3 vec = BlockVector3.at(x, y, z);

                    if (mask.test(vec)) {
                        maxX = x;
                        break outer;
                    }
                }
            }
        }

        int minZ = 0;

        outer: for (int z = min.z(); z <= max.z(); z++) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockVector3 vec = BlockVector3.at(x, y, z);

                    if (mask.test(vec)) {
                        minZ = z;
                        break outer;
                    }
                }
            }
        }

        int maxZ = minZ;

        outer: for (int z = max.z(); z > minZ; z--) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockVector3 vec = BlockVector3.at(x, y, z);

                    if (mask.test(vec)) {
                        maxZ = z;
                        break outer;
                    }
                }
            }
        }

        final CuboidRegionSelector selector;
        if (session.getRegionSelector(world) instanceof ExtendingCuboidRegionSelector) {
            selector = new ExtendingCuboidRegionSelector(world, BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
        } else {
            selector = new CuboidRegionSelector(world, BlockVector3.at(minX, minY, minZ), BlockVector3.at(maxX, maxY, maxZ));
        }
        session.setRegionSelector(world, selector);

        session.getRegionSelector(world).learnChanges();
        session.getRegionSelector(world).explainRegionAdjust(actor, session);
        actor.printInfo(Component.translatable("worldedit.trim.trim"));
    }

    @Command(
        name = "/size",
        desc = "Get information about the selection"
    )
    @CommandPermissions("worldedit.selection.size")
    public void size(Actor actor, World world, LocalSession session,
                     @Switch(name = 'c', desc = "Get clipboard info instead")
                         boolean clipboardInfo) throws WorldEditException {
        Region region;
        if (clipboardInfo) {
            ClipboardHolder holder = session.getClipboard();
            Clipboard clipboard = holder.getClipboard();
            region = clipboard.getRegion();

            BlockVector3 origin = clipboard.getOrigin();
            actor.printInfo(Component.translatable("worldedit.size.offset", Component.text(origin.toString())));
        } else {
            region = session.getSelection(world);

            actor.printInfo(Component.translatable("worldedit.size.type", Component.text(session.getRegionSelector(world).getTypeName())));

            for (Component line : session.getRegionSelector(world).getSelectionInfoLines()) {
                actor.printInfo(line);
            }
        }
        BlockVector3 size = region.getMaximumPoint()
                .subtract(region.getMinimumPoint())
                .add(1, 1, 1);

        actor.printInfo(Component.translatable("worldedit.size.size", Component.text(size.toString())));
        actor.printInfo(Component.translatable("worldedit.size.distance", Component.text(region.getMaximumPoint().distance(region.getMinimumPoint()))));
        actor.printInfo(Component.translatable("worldedit.size.blocks", Component.text(region.getVolume())));
    }

    @Command(
        name = "/count",
        desc = "Counts the number of blocks matching a mask"
    )
    @CommandPermissions("worldedit.analysis.count")
    public int count(Actor actor, World world, LocalSession session, EditSession editSession,
                      @Arg(desc = "The mask of blocks to match")
                          Mask mask) throws WorldEditException {
        int count = editSession.countBlocks(session.getSelection(world), mask);
        actor.printInfo(Component.translatable("worldedit.count.counted", Component.text(count)));
        return count;
    }

    @Command(
        name = "/distr",
        desc = "Get the distribution of blocks in the selection"
    )
    @CommandPermissions("worldedit.analysis.distr")
    public void distr(Actor actor, World world, LocalSession session,
                      @Switch(name = 'c', desc = "Get the distribution of the clipboard instead")
                          boolean clipboardDistr,
                      @Switch(name = 'd', desc = "Separate blocks by state")
                          boolean separateStates,
                      @ArgFlag(name = 'p', desc = "Gets page from a previous distribution.")
                          Integer page) throws WorldEditException {
        List<Countable<BlockState>> distribution;

        if (page == null) {
            if (clipboardDistr) {
                Clipboard clipboard = session.getClipboard().getClipboard(); // throws if missing
                BlockDistributionCounter count = new BlockDistributionCounter(clipboard, separateStates);
                RegionVisitor visitor = new RegionVisitor(clipboard.getRegion(), count);
                Operations.completeBlindly(visitor);
                distribution = count.getDistribution();
            } else {
                try (EditSession editSession = session.createEditSession(actor)) {
                    distribution = editSession.getBlockDistribution(session.getSelection(world), separateStates);
                }
            }
            session.setLastDistribution(distribution);
            page = 1;
        } else {
            distribution = session.getLastDistribution();
            if (distribution == null) {
                actor.printError(Component.translatable("worldedit.distr.no-previous"));
                return;
            }
        }

        if (distribution.isEmpty()) {  // *Should* always be false
            actor.printError(Component.translatable("worldedit.distr.no-blocks"));
            return;
        }

        final int finalPage = page;
        WorldEditAsyncCommandBuilder.createAndSendMessage(actor, () -> {
            BlockDistributionResult res = new BlockDistributionResult(distribution, separateStates);
            if (!actor.isPlayer()) {
                res.formatForConsole();
            }
            return res.build(finalPage);
        }, (Component) null);
    }

    @Command(
        name = "/sel",
        aliases = { ";", "/desel", "/deselect" },
        desc = "Choose a region selector"
    )
    public void select(Actor actor, World world, LocalSession session,
                       @Arg(desc = "Selector to switch to", def = "")
                           SelectorChoiceOrList selectorChoiceOrList,
                       @Switch(name = 'd', desc = "Set default selector")
                           boolean setDefaultSelector) throws WorldEditException {
        if (selectorChoiceOrList == null) {
            session.getRegionSelector(world).clear();
            session.dispatchCUISelection(actor);
            actor.printInfo(Component.translatable("worldedit.select.cleared"));
            return;
        }

        if (!(selectorChoiceOrList instanceof SelectorChoice selectorChoice)) {
            CommandListBox box = new CommandListBox("Selection modes", null, null);
            box.setHidingHelp(true);
            box.builder().append(Component.text("Select one of the modes below:")).append(Component.newline());
            box.appendCommand("cuboid", Component.translatable("worldedit.select.cuboid.description"), "//sel cuboid");
            box.appendCommand("extend", Component.translatable("worldedit.select.extend.description"), "//sel extend");
            box.appendCommand("poly", Component.translatable("worldedit.select.poly.description"), "//sel poly");
            box.appendCommand("ellipsoid", Component.translatable("worldedit.select.ellipsoid.description"), "//sel ellipsoid");
            box.appendCommand("sphere", Component.translatable("worldedit.select.sphere.description"), "//sel sphere");
            box.appendCommand("cyl", Component.translatable("worldedit.select.cyl.description"), "//sel cyl");
            box.appendCommand("convex", Component.translatable("worldedit.select.convex.description"), "//sel convex");
            actor.print(box.build(1));
            return;
        }


        final RegionSelector oldSelector = session.getRegionSelector(world);

        final RegionSelector newSelector = selectorChoice.createNewSelector(oldSelector);
        selectorChoice.explainNewSelector(actor);

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
                actor.printInfo(Component.translatable("worldedit.select.default-set", Component.text(found.name())));
            } else {
                throw new RuntimeException("Something unexpected happened. Please report this.");
            }
        }

        session.setRegionSelector(world, newSelector);
        session.dispatchCUISelection(actor);
    }

    private static class BlockDistributionResult extends PaginationBox {

        private final List<Countable<BlockState>> distribution;
        private final int totalBlocks;
        private final boolean separateStates;

        BlockDistributionResult(List<Countable<BlockState>> distribution, boolean separateStates) {
            super("Block Distribution", "//distr -p %page%" + (separateStates ? " -d" : ""));
            this.distribution = distribution;
            // note: doing things like region.getArea is inaccurate for non-cuboids.
            this.totalBlocks = distribution.stream().mapToInt(Countable::getAmount).sum();
            this.separateStates = separateStates;
            setComponentsPerPage(7);
        }

        @Override
        public Component component(int number) {
            Countable<BlockState> c = distribution.get(number);
            TextComponent.Builder line = Component.text();

            final int count = c.getAmount();

            final double perc = count / (double) totalBlocks * 100;
            final int maxDigits = (int) (Math.log10(totalBlocks) + 1);
            final int curDigits = (int) (Math.log10(count) + 1);
            line.append(Component.text(String.format("%s%.3f%%  ", perc < 10 ? "  " : "", perc), NamedTextColor.GOLD));
            final int space = maxDigits - curDigits;
            String pad = Strings.repeat(" ", space == 0 ? 2 : 2 * space + 1);
            line.append(Component.text(String.format("%s%s", count, pad), NamedTextColor.YELLOW));

            final BlockState state = c.getID();
            final BlockType blockType = state.getBlockType();
            Component blockName = blockType.getDisplayName().color(NamedTextColor.LIGHT_PURPLE);
            TextComponent toolTip;
            if (separateStates && state != blockType.getDefaultState()) {
                toolTip = Component.text(state.getAsString(), NamedTextColor.GRAY);
                blockName = blockName.append(Component.text("*", NamedTextColor.LIGHT_PURPLE));
            } else {
                toolTip = Component.text(blockType.id(), NamedTextColor.GRAY);
            }
            blockName = blockName.hoverEvent(HoverEvent.showText(toolTip));
            line.append(blockName);

            return line.build();
        }

        @Override
        public int getComponentsSize() {
            return distribution.size();
        }

        @Override
        public Component build(int page) throws InvalidComponentException {
            super.builder().append(Component.translatable("worldedit.distr.total", NamedTextColor.GRAY, Component.text(totalBlocks)))
                    .append(Component.newline());
            return super.build(page);
        }
    }
}
