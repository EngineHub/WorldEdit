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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_11;

import net.minecraft.util.ProblemReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Function;
import java.util.function.Supplier;

final class PaperweightLoggingProblemReporter implements ProblemReporter, AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();

    static <T> T with(Supplier<String> contextSupplier, Function<ProblemReporter, T> consumer) {
        try (var problemReporter = new PaperweightLoggingProblemReporter(contextSupplier)) {
            return consumer.apply(problemReporter);
        }
    }

    PaperweightLoggingProblemReporter(Supplier<String> contextSupplier) {
        this.contextSupplier = contextSupplier;
    }

    private final Collector delegate = new Collector();
    private final Supplier<String> contextSupplier;

    @Override
    public ProblemReporter forChild(PathElement child) {
        return delegate.forChild(child);
    }

    @Override
    public void report(Problem problem) {
        delegate.report(problem);
    }

    @Override
    public void close() {
        if (!delegate.isEmpty()) {
            LOGGER.warn("Problems were reported during {}: {}", contextSupplier.get(), delegate.getTreeReport());
        }
    }
}
