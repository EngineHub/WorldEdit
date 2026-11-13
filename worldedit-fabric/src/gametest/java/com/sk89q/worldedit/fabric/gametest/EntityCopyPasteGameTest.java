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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.entity.decoration.painting.PaintingVariants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntArrayTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Copies a region holding hanging, block-aligned, leashed and free entities to the clipboard
 * and pastes it elsewhere under rotations and flips, checking each pasted entity against the
 * transformed original. Expectations are derived from the transform alone: every entity's
 * bounding box must be the transformed original bounding box.
 */
public class EntityCopyPasteGameTest {
    /** Source region corners and the paste origin, relative to the test structure. */
    private static final BlockPos SOURCE_MIN = new BlockPos(0, 0, 0);
    private static final BlockPos SOURCE_MAX = new BlockPos(2, 2, 2);
    private static final BlockPos SOURCE_CENTER = new BlockPos(1, 1, 1);
    private static final BlockPos PASTE_AT = new BlockPos(5, 1, 5);

    /**
     * The fence the cow is leashed to, and the block it considers home.
     */
    private static final BlockPos FENCE_POS = new BlockPos(2, 0, 0);
    private static final BlockPos HOME_POS = new BlockPos(0, 0, 0);

    private static final String FRAME_WALL = "frame_wall";
    private static final String FRAME_FLOOR = "frame_floor";
    private static final String PAINTING_WIDE = "painting_wide";
    private static final String PAINTING_BIG = "painting_big";
    private static final String PAINTING_SMALL = "painting_small";
    private static final String STAND = "stand";
    private static final String STAND_ON_CORNER = "stand_on_corner";
    private static final String COW = "cow";

    private static final int FLOOR_FRAME_ROTATION = 1;

    private static final Set<String> EVEN_SIZED_PAINTINGS = Set.of(PAINTING_WIDE, PAINTING_BIG);
    private static final Set<String> EVERYTHING_BUT_EVEN_SIZED_PAINTINGS = Set.of(
        FRAME_WALL, FRAME_FLOOR, PAINTING_SMALL, STAND, STAND_ON_CORNER, COW
    );

