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

package com.sk89q.worldedit.command.argument;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.io.file.FilenameException;
import com.sk89q.worldedit.util.schematic.SchematicPath;
import com.sk89q.worldedit.util.schematic.SchematicsManager;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.enginehub.piston.converter.SuggestionHelper.limitByPrefix;

public class SchematicConverter implements ArgumentConverter<SchematicPath> {

    public static void register(WorldEdit worldEdit, CommandManager commandManager) {
        commandManager.registerConverter(Key.of(SchematicPath.class), new SchematicConverter(worldEdit));
    }

    private final WorldEdit worldEdit;

    private SchematicConverter(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    private final TextComponent choices = TextComponent.of("schematic filename");

    @Override
    public Component describeAcceptableArguments() {
        return choices;
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        SchematicsManager schematicsManager = worldEdit.getSchematicsManager();
        Path schematicsRootPath = schematicsManager.getRoot();

        return limitByPrefix(schematicsManager.getList().stream()
                .map(s -> schematicsRootPath.relativize(s.path()).toString()), input);
    }

    @Override
    public ConversionResult<SchematicPath> convert(String s, InjectedValueAccess injectedValueAccess) {
        Path schematicsRoot = worldEdit.getSchematicsManager().getRoot();
        // resolve as subpath of schematicsRoot
        Path schematicPath = schematicsRoot.resolve(s).toAbsolutePath();
        // then check whether it is still a subpath to rule out "../"
        if (!schematicPath.startsWith(schematicsRoot)) {
            return FailedConversion.from(new FilenameException(s));
        }
        // check whether the file exists
        if (Files.exists(schematicPath)) {
            // continue as relative path to schematicsRoot
            schematicPath = schematicsRoot.relativize(schematicPath);
            return SuccessfulConversion.fromSingle(new SchematicPath(schematicPath));
        } else {
            return FailedConversion.from(new FilenameException(s));
        }
    }
}
