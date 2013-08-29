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

package org.j3d.aviatrix3d.rendering;

// External imports
// None

// Local imports
// None

/**
 * A cullable that represents a single visual composited layer.
 * <p>
 *
 * A layer has zero or more viewports to process.
 *
 * @author Justin Couch
 * @version $Revision: 2.2 $
 */
public interface ViewEnvironmentCullable extends Cullable
{
    /** The projection type is perspective mode */
    public static final int PERSPECTIVE_PROJECTION = 1;

    /** The projection type is perspective mode */
    public static final int ORTHOGRAPHIC_PROJECTION = 2;

    /** Index into the viewport size array for the X position */
    public static final int VIEW_X = 0;

    /** Index into the viewport size array for the Y position */
    public static final int VIEW_Y = 1;

    /** Index into the viewport size array for the width */
    public static final int VIEW_WIDTH = 2;

    /** Index into the viewport size array for the height */
    public static final int VIEW_HEIGHT = 3;

    /**
     * Check to see if stereo has been enabled for this environment.
     *
     * @return true if stereo rendering is to be used
     */
    public boolean isStereoEnabled();

    /**
     * Get the type of view environment defined.
     */
    public int getProjectionType();

    /**
     * Get the frustum based on the projectionType.  Perspective used
     * the current view setup of FoV, near and far clipping distances and
     * the aspectRatio ratio, generate the 6 parameters that describe a view
     * frustum. These parameters are what could be used as arguments to the
     * glFrustum call. The parameter order for perspective is:
     * [x min, x max, y min, y max, z near, z far]
     * Orthographic uses the parameters specified in the setOrthoParams.
     * The parameter order for orthographic:
     * [left, right, bottom, top, near, far]
     *
     * @param frustum An array at least 6 in length for the values generated
     */
    public void getViewFrustum(double[] frustum);

    /**
     * Get the currently set field of view. The field of view is specified in
     * degrees.
     *
     * @return A value 0 <= x <= 180;
     */
    public double getFieldOfView();

    /**
     * Get the currently set dimensions of the viewport. This is automatically
     * pushed down from the parent Viewport instance. The values are described
     * as<br/>
     * viewport[0] = x<br/>
     * viewport[1] = y<br/>
     * viewport[2] = width<br/>
     * viewport[3] = height<br/>
     *
     * @return The current viewport size
     */
    public int[] getViewportDimensions();
}
