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

/**
 * Resolves parameters of a method into potential command arguments.
 *
 * <p>Because commands are defined parametrically only by Java parameter types,
 * such as {@code methodName(Vector v, Player p)}, somehow this must be converted
 * into, for example, the expected command argument syntax of
 * @code /command <x> <y> <z> <player>}. The purpose of resolvers is to achieve that by
 * attempting to convert Java types into command argument consumers that can
 * instantiate the required objects.</p>
 *
 * <p>Resolvers are called in two stages:</p>
 * <ul>
 *     <li>{@link #getConfidenceFor(Class, java.lang.annotation.Annotation[])} is called
 *     to get a non-negative numeric confidence level indicating the resolver's
 *     own confidence as the intended parameter resolver for the given method parameter.</li>
 *     <li>If a resolver is chosen as the resolver for a given parameter,
 *     then the {@link #getResolver(Class, java.lang.annotation.Annotation[])} method
 *     is called to create a {@link ParameterResolver} to parse arguments.</li>
 * </ul>
 *
 * <p>Sometimes, a Java type may be too ambiguous (for example, {@code Vector} could
 * imply either a coordinate, or a direction), and so annotations may be required
 * to indicate that a given resolver is expected to handle the given parameter. Resolvers
 * that utilize annotations should generally be more confident because annotations
 * are more explicit.</p>
 *
 * <p>Confidence levels are intended to figure out which resolver should take priority
 * when multiple resolvers match the same method parameter. See the documentation
 * for {@link #getConfidenceFor(Class, java.lang.annotation.Annotation[])} for more
 * specific information.</p>
 *
 * @param <T> the type returned
 */
public interface ParameterResolverFactory<T> {

    /**
     * The parameter is not compatible with this resolver.
     */
    public static final int NO_CONFIDENCE = 0;

    /**
     * The parameter's class appears to match the type of classes that this resolver
     * works for, and it is likely compatible.
     */
    public static final int CLASS_CONFIDENCE = 10;

    /**
     * An annotation indicates that it is very likely that this resolver is meant to
     * handle the given parameter.
     */
    public static final int MEDIUM_CONFIDENCE = 50;

    /**
     * Without a doubt, this resolver should handle the parameter. Avoid use of this
     * when possible.
     */
    public static final int HIGH_CONFIDENCE = 100;

    /**
     * Return a non-negative number indicating the confidence level of this resolver
     * for the given method parameter type.
     *
     * <p>Confidence levels are intended to figure out which resolver should take priority
     * when multiple resolvers match the same method parameter. Recommended confidence
     * levels are as follows:</p>
     *
     * <ul>
     *     <li>If it is not possible for this resolver to handle the given method
     *     parameter, a level of 0 ({@link #NO_CONFIDENCE}) should be returned.</li>
     *     <li>A simple class inheritance check should have a confidence of 10
     *     ({@link #CLASS_CONFIDENCE}).</li>
     *     <li>A medium confidence (possibly induced by a vague annotation) would
     *     have a confidence of 50 ({@link #MEDIUM_CONFIDENCE}).</li>
     *     <li>A high confidence level would have a confidence of 100
     *     ({@link #HIGH_CONFIDENCE}).</li>
     * </ul>
     *
     * @param clazz the type of the parameter
     * @param annotations a list of annotations on the parameter
     * @return a non-negative confidence level, 0 indicating no confidence
     */
    int getConfidenceFor(Class<?> clazz, Annotation[] annotations);

    /**
     * Get the resolver for the given method parameter.
     *
     * <p>This method should not be called unless {
     * @link #getConfidenceFor(Class, java.lang.annotation.Annotation[])} returns
     * a value greater than 0. If this method is improperly called, it is acceptable
     * to throw an {@link IllegalArgumentException}.</p>
     *
     * @param clazz the class
     * @param annotations the annotations
     * @return the resolver
     */
    ParameterResolver<T> getResolver(Class<?> clazz, Annotation[] annotations);

}
