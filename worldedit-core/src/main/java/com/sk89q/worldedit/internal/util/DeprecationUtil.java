/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.util;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.Constants;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.lang.reflect.Method;
import java.util.stream.Stream;

public class DeprecationUtil {

    private DeprecationUtil() {
    }

    /**
     * Verify that one of the two functions is overridden. Caller method must be the new method,
     * annotated with {@link NonAbstractForCompatibility}.
     *
     * @param implementingClass the result of calling {@link Object#getClass()}
     */
    public static void checkDelegatingOverride(Class<?> implementingClass) {
        // pull the information about the caller
        StackTraceElement caller = Throwables.lazyStackTrace(new Throwable()).get(1);
        // find the matching caller method
        Method callingMethod = getCallingMethod(caller);
        NonAbstractForCompatibility annotation =
            callingMethod.getAnnotation(NonAbstractForCompatibility.class);
        // get the deprecated method
        Method deprecatedMethod;
        try {
            deprecatedMethod = implementingClass.getMethod(
                annotation.delegateName(), annotation.delegateParams()
            );
        } catch (NoSuchMethodException e) {
            throw new AssertionError(
                "Missing method referenced by " + NonAbstractForCompatibility.class, e
            );
        }
        // Check if the deprecated method was overridden. If the declaring class is the caller's
        // class, then it wasn't. That means that the caller method (i.e. the new method) should be
        // overridden by the implementing class.
        // There's no need to check if the new method has been overridden, since the only other
        // way this could be reached is if someone calls `super.xyz`, which they have no reason to.
        if (deprecatedMethod.getDeclaringClass().getName().equals(caller.getClassName())) {
            throw new IllegalStateException("Class " + implementingClass.getName()
                + " must override " + methodToString(callingMethod));
        }
    }

    private static Method getCallingMethod(StackTraceElement callerInfo) {
        Method[] declaredMethods;
        try {
            declaredMethods = Class.forName(callerInfo.getClassName()).getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            throw new AssertionError("Caller class missing?", e);
        }
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.isAnnotationPresent(NonAbstractForCompatibility.class)
                && declaredMethod.getName().equals(callerInfo.getMethodName())) {
                return declaredMethod;
            }
        }
        throw new IllegalStateException("Failed to find caller method "
            + callerInfo.getMethodName() + " annotated with " + NonAbstractForCompatibility.class);
    }

    private static String methodToString(Method method) {
        StringBuilder builder = new StringBuilder(method.getDeclaringClass().getCanonicalName())
            .append('.')
            .append(method.getName())
            .append('(');
        Joiner.on(", ").appendTo(builder, Stream.of(method.getParameterTypes())
            .map(Class::getSimpleName)
            .iterator());
        builder.append(')');
        return builder.toString();
    }

    public static boolean isSign(BlockType blockType) {
        @SuppressWarnings("deprecation")
        BlockType sign = BlockTypes.SIGN;
        @SuppressWarnings("deprecation")
        BlockType wallSign = BlockTypes.WALL_SIGN;
        return blockType == sign || blockType == wallSign
            || BlockCategories.SIGNS.contains(blockType);
    }

    public static String getHeadOwnerKey() {
        int dataVersion = WorldEdit.getInstance().getPlatformManager()
            .queryCapability(Capability.GAME_HOOKS).getDataVersion();
        return dataVersion >= Constants.DATA_VERSION_MC_1_16 ? "SkullOwner" : "Owner";
    }

}
