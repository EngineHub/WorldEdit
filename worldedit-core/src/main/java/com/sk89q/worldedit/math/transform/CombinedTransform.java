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

package com.sk89q.worldedit.math.transform;

import com.sk89q.worldedit.math.Vector3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Combines several transforms in order.
 */
public class CombinedTransform implements Transform {

    private final Transform[] transforms;

    /**
     * Create a new combined transformation.
     *
     * @param transforms a list of transformations
     */
    public CombinedTransform(Transform... transforms) {
        checkNotNull(transforms);
        this.transforms = Arrays.copyOf(transforms, transforms.length);
    }

    /**
     * Create a new combined transformation.
     *
     * @param transforms a list of transformations
     */
    public CombinedTransform(Collection<Transform> transforms) {
        this(transforms.toArray(new Transform[checkNotNull(transforms).size()]));
    }

    @Override
    public boolean isIdentity() {
        for (Transform transform : transforms) {
            if (!transform.isIdentity()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Vector3 apply(Vector3 vector) {
        for (Transform transform : transforms) {
            vector = transform.apply(vector);
        }
        return vector;
    }

    @Override
    public Transform inverse() {
        List<Transform> list = new ArrayList<>();
        for (int i = transforms.length - 1; i >= 0; i--) {
            list.add(transforms[i].inverse());
        }
        return new CombinedTransform(list);
    }

    @Override
    public Transform combine(Transform other) {
        checkNotNull(other);
        if (other instanceof CombinedTransform) {
            CombinedTransform combinedOther = (CombinedTransform) other;
            Transform[] newTransforms = new Transform[transforms.length + combinedOther.transforms.length];
            System.arraycopy(transforms, 0, newTransforms, 0, transforms.length);
            System.arraycopy(combinedOther.transforms, 0, newTransforms, transforms.length, combinedOther.transforms.length);
            return new CombinedTransform(newTransforms);
        } else {
            return new CombinedTransform(this, other);
        }
    }

}
