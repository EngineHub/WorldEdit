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

import com.google.common.collect.Lists;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.util.List;

import static com.sk89q.worldedit.command.util.Logging.LogMode.PLACEMENT;
import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;

/**
 * Clipboard commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class ClipboardCommands {

    @Command(
        name = "/copy",
        desc = "Copy the selection to the clipboard"
    )
    @CommandPermissions("worldedit.clipboard.copy")
    public void copy(Player player, LocalSession session, EditSession editSession,
                     @Selection Region region,
                     @Switch(name = 'e', desc = "Also copy entities")
                         boolean copyEntities,
                     @Switch(name = 'b', desc = "Also copy biomes")
                         boolean copyBiomes,
                     @ArgFlag(name = 'm', desc = "Set the include mask, non-matching blocks become air", def = "")
                         Mask mask) throws WorldEditException {
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(session.getPlacementPosition(player));
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setCopyingEntities(copyEntities);
        copy.setCopyingBiomes(copyBiomes);
        if (mask != null) {
            copy.setSourceMask(mask);
        }
        Operations.completeLegacy(copy);
        session.setClipboard(new ClipboardHolder(clipboard));

        List<String> messages = Lists.newArrayList();
        copy.addStatusMessages(messages);
        messages.forEach(player::print);
    }

    @Command(
        name = "/cut",
        desc = "Cut the selection to the clipboard"
    )
    @CommandPermissions("worldedit.clipboard.cut")
    @Logging(REGION)
    public void cut(Player player, LocalSession session, EditSession editSession,
                    @Selection Region region,
                    @Arg(desc = "Pattern to leave in place of the selection", def = "air")
                        Pattern leavePattern,
                    @Switch(name = 'e', desc = "Also cut entities")
                        boolean copyEntities,
                    @Switch(name = 'b', desc = "Also copy biomes, source biomes are unaffected")
                        boolean copyBiomes,
                    @ArgFlag(name = 'm', desc = "Set the exclude mask, non-matching blocks become air", def = "")
                        Mask mask) throws WorldEditException {

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(session.getPlacementPosition(player));
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setSourceFunction(new BlockReplace(editSession, leavePattern));
        copy.setCopyingEntities(copyEntities);
        copy.setRemovingEntities(true);
        copy.setCopyingBiomes(copyBiomes);
        if (mask != null) {
            copy.setSourceMask(mask);
        }
        Operations.completeLegacy(copy);
        session.setClipboard(new ClipboardHolder(clipboard));

        List<String> messages = Lists.newArrayList();
        copy.addStatusMessages(messages);
        messages.forEach(player::print);
    }

    @Command(
        name = "/paste",
        desc = "Paste the clipboard's contents"
    )
    @CommandPermissions("worldedit.clipboard.paste")
    @Logging(PLACEMENT)
    public void paste(Player player, LocalSession session, EditSession editSession,
                      @Switch(name = 'a', desc = "Skip air blocks")
                          boolean ignoreAirBlocks,
                      @Switch(name = 'o', desc = "Paste at the original position")
                          boolean atOrigin,
                      @Switch(name = 's', desc = "Select the region after pasting")
                          boolean selectPasted,
                      @Switch(name = 'e', desc = "Paste entities if available")
                          boolean pasteEntities,
                      @Switch(name = 'b', desc = "Paste biomes if available")
                          boolean pasteBiomes,
                      @ArgFlag(name = 'm', desc = "Only paste blocks matching this mask", def = "")
                          Mask sourceMask) throws WorldEditException {

        ClipboardHolder holder = session.getClipboard();
        Clipboard clipboard = holder.getClipboard();
        Region region = clipboard.getRegion();

        BlockVector3 to = atOrigin ? clipboard.getOrigin() : session.getPlacementPosition(player);
        Operation operation = holder
                .createPaste(editSession)
                .to(to)
                .ignoreAirBlocks(ignoreAirBlocks)
                .copyBiomes(pasteBiomes)
                .copyEntities(pasteEntities)
                .maskSource(sourceMask)
                .build();
        Operations.completeLegacy(operation);

        if (selectPasted) {
            BlockVector3 clipboardOffset = clipboard.getRegion().getMinimumPoint().subtract(clipboard.getOrigin());
            Vector3 realTo = to.toVector3().add(holder.getTransform().apply(clipboardOffset.toVector3()));
            Vector3 max = realTo.add(holder.getTransform().apply(region.getMaximumPoint().subtract(region.getMinimumPoint()).toVector3()));
            RegionSelector selector = new CuboidRegionSelector(player.getWorld(), realTo.toBlockPoint(), max.toBlockPoint());
            session.setRegionSelector(player.getWorld(), selector);
            selector.learnChanges();
            selector.explainRegionAdjust(player, session);
        }

        player.print("The clipboard has been pasted at " + to);
        List<String> messages = Lists.newArrayList();
        operation.addStatusMessages(messages);
        messages.forEach(player::print);
    }

    @Command(
        name = "/rotate",
        desc = "Rotate the contents of the clipboard",
        descFooter = "Non-destructively rotate the contents of the clipboard.\n" +
            "Angles are provided in degrees and a positive angle will result in a clockwise rotation. " +
            "Multiple rotations can be stacked. Interpolation is not performed so angles should be a multiple of 90 degrees.\n"
    )
    @CommandPermissions("worldedit.clipboard.rotate")
    public void rotate(Player player, LocalSession session,
                       @Arg(desc = "Amount to rotate on the y-axis")
                           double yRotate,
                       @Arg(desc = "Amount to rotate on the x-axis", def = "0")
                           double xRotate,
                       @Arg(desc = "Amount to rotate on the z-axis", def = "0")
                           double zRotate) throws WorldEditException {
        if (Math.abs(yRotate % 90) > 0.001 ||
            Math.abs(xRotate % 90) > 0.001 ||
            Math.abs(zRotate % 90) > 0.001) {
            player.printDebug("Note: Interpolation is not yet supported, so angles that are multiples of 90 is recommended.");
        }

        ClipboardHolder holder = session.getClipboard();
        AffineTransform transform = new AffineTransform();
        transform = transform.rotateY(-yRotate);
        transform = transform.rotateX(-xRotate);
        transform = transform.rotateZ(-zRotate);
        holder.setTransform(holder.getTransform().combine(transform));
        player.print("The clipboard copy has been rotated.");
    }

    @Command(
        name = "/flip",
        desc = "Flip the contents of the clipboard across the origin"
    )
    @CommandPermissions("worldedit.clipboard.flip")
    public void flip(Player player, LocalSession session,
                     @Arg(desc = "The direction to flip, defaults to look direction.", def = Direction.AIM)
                     @Direction BlockVector3 direction) throws WorldEditException {
        ClipboardHolder holder = session.getClipboard();
        AffineTransform transform = new AffineTransform();
        transform = transform.scale(direction.abs().multiply(-2).add(1, 1, 1).toVector3());
        holder.setTransform(holder.getTransform().combine(transform));
        player.print("The clipboard copy has been flipped.");
    }

    @Command(
        name = "clearclipboard",
        desc = "Clear your clipboard"
    )
    @CommandPermissions("worldedit.clipboard.clear")
    public void clearClipboard(Player player, LocalSession session) throws WorldEditException {
        session.setClipboard(null);
        player.print("Clipboard cleared.");
    }
}
