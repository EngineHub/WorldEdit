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

package com.sk89q.worldedit.util.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

/**
 * Argument provider for various vectors.
 */
public final class VariedVectors implements ArgumentsProvider, AnnotationConsumer<VariedVectors.Test> {

    @Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ArgumentsSource(VariedVectors.class)
    @ParameterizedTest(name = ParameterizedTest.ARGUMENTS_PLACEHOLDER)
    public @interface Test {

        /**
         * If {@code true}, provide a non-matching vector from
         * the existing vectors set as well. This will nearly
         * square the number of tests executed, since it will
         * test <em>every</em> non-matching vector.
         */
        boolean provideNonMatching() default false;

        /**
         * If {@code true}, only provide vectors inside the range of Vanilla MC.
         * This caps the Y value to 255.
         */
        boolean capToVanilla() default false;

        int divisionsXZ() default -1;

        int divisionsY() default -1;

    }

    private boolean provideNonMatching;
    private VariedVectorGenerator generator;

    @Override
    public void accept(Test test) {
        provideNonMatching = test.provideNonMatching();
        generator = new VariedVectorGenerator(test.capToVanilla(), test.divisionsXZ(), test.divisionsY());
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        if (provideNonMatching) {
            return generator.makeVectorsStream()
                .flatMap(vec -> generator.makeVectorsStream().filter(v -> !v.equals(vec))
                    .map(v -> Arguments.of(vec, v)));
        }
        return generator.makeVectorsStream().map(Arguments::of);
    }

}
