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

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.action.WorldAction;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

class ArrangerPipelineImpl implements ArrangerPipeline {

    private final List<Arranger> arrangers;

    ArrangerPipelineImpl(Iterable<? extends Arranger> arrangers) {
        this.arrangers = ImmutableList.copyOf(arrangers);
    }

    @Override
    public void rearrange(List<WorldAction> actions) {
        Context context = new Context(actions);
        for (Arranger arranger : arrangers) {
            context.process(arranger);
        }
        checkState(context.groupedActions.isEmpty(), "Final arranger did not consume all groups");
    }

    private static final class Context implements ArrangerContext {

        private final List<List<WorldAction>> groupedActions = new ArrayList<>();
        private boolean clWritable;
        private List<WorldAction> currentList;

        private Context(List<WorldAction> actions) {
            groupedActions.add(actions);
        }

        void process(Arranger arranger) {
            ImmutableList<List<WorldAction>> currentGroups = ImmutableList.copyOf(groupedActions);
            groupedActions.clear();
            for (List<WorldAction> g : currentGroups) {
                currentList = g;
                clWritable = false;
                arranger.rearrange(this);
            }
            currentList = null;
        }

        private List<WorldAction> getCurrentList() {
            checkState(currentList != null, "No group being processed. Out-of-order call occurring?");
            return currentList;
        }

        @Override
        public int getActionCount() {
            return getCurrentList().size();
        }

        @Override
        public WorldAction getAction(int i) {
            return getCurrentList().get(i);
        }

        @Override
        public List<WorldAction> getActionWriteList() {
            if (!clWritable) {
                currentList = new ArrayList<>(currentList);
                clWritable = true;
            }
            return currentList;
        }

        @Override
        public void markGroup(int start, int end) {
            checkArgument(start >= 0, "Group begins before list starts");
            checkArgument(end <= currentList.size(), "Group ends after list");
            List<WorldAction> result;
            if (start == 0 && end == currentList.size()) {
                // optimize: we can just re-use the list
                result = currentList;
                clWritable = false;
            } else {
                result = ImmutableList.copyOf(currentList.subList(start, end));
            }
            groupedActions.add(result);
        }

    }

}
