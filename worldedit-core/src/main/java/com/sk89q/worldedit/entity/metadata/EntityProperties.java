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

package com.sk89q.worldedit.entity.metadata;

/**
 * Describes various classes of entities.
 */
public interface EntityProperties {

    /**
     * Test whether the entity is a player-derived entity.
     *
     * @return true if a player derived entity
     */
    boolean isPlayerDerived();

    /**
     * Test whether the entity is a projectile.
     *
     * @return true if a projectile
     */
    boolean isProjectile();

    /**
     * Test whether the entity is an item.
     *
     * @return true if an item
     */
    boolean isItem();

    /**
     * Test whether the entity is a falling block.
     *
     * @return true if a falling block
     */
    boolean isFallingBlock();

    /**
     * Test whether the entity is a painting.
     *
     * @return true if a painting
     */
    boolean isPainting();

    /**
     * Test whether the entity is an item frame.
     *
     * @return true if an item frame
     */
    boolean isItemFrame();

    /**
     * Test whether the entity is a boat.
     *
     * @return true if a boat
     */
    boolean isBoat();

    /**
     * Test whether the entity is a minecart.
     *
     * @return true if a minecart
     */
    boolean isMinecart();

    /**
     * Test whether the entity is a primed TNT block.
     *
     * @return true if TNT
     */
    boolean isTNT();

    /**
     * Test whether the entity is an experience orb.
     *
     * @return true if an experience orb
     */
    boolean isExperienceOrb();

    /**
     * Test whether the entity is a living entity.
     *
     * <p>A "living entity" is the superclass of many living entity classes
     * in Minecraft.</p>
     *
     * @return true if a living entity
     */
    boolean isLiving();

    /**
     * Test whether the entity is an animal.
     *
     * @return true if an animal
     */
    boolean isAnimal();

    /**
     * Test whether the entity is an ambient creature, which includes
     * the bat.
     *
     * @return true if an ambient creature
     */
    boolean isAmbient();

    /**
     * Test whether the entity is a non-player controlled character, which
     * includes villagers, NPCs from mods, and so on.
     *
     * @return true if an NPC
     */
    boolean isNPC();

    /**
     * Test whether the entity is the iron golem from Minecraft.
     *
     * @return true if an iron golem
     */
    boolean isGolem();

    /**
     * Test whether the entity is tameable and is tamed.
     *
     * @return true if tamed
     */
    boolean isTamed();

    /**
     * Test whether the entity has been named (tagged).
     *
     * @return true if named
     */
    boolean isTagged();

    /**
     * Test whether the entity is an armor stand.
     *
     * @return true if an armor stand
     */
    boolean isArmorStand();

    /**
     * Test whether this entity can be pasted.
     *
     * @return true if pasteable
     */
    boolean isPasteable();

    /**
     * Test whether the entity is a water creature.
     *
     * @return true if water creature
     */
    boolean isWaterCreature();
}
