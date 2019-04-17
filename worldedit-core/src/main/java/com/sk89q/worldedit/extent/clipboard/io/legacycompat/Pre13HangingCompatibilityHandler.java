package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.world.entity.EntityType;

public class Pre13HangingCompatibilityHandler implements EntityNBTCompatibilityHandler {

    @Override
    public boolean isAffectedEntity(EntityType type, CompoundTag entityTag) {
        return entityTag.getValue().get("Facing") instanceof ByteTag;
    }

    @Override
    public CompoundTag updateNBT(EntityType type, CompoundTag entityTag) {
        int newFacing = MCDirections.toHanging(
            MCDirections.fromPre13Hanging(entityTag.getByte("Facing"))
        );
        return entityTag.createBuilder()
            .putByte("Facing", (byte) newFacing)
            .build();
    }

}
