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

package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.fluid.FluidState;
import com.sk89q.worldedit.world.fluid.FluidType;
import com.sk89q.worldedit.world.fluid.FluidTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WaterSupplyingBlockMaskTest extends BaseWorldEditTest {

    private static final BlockVector3 POSITION = BlockVector3.at(0, 64, 0);

    @Test
    void matchesStillWater() {
        assertTrue(maskFor(FluidTypes.WATER).test(POSITION));
    }

    @Test
    void matchesFlowingWater() {
        assertTrue(maskFor(FluidTypes.FLOWING_WATER).test(POSITION));
    }

    @Test
    void doesNotMatchLava() {
        assertFalse(maskFor(FluidTypes.LAVA).test(POSITION));
        assertFalse(maskFor(FluidTypes.FLOWING_LAVA).test(POSITION));
    }

    @Test
    void doesNotMatchEmptyFluid() {
        assertFalse(maskFor(FluidTypes.EMPTY).test(POSITION));
    }

    private static WaterSupplyingBlockMask maskFor(FluidType fluidType) {
        Extent extent = mock(Extent.class);
        BlockState block = mock(BlockState.class);
        FluidState fluid = mock(FluidState.class);
        when(extent.getBlock(POSITION)).thenReturn(block);
        when(block.getFluidState()).thenReturn(fluid);
        when(fluid.getType()).thenReturn(fluidType);
        return new WaterSupplyingBlockMask(extent);
    }
}
