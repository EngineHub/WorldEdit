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

package com.sk89q.worldedit.util.io;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

public final class Closer implements Closeable {

    private static final Logger logger = Logger.getLogger(Closer.class.getCanonicalName());

    /**
     * The suppressor implementation to use for the current Java version.
     */
    private static final Suppressor SUPPRESSOR = SuppressingSuppressor.isAvailable()
            ? SuppressingSuppressor.INSTANCE
            : LoggingSuppressor.INSTANCE;

    /**
     * Creates a new {@link Closer}.
     */
    public static Closer create() {
        return new Closer(SUPPRESSOR);
    }

    @VisibleForTesting
    final Suppressor suppressor;

    // only need space for 2 elements in most cases, so try to use the smallest array possible
    private final Deque<Closeable> stack = new ArrayDeque<>(4);
    private final Deque<ZipFile> zipStack = new ArrayDeque<>(4);
    private Throwable thrown;

    @VisibleForTesting Closer(Suppressor suppressor) {
        this.suppressor = checkNotNull(suppressor); // checkNotNull to satisfy null tests
    }

    /**
     * Registers the given {@code closeable} to be closed when this {@code Closer} is
     * {@linkplain #close closed}.
     *
     * @return the given {@code closeable}
     */
    // close. this word no longer has any meaning to me.
    public <C extends Closeable> C register(C closeable) {
        stack.push(closeable);
        return closeable;
    }

    /**
     * Registers the given {@code zipFile} to be closed when this {@code Closer} is
     * {@linkplain #close closed}.
     *
     * @return the given {@code closeable}
     */
    public <Z extends ZipFile> Z register(Z zipFile) {
        zipStack.push(zipFile);
        return zipFile;
    }

