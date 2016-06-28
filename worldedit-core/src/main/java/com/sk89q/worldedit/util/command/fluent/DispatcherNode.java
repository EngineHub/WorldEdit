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

package com.sk89q.worldedit.util.command.fluent;

import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.Dispatcher;
import com.sk89q.worldedit.util.command.SimpleDispatcher;
import com.sk89q.worldedit.util.command.parametric.ParametricBuilder;

/**
 * A collection of commands.
 */
public class DispatcherNode {

    private final CommandGraph graph;
    private final DispatcherNode parent;
    private final SimpleDispatcher dispatcher;
    
    /**
     * Create a new instance.
     * 
     * @param graph the root fluent graph object
     * @param parent the parent node, or null
     * @param dispatcher the dispatcher for this node
     */
    DispatcherNode(CommandGraph graph, DispatcherNode parent, 
            SimpleDispatcher dispatcher) {
        this.graph = graph;
        this.parent = parent;
        this.dispatcher = dispatcher;
    }
    
    /**
     * Set the description.
     * 
     * <p>This can only be used on {@link DispatcherNode}s returned by
     * {@link #group(String...)}.</p>
     * 
     * @param description the description
     * @return this object
     */
    public DispatcherNode describeAs(String description) {
        dispatcher.getDescription().setDescription(description);
        return this;
    }

    /**
     * Register a command with this dispatcher.
     * 
     * @param callable the executor
     * @param alias the list of aliases, where the first alias is the primary one
     */
    public DispatcherNode register(CommandCallable callable, String... alias) {
        dispatcher.registerCommand(callable, alias);
        return this;
    }

    /**
     * Build and register a command with this dispatcher using the 
     * {@link ParametricBuilder} assigned on the root {@link CommandGraph}.
     * 
     * @param object the object provided to the {@link ParametricBuilder}
     * @return this object
     * @see ParametricBuilder#registerMethodsAsCommands(com.sk89q.worldedit.util.command.Dispatcher, Object)
     */
    public DispatcherNode registerMethods(Object object) {
        ParametricBuilder builder = graph.getBuilder();
        if (builder == null) {
            throw new RuntimeException("No ParametricBuilder set");
        }
        builder.registerMethodsAsCommands(getDispatcher(), object);
        return this;
    }
    
    /**
     * Create a new command that will contain sub-commands.
     * 
     * <p>The object returned by this method can be used to add sub-commands. To
     * return to this "parent" context, use {@link DispatcherNode#graph()}.</p>
     * 
     * @param alias the list of aliases, where the first alias is the primary one
     * @return an object to place sub-commands
     */
    public DispatcherNode group(String... alias) {
        SimpleDispatcher command = new SimpleDispatcher();
        getDispatcher().registerCommand(command, alias);
        return new DispatcherNode(graph, this, command);
    }
    
    /**
     * Return the parent node.
     * 
     * @return the parent node
     * @throws RuntimeException if there is no parent node.
     */
    public DispatcherNode parent() {
        if (parent != null) {
            return parent;
        }
        
        throw new RuntimeException("This node does not have a parent");
    }
    
    /**
     * Get the root command graph.
     * 
     * @return the root command graph
     */
    public CommandGraph graph() {
        return graph;
    }
    
    /**
     * Get the underlying dispatcher of this object.
     * 
     * @return the dispatcher
     */
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

}
