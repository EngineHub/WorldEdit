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

import com.google.common.primitives.Chars;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.InvalidUsageException;
import com.sk89q.worldedit.util.command.MissingParameterException;
import com.sk89q.worldedit.util.command.Parameter;
import com.sk89q.worldedit.util.command.SimpleDescription;
import com.sk89q.worldedit.util.command.UnconsumedParameterException;
import com.sk89q.worldedit.util.command.binding.Switch;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The implementation of a {@link CommandCallable} for the {@link ParametricBuilder}.
 */
class ParametricCallable implements CommandCallable {

    private final ParametricBuilder builder;
    private final Object object;
    private final Method method;
    private final ParameterData[] parameters;
    private final Set<Character> valueFlags = new HashSet<>();
    private final boolean anyFlags;
    private final Set<Character> legacyFlags = new HashSet<>();
    private final SimpleDescription description = new SimpleDescription();
    private final CommandPermissions commandPermissions;

    /**
     * Create a new instance.
     * 
     * @param builder the parametric builder
     * @param object the object to invoke on
     * @param method the method to invoke
     * @param definition the command definition annotation
     * @throws ParametricException thrown on an error
     */
    ParametricCallable(ParametricBuilder builder, Object object, Method method, Command definition) throws ParametricException {
        this.builder = builder;
        this.object = object;
        this.method = method;
        
        Annotation[][] annotations = method.getParameterAnnotations();
        String[] names = builder.getParanamer().lookupParameterNames(method, false);
        Type[] types = method.getGenericParameterTypes();
        parameters = new ParameterData[types.length];
        List<Parameter> userParameters = new ArrayList<>();
        
        // This helps keep tracks of @Nullables that appear in the middle of a list
        // of parameters
        int numOptional = 0;
        
        // Set permission hint
        CommandPermissions permHint = method.getAnnotation(CommandPermissions.class);
        if (permHint != null) {
            description.setPermissions(Arrays.asList(permHint.value()));
        }

        // Go through each parameter
        for (int i = 0; i < types.length; i++) {
            Type type = types[i];
            
            ParameterData parameter = new ParameterData();
            parameter.setType(type);
            parameter.setModifiers(annotations[i]);

            // Search for annotations
            for (Annotation annotation : annotations[i]) {
                if (annotation instanceof Switch) {
                    parameter.setFlag(((Switch) annotation).value(), type != boolean.class);
                } else if (annotation instanceof Optional) {
                    parameter.setOptional(true);
                    String[] value = ((Optional) annotation).value();
                    if (value.length > 0) {
                        parameter.setDefaultValue(value);
                    }
                // Special annotation bindings
                } else if (parameter.getBinding() == null) {
                    parameter.setBinding(builder.getBindings().get(annotation.annotationType()));
                    parameter.setClassifier(annotation);
                }
            }

            parameter.setName(names.length > 0 ? names[i] : generateName(type, parameter.getClassifier(), i));

            // Track all value flags
            if (parameter.isValueFlag()) {
                valueFlags.add(parameter.getFlag());
            }

            // No special @annotation binding... let's check for the type
            if (parameter.getBinding() == null) {
                parameter.setBinding(builder.getBindings().get(type));

                // Don't know how to parse for this type of value
                if (parameter.getBinding() == null) {
                    throw new ParametricException("Don't know how to handle the parameter type '" + type + "' in\n" + method.toGenericString());
                }
            }
            
            // Do some validation of this parameter
            parameter.validate(method, i + 1);
            
            // Keep track of optional parameters
            if (parameter.isOptional() && parameter.getFlag() == null) {
                numOptional++;
            } else {
                if (numOptional > 0 && parameter.isNonFlagConsumer()) {
                    if (parameter.getConsumedCount() < 0) {
                        throw new ParametricException(
                                "Found an parameter using the binding " + 
                                parameter.getBinding().getClass().getCanonicalName() + 
                                "\nthat does not know how many arguments it consumes, but " +
                                "it follows an optional parameter\nMethod: " +
                                method.toGenericString());
                    }
                }
            }
            
            parameters[i] = parameter;
            
            // Make a list of "real" parameters
            if (parameter.isUserInput()) {
                userParameters.add(parameter);
            }
        }

        // Gather legacy flags
        anyFlags = definition.anyFlags();
        legacyFlags.addAll(Chars.asList(definition.flags().toCharArray()));

        // Finish description
        description.setDescription(!definition.desc().isEmpty() ? definition.desc() : null);
        description.setHelp(!definition.help().isEmpty() ? definition.help() : null);
        description.overrideUsage(!definition.usage().isEmpty() ? definition.usage() : null);

        for (InvokeListener listener : builder.getInvokeListeners()) {
            listener.updateDescription(object, method, parameters, description);
        }
        
        // Set parameters
        description.setParameters(userParameters);

        // Get permissions annotation
        commandPermissions = method.getAnnotation(CommandPermissions.class);
    }

