package com.sk89q.worldedit.arranger;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;

public class SetBlockAction implements BlockPositionedAction {

    // Never inline this, it must be a singleton.
    private static final ActionType TYPE = count ->
        TranslatableComponent.of("worldedit.arranger.set-block.type",
            TextComponent.of(count));

    public static ActionType getType() {
        return TYPE;
    }

    private final long position;
    private final BaseBlock block;

    public SetBlockAction(BlockVector3 position, BaseBlock block) {
        this.position = position.toLongPackedForm();
        this.block = block;
    }

    @Override
    public BlockVector3 getBlockPosition() {
        return BlockVector3.fromLongPackedForm(position);
    }

    public BaseBlock getBlock() {
        return block;
    }

    @Override
    public ActionReport apply(World world) throws WorldEditException {
        if (world.setBlock(getBlockPosition(), block)) {
            return ActionReport.single(TYPE);
        }
        return ActionReport.empty();
    }

    @Override
    public Component describe() {
        BlockVector3 blockVector = getBlockPosition();
        return TranslatableComponent.of("worldedit.arranger.set-block.full",
            TextComponent.of(blockVector.getX()),
            TextComponent.of(blockVector.getY()),
            TextComponent.of(blockVector.getZ()),
            TextComponent.of(block.toString()));
    }
}
