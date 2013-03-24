// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.blocks;

import java.util.HashMap;
import java.util.Map;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.MobType;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.data.InvalidFormatException;

/**
 * A mob spawner block.
 *
 * @author sk89q
 */
public class MobSpawnerBlock extends BaseBlock implements TileEntityBlock {

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
     * Construct the mob spawner block with a pig as the mob type.
     */
    public MobSpawnerBlock() {
        super(BlockID.MOB_SPAWNER);
        this.mobType = MobType.PIG.getName();
    }

    /**
     * Construct the mob spawner block with a given mob type.
     *
     * @param mobType mob type
     */
    public MobSpawnerBlock(String mobType) {
        super(BlockID.MOB_SPAWNER);
        this.mobType = mobType;
    }

    /**
     * Construct the mob spawner block with a specified data value.
     *
     * @param data data value
     */
    public MobSpawnerBlock(int data) {
        super(BlockID.MOB_SPAWNER, data);
    }

    /**
     * Construct the mob spawner block.
     *
     * @param data data value
     * @param mobType mob type
     */
    public MobSpawnerBlock(int data, String mobType) {
        super(BlockID.MOB_SPAWNER, data);
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
        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("EntityId", new StringTag("EntityId", mobType));
        values.put("Delay", new ShortTag("Delay", delay));
        values.put("SpawnCount", new ShortTag("SpawnCount", spawnCount));
        values.put("SpawnRange", new ShortTag("SpawnRange", spawnRange));
        values.put("MinSpawnDelay", new ShortTag("MinSpawnDelay", minSpawnDelay));
        values.put("MaxSpawnDelay", new ShortTag("MaxSpawnDelay", maxSpawnDelay));
        values.put("MaxNearbyEntities", new ShortTag("MaxNearbyEntities", maxNearbyEntities));
        values.put("RequiredPlayerRange", new ShortTag("RequiredPlayerRange", requiredPlayerRange));
        values.put("SpawnData", new CompoundTag("SpawnData", spawnData == null ? null : spawnData.getValue()));
        values.put("SpawnPotentials", new ListTag("SpawnPotentials", CompoundTag.class, spawnPotentials == null ? null : spawnPotentials.getValue()));

        return new CompoundTag(getNbtId(), values);
    }

    @Override
    public void setNbtData(CompoundTag rootTag) throws DataException {
        if (rootTag == null) {
            return;
        }

        Map<String, Tag> values = rootTag.getValue();

        Tag t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag) t).getValue().equals("MobSpawner")) {
            throw new DataException("'MobSpawner' tile entity expected");
        }

        StringTag mobTypeTag = NBTUtils.getChildTag(values, "EntityId", StringTag.class);
        ShortTag delayTag = NBTUtils.getChildTag(values, "Delay", ShortTag.class);

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
            spawnRangeTag = NBTUtils.getChildTag(values, "SpawnRange", ShortTag.class);
            minSpawnDelayTag = NBTUtils.getChildTag(values, "MinSpawnDelay", ShortTag.class);
            maxSpawnDelayTag = NBTUtils.getChildTag(values, "MaxSpawnDelay", ShortTag.class);
            maxNearbyEntitiesTag = NBTUtils.getChildTag(values, "MaxNearbyEntities", ShortTag.class);
            requiredPlayerRangeTag = NBTUtils.getChildTag(values, "RequiredPlayerRange", ShortTag.class);
            spawnPotentialsTag = NBTUtils.getChildTag(values, "SpawnPotentials", ListTag.class);
            spawnDataTag = NBTUtils.getChildTag(values, "SpawnData", CompoundTag.class);
        } catch (InvalidFormatException e) { // leave tag as null, handle later
        }

        this.spawnCount = spawnCountTag == null ? null : spawnCountTag.getValue();
        this.spawnRange = spawnRangeTag == null ? null : spawnRangeTag.getValue();
        this.minSpawnDelay = minSpawnDelayTag == null ? null : minSpawnDelayTag.getValue();
        this.maxSpawnDelay = maxSpawnDelayTag == null ? null : maxSpawnDelayTag.getValue();
        this.maxNearbyEntities = maxNearbyEntitiesTag == null ? null : maxNearbyEntitiesTag.getValue();
        this.requiredPlayerRange = requiredPlayerRangeTag == null ? null : requiredPlayerRangeTag.getValue();
        this.spawnPotentials = new ListTag("SpawnPotentials", CompoundTag.class, spawnPotentialsTag == null ? null : spawnPotentialsTag.getValue());
        this.spawnData = new CompoundTag("SpawnData", spawnDataTag == null ? null : spawnDataTag.getValue());

    }
}
