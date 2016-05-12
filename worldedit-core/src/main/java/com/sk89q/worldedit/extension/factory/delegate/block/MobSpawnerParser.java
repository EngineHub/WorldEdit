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

package com.sk89q.worldedit.extension.factory.delegate.block;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.metadata.MobType;
import com.sk89q.worldedit.extension.factory.DelegateParser;
import com.sk89q.worldedit.extension.input.NoMatchException;

import java.util.HashMap;
import java.util.Map;

public class MobSpawnerParser implements DelegateParser<CompoundTag> {
    @Override
    public CompoundTag createFromArguments(String[] blockAndExtraData) throws NoMatchException {
        MobType mobType = MobType.PIG;

        // Allow setting mob spawn type
        if (blockAndExtraData.length > 1) {
            String mobName = blockAndExtraData[1];
            for (MobType aMobType : MobType.values()) {
                if (aMobType.getName().toLowerCase().equals(mobName.toLowerCase())) {
                    mobType = aMobType;
                    break;
                }
            }
            if (!WorldEdit.getInstance().getServer().isValidMobType(mobName)) {
                throw new NoMatchException("Unknown mob type '" + mobName + "'");
            }
        }

        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("EntityId", new StringTag(mobType.getName()));
        return new CompoundTag(values);
    }
}
