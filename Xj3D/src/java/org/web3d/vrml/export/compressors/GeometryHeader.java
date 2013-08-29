/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.export.compressors;

/**
 * Header for compressed geometry.
 *
 * @author Alan Hudson
 */
public class GeometryHeader {
    // Uniform scale applied to model
    private float scale;

    // The original bounds
    private float[] bounds;

    // Does this model contain normals
    private boolean hasNormals;

    // Does this model contain colors
    private boolean hasColors;

    // Does this model contain coordinates
    private boolean hasCoordinates;

    // Does this model contain textureCoordinates
    private boolean hasTexCoords;

    public GeometryHeader() {
        bounds = new float[3];
    }

    public GeometryHeader(float scale, float[] bounds,
        boolean hasCoordinates, boolean hasNormals, boolean hasColors,
        boolean hasTexCoords) {

        this.scale = scale;
        this.bounds = bounds;
        this.hasCoordinates = hasCoordinates;
        this.hasNormals = hasNormals;
        this.hasColors = hasColors;
        this.hasTexCoords = hasTexCoords;
    }

    /**
     * Get the size of the header in ints.  Right now this
     * must be int aligned.
     *
     * @return The size in bytes
     */
    public static int getSize() {
        return 4 + 3 * 4 + 1;
    }

    /**
     * Encode this header into a buffer.
     *
     * @param buffer The buffer to encode to
     * @param start The starting index to encode to
     */
    public void encode(int[] buffer, int start) {
        buffer[start++] = Float.floatToIntBits(scale);
        buffer[start++] = Float.floatToIntBits(bounds[0]);
        buffer[start++] = Float.floatToIntBits(bounds[1]);
        buffer[start++] = Float.floatToIntBits(bounds[2]);

        // Encode flags all in one int
        buffer[start++] = (hasCoordinates ? 1 : 0) << 3 |
           (hasNormals ? 1 : 0) << 2 |
           (hasColors ? 1 : 0) << 1 |
           (hasTexCoords ? 1 : 0);
System.out.println("Encoded flags: " + buffer[start - 1]);
    }

    /**
     * Dencode this header from a buffer.
     *
     * @param buffer The buffer to decode from
     * @param start The starting index to decode from
     */
    public void decode(int[] buffer, int start) {
        scale = Float.intBitsToFloat(buffer[start++]);
        bounds[0] = Float.intBitsToFloat(buffer[start++]);
        bounds[1] = Float.intBitsToFloat(buffer[start++]);
        bounds[2] = Float.intBitsToFloat(buffer[start++]);

        int flags = buffer[start++];
System.out.println("Decoded flags: " + buffer[flags]);

        hasCoordinates = ((flags & (1 << 3)) > 0) ? true : false;
        hasNormals = ((flags & (1 << 2)) > 0) ? true : false;
        hasColors = ((flags & (1 << 1)) > 0) ? true : false;
        hasTexCoords = ((flags & (1)) > 0) ? true : false;
    }

    /**
     * Does this model contain normals.
     *
     * @return Does it contain normals
     */
    public boolean hasNormals() {
        return this.hasNormals();
    }

    public float[] getBounds() {
        return bounds;
    }

    public float getScale() {
        return scale;
    }

    /**
     * Get a string representation.
     *
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("Geometry Header: scale: ");
        buf.append(Float.toString(scale));
        buf.append(" bounds: (");
        buf.append(Float.toString(bounds[0]));
        buf.append(' ');
        buf.append(Float.toString(bounds[1]));
        buf.append(' ');
        buf.append(Float.toString(bounds[2]));
        buf.append(")");
        buf.append(" hasCoords: ");
        buf.append(hasCoordinates);
        buf.append(" hasNormals: ");
        buf.append(hasNormals);
        buf.append(" hasColors: ");
        buf.append(hasColors);
        buf.append(" hasTexCoords: ");
        buf.append(hasTexCoords);

        return buf.toString();
    }
}