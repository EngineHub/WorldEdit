package com.sk89q.worldedit.extension.platform.scheduler;

import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.util.Location;

public interface SchedulerAdapter {

    /**
     * Schedules the specified task to be executed asynchronously after the delay has passed,
     * and then periodically executed with the specified period.
     *
     * @param runnable The task to execute.
     * @param delay    The time delay to pass before the task should be executed.
     * @param period   The time between task executions after the first execution of the task.
     */
    void runAsyncRate(Runnable runnable, long delay, long period);

    /**
     * Schedules a task. If the task failed to schedule because the scheduler is retired (entity removed),
     * then returns {@code false}. Otherwise, either the run callback will be invoked after the specified delay,
     * or the retired callback will be invoked if the scheduler is retired.
     * Note that the retired callback is invoked in critical code, so it should not attempt to remove the entity,
     * remove other entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity   The entity relative to which the scheduler is obtained.
     * @param runnable The task to execute.
     */
    default void executeAtEntity(Entity entity, Runnable runnable) {
        entity.executeAtEntity(runnable);
    }

    /**
     * Schedules a task with the given delay. If the task failed to schedule because the scheduler is retired (entity removed),
     * then returns {@code false}. Otherwise, either the run callback will be invoked after the specified delay,
     * or the retired callback will be invoked if the scheduler is retired.
     * Note that the retired callback is invoked in critical code, so it should not attempt to remove the entity,
     * remove other entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity   The entity relative to which the scheduler is obtained.
     * @param runnable The task to execute.
     * @param delay    The time delay to pass before the task should be executed, in ticks.
     */
    default void runAtEntityDelayed(Entity entity, Runnable runnable, long delay) {
        entity.runAtEntityDelayed(runnable, delay);
    }

    /**
     * Schedules a task to be executed on the region which owns the location.
     *
     * @param location The location at which the region executing should own.
     * @param runnable The task to execute.
     */
    void executeAtRegion(Location location, Runnable runnable);

    /**
     * Attempts to cancel all tasks scheduled by the plugin.
     */
    void cancelTasks();
}