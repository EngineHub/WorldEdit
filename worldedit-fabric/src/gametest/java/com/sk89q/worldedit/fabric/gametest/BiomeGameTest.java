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

package com.sk89q.worldedit.fabric.gametest;

import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Biomes are stored per 4x4x4 cell, so setting one position changes the whole cell and nothing else.
 */
public class BiomeGameTest {
    private static final BlockPos TEST_POS = new BlockPos(3, 3, 3);

    @GameTest
    public void setBiomeRoundTripsThroughWorld(GameTestHelper helper) {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 pos = FabricAdapter.get().adapt(helper.absolutePos(TEST_POS));

        List<BiomeType> biomes = List.of(BiomeTypes.DESERT, BiomeTypes.JUNGLE, BiomeTypes.SNOWY_PLAINS, BiomeTypes.THE_VOID);
        BiomeType original = world.getBiome(pos);
        try {
            for (BiomeType biome : biomes) {
                helper.assertTrue(world.setBiome(pos, biome), "setBiome should report success for " + biome.id());
                helper.assertValueEqual(world.getBiome(pos), biome, "biome read back after setting " + biome.id());
            }
        } finally {
            world.setBiome(pos, original);
        }
        helper.succeed();
    }

    @GameTest
    public void setBiomeAffectsExactlyOneCell(GameTestHelper helper) {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 pos = FabricAdapter.get().adapt(helper.absolutePos(TEST_POS));
        BlockVector3 cellMin = BlockVector3.at(pos.x() & ~3, pos.y() & ~3, pos.z() & ~3);

        // The cell itself plus one cell of border on every side
        List<BlockVector3> probes = new ArrayList<>();
        List<BiomeType> originals = new ArrayList<>();
        for (int x = -4; x < 8; x++) {
            for (int y = -4; y < 8; y++) {
                for (int z = -4; z < 8; z++) {
                    BlockVector3 probe = cellMin.add(x, y, z);
                    probes.add(probe);
                    originals.add(world.getBiome(probe));
                }
            }
        }

        BiomeType target = BiomeTypes.DESERT;
        BiomeType differentFromTarget = BiomeTypes.JUNGLE;
        try {
            // Make sure a change is observable wherever the neighbourhood was already a desert
            world.setBiome(pos, differentFromTarget);
            world.setBiome(pos, target);

            for (int i = 0; i < probes.size(); i++) {
                BlockVector3 probe = probes.get(i);
                boolean inCell = probe.x() >> 2 == cellMin.x() >> 2
                    && probe.y() >> 2 == cellMin.y() >> 2
                    && probe.z() >> 2 == cellMin.z() >> 2;
                BiomeType expected = inCell ? target : originals.get(i);
                helper.assertValueEqual(world.getBiome(probe), expected, "biome at " + probe + (inCell ? " (in cell)" : " (outside cell)"));
            }
        } finally {
            for (int i = 0; i < probes.size(); i++) {
                world.setBiome(probes.get(i), originals.get(i));
            }
        }
        helper.succeed();
    }

    @GameTest
    public void worldBiomeMatchesNativeBiome(GameTestHelper helper) {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockPos nativePos = helper.absolutePos(TEST_POS);
        BlockVector3 pos = FabricAdapter.get().adapt(nativePos);

        BiomeType original = world.getBiome(pos);
        BiomeType target = BiomeTypes.SNOWY_PLAINS.equals(original) ? BiomeTypes.DESERT : BiomeTypes.SNOWY_PLAINS;
        try {
            world.setBiome(pos, target);
            // Not Level#getBiome, which blurs the result with neighbouring cells
            String nativeId = helper.getLevel()
                .getNoiseBiome(nativePos.getX() >> 2, nativePos.getY() >> 2, nativePos.getZ() >> 2)
                .unwrapKey().orElseThrow().identifier().toString();
            helper.assertValueEqual(nativeId, target.id(), "native biome after WorldEdit set it");
        } finally {
            world.setBiome(pos, original);
        }
        helper.succeed();
    }
}