    /**
     * Stores the given throwable and rethrows it. It will be rethrown as is if it is an
     * {@code IOException}, {@code RuntimeException} or {@code Error}. Otherwise, it will be rethrown
     * wrapped in a {@code RuntimeException}. <b>Note:</b> Be sure to declare all of the checked
     * exception types your try block can throw when calling an overload of this method so as to avoid
     * losing the original exception type.
     *
     * <p>This method always throws, and as such should be called as
     * {@code throw closer.rethrow(e);} to ensure the compiler knows that it will throw.
     *
     * @return this method does not return; it always throws
     * @throws IOException when the given throwable is an IOException
     */
    public RuntimeException rethrow(Throwable e) throws IOException {
        thrown = e;
        Throwables.propagateIfPossible(e, IOException.class);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    /**
     * Stores the given throwable and rethrows it. It will be rethrown as is if it is an
     * {@code IOException}, {@code RuntimeException}, {@code Error} or a checked exception of the
     * given type. Otherwise, it will be rethrown wrapped in a {@code RuntimeException}. <b>Note:</b>
     * Be sure to declare all of the checked exception types your try block can throw when calling an
     * overload of this method so as to avoid losing the original exception type.
     *
     * <p>This method always throws, and as such should be called as
     * {@code throw closer.rethrow(e, ...);} to ensure the compiler knows that it will throw.
     *
     * @return this method does not return; it always throws
     * @throws IOException when the given throwable is an IOException
     * @throws X when the given throwable is of the declared type X
     */
    public <X extends Exception> RuntimeException rethrow(Throwable e,
                                                          Class<X> declaredType) throws IOException, X {
        thrown = e;
        Throwables.propagateIfPossible(e, IOException.class);
        Throwables.propagateIfPossible(e, declaredType);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    /**
     * Stores the given throwable and rethrows it. It will be rethrown as is if it is an
     * {@code IOException}, {@code RuntimeException}, {@code Error} or a checked exception of either
     * of the given types. Otherwise, it will be rethrown wrapped in a {@code RuntimeException}.
     * <b>Note:</b> Be sure to declare all of the checked exception types your try block can throw
     * when calling an overload of this method so as to avoid losing the original exception type.
     *
     * <p>This method always throws, and as such should be called as
     * {@code throw closer.rethrow(e, ...);} to ensure the compiler knows that it will throw.
     *
     * @return this method does not return; it always throws
     * @throws IOException when the given throwable is an IOException
     * @throws X1 when the given throwable is of the declared type X1
     * @throws X2 when the given throwable is of the declared type X2
     */
    public <X1 extends Exception, X2 extends Exception> RuntimeException rethrow(
            Throwable e, Class<X1> declaredType1, Class<X2> declaredType2) throws IOException, X1, X2 {
        thrown = e;
        Throwables.propagateIfPossible(e, IOException.class);
        Throwables.propagateIfPossible(e, declaredType1, declaredType2);
        Throwables.throwIfUnchecked(e);
        throw new RuntimeException(e);
    }

    /**
     * Closes all {@code Closeable} instances that have been added to this {@code Closer}. If an
     * exception was thrown in the try block and passed to one of the {@code exceptionThrown} methods,
     * any exceptions thrown when attempting to close a closeable will be suppressed. Otherwise, the
     * <i>first</i> exception to be thrown from an attempt to close a closeable will be thrown and any
     * additional exceptions that are thrown after that will be suppressed.
     */
    @Override
    public void close() throws IOException {
        Throwable throwable = thrown;

        // close closeables in LIFO order
        while (!stack.isEmpty()) {
            Closeable closeable = stack.pop();
            try {
                closeable.close();
            } catch (Throwable e) {
                if (throwable == null) {
                    throwable = e;
                } else {
                    suppressor.suppress(closeable, throwable, e);
                }
            }
        }
        while (!zipStack.isEmpty()) {
            ZipFile zipFile = zipStack.pop();
            try {
                zipFile.close();
            } catch (Throwable e) {
                if (throwable == null) {
                    throwable = e;
                } else {
                    suppressor.suppress(zipFile, throwable, e);
                }
            }
        }

        if (thrown == null && throwable != null) {
            Throwables.propagateIfPossible(throwable, IOException.class);
            throw new AssertionError(throwable); // not possible
        }
    }

    /**
     * Suppression strategy interface.
     */
    @VisibleForTesting interface Suppressor {
        /**
         * Suppresses the given exception ({@code suppressed}) which was thrown when attempting to close
         * the given closeable. {@code thrown} is the exception that is actually being thrown from the
         * method. Implementations of this method should not throw under any circumstances.
         */
        void suppress(Object closeable, Throwable thrown, Throwable suppressed);
    }

    /**
     * Suppresses exceptions by logging them.
     */
    @VisibleForTesting static final class LoggingSuppressor implements Suppressor {

        static final LoggingSuppressor INSTANCE = new LoggingSuppressor();

        @Override
        public void suppress(Object closeable, Throwable thrown, Throwable suppressed) {
            // log to the same place as Closeables
            logger.log(Level.WARNING, "Suppressing exception thrown when closing " + closeable, suppressed);
        }
    }

    /**
     * Suppresses exceptions by adding them to the exception that will be thrown using JDK7's
     * addSuppressed(Throwable) mechanism.
     */
    @VisibleForTesting static final class SuppressingSuppressor implements Suppressor {

        static final SuppressingSuppressor INSTANCE = new SuppressingSuppressor();

        static boolean isAvailable() {
            return addSuppressed != null;
        }

        static final Method addSuppressed = getAddSuppressed();

        private static Method getAddSuppressed() {
            try {
                return Throwable.class.getMethod("addSuppressed", Throwable.class);
            } catch (Throwable e) {
                return null;
            }
        }

        @Override
        public void suppress(Object closeable, Throwable thrown, Throwable suppressed) {
            // ensure no exceptions from addSuppressed
            if (thrown == suppressed) {
                return;
            }
            try {
                addSuppressed.invoke(thrown, suppressed);
            } catch (Throwable e) {
                // if, somehow, IllegalAccessException or another exception is thrown, fall back to logging
                LoggingSuppressor.INSTANCE.suppress(closeable, thrown, suppressed);
            }
        }
    }
}
