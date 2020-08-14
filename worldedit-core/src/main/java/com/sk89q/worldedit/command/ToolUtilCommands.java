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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

/**
 * Tool commands.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class ToolUtilCommands {
    private final WorldEdit we;

    public ToolUtilCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        name = "/",
        aliases = { "," },
        desc = "Toggle the super pickaxe function"
    )
    @CommandPermissions("worldedit.superpickaxe")
    public void togglePickaxe(Player player, LocalSession session,
                              @Arg(desc = "The new super pickaxe state", def = "")
                                  Boolean superPickaxe) {
        boolean hasSuperPickAxe = session.hasSuperPickAxe();
        if (superPickaxe != null && superPickaxe == hasSuperPickAxe) {
            player.printError(TranslatableComponent.of(superPickaxe ? "worldedit.tool.superpickaxe.enabled.already" : "worldedit.tool.superpickaxe.disabled.already"));
            return;
        }
        if (hasSuperPickAxe) {
            session.disableSuperPickAxe();
            player.printInfo(TranslatableComponent.of("worldedit.tool.superpickaxe.disabled"));
        } else {
            session.enableSuperPickAxe();
            player.printInfo(TranslatableComponent.of("worldedit.tool.superpickaxe.enabled"));
        }

    }

    @Command(
        name = "mask",
        desc = "Set the brush mask"
    )
    @CommandPermissions("worldedit.brush.options.mask")
    public void mask(Player player, LocalSession session,
                     @Arg(desc = "The mask to set", def = "")
                         Mask mask) throws WorldEditException {
        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setMask(mask);
        if (mask == null) {
            player.printInfo(TranslatableComponent.of("worldedit.tool.mask.disabled"));
        } else {
            player.printInfo(TranslatableComponent.of("worldedit.tool.mask.set"));
        }
    }

    @Command(
        name = "material",
        aliases = { "/material" },
        desc = "Set the brush material"
    )
    @CommandPermissions("worldedit.brush.options.material")
    public void material(Player player, LocalSession session,
                         @Arg(desc = "The pattern of blocks to use")
                             Pattern pattern) throws WorldEditException {
        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setFill(pattern);
        player.printInfo(TranslatableComponent.of("worldedit.tool.material.set"));
    }

    @Command(
        name = "range",
        desc = "Set the brush range"
    )
    @CommandPermissions("worldedit.brush.options.range")
    public void range(Player player, LocalSession session,
                      @Arg(desc = "The range of the brush")
                          int range) throws WorldEditException {
        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setRange(range);
        player.printInfo(TranslatableComponent.of("worldedit.tool.range.set"));
    }

    @Command(
        name = "size",
        desc = "Set the brush size"
    )
    @CommandPermissions("worldedit.brush.options.size")
    public void size(Player player, LocalSession session,
                     @Arg(desc = "The size of the brush")
                         int size) throws WorldEditException {
        we.checkMaxBrushRadius(size);

        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setSize(size);
        player.printInfo(TranslatableComponent.of("worldedit.tool.size.set"));
    }

    @Command(
        name = "tracemask",
        desc = "Set the mask used to stop tool traces"
    )
    @CommandPermissions("worldedit.brush.options.tracemask")
    public void traceMask(Player player, LocalSession session,
                          @Arg(desc = "The trace mask to set", def = "")
                             Mask mask) throws WorldEditException {
        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setTraceMask(mask);
        if (mask == null) {
            player.printInfo(TranslatableComponent.of("worldedit.tool.tracemask.disabled"));
        } else {
            player.printInfo(TranslatableComponent.of("worldedit.tool.tracemask.set"));
        }
    }
}
