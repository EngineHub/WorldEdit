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

import com.sk89q.minecraft.util.commands.CommandException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A binding helper that uses the {@link BindingMatch} annotation to make
 * writing bindings extremely easy.
 * 
 * <p>Methods must have the following and only the following parameters:</p>
 * 
 * <ul>
 *   <li>A {@link ArgumentStack}</li>
 *   <li>A {@link Annotation} <strong>if there is a classifier set</strong></li>
 *   <li>A {@link Annotation}[] 
 *       <strong>if there {@link BindingMatch#provideModifiers()} is true</strong></li>
 * </ul>
 * 
 * <p>Methods may throw any exception. Exceptions may be converted using a
 * {@link ExceptionConverter} registered with the {@link ParametricBuilder}.</p>
 */
public class BindingHelper implements Binding {
    
    private final List<BoundMethod> bindings;
    private final Type[] types;
    
    /**
     * Create a new instance.
     */
    public BindingHelper() {
        List<BoundMethod> bindings = new ArrayList<>();
        List<Type> types = new ArrayList<>();
        
        for (Method method : this.getClass().getMethods()) {
            BindingMatch info = method.getAnnotation(BindingMatch.class);
            if (info != null) {
                Class<? extends Annotation> classifier = null;
                
                // Set classifier
                if (!info.classifier().equals(Annotation.class)) {
                    classifier = info.classifier();
                    types.add(classifier);
                }
                
                for (Type t : info.type()) {
                    Type type = null;
                    
                    // Set type
                    if (!t.equals(Class.class)) {
                        type = t;
                        if (classifier == null) {
                            types.add(type); // Only if there is no classifier set!
                        }
                    }
                    
                    // Check to see if at least one is set
                    if (type == null && classifier == null) {
                        throw new RuntimeException(
                                "A @BindingMatch needs either a type or classifier set");
                    }
                    
                    BoundMethod handler = new BoundMethod(info, type, classifier, method);
                    bindings.add(handler);
                }
            }
        }
        
        Collections.sort(bindings);
        
        this.bindings = bindings;
        
        Type[] typesArray = new Type[types.size()];
        types.toArray(typesArray);
        this.types = typesArray;
        
    }
    
    /**
     * Match a {@link BindingMatch} according to the given parameter.
     * 
     * @param parameter the parameter
     * @return a binding
     */
    private BoundMethod match(ParameterData parameter) {
        for (BoundMethod binding : bindings) {
            Annotation classifer = parameter.getClassifier();
            Type type = parameter.getType();
            
            if (binding.classifier != null) {
                if (classifer != null && classifer.annotationType().equals(binding.classifier)) {
                    if (binding.type == null || binding.type.equals(type)) {
                        return binding;
                    }
                }
            } else if (binding.type.equals(type)) {
                return binding;
            }
        }
        
        throw new RuntimeException("Unknown type");
    }

    @Override
    public Type[] getTypes() {
        return types;
    }

    @Override
    public int getConsumedCount(ParameterData parameter) {
        return match(parameter).annotation.consumedCount();
    }

    @Override
    public BindingBehavior getBehavior(ParameterData parameter) {
        return match(parameter).annotation.behavior();
    }

    @Override
    public Object bind(ParameterData parameter, ArgumentStack scoped,
            boolean onlyConsume) throws ParameterException, CommandException, InvocationTargetException {
        BoundMethod binding = match(parameter);
        List<Object> args = new ArrayList<>();
        args.add(scoped);
        
        if (binding.classifier != null) {
            args.add(parameter.getClassifier());
        }
        
        if (binding.annotation.provideModifiers()) {
            args.add(parameter.getModifiers());
        }
        
        if (onlyConsume && binding.annotation.behavior() == BindingBehavior.PROVIDES) {
            return null; // Nothing to consume, nothing to do
        }
        
        Object[] argsArray = new Object[args.size()];
        args.toArray(argsArray);
        
        try {
            return binding.method.invoke(this, argsArray);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Processing of classifier " + parameter.getClassifier() + 
                    " and type " + parameter.getType() + " failed for method\n" +
                    binding.method + "\nbecause the parameters for that method are wrong", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof ParameterException) {
                throw (ParameterException) e.getCause();
            } else if (e.getCause() instanceof CommandException) {
                throw (CommandException) e.getCause();
            }
            throw e;
        }
    }

    @Override
    public List<String> getSuggestions(ParameterData parameter, String prefix) {
        return new ArrayList<>();
    }
    
    private static class BoundMethod implements Comparable<BoundMethod> {
        private final BindingMatch annotation;
        private final Type type;
        private final Class<? extends Annotation> classifier;
        private final Method method;
        
        BoundMethod(BindingMatch annotation, Type type, 
                Class<? extends Annotation> classifier, Method method) {
            this.annotation = annotation;
            this.type = type;
            this.classifier = classifier;
            this.method = method;
        }

        @Override
        public int compareTo(BoundMethod o) {
            if (classifier != null && o.classifier == null) {
                return -1;
            } else if (classifier == null && o.classifier != null) {
                return 1;
            } else if (classifier != null && o.classifier != null) {
                if (type != null && o.type == null) {
                    return -1;
                } else if (type == null && o.type != null) {
                    return 1;
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }
    }

}
