package com.sk89q.worldedit.extension.platform;

import com.google.auto.value.AutoAnnotation;
import com.sk89q.worldedit.internal.annotation.Radii;

/**
 * Holder for generated annotation classes.
 */
class Annotations {

    @AutoAnnotation
    static Radii radii(int value) {
        return new AutoAnnotation_Annotations_radii(value);
    }

}
