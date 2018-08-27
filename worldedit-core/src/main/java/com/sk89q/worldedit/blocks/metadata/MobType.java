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

package com.sk89q.worldedit.blocks.metadata;

/**
 * Represents the possible types of mobs.
 */
public enum MobType {
    BAT("Bat"),
    BLAZE("Blaze"),
    CAVE_SPIDER("CaveSpider"),
    CHICKEN("Chicken"),
    COW("Cow"),
    CREEPER("Creeper"),
    ENDERDRAGON("EnderDragon"),
    ENDERMAN("Enderman"),
    GHAST("Ghast"),
    GIANT("Giant"),
    VILLAGER_GOLEM("VillagerGolem"),
    HORSE("EntityHorse"),
    MAGMA_CUBE("LavaSlime"),
    MOOSHROOM("MushroomCow"),
    OCELOT("Ozelot"),
    PIG("Pig"),
    PIG_ZOMBIE("PigZombie"),
    SHEEP("Sheep"),
    SILVERFISH("Silverfish"),
    SKELETON("Skeleton"),
    SLIME("Slime"),
    SNOWMAN("SnowMan"),
    SPIDER("Spider"),
    SQUID("Squid"),
    VILLAGER("Villager"),
    WITCH("Witch"),
    WITHER("WitherBoss"),
    WOLF("Wolf"),
    ZOMBIE("Zombie");

    private final String name;

    MobType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
