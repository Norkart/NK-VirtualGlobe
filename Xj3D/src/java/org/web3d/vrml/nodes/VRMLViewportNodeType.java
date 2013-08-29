/*****************************************************************************
 *                      Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

/**
 * Base representation of a viewpoint node in the scene.
 * <p>
 *
 * A viewpoint provides a place to examine the contents of the virtual world.
 * Depending on the world type, the way of specifying a viewpoint's location
 * and orientation may change. This base interface describes all of the common
 * requirements for a viewpoint.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.2 $
 */
public interface VRMLViewportNodeType extends VRMLNodeType {

    /** A viewport that is currently undefined due to externproto resolution. */
    public static final int VIEWPORT_UNDEFINED = 0;

    /** A viewport that takes up the full window space */
    public static final int VIEWPORT_FULLWINDOW = 1;

    /** A viewport that takes up a percentage of the window */
    public static final int VIEWPORT_PROPORTIONAL = 2;

    /** A viewport that takes up a fixed pixel size */
    public static final int VIEWPORT_FIXED = 3;

    /** A viewport that allows customisable fixed/proporational sizing */
    public static final int VIEWPORT_CUSTOM = 4;

    /**
     * Get the type of viewport layout policy that this node represents.
     * This determines how the viewport is managed by the system during window
     * resizes etc. It is a fixed value that never changes for the node
     * implementation.
     *
     * @return One of the VIEWPORT_* constant values
     */
    public int getViewportType();

    /**
     * Query whether the viewport X component uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedX();

    /**
     * Query whether the viewport Y component uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedY();

    /**
     * Query whether the viewport width uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedWidth();

    /**
     * Query whether the viewport height uses fixed (pixel) addressing
     * or is proportional to the screen size.
     *
     * @return True if fixed addressing is to be used
     */
    public boolean isFixedHeight();

    /**
     * Get the starting X coordinate of the viewport. Whether this is treated
     * as a pixel or percentage depends on the type of viewport node this node
     * is. If it is a pixel value, it will always be an integer value.
     *
     * @return The X coordinate to start the viewport at
     */
    public float getViewX();

    /**
     * Get the starting Y coordinate of the viewport. Whether this is treated
     * as a pixel or percentage depends on the type of viewport node this node
     * is. If it is a pixel value, it will always be an integer value.
     *
     * @return The Y coordinate to start the viewport at
     */
    public float getViewY();

    /**
     * Get the width of the viewport. Whether this is treated as a
     * pixel or percentage depends on the type of viewport node this node is.
     * If it is a pixel value, it will always be an integer value.
     *
     * @return The width of the viewport
     */
    public float getViewWidth();

    /**
     * Get the height of the viewport. Whether this is treated as a
     * pixel or percentage depends on the type of viewport node this node is.
     * If it is a pixel value, it will always be an integer value.
     *
     * @return The height of the viewport
     */
    public float getViewHeight();
}
