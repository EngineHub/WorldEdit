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

package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.util.gson.GsonUtil;
import com.sk89q.worldedit.world.block.BlockState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a sign block.
 *
 * @deprecated WorldEdit does not handle interpreting NBT,
 *     deprecated for removal without replacement
 */
@Deprecated
public class SignBlock extends LegacyBaseBlockWrapper {

    private String[] text;

    private static final String EMPTY =  "{\"text\":\"\"}";

    /**
     * Construct the sign with text.
     *
     * @param blockState The block state
     * @param text lines of text
     */
    public SignBlock(BlockState blockState, String[] text) {
        super(blockState);
        if (text == null) {
            this.text = new String[] { EMPTY, EMPTY, EMPTY, EMPTY };
            return;
        }
        for (int i = 0; i < text.length; i++) {
            if (text[i].isEmpty()) {
                text[i] = EMPTY;
            } else {
                text[i] = "{\"text\":" + GsonUtil.stringValue(text[i]) + "}";
            }
        }
        this.text = text;
    }

    private boolean isLegacy() {
        int dataVersion = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion();
        return dataVersion < Constants.DATA_VERSION_MC_1_20;
    }

    /**
     * Get the text.
     *
     * @return the text
     */
    public String[] getText() {
        return text;
    }

    /**
     * Set the text.
     *
     * @param text the text to set
     */
    public void setText(String[] text) {
        if (text == null) {
            throw new IllegalArgumentException("Can't set null text for a sign");
        }
        this.text = text;
    }

    // Suppress InlineMeSuggester: This method cannot be made final due to backwards compatibility
    @SuppressWarnings("InlineMeSuggester")
    @Override
    @Deprecated
    public boolean hasNbtData() {
        return true;
    }

    @Override
    public String getNbtId() {
        return "minecraft:sign";
    }

    @Override
    @Deprecated
    public CompoundTag getNbtData() {
        Map<String, Tag<?, ?>> values = new HashMap<>();
        if (isLegacy()) {
            values.put("Text1", new StringTag(text[0]));
            values.put("Text2", new StringTag(text[1]));
            values.put("Text3", new StringTag(text[2]));
            values.put("Text4", new StringTag(text[3]));
        } else {
            ListTag<?, ?> messages = new ListTag<>(StringTag.class, Arrays.stream(text).map(StringTag::new).collect(Collectors.toList()));
            Map<String, Tag<?, ?>> frontTextTag = new HashMap<>();
            frontTextTag.put("messages", messages);
            values.put("front_text", new CompoundTag(frontTextTag));
        }
        return new CompoundTag(values);
    }

    @Override
    @Deprecated
    public void setNbtData(CompoundTag rootTag) {
        if (rootTag == null) {
            return;
        }

        Map<String, Tag<?, ?>> values = rootTag.getValue();

        Tag<?, ?> t;

        text = new String[] { EMPTY, EMPTY, EMPTY, EMPTY };

        t = values.get("id");
        if (!(t instanceof StringTag) || !((StringTag) t).getValue().equals(getNbtId())) {
            throw new RuntimeException(String.format("'%s' tile entity expected", getNbtId()));
        }

        if (isLegacy()) {
            t = values.get("Text1");
            if (t instanceof StringTag) {
                text[0] = ((StringTag) t).getValue();
            }

            t = values.get("Text2");
            if (t instanceof StringTag) {
                text[1] = ((StringTag) t).getValue();
            }

            t = values.get("Text3");
            if (t instanceof StringTag) {
                text[2] = ((StringTag) t).getValue();
            }

            t = values.get("Text4");
            if (t instanceof StringTag) {
                text[3] = ((StringTag) t).getValue();
            }
        } else {
            CompoundTag frontTextTag = (CompoundTag) values.get("front_text");
            ListTag<?, ?> messagesTag = frontTextTag.getListTag("messages");
            for (int i = 0; i < messagesTag.getValue().size(); i++) {
                StringTag tag = (StringTag) messagesTag.getValue().get(i);
                text[i] = tag.getValue();
            }
        }
    }

}
