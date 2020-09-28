package com.sk89q.worldedit.arranger;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;

public class ApplySideEffectsAction implements BlockPositionedAction {

    // Never inline this, it must be a singleton.
    private static final ActionType TYPE = count ->
        TranslatableComponent.of("worldedit.arranger.apply-side-effects.type",
            TextComponent.of(count));

    public static ActionType getType() {
        return TYPE;
    }

    private final BlockVector3 position;
    private final BlockState previousBlock;
    private final SideEffectSet sideEffectSet;

    public ApplySideEffectsAction(BlockVector3 position, BlockState previousBlock,
                                  SideEffectSet sideEffectSet) {
        this.position = position;
        this.previousBlock = previousBlock;
        this.sideEffectSet = sideEffectSet;
    }

    @Override
    public BlockVector3 getBlockPosition() {
        return position;
    }

    @Override
    public ActionReport apply(World world) throws WorldEditException {
        if (world.applySideEffects(position, previousBlock, sideEffectSet).size() > 0) {
            return ActionReport.single(TYPE);
        }
        return ActionReport.empty();
    }

    @Override
    public Component describe() {
        return TranslatableComponent.of("worldedit.arranger.apply-side-effects.full",
            TextComponent.of(position.getX()),
            TextComponent.of(position.getY()),
            TextComponent.of(position.getZ()),
            TextComponent.of(previousBlock.toString()),
            sideEffectSet.describe());
    }
}