    @GameTest
    public void identity(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform());
    }

    @GameTest
    public void rotate90(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().rotateY(90));
    }

    @GameTest
    public void rotate180(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().rotateY(180));
    }

    @GameTest
    public void rotate270(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().rotateY(270));
    }

    @GameTest
    public void flipX(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().scale(-1, 1, 1), EVEN_SIZED_PAINTINGS);
    }

    @GameTest
    public void flipZ(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().scale(1, 1, -1), EVEN_SIZED_PAINTINGS);
    }

    /**
     * Known bug, so this is not a required test (its failure is reported but does not fail the run): flipping a painting that is 2 blocks wide or tall leaves
     * it one block off along its width axis, so it no longer covers the blocks it should.
     *
     * <p>
     * The source has a {@code courbet} (2x1) and a {@code match} (2x2) painting. After a flip on X
     * (or Z, or a flip combined with a rotation) each pasted painting's bounding box is the mirror
     * image of the original shifted by one block. {@code ExtentEntityCopy} mirrors only the anchor
     * block ({@code block_pos}), yet a painting always extends from its anchor in a fixed direction
     * relative to its facing, so the mirror image of an even-sized painting needs a different anchor.
     * Odd-sized paintings, item frames and every pure rotation are unaffected.
     * </p>
     */
    @GameTest(required = false)
    public void flipXEvenSizedPaintings(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().scale(-1, 1, 1), EVERYTHING_BUT_EVEN_SIZED_PAINTINGS);
    }

    /**
     * Known bug, see {@link #flipXEvenSizedPaintings}.
     */
    @GameTest(required = false)
    public void flipZEvenSizedPaintings(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().scale(1, 1, -1), EVERYTHING_BUT_EVEN_SIZED_PAINTINGS);
    }

    /**
     * Known bug, see {@link #flipXEvenSizedPaintings}.
     */
    @GameTest(required = false)
    public void flipXAndRotate90EvenSizedPaintings(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(
            helper, new AffineTransform().scale(-1, 1, 1).rotateY(90), EVERYTHING_BUT_EVEN_SIZED_PAINTINGS
        );
    }

    @GameTest
    public void flipXAndRotate90(GameTestHelper helper) throws WorldEditException {
        copyPasteAndCheck(helper, new AffineTransform().scale(-1, 1, 1).rotateY(90), EVEN_SIZED_PAINTINGS);
    }

    /**
     * An item in a floor-mounted frame has to turn with the paste. The sense of the turn isn't
     * pinned down here, only that quarter turns land on the right set of steps and differ.
     */
    @GameTest
    public void floorItemFrameRotation(GameTestHelper helper) throws WorldEditException {
        List<Original> originals = buildSource(helper);
        helper.assertValueEqual(
            pasteAndReadFloorFrameRotation(helper, originals, new AffineTransform()),
            FLOOR_FRAME_ROTATION,
            "item rotation after identity paste"
        );
        helper.succeed();
    }

    @GameTest
    public void floorItemFrameRotation180(GameTestHelper helper) throws WorldEditException {
        List<Original> originals = buildSource(helper);
        helper.assertValueEqual(
            pasteAndReadFloorFrameRotation(helper, originals, new AffineTransform().rotateY(180)),
            (FLOOR_FRAME_ROTATION + 4) % 8,
            "item rotation after a half turn"
        );
        helper.succeed();
    }

    @GameTest
    public void floorItemFrameRotationQuarterTurns(GameTestHelper helper) throws WorldEditException {
        List<Original> originals = buildSource(helper);
        int clockwise = pasteAndReadFloorFrameRotation(helper, originals, new AffineTransform().rotateY(90));
        // The next paste lands on the same spot, so clear everything this one made away first
        for (Entity entity : entitiesIn(helper.getLevel(), pasteBox(helper))) {
            if (originals.stream().noneMatch(o -> o.id.equals(entity.getUUID()))) {
                entity.discard();
            }
        }
        int counter = pasteAndReadFloorFrameRotation(helper, originals, new AffineTransform().rotateY(-90));

        int steps1 = Math.floorMod(clockwise - FLOOR_FRAME_ROTATION, 8);
        int steps2 = Math.floorMod(counter - FLOOR_FRAME_ROTATION, 8);
        helper.assertTrue(
            (steps1 == 2 && steps2 == 6) || (steps1 == 6 && steps2 == 2),
            "quarter turns should move the item 2 and 6 steps, got " + steps1 + " and " + steps2
        );
        helper.succeed();
    }

    private int pasteAndReadFloorFrameRotation(
        GameTestHelper helper, List<Original> originals, Transform transform
    ) throws WorldEditException {
        copyAndPaste(helper, transform);
        Entity entity = findPasted(helper, FRAME_FLOOR, originals);
        helper.assertTrue(entity instanceof ItemFrame, "floor frame was not pasted as an item frame: " + entity);
        return ((ItemFrame) entity).getRotation();
    }

    private void copyPasteAndCheck(GameTestHelper helper, Transform transform) throws WorldEditException {
        copyPasteAndCheck(helper, transform, Set.of());
    }

    private void copyPasteAndCheck(
        GameTestHelper helper, Transform transform, Set<String> ignored
    ) throws WorldEditException {
        List<Original> originals = buildSource(helper);
        copyAndPaste(helper, transform);

        Vec3 sourcePivot = helper.absoluteVec(Vec3.atCenterOf(SOURCE_CENTER));
        Vec3 pastePivot = helper.absoluteVec(Vec3.atCenterOf(PASTE_AT));

        List<String> problems = new ArrayList<>();
        for (Original original : originals) {
            if (ignored.contains(original.name)) {
                continue;
            }
            Entity pasted = findPasted(helper, original.name, originals);
            if (pasted == null) {
                problems.add(original.name + " was not pasted");
                continue;
            }
            if (original.facing != null) {
                // Hanging entities are defined by the block(s) they cover, so the whole box has to move
                AABB expectedBox = transformBox(original.box, transform, sourcePivot, pastePivot);
                if (!sameBox(expectedBox, pasted.getBoundingBox())) {
                    problems.add(original.name + " occupies " + pasted.getBoundingBox() + ", expected " + expectedBox);
                }
                Direction expectedFacing = transformDirection(original.facing, transform);
                if (!(pasted instanceof HangingEntity hanging)) {
                    problems.add(original.name + " was pasted as " + pasted.getType());
                } else if (hanging.getDirection() != expectedFacing) {
                    problems.add(original.name + " faces " + hanging.getDirection() + ", expected " + expectedFacing);
                }
            } else {
                // Free entities keep standing upright, so only where their feet are moves
                Vector3 expected = transform.apply(toVector(original.position.subtract(sourcePivot)))
                    .add(toVector(pastePivot));
                if (toVector(pasted.position()).distance(expected) > 1e-4) {
                    problems.add(original.name + " is at " + pasted.position() + ", expected " + expected);
                }
            }
            if (original.yaw != null) {
                float expectedYaw = transformYaw(original.yaw, transform);
                if (Math.abs(Mth.wrapDegrees(pasted.getYRot() - expectedYaw)) > 1) {
                    problems.add(original.name + " has yaw " + pasted.getYRot() + ", expected " + expectedYaw);
                }
            }
        }

        if (!ignored.contains(COW)) {
            Entity cow = findPasted(helper, COW, originals);
            if (cow != null) {
                checkLeashAndHome(helper, transform, cow, problems);
            }
        }

        helper.assertTrue(problems.isEmpty(), problems.size() + " problem(s): " + problems);
        helper.succeed();
    }

    /**
     * The leash and home positions are block positions stored in NBT, so they have to be moved too.
     */
    private void checkLeashAndHome(GameTestHelper helper, Transform transform, Entity cow, List<String> problems) {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 fence = relativeToPaste(helper, FENCE_POS, transform);
        BlockVector3 home = relativeToPaste(helper, HOME_POS, transform);

        LinCompoundTag nbt = world.getEntities(new CuboidRegion(
            FabricAdapter.get().adapt(cow.blockPosition()), FabricAdapter.get().adapt(cow.blockPosition())
        )).stream()
            .filter(e -> e.getState() != null && e.getState().getNbt() != null
                && e.getLocation().toVector().distance(toVector(cow.position())) < 1e-6)
            .map(e -> e.getState().getNbt())
            .findFirst()
            .orElse(null);
        if (nbt == null) {
            problems.add("could not read the pasted cow's NBT");
            return;
        }
        expectIntArray(nbt, "leash", fence, problems);
        expectIntArray(nbt, "home_pos", home, problems);
    }

    private void expectIntArray(LinCompoundTag nbt, String key, BlockVector3 expected, List<String> problems) {
        LinIntArrayTag tag = nbt.findTag(key, LinTagType.intArrayTag());
        if (tag == null) {
            problems.add(key + " is missing from the pasted cow, expected " + expected);
            return;
        }
        int[] value = tag.value();
        if (value.length != 3 || value[0] != expected.x() || value[1] != expected.y() || value[2] != expected.z()) {
            problems.add(key + " is " + Arrays.toString(value) + ", expected " + expected);
        }
    }

    /**
     * Where a block at the given structure-relative position in the source ends up.
     */
    private BlockVector3 relativeToPaste(GameTestHelper helper, BlockPos sourcePos, Transform transform) {
        FabricAdapter adapter = FabricAdapter.get();
        Vector3 offset = adapter.adapt(helper.absolutePos(sourcePos)).toVector3()
            .subtract(adapter.adapt(helper.absolutePos(SOURCE_CENTER)).toVector3());
        // Rotations and flips of whole blocks are exact up to floating point noise, so round rather than truncate
        Vector3 moved = transform.apply(offset);
        return adapter.adapt(helper.absolutePos(PASTE_AT))
            .add(BlockVector3.at((int) Math.round(moved.x()), (int) Math.round(moved.y()), (int) Math.round(moved.z())));
    }

    private void copyAndPaste(GameTestHelper helper, Transform transform) throws WorldEditException {
        FabricAdapter adapter = FabricAdapter.get();
        World world = adapter.fromNativeWorld(helper.getLevel());
        CuboidRegion region = new CuboidRegion(
            world,
            adapter.adapt(helper.absolutePos(SOURCE_MIN)),
            adapter.adapt(helper.absolutePos(SOURCE_MAX))
        );
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOrigin(adapter.adapt(helper.absolutePos(SOURCE_CENTER)));

        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()) {
            ForwardExtentCopy copy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
            copy.setCopyingEntities(true);
            Operations.complete(copy);

            ClipboardHolder holder = new ClipboardHolder(clipboard);
            holder.setTransform(transform);
            Operations.complete(holder.createPaste(session)
                .to(adapter.adapt(helper.absolutePos(PASTE_AT)))
                .copyEntities(true)
                .build());
        }
    }

    private record Original(String name, UUID id, Vec3 position, AABB box, Direction facing, Float yaw) {
    }

    /**
     * Spawn the entities to copy. Returns what each one looked like before the copy.
     */
    private List<Original> buildSource(GameTestHelper helper) {
        // Nothing backs the hanging entities, which vanilla would drop on its next survival check, so
        // everything here and in the assertions has to happen in the tick that spawns them.
        ServerLevel level = helper.getLevel();
        List<Original> originals = new ArrayList<>();

        helper.setBlock(FENCE_POS, Blocks.OAK_FENCE);

        ItemFrame wallFrame = new ItemFrame(level, helper.absolutePos(new BlockPos(0, 1, 0)), Direction.EAST);
        wallFrame.setItem(new ItemStack(Items.DIAMOND), false);
        add(level, originals, FRAME_WALL, wallFrame, wallFrame.getDirection(), null);

        ItemFrame floorFrame = new ItemFrame(level, helper.absolutePos(new BlockPos(1, 0, 1)), Direction.UP);
        floorFrame.setItem(new ItemStack(Items.DIAMOND), false);
        floorFrame.setRotation(FLOOR_FRAME_ROTATION);
        add(level, originals, FRAME_FLOOR, floorFrame, floorFrame.getDirection(), null);

        // 2 wide, 2x2 and 1x1: even sizes are the ones that snap to the wrong block when mispositioned
        add(level, originals, PAINTING_WIDE, painting(helper, PaintingVariants.COURBET, new BlockPos(1, 1, 2), Direction.NORTH), Direction.NORTH, null);
        add(level, originals, PAINTING_BIG, painting(helper, PaintingVariants.MATCH, new BlockPos(2, 1, 1), Direction.WEST), Direction.WEST, null);
        add(level, originals, PAINTING_SMALL, painting(helper, PaintingVariants.KEBAB, new BlockPos(0, 2, 1), Direction.EAST), Direction.EAST, null);

        ArmorStand stand = new ArmorStand(level, 0, 0, 0);
        Vec3 standPos = helper.absoluteVec(new Vec3(1.5, 0, 1.5));
        stand.snapTo(standPos.x, standPos.y, standPos.z, 30, 0);
        add(level, originals, STAND, stand, null, 30f);

        // Standing exactly on a block corner rather than a block centre
        ArmorStand cornerStand = new ArmorStand(level, 0, 0, 0);
        Vec3 corner = helper.absoluteVec(new Vec3(2, 0, 2));
        cornerStand.snapTo(corner.x, corner.y, corner.z, 135, 0);
        add(level, originals, STAND_ON_CORNER, cornerStand, null, 135f);

        Cow cow = helper.spawnWithNoFreeWill(EntityTypes.COW, new BlockPos(0, 0, 2));
        cow.setCustomName(Component.literal(COW));
        cow.setHomeTo(helper.absolutePos(HOME_POS), 4);
        cow.setLeashedTo(LeashFenceKnotEntity.getOrCreateKnot(level, helper.absolutePos(FENCE_POS)), true);
        originals.add(new Original(COW, cow.getUUID(), cow.position(), cow.getBoundingBox(), null, null));

        return originals;
    }

    private Painting painting(GameTestHelper helper, ResourceKey<PaintingVariant> variant, BlockPos relative, Direction facing) {
        return new Painting(
            helper.getLevel(),
            helper.absolutePos(relative),
            facing,
            helper.getLevel().registryAccess().lookupOrThrow(Registries.PAINTING_VARIANT).getOrThrow(variant)
        );
    }

    private void add(ServerLevel level, List<Original> originals, String name, Entity entity, Direction facing, Float yaw) {
        entity.setCustomName(Component.literal(name));
        level.addFreshEntity(entity);
        originals.add(new Original(name, entity.getUUID(), entity.position(), entity.getBoundingBox(), facing, yaw));
    }

    /**
     * Where pasted entities can be, kept inside the test's own structure so a neighbouring
     * test's identically named entities can't be mistaken for ours.
     */
    private AABB pasteBox(GameTestHelper helper) {
        AABB box = new AABB(helper.absolutePos(PASTE_AT)).inflate(2.5);
        AABB bounds = helper.getBounds();
        return new AABB(
            Math.max(box.minX, bounds.minX), Math.max(box.minY, bounds.minY), Math.max(box.minZ, bounds.minZ),
            Math.min(box.maxX, bounds.maxX), Math.min(box.maxY, bounds.maxY), Math.min(box.maxZ, bounds.maxZ)
        );
    }

    private List<Entity> entitiesIn(ServerLevel level, AABB box) {
        return level.getEntities((Entity) null, box, _ -> true);
    }

    /**
     * Find the entity with the given name that isn't the one it was copied from.
     */
    private Entity findPasted(GameTestHelper helper, String name, List<Original> originals) {
        UUID source = originals.stream().filter(o -> o.name.equals(name)).findFirst().orElseThrow().id;
        for (Entity entity : entitiesIn(helper.getLevel(), pasteBox(helper))) {
            if (!entity.getUUID().equals(source)
                && entity.getCustomName() != null && entity.getCustomName().getString().equals(name)) {
                return entity;
            }
        }
        return null;
    }

    private static Vector3 toVector(Vec3 vec) {
        return Vector3.at(vec.x, vec.y, vec.z);
    }

    private static AABB transformBox(AABB box, Transform transform, Vec3 sourcePivot, Vec3 pastePivot) {
        double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        for (int i = 0; i < 8; i++) {
            Vector3 corner = Vector3.at(
                (i & 1) == 0 ? box.minX : box.maxX,
                (i & 2) == 0 ? box.minY : box.maxY,
                (i & 4) == 0 ? box.minZ : box.maxZ
            );
            Vector3 moved = transform.apply(corner.subtract(toVector(sourcePivot))).add(toVector(pastePivot));
            double[] values = {moved.x(), moved.y(), moved.z()};
            for (int axis = 0; axis < 3; axis++) {
                min[axis] = Math.min(min[axis], values[axis]);
                max[axis] = Math.max(max[axis], values[axis]);
            }
        }
        return new AABB(min[0], min[1], min[2], max[0], max[1], max[2]);
    }

    private static boolean sameBox(AABB a, AABB b) {
        double epsilon = 1e-4;
        return Math.abs(a.minX - b.minX) < epsilon && Math.abs(a.minY - b.minY) < epsilon
            && Math.abs(a.minZ - b.minZ) < epsilon && Math.abs(a.maxX - b.maxX) < epsilon
            && Math.abs(a.maxY - b.maxY) < epsilon && Math.abs(a.maxZ - b.maxZ) < epsilon;
    }

    private static Direction transformDirection(Direction direction, Transform transform) {
        Vector3 moved = transform.apply(Vector3.at(direction.getStepX(), direction.getStepY(), direction.getStepZ()))
            .subtract(transform.apply(Vector3.ZERO));
        return Direction.getApproximateNearest(moved.x(), moved.y(), moved.z());
    }

    /**
     * Minecraft yaw 0 faces +Z, 90 faces -X.
     */
    private static float transformYaw(float yaw, Transform transform) {
        double radians = Math.toRadians(yaw);
        Vector3 moved = transform.apply(Vector3.at(-Math.sin(radians), 0, Math.cos(radians)))
            .subtract(transform.apply(Vector3.ZERO));
        return (float) Math.toDegrees(Math.atan2(-moved.x(), moved.z()));
    }
}
