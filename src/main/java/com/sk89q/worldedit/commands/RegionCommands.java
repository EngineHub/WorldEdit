// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;

import javax.annotation.Nullable;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.rebar.command.binding.Range;
import com.sk89q.rebar.command.binding.Switch;
import com.sk89q.rebar.command.binding.Text;
import com.sk89q.rebar.command.binding.Unmanaged;
import com.sk89q.rebar.command.parametric.Optional;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.HeightMap;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.annotation.Direction;
import com.sk89q.worldedit.annotation.Selection;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.expression.ExpressionException;
import com.sk89q.worldedit.filtering.GaussianKernel;
import com.sk89q.worldedit.filtering.HeightMapFilter;
import com.sk89q.worldedit.masks.ExistingBlockMask;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.operation.RejectedOperationException;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.transform.ReplaceBlocks;

/**
 * Commands that deal with a {@link Region} provided by the user's current
 * selection.
 */
public class RegionCommands {
    
    private final WorldEdit worldEdit;

    /**
     * Construct a new instance.
     * 
     * @param worldEdit an instance of WorldEdit
     */
    public RegionCommands(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /*
     * Set blocks within a region to another block.
     */
    @Command(aliases = "/set",
             desc = "Set all the blocks inside the selection to a block")
    @CommandPermissions("worldedit.region.set")
    @Logging(REGION)
    public void setBlocks(LocalPlayer player, @Unmanaged EditSession editSession,
            @Selection Region region, Pattern replaceWith, @Unmanaged CommandContext context)
            throws WorldEditException, RejectedOperationException {
        ReplaceBlocks op = new ReplaceBlocks(editSession, region, replaceWith);
        worldEdit.execute(player, op, editSession, "/" + context.getCommand());
    }

    /*
     * Replace all the blocks within a region that match a given mask into a
     * given block pattern.
     */
    @Command(aliases = { "/replace", "/re", "/rep", "/r" },
             desc = "Replace all blocks in the selection with another")
    @CommandPermissions("worldedit.region.replace")
    @Logging(REGION)
    public void replaceBlocks(LocalPlayer player, EditSession editSession,
            @Selection Region region, @Nullable Mask mask, Pattern replaceWith)
            throws MaxChangedBlocksException {
        
        // No mask? Replace non-air blocks
        if (mask == null) {
            mask = new ExistingBlockMask();
        }
        
        int affected = editSession.replaceBlocks(region, mask, replaceWith);
        player.print(affected + " block(s) have been replaced.");
    }

    /*
     * Place a pattern on top of blocks in a cuboid area contain the selection.
     */
    @Command(aliases = { "/overlay", "/over", "/o" },
             desc = "Set a block on top of blocks in the region")
    @CommandPermissions("worldedit.region.overlay")
    @Logging(REGION)
    public void overlayBlocks(LocalPlayer player, EditSession editSession,
            @Selection Region region, Pattern overlayWith)
            throws MaxChangedBlocksException {
        
        int affected = editSession.overlayCuboidBlocks(region, overlayWith);
        player.print(affected + " block(s) have been changed.");
    }

    /*
     * Set the block in the center.
     */
    @Command(aliases = { "/center", "/middle" },
             desc = "Set the center block(s)")
    @Logging(REGION)
    @CommandPermissions("worldedit.region.center")
    public void setCenterBlock(LocalPlayer player, EditSession editSession,
            @Selection Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        
        int affected = editSession.center(region, pattern);
        player.print("Center set ("+ affected + " blocks changed)");
    }

    /*
     * Make an area look natural.
     */
    @Command(aliases = { "/naturalize", "/n", "/nature", "/natural" },
             desc = "3 layers of dirt on top then rock below")
    @CommandPermissions("worldedit.region.naturalize")
    @Logging(REGION)
    public void makeNatural(LocalPlayer player, EditSession editSession,
            @Selection Region region) throws MaxChangedBlocksException {
        
        int affected = editSession.naturalizeCuboidBlocks(region);
        player.print(affected + " block(s) have been naturalized.");
    }

    /*
     * Set the four walls of a region.
     */
    @Command(aliases = { "/walls", "/sides" },
             desc = "Set the four sides of the selection")
    @CommandPermissions("worldedit.region.walls")
    @Logging(REGION)
    public void setSideBlocks(LocalPlayer player, EditSession editSession,
            @Selection Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        
        int affected = editSession.makeCuboidWalls(region, pattern);
        player.print(affected + " block(s) have been changed.");
    }

    /*
     * Set the faces of a region.
     */
    @Command(aliases = { "/faces", "/outline" },
             desc = "Build the walls, ceiling, and floor of a selection")
    @CommandPermissions("worldedit.region.faces")
    @Logging(REGION)
    public void setFaceBlocks(LocalPlayer player, EditSession editSession,
            @Selection Region region, Pattern pattern)
            throws MaxChangedBlocksException {
        
        int affected = editSession.makeCuboidFaces(region, pattern);
        player.print(affected + " block(s) have been changed.");
    }

    /*
     * Smooth the elevation of a region.
     */
    @Command(aliases = "/smooth",
             desc = "Smooth the elevation in the selection",
             help = "Smooths the elevation in the selection.\n" +
                    "The -n flag makes it only consider naturally occuring blocks.")
    @CommandPermissions("worldedit.region.smooth")
    @Logging(REGION)
    public void smoothen(LocalPlayer player, EditSession editSession,
            @Selection Region region,
            @Nullable @Range(min = 1) Integer iterations,
            @Switch('n') boolean onlyNaturalBlocks)
            throws MaxChangedBlocksException {
        
        if (iterations == null) {
            iterations = 1;
        }

        HeightMap heightMap = new HeightMap(editSession, region, onlyNaturalBlocks);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        int affected = heightMap.applyFilter(filter, iterations);
        player.print("Terrain's height map smoothed. " + affected + " block(s) changed.");

    }

    /*
     * Move the contents of a region.
     */
    @Command(aliases = "/move",
             desc = "Move the contents of the selection",
             help = "Moves the contents of the selection.\n" +
                    "The -s flag shifts the selection to the target location.\n" +
                    "Optionally fills the old location with a given left over block.")
    @CommandPermissions("worldedit.region.move")
    @Logging(ORIENTATION_REGION)
    public void move(LocalPlayer player, LocalSession session,
            EditSession editSession, @Selection Region region,
            @Optional("1") int count, @Optional(Direction.AIM) @Direction Vector direction,
            @Nullable Pattern fillIn, @Switch('s') boolean shiftSelection)
            throws WorldEditException {
        if (fillIn == null) {
            fillIn = new SingleBlockPattern(new BaseBlock(BlockID.AIR));
        }

        int affected = editSession.moveCuboidRegion(region, direction, count, true, fillIn);

        player.print(affected + " blocks moved.");

        if (shiftSelection) {
            region.shift(direction.multiply(count));
            
            RegionSelector selector = session.getRegionSelector(player.getWorld());
            selector.learnChanges();
            selector.explainRegionAdjust(player, session);
        }
    }

    /*
     * Stack the contents of a selection repeatedly.
     */
    @Command(aliases = "/stack",
             desc = "Repeat the contents of the selection",
             help = "Repeats the contents of the selection.\n" +
                    "Flags:\n" +
                    "  -s shifts the selection to the last stacked copy\n" +
                    "  -a skips air blocks")
    @CommandPermissions("worldedit.region.stack")
    @Logging(ORIENTATION_REGION)
    public void stack(LocalPlayer player, LocalSession session,
            EditSession editSession, @Selection Region region,
            @Optional("1") int count, @Optional(Direction.AIM) @Direction Vector direction,
            @Switch('s') boolean shiftSelection,
            @Switch('a') boolean ignoreAirBlocks) throws WorldEditException {
        
        int affected = editSession.stackCuboidRegion(
                region, direction, count, !ignoreAirBlocks);

        player.print(affected + " blocks changed. Undo with //undo");

        if (shiftSelection) {
            Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint());
            Vector shiftVector = direction.multiply(count * (Math.abs(direction.dot(size)) + 1));
            region.shift(shiftVector);
            
            RegionSelector selector = session.getRegionSelector(player.getWorld());
            selector.learnChanges();
            selector.explainRegionAdjust(player, session);
        }
    }

