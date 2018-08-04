/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.command.parametric;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableBiMap.Builder;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.util.auth.Authorizer;
import com.sk89q.worldedit.util.auth.NullAuthorizer;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.CommandCompleter;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.NullCompleter;
import com.sk89q.worldedit.util.command.binding.PrimitiveBindings;
import com.sk89q.worldedit.util.command.binding.StandardBindings;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.thoughtworks.paranamer.CachingParanamer;
import com.thoughtworks.paranamer.Paranamer;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates commands using annotations placed on methods and individual parameters of
 * such methods.
 * 
 * @see Command defines a command
 * @see Switch defines a flag
 */
public class ParametricBuilder {

    private final Map<Type, Binding> bindings = new HashMap<>();
    private final Paranamer paranamer = new CachingParanamer();
    private final List<InvokeListener> invokeListeners = new ArrayList<>();
    private Authorizer authorizer = new NullAuthorizer();
    private CommandCompleter defaultCompleter = new NullCompleter();
    
    /**
     * Create a new builder.
     * 
     * <p>This method will install {@link PrimitiveBindings} and 
     * {@link StandardBindings} and default bindings.</p>
     */
    public ParametricBuilder() {
        addBinding(new PrimitiveBindings());
        addBinding(new StandardBindings());
    }

    /**
     * Add a binding for a given type or classifier (annotation).
     * 
     * <p>Whenever a method parameter is encountered, a binding must be found for it
     * so that it can be called later to consume the stack of arguments provided by
     * the user and return an object that is later passed to 
     * {@link Method#invoke(Object, Object...)}.</p>
     * 
     * <p>Normally, a {@link Type} is used to discern between different bindings, but
     * if this is not specific enough, an annotation can be defined and used. This
     * makes it a "classifier" and it will take precedence over the base type. For
     * example, even if there is a binding that handles {@link String} parameters,
     * a special {@code @MyArg} annotation can be assigned to a {@link String}
     * parameter, which will cause the {@link Builder} to consult the {@link Binding}
     * associated with {@code @MyArg} rather than with the binding for
     * the {@link String} type.</p>
     * 
     * @param binding the binding
     * @param type a list of types (if specified) to override the binding's types
     */
    public void addBinding(Binding binding, Type... type) {
        if (type == null || type.length == 0) {
            type = binding.getTypes();
        }
        
        for (Type t : type) {
            bindings.put(t, binding);
        }
    }
    
    /**
     * Attach an invocation listener.
     * 
     * <p>Invocation handlers are called in order that their listeners are
     * registered with a {@link ParametricBuilder}. It is not guaranteed that
     * a listener may be called, in the case of a {@link CommandException} being
     * thrown at any time before the appropriate listener or handler is called.
     * It is possible for a 
     * {@link InvokeHandler#preInvoke(Object, Method, ParameterData[], Object[], CommandContext)} to
     * be called for a invocation handler, but not the associated
     * {@link InvokeHandler#postInvoke(Object, Method, ParameterData[], Object[], CommandContext)}.</p>
     * 
     * <p>An example of an invocation listener is one to handle
     * {@link CommandPermissions}, by first checking to see if permission is available
     * in a {@link InvokeHandler#preInvoke(Object, Method, ParameterData[], Object[], CommandContext)}
     * call. If permission is not found, then an appropriate {@link CommandException}
     * can be thrown to cease invocation.</p>
     * 
     * @param listener the listener
     * @see InvokeHandler the handler
     */
    public void addInvokeListener(InvokeListener listener) {
        invokeListeners.add(listener);
    }

    /**
     * Build a list of commands from methods specially annotated with {@link Command}
     * (and other relevant annotations) and register them all with the given
     * {@link Dispatcher}.
     * 
     * @param dispatcher the dispatcher to register commands with
     * @param object the object contain the methods
     * @throws ParametricException thrown if the commands cannot be registered
     */
    public void registerMethodsAsCommands(Dispatcher dispatcher, Object object) throws ParametricException {
        for (Method method : object.getClass().getDeclaredMethods()) {
            Command definition = method.getAnnotation(Command.class);
            if (definition != null) {
                CommandCallable callable = build(object, method, definition);
                dispatcher.registerCommand(callable, definition.aliases());
            }
        }
    }

    /**
     * Build a {@link CommandCallable} for the given method.
     * 
     * @param object the object to be invoked on
     * @param method the method to invoke
     * @param definition the command definition annotation
     * @return the command executor
     * @throws ParametricException thrown on an error
     */
    private CommandCallable build(Object object, Method method, Command definition) 
            throws ParametricException {
        return new ParametricCallable(this, object, method, definition);
    }

    /**
     * Get the object used to get method names on Java versions before 8 (assuming
     * that Java 8 is given the ability to reliably reflect method names at runtime).
     * 
     * @return the paranamer
     */
    Paranamer getParanamer() {
        return paranamer;
    }
    
    /**
     * Get the map of bindings.
     * 
     * @return the map of bindings
     */
    Map<Type, Binding> getBindings() {
        return bindings;
    }

    /**
     * Get a list of invocation listeners.
     * 
     * @return a list of invocation listeners
     */
    List<InvokeListener> getInvokeListeners() {
        return invokeListeners;
    }

    /**
     * Get the authorizer.
     *
     * @return the authorizer
     */
    public Authorizer getAuthorizer() {
        return authorizer;
    }

    /**
     * Set the authorizer.
     *
     * @param authorizer the authorizer
     */
    public void setAuthorizer(Authorizer authorizer) {
        checkNotNull(authorizer);
        this.authorizer = authorizer;
    }

    /**
     * Get the default command suggestions provider that will be used if
     * no suggestions are available.
     *
     * @return the default command completer
     */
    public CommandCompleter getDefaultCompleter() {
        return defaultCompleter;
    }

    /**
     * Set the default command suggestions provider that will be used if
     * no suggestions are available.
     *
     * @param defaultCompleter the default command completer
     */
    public void setDefaultCompleter(CommandCompleter defaultCompleter) {
        checkNotNull(defaultCompleter);
        this.defaultCompleter = defaultCompleter;
    }

}
