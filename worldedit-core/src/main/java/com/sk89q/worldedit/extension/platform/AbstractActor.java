package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.NamedTextColor;

import java.util.function.Consumer;

/**
 * Base class for implementing an actor. Provides reasonable defaults.
 */
public abstract class AbstractActor implements Actor {

    private final Consumer<Component> sendMessage;

    protected AbstractActor(Consumer<Component> sendMessage) {
        this.sendMessage = sendMessage;
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            print(TextComponent.of(part));
        }
    }

    @Override
    @Deprecated
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            print(TextComponent.of(part, NamedTextColor.LIGHT_PURPLE));
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            print(TextComponent.of(part, NamedTextColor.GRAY));
        }
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            print(TextComponent.of(part, NamedTextColor.RED));
        }
    }

    @Override
    public void print(Component component) {
        sendMessage.accept(WorldEditText.format(component, getLocale()));
    }

}
