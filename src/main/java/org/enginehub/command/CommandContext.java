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

import java.util.*;

/**
 * Parses user input and extracts the command, arguments, and flags.
 *
 * <p>For example, if the user input of
 * {@code /cmd -an floated eduardo "alice surita" -- -b} was given to this object
 * (note: -n is a value flag), the following information would have been extracted:</p>
 *
 * <ul>
 *     <li>The command name ("/cmd")</li>
 *     <li>The boolean flag ("a")</li>
 *     <li>The value flag ("n" with value "floated")</li>
 *     <li>Argument 0, corresponding to "eduardo"</li>
 *     <li>Argument 1, corresponding to "alice surita" (without quotation marks)</li>
 *     <li>Argument 2, corresponding to "-b"</li>
 * </ul>
 *
 * <p>The use of "--" indicates the end of flag parsing, otherwise -b would
 * have been parsed as a boolean flag of "b". Flags can appear anywhere in the
 * input, as long as they are before the special flag termination argument. Flags can
 * also be grouped together or listed separately, as long as all flags are prefixed
 * with a dash ("-"). There is no support for flag names that consist of more
 * than one character and so {@code -abcde} can only indicate five different flags. Each
 * value flag consumes one further argument following it, or the argument following the
 * previously consumed argument (in the case of grouped value flags, a.k.a.
 * {@code -abc value1 value2 value3}).</p>
 *
 * <p>The reason that "n" was known to be a value flag (and thus requiring the
 * consumption of a later argument) was because it would have been provided in the set
 * given to {@link #CommandContext(String[], java.util.Set)} or
 * {@link #CommandContext(String, java.util.Set)}. If "n" had <strong>not</strong> been
 * provided in that set, then the results would have been a bit different:</p>
 *
 * <ul>
 *     <li>The command name ("/cmd")</li>
 *     <li>The boolean flag ("a")</li>
 *     <li>The boolean flag ("n")</li>
 *     <li>Argument 0, corresponding to "floated"</li>
 *     <li>Argument 1, corresponding to "eduardo"</li>
 *     <li>Argument 2, corresponding to "alice surita" (without quotation marks)</li>
 *     <li>Argument 3, corresponding to "-b"</li>
 * </ul>
 *
 * <p>An important thing to note is that / was part of the string passed to this object,
 * and thus the command extracted by this object would have had the slash in it.
 * This is not recommended as that requires downstream code to be aware of the
 * current command prefix (which may be an exclamation mark, a period, or a host of
 * other possibilities) if it wishes to inspect the command. Therefore,
 * rather than passing {@code /cmd -an floated eduardo "alice surita" -- -b} to
 * a constructor of this class, it would have been preferred if
 * {@code cmd -an floated eduardo "alice surita" -- -b} was passed. However, this
 * class does not particularly care if the prefix is not removed and this
 * recommendation does not have to be followed.</p>
 *
 * <p>Additionally, sometimes extra information that may be needed for a command
 * to be executed is derived from other sources (and not from user input). This
 * "out of bound" data can be attached to instances of this class. For example, it may
 * be required to know <em>who</em> issued the command, and that information
 * could be stored on this object.</p>
 *
 * <p>Convenience methods are provided to assist in parsing the arguments. Arguments
 * can be read with an absolute index or arguments can be "consumed" using an internal
 * counter that increments one place during each call to a next*() method.</p>
 *
 * <p>This object implements {@link List}<String> for convenience reasons,
 * but be aware that contexts are not mutable and any methods that would
 * modify the list throw an {@link UnsupportedOperationException}.</p>
 */
public class CommandContext implements List<String>, Iterable<String> {

    private final String command;
    private final List<String> parsedArgs;
    private final List<Integer> originalArgIndices;
    private final String[] originalArgs;
    private final Set<Character> booleanFlags = new HashSet<Character>();
    private final Map<Character, String> valueFlags = new HashMap<Character, String>();
    private final boolean isHanging;

    /**
     * Internal counter for the next*() methods.
     */
    private int argIndex = 0;

    /**
     * Construct an instance using a string of arguments. The first argument should be
     * the command name, ideally without the command prefix (/, !, or whatever it may be).
     *
     * @param args the arguments
     * @throws CommandException thrown if there was an error
     * @see #CommandContext(String[], java.util.Set) for more details
     */
    public CommandContext(String args) throws CommandException {
        this(args.split(" "), null);
    }

