/*****************************************************************************
 *                     Yumetech, Inc Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.j3d.aviatrix3d;

// External imports
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL;

// Local imports
import org.j3d.aviatrix3d.picking.*;

/**
 * Raster object that represents a single drawn object using individual bits.
 * <p>
 *
 * Bitmaps describe what should be seen on screen, but not the colour. Colour
 * information is derived from the Pixmap's appearance information. A bitmap
 * also has a local offset field. A user may set explicit bounds for this
 * object, but in doing so may or may not truncate the bits themselves.
 * <p>
 *
 * When providing byte data, each bit represents the state of a single pixel
 * as either on or off. The bitmap width does not need to be a power of 2 or
 * eight to handle this. An image that is 2 pixels high and 10 pixels wide
 * would be represented by 4 bytes. The first byte is the first 8 bits of the
 * input for the first row. The second byte uses the first 2 bits for the
 * remaining part of the first row. The other 6 bits are ignored. The third byte
 * starts the next row, and finally the first 2 bits of the the fourth byte
 * provide the last two pixels of the second row.
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public class BitmapRaster extends Raster
{
    /** Message when the width is less than or equal to zero */
    private static final String NEG_WIDTH_MSG =
        "Bitmap width provided is less than or equal to zero: ";

    /** Message when the height is less than or equal to zero */
    private static final String NEG_HEIGHT_MSG =
        "Bitmap height provided is less than or equal to zero: ";

    /** Error message when the provided bit array is too small */
    private static final String BIT_ARRAY_SIZE_ERR =
        "The provided array of bits is too small for the current width " +
        "and height specified";

    /** Buffer for bitmap pixel data */
    private ByteBuffer pixelBuffer;

    /** Local reference to the pixel data */
    private byte[] pixels;

    /** The origin coordinate to use with the bitmap */
    private float[] origin;

    /** The width of the bitmap in pixels */
    private int pixelWidth;

    /** The height of the bitmap in pixels */
    private int pixelHeight;

    /** The number of bytes needed for the width */
    private int byteWidth;

    /**
     * Create a new empty raster with the given width and height.
     *
     * @param width The width of the raster in pixels
     * @param height The height of the raster in pixels
     * @throws IllegalArgumentException The width or height are not positive
     */
    public BitmapRaster(int width, int height)
    {
        if(width <= 0)
            throw new IllegalArgumentException(NEG_WIDTH_MSG + width);

        if(height <= 0)
            throw new IllegalArgumentException(NEG_HEIGHT_MSG + height);

        origin = new float[2];
        pixelWidth = width;
        pixelHeight = height;

        byteWidth = width / 8;

        if((width % 8) != 0)
            byteWidth++;

        pixelBuffer = ByteBuffer.allocateDirect(pixelHeight * byteWidth);
    }

    //---------------------------------------------------------------
    // Methods defined by GeometryRenderable
    //---------------------------------------------------------------

    /**
     * Render the geometry now.
     *
     * @param gl The GL context to render with
     */
    public void render(GL gl)
    {
        gl.glBitmap(pixelWidth,
                    pixelHeight,
                    origin[0],
                    origin[1],
                    0,
                    0,
                    pixelBuffer);
    }

    //---------------------------------------------------------------
    // Methods defined by LeafPickTarget
    //---------------------------------------------------------------

    /**
     * Check for all intersections against this geometry using a line segment and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param start The start point of the segment
     * @param end The end point of the segment
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    public boolean pickLineSegment(float[] start,
                                   float[] end,
                                   boolean findAny,
                                   float[] dataOut,
                                   int dataOutFlags)
        throws NotPickableException
    {
        super.pickLineSegment(start, end, findAny, dataOut, dataOutFlags);

        return false;
    }

    /**
     * Check for all intersections against this geometry using a line ray and
     * return the exact distance away of the closest picking point. Default
     * implementation always returns false indicating that nothing was found.
     * Derived classes should override and provide a real implementation.
     *
     * @param origin The start point of the ray
     * @param direction The direction vector of the ray
     * @param findAny True if it only has to find a single intersection and can
     *   exit as soon as it finds the first intersection. False if it must find
     *   the closest polygon
     * @param dataOut An array to put the data in for the intersection. Exact
     *   format is described by the flags
     * @param dataOutFlags A set of derived-class specific flags describing what
     *   data should be included in the output array
     * @return True if an intersection was found according to the input request
     * @throws NotPickableException This object has been marked as non pickable,
     *   but you decided to try to call the method anyway
     */
    public boolean pickLineRay(float[] origin,
                               float[] direction,
                               boolean findAny,
                               float[] dataOut,
                               int dataOutFlags)
        throws NotPickableException
    {
        super.pickLineRay(origin, direction, findAny, dataOut, dataOutFlags);

        return false;
    }

    //----------------------------------------------------------
    // Methods defined by Raster
    //----------------------------------------------------------

    /**
     * Update this node's bounds and then call the parent to update it's
     * bounds. Used to propogate bounds changes from the leaves of the tree
     * to the root. A node implementation may decide when and where to tell
     * the parent(s)s that updates are ready.
     */
    protected void updateBounds()
    {
        if(pixels != null)
            super.updateBounds();
    }

    /**
     * Internal method to recalculate the implicit bounds of this Node. By
     * default the bounds are a point sphere, so derived classes should
     * override this method with something better.
     */
    protected void recomputeBounds()
    {
        if(pixels != null)
            return;

        BoundingBox bbox = (BoundingBox)bounds;

        if(bounds == null)
        {
            bbox = new BoundingBox();
            bounds = bbox;
        }

        bbox.setMinimum(0, 0, 0);
        bbox.setMaximum(pixelWidth, pixelHeight, 0);
    }

    //----------------------------------------------------------
    // Methods defined by SceneGraphObject
    //----------------------------------------------------------

    /**
     * Notification that this object is live now. Overridden to make sure that
     * the live state of the nodes represents the same state as the parent
     * scene graph.
     *
     * @param state true if this should be marked as live now
     */
    protected void setLive(boolean state)
    {
        boolean old_state = alive;

        super.setLive(state);

        if(!old_state && state)
            recomputeBounds();
    }

    //---------------------------------------------------------------
    // Methods defined by Comparable
    //---------------------------------------------------------------

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param o The objec to be compared
     * @return -1, 0 or 1 depending on order
     * @throws ClassCastException The specified object's type prevents it from
     *    being compared to this Object
     */
    public int compareTo(Object o)
        throws ClassCastException
    {
        BitmapRaster app = (BitmapRaster)o;
        return compareTo(app);
    }

    //---------------------------------------------------------------
    // Methods defined by Object
    //---------------------------------------------------------------

    /**
     * Compare this object for equality to the given object.
     *
     * @param o The object to be compared
     * @return True if these represent the same values
     */
    public boolean equals(Object o)
    {
        if(!(o instanceof BitmapRaster))
            return false;
        else
            return equals((BitmapRaster)o);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the height of this bitmap.
     *
     * @return the height.
     */
    public int getHeight()
    {
        return pixelHeight;
    }

    /**
     * Get the width of this bitmap.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return pixelWidth;
    }

    /**
     * Set the pixel data for the bitmap contents. Each byte represents 8
     * pixels of the image. See class documentation for more information. A
     * null reference can be used to clear the bitmap and not have anything
     * appear on screen.
     *
     * @param bitmask The bits to use for the raster or null to clear
     * @throws IllegalArgumentException The number of bytes is not sufficient to
     *   fulfill the previously set width and height of the bitmap
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener data callback method
     */
    public void setBits(byte[] bitmask)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isDataWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(bitmask == null)
        {
            pixelBuffer.clear();
            return;
        }

        if(bitmask.length < pixelHeight * byteWidth)
            throw new IllegalArgumentException(BIT_ARRAY_SIZE_ERR);

        pixelBuffer.put(bitmask, 0, pixelHeight * byteWidth);
        pixelBuffer.rewind();
    }

    /**
     * Set the pixel data for the bitmap contents and change the size of the
     * raster at the same time. Each byte represents 8 pixels of the image.
     * See class documentation for more information. A null reference can be
     * used to clear the bitmap and not have anything appear on screen. Because
     * this is also changing the bounds of the image, it needs to be called
     * during the bounds callback, not the data callback like the other
     * setBits method.
     *
     * @param bitmask The bits to use for the raster or null to clear
     * @param width The width of the raster in pixels
     * @param height The height of the raster in pixels
     * @throws IllegalArgumentException The width or height are not positive
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setBits(byte[] bitmask, int width, int height)
        throws InvalidWriteTimingException, IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(width <= 0)
            throw new IllegalArgumentException(NEG_WIDTH_MSG + width);

        if(height <= 0)
            throw new IllegalArgumentException(NEG_HEIGHT_MSG + height);

        if(bitmask == null)
        {
            pixelBuffer.clear();
            return;
        }

        int b_width = width / 8;

        if((width % 8) != 0)
            b_width++;

        if(bitmask.length < height * b_width)
            throw new IllegalArgumentException(BIT_ARRAY_SIZE_ERR);

        byteWidth = b_width;
        pixelWidth = width;
        pixelHeight = height;

        pixelBuffer = ByteBuffer.allocateDirect(pixelHeight * byteWidth);
        pixelBuffer.put(bitmask, 0, pixelHeight * byteWidth);
        pixelBuffer.rewind();
    }

    /**
     * Retrieve the vertices that are currently set. The array must be at
     * least as long as the valid vertex count, times 3. If none are set
     * currently or have been cleared, the provided array is untouched.
     *
     * @param bitmask The array to copy the bit mask values into
     * @throws ArrayIndexOutOfBoundsException The provided array is too short
     */
    public void getBits(byte[] bitmask)
    {
        System.arraycopy(pixels, 0, bitmask, 0, pixelHeight * byteWidth);
    }

    /**
     * Set the origin offset of the bitmap to be used when rendering. The
     * origin allows an offset to be defined relative to the raster position.
     * Raster position is generated from the parent pixel transforms, and this
     * allows for bitmaps to be offset from that.
     *
     * @param x The X coordinate of the origin
     * @param y The Y coordinate of the origin
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the NodeUpdateListener bounds callback method
     */
    public void setOrigin(float x, float y)
        throws InvalidWriteTimingException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isBoundsWritePermitted(this))
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        origin[0] = x;
        origin[1] = y;
    }

    /**
     * Get the current origin of the bitmap. The default origin is (0,0).
     *
     * @param origin An array of length 2 to copy the values into
     */
    public void getOrigin(float[] origin)
    {
        origin[0] = this.origin[0];
        origin[1] = this.origin[1];
    }

    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * @param br The argument instance to be compared
     * @return -1, 0 or 1 depending on order
     */
    public int compareTo(BitmapRaster br)
    {
        if(br == null)
            return 1;

        if(br == this)
            return 0;

        return 0;
    }

    /**
     * Compares this object with the specified object to check for equivalence.
     *
     * @param br The shape instance to be compared
     * @return true if the objects represent identical values
     */
    public boolean equals(BitmapRaster br)
    {
        return (br == this);
    }
}
