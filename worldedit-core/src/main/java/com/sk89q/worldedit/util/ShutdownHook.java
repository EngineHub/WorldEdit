package com.sk89q.worldedit.util;

public class ShutdownHook<V> implements AutoCloseable {

    private final Thread hook;
    private final V value;

    public ShutdownHook(Thread hook, V value) {
        this.hook = hook;
        this.value = value;

        Runtime.getRuntime().addShutdownHook(hook);
    }

    public V getValue() {
        return value;
    }

    @Override
    public void close() throws Exception {
        Runtime.getRuntime().removeShutdownHook(hook);
    }
}
