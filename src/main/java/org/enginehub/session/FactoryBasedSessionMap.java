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

package org.enginehub.session;

import java.lang.annotation.Annotation;

import org.enginehub.command.CommandContext;
import org.enginehub.command.parametric.ParameterResolver;
import org.enginehub.command.parametric.ParameterResolverFactory;
import org.enginehub.command.parametric.ParametricBuilder;

/**
 * An implementation of {@link GuaranteedSessionMap} that creates new sessions
 * using a {@link SessionFactory}.
 * 
 * <p>As a convenience, this class also implements {@link ParameterResolverFactory},
 * which allows use of instances of this class with {@link ParametricBuilder}. However,
 * if you decide to use this capability, you must call 
 * {@link #FactoryBasedSessionMap(SessionFactory, Class, Class)} so that the instance
 * has a reference of the classes (since Java uses type erasure and types
 * within the generic <> definitions are not available at runtime).</p>
 * 
 * <p>In respect to being a {@link ParameterResolver}, this class pulls the "key
 * object" (that is used to index sessions by) using 
 * {@link CommandContext#getSafeObject(Class)} using the key class provided.</p>
 *
 * @param <K> the session key
 * @param <V> the session object
 */
public class FactoryBasedSessionMap<K, V extends Session> 
        extends GuaranteedSessionMap<K, V>
        implements ParameterResolverFactory<V>, ParameterResolver<V> {
    
    private final SessionFactory<K, V> factory;
    private final Class<? extends K> keyClass;
    private final Class<? extends V> sessionClass;
    
    /**
     * Construct a new instance using the given factory.
     * 
     * <p>If you're using this method, you cannot use the 
     * {@link ParameterResolverFactory} capability of this class. See
     * {@link #FactoryBasedSessionMap(SessionFactory, Class, Class)}.</p>
     * 
     * @param factory the factory
     */
    public FactoryBasedSessionMap(SessionFactory<K, V> factory) {
        this.factory = factory;
        this.keyClass = null;
        this.sessionClass = null;
    }
    
    /**
     * Construct a new instance using the given factory.
     * 
     * <p>You can call the simpler {@link #FactoryBasedSessionMap(SessionFactory)} if
     * you are not interested in using this class as a
     * {@link ParameterResolverFactory}.
     * 
     * @param factory the factory
     * @param keyClass the class of the key object
     * @param sessionClass the class of the session object
     */
    @SuppressWarnings("unchecked")
    public FactoryBasedSessionMap(SessionFactory<K, V> factory, 
            Class<?> keyClass, Class<?> sessionClass) {
        // Should really do Class<? extends K/V> but Java's not being happy
        this.factory = factory;
        this.keyClass = (Class<? extends K>) keyClass;
        this.sessionClass = (Class<? extends V>) sessionClass;
    }

    @Override
    protected V create(K key) {
        return factory.createSession(key);
    }

    @Override
    public int getConfidenceFor(Class<?> clazz, Annotation[] annotations) {
        return this.sessionClass.isAssignableFrom(clazz) ? CLASS_CONFIDENCE : NO_CONFIDENCE;
    }

    @Override
    public ParameterResolver<V> getResolver(Class<?> clazz,
            Annotation[] annotations) {
        return this;
    }

    @Override
    public V resolve(CommandContext context) {
        // Will auto-create the session since this is a GuaranteedSessionMap
        return get(context.getSafeObject(keyClass));
    }

}
