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

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.List;

/**
 * Sets blocks that carry NBT through the platform world and checks both the real block
 * entity and what WorldEdit reads back (which is what a copy would see).
 */
public class BlockEntityNbtGameTest {
    private static final BlockPos TEST_POS = new BlockPos(3, 3, 3);

    @GameTest
    public void signWithMixedComponentListWithoutSideEffects(GameTestHelper helper) throws WorldEditException {
        checkSignWithMixedComponentList(helper, SideEffectSet.none());
    }

    @GameTest
    public void signWithMixedComponentListWithDefaultSideEffects(GameTestHelper helper) throws WorldEditException {
        checkSignWithMixedComponentList(helper, SideEffectSet.defaults());
    }

    private void checkSignWithMixedComponentList(GameTestHelper helper, SideEffectSet sideEffects) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 pos = FabricAdapter.get().adapt(helper.absolutePos(TEST_POS));
        helper.setBlock(TEST_POS.below(), Blocks.STONE);

        // Plain-string components are stored by Minecraft as strings, styled ones as compounds, so the
        // list is heterogeneous. Lin lists can't be, so such strings are wrapped as {"": <tag>} (#2777).
        LinListTag<LinCompoundTag> messages = LinListTag.<LinCompoundTag>builder(LinTagType.compoundTag())
            .add(LinCompoundTag.builder().putString("", "plain line").build())
            .add(LinCompoundTag.builder().putString("text", "styled line").putString("color", "red").build())
            .add(LinCompoundTag.builder().putString("", "another plain line").build())
            .add(LinCompoundTag.builder().putString("text", "last").putByte("bold", (byte) 1).build())
            .build();
        LinCompoundTag frontText = LinCompoundTag.builder()
            .put("messages", messages)
            .putString("color", "blue")
            .putByte("has_glowing_text", (byte) 1)
            .build();
        LinCompoundTag signNbt = LinCompoundTag.builder().put("front_text", frontText).build();

        world.setBlock(pos, BlockTypes.OAK_SIGN.getDefaultState().toBaseBlock(signNbt), sideEffects);

        SignBlockEntity sign = helper.getBlockEntity(TEST_POS, SignBlockEntity.class);
        SignText front = sign.getText(SignTextSlot.FRONT);
        List<Component> actual = front.getMessages(false);
        helper.assertValueEqual(actual.size(), 4, "sign line count");
        helper.assertValueEqual(actual.get(0).getString(), "plain line", "line 1");
        helper.assertValueEqual(actual.get(1).getString(), "styled line", "line 2");
        helper.assertValueEqual(actual.get(2).getString(), "another plain line", "line 3");
        helper.assertValueEqual(actual.get(3).getString(), "last", "line 4");
        helper.assertTrue(front.hasGlowingText(), "glowing text was lost");
        helper.assertValueEqual(front.getColor().getSerializedName(), "blue", "text colour");

        // And back out again, as a copy would read it
        LinCompoundTag readFront = readNbt(helper, world, pos).findTag("front_text", LinTagType.compoundTag());
        helper.assertTrue(readFront != null, "front_text is missing from the NBT WorldEdit reads back");
        LinListTag<?> readMessages = readFront.findTag("messages", LinTagType.listTag());
        helper.assertTrue(readMessages != null && readMessages.value().size() == 4, "messages read back: " + readMessages);
        // Plain strings come back wrapped as {"": <string>}, styled components as they are
        helper.assertValueEqual(readComponentText(helper, readMessages, 0, ""), "plain line", "line 1 read back");
        helper.assertValueEqual(readComponentText(helper, readMessages, 1, "text"), "styled line", "line 2 read back");
        helper.assertValueEqual(readComponentText(helper, readMessages, 2, ""), "another plain line", "line 3 read back");
        helper.assertValueEqual(readComponentText(helper, readMessages, 3, "text"), "last", "line 4 read back");

