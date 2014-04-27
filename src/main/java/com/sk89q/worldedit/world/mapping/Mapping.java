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

package com.sk89q.worldedit.world.mapping;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.Tameable;

import javax.annotation.Nullable;

/**
 * A mapping can create state objects (such as {@link BaseBlock})
 * from implementation-independent identifiers (such as "minecraft.stone"),
 * as well as perform the opposite task of retrieving
 * the identifier for a given state object (whenever possible at least, because
 * a mapping may not be compatible with a set of state objects that may have
 * come from another implementation). In addition, a mapping may be able to
 * retrieve a variety of metadata objects (such as {@link Tameable}) to
 * describe a given state object.
 * </p>
 * However, it may be possible that a mapping be "dumb" and not be able to,
 * for example, return a {@link Tameable} for a "Horse" entity, simply because
 * it has not be implemented. Any code that queries a mapping must be
 * aware of this and act accordingly.
 */
public interface Mapping {

    /**
     * Get the mapping for block identifiers.
     *
     * @return a block mapping
     */
    Resolver<BaseBlock> getBlockMapping();

    /**
     * Get the mapping for entity identifiers.
     *
     * @return an entity mapping
     */
    Resolver<BaseEntity> getEntityMapping();

    /**
     * Attempt to return an instance of the given class for the given block.
     * For example, {@code getMetaData(block, Inventory.class)} might return
     * an instance if the block happens to contain an inventory.
     * </p>
     * If the given block is not of the given class (i.e. a dirt block does
     * not have an inventory) or if the information is simply not available,
     * {@code null} will be returned.
     * </p>
     * Mutator methods on the returned instance should change the block.
     *
     * @param block the block
     * @param metaDataClass the metadata class for the returned instance
     * @param <T> the metadata class
     * @return an instance of the given class, otherwise null
     */
    @Nullable <T> T getMetaData(BaseBlock block, Class<T> metaDataClass);

    /**
     * Attempt to return an instance of the given class for the given entity.
     * For example, {@code getMetaData(entity, Creature.class)} might return
     * an instance if the entity happens to be a creature.
     * </p>
     * If the given entity is not of the given class (i.e. a painting is
     * not a creature) or if the information is simply not available,
     * {@code null} will be returned.
     * </p>
     * Mutator methods on the returned instance should change the entity.
     *
     * @param entity the entity
     * @param metaDataClass the metadata class for the returned instance
     * @param <T> the metadata class
     * @return an instance of the given class, otherwise null
     */
    @Nullable <T> T getMetaData(Entity entity, Class<T> metaDataClass);

    /**
     * Attempt to return an instance of the given class for the given entity.
     * For example, {@code getMetaData(entity, Creature.class)} might return
     * an instance if the entity happens to be a creature.
     * </p>
     * If the given entity is not of the given class (i.e. a painting is
     * not a creature) or if the information is simply not available,
     * {@code null} will be returned.
     * </p>
     * Mutator methods on the returned instance should change the entity.
     *
     * @param entity the entity
     * @param metaDataClass the metadata class for the returned instance
     * @param <T> the metadata class
     * @return an instance of the given class, otherwise null
     */
    @Nullable <T> T getMetaData(BaseEntity entity, Class<T> metaDataClass);

}
