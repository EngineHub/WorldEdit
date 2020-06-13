package com.sk89q.worldedit.fabric.internal;

import net.minecraft.util.thread.ThreadExecutor;

public interface ExtendedServerChunkManager {

    ThreadExecutor<Runnable> getMainThreadExecutor();

}
