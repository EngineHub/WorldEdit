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

package com.sk89q.worldedit.world.entity;

import javax.annotation.Nullable;

public class EntityTypes {

    public static final EntityType AREA_EFFECT_CLOUD = register("minecraft:area_effect_cloud");
    public static final EntityType ARMOR_STAND = register("minecraft:armor_stand");
    public static final EntityType ARROW = register("minecraft:arrow");
    public static final EntityType BAT = register("minecraft:bat");
    public static final EntityType BLAZE = register("minecraft:blaze");
    public static final EntityType BOAT = register("minecraft:boat");
    public static final EntityType CAVE_SPIDER = register("minecraft:cave_spider");
    public static final EntityType CHEST_MINECART = register("minecraft:chest_minecart");
    public static final EntityType CHICKEN = register("minecraft:chicken");
    public static final EntityType COD = register("minecraft:cod");
    public static final EntityType COMMAND_BLOCK_MINECART = register("minecraft:command_block_minecart");
    public static final EntityType COW = register("minecraft:cow");
    public static final EntityType CREEPER = register("minecraft:creeper");
    public static final EntityType DOLPHIN = register("minecraft:dolphin");
    public static final EntityType DONKEY = register("minecraft:donkey");
    public static final EntityType DRAGON_FIREBALL = register("minecraft:dragon_fireball");
    public static final EntityType DROWNED = register("minecraft:drowned");
    public static final EntityType EGG = register("minecraft:egg");
    public static final EntityType ELDER_GUARDIAN = register("minecraft:elder_guardian");
    public static final EntityType END_CRYSTAL = register("minecraft:end_crystal");
    public static final EntityType ENDER_DRAGON = register("minecraft:ender_dragon");
    public static final EntityType ENDER_PEARL = register("minecraft:ender_pearl");
    public static final EntityType ENDERMAN = register("minecraft:enderman");
    public static final EntityType ENDERMITE = register("minecraft:endermite");
    public static final EntityType EVOKER = register("minecraft:evoker");
    public static final EntityType EVOKER_FANGS = register("minecraft:evoker_fangs");
    public static final EntityType EXPERIENCE_BOTTLE = register("minecraft:experience_bottle");
    public static final EntityType EXPERIENCE_ORB = register("minecraft:experience_orb");
    public static final EntityType EYE_OF_ENDER = register("minecraft:eye_of_ender");
    public static final EntityType FALLING_BLOCK = register("minecraft:falling_block");
    public static final EntityType FIREBALL = register("minecraft:fireball");
    public static final EntityType FIREWORK_ROCKET = register("minecraft:firework_rocket");
    public static final EntityType FISHING_BOBBER = register("minecraft:fishing_bobber");
    public static final EntityType FURNACE_MINECART = register("minecraft:furnace_minecart");
    public static final EntityType GHAST = register("minecraft:ghast");
    public static final EntityType GIANT = register("minecraft:giant");
    public static final EntityType GUARDIAN = register("minecraft:guardian");
    public static final EntityType HOPPER_MINECART = register("minecraft:hopper_minecart");
    public static final EntityType HORSE = register("minecraft:horse");
    public static final EntityType HUSK = register("minecraft:husk");
    public static final EntityType ILLUSIONER = register("minecraft:illusioner");
    public static final EntityType IRON_GOLEM = register("minecraft:iron_golem");
    public static final EntityType ITEM = register("minecraft:item");
    public static final EntityType ITEM_FRAME = register("minecraft:item_frame");
    public static final EntityType LEASH_KNOT = register("minecraft:leash_knot");
    public static final EntityType LIGHTNING_BOLT = register("minecraft:lightning_bolt");
    public static final EntityType LLAMA = register("minecraft:llama");
    public static final EntityType LLAMA_SPIT = register("minecraft:llama_spit");
    public static final EntityType MAGMA_CUBE = register("minecraft:magma_cube");
    public static final EntityType MINECART = register("minecraft:minecart");
    public static final EntityType MOOSHROOM = register("minecraft:mooshroom");
    public static final EntityType MULE = register("minecraft:mule");
    public static final EntityType OCELOT = register("minecraft:ocelot");
    public static final EntityType PAINTING = register("minecraft:painting");
    public static final EntityType PARROT = register("minecraft:parrot");
    public static final EntityType PHANTOM = register("minecraft:phantom");
    public static final EntityType PIG = register("minecraft:pig");
    public static final EntityType PLAYER = register("minecraft:player");
    public static final EntityType POLAR_BEAR = register("minecraft:polar_bear");
    public static final EntityType POTION = register("minecraft:potion");
    public static final EntityType PUFFERFISH = register("minecraft:pufferfish");
    public static final EntityType RABBIT = register("minecraft:rabbit");
    public static final EntityType SALMON = register("minecraft:salmon");
    public static final EntityType SHEEP = register("minecraft:sheep");
    public static final EntityType SHULKER = register("minecraft:shulker");
    public static final EntityType SHULKER_BULLET = register("minecraft:shulker_bullet");
    public static final EntityType SILVERFISH = register("minecraft:silverfish");
    public static final EntityType SKELETON = register("minecraft:skeleton");
    public static final EntityType SKELETON_HORSE = register("minecraft:skeleton_horse");
    public static final EntityType SLIME = register("minecraft:slime");
    public static final EntityType SMALL_FIREBALL = register("minecraft:small_fireball");
    public static final EntityType SNOW_GOLEM = register("minecraft:snow_golem");
    public static final EntityType SNOWBALL = register("minecraft:snowball");
    public static final EntityType SPAWNER_MINECART = register("minecraft:spawner_minecart");
    public static final EntityType SPECTRAL_ARROW = register("minecraft:spectral_arrow");
    public static final EntityType SPIDER = register("minecraft:spider");
    public static final EntityType SQUID = register("minecraft:squid");
    public static final EntityType STRAY = register("minecraft:stray");
    public static final EntityType TNT = register("minecraft:tnt");
    public static final EntityType TNT_MINECART = register("minecraft:tnt_minecart");
    public static final EntityType TRIDENT = register("minecraft:trident");
    public static final EntityType TROPICAL_FISH = register("minecraft:tropical_fish");
    public static final EntityType TURTLE = register("minecraft:turtle");
    public static final EntityType VEX = register("minecraft:vex");
    public static final EntityType VILLAGER = register("minecraft:villager");
    public static final EntityType VINDICATOR = register("minecraft:vindicator");
    public static final EntityType WITCH = register("minecraft:witch");
    public static final EntityType WITHER = register("minecraft:wither");
    public static final EntityType WITHER_SKELETON = register("minecraft:wither_skeleton");
    public static final EntityType WITHER_SKULL = register("minecraft:wither_skull");
    public static final EntityType WOLF = register("minecraft:wolf");
    public static final EntityType ZOMBIE = register("minecraft:zombie");
    public static final EntityType ZOMBIE_HORSE = register("minecraft:zombie_horse");
    public static final EntityType ZOMBIE_PIGMAN = register("minecraft:zombie_pigman");
    public static final EntityType ZOMBIE_VILLAGER = register("minecraft:zombie_villager");

    private EntityTypes() {
    }

    private static EntityType register(final String id) {
        return register(new EntityType(id));
    }

    public static EntityType register(final EntityType entityType) {
        return EntityType.REGISTRY.register(entityType.getId(), entityType);
    }

    public static @Nullable EntityType get(final String id) {
        return EntityType.REGISTRY.get(id);
    }

}
