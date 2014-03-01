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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.generator;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Generates flora over an applied area.
 */
public class FloraPlacer extends GroundGenerator {

    private final EditSession editSession;
    private FloraGenerator floraGenerator;

    /**
     * Create a new instance.
     *
     * @param editSession the edit session
     */
    public FloraPlacer(EditSession editSession) {
        super(editSession);
        this.editSession = editSession;
        this.floraGenerator = new FloraGenerator(editSession);
    }

    /**
     * Get the flora generator.
     *
     * @return the flora generator
     */
    public FloraGenerator getFloraGenerator() {
        return floraGenerator;
    }

    /**
     * Set the flora generator.
     *
     * @param floraGenerator the flora generator
     */
    public void setFloraGenerator(FloraGenerator floraGenerator) {
        this.floraGenerator = floraGenerator;
    }

    @Override
    protected boolean apply(Vector pt, BaseBlock block) throws WorldEditException {
        return floraGenerator.apply(pt);
    }

}