    /**
     * Construct an instance using an array of arguments. The first argument should be
     * the command name, ideally without the command prefix (/, !, or whatever it may be).
     *
     * @param args the arguments
     * @throws CommandException thrown if there was an error
     * @see #CommandContext(String[], java.util.Set) for more details
     */
    public CommandContext(String[] args) throws CommandException {
        this(args, null);
    }

    /**
     * Construct an instance using a string of arguments, with support for value
     * (non-boolean) flags. The first argument should be the command name, ideally
     * without the command prefix (/, !, or whatever it may be).
     *
     * @param args the arguments
     * @param valueFlags a set containing a list of flags that are value flags
     * @throws CommandException
     * @see #CommandContext(String[], java.util.Set) for more details
     */
    public CommandContext(String args, Set<Character> valueFlags) throws CommandException {
        this(args.split(" "), valueFlags);
    }

    /**
     * Construct an instance using an array of user input tokens, with support for
     * extracting non-boolean value flags.
     *
     * <p>Value flags that are expected MUST be listed in the set given to this
     * method, otherwise the flags will be interpreted as boolean flags and
     * the corresponding values would be parsed as if they were regular
     * non-flag arguments.</p>
     *
     * @param args the arguments
     * @param valueFlags a set containing all value flags, or pass null if none
     * @throws CommandException thrown if flag fails for some reason
     * @throws IllegalArgumentException if the given array of arguments has length 0
     */
    public CommandContext(String[] args, Set<Character> valueFlags) throws CommandException {
        if (args.length == 0) {
            throw new IllegalArgumentException("Argument array must have length > 0");
        }

        if (valueFlags == null) {
            valueFlags = Collections.emptySet();
        }

        originalArgs = args;
        command = args[0];
        isHanging = (args[args.length - 1].length() == 0); // Trailing space?

        // Eliminate empty args and combine multiword args first
        List<Integer> argIndexList = new ArrayList<Integer>(args.length);
        List<String> argList = new ArrayList<String>(args.length);
        for (int i = 1; i < args.length; ++i) {
            String arg = args[i];
            if (arg.length() == 0) {
                continue;
            }

            argIndexList.add(i);

            switch (arg.charAt(0)) {
                case '\'':
                case '"':
                    final StringBuilder build = new StringBuilder();
                    final char quotedChar = arg.charAt(0);

                    int endIndex;
                    for (endIndex = i; endIndex < args.length; ++endIndex) {
                        final String arg2 = args[endIndex];
                        if (arg2.charAt(arg2.length() - 1) == quotedChar) {
                            if (endIndex != i) build.append(' ');
                            build.append(arg2.substring(endIndex == i ? 1 : 0, arg2.length() - 1));
                            break;
                        } else if (endIndex == i) {
                            build.append(arg2.substring(1));
                        } else {
                            build.append(' ').append(arg2);
                        }
                    }

                    if (endIndex < args.length) {
                        arg = build.toString();
                        i = endIndex;
                    }

                    // In case there is an empty quoted string
                    if (arg.length() == 0) {
                        continue;
                    }
                    // else raise exception about hanging quotes?
            }
            argList.add(arg);
        }

        // Then flags

        this.originalArgIndices = new ArrayList<Integer>(argIndexList.size());
        this.parsedArgs = new ArrayList<String>(argList.size());

        for (int nextArg = 0; nextArg < argList.size(); ) {
            // Fetch argument
            String arg = argList.get(nextArg++);

            // Not a flag?
            if (arg.charAt(0) != '-' || arg.length() == 1 || !arg.matches("^-[a-zA-Z]+$")) {
                originalArgIndices.add(argIndexList.get(nextArg - 1));
                parsedArgs.add(arg);
                continue;
            }

            // Handle flag parsing terminator --
            if (arg.equals("--")) {
                while (nextArg < argList.size()) {
                    originalArgIndices.add(argIndexList.get(nextArg));
                    parsedArgs.add(argList.get(nextArg++));
                }
                break;
            }

            // Go through the flag characters
            for (int i = 1; i < arg.length(); ++i) {
                char flagName = arg.charAt(i);

                if (valueFlags.contains(flagName)) {
                    if (this.valueFlags.containsKey(flagName)) {
                        throw new CommandException(
                                "Value flag '" + flagName + "' already given");
                    }

                    if (nextArg >= argList.size()) {
                        throw new CommandException(
                                "No value specified for the '-" + flagName + "' flag.");
                    }

                    // If it is a value flag, read another argument and add it
                    this.valueFlags.put(flagName, argList.get(nextArg++));
                } else {
                    booleanFlags.add(flagName);
                }
            }
        }
    }

