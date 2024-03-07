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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.math.Vector3;

/**
 * An affine transform.
 *
 * <p>This class is based on the one from the
 * <a href="http://geom-java.sourceforge.net/index.html">JavaGeom project</a>,
 * which is licensed under LGPL v2.1.</p>
 */
public record AffineTransform(
    // coefficients for x coordinate.
    double m00, double m01, double m02, double m03,
    // coefficients for y coordinate.
    double m10, double m11, double m12, double m13,
    // coefficients for z coordinate.
    double m20, double m21, double m22, double m23
) implements Transform {

    // ===================================================================
    // constructors

    /**
     * Creates a new affine transform3D set to the identity.
     */
    public AffineTransform() {
        // init to identity matrix
        this(
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0
        );
    }

    private static double indexCoeffs(double[] coeffs, int destIndex) {
        if (coeffs.length == 12) {
            return coeffs[destIndex];
        }
        if (coeffs.length != 9) {
            throw new IllegalArgumentException("Input array must have 9 or 12 elements");
        }
        if (destIndex > 0 && destIndex % 3 == 0) {
            // Length 9 has holes in m03, m13, m23
            return 0;
        }
        // Adjust for missing holes
        destIndex -= destIndex / 3;
        return coeffs[destIndex];
    }

    /**
     * Creates a new affine transform from the given coefficients.
     *
     * @param coefs array of 9 to 12 coefficients
     * @deprecated Use {@link #AffineTransform(double, double, double, double, double, double, double, double, double, double, double, double)} instead
     */
    @Deprecated
    public AffineTransform(double[] coefs) {
        this(
            indexCoeffs(coefs, 0), indexCoeffs(coefs, 1), indexCoeffs(coefs, 2), indexCoeffs(coefs, 3),
            indexCoeffs(coefs, 4), indexCoeffs(coefs, 5), indexCoeffs(coefs, 6), indexCoeffs(coefs, 7),
            indexCoeffs(coefs, 8), indexCoeffs(coefs, 9), indexCoeffs(coefs, 10), indexCoeffs(coefs, 11)
        );
    }

    // ===================================================================
    // accessors

    @Override
    public boolean isIdentity() {
        return m00 == m11 && m11 == m22 && m22 == 1
            && m01 == m02 && m02 == m03 && m03 == 0
            && m10 == m12 && m12 == m13 && m13 == 0
            && m20 == m21 && m21 == m23 && m23 == 0;
    }

    /**
     * Returns the affine coefficients of the transform. Result is an array of
     * 12 double.
     */
    public double[] coefficients() {
        return new double[]{m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23};
    }

    /**
     * Computes the determinant of this transform. Can be zero.
     *
     * @return the determinant of the transform.
     */
    private double determinant() {
        return m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m20 * m12)
                + m02 * (m10 * m21 - m20 * m11);
    }

    /**
     * Computes the inverse affine transform.
     */
    @Override
    public AffineTransform inverse() {
        if (isIdentity()) {
            return this;
        }
        double det = this.determinant();
        return new AffineTransform(
                (m11 * m22 - m21 * m12) / det,
                (m21 * m02 - m01 * m22) / det,
                (m01 * m12 - m11 * m02) / det,
                (m01 * (m22 * m13 - m12 * m23) + m02 * (m11 * m23 - m21 * m13)
                        - m03 * (m11 * m22 - m21 * m12)) / det,
                (m20 * m12 - m10 * m22) / det,
                (m00 * m22 - m20 * m02) / det,
                (m10 * m02 - m00 * m12) / det,
                (m00 * (m12 * m23 - m22 * m13) - m02 * (m10 * m23 - m20 * m13)
                        + m03 * (m10 * m22 - m20 * m12)) / det,
                (m10 * m21 - m20 * m11) / det,
                (m20 * m01 - m00 * m21) / det,
                (m00 * m11 - m10 * m01) / det,
                (m00 * (m21 * m13 - m11 * m23) + m01 * (m10 * m23 - m20 * m13)
                        - m03 * (m10 * m21 - m20 * m11)) / det);
    }

    // ===================================================================
    // general methods

    /**
     * Returns the affine transform created by applying first the affine
     * transform given by the parameters, then this affine transform.
     *
     * @return the composition this * that
     */
    public AffineTransform concatenate(double o00, double o01, double o02, double o03,
                                       double o10, double o11, double o12, double o13,
                                       double o20, double o21, double o22, double o23) {
        double n00 = m00 * o00 + m01 * o10 + m02 * o20;
        double n01 = m00 * o01 + m01 * o11 + m02 * o21;
        double n02 = m00 * o02 + m01 * o12 + m02 * o22;
        double n03 = m00 * o03 + m01 * o13 + m02 * o23 + m03;
        double n10 = m10 * o00 + m11 * o10 + m12 * o20;
        double n11 = m10 * o01 + m11 * o11 + m12 * o21;
        double n12 = m10 * o02 + m11 * o12 + m12 * o22;
        double n13 = m10 * o03 + m11 * o13 + m12 * o23 + m13;
        double n20 = m20 * o00 + m21 * o10 + m22 * o20;
        double n21 = m20 * o01 + m21 * o11 + m22 * o21;
        double n22 = m20 * o02 + m21 * o12 + m22 * o22;
        double n23 = m20 * o03 + m21 * o13 + m22 * o23 + m23;
        return new AffineTransform(
            n00, n01, n02, n03,
            n10, n11, n12, n13,
            n20, n21, n22, n23);
    }

    /**
     * Returns the affine transform created by applying first the affine
     * transform given by {@code that}, then this affine transform.
     *
     * @param that the transform to apply first
     * @return the composition this * that
     */
    public AffineTransform concatenate(AffineTransform that) {
        return concatenate(
            that.m00, that.m01, that.m02, that.m03,
            that.m10, that.m11, that.m12, that.m13,
            that.m20, that.m21, that.m22, that.m23
        );
    }

    /**
     * Return the affine transform created by applying first this affine
     * transform, then the affine transform given by {@code that}.
     *
     * @param that the transform to apply in a second step
     * @return the composition that * this
     */
    public AffineTransform preConcatenate(AffineTransform that) {
        double n00 = that.m00 * m00 + that.m01 * m10 + that.m02 * m20;
        double n01 = that.m00 * m01 + that.m01 * m11 + that.m02 * m21;
        double n02 = that.m00 * m02 + that.m01 * m12 + that.m02 * m22;
        double n03 = that.m00 * m03 + that.m01 * m13 + that.m02 * m23 + that.m03;
        double n10 = that.m10 * m00 + that.m11 * m10 + that.m12 * m20;
        double n11 = that.m10 * m01 + that.m11 * m11 + that.m12 * m21;
        double n12 = that.m10 * m02 + that.m11 * m12 + that.m12 * m22;
        double n13 = that.m10 * m03 + that.m11 * m13 + that.m12 * m23 + that.m13;
        double n20 = that.m20 * m00 + that.m21 * m10 + that.m22 * m20;
        double n21 = that.m20 * m01 + that.m21 * m11 + that.m22 * m21;
        double n22 = that.m20 * m02 + that.m21 * m12 + that.m22 * m22;
        double n23 = that.m20 * m03 + that.m21 * m13 + that.m22 * m23 + that.m23;
        return new AffineTransform(
                n00, n01, n02, n03,
                n10, n11, n12, n13,
                n20, n21, n22, n23);
    }

    public AffineTransform translate(Vector3 vec) {
        return translate(vec.x(), vec.y(), vec.z());
    }

    public AffineTransform translate(BlockVector3 vec) {
        return translate(vec.x(), vec.y(), vec.z());
    }

    public AffineTransform translate(double x, double y, double z) {
        return concatenate(1, 0, 0, x, 0, 1, 0, y, 0, 0, 1, z);
    }

    public AffineTransform rotateX(double theta) {
        double cot = MathUtils.dCos(theta);
        double sit = MathUtils.dSin(theta);
        return concatenate(
            1, 0, 0, 0,
            0, cot, -sit, 0,
            0, sit, cot, 0
        );
    }

    public AffineTransform rotateY(double theta) {
        double cot = MathUtils.dCos(theta);
        double sit = MathUtils.dSin(theta);
        return concatenate(
            cot, 0, sit, 0,
            0, 1, 0, 0,
            -sit, 0, cot, 0
        );
    }

    public AffineTransform rotateZ(double theta) {
        double cot = MathUtils.dCos(theta);
        double sit = MathUtils.dSin(theta);
        return concatenate(
            cot, -sit, 0, 0,
            sit, cot, 0, 0,
            0, 0, 1, 0
        );
    }

    public AffineTransform scale(double s) {
        return scale(s, s, s);
    }

    public AffineTransform scale(double sx, double sy, double sz) {
        return concatenate(sx, 0, 0, 0, 0, sy, 0, 0, 0, 0, sz, 0);
    }

    public AffineTransform scale(Vector3 vec) {
        return scale(vec.x(), vec.y(), vec.z());
    }

    @Override
    public Vector3 apply(Vector3 vector) {
        return Vector3.at(
                vector.x() * m00 + vector.y() * m01 + vector.z() * m02 + m03,
                vector.x() * m10 + vector.y() * m11 + vector.z() * m12 + m13,
                vector.x() * m20 + vector.y() * m21 + vector.z() * m22 + m23);
    }

    public AffineTransform combine(AffineTransform other) {
        return concatenate(other);
    }

    @Override
    public Transform combine(Transform other) {
        if (other instanceof AffineTransform otherTransform) {
            return concatenate(otherTransform);
        } else {
            return new CombinedTransform(this, other);
        }
    }

    /**
     * Returns if this affine transform represents a horizontal flip.
     */
    public boolean isHorizontalFlip() {
        // use the determinant of the x-z submatrix to check if this is a horizontal flip
        return m00 * m22 - m02 * m20 < 0;
    }

    /**
     * Returns if this affine transform represents a vertical flip.
     */
    public boolean isVerticalFlip() {
        return m11 < 0;
    }

    @Override
    public String toString() {
        return String.format("Affine[%g %g %g %g, %g %g %g %g, %g %g %g %g]}", m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23);
    }


}
