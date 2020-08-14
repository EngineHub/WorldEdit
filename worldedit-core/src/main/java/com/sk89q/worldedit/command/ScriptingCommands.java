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
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.command.util.Logging.LogMode.ALL;

/**
 * Commands related to scripting.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class ScriptingCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public ScriptingCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "cs",
        desc = "Execute a CraftScript"
    )
    @CommandPermissions("worldedit.scripting.execute")
    @Logging(ALL)
    public void execute(Player player, LocalSession session,
                        @Arg(desc = "Filename of the CraftScript to load")
                            String filename,
                        @Arg(desc = "Arguments to the CraftScript", def = "", variable = true)
                            List<String> args) throws WorldEditException {
        if (!player.hasPermission("worldedit.scripting.execute." + filename)) {
            player.printError(TranslatableComponent.of("worldedit.execute.script-permissions"));
            return;
        }

        session.setLastScript(filename);

        File dir = worldEdit.getWorkingDirectoryFile(worldEdit.getConfiguration().scriptsDir);
        File f = worldEdit.getSafeOpenFile(player, dir, filename, "js", "js");

        worldEdit.runScript(player, f, Stream.concat(Stream.of(filename), args.stream())
            .toArray(String[]::new));
    }

    @Command(
        name = ".s",
        desc = "Execute last CraftScript"
    )
    @CommandPermissions("worldedit.scripting.execute")
    @Logging(ALL)
    public void executeLast(Player player, LocalSession session,
                            @Arg(desc = "Arguments to the CraftScript", def = "", variable = true)
                                List<String> args) throws WorldEditException {

        String lastScript = session.getLastScript();

        if (!player.hasPermission("worldedit.scripting.execute." + lastScript)) {
            player.printError(TranslatableComponent.of("worldedit.execute.script-permissions"));
            return;
        }

        if (lastScript == null) {
            player.printError(TranslatableComponent.of("worldedit.executelast.no-script"));
            return;
        }

        File dir = worldEdit.getWorkingDirectoryPath(worldEdit.getConfiguration().scriptsDir).toFile();
        File f = worldEdit.getSafeOpenFile(player, dir, lastScript, "js", "js");

        worldEdit.runScript(player, f, Stream.concat(Stream.of(lastScript), args.stream())
            .toArray(String[]::new));
    }
}