    @Override
    public Object call(String stringArguments, CommandLocals locals, String[] parentCommands) throws CommandException {
        // Test permission
        if (!testPermission(locals)) {
            throw new CommandPermissionsException();
        }

        String calledCommand = parentCommands.length > 0 ? parentCommands[parentCommands.length - 1] : "_";
        String[] split = CommandContext.split(calledCommand + " " + stringArguments);
        CommandContext context = new CommandContext(split, getValueFlags(), false, locals);

        // Provide help if -? is specified
        if (context.hasFlag('?')) {
            throw new InvalidUsageException(null, this, true);
        }

        Object[] args = new Object[parameters.length];
        ContextArgumentStack arguments = new ContextArgumentStack(context);
        ParameterData parameter = null;

        try {
            // preProcess handlers
            List<InvokeHandler> handlers = new ArrayList<>();
            for (InvokeListener listener : builder.getInvokeListeners()) {
                InvokeHandler handler = listener.createInvokeHandler();
                handlers.add(handler);
                handler.preProcess(object, method, parameters, context);
            }

            // Collect parameters
            for (int i = 0; i < parameters.length; i++) {
                parameter = parameters[i];

                if (mayConsumeArguments(i, arguments)) {
                    // Parse the user input into a method argument
                    ArgumentStack usedArguments = getScopedContext(parameter, arguments);

                    try {
                        args[i] = parameter.getBinding().bind(parameter, usedArguments, false);
                    } catch (MissingParameterException e) {
                        // Not optional? Then we can't execute this command
                        if (!parameter.isOptional()) {
                            throw e;
                        }

                        args[i] = getDefaultValue(i, arguments);
                    }
                } else {
                    args[i] = getDefaultValue(i, arguments);
                }
            }

            // Check for unused arguments
            checkUnconsumed(arguments);

            // preInvoke handlers
            for (InvokeHandler handler : handlers) {
                handler.preInvoke(object, method, parameters, args, context);
            }

            // Execute!
            method.invoke(object, args);

            // postInvoke handlers
            for (InvokeHandler handler : handlers) {
                handler.postInvoke(handler, method, parameters, args, context);
            }
        } catch (MissingParameterException e) {
            throw new InvalidUsageException("Too few parameters!", this);
        } catch (UnconsumedParameterException e) {
            throw new InvalidUsageException("Too many parameters! Unused parameters: " + e.getUnconsumed(), this);
        } catch (ParameterException e) {
            assert parameter != null;
            String name = parameter.getName();

            throw new InvalidUsageException("For parameter '" + name + "': " + e.getMessage(), this);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof CommandException) {
                throw (CommandException) e.getCause();
            }
            throw new WrappedCommandException(e);
        } catch (Throwable t) {
            throw new WrappedCommandException(t);
        }

