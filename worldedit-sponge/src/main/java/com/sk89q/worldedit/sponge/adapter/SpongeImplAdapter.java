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

package com.sk89q.worldedit.sponge.adapter;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.sponge.SpongeWorld;
import net.minecraft.nbt.*;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.biome.BiomeType;

/**
 * An interface for various things that can't be done through the Sponge API.
 */
public interface SpongeImplAdapter {

    /**
     * Resolves the numerical ID from this {@link ItemType}
     *
     * @param type The itemtype
     * @return The numerical ID
     */
    int resolve(ItemType type);

    /**
     * Resolves the numerical ID from this {@link BlockType}
     *
     * @param type The blocktype
     * @return The numerical ID
     */
    int resolve(BlockType type);

    /**
     * Resolves the numerical ID from this {@link BiomeType}
     *
     * @param type The biometype
     * @return The numerical ID
     */
    int resolve(BiomeType type);

    ItemType resolveItem(int intID);

    BlockType resolveBlock(int intID);

    BiomeType resolveBiome(int intID);

    NBTBase toNative(Tag tag);

    NBTTagIntArray toNative(IntArrayTag tag);

    NBTTagList toNative(ListTag tag);

    NBTTagLong toNative(LongTag tag);

    NBTTagString toNative(StringTag tag);

    NBTTagInt toNative(IntTag tag);

    NBTTagByte toNative(ByteTag tag);

    NBTTagByteArray toNative(ByteArrayTag tag);

    NBTTagCompound toNative(CompoundTag tag);

    NBTTagFloat toNative(FloatTag tag);

    NBTTagShort toNative(ShortTag tag);

    NBTTagDouble toNative(DoubleTag tag);

    Tag fromNative(NBTBase other);

    IntArrayTag fromNative(NBTTagIntArray other);

    ListTag fromNative(NBTTagList other);

    EndTag fromNative(NBTTagEnd other);

    LongTag fromNative(NBTTagLong other);

    StringTag fromNative(NBTTagString other);

    IntTag fromNative(NBTTagInt other);

    ByteTag fromNative(NBTTagByte other);

    ByteArrayTag fromNative(NBTTagByteArray other);

    CompoundTag fromNative(NBTTagCompound other);

    FloatTag fromNative(NBTTagFloat other);

    ShortTag fromNative(NBTTagShort other);

    DoubleTag fromNative(NBTTagDouble other);

    BaseEntity createBaseEntity(Entity entity);

    ItemStack makeSpongeStack(BaseItemStack itemStack);

    SpongeWorld getWorld(World world);
}
