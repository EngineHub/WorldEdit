package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.entity.EntityType;

public class Pre13HangingCompatibilityHandler implements EntityNBTCompatibilityHandler {

    @Override
    public boolean isAffectedEntity(EntityType type, CompoundTag tag) {
        boolean hasLegacyDirection = tag.containsKey("Dir");
        boolean hasFacing = tag.containsKey("Facing");
        return hasLegacyDirection || hasFacing;
    }

    @Override
    public CompoundTag updateNBT(EntityType type, CompoundTag tag) {
        boolean hasLegacyDirection = tag.containsKey("Dir");
        boolean hasFacing = tag.containsKey("Facing");
        int d;
        if (hasLegacyDirection) {
            d = MCDirections.fromLegacyHanging((byte) tag.asInt("Dir"));
        } else {
            d = tag.asInt("Facing");
        }

        Direction newDirection = MCDirections.fromPre13Hanging(d);

        byte hangingByte = (byte) MCDirections.toHanging(newDirection);

        CompoundTagBuilder builder = tag.createBuilder();
        builder.putByte("Direction", hangingByte);
        builder.putByte("Facing", hangingByte);
        builder.putByte("Dir", MCDirections.toLegacyHanging(MCDirections.toHanging(newDirection)));
        return builder.build();
    }

}
