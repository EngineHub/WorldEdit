package com.sk89q.worldedit.fabric.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.util.NonBlockingThreadExecutor;
import net.minecraft.util.snooper.SnooperListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer extends NonBlockingThreadExecutor<ServerTask> implements SnooperListener, CommandOutput, AutoCloseable, Runnable {

    public MixinMinecraftServer(String string_1) {
        super(string_1);
    }

    @Shadow
    public long timeReference;
}
