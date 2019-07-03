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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.factory.ReplaceFactory;
import com.sk89q.worldedit.command.factory.TreeGeneratorFactory;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.ButcherBrush;
import com.sk89q.worldedit.command.tool.brush.ClipboardBrush;
import com.sk89q.worldedit.command.tool.brush.CylinderBrush;
import com.sk89q.worldedit.command.tool.brush.GravityBrush;
import com.sk89q.worldedit.command.tool.brush.HollowCylinderBrush;
import com.sk89q.worldedit.command.tool.brush.HollowSphereBrush;
import com.sk89q.worldedit.command.tool.brush.OperationFactoryBrush;
import com.sk89q.worldedit.command.tool.brush.SmoothBrush;
import com.sk89q.worldedit.command.tool.brush.SphereBrush;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.CreatureButcher;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.factory.Apply;
import com.sk89q.worldedit.function.factory.Deform;
import com.sk89q.worldedit.function.factory.Paint;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.request.RequestExtent;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Commands to set brush shape.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class BrushCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public BrushCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "sphere",
        aliases = { "s" },
        desc = "Choose the sphere brush"
    )
    @CommandPermissions("worldedit.brush.sphere")
    public void sphereBrush(Player player, LocalSession session,
                            @Arg(desc = "The pattern of blocks to set")
                                Pattern pattern,
                            @Arg(desc = "The radius of the sphere", def = "2")
                                double radius,
                            @Switch(name = 'h', desc = "Create hollow spheres instead")
                                boolean hollow) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setFill(pattern);
        tool.setSize(radius);

        if (hollow) {
            tool.setBrush(new HollowSphereBrush(), "worldedit.brush.sphere");
        } else {
            tool.setBrush(new SphereBrush(), "worldedit.brush.sphere");
        }

        player.print(String.format("Sphere brush shape equipped (%.0f).", radius));
    }

    @Command(
        name = "cylinder",
        aliases = { "cyl", "c" },
        desc = "Choose the cylinder brush"
    )
    @CommandPermissions("worldedit.brush.cylinder")
    public void cylinderBrush(Player player, LocalSession session,
                              @Arg(desc = "The pattern of blocks to set")
                                  Pattern pattern,
                              @Arg(desc = "The radius of the cylinder", def = "2")
                                  double radius,
                              @Arg(desc = "The height of the cylinder", def = "1")
                                  int height,
                              @Switch(name = 'h', desc = "Create hollow cylinders instead")
                                  boolean hollow) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);
        worldEdit.checkMaxBrushRadius(height);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setFill(pattern);
        tool.setSize(radius);

        if (hollow) {
            tool.setBrush(new HollowCylinderBrush(height), "worldedit.brush.cylinder");
        } else {
            tool.setBrush(new CylinderBrush(height), "worldedit.brush.cylinder");
        }

        player.print(String.format("Cylinder brush shape equipped (%.0f by %d).", radius, height));
    }

    @Command(
        name = "clipboard",
        aliases = { "copy" },
        desc = "Choose the clipboard brush"
    )
    @CommandPermissions("worldedit.brush.clipboard")
    public void clipboardBrush(Player player, LocalSession session,
                               @Switch(name = 'a', desc = "Don't paste air from the clipboard")
                                   boolean ignoreAir,
                               @Switch(name = 'o', desc = "Paste starting at the target location, instead of centering on it")
                                   boolean usingOrigin,
                               @Switch(name = 'e', desc = "Paste entities if available")
                                   boolean pasteEntities,
                               @Switch(name = 'b', desc = "Paste biomes if available")
                                   boolean pasteBiomes,
                               @ArgFlag(name = 'm', desc = "Skip blocks matching this mask in the clipboard", def = "")
                                   Mask sourceMask) throws WorldEditException {
        ClipboardHolder holder = session.getClipboard();
        Clipboard clipboard = holder.getClipboard();

        BlockVector3 size = clipboard.getDimensions();

        worldEdit.checkMaxBrushRadius(size.getBlockX() / 2D - 1);
        worldEdit.checkMaxBrushRadius(size.getBlockY() / 2D - 1);
        worldEdit.checkMaxBrushRadius(size.getBlockZ() / 2D - 1);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setBrush(new ClipboardBrush(holder, ignoreAir, usingOrigin, pasteEntities, pasteBiomes, sourceMask), "worldedit.brush.clipboard");

        player.print("Clipboard brush shape equipped.");
    }

    @Command(
        name = "smooth",
        desc = "Choose the terrain softener brush",
        descFooter = "Example: '/brush smooth 2 4 grass_block,dirt,stone'"
    )
    @CommandPermissions("worldedit.brush.smooth")
    public void smoothBrush(Player player, LocalSession session,
                            @Arg(desc = "The radius to sample for softening", def = "2")
                                double radius,
                            @Arg(desc = "The number of iterations to perform", def = "4")
                                int iterations,
                            @Arg(desc = "The mask of blocks to use for the heightmap", def = "")
                                Mask mask) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setSize(radius);
        tool.setBrush(new SmoothBrush(iterations, mask), "worldedit.brush.smooth");

        player.print(String.format("Smooth brush equipped (%.0f x %dx, using %s).", radius, iterations, mask == null ? "any block" : "filter"));
    }

    @Command(
        name = "extinguish",
        aliases = { "ex" },
        desc = "Shortcut fire extinguisher brush"
    )
    @CommandPermissions("worldedit.brush.ex")
    public void extinguishBrush(Player player, LocalSession session,
                                @Arg(desc = "The radius to extinguish", def = "5")
                                    double radius) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        Pattern fill = new BlockPattern(BlockTypes.AIR.getDefaultState());
        tool.setFill(fill);
        tool.setSize(radius);
        tool.setMask(new BlockTypeMask(new RequestExtent(), BlockTypes.FIRE));
        tool.setBrush(new SphereBrush(), "worldedit.brush.ex");

        player.print(String.format("Extinguisher equipped (%.0f).", radius));
    }

    @Command(
        name = "gravity",
        aliases = { "grav" },
        desc = "Gravity brush, simulates the effect of gravity"
    )
    @CommandPermissions("worldedit.brush.gravity")
    public void gravityBrush(Player player, LocalSession session,
                             @Arg(desc = "The radius to apply gravity in", def = "5")
                                 double radius,
                             @Switch(name = 'h', desc = "Affect blocks starting at max Y, rather than the target location Y + radius")
                                 boolean fromMaxY) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setSize(radius);
        tool.setBrush(new GravityBrush(fromMaxY), "worldedit.brush.gravity");

        player.print(String.format("Gravity brush equipped (%.0f).",
                radius));
    }

    @Command(
        name = "butcher",
        aliases = { "kill" },
        desc = "Butcher brush, kills mobs within a radius"
    )
    @CommandPermissions("worldedit.brush.butcher")
    public void butcherBrush(Player player, LocalSession session,
                             @Arg(desc = "Radius to kill mobs in", def = "5")
                                 double radius,
                             @Switch(name = 'p', desc = "Also kill pets")
                                 boolean killPets,
                             @Switch(name = 'n', desc = "Also kill NPCs")
                                 boolean killNpcs,
                             @Switch(name = 'g', desc = "Also kill golems")
                                 boolean killGolems,
                             @Switch(name = 'a', desc = "Also kill animals")
                                 boolean killAnimals,
                             @Switch(name = 'b', desc = "Also kill ambient mobs")
                                 boolean killAmbient,
                             @Switch(name = 't', desc = "Also kill mobs with name tags")
                                 boolean killWithName,
                             @Switch(name = 'f', desc = "Also kill all friendly mobs (Applies the flags `-abgnpt`)")
                                 boolean killFriendly,
                             @Switch(name = 'r', desc = "Also destroy armor stands")
                                 boolean killArmorStands) throws WorldEditException {
        LocalConfiguration config = worldEdit.getConfiguration();

        double maxRadius = config.maxBrushRadius;
        // hmmmm not horribly worried about this because -1 is still rather efficient,
        // the problem arises when butcherMaxRadius is some really high number but not infinite
        // - original idea taken from https://github.com/sk89q/worldedit/pull/198#issuecomment-6463108
        if (player.hasPermission("worldedit.butcher")) {
            maxRadius = Math.max(config.maxBrushRadius, config.butcherMaxRadius);
        }
        if (radius > maxRadius) {
            player.printError("Maximum allowed brush radius: " + maxRadius);
            return;
        }

        CreatureButcher flags = new CreatureButcher(player);
        flags.or(CreatureButcher.Flags.FRIENDLY      , killFriendly); // No permission check here. Flags will instead be filtered by the subsequent calls.
        flags.or(CreatureButcher.Flags.PETS          , killPets, "worldedit.butcher.pets");
        flags.or(CreatureButcher.Flags.NPCS          , killNpcs, "worldedit.butcher.npcs");
        flags.or(CreatureButcher.Flags.GOLEMS        , killGolems, "worldedit.butcher.golems");
        flags.or(CreatureButcher.Flags.ANIMALS       , killAnimals, "worldedit.butcher.animals");
        flags.or(CreatureButcher.Flags.AMBIENT       , killAmbient, "worldedit.butcher.ambient");
        flags.or(CreatureButcher.Flags.TAGGED        , killWithName, "worldedit.butcher.tagged");
        flags.or(CreatureButcher.Flags.ARMOR_STAND   , killArmorStands, "worldedit.butcher.armorstands");

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setSize(radius);
        tool.setBrush(new ButcherBrush(flags), "worldedit.brush.butcher");

        player.print(String.format("Butcher brush equipped (%.0f).", radius));
    }

    @Command(
        name = "deform",
        desc = "Deform brush, applies an expression to an area"
    )
    @CommandPermissions("worldedit.brush.deform")
    public void deform(Player player, LocalSession localSession,
                       @Arg(desc = "The shape of the region")
                           RegionFactory shape,
                       @Arg(desc = "The size of the brush", def = "5")
                           double radius,
                       @Arg(desc = "Expression to apply", def = "y-=0.2")
                           String expression,
                       @Switch(name = 'r', desc = "Use the game's coordinate origin")
                           boolean useRawCoords,
                       @Switch(name = 'o', desc = "Use the placement position as the origin")
                           boolean usePlacement) throws WorldEditException {
        Deform deform = new Deform(expression);
        if (useRawCoords) {
            deform.setMode(Deform.Mode.RAW_COORD);
        } else if (usePlacement) {
            deform.setMode(Deform.Mode.OFFSET);
            deform.setOffset(localSession.getPlacementPosition(player).toVector3());
        }
        setOperationBasedBrush(player, localSession, radius,
            deform, shape, "worldedit.brush.deform");
    }

    @Command(
        name = "set",
        desc = "Set brush, sets all blocks in the area"
    )
    @CommandPermissions("worldedit.brush.set")
    public void set(Player player, LocalSession localSession,
                    @Arg(desc = "The shape of the region")
                        RegionFactory shape,
                    @Arg(desc = "The size of the brush", def = "5")
                        double radius,
                    @Arg(desc = "The pattern of blocks to set")
                        Pattern pattern) throws WorldEditException {
        setOperationBasedBrush(player, localSession, radius,
            new Apply(new ReplaceFactory(pattern)), shape, "worldedit.brush.set");
    }

    @Command(
        name = "forest",
        desc = "Forest brush, creates a forest in the area"
    )
    @CommandPermissions("worldedit.brush.forest")
    public void forest(Player player, LocalSession localSession,
                       @Arg(desc = "The shape of the region")
                           RegionFactory shape,
                       @Arg(desc = "The size of the brush", def = "5")
                           double radius,
                       @Arg(desc = "The density of the brush", def = "20")
                           double density,
                       @Arg(desc = "The type of tree to use")
                           TreeGenerator.TreeType type) throws WorldEditException {
        setOperationBasedBrush(player, localSession, radius,
            new Paint(new TreeGeneratorFactory(type), density / 100), shape, "worldedit.brush.forest");
    }

    @Command(
        name = "raise",
        desc = "Raise brush, raise all blocks by one"
    )
    @CommandPermissions("worldedit.brush.raise")
    public void raise(Player player, LocalSession localSession,
                      @Arg(desc = "The shape of the region")
                          RegionFactory shape,
                      @Arg(desc = "The size of the brush", def = "5")
                          double radius) throws WorldEditException {
        setOperationBasedBrush(player, localSession, radius,
            new Deform("y-=1"), shape, "worldedit.brush.raise");
    }

    @Command(
        name = "lower",
        desc = "Lower brush, lower all blocks by one"
    )
    @CommandPermissions("worldedit.brush.lower")
    public void lower(Player player, LocalSession localSession,
                      @Arg(desc = "The shape of the region")
                          RegionFactory shape,
                      @Arg(desc = "The size of the brush", def = "5")
                          double radius) throws WorldEditException {
        setOperationBasedBrush(player, localSession, radius,
            new Deform("y+=1"), shape, "worldedit.brush.lower");
    }

    static void setOperationBasedBrush(Player player, LocalSession session, double radius,
                                        Contextual<? extends Operation> factory,
                                        RegionFactory shape,
                                        String permission) throws WorldEditException {
        WorldEdit.getInstance().checkMaxBrushRadius(radius);
        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setSize(radius);
        tool.setFill(null);
        tool.setBrush(new OperationFactoryBrush(factory, shape, session), permission);

        player.print("Set brush to " + factory);
    }
}
