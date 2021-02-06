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

package com.sk89q.worldedit.blocks;

import com.google.common.collect.ImmutableMap;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.StringBinaryTag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.Map;

/**
 * A skull block.
 */
public class SkullBlock extends BaseBlock {

    private String owner = ""; // notchian

    /**
     * Construct the skull block with a default type of skelton.
     * @param state BlockState to set
     */
    public SkullBlock(BlockState state) {
        super(state);
        this.owner = "";
    }

    /**
     * Construct the skull block with a given rotation and owner.
     * The type is assumed to be player unless owner is null or empty.
     * @param blockState BlockState to set
     * @param owner name of player
     */
    public SkullBlock(BlockState blockState, String owner) {
        super(blockState);
        this.setOwner(owner);
    }

    /**
     * Set the skull's owner. Automatically sets type to player if not empty or null.
     * @param owner player name to set the skull to
     */
    public void setOwner(String owner) {
        if (owner == null) {
            this.owner = "";
        } else {
            if (owner.length() > 16 || owner.isEmpty()) {
                this.owner = "";
            } else {
                this.owner = owner;
            }
        }
    }

    /**
     * Get the skull's owner. Returns null if unset.
     * @return player name or null
     */
    public String getOwner() {
        return owner;
    }

    @Override
    public boolean hasNbt() {
        return true;
    }

    @Override
    public String getNbtId() {
        return "skull";
    }

    @Override
    public CompoundBinaryTag getNbt() {
        CompoundBinaryTag.Builder values = CompoundBinaryTag.builder();
        values.put(DeprecationUtil.getHeadOwnerKey(), CompoundBinaryTag.from(
            ImmutableMap.of("Name", StringBinaryTag.of(owner))
        ));
        return values.build();
    }

    @Override
    public void setNbtData(CompoundTag rootTag) {
        if (rootTag == null) {
            return;
        }

        Map<String, Tag> values = rootTag.getValue();

        Tag t;

        t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag) t).getValue().equals(getNbtId())) {
            throw new RuntimeException(String.format("'%s' tile entity expected", getNbtId()));
        }

        t = values.get(DeprecationUtil.getHeadOwnerKey());
        if (t instanceof CompoundTag) {
            setOwner(((CompoundTag) t).getValue().get("Name").getValue().toString());
        }
    }
}
