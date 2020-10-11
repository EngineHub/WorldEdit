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
import com.sk89q.worldedit.world.block.BlockTypes;

public class ErodeBrush implements Brush {

    private final int minFillFaces;
    private final int numFillIterations;
    private final int minErodeFaces;
    private final int numErodeIterations;

    public ErodeBrush(int minFillFaces, int numFillIterations, int minErodeFaces, int numErodeIterations) {
        this.minFillFaces = minFillFaces;
        this.numFillIterations = numFillIterations;
        this.minErodeFaces = minErodeFaces;
        this.numErodeIterations = numErodeIterations;
    }

    @Override
    public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
        editSession.erode(position, size, this.minFillFaces, this.numFillIterations, this.minErodeFaces, this.numErodeIterations);
    }

}