        helper.succeed();
    }

    @GameTest
    public void chestContentsWithoutSideEffects(GameTestHelper helper) throws WorldEditException {
        checkChestContents(helper, SideEffectSet.none());
    }

    @GameTest
    public void chestContentsWithDefaultSideEffects(GameTestHelper helper) throws WorldEditException {
        checkChestContents(helper, SideEffectSet.defaults());
    }

    private void checkChestContents(GameTestHelper helper, SideEffectSet sideEffects) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 pos = FabricAdapter.get().adapt(helper.absolutePos(TEST_POS));

        LinCompoundTag nbt = LinCompoundTag.builder()
            .put("Items", LinListTag.<LinCompoundTag>builder(LinTagType.compoundTag())
                .add(LinCompoundTag.builder()
                    .putByte("Slot", (byte) 0).putString("id", "minecraft:diamond").putInt("count", 3).build())
                .add(LinCompoundTag.builder()
                    .putByte("Slot", (byte) 26).putString("id", "minecraft:stick").putInt("count", 64).build())
                .build())
            .build();

        world.setBlock(pos, BlockTypes.CHEST.getDefaultState().toBaseBlock(nbt), sideEffects);

        ChestBlockEntity chest = helper.getBlockEntity(TEST_POS, ChestBlockEntity.class);
        helper.assertTrue(chest.getItem(0).is(Items.DIAMOND), "slot 0 is not a diamond");
        helper.assertValueEqual(chest.getItem(0).getCount(), 3, "slot 0 count");
        helper.assertTrue(chest.getItem(26).is(Items.STICK), "slot 26 is not a stick");
        helper.assertValueEqual(chest.getItem(26).getCount(), 64, "slot 26 count");
        helper.assertTrue(chest.getItem(1).isEmpty(), "slot 1 should be empty");

        LinListTag<?> items = readNbt(helper, world, pos).findTag("Items", LinTagType.listTag());
        helper.assertTrue(items != null && items.value().size() == 2, "expected two items read back, got " + items);

        helper.succeed();
    }

    @GameTest
    public void skullDataWithoutSideEffects(GameTestHelper helper) throws WorldEditException {
        checkSkullData(helper, SideEffectSet.none());
    }

    @GameTest
    public void skullDataWithDefaultSideEffects(GameTestHelper helper) throws WorldEditException {
        checkSkullData(helper, SideEffectSet.defaults());
    }

    private void checkSkullData(GameTestHelper helper, SideEffectSet sideEffects) throws WorldEditException {
        World world = FabricAdapter.get().fromNativeWorld(helper.getLevel());
        BlockVector3 pos = FabricAdapter.get().adapt(helper.absolutePos(TEST_POS));

        LinCompoundTag nbt = LinCompoundTag.builder()
            .putString("note_block_sound", "minecraft:entity.pig.ambient")
            .putString("custom_name", "Some Head")
            .build();

        world.setBlock(pos, BlockTypes.SKELETON_SKULL.getDefaultState().toBaseBlock(nbt), sideEffects);

        SkullBlockEntity skull = helper.getBlockEntity(TEST_POS, SkullBlockEntity.class);
        helper.assertValueEqual(skull.getNoteBlockSound(), Identifier.parse("minecraft:entity.pig.ambient"), "sound");

        LinCompoundTag readBack = readNbt(helper, world, pos);
        helper.assertValueEqual(readString(helper, readBack, "note_block_sound"), "minecraft:entity.pig.ambient", "sound read back");
        helper.assertValueEqual(readString(helper, readBack, "custom_name"), "Some Head", "name read back");

        helper.succeed();
    }

    private static String readComponentText(GameTestHelper helper, LinListTag<?> messages, int index, String key) {
        helper.assertTrue(messages.value().get(index) instanceof LinCompoundTag, "line " + (index + 1) + " is not a compound");
        return readString(helper, (LinCompoundTag) messages.value().get(index), key);
    }

    private static LinCompoundTag readNbt(GameTestHelper helper, World world, BlockVector3 pos) {
        BaseBlock block = world.getFullBlock(pos);
        helper.assertTrue(block.getNbt() != null, "WorldEdit read no NBT back for " + block);
        return block.getNbt();
    }

    private static String readString(GameTestHelper helper, LinCompoundTag tag, String key) {
        LinStringTag value = tag.findTag(key, LinTagType.stringTag());
        helper.assertTrue(value != null, key + " is missing from the NBT WorldEdit reads back");
        return value.value();
    }
}
