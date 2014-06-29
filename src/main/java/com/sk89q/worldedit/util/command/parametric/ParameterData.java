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

import com.sk89q.worldedit.util.command.SimpleParameter;
import com.sk89q.worldedit.util.command.binding.PrimitiveBindings;
import com.sk89q.worldedit.util.command.binding.Range;
import com.sk89q.worldedit.util.command.binding.Text;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Describes a parameter in detail.
 */
public class ParameterData extends SimpleParameter {

    private Binding binding;
    private Annotation classifier;
    private Annotation[] modifiers;
    private Type type;
    
    /**
     * Get the binding associated with this parameter.
     * 
     * @return the binding
     */
    public Binding getBinding() {
        return binding;
    }

    /**
     * Set the binding associated with this parameter.
     * 
     * @param binding the binding
     */
    void setBinding(Binding binding) {
        this.binding = binding;
    }

    /**
     * Set the main type of this parameter.
     * 
     * <p>The type is normally that is used to determine which binding is used
     * for a particular method's parameter.</p>
     * 
     * @return the main type
     * @see #getClassifier() which can override the type
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Set the main type of this parameter.
     * 
     * @param type the main type
     */
    void setType(Type type) {
        this.type = type;
    }
    
    /**
     * Get the classifier annotation.
     * 
     * <p>Normally, the type determines what binding is called, but classifiers
     * take precedence if one is found (and registered with 
     * {@link ParametricBuilder#addBinding(Binding, Type...)}). 
     * An example of a classifier annotation is {@link Text}.</p>
     * 
     * @return the classifier annotation, null is possible
     */
    public Annotation getClassifier() {
        return classifier;
    }
    
    /**
     * Set the classifier annotation.
     * 
     * @param classifier the classifier annotation, null is possible
     */
    void setClassifier(Annotation classifier) {
        this.classifier = classifier;
    }
    
    /**
     * Get a list of modifier annotations.
     * 
     * <p>Modifier annotations are not considered in the process of choosing a binding
     * for a method parameter, but they can be used to modify the behavior of a binding.
     * An example of a modifier annotation is {@link Range}, which can restrict
     * numeric values handled by {@link PrimitiveBindings} to be within a range. The list
     * of annotations may contain a classifier and other unrelated annotations.</p>
     * 
     * @return a list of annotations
     */
    public Annotation[] getModifiers() {
        return modifiers;
    }

    /**
     * Set the list of modifiers.
     * 
     * @param modifiers a list of annotations
     */
    void setModifiers(Annotation[] modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Return the number of arguments this binding consumes.
     * 
     * @return -1 if unknown or unavailable
     */
    int getConsumedCount() {
        return getBinding().getConsumedCount(this);
    }

    /**
     * Get whether this parameter is entered by the user.
     * 
     * @return true if this parameter is entered by the user.
     */
    boolean isUserInput() {
        return getBinding().getBehavior(this) != BindingBehavior.PROVIDES;
    }

    /**
     * Get whether this parameter consumes non-flag arguments.
     * 
     * @return true if this parameter consumes non-flag arguments
     */
    boolean isNonFlagConsumer() {
        return getBinding().getBehavior(this) != BindingBehavior.PROVIDES && !isValueFlag();
    }
    
    /**
     * Validate this parameter and its binding.
     */
    void validate(Method method, int parameterIndex) throws ParametricException {
        // We can't have indeterminate consumers without @Switches otherwise
        // it may screw up parameter processing for later bindings
        BindingBehavior behavior = getBinding().getBehavior(this);
        boolean indeterminate = (behavior == BindingBehavior.INDETERMINATE);
        if (!isValueFlag() && indeterminate) {
            throw new ParametricException(
                    "@Switch missing for indeterminate consumer\n\n" +
                    "Notably:\nFor the type " + type + ", the binding " + 
                    getBinding().getClass().getCanonicalName() + 
                    "\nmay or may not consume parameters (isIndeterminateConsumer(" + type + ") = true)" +
                    "\nand therefore @Switch(flag) is required for parameter #" + parameterIndex + " of \n" +
                    method.toGenericString());
        }
        
        // getConsumedCount() better return -1 if the BindingBehavior is not CONSUMES
        if (behavior != BindingBehavior.CONSUMES && binding.getConsumedCount(this) != -1) {
            throw new ParametricException(
                    "getConsumedCount() does not return -1 for binding " + 
                    getBinding().getClass().getCanonicalName() + 
                    "\neven though its behavior type is " + behavior.name() +
                    "\nfor parameter #" + parameterIndex + " of \n" +
                    method.toGenericString());
        }
        
        // getConsumedCount() should not return 0 if the BindingBehavior is not PROVIDES
        if (behavior != BindingBehavior.PROVIDES && binding.getConsumedCount(this) == 0) {
            throw new ParametricException(
                    "getConsumedCount() must not return 0 for binding " + 
                    getBinding().getClass().getCanonicalName() + 
                    "\nwhen its behavior type is " + behavior.name() + " and not PROVIDES " +
                    "\nfor parameter #" + parameterIndex + " of \n" +
                    method.toGenericString());
        }
    }

}
