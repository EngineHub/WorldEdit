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

/**
 * Represents the possible types of mob spawners.
 *
 * @author md_5
 */
public enum MobSpawnerType {
    CHICKEN("Chicken"),
    COW("Cow"),
    CREEPER("Creeper"),
    GHAST("Ghast"),
    GIANT("Giant"),
    MONSTER("Monster"),
    PIG("Pig"),
    PIG_ZOMBIE("PigZombie"),
    SHEEP("Sheep"),
    SKELETON("Skeleton"),
    SLIME("Slime"),
    SPIDER("Spider"),
    SQUID("Squid"),
    ZOMBIE("Zombie"),
    WOLF("Wolf");

    private String name;

    private MobSpawnerType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}