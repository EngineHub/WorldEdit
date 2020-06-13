package com.sk89q.worldedit.fabric;

import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

// For now, this does nothing, but might be useful later for regen progress communication.
class WorldEditGenListener implements WorldGenerationProgressListener {

    @Override
    public void start(ChunkPos spawnPos) {
    }

    @Override
    public void setChunkStatus(ChunkPos pos, @Nullable ChunkStatus status) {
    }

    @Override
    public void stop() {
    }

}