    /*
     * Regenerate a chunk.
     */
    @Command(aliases = { "/regen", "/regenerate" },
             desc = "Regenerates the contents of the selection",
             help = "Regenerates the contents of the current selection.\n" +
                    "This command might affect things outside the selection,\n" +
                    "if they are within the same chunk.")
    @CommandPermissions("worldedit.regen")
    @Logging(REGION)
    public void regenerateChunk(LocalPlayer player, LocalSession session,
            EditSession editSession, @Selection Region region)
            throws WorldEditException {

        Mask mask = session.getMask();
        try {
            session.setMask(null);
            player.getWorld().regenerate(region, editSession);
        } finally {
            session.setMask(mask);
        }
        player.print("Region regenerated.");
    }

    /*
     * Deform the contents of a region given an expression.
     */
    @Command(aliases = "/deform",
             desc = "Deforms a selected region with an expression",
             help = "Deforms a selected region with an expression\n" +
                    "The expression is executed for each block and is expected\n" +
                    "to modify the variables x, y and z to point to a new block\n" +
                    "to fetch. See also tinyurl.com/wesyntax.")
    @CommandPermissions("worldedit.region.deform")
    @Logging(ALL)
    public void deform(LocalPlayer player, LocalSession session,
            EditSession editSession, @Selection Region region,
            @Switch('r') boolean relativeXYZ, @Switch('o') boolean atOrigin,
            @Text String expression) throws WorldEditException,
            ExpressionException {
        
        final Vector zero;
        Vector unit;

        if (relativeXYZ) {
            zero = new Vector(0, 0, 0);
            unit = new Vector(1, 1, 1);
        } else if (atOrigin) {
            zero = session.getPlacementPosition(player);
            unit = new Vector(1, 1, 1);
        } else {
            final Vector min = region.getMinimumPoint();
            final Vector max = region.getMaximumPoint();

            zero = max.add(min).multiply(0.5);
            unit = max.subtract(zero);

            if (unit.getX() == 0) unit = unit.setX(1.0);
            if (unit.getY() == 0) unit = unit.setY(1.0);
            if (unit.getZ() == 0) unit = unit.setZ(1.0);
        }

        int affected = editSession.deformRegion(region, zero, unit, expression);
        player.findFreePosition();
        player.print(affected + " block(s) have been deformed.");
    }

    /*
     * Hollow out the contents of a region.
     */
    @Command(aliases = "/hollow",
             desc = "Hollows out the object contained in this selection",
             help = "Hollows out the object contained in this selection.\n" +
                    "Optionally fills the hollowed out part with the given block.\n" +
                    "Thickness is measured in manhattan distance.")
    @CommandPermissions("worldedit.region.hollow")
    @Logging(REGION)
    public void hollow(LocalPlayer player, EditSession editSession,
            @Selection Region region,
            @Optional("1") @Range(min = 1) int thickness,
            @Nullable Pattern pattern) throws WorldEditException {
        
        if (pattern == null) {
            pattern = new SingleBlockPattern(new BaseBlock(BlockID.AIR));
        }

        int affected = editSession.hollowOutRegion(region, thickness, pattern);
        player.print(affected + " block(s) have been changed.");
    }
    
}
