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

package com.sk89q.worldedit.function.visitor;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Performs a breadth-first search starting from points added with
 * {@link #visit(com.sk89q.worldedit.Vector)}. The search continues
 * to a certain adjacent point provided that the method
 * {@link #isVisitable(com.sk89q.worldedit.Vector, com.sk89q.worldedit.Vector)}
 * returns true for that point.
 *
 * <p>As an abstract implementation, this class can be used to implement
 * functionality that starts at certain points and extends outward from
 * those points.</p>
 */
public abstract class BreadthFirstSearch implements Operation {

    private final RegionFunction function;
    private final Queue<BlockVector> queue = new ArrayDeque<>();
    private final Set<BlockVector> visited = new HashSet<>();
    private final List<Vector> directions = new ArrayList<>();
    private int affected = 0;

    /**
     * Create a new instance.
     *
     * @param function the function to apply to visited blocks
     */
    protected BreadthFirstSearch(RegionFunction function) {
        checkNotNull(function);
        this.function = function;
        addAxes();
    }

    /**
     * Get the list of directions will be visited.
     *
     * <p>Directions are {@link com.sk89q.worldedit.Vector}s that determine
     * what adjacent points area available. Vectors should not be
     * unit vectors. An example of a valid direction is
     * {@code new Vector(1, 0, 1)}.</p>
     *
     * <p>The list of directions can be cleared.</p>
     *
     * @return the list of directions
     */
    protected Collection<Vector> getDirections() {
        return directions;
    }

    /**
     * Add the directions along the axes as directions to visit.
     */
    protected void addAxes() {
        directions.add(new Vector(0, -1, 0));
        directions.add(new Vector(0, 1, 0));
        directions.add(new Vector(-1, 0, 0));
        directions.add(new Vector(1, 0, 0));
        directions.add(new Vector(0, 0, -1));
        directions.add(new Vector(0, 0, 1));
    }

    /**
     * Add the diagonal directions as directions to visit.
     */
    protected void addDiagonal() {
        directions.add(new Vector(1, 0, 1));
        directions.add(new Vector(-1, 0, -1));
        directions.add(new Vector(1, 0, -1));
        directions.add(new Vector(-1, 0, 1));
    }

    /**
     * Add the given location to the list of locations to visit, provided
     * that it has not been visited. The position passed to this method
     * will still be visited even if it fails
     * {@link #isVisitable(com.sk89q.worldedit.Vector, com.sk89q.worldedit.Vector)}.
     *
     * <p>This method should be used before the search begins, because if
     * the position <em>does</em> fail the test, and the search has already
     * visited it (because it is connected to another root point),
     * the search will mark the position as "visited" and a call to this
     * method will do nothing.</p>
     *
     * @param position the position
     */
    public void visit(Vector position) {
        BlockVector blockVector = position.toBlockVector();
        if (!visited.contains(blockVector)) {
            queue.add(blockVector);
            visited.add(blockVector);
        }
    }

    /**
     * Try to visit the given 'to' location.
     *
     * @param from the origin block
     * @param to the block under question
     */
    private void visit(Vector from, Vector to) {
        BlockVector blockVector = to.toBlockVector();
        if (!visited.contains(blockVector)) {
            visited.add(blockVector);
            if (isVisitable(from, to)) {
                queue.add(blockVector);
            }
        }
    }

    /**
     * Return whether the given 'to' block should be visited, starting from the
     * 'from' block.
     *
     * @param from the origin block
     * @param to the block under question
     * @return true if the 'to' block should be visited
     */
    protected abstract boolean isVisitable(Vector from, Vector to);

    /**
     * Get the number of affected objects.
     *
     * @return the number of affected
     */
    public int getAffected() {
        return affected;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        Vector position;
        
        while ((position = queue.poll()) != null) {
            if (function.apply(position)) {
                affected++;
            }

            for (Vector dir : directions) {
                visit(position, position.add(dir));
            }
        }

        return null;
    }

    @Override
    public void cancel() {
    }

    @Override
    public void addStatusMessages(List<String> messages) {
        messages.add(getAffected() + " blocks affected");
    }

}
