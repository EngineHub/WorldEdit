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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;

public class DefaultItemParser extends InputParser<BaseItem> {

    protected DefaultItemParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public BaseItem parseFromInput(String input, ParserContext context) throws InputParseException {
        String[] tokens = input.split(":", 3);
        BaseItem item;
        short damage = 0;

        try {
            int id = Integer.parseInt(tokens[0]);

            // Parse metadata
            if (tokens.length == 2) {
                try {
                    damage = Short.parseShort(tokens[1]);
                } catch (NumberFormatException ignored) {
                    throw new InputParseException("Expected '" + tokens[1] + "' to be a damage value but it's not a number");
                }
            }

            item = context.requireWorld().getWorldData().getItemRegistry().createFromId(id);
        } catch (NumberFormatException e) {
            String name = tokens[0];
            if (input.length() >= 2) {
                name += ":" + tokens[1];
            }

            // Parse metadata
            if (tokens.length == 3) {
                try {
                    damage = Short.parseShort(tokens[2]);
                } catch (NumberFormatException ignored) {
                    throw new InputParseException("Expected '" + tokens[2] + "' to be a damage value but it's not a number");
                }
            }

            item = context.requireWorld().getWorldData().getItemRegistry().createFromId(name);
        }

        if (item == null) {
            throw new InputParseException("'" + input + "' did not match any item");
        } else {
            item.setDamage(damage);
            return item;
        }
    }

}
