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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Direction.Flag;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.entity.EntityTypes;
import org.enginehub.linbus.tree.LinByteTag;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinNumberTag;
import org.enginehub.linbus.tree.LinStringTag;
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
            if (tag != null) {
                if (tag.value().get("block_pos") instanceof LinIntArrayTag blockPos && blockPos.value().length == 3) {
                    // New block_pos value
                    location = location.setPosition(Vector3.at(
                            blockPos.value()[0],
                            blockPos.value()[1],
                            blockPos.value()[2]
                    ).add(0.5, 0.5, 0.5));
                    hasTilePosition = true;
                } else if (tag.value().get("TileX") instanceof LinNumberTag<?> tagX
                        && tag.value().get("TileY") instanceof LinNumberTag<?> tagY
                        && tag.value().get("TileZ") instanceof LinNumberTag<?> tagZ
                ) {
                    // Legacy TileX/Y/Z values
                    location = location.setPosition(Vector3.at(
                            tagX.value().intValue(),
                            tagY.value().intValue(),
                            tagZ.value().intValue()
                    ).add(0.5, 0.5, 0.5));
                    hasTilePosition = true;
                }
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
                    state = new BaseEntity(state.getType(), LazyReference.computed(tag.toBuilder()
                        .put("Leash", leashCompound.toBuilder()
                            .putInt("X", newLeash.x())
                            .putInt("Y", newLeash.y())
                            .putInt("Z", newLeash.z())
                            .build()
                        ).build()));
                    tag = state.getNbt();
                }
            }
            LinIntArrayTag leashArray = tag.findTag("leash", LinTagType.intArrayTag());
            if (leashArray != null) {
                Vector3 tilePosition = Vector3.at(
                        leashArray.value()[0], leashArray.value()[1], leashArray.value()[2]
                );
                BlockVector3 newLeash = transform.apply(tilePosition.subtract(from)).add(to).toBlockPoint();
                state = new BaseEntity(state.getType(), LazyReference.computed(tag.toBuilder()
                        .putIntArray("leash", new int[]{newLeash.x(), newLeash.y(), newLeash.z()})
                        .build()));
                tag = state.getNbt();
            }

            // Handle home position for mobs
            LinIntArrayTag homePosArray = tag.findTag("home_pos", LinTagType.intArrayTag());
            if (homePosArray != null) {
                Vector3 tilePosition = Vector3.at(
                        homePosArray.value()[0], homePosArray.value()[1], homePosArray.value()[2]
                );
                BlockVector3 newLeash = transform.apply(tilePosition.subtract(from)).add(to).toBlockPoint();
                state = new BaseEntity(state.getType(), LazyReference.computed(tag.toBuilder()
                        .putIntArray("home_pos", new int[]{newLeash.x(), newLeash.y(), newLeash.z()})
                        .build()));
                tag = state.getNbt();
            }

            // Handle hanging entities (paintings, item frames, etc.)

            Vector3 tilePosition = null;

            if (tag.value().get("block_pos") instanceof LinIntArrayTag blockPos) {
                tilePosition = Vector3.at(
                        blockPos.value()[0], blockPos.value()[1], blockPos.value()[2]
                );
            }

            if (tag.value().get("TileX") instanceof LinNumberTag<?> tagX
                && tag.value().get("TileY") instanceof LinNumberTag<?> tagY
                && tag.value().get("TileZ") instanceof LinNumberTag<?> tagZ) {
                tilePosition = Vector3.at(
                        tagX.value().intValue(), tagY.value().intValue(), tagZ.value().intValue()
                );
            }

            if (tilePosition != null) {
                BlockVector3 newTilePosition = transform.apply(tilePosition.subtract(from)).add(to).toBlockPoint();

                LinCompoundTag.Builder builder = tag.toBuilder();

                if (WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion() < Constants.DATA_VERSION_MC_1_21_5) {
                    // TODO remove when we drop support for 1.21.4
                    builder.putInt("TileX", newTilePosition.x())
                            .putInt("TileY", newTilePosition.y())
                            .putInt("TileZ", newTilePosition.z());
                } else {
                    builder.putIntArray("block_pos", new int[]{newTilePosition.x(), newTilePosition.y(), newTilePosition.z()});
                }

                if (tryGetFacingData(tag) instanceof FacingTagData(String facingKey, LinNumberTag<?> tagFacing)) {
                    if (state.getType() == EntityTypes.PAINTING) { // Paintings have different facing values
                        Direction direction = MCDirections.fromHorizontalHanging(tagFacing.value().intValue());
                        Vector3 vector = transform.apply(direction.toVector()).subtract(transform.apply(Vector3.ZERO)).normalize();
                        Direction newDirection = Direction.findClosest(vector, Flag.CARDINAL);
                        byte facingValue = (byte) MCDirections.toHorizontalHanging(newDirection);
                        builder.putByte(facingKey, facingValue);
                    } else {
                        Direction facingDirection = MCDirections.fromHanging(tagFacing.value().intValue());
                        Vector3 facingVector = transform.apply(facingDirection.toVector()).subtract(transform.apply(Vector3.ZERO)).normalize();
                        Direction newFacingDirection = Direction.findClosest(facingVector, Flag.CARDINAL | Flag.UPRIGHT);
                        byte facingValue = (byte) MCDirections.toHanging(newFacingDirection);
                        builder.putByte(facingKey, facingValue);

                        String itemRotationKey = "ItemRotation";
                        if (!transform.isIdentity() && tag.value().get(itemRotationKey) instanceof LinByteTag tagItemRotation) {
                            String itemId = getItemInItemFrame(tag);
                            int availableRotations = itemId != null && itemId.equals("minecraft:filled_map") ? 4 : 8;
                            Direction rotationBaseDirection =
                                    facingDirection == Direction.UP || facingDirection == Direction.DOWN
                                            ? Direction.NORTH
                                            : Direction.UP;
                            int itemRotation = tagItemRotation.value().intValue();
                            Vector3 rotationVector = getItemRotationVector(
                                    rotationBaseDirection, facingDirection.toVector(), itemRotation, availableRotations
                            );
                            Vector3 newRotationVector = transform.apply(rotationVector);
                            Direction newRotationBaseDirection =
                                    newFacingDirection == Direction.UP || newFacingDirection == Direction.DOWN
                                            ? Direction.NORTH
                                            : Direction.UP;
                            byte newItemRotation = (byte) getItemRotationSteps(
                                    newRotationBaseDirection, newFacingDirection, newRotationVector, availableRotations
                            );
                            builder.putByte(itemRotationKey, newItemRotation);
                        }

                    }
                }

                return new BaseEntity(state.getType(), LazyReference.computed(builder.build()));
            }
        }

        return state;
    }

    private Vector3 getItemRotationVector(Direction baseDirection, Vector3 facingVector, int itemRotation, int rotations) {
        Vector3 baseVec = baseDirection.toVector().normalize();
        double angle = Math.toRadians(itemRotation * (360f / rotations));
        Vector3 rotated = rotateAroundAxis(baseVec, facingVector, -angle);
        return rotated.normalize();
    }

    private Vector3 rotateAroundAxis(Vector3 vec, Vector3 axis, double angle) {
        axis = axis.normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return vec.multiply(cos).add(axis.cross(vec).multiply(sin)).add(axis.multiply(axis.dot(vec) * (1 - cos)));
    }

    private int getItemRotationSteps(Direction baseDirection, Direction facingDirection, Vector3 targetVector, int rotations) {
        Vector3 baseVec = baseDirection.toVector();

        double det = facingDirection.toVector().dot(baseVec.cross(targetVector));
        double dot = baseVec.dot(targetVector);
        double signedAngle = Math.atan2(det, dot);

        double stepsDouble = -signedAngle / (Math.PI / (rotations / 2f));
        int steps = (int) Math.round(stepsDouble) % rotations;
        if (steps < 0) {
            steps += rotations;
        }

        if (facingDirection == Direction.DOWN) {
            steps = (steps + (rotations / 2)) % rotations;
        }

        return steps;
    }

    private String getItemInItemFrame(LinCompoundTag tag) {
        if (tag.value().get("Item") instanceof LinCompoundTag tagItem) {
            if (tagItem.value().get("id") instanceof LinStringTag tagId) {
                return tagId.value();
            }
        }

        return null;
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
