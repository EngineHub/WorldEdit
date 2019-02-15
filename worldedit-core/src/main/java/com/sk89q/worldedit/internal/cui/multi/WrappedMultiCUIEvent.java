package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.internal.cui.CUIEvent;

public class WrappedMultiCUIEvent implements CUIMultiEvent {
    private final CUIEvent wrapped;

    public WrappedMultiCUIEvent(CUIEvent wrapped) {
        if (wrapped instanceof CUIMultiEvent) {
            throw new IllegalArgumentException("Can't wrap a multi cui event!");
        }
        this.wrapped = wrapped;
    }

    @Override
    public String getTypeId() {
        return "+" + wrapped.getTypeId();
    }

    @Override
    public String[] getParameters() {
        return wrapped.getParameters();
    }
}
