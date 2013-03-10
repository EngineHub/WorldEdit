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

import static org.enginehub.command.CommandUtils.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.enginehub.util.Proposal;

/**
 * Stores a list of commands and executes the appropriate one when a command needs to
 * be processed (commands are case in-sensitive).
 *
 * <p>Execution of commands is fully thread-safe, in respect to multiple threads
 * attempting to call {@link #execute(CommandContext)} at the same time (assuming that
 * the commands themselves are thread-safe) but managing the list of commands may not
 * be thread-safe. Avoid executing commands while also adjusting the list of commands.</p>
 */
public class CommandManager implements CommandGroup, SuggestionProvider, CommandExecutor {

    /**
     * Stores the list of commands, with lowercase keys.
     */
    private final Map<String, Command> commands = new LinkedHashMap<String, Command>();

    /**
     * Whether hidden commands should also be un-executable.
     */
    private boolean executeOnlyVisible = false;

    /**
     * Get whether hidden methods are also un-executable.
     *
     * @return true if hidden methods should be un-executable
     */
    public boolean getExecuteOnlyVisible() {
        return executeOnlyVisible;
    }

    /**
     * Set hidden methods to be un-executable.
     *
     * @param executeOnlyVisible true to make hidden methods un-executable
     */
    public void setExecuteOnlyVisible(boolean executeOnlyVisible) {
        this.executeOnlyVisible = executeOnlyVisible;
    }

    /**
     * Match the command for a given context.
     *
     * @param context the context
     * @return a command matching the context, or null
     */
    private Command match(CommandContext context) {
        String lower = context.getCommand().toLowerCase();
        return commands.get(lower);
    }

    /**
     * Executes the command given the context.
     *
     * <p>All {@link Throwable}s will be wrpaped into an </p>
     *
     * @param context the context containing parameters
     * @return true if the command existed
     * @throws CommandException thrown on any error error
     */
    @Override
    public boolean execute(CommandContext context) throws CommandException {
        Command command = match(context);
        if (command != null) {
            // Don't execute hidden commands if desired
            if (executeOnlyVisible && !isVisible(command, context)) {
                return false;
            }

            try {
                command.execute(context);
                return true;
            } catch (Throwable t) {
                throw new ExecutionException(t);
            }
        } else {
            return false;
        }
    }

    @Override
    public Set<Suggestion> getProposals(CommandContext context) {
        // Suggest all the commands!
        if (context.isCompletelyEmpty()) {
            Set<Suggestion> proposals = new HashSet<Suggestion>();

            for (Command command : this) {
                if (!isVisible(command, context)) {
                    continue; // Don't suggest invisible commands!
                }

                proposals.add(new Suggestion(command.getAliases()[0]));
            }

            return proposals;

        // Suggest a command completion
        } else if (!context.isHanging()) {
            Set<Suggestion> proposals = new HashSet<Suggestion>();
            String test = context.getCommand().toLowerCase();

            loopCommand:
            for (Command command : this) {
                if (!isVisible(command, context)) {
                    continue; // Don't suggest invisible commands!
                }

                for (String alias : command.getAliases()) {
                    String lowerAlias = alias.toLowerCase();

                    if (lowerAlias.equalsIgnoreCase(test)) {
                        continue loopCommand;

                    // More likely "starts with" test
                    } else if (lowerAlias.startsWith(test)) {
                        proposals.add(new Suggestion(alias.substring(test.length())));
                        continue loopCommand;

                    // Less likely "contains" test
                    } else if (test.length() >= 2 && lowerAlias.contains(test)) {
                        proposals.add(new Suggestion(alias).replaceWord()
                                .confidence(Proposal.LOW_CONFIDENCE));
                        continue loopCommand;
                    }
                }
            }

            return proposals;

        // Ask the command to provide a suggestion
        } else {
            Command command = match(context);
            if (command != null && isVisible(command, context)) { // Do we match a command?
                if (command instanceof SuggestionProvider) {
                    return ((SuggestionProvider) command).getProposals(context);
                }
            } // Well, if there was a command to begin with...!
        }

        return Collections.emptySet(); // No suggestions from us
    }

    @Override
    public synchronized void clear() {
        commands.clear();
    }

    @Override
    public synchronized int size() {
        return commands.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return commands.isEmpty();
    }

    @Override
    public synchronized boolean contains(Object o) {
        return commands.containsValue(o);
    }

    @Override
    public synchronized Iterator<Command> iterator() {
        return commands.values().iterator();
    }

    @Override
    public synchronized Object[] toArray() {
        return commands.values().toArray();
    }

    @Override
    public synchronized <T> T[] toArray(T[] a) {
        return commands.values().toArray(a);
    }

    @Override
    public synchronized boolean add(Command command) {
        boolean changed = false;

        for (String alias : command.getAliases()) {
            if (alias == null) {
                throw new IllegalArgumentException("Command instance has null alias");
            }

            commands.put(alias.toLowerCase(), command);
            changed = true;
        }

        return changed;
    }

    @Override
    public synchronized boolean remove(Object o) {
        if (o instanceof Command) {
            boolean changed = false;
            Command command = (Command) o;

            for (String alias : command.getAliases()) {
                if (alias == null) {
                    throw new IllegalArgumentException("Command instance has null alias");
                }

                String lower = alias.toLowerCase();
                Command existing = commands.get(lower);
                if (existing == command) {
                    commands.remove(lower);
                    changed = true;
                }
            }

            return changed;
        } else {
            return false;
        }
    }

    @Override
    public synchronized boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (contains(o)) return false;
        }
        return true;
    }

    @Override
    public synchronized boolean addAll(Collection<? extends Command> c) {
        boolean changed = false;
        for (Command command : c) {
            changed = add(command) || changed;
        }
        return changed;
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        boolean changed = false;
        for (Object o : c) {
            changed = remove(o) || changed;
        }
        return changed;
    }

    @Override
    public synchronized boolean retainAll(Collection<?> c) {
        boolean changed = false;
        Iterator<Command> it = commands.values().iterator();

        while (it.hasNext()) {
            Command command = it.next();
            if (!c.contains(command)) {
                it.remove();
                changed = true;
            }
        }

        return changed;
    }

}
