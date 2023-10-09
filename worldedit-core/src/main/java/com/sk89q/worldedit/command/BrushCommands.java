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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.argument.HeightConverter;
import com.sk89q.worldedit.command.factory.FeatureGeneratorFactory;
import com.sk89q.worldedit.command.factory.ReplaceFactory;
import com.sk89q.worldedit.command.factory.TreeGeneratorFactory;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.command.tool.brush.ButcherBrush;
import com.sk89q.worldedit.command.tool.brush.ClipboardBrush;
import com.sk89q.worldedit.command.tool.brush.CylinderBrush;
import com.sk89q.worldedit.command.tool.brush.GravityBrush;
import com.sk89q.worldedit.command.tool.brush.HollowCylinderBrush;
import com.sk89q.worldedit.command.tool.brush.HollowSphereBrush;
import com.sk89q.worldedit.command.tool.brush.ImageHeightmapBrush;
import com.sk89q.worldedit.command.tool.brush.MorphBrush;
import com.sk89q.worldedit.command.tool.brush.OperationFactoryBrush;
import com.sk89q.worldedit.command.tool.brush.SmoothBrush;
import com.sk89q.worldedit.command.tool.brush.SnowSmoothBrush;
import com.sk89q.worldedit.command.tool.brush.SphereBrush;
import com.sk89q.worldedit.command.tool.brush.SplatterBrush;
import com.sk89q.worldedit.command.util.AsyncCommandBuilder;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.CreatureButcher;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.factory.ApplyLayer;
import com.sk89q.worldedit.function.factory.ApplyRegion;
import com.sk89q.worldedit.function.factory.BiomeFactory;
import com.sk89q.worldedit.function.factory.Deform;
import com.sk89q.worldedit.function.factory.Paint;
import com.sk89q.worldedit.function.factory.Snow;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.ClipboardMask;
import com.sk89q.worldedit.internal.annotation.VertHeight;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.CylinderRegionFactory;
import com.sk89q.worldedit.regions.factory.FixedHeightCuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.FixedHeightCylinderRegionFactory;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.regions.factory.SphereRegionFactory;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.session.request.RequestExtent;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.event.ClickEvent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.asset.AssetLoadTask;
import com.sk89q.worldedit.util.asset.AssetLoader;
import com.sk89q.worldedit.util.asset.holder.ImageHeightmap;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Commands to set brush shape.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class BrushCommands {

    private final WorldEdit worldEdit;

    private static final Component UNBIND_COMMAND_COMPONENT = Component.text("/brush unbind", NamedTextColor.AQUA)
                                                                   .clickEvent(ClickEvent.suggestCommand("/brush unbind"));

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
        name = "none",
        aliases = "unbind",
        desc = "Unbind a bound brush from your current item"
    )
    void none(Player player, LocalSession session) throws WorldEditException {
        ToolCommands.setToolNone(player, session, true);
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

        Brush brush = hollow ? new HollowSphereBrush() : new SphereBrush();

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            brush,
            "worldedit.brush.sphere"
        );
        tool.setFill(pattern);
        tool.setSize(radius);

        player.printInfo(Component.translatable("worldedit.brush.sphere.equip", Component.text(String.format("%.0f", radius))));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
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

        Brush brush = hollow ? new HollowCylinderBrush(height) : new CylinderBrush(height);

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            brush,
            "worldedit.brush.cylinder"
        );
        tool.setFill(pattern);
        tool.setSize(radius);

        player.printInfo(Component.translatable("worldedit.brush.cylinder.equip", Component.text((int) radius), Component.text(height)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }

    @Command(
        name = "splatter",
        aliases = { "splat" },
        desc = "Choose the splatter brush"
    )
    @CommandPermissions("worldedit.brush.splatter")
    public void splatterBrush(Player player, LocalSession session,
                              @Arg(desc = "The pattern of blocks to set")
                                  Pattern pattern,
                              @Arg(desc = "The radius of the splatter", def = "2")
                                  double radius,
                              @Arg(desc = "The decay of the splatter between 0 and 10", def = "1")
                                  int decay) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        if (decay < 0 || decay > 10) {
            player.printError(Component.translatable("worldedit.brush.splatter.decay-out-of-range", Component.text(decay)));
            return;
        }

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new SplatterBrush(decay),
            "worldedit.brush.splatter"
        );
        tool.setFill(pattern);
        tool.setSize(radius);

        player.printInfo(Component.translatable("worldedit.brush.splatter.equip", Component.text((int) radius), Component.text(decay)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
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
                               @Switch(name = 'v', desc = "Include structure void blocks")
                                   boolean pasteStructureVoid,
                               @Switch(name = 'o', desc = "Paste starting at the target location, instead of centering on it")
                                   boolean usingOrigin,
                               @Switch(name = 'e', desc = "Paste entities if available")
                                   boolean pasteEntities,
                               @Switch(name = 'b', desc = "Paste biomes if available")
                                   boolean pasteBiomes,
                               @ArgFlag(name = 'm', desc = "Skip blocks matching this mask in the clipboard")
                               @ClipboardMask
                                   Mask sourceMask) throws WorldEditException {
        ClipboardHolder holder = session.getClipboard();

        Clipboard clipboard = holder.getClipboard();
        ClipboardHolder newHolder = new ClipboardHolder(clipboard);
        newHolder.setTransform(holder.getTransform());

        BlockVector3 size = clipboard.getDimensions();

        worldEdit.checkMaxBrushRadius(size.x() / 2D - 1);
        worldEdit.checkMaxBrushRadius(size.y() / 2D - 1);
        worldEdit.checkMaxBrushRadius(size.z() / 2D - 1);

        session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new ClipboardBrush(newHolder, ignoreAir, !pasteStructureVoid, usingOrigin, pasteEntities, pasteBiomes, sourceMask),
            "worldedit.brush.clipboard"
        );

        player.printInfo(Component.translatable("worldedit.brush.clipboard.equip"));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
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

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new SmoothBrush(iterations, mask),
            "worldedit.brush.smooth"
        );
        tool.setSize(radius);

        player.printInfo(Component.translatable(
                "worldedit.brush.smooth.equip",
                Component.text((int) radius),
                Component.text(iterations),
                Component.translatable("worldedit.brush.smooth." + (mask == null ? "no" : "") + "filter")
        ));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }

    @Command(
        name = "snowsmooth",
        desc = "Choose the snow terrain softener brush",
        descFooter = "Example: '/brush snowsmooth 5 1 -l 3'"
    )
    @CommandPermissions("worldedit.brush.snowsmooth")
    public void snowSmoothBrush(Player player, LocalSession session,
                                @Arg(desc = "The radius to sample for softening", def = "2")
                                    double radius,
                                @Arg(desc = "The number of iterations to perform", def = "4")
                                    int iterations,
                                @ArgFlag(name = 'l', desc = "The number of snow blocks under snow", def = "1")
                                    int snowBlockCount,
                                @ArgFlag(name = 'm', desc = "The mask of blocks to use for the heightmap")
                                    Mask mask) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new SnowSmoothBrush(iterations, snowBlockCount, mask),
            "worldedit.brush.snowsmooth"
        );
        tool.setSize(radius);

        player.printInfo(Component.translatable(
                "worldedit.brush.snowsmooth.equip",
                Component.text((int) radius),
                Component.text(iterations),
                Component.translatable("worldedit.brush.snowsmooth." + (mask == null ? "no" : "") + "filter"),
                Component.text(snowBlockCount)
        ));
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

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new SphereBrush(),
            "worldedit.brush.ex"
        );
        tool.setFill(BlockTypes.AIR.getDefaultState());
        tool.setSize(radius);
        tool.setMask(new BlockTypeMask(new RequestExtent(), BlockTypes.FIRE));

        player.printInfo(Component.translatable("worldedit.brush.extinguish.equip", Component.text((int) radius)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
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
                             @ArgFlag(
                                 name = 'h',
                                 desc = "Affect blocks between the given height, "
                                     + "upwards and downwards, "
                                     + "rather than the target location Y + radius",
                                 def = HeightConverter.DEFAULT_VALUE
                             )
                             @VertHeight
                                 Integer height) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new GravityBrush(height),
            "worldedit.brush.gravity"
        );
        tool.setSize(radius);

        player.printInfo(Component.translatable("worldedit.brush.gravity.equip", Component.text((int) radius)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
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
                                 boolean killArmorStands,
                             @Switch(name = 'w', desc = "Also kill water mobs")
                                 boolean killWater) throws WorldEditException {
        LocalConfiguration config = worldEdit.getConfiguration();

        double maxRadius = config.maxBrushRadius;
        // hmmmm not horribly worried about this because -1 is still rather efficient,
        // the problem arises when butcherMaxRadius is some really high number but not infinite
        // - original idea taken from https://github.com/sk89q/worldedit/pull/198#issuecomment-6463108
        if (player.hasPermission("worldedit.butcher")) {
            maxRadius = Math.max(config.maxBrushRadius, config.butcherMaxRadius);
        }
        if (radius > maxRadius) {
            player.printError(Component.translatable("worldedit.brush.radius-too-large", Component.text(maxRadius)));
            return;
        }

        CreatureButcher flags = new CreatureButcher(player);
        flags.or(CreatureButcher.Flags.FRIENDLY, killFriendly); // No permission check here. Flags will instead be filtered by the subsequent calls.
        flags.or(CreatureButcher.Flags.PETS, killPets, "worldedit.butcher.pets");
        flags.or(CreatureButcher.Flags.NPCS, killNpcs, "worldedit.butcher.npcs");
        flags.or(CreatureButcher.Flags.GOLEMS, killGolems, "worldedit.butcher.golems");
        flags.or(CreatureButcher.Flags.ANIMALS, killAnimals, "worldedit.butcher.animals");
        flags.or(CreatureButcher.Flags.AMBIENT, killAmbient, "worldedit.butcher.ambient");
        flags.or(CreatureButcher.Flags.TAGGED, killWithName, "worldedit.butcher.tagged");
        flags.or(CreatureButcher.Flags.ARMOR_STAND, killArmorStands, "worldedit.butcher.armorstands");
        flags.or(CreatureButcher.Flags.WATER, killWater, "worldedit.butcher.water");

        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new ButcherBrush(flags), "worldedit.brush.butcher"
        );
        tool.setSize(radius);

        player.printInfo(Component.translatable("worldedit.brush.butcher.equip", Component.text((int) radius)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }

    @Command(
        name = "heightmap",
        desc = "Heightmap brush, raises or lowers terrain using an image heightmap"
    )
    @CommandPermissions("worldedit.brush.heightmap")
    void heightmapBrush(Player player, LocalSession session,
                    @Arg(desc = "The name of the image")
                        String imageName,
                    @Arg(desc = "The size of the brush", def = "5")
                        double radius,
                    @Arg(desc = "The intensity of the brush", def = "5")
                        double intensity,
                    @Switch(name = 'e', desc = "Erase blocks instead of filling them")
                        boolean erase,
                    @Switch(name = 'f', desc = "Don't change blocks above the selected height")
                        boolean flatten,
                    @Switch(name = 'r', desc = "Randomizes the brush's height slightly.")
                        boolean randomize) throws WorldEditException {
        Optional<AssetLoader<ImageHeightmap>> loader = worldEdit.getAssetLoaders().getAssetLoader(ImageHeightmap.class, imageName);

        if (loader.isPresent()) {
            worldEdit.checkMaxBrushRadius(radius);

            AssetLoadTask<ImageHeightmap> task = new AssetLoadTask<>(loader.get(), imageName);
            AsyncCommandBuilder.wrap(task, player)
                .registerWithSupervisor(worldEdit.getSupervisor(), "Loading asset " + imageName)
                .setDelayMessage(Component.translatable("worldedit.asset.load.loading"))
                .setWorkingMessage(Component.translatable("worldedit.asset.load.still-loading"))
                .onSuccess(Component.translatable("worldedit.brush.heightmap.equip", Component.text((int) radius)), heightmap -> {
                    BrushTool tool;
                    try {
                        tool = session.forceBrush(
                            player.getItemInHand(HandSide.MAIN_HAND).getType(),
                            new ImageHeightmapBrush(heightmap, intensity, erase, flatten, randomize),
                            "worldedit.brush.heightmap"
                        );
                    } catch (InvalidToolBindException e) {
                        throw new RuntimeException(e);
                    }
                    tool.setSize(radius);
                    ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
                })
                .onFailure(Component.translatable("worldedit.asset.load.failed"), worldEdit.getPlatformManager().getPlatformCommandManager().getExceptionConverter())
                .buildAndExec(worldEdit.getExecutorService());
        } else {
            player.printError(Component.translatable("worldedit.brush.heightmap.unknown", Component.text(imageName)));
        }
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
            new ApplyRegion(new ReplaceFactory(pattern)), shape, "worldedit.brush.set");
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
        name = "feature",
        desc = "Feature brush, paints Minecraft generation features"
    )
    @CommandPermissions("worldedit.brush.feature")
    public void feature(Player player, LocalSession localSession,
                       @Arg(desc = "The shape of the region")
                       RegionFactory shape,
                       @Arg(desc = "The size of the brush", def = "5")
                       double radius,
                       @Arg(desc = "The density of the brush", def = "5")
                       double density,
                       @Arg(desc = "The type of feature to use")
                       ConfiguredFeatureType type) throws WorldEditException {
        setOperationBasedBrush(player, localSession, radius,
            new ApplyRegion(new FeatureGeneratorFactory(type, density / 100)), shape, "worldedit.brush.feature");
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
            new Deform("y-=1", Deform.Mode.RAW_COORD), shape, "worldedit.brush.raise");
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
            new Deform("y+=1", Deform.Mode.RAW_COORD), shape, "worldedit.brush.lower");
    }

    @Command(
        name = "snow",
        desc = "Snow brush, sets snow in the area"
    )
    @CommandPermissions("worldedit.brush.snow")
    public void snow(Player player, LocalSession localSession,
                     @Arg(desc = "The shape of the region")
                         RegionFactory shape,
                     @Arg(desc = "The size of the brush", def = "5")
                         double radius,
                     @Switch(name = 's', desc = "Whether to stack snow")
                         boolean stack) throws WorldEditException {

        if (shape instanceof CylinderRegionFactory) {
            shape = new CylinderRegionFactory(radius);
        }

        setOperationBasedBrush(player, localSession, radius,
            new ApplyLayer(new Snow(stack)), shape, "worldedit.brush.snow");
    }

    @Command(
        name = "biome",
        desc = "Biome brush, sets biomes in the area"
    )
    @CommandPermissions("worldedit.brush.biome")
    public void biome(Player player, LocalSession localSession,
                      @Arg(desc = "The shape of the region")
                          RegionFactory shape,
                      @Arg(desc = "The size of the brush", def = "5")
                          double radius,
                      @Arg(desc = "The biome type")
                          BiomeType biomeType,
                      @Switch(name = 'c', desc = "Whether to set the full column")
                          boolean column) throws WorldEditException {

        if (column) {
            // Convert this shape factory to a column-based one, if possible
            if (shape instanceof CylinderRegionFactory || shape instanceof SphereRegionFactory) {
                // Sphere regions that are Y-expended are just cylinders
                shape = new FixedHeightCylinderRegionFactory(player.getWorld().getMinY(), player.getWorld().getMaxY());
            } else if (shape instanceof CuboidRegionFactory) {
                shape = new FixedHeightCuboidRegionFactory(player.getWorld().getMinY(), player.getWorld().getMaxY());
            } else {
                player.printError(Component.translatable("worldedit.brush.biome.column-supported-types"));
                return;
            }
        }

        setOperationBasedBrush(player, localSession, radius,
            new ApplyRegion(new BiomeFactory(biomeType)), shape, "worldedit.brush.biome");
        player.printInfo(Component.translatable("worldedit.setbiome.warning"));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }

    @Command(
        name = "morph",
        desc = "Morph brush, morphs blocks in the area"
    )
    @CommandPermissions("worldedit.brush.morph")
    public void morph(Player player, LocalSession session,
                      @Arg(desc = "The size of the brush", def = "5")
                          double brushSize,
                      @Arg(desc = "Minimum number of faces for erosion", def = "3")
                          int minErodeFaces,
                      @Arg(desc = "Erode iterations", def = "1")
                          int numErodeIterations,
                      @Arg(desc = "Minimum number of faces for dilation", def = "3")
                          int minDilateFaces,
                      @Arg(desc = "Dilate iterations", def = "1")
                          int numDilateIterations) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(brushSize);
        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new MorphBrush(minErodeFaces, numErodeIterations, minDilateFaces, numDilateIterations),
            "worldedit.brush.morph"
        );
        tool.setSize(brushSize);

        player.printInfo(Component.translatable("worldedit.brush.morph.equip", Component.text((int) brushSize)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }

    @Command(
        name = "erode",
        desc = "Erode preset for morph brush, erodes blocks in the area"
    )
    @CommandPermissions("worldedit.brush.morph")
    public void erode(Player player, LocalSession session,
                      @Arg(desc = "The size of the brush", def = "5")
                          double brushSize) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(brushSize);
        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new MorphBrush(2, 1, 5, 1),
            "worldedit.brush.morph"
        );
        tool.setSize(brushSize);

        player.printInfo(Component.translatable("worldedit.brush.morph.equip", Component.text((int) brushSize)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }

    @Command(
        name = "dilate",
        desc = "Dilate preset for morph brush, dilates blocks in the area"
    )
    @CommandPermissions("worldedit.brush.morph")
    public void dilate(Player player, LocalSession session,
                       @Arg(desc = "The size of the brush", def = "5")
                           double brushSize) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(brushSize);
        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new MorphBrush(5, 1, 2, 1),
            "worldedit.brush.morph"
        );
        tool.setSize(brushSize);

        player.printInfo(Component.translatable("worldedit.brush.morph.equip", Component.text((int) brushSize)));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }

    static void setOperationBasedBrush(Player player, LocalSession session, double radius,
                                        Contextual<? extends Operation> factory,
                                        RegionFactory shape,
                                        String permission) throws WorldEditException {
        WorldEdit.getInstance().checkMaxBrushRadius(radius);
        BrushTool tool = session.forceBrush(
            player.getItemInHand(HandSide.MAIN_HAND).getType(),
            new OperationFactoryBrush(factory, shape, session),
            permission
        );
        tool.setSize(radius);
        tool.setFill(null);

        player.printInfo(Component.translatable("worldedit.brush.operation.equip", Component.text(factory.toString())));
        ToolCommands.sendUnbindInstruction(player, UNBIND_COMMAND_COMPONENT);
    }
}
