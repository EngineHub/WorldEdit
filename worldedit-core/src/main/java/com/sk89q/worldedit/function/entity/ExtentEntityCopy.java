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

package com.sk89q.worldedit.function.entity;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Direction.Flag;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinTagType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Copies entities provided to the function to the provided destination
 * {@code Extent}.
 */
public class ExtentEntityCopy implements EntityFunction {

    private final Extent destination;
    private final Vector3 from;
    private final Vector3 to;
    private final Transform transform;
    private boolean removing;

    /**
     * Create a new instance.
     *
     * @param from the from position
     * @param destination the destination {@code Extent}
     * @param to the destination position
     * @param transform the transformation to apply to both position and orientation
     */
    public ExtentEntityCopy(Vector3 from, Extent destination, Vector3 to, Transform transform) {
        checkNotNull(from);
        checkNotNull(destination);
        checkNotNull(to);
        checkNotNull(transform);
        this.destination = destination;
        this.from = from;
        this.to = to;
        this.transform = transform;
    }

    /**
     * Return whether entities that are copied should be removed.
     *
     * @return true if removing
     */
    public boolean isRemoving() {
        return removing;
    }

    /**
     * Set whether entities that are copied should be removed.
     *
     * @param removing true if removing
     */
    public void setRemoving(boolean removing) {
        this.removing = removing;
    }

    @Override
    public boolean apply(Entity entity) throws WorldEditException {
        BaseEntity state = entity.getState();
        if (state != null) {
            Location newLocation;
            Location location = entity.getLocation();
            // If the entity has stored the location in the NBT data, we use that location
            LinCompoundTag tag = state.getNbt();
            boolean hasTilePosition = false;
            if (tag != null
                && tag.value().get("TileX") instanceof LinNumberTag<?> tagX
                && tag.value().get("TileY") instanceof LinNumberTag<?> tagY
                && tag.value().get("TileZ") instanceof LinNumberTag<?> tagZ
            ) {
                location = location.setPosition(Vector3.at(
                    tagX.value().intValue(),
                    tagY.value().intValue(),
                    tagZ.value().intValue()
                ).add(0.5, 0.5, 0.5));
                hasTilePosition = true;
            }

            Vector3 pivot = from.round().add(0.5, 0.5, 0.5);
            Vector3 newPosition = transform.apply(location.toVector().subtract(pivot));
            if (hasTilePosition) {
                newPosition = newPosition.subtract(0.5, 0.5, 0.5);
            }
            Vector3 newDirection;

            newDirection = transform.isIdentity()
                ? entity.getLocation().getDirection()
                : transform.apply(location.getDirection()).subtract(transform.apply(Vector3.ZERO)).normalize();
            newLocation = new Location(destination, newPosition.add(to.round().add(0.5, 0.5, 0.5)), newDirection);

            // Some entities store their position data in NBT
            state = transformNbtData(state);

            boolean success = destination.createEntity(newLocation, state) != null;

            // Remove
            if (isRemoving() && success) {
                entity.remove();
            }

            return success;
        } else {
            return false;
        }
    }

    /**
     * Transform NBT data in the given entity state and return a new instance
     * if the NBT data needs to be transformed.
     *
     * @param state the existing state
     * @return a new state or the existing one
     */
    private BaseEntity transformNbtData(BaseEntity state) {
        LinCompoundTag tag = state.getNbt();

        if (tag != null) {
            // Handle leashed entities
            LinCompoundTag leashCompound = tag.findTag("Leash", LinTagType.compoundTag());
            if (leashCompound != null) {
                if (tag.value().get("X") instanceof LinNumberTag<?> tagX
                    && tag.value().get("Y") instanceof LinNumberTag<?> tagY
                    && tag.value().get("Z") instanceof LinNumberTag<?> tagZ
                ) {
                    // leashed to a fence
                    Vector3 tilePosition = Vector3.at(
                        tagX.value().intValue(), tagY.value().intValue(), tagZ.value().intValue()
                    );
                    BlockVector3 newLeash = transform.apply(tilePosition.subtract(from)).add(to).toBlockPoint();
                    return new BaseEntity(state.getType(), LazyReference.computed(tag.toBuilder()
                        .put("Leash", leashCompound.toBuilder()
                            .putInt("X", newLeash.x())
                            .putInt("Y", newLeash.y())
                            .putInt("Z", newLeash.z())
                            .build()
                        ).build()));
                }
            }

            // Handle hanging entities (paintings, item frames, etc.)

            if (tag.value().get("TileX") instanceof LinNumberTag<?> tagX
                && tag.value().get("TileY") instanceof LinNumberTag<?> tagY
                && tag.value().get("TileZ") instanceof LinNumberTag<?> tagZ) {
                Vector3 tilePosition = Vector3.at(
                    tagX.value().intValue(), tagY.value().intValue(), tagZ.value().intValue()
                );
                BlockVector3 newTilePosition = transform.apply(tilePosition.subtract(from)).add(to).toBlockPoint();

                LinCompoundTag.Builder builder = tag.toBuilder()
                    .putInt("TileX", newTilePosition.x())
                    .putInt("TileY", newTilePosition.y())
                    .putInt("TileZ", newTilePosition.z());

                if (tryGetFacingData(tag) instanceof FacingTagData(String facingKey, LinNumberTag<?> tagFacing)) {
                    boolean isPainting = state.getType() == EntityTypes.PAINTING; // Paintings have different facing values
                    Direction direction = isPainting
                        ? MCDirections.fromHorizontalHanging(tagFacing.value().intValue())
                        : MCDirections.fromHanging(tagFacing.value().intValue());

                    if (direction != null) {
                        Vector3 vector = transform.apply(direction.toVector()).subtract(transform.apply(Vector3.ZERO)).normalize();
                        Direction newDirection = Direction.findClosest(vector, Flag.CARDINAL);

                        if (newDirection != null) {
                            byte facingValue = (byte) (
                                isPainting
                                    ? MCDirections.toHorizontalHanging(newDirection)
                                    : MCDirections.toHanging(newDirection)
                            );
                            builder.putByte(facingKey, facingValue);
                        }
                    }
                }

                return new BaseEntity(state.getType(), LazyReference.computed(builder.build()));
            }
        }

        return state;
    }

    private record FacingTagData(String facingKey, LinNumberTag<?> tagFacing) {
    }

    private static FacingTagData tryGetFacingData(LinCompoundTag tag) {
        if (tag.value().get("Facing") instanceof LinNumberTag<?> tagFacingCapital) {
            return new FacingTagData("Facing", tagFacingCapital);
        } else if (tag.value().get("facing") instanceof LinNumberTag<?> tagFacingLower) {
            return new FacingTagData("facing", tagFacingLower);
        } else {
            return null;
        }
    }
}
