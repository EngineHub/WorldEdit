/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.formatting.component;

import com.google.common.base.Strings;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;

import java.util.List;

public class BlockDistributionResult extends PaginationBox {

    private final List<Countable<BlockState>> distribution;
    private final int totalBlocks;
    private final boolean separateStates;

    public BlockDistributionResult(List<Countable<BlockState>> distribution, boolean separateStates) {
        super("Block Distribution", "//distr -p %page%" + (separateStates ? " -d" : ""));
        this.distribution = distribution;
        // note: doing things like region.getArea is inaccurate for non-cuboids.
        this.totalBlocks = distribution.stream().mapToInt(Countable::getAmount).sum();
        this.separateStates = separateStates;
        setComponentsPerPage(7);
    }

    @Override
    public Component getComponent(int number) {
        Countable<BlockState> c = distribution.get(number);
        TextComponent.Builder line = TextComponent.builder();

        final int count = c.getAmount();

        final double perc = count / (double) totalBlocks * 100;
        final int maxDigits = (int) (Math.log10(totalBlocks) + 1);
        final int curDigits = (int) (Math.log10(count) + 1);
        line.append(String.format("%s%.3f%%  ", perc < 10 ? "  " : "", perc), TextColor.GOLD);
        final int space = maxDigits - curDigits;
        String pad = Strings.repeat(" ", space == 0 ? 2 : 2 * space + 1);
        line.append(String.format("%s%s", count, pad), TextColor.YELLOW);

        final BlockState state = c.getID();
        final BlockType blockType = state.getBlockType();
        TextComponent blockName = TextComponent.of(blockType.getName(), TextColor.LIGHT_PURPLE);
        TextComponent toolTip;
        if (separateStates && state != blockType.getDefaultState()) {
            toolTip = TextComponent.of(state.getAsString(), TextColor.GRAY);
            blockName = blockName.append(TextComponent.of("*", TextColor.LIGHT_PURPLE));
        } else {
            toolTip = TextComponent.of(blockType.getId(), TextColor.GRAY);
        }
        blockName = blockName.hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, toolTip));
        line.append(blockName);

        return line.build();
    }

    @Override
    public int getComponentsSize() {
        return distribution.size();
    }

    @Override
    public Component create(int page) throws InvalidComponentException {
        super.getContents().append(TextComponent.of("Total Block Count: " + totalBlocks, TextColor.GRAY))
                .append(TextComponent.newline());
        return super.create(page);
    }
}
