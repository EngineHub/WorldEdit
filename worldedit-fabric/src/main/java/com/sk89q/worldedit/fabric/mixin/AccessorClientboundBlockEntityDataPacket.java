package com.sk89q.worldedit.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientboundBlockEntityDataPacket.class)
public interface AccessorClientboundBlockEntityDataPacket {
    @Invoker("<init>")
    static ClientboundBlockEntityDataPacket construct(BlockPos pos, BlockEntityType<?> blockEntityType, CompoundTag compoundTag) {
        throw new AssertionError("This is replaced by Mixin to call the constructor.");
    }
}