    /**
     * Get the command, which is the first value in the user input..
     *
     * <p>The returned command should <em>not</em> contain the command prefix
     * (/, !, etc.) unless the code constructing this instance did not remove it
     * before passing it to this object.</p>
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns whether there is a hanging argument at the end, such
     * as in the example "/command arg1 " where there is a space at the end.
     *
     * @return if there is a hanging argument
     */
    public boolean isHanging() {
        return isHanging;
    }

    /**
     * Convenience method to check if a given command matches the command in this
     * object. This merely returns the value of a non-case sensitive
     * {@link String#equalsIgnoreCase(String)} call.
     *
     * <p>If the user input passed to this object contained the command prefix
     * (i.e. /command), which it should ideally not, this method will only match
     * if the given parameter also has the same prefix.</p>
     *
     * @param command the command to check
     * @return true if it matches
     */
    public boolean matches(String command) {
        return this.command.equalsIgnoreCase(command);
    }

    /**
     * Get the argument at the given index as a string for the command.
     *
     * <p>This method considers the first index (0) to be the first argument
     * of the command, not the first token in the user input string. For example,
     * the first argument (index 0) in {@code /tp eduardo alice} is "eduardo"
     * and not "/tp".</p>
     *
     * @param index the index to return
     * @return the value
     * @throws IndexOutOfBoundsException if the index is beyond the maximum
     * @see #getCommand() to get the command
     */
    public String get(int index) {
        return parsedArgs.get(index);
    }

    /**
     * Get the argument at the given index as a string for the command.
     *
     * <p>This method considers the first index (0) to be the first argument
     * of the command, not the first token in the user input string. For example,
     * the first argument (index 0) in {@code /tp eduardo alice} is "eduardo"
     * and not "/tp".</p>
     *
     * @param index the index to return
     * @param def the default value if the argument was not provided
     * @return the value, or the default if the argument was not specified
     * @see #getCommand() to get the command
     */
    public String get(int index, String def) {
        return index < parsedArgs.size() ? parsedArgs.get(index) : def;
    }

    /**
     * Get the argument at the given index as a string for the command, as well as
     * all arguments following, joined together with spaces.
     * For example, if the command was "/setname Eduardo Harry" then a call
     * to this method with an initial index of 0 would return "Eduardo Harry"
     * (without quotation marks).
     *
     * <p>This method considers the first index (0) to be the first argument
     * of the command, not the first token in the user input string. For example,
     * the first argument (index 0) in {@code /tp eduardo alice} is "eduardo"
     * and not "/tp".</p>
     *
     * @param index the first index to start consuming arguments from
     * @return the value, or the default if the argument was not specified
     * @throws IndexOutOfBoundsException if the index is beyond the maximum
     * @see #getCommand() to get the command
     */
    public String getAll(int index) {
        index = originalArgIndices.get(index);
        StringBuilder buffer = new StringBuilder(originalArgs[index]);
        for (int i = index + 1; i < originalArgs.length; ++i) {
            buffer.append(" ").append(originalArgs[i]);
        }
        return buffer.toString();
    }

    /**
     * Get the integer argument at the given index. See {@link #get(int)} for a more
     * thorough explanation of the mechanics.
     *
     * <p>This method merely gets the string argument at the given location and
     * converts it to a integer (if possible, otherwise an exception is thrown).</p>
     *
     * @param index the index
     * @return the integer
     * @throws NumberFormatException if the given argument was not actually a number
     * @throws IndexOutOfBoundsException if the index is beyond the maximum
     */
    public int getInteger(int index) throws NumberFormatException {
        return Integer.parseInt(parsedArgs.get(index));
    }

    /**
     * Get the integer argument at the given index, returning a default if the argument
     * was not specified (but an exception is thrown if the argument is provided but
     * is not a valid numeric value). See {@link #get(int)} for a more
     * thorough explanation of the mechanics.
     *
     * <p>This method merely gets the string argument at the given location and
     * converts it to a integer (if possible, otherwise an exception is thrown).</p>
     *
     * @param index the index
     * @return the integer
     * @throws NumberFormatException if the given argument was not actually a number
     */
    public int getInteger(int index, int def) throws NumberFormatException {
        return index < parsedArgs.size() ? Integer.parseInt(parsedArgs.get(index)) : def;
    }

