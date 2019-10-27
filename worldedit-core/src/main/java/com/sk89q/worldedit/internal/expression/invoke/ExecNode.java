package com.sk89q.worldedit.internal.expression.invoke;

import org.antlr.v4.runtime.ParserRuleContext;

import java.lang.invoke.MethodHandle;

class ExecNode {
    final ParserRuleContext ctx;
    final MethodHandle handle;

    ExecNode(ParserRuleContext ctx, MethodHandle handle) {
        this.ctx = ctx;
        this.handle = handle;
    }
}
