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
import com.sk89q.worldedit.command.tool.AreaPickaxe;
import com.sk89q.worldedit.command.tool.RecursivePickaxe;
import com.sk89q.worldedit.command.tool.SinglePickaxe;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class SuperPickaxeCommands {
    private final WorldEdit we;

    public SuperPickaxeCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        name = "single",
        desc = "Enable the single block super pickaxe mode"
    )
    @CommandPermissions("worldedit.superpickaxe")
    public void single(Player player, LocalSession session) throws WorldEditException {
        session.setSuperPickaxe(new SinglePickaxe());
        session.enableSuperPickAxe();
        player.printInfo(TranslatableComponent.of("worldedit.tool.superpickaxe.mode.single"));
    }

    @Command(
        name = "area",
        desc = "Enable the area super pickaxe pickaxe mode"
    )
    @CommandPermissions("worldedit.superpickaxe.area")
    public void area(Player player, LocalSession session,
                     @Arg(desc = "The range of the area pickaxe")
                         int range) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (range > config.maxSuperPickaxeSize) {
            player.printError(TranslatableComponent.of("worldedit.tool.superpickaxe.max-range", TextComponent.of(config.maxSuperPickaxeSize)));
            return;
        }

        session.setSuperPickaxe(new AreaPickaxe(range));
        session.enableSuperPickAxe();
        player.printInfo(TranslatableComponent.of("worldedit.tool.superpickaxe.mode.area"));
    }

    @Command(
        name = "recursive",
        aliases = { "recur" },
        desc = "Enable the recursive super pickaxe pickaxe mode"
    )
    @CommandPermissions("worldedit.superpickaxe.recursive")
    public void recursive(Player player, LocalSession session,
                          @Arg(desc = "The range of the recursive pickaxe")
                              double range) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();

        if (range > config.maxSuperPickaxeSize) {
            player.printError(TranslatableComponent.of("worldedit.tool.superpickaxe.max-range", TextComponent.of(config.maxSuperPickaxeSize)));
            return;
        }

        session.setSuperPickaxe(new RecursivePickaxe(range));
        session.enableSuperPickAxe();
        player.printInfo(TranslatableComponent.of("worldedit.tool.superpickaxe.mode.recursive"));
    }
}
