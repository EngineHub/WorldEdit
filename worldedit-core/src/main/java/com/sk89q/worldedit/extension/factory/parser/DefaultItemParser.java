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

package com.sk89q.worldedit.extension.factory.parser;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

public class DefaultItemParser extends InputParser<BaseItem> {

    public DefaultItemParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public BaseItem parseFromInput(String input, ParserContext context) throws InputParseException {
        BaseItem item = null;
        // Legacy matcher
        if (context.isTryingLegacy()) {
            try {
                String[] split = input.split(":");
                ItemType type;
                if (split.length == 1) {
                    type = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]));
                } else {
                    type = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
                item = new BaseItem(type);
            } catch (NumberFormatException e) {
            }
        }

        if (item == null) {
            ItemType type = ItemTypes.get(input.toLowerCase());
            if (type != null) {
                item = new BaseItem(type);
            }
        }

        if (item == null) {
            throw new InputParseException("'" + input + "' did not match any item");
        } else {
            return item;
        }
    }

}