    /**
     * Get the double argument at the given index. See {@link #get(int)} for a more
     * thorough explanation of the mechanics.
     *
     * <p>This method merely gets the string argument at the given location and
     * converts it to a double (if possible, otherwise an exception is thrown).</p>
     *
     * @param index the index
     * @return the integer
     * @throws NumberFormatException if the given argument was not actually a number
     * @throws IndexOutOfBoundsException if the index is beyond the maximum
     */
    public double getDouble(int index) throws NumberFormatException {
        return Double.parseDouble(parsedArgs.get(index));
    }

    /**
     * Get the double argument at the given index, returning a default if the argument
     * was not specified (but an exception is thrown if the argument is provided but
     * is not a valid numeric value). See {@link #get(int)} for a more
     * thorough explanation of the mechanics.
     *
     * <p>This method merely gets the string argument at the given location and
     * converts it to a double (if possible, otherwise an exception is thrown).</p>
     *
     * @param index the index
     * @return the integer
     * @throws NumberFormatException if the given argument was not actually a number
     */
    public double getDouble(int index, double def) throws NumberFormatException {
        return index < parsedArgs.size() ? Double.parseDouble(parsedArgs.get(index)) : def;
    }

    /**
     * Get the next argument as a string, starting at the first command argument
     * (not including the command).
     *
     * <p>If an error occurs when fetching the value, the internal counter will not
     * be advanced.</p>
     *
     * @return the value
     * @throws IndexOutOfBoundsException if there are no more arguments
     * @see #get(int) for mechanics
     */
    public String next() {
        String value = get(argIndex);
        argIndex++;
        return value;
    }

    /**
     * Get the next argument as an integer, starting at the first command argument
     * (not including the command).
     *
     * <p>If an error occurs when fetching the value, the internal counter will not
     * be advanced.</p>
     *
     * @return the value
     * @throws NumberFormatException if the given argument was not actually a number
     * @throws IndexOutOfBoundsException if there are no more arguments
     * @see #getInteger(int) for mechanics
     */
    public int nextInteger() {
        int value = getInteger(argIndex);
        argIndex++;
        return value;
    }

    /**
     * Get the next argument as an double, starting at the first command argument
     * (not including the command).
     *
     * <p>If an error occurs when fetching the value, the internal counter will not
     * be advanced.</p>
     *
     * @return the value
     * @throws NumberFormatException if the given argument was not actually a number
     * @throws IndexOutOfBoundsException if there are no more arguments
     * @see #getDouble(int) for mechanics
     */
    public double nextDouble() {
        double value = getDouble(argIndex);
        argIndex++;
        return value;
    }

    /**
     * Get the next argument as a string, starting at the first command argument
     * (not including the command).
     *
     * <p>The internal argument index will not advance.</p>
     *
     * @return the value
     * @throws IndexOutOfBoundsException if the index is beyond the maximum
     * @see #get(int) for mechanics
     */
    public String peek() {
        return get(argIndex);
    }

    /**
     * Get the next argument as an integer, starting at the first command argument
     * (not including the command).
     *
     * <p>The internal argument index will not advance.</p>
     *
     * @return the value
     * @throws NumberFormatException if the given argument was not actually a number
     * @throws IndexOutOfBoundsException if the index is beyond the maximum
     * @see #getInteger(int) for mechanics
     */
    public int peekInteger() {
        return getInteger(argIndex);
    }

    /**
     * Get the next argument as an double, starting at the first command argument
     * (not including the command).
     *
     * <p>The internal argument index will not advance.</p>
     *
     * @return the value
     * @throws NumberFormatException if the given argument was not actually a number
     * @throws IndexOutOfBoundsException if the index is beyond the maximum
     * @see #getDouble(int) for mechanics
     */
    public double peekDouble() {
        return getDouble(argIndex);
    }

    /**
     * Returns whether there are more arguments to consume.
     *
     * @return true if there are more arguments
     */
    public boolean hasMore() {
        return argIndex < parsedArgs.size();
    }

    /**
     * Returns whether the given flag (either boolean or value) is set.
     *
     * @param ch the flag character
     * @return true if the flag is set
     */
    public boolean hasFlag(char ch) {
        return booleanFlags.contains(ch) || valueFlags.containsKey(ch);
    }

    /**
     * Get the list of boolean flags that have been set by the user.
     *
     * @return the list of boolean flags
     */
    public Set<Character> getBooleanFlags() {
        return booleanFlags;
    }