        return true;
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        return builder.getDefaultCompleter().getSuggestions(arguments, locals);
    }

    /**
     * Get a list of value flags used by this command.
     *
     * @return a list of value flags
     */
    public Set<Character> getValueFlags() {
        return valueFlags;
    }

    @Override
    public SimpleDescription getDescription() {
        return description;
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        if (commandPermissions != null) {
            for (String perm : commandPermissions.value()) {
                if (builder.getAuthorizer().testPermission(locals, perm)) {
                    return true;
                }
            }

            return false;
        } else {
            return true;
        }
    }

    /**
     * Get the right {@link ArgumentStack}.
     * 
     * @param parameter the parameter
     * @param existing the existing scoped context
     * @return the context to use
     */
    private static ArgumentStack getScopedContext(Parameter parameter, ArgumentStack existing) {
        if (parameter.getFlag() != null) {
            CommandContext context = existing.getContext();
            
            if (parameter.isValueFlag()) {
                return new StringArgumentStack(context, context.getFlag(parameter.getFlag()), false);
            } else {
                String v = context.hasFlag(parameter.getFlag()) ? "true" : "false";
                return new StringArgumentStack(context, v, true);
            }
        }
        
        return existing;
    }
    
    /**
     * Get whether a parameter is allowed to consume arguments.
     * 
     * @param i the index of the parameter
     * @param scoped the scoped context
     * @return true if arguments may be consumed
     */
    private boolean mayConsumeArguments(int i, ContextArgumentStack scoped) {
        CommandContext context = scoped.getContext();
        ParameterData parameter = parameters[i];
        
        // Flag parameters: Always consume
        // Required non-flag parameters: Always consume
        // Optional non-flag parameters:
        //     - Before required parameters: Consume if there are 'left over' args
        //     - At the end: Always consumes

        if (parameter.isOptional()) {
            if (parameter.getFlag() != null) {
                return !parameter.isValueFlag() || context.hasFlag(parameter.getFlag());
            } else {
                int numberFree = context.argsLength() - scoped.position();
                for (int j = i; j < parameters.length; j++) {
                    if (parameters[j].isNonFlagConsumer() && !parameters[j].isOptional()) {
                        // We already checked if the consumed count was > -1
                        // when we created this object
                        numberFree -= parameters[j].getConsumedCount();
                    }
                }

                // Skip this optional parameter
                if (numberFree < 1) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * Get the default value for a parameter.
     * 
     * @param i the index of the parameter
     * @param scoped the scoped context
     * @return a value
     * @throws ParameterException on an error
     * @throws CommandException on an error
     */
    private Object getDefaultValue(int i, ContextArgumentStack scoped) throws ParameterException, CommandException, InvocationTargetException {
        CommandContext context = scoped.getContext();
        ParameterData parameter = parameters[i];
        
        String[] defaultValue = parameter.getDefaultValue();
        if (defaultValue != null) {
            try {
                return parameter.getBinding().bind(parameter, new StringArgumentStack(context, defaultValue, false), false);
            } catch (MissingParameterException e) {
                throw new ParametricException(
                        "The default value of the parameter using the binding " + 
                        parameter.getBinding().getClass() + " in the method\n" +
                        method.toGenericString() + "\nis invalid");
            }
        }
        
        return null;
    }

    
    /**
     * Check to see if all arguments, including flag arguments, were consumed.
     * 
     * @param scoped the argument scope 
     * @throws UnconsumedParameterException thrown if parameters were not consumed
     */
    private void checkUnconsumed(ContextArgumentStack scoped) throws UnconsumedParameterException {
        CommandContext context = scoped.getContext();
        String unconsumed;
        String unconsumedFlags = getUnusedFlags(context);
        
        if ((unconsumed = scoped.getUnconsumed()) != null) {
            throw new UnconsumedParameterException(unconsumed + " " + unconsumedFlags);
        }
        
        if (unconsumedFlags != null) {
            throw new UnconsumedParameterException(unconsumedFlags);
        }
    }
    
    /**
     * Get any unused flag arguments.
     * 
     * @param context the command context
     */
    private String getUnusedFlags(CommandContext context) {
        if (!anyFlags) {
            Set<Character> unusedFlags = null;
            for (char flag : context.getFlags()) {
                boolean found = false;

                if (legacyFlags.contains(flag)) {
                    break;
                }

                for (ParameterData parameter : parameters) {
                    Character paramFlag = parameter.getFlag();
                    if (paramFlag != null && flag == paramFlag) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    if (unusedFlags == null) {
                        unusedFlags = new HashSet<>();
                    }
                    unusedFlags.add(flag);
                }
            }

            if (unusedFlags != null) {
                StringBuilder builder = new StringBuilder();
                for (Character flag : unusedFlags) {
                    builder.append("-").append(flag).append(" ");
                }

                return builder.toString().trim();
            }
        }

        return null;
    }
    
    /**
     * Generate a name for a parameter.
     * 
     * @param type the type
     * @param classifier the classifier
     * @param index the index
     * @return a generated name
     */
    private static String generateName(Type type, Annotation classifier, int index) {
        if (classifier != null) {
            return classifier.annotationType().getSimpleName().toLowerCase();
        } else {
            if (type instanceof Class<?>) {
                return ((Class<?>) type).getSimpleName().toLowerCase();
            } else {
                return "unknown" + index;
            }
        }
    }

}
