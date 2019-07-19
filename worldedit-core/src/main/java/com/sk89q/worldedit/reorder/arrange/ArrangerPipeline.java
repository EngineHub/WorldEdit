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

package com.sk89q.worldedit.reorder.arrange;

import java.util.List;

/**
 * A sequential list of Arrangers.
 *
 * <p>
 * The last Arranger <strong>MUST</strong> not touch its output stream.
 * Instead, it should handle the actual placements.
 * </p>
 */
public interface ArrangerPipeline {

    static ArrangerPipeline create() {
        return new ArrangerPipelineImpl();
    }

    /**
     * The list of Arrangers. Use this to add to the pipeline.
     *
     * @return a mutable list of Arrangers
     */
    List<Arranger> arrangers();

    /**
     * Create a new stream to write messages to. It will use the Arrangers listed at this
     * moment, copying them into the stream object.
     */
    PlacementOutputStream openStream();

}
