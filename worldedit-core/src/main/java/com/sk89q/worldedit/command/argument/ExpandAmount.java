package com.sk89q.worldedit.command.argument;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ExpandAmount {

    public static ExpandAmount vert() {
        return new ExpandAmount(null);
    }

    public static ExpandAmount from(int amount) {
        return new ExpandAmount(amount);
    }

    @Nullable
    private final Integer amount;

    private ExpandAmount(@Nullable Integer amount) {
        this.amount = amount;
    }

    public boolean isVert() {
        return amount == null;
    }

    public int getAmount() {
        return checkNotNull(amount, "This amount is vertical, i.e. undefined");
    }

}
