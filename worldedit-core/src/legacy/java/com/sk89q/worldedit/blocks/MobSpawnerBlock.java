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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sk89q.worldedit.util.NbtUtils;
import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.BinaryTagTypes;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.IntBinaryTag;
import com.sk89q.worldedit.util.nbt.ListBinaryTag;
import com.sk89q.worldedit.util.nbt.ShortBinaryTag;
import com.sk89q.worldedit.util.nbt.StringBinaryTag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

/**
 * A mob spawner block.
 */
public class MobSpawnerBlock extends BaseBlock {

    private String mobType;
    private short delay = -1;

    // advanced mob spawner features
    private short spawnCount = 4;
    private short spawnRange = 4;
    private CompoundBinaryTag spawnData;
    private ListBinaryTag spawnPotentials;
    private short minSpawnDelay = 200;
    private short maxSpawnDelay = 800;
    private short maxNearbyEntities = 6;
    private short requiredPlayerRange = 16;

    /**
     * Construct the mob spawner block with a specified data value.
     *
     * @param blockState The block state
     */
    public MobSpawnerBlock(BlockState blockState) {
        super(blockState);
    }

    /**
     * Construct the mob spawner block.
     *
     * @param blockState The block state
     * @param mobType mob type
     */
    public MobSpawnerBlock(BlockState blockState, String mobType) {
        super(blockState);
        this.mobType = mobType;
    }

    /**
     * Get the mob type.
     *
     * @return the mob type
     */
    public String getMobType() {
        return mobType;
    }

    /**
     * Set the mob type.
     *
     * @param mobType the mob type
     */
    public void setMobType(String mobType) {
        this.mobType = mobType;
    }

    /**
     * Get the spawn delay.
     *
     * @return the delay
     */
    public short getDelay() {
        return delay;
    }

    /**
     * Set the spawn delay.
     *
     * @param delay the delay to set
     */
    public void setDelay(short delay) {
        this.delay = delay;
    }

    @Override
    public boolean hasNbt() {
        return true;
    }

    @Override
    public String getNbtId() {
        return "minecraft:mob_spawner";
    }

    @Override
    public CompoundBinaryTag getNbt() {
        CompoundBinaryTag.Builder values = CompoundBinaryTag.builder();
        values.put("Delay", ShortBinaryTag.of(delay));
        values.put("SpawnCount", ShortBinaryTag.of(spawnCount));
        values.put("SpawnRange", ShortBinaryTag.of(spawnRange));
        values.put("MinSpawnDelay", ShortBinaryTag.of(minSpawnDelay));
        values.put("MaxSpawnDelay", ShortBinaryTag.of(maxSpawnDelay));
        values.put("MaxNearbyEntities", ShortBinaryTag.of(maxNearbyEntities));
        values.put("RequiredPlayerRange", ShortBinaryTag.of(requiredPlayerRange));
        if (spawnData == null) {
            values.put("SpawnData", CompoundBinaryTag.builder().put("id", StringBinaryTag.of(mobType)).build());
        } else {
            values.put("SpawnData", spawnData);
        }
        if (spawnPotentials == null) {
            values.put("SpawnPotentials", ListBinaryTag.of(
                BinaryTagTypes.COMPOUND,
                ImmutableList.of(CompoundBinaryTag.from(ImmutableMap.of(
                    "Weight", IntBinaryTag.of(1),
                    "Entity", CompoundBinaryTag.from(ImmutableMap.of("id", StringBinaryTag.of(mobType)))
                )))
            ));
        } else {
            values.put("SpawnPotentials", spawnPotentials);
        }

        return values.build();
    }

    @Override
    public void setNbt(CompoundBinaryTag rootTag) {
        if (rootTag == null) {
            return;
        }

        BinaryTag t = rootTag.get("id");
        if (!(t instanceof StringBinaryTag) || !((StringBinaryTag) t).value().equals(getNbtId())) {
            throw new RuntimeException(String.format("'%s' tile entity expected", getNbtId()));
        }

        CompoundBinaryTag spawnDataTag;
        String mobType;
        ShortBinaryTag delayTag;

        try {
            spawnDataTag = NbtUtils.getChildTag(rootTag, "SpawnData", CompoundBinaryTag.class);
            mobType = spawnDataTag.getString("id");
            if (mobType.equals("")) {
                throw new InvalidFormatException("No spawn id.");
            }
            this.mobType = mobType;
            this.spawnData = spawnDataTag;
        } catch (InvalidFormatException ignored) {
            throw new RuntimeException("Invalid mob spawner data: no SpawnData and/or no Delay");
        }
        try {
            delayTag = NbtUtils.getChildTag(rootTag, "Delay", ShortBinaryTag.class);
            this.delay = delayTag.value();
        } catch (InvalidFormatException ignored) {
            this.delay = -1;
        }

        ShortBinaryTag spawnCountTag = null;
        ShortBinaryTag spawnRangeTag = null;
        ShortBinaryTag minSpawnDelayTag = null;
        ShortBinaryTag maxSpawnDelayTag = null;
        ShortBinaryTag maxNearbyEntitiesTag = null;
        ShortBinaryTag requiredPlayerRangeTag = null;
        ListBinaryTag spawnPotentialsTag = null;
        try {
            spawnCountTag = NbtUtils.getChildTag(rootTag, "SpawnCount", ShortBinaryTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            spawnRangeTag = NbtUtils.getChildTag(rootTag, "SpawnRange", ShortBinaryTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            minSpawnDelayTag = NbtUtils.getChildTag(rootTag, "MinSpawnDelay", ShortBinaryTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            maxSpawnDelayTag = NbtUtils.getChildTag(rootTag, "MaxSpawnDelay", ShortBinaryTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            maxNearbyEntitiesTag = NbtUtils.getChildTag(rootTag, "MaxNearbyEntities", ShortBinaryTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            requiredPlayerRangeTag = NbtUtils.getChildTag(rootTag, "RequiredPlayerRange", ShortBinaryTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            spawnPotentialsTag = NbtUtils.getChildTag(rootTag, "SpawnPotentials", ListBinaryTag.class);
        } catch (InvalidFormatException ignored) {
        }

        if (spawnCountTag != null) {
            this.spawnCount = spawnCountTag.value();
        }
        if (spawnRangeTag != null) {
            this.spawnRange = spawnRangeTag.value();
        }
        if (minSpawnDelayTag != null) {
            this.minSpawnDelay = minSpawnDelayTag.value();
        }
        if (maxSpawnDelayTag != null) {
            this.maxSpawnDelay = maxSpawnDelayTag.value();
        }
        if (maxNearbyEntitiesTag != null) {
            this.maxNearbyEntities = maxNearbyEntitiesTag.value();
        }
        if (requiredPlayerRangeTag != null) {
            this.requiredPlayerRange = requiredPlayerRangeTag.value();
        }
        if (spawnPotentialsTag != null) {
            this.spawnPotentials = spawnPotentialsTag;
        }
    }

}
