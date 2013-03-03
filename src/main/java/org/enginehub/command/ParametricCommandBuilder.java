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

import java.lang.reflect.Method;
import java.util.*;

/**
 * Walks a class and finds methods that have been annotated with {@link Expose} in
 * order to turn them automatically into {@link Command}s.
 *
 * <p>Argument parsing for commands are automatically completed through the use of
 * a matching {@link ParameterResolver}, which must be registered with an instance of
 * this class.</p>
 */
public class ParametricCommandBuilder implements Set<ParameterResolverFactory> {

    private final Set<ParameterResolverFactory> factories =
            new LinkedHashSet<ParameterResolverFactory>();

    public ParametricCommand build(Class<?> clazz) {

    }

    public ParametricCommand build(Object object) {

    }

    /**
     * Generate a command from a given method.
     *
     * @param method the method
     * @return a command
     * @throws IllegalArgumentException if the method lacks {@link Expose}
     */
    public ParametricCommand build(Method method) {
        Expose expose = method.getAnnotation(Expose.class);

        if (expose == null) {
            throw new IllegalArgumentException("Expected method to have @Expose annotation");
        }

        ParametricCommand command = new ParametricCommand(method, expose.aliases());

        String[] aliases = expose.aliases();

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
