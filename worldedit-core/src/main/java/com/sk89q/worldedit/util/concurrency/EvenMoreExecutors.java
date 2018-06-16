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

package com.sk89q.worldedit.util.concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Even more {@code ExecutorService} factory methods.
 */
public final class EvenMoreExecutors {

    private EvenMoreExecutors() {
    }

    /**
     * Creates a thread pool that creates new threads as needed up to
     * a maximum number of threads, but will reuse previously constructed
     * threads when they are available.
     *
     * @param minThreads the minimum number of threads to have at a given time
     * @param maxThreads the maximum number of threads to have at a given time
     * @param queueSize the size of the queue before new submissions are rejected
     * @return the newly created thread pool
     */
    public static ExecutorService newBoundedCachedThreadPool(int minThreads, int maxThreads, int queueSize) {
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                minThreads, maxThreads,
                60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize));
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        return threadPoolExecutor;
    }

}
