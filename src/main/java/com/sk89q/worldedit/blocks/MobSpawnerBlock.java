// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.data.*;
import java.util.Map;
import java.util.HashMap;

/**
 * Represents chests.
 *
 * @author sk89q
 */
public class MobSpawnerBlock extends BaseBlock implements TileEntityBlock {
    /**
     * Store mob spawn type.
     */
    private String mobType;
    /**
     * Delay until next spawn.
     */
    private short delay;

    /**
     * Construct the mob spawner block.
     *
     */
    public MobSpawnerBlock() {
        super(BlockID.MOB_SPAWNER);
        this.mobType = "Pig";
    }

    /**
     * Construct the mob spawner block.
     *
     * @param mobType
     */
    public MobSpawnerBlock(String mobType) {
        super(BlockID.MOB_SPAWNER);
        this.mobType = mobType;
    }

    /**
     * Construct the mob spawner block.
     *
     * @param data
     */
    public MobSpawnerBlock(int data) {
        super(BlockID.MOB_SPAWNER, data);
    }

    /**
     * Construct the mob spawner block.
     *
     * @param data
     * @param mobType
     */
    public MobSpawnerBlock(int data, String mobType) {
        super(BlockID.MOB_SPAWNER, data);
        this.mobType = mobType;
    }

    /**
     * Get the mob type.
     *
     * @return
     */
    public String getMobType() {
        return mobType;
    }

    /**
     * Set the mob type.
     * 
     * @param mobType 
     */
    public void setMobType(String mobType) {
        this.mobType = mobType;
    }

    /**
     * @return the delay
     */
    public short getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(short delay) {
        this.delay = delay;
    }

    /**
     * Get the tile entity ID.
     *
     * @return
     */
    public String getTileEntityID() {
        return "MobSpawner";
    }

    /**
     * Store additional tile entity data. Returns true if the data is used.
     *
     * @return map of values
     * @throws DataException
     */
    public Map<String,Tag> toTileEntityNBT()
            throws DataException {
        Map<String,Tag> values = new HashMap<String,Tag>();
        values.put("EntityId", new StringTag("EntityId", mobType));
        values.put("Delay", new ShortTag("Delay", delay));
        return values;
    }

    /**
     * Get additional information from the title entity data.
     *
     * @param values
     * @throws DataException
     */
    public void fromTileEntityNBT(Map<String,Tag> values)
            throws DataException  {
        if (values == null) {
            return;
        }

        Tag t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag)t).getValue().equals("MobSpawner")) {
            throw new DataException("'MobSpawner' tile entity expected");
        }

        StringTag mobTypeTag = (StringTag)Chunk.getChildTag(values, "EntityId", StringTag.class);
        ShortTag delayTag = (ShortTag)Chunk.getChildTag(values, "Delay", ShortTag.class);

        this.mobType = mobTypeTag.getValue();
        this.delay = delayTag.getValue();
    }
}
