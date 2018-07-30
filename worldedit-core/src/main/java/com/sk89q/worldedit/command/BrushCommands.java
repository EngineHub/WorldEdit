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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.brush.ButcherBrush;
import com.sk89q.worldedit.command.tool.brush.ClipboardBrush;
import com.sk89q.worldedit.command.tool.brush.CylinderBrush;
import com.sk89q.worldedit.command.tool.brush.GravityBrush;
import com.sk89q.worldedit.command.tool.brush.HollowCylinderBrush;
import com.sk89q.worldedit.command.tool.brush.HollowSphereBrush;
import com.sk89q.worldedit.command.tool.brush.SmoothBrush;
import com.sk89q.worldedit.command.tool.brush.SphereBrush;
import com.sk89q.worldedit.command.util.CreatureButcher;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.parametric.Optional;
import com.sk89q.worldedit.world.block.BlockTypes;

/**
 * Commands to set brush shape.
 */
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
        aliases = { "sphere", "s" },
        usage = "<pattern> [radius]",
        flags = "h",
        desc = "Choose the sphere brush",
        help =
            "Chooses the sphere brush.\n" +
            "The -h flag creates hollow spheres instead.",
        min = 1,
        max = 2
    )
    @CommandPermissions("worldedit.brush.sphere")
    public void sphereBrush(Player player, LocalSession session, EditSession editSession, Pattern fill,
                            @Optional("2") double radius, @Switch('h') boolean hollow) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setFill(fill);
        tool.setSize(radius);

        if (hollow) {
            tool.setBrush(new HollowSphereBrush(), "worldedit.brush.sphere");
        } else {
            tool.setBrush(new SphereBrush(), "worldedit.brush.sphere");
        }

        player.print(String.format("Sphere brush shape equipped (%.0f).", radius));
    }

    @Command(
        aliases = { "cylinder", "cyl", "c" },
        usage = "<block> [radius] [height]",
        flags = "h",
        desc = "Choose the cylinder brush",
        help =
            "Chooses the cylinder brush.\n" +
            "The -h flag creates hollow cylinders instead.",
        min = 1,
        max = 3
    )
    @CommandPermissions("worldedit.brush.cylinder")
    public void cylinderBrush(Player player, LocalSession session, EditSession editSession, Pattern fill,
                              @Optional("2") double radius, @Optional("1") int height, @Switch('h') boolean hollow) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);
        worldEdit.checkMaxBrushRadius(height);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setFill(fill);
        tool.setSize(radius);

        if (hollow) {
            tool.setBrush(new HollowCylinderBrush(height), "worldedit.brush.cylinder");
        } else {
            tool.setBrush(new CylinderBrush(height), "worldedit.brush.cylinder");
        }

        player.print(String.format("Cylinder brush shape equipped (%.0f by %d).", radius, height));
    }

    @Command(
        aliases = { "clipboard", "copy" },
        usage = "",
        desc = "Choose the clipboard brush",
        help =
            "Chooses the clipboard brush.\n" +
            "The -a flag makes it not paste air.\n" +
            "Without the -p flag, the paste will appear centered at the target location. " +
            "With the flag, then the paste will appear relative to where you had " +
            "stood relative to the copied area when you copied it."
    )
    @CommandPermissions("worldedit.brush.clipboard")
    public void clipboardBrush(Player player, LocalSession session, EditSession editSession, @Switch('a') boolean ignoreAir, @Switch('p') boolean usingOrigin) throws WorldEditException {
        ClipboardHolder holder = session.getClipboard();
        Clipboard clipboard = holder.getClipboard();

        Vector size = clipboard.getDimensions();

        worldEdit.checkMaxBrushRadius(size.getBlockX());
        worldEdit.checkMaxBrushRadius(size.getBlockY());
        worldEdit.checkMaxBrushRadius(size.getBlockZ());

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setBrush(new ClipboardBrush(holder, ignoreAir, usingOrigin), "worldedit.brush.clipboard");

        player.print("Clipboard brush shape equipped.");
    }

    @Command(
        aliases = { "smooth" },
        usage = "[size] [iterations]",
        desc = "Choose the terrain softener brush",
        help =
            "Chooses the terrain softener brush.",
        min = 0,
        max = 2
    )
    @CommandPermissions("worldedit.brush.smooth")
    public void smoothBrush(Player player, LocalSession session, EditSession editSession,
                            @Optional("2") double radius, @Optional("4") int iterations) throws WorldEditException {

        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setSize(radius);
        tool.setBrush(new SmoothBrush(iterations), "worldedit.brush.smooth");

        player.print(String.format("Smooth brush equipped (%.0f x %dx, using any block).", radius, iterations));
    }

    @Command(
        aliases = { "ex", "extinguish" },
        usage = "[radius]",
        desc = "Shortcut fire extinguisher brush",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.brush.ex")
    public void extinguishBrush(Player player, LocalSession session, EditSession editSession, @Optional("5") double radius) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        Pattern fill = new BlockPattern(BlockTypes.AIR.getDefaultState());
        tool.setFill(fill);
        tool.setSize(radius);
        tool.setMask(new BlockMask(editSession, BlockTypes.FIRE.getDefaultState().toFuzzy()));
        tool.setBrush(new SphereBrush(), "worldedit.brush.ex");

        player.print(String.format("Extinguisher equipped (%.0f).", radius));
    }

    @Command(
            aliases = { "gravity", "grav" },
            usage = "[radius]",
            flags = "h",
            desc = "Gravity brush",
            help =
                "This brush simulates the affect of gravity.\n" +
                "The -h flag makes it affect blocks starting at the world's max y, " +
                    "instead of the clicked block's y + radius.",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.brush.gravity")
    public void gravityBrush(Player player, LocalSession session, EditSession editSession, @Optional("5") double radius, @Switch('h') boolean fromMaxY) throws WorldEditException {
        worldEdit.checkMaxBrushRadius(radius);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setSize(radius);
        tool.setBrush(new GravityBrush(fromMaxY), "worldedit.brush.gravity");

        player.print(String.format("Gravity brush equipped (%.0f).",
                radius));
    }
    
    @Command(
            aliases = { "butcher", "kill" },
            usage = "[radius]",
            flags = "plangbtfr",
            desc = "Butcher brush",
            help = "Kills nearby mobs within the specified radius.\n" +
                    "Flags:\n" +
                    "  -p also kills pets.\n" +
                    "  -n also kills NPCs.\n" +
                    "  -g also kills Golems.\n" +
                    "  -a also kills animals.\n" +
                    "  -b also kills ambient mobs.\n" +
                    "  -t also kills mobs with name tags.\n" +
                    "  -f compounds all previous flags.\n" +
                    "  -r also destroys armor stands.\n" +
                    "  -l currently does nothing.",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.brush.butcher")
    public void butcherBrush(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        LocalConfiguration config = worldEdit.getConfiguration();

        double radius = args.argsLength() > 0 ? args.getDouble(0) : 5;
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
        flags.fromCommand(args);

        BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
        tool.setSize(radius);
        tool.setBrush(new ButcherBrush(flags), "worldedit.brush.butcher");

        player.print(String.format("Butcher brush equipped (%.0f).", radius));
    }
}
