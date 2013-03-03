// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package org.enginehub.command;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.*;

/**
 * Walks a class and finds methods that have been annotated with {@link Expose} in
 * order to turn them automatically into {@link Command}s.
 *
 * <p>Argument parsing for commands are automatically completed through the use of
 * a matching {@link ParameterResolver}, which must be registered with an instance of
 * this class.</p>
 */
public class ParametricCommandBuilder implements Set<ParameterResolverFactory> {

    private final static Logger logger =
            Logger.getLogger(ParametricCommand.class.getCanonicalName());

    private final Set<ParameterResolverFactory> factories =
            new LinkedHashSet<ParameterResolverFactory>();

    /**
     * Generate a list of commands from the static methods of a class.
     *
     * <p>Only public methods annotated with {@link Expose} will be included. Protected
     * or private methods will be ignored.</p>
     *
     * <p>Errors are consumed and only printed to the logger.</p>
     *
     * @param clazz the class
     * @return a list of commands
     * @throws IllegalArgumentException if the method lacks {@link Expose}
     */
    public List<ParametricCommand> build(Class<?> clazz) {
        try {
            return build(clazz, true);
        } catch (BuilderException e) {
            // Should never happen
            return Collections.emptyList();
        }
    }

    /**
     * Generate a list of commands from the static methods of a class.
     *
     * <p>Only public methods annotated with {@link Expose} will be included. Protected
     * or private methods will be ignored.</p>
     *
     * @param clazz the class
     * @param ignoreErrors true to only print errors to the logger
     * @return a list of commands
     */
    public List<ParametricCommand> build(Class<?> clazz, boolean ignoreErrors)
            throws BuilderException {
        Method[] methods = clazz.getMethods();
        return build(null, methods, ignoreErrors);
    }

    /**
     * Generate a list of commands from the instance of an object.
     *
     * <p>Only public methods annotated with {@link Expose} will be included. Protected
     * or private methods will be ignored.</p>
     *
     * <p>Errors are consumed and only printed to the logger.</p>
     *
     * @param object the object
     * @return a list of commands
     * @throws IllegalArgumentException if the method lacks {@link Expose}
     */
    public List<ParametricCommand> build(Object object) {
        try {
            return build(object, true);
        } catch (BuilderException e) {
            // Should never happen
            return Collections.emptyList();
        }
    }

    /**
     * Generate a list of commands from the instance of an object.
     *
     * <p>Only public methods annotated with {@link Expose} will be included. Protected
     * or private methods will be ignored.</p>
     *
     * @param object the object
     * @param ignoreErrors true to only print errors to the logger
     * @return a list of commands
     */
    public List<ParametricCommand> build(Object object, boolean ignoreErrors)
            throws BuilderException {
        Method[] methods = object.getClass().getMethods();
        return build(object, methods, ignoreErrors);
    }

    /**
     * Generate a list of commands from a list of methods and the corresponding
     * object (possibly null for static methods).
     *
     * <p>Only public methods annotated with {@link Expose} will be included. Protected
     * or private methods will be ignored.</p>
     *
     * @param object the object, or null for static methods
     * @param ignoreErrors true to only print errors to the logger
     * @return a list of commands
     */
    private List<ParametricCommand> build(Object object, Method[] methods,
                                         boolean ignoreErrors) throws BuilderException {
        List<ParametricCommand> commands = new ArrayList<ParametricCommand>();

        for (Method method : methods) {
            if (method.getAnnotation(Expose.class) == null) {
                continue; // Only methods with @Expose
            }

            boolean isStatic = Modifier.isStatic(method.getModifiers());

            if ((isStatic && object != null) || (!isStatic && object == null)) {
                continue; // If object is provided but the method is static, skip,
                          // or if the object is not provided, but the method is not
                          // static, skip
            }

            try {
                commands.add(build(isStatic ? null : object, method));
            } catch (BuilderException e) {
                if (ignoreErrors) {
                    logger.log(Level.WARNING, "Failed to build command from method " +
                            "named '" + method.getName() + "' of class '" +
                            object.getClass().getCanonicalName() + "'", e);
                } else {
                    throw e;
                }
            }
        }

        return commands;
    }

    /**
     * Generate a command from a given accessible method.
     *
     * @param object the object, or null for static methods
     * @param method the method
     * @return a command
     * @throws IllegalArgumentException if the method lacks {@link Expose}
     */
    public ParametricCommand build(Object object, Method method) throws BuilderException {
        Expose expose = method.getAnnotation(Expose.class);

        if (expose == null) {
            throw new IllegalArgumentException("Method expected to have @Expose annotation");
        }

        ParametricCommand command = new ParametricCommand(object, method, expose.aliases());
        command.setName(expose.name());
        command.setDescription(expose.description());
        command.setHelp(expose.help());
        // @TODO: Build the usage string dynamically

        // Build parameter consumers
        Class<?>[] types = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();
        List<ParameterResolver<?>> resolvers = new ArrayList<ParameterResolver<?>>();

        for (int i = 0; i < types.length; i++) {
            ParameterResolverFactory<?> factory = find(types[i], annotations[i]);

            if (factory != null) {
                ParameterResolver<?> resolver = factory.getResolver(types[i], annotations[i]);
                resolvers.add(resolver);
            } else {
                throw new BuilderException("Don't know how to handle " +
                        types[i].getCanonicalName() + " (a ParameterResolverFactory was " +
                        "not found)");
            }
        }

        command.setResolvers((ParameterResolver<?>[]) resolvers.toArray());

        return command;
    }

    /**
     * Find the parameter resolver factory for the given type and annotations.
     *
     * <p>If two factories have the same confidence, the behavior is undefined. One
     * factory will nevertheless be selected.</p>
     *
     * @param type the type
     * @param annotations the list of annotations
     * @return the best factory, or null
     */
    private ParameterResolverFactory<?> find(Class<?> type, Annotation[] annotations) {
        int highest = 0;
        ParameterResolverFactory factory = null;

        for (ParameterResolverFactory f : this) {
            int confidence = f.getConfidenceFor(type, annotations);
            if (confidence > 0 && confidence >= highest) {
                factory = f;
            }
        }

        return factory;
    }

    /* Extending would cause us to expose LinkedHashSet<E>, when we only want to expose
     * Set<E>, and thus all these redundant proxy methods. */

    @Override
    public int size() {
        return factories.size();
    }

    @Override
    public boolean isEmpty() {
        return factories.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return factories.contains(o);
    }

    @Override
    public Iterator<ParameterResolverFactory> iterator() {
        return factories.iterator();
    }

    @Override
    public Object[] toArray() {
        return factories.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return factories.toArray(a);
    }

    @Override
    public boolean add(ParameterResolverFactory parameterResolverFactory) {
        return factories.add(parameterResolverFactory);
    }

    @Override
    public boolean remove(Object o) {
        return factories.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return factories.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends ParameterResolverFactory> c) {
        return factories.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return factories.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return factories.removeAll(c);
    }

    @Override
    public void clear() {
        factories.clear();
    }

}
