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

package com.sk89q.worldedit.command.argument;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.command.composition.SimpleCommand;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.world.World;

public class PatternParser extends SimpleCommand<Pattern> {

    private final StringParser stringParser;

    public PatternParser(String name) {
        stringParser = addParameter(new StringParser(name, "The pattern"));
    }

    @Override
    public Pattern call(CommandArgs args, CommandLocals locals) throws CommandException {
        String patternString = stringParser.call(args, locals);

        Actor actor = locals.get(Actor.class);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);

        ParserContext parserContext = new ParserContext();
        parserContext.setActor(actor);
        if (actor instanceof Entity) {
            Extent extent = ((Entity) actor).getExtent();
            if (extent instanceof World) {
                parserContext.setWorld((World) extent);
            }
        }
        parserContext.setSession(session);

        try {
            return WorldEdit.getInstance().getPatternFactory().parseFromInput(patternString, parserContext);
        } catch (InputParseException e) {
            throw new CommandException(e.getMessage(), e);
        }
    }

    @Override
    public String getDescription() {
        return "Choose a pattern";
    }

    @Override
    public boolean testPermission0(CommandLocals locals) {
        return true;
    }

}