    /**
     * Get the map of value flags that have been set by the user.
     *
     * @return the map of value flags
     */
    public Map<Character, String> getValueFlags() {
        return valueFlags;
    }

    /**
     * Get the string value of a value flag.
     *
     * @param ch the flag character
     * @return the value, or null if it is not set
     */
    public String getFlag(char ch) {
        return valueFlags.get(ch);
    }

    /**
     * Get the string value of a value flag.
     *
     * @param ch the flag character
     * @param def the default value to return if the flag is not set
     * @return the value, or null if it is not set
     */
    public String getFlag(char ch, String def) {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return value;
    }

    /**
     * Get the integer value of a value flag.
     *
     * @param ch the flag character
     * @return the value
     * @throws NumberFormatException if the given argument was not actually a number,
     *                               or if the argument is not even set
     */
    public int getFlagInteger(char ch) throws NumberFormatException {
        return Integer.parseInt(valueFlags.get(ch));
    }

    /**
     * Get the integer value of a value flag.
     *
     * @param ch the flag character
     * @param def the default value to return if the flag is not set
     * @return the value, or null if it is not set
     * @throws NumberFormatException if the given argument was not actually a number
     */
    public int getFlagInteger(char ch, int def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Integer.parseInt(value);
    }

    /**
     * Get the double value of a value flag.
     *
     * @param ch the flag character
     * @return the value, or null if it is not set
     * @throws NumberFormatException if the given argument was not actually a number,
     *                               or if the argument is not even set
     */
    public double getFlagDouble(char ch) throws NumberFormatException {
        return Double.parseDouble(valueFlags.get(ch));
    }

    /**
     * Get the double value of a value flag.
     *
     * @param ch the flag character
     * @param def the default value to return if the flag is not set
     * @return the value, or null if it is not set
     * @throws NumberFormatException if the given argument was not actually a number
     */
    public double getFlagDouble(char ch, double def) throws NumberFormatException {
        final String value = valueFlags.get(ch);
        if (value == null) {
            return def;
        }

        return Double.parseDouble(value);
    }

    /**
     * Return whether the user input was completely empty.
     *
     * @return true if there is no command and no arguments
     */
    public boolean isCompletelyEmpty() {
        return size() == 0 && getCommand().length() == 0;
    }

    /**
     * Get the number of arguments, not including the command.
     */
    @Override
    public boolean isEmpty() {
        return size() > 0;
    }

    /**
     * Get an array of arguments, which does not include the command.
     */
    @Override
    public Object[] toArray() {
        return parsedArgs.toArray();
    }

    /**
     * Get an array of arguments, which does not include the command.
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return parsedArgs.toArray(a);
    }

    /**
     * Get the number of arguments, which does not include the command.
     */
    @Override
    public int size() {
        return parsedArgs.size();
    }

    /**
     * Get an iterator over just the arguments (excluding the command).
     */
    @Override
    public Iterator<String> iterator() {
        return parsedArgs.iterator();
    }

    /**
     * Get a list iterator over just the arguments (excluding the command).
     */
    @Override
    public ListIterator<String> listIterator() {
        return parsedArgs.listIterator();
    }

    /**
     * Get a list iterator over just the arguments (excluding the command).
     */
    @Override
    public ListIterator<String> listIterator(int index) {
        return parsedArgs.listIterator(index);
    }

    /**
     * Get a sub-list of just the arguments (excluding the command).
     */
    @Override
    public List<String> subList(int fromIndex, int toIndex) {
        return parsedArgs.subList(fromIndex, toIndex);
    }

    /**
     * Does not particularly make sense, but it is implemented.
     */
    @Override
    public boolean contains(Object o) {
        return parsedArgs.contains(o);
    }

    /**
     * Does not particularly make sense, but it is implemented.
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return parsedArgs.containsAll(c);
    }

    /**
     * Does not particularly make sense, but it is implemented.
     */
    @Override
    public int indexOf(Object o) {
        return parsedArgs.indexOf(o);
    }

    /**
     * Does not particularly make sense, but it is implemented.
     */
    @Override
    public int lastIndexOf(Object o) {
        return parsedArgs.lastIndexOf(o);
    }

    /**
     * Not supported.
     */
    @Override
    public boolean add(String s) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public void add(int index, String element) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public String remove(int index) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public boolean addAll(Collection<? extends String> c) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public boolean addAll(int index, Collection<? extends String> c) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public String set(int index, String element) {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

    /**
     * Not supported.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException("CommandContexts are non-mutable");
    }

}
