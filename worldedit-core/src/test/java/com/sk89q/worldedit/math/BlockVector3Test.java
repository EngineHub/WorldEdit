package com.sk89q.worldedit.math;

import com.sk89q.worldedit.util.test.VariedVectors;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("A 3D block vector")
public class BlockVector3Test {

    @VariedVectors.Test(capToVanilla = true, divisionsXZ = 50, divisionsY = 50)
    @DisplayName("survives a round-trip through long-packing")
    void longPackingRoundTrip(BlockVector3 vec) {
        assertEquals(vec, BlockVector3.fromLongPackedForm(vec.toLongPackedForm()));
    }

}
