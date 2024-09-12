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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21.wna;

import com.google.common.base.Throwables;
import com.sk89q.worldedit.bukkit.adapter.impl.v1_21.StaticRefraction;
import com.sk89q.worldedit.internal.wna.NativeBlockState;
import com.sk89q.worldedit.internal.wna.NativeChunkSection;
import net.minecraft.core.Holder;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public record PaperweightNativeChunkSection(LevelChunkSection delegate) implements NativeChunkSection {
    private static final MethodHandle GET_DATA;
    private static final MethodHandle SET_DATA;
    private static final MethodHandle CREATE_OR_REUSE_DATA;
    private static final MethodHandle GET_STORAGE;
    private static final MethodHandle GET_PALETTE;
    private static final MethodHandle COPY_FROM;

    static {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> dataClass = Class.forName(StaticRefraction.PALETTED_CONTAINER_DATA_CLASS);

            Field f = PalettedContainer.class.getDeclaredField(StaticRefraction.PALETTED_CONTAINER_DATA);
            f.setAccessible(true);
            GET_DATA = lookup.unreflectGetter(f);
            SET_DATA = lookup.unreflectSetter(f);

            Method m = PalettedContainer.class.getDeclaredMethod(
                StaticRefraction.CREATE_OR_REUSE_DATA, dataClass, int.class
            );
            m.setAccessible(true);
            CREATE_OR_REUSE_DATA = lookup.unreflect(m);

            f = dataClass.getDeclaredField(StaticRefraction.PALETTED_CONTAINER_DATA_STORAGE);
            f.setAccessible(true);
            GET_STORAGE = lookup.unreflectGetter(f);

            f = dataClass.getDeclaredField(StaticRefraction.PALETTED_CONTAINER_DATA_PALETTE);
            f.setAccessible(true);
            GET_PALETTE = lookup.unreflectGetter(f);

            m = dataClass.getDeclaredMethod(StaticRefraction.COPY_FROM, Palette.class, BitStorage.class);
            m.setAccessible(true);
            COPY_FROM = lookup.unreflect(m);
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public boolean isOnlyAir() {
        return delegate.hasOnlyAir();
    }

    @Override
    public NativeBlockState getThenSetBlock(int i, int j, int k, NativeBlockState blockState) {
        BlockState nativeState = ((PaperweightNativeBlockState) blockState).delegate();
        if (isOnlyAir() && nativeState.isAir()) {
            return blockState;
        }
        return new PaperweightNativeBlockState(delegate.setBlockState(i, j, k, nativeState, false));
    }

    @Override
    public NativeBlockState getBlock(int i, int j, int k) {
        return new PaperweightNativeBlockState(delegate.getBlockState(i, j, k));
    }

    @Override
    public NativeChunkSection copy() {
        return new PaperweightNativeChunkSection(new LevelChunkSection(
            copyPalettedContainer(delegate.getStates()), copyPalettedContainer((PalettedContainer<Holder<Biome>>) delegate.getBiomes())
        ));
    }

    /**
     * Mojang is bad at writing code and their copy method doesn't replace the resize handler so it resizes the wrong
     * thing. Reinitialize their data for them.
     *
     * @param container the container to copy
     * @return the copied container
     */
    @SuppressWarnings("unchecked")
    private static <T> PalettedContainer<T> copyPalettedContainer(PalettedContainer<T> container) {
        try {
            // First init by directly moving the fields over
            PalettedContainer<T> copy = container.recreate();
            Object data = GET_DATA.invoke(container);
            BitStorage storage = (BitStorage) GET_STORAGE.invoke(data);
            // Force re-create by using `null`, to make an actual copy of the data with the new resize handler.
            Object data2 = CREATE_OR_REUSE_DATA.invoke(copy, null, storage.getSize());
            Palette<T> palette = (Palette<T>) GET_PALETTE.invoke(data);
            COPY_FROM.invoke(data2, palette, storage);
            SET_DATA.invoke(copy, data2);

            return copy;
        } catch (Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }
}
