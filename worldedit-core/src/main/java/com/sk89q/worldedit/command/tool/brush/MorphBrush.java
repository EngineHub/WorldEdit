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

package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

public class MorphBrush implements Brush {

    private final int minErodeFaces;
    private final int numErodeIterations;
    private final int minDilateFaces;
    private final int minDilateIterations;

    public MorphBrush(int minErodeFaces, int numErodeIterations, int minDilateFaces, int minDilateIterations) {
        this.minErodeFaces = minErodeFaces;
        this.numErodeIterations = numErodeIterations;
        this.minDilateFaces = minDilateFaces;
        this.minDilateIterations = minDilateIterations;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        editSession.morph(position, size, this.minErodeFaces, this.numErodeIterations, this.minDilateFaces, this.minDilateIterations);
    }

}
