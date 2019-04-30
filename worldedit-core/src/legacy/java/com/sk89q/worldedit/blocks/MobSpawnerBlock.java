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

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;

/**
 * A mob spawner block.
 */
public class MobSpawnerBlock extends BaseBlock {

    private String mobType;
    private short delay;

    // advanced mob spawner features
    private short spawnCount;
    private short spawnRange;
    private CompoundTag spawnData;
    private ListTag spawnPotentials;
    private short minSpawnDelay;
    private short maxSpawnDelay;
    private short maxNearbyEntities;
    private short requiredPlayerRange;

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
    public boolean hasNbtData() {
        return true;
    }

    @Override
    public String getNbtId() {
        return "MobSpawner";
    }

    @Override
    public CompoundTag getNbtData() {
        Map<String, Tag> values = new HashMap<>();
        values.put("EntityId", new StringTag(mobType));
        values.put("Delay", new ShortTag(delay));
        values.put("SpawnCount", new ShortTag(spawnCount));
        values.put("SpawnRange", new ShortTag(spawnRange));
        values.put("MinSpawnDelay", new ShortTag(minSpawnDelay));
        values.put("MaxSpawnDelay", new ShortTag(maxSpawnDelay));
        values.put("MaxNearbyEntities", new ShortTag(maxNearbyEntities));
        values.put("RequiredPlayerRange", new ShortTag(requiredPlayerRange));
        if (spawnData != null) {
            values.put("SpawnData", new CompoundTag(spawnData.getValue()));
        }
        if (spawnPotentials != null) {
            values.put("SpawnPotentials", new ListTag(CompoundTag.class, spawnPotentials.getValue()));
        }

        return new CompoundTag(values);
    }

    @Override
    public void setNbtData(CompoundTag rootTag) {
        if (rootTag == null) {
            return;
        }

        Map<String, Tag> values = rootTag.getValue();

        Tag t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag) t).getValue().equals("MobSpawner")) {
            throw new RuntimeException("'MobSpawner' tile entity expected");
        }

        StringTag mobTypeTag;
        ShortTag delayTag;

        try {
            mobTypeTag = NBTUtils.getChildTag(values, "EntityId", StringTag.class);
            delayTag = NBTUtils.getChildTag(values, "Delay", ShortTag.class);
        } catch (InvalidFormatException ignored) {
            throw new RuntimeException("Invalid mob spawner data: no EntityId and/or no Delay");
        }

        this.mobType = mobTypeTag.getValue();
        this.delay = delayTag.getValue();

        ShortTag spawnCountTag = null;
        ShortTag spawnRangeTag = null;
        ShortTag minSpawnDelayTag = null;
        ShortTag maxSpawnDelayTag = null;
        ShortTag maxNearbyEntitiesTag = null;
        ShortTag requiredPlayerRangeTag = null;
        ListTag spawnPotentialsTag = null;
        CompoundTag spawnDataTag = null;
        try {
            spawnCountTag = NBTUtils.getChildTag(values, "SpawnCount", ShortTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            spawnRangeTag = NBTUtils.getChildTag(values, "SpawnRange", ShortTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            minSpawnDelayTag = NBTUtils.getChildTag(values, "MinSpawnDelay", ShortTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            maxSpawnDelayTag = NBTUtils.getChildTag(values, "MaxSpawnDelay", ShortTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            maxNearbyEntitiesTag = NBTUtils.getChildTag(values, "MaxNearbyEntities", ShortTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            requiredPlayerRangeTag = NBTUtils.getChildTag(values, "RequiredPlayerRange", ShortTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            spawnPotentialsTag = NBTUtils.getChildTag(values, "SpawnPotentials", ListTag.class);
        } catch (InvalidFormatException ignored) {
        }
        try {
            spawnDataTag = NBTUtils.getChildTag(values, "SpawnData", CompoundTag.class);
        } catch (InvalidFormatException ignored) {
        }

        if (spawnCountTag != null) {
            this.spawnCount = spawnCountTag.getValue();
        }
        if (spawnRangeTag != null) {
            this.spawnRange = spawnRangeTag.getValue();
        }
        if (minSpawnDelayTag != null) {
            this.minSpawnDelay = minSpawnDelayTag.getValue();
        }
        if (maxSpawnDelayTag != null) {
            this.maxSpawnDelay = maxSpawnDelayTag.getValue();
        }
        if (maxNearbyEntitiesTag != null) {
            this.maxNearbyEntities = maxNearbyEntitiesTag.getValue();
        }
        if (requiredPlayerRangeTag != null) {
            this.requiredPlayerRange = requiredPlayerRangeTag.getValue();
        }
        if (spawnPotentialsTag != null) {
            this.spawnPotentials = new ListTag(CompoundTag.class, spawnPotentialsTag.getValue());
        }
        if (spawnDataTag != null) {
            this.spawnData = new CompoundTag(spawnDataTag.getValue());
        }
    }

}
