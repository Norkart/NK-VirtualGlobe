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
import javax.vecmath.Point3f;

// Local imports
import org.j3d.aviatrix3d.rendering.ViewEnvironmentCullable;

/**
 * Representation of the physical environment setup used to connect
 * a virtual Viewpoint object to the real one that is rendered on a drawable
 * surface.
 * </p>
 * <p>
 * Most of the properties of this class are changed during the app update
 * observer callback cycle.
 * </p>
 * <p>
 * <b>Frustum Generation</b>
 * </p>
 * <p>
 * The view frustum is generated depends on the aspect ratio. If the user
 * sets an explicit aspect ratio, this is used in preference. If no aspect
 * ratio is set (or the value set to <= 0) then the dimensions of the
 * viewport that this is contained within are used to automatically calculate
 * an aspect ratio.
 * </p>
 * <p>
 * The aspect ratio is not set by default.
 * </p>
 *
 * <b>Stereo support</b>
 * </p>
 * <p>
 * If the output device is capable of supporting stereo rendering, this class
 * can be used to enable it. Since the stereo flags are supplied here, that
 * allows the end user to control which layer(s) should be rendered in stereo
 * and which should not. For example, a HUD may want to have a text overlay
 * that has no depth applied, rendered over the top of the main layer, which
 * has stereo applied. One environment has the flag set, the other does not.
 * <p>
 * By default, stereo is disabled.
 * </p>
 *
 * <p>
 * <b>Field of View and aspect ratio</b>
 * </p>
 * <p>
 * The field of view calculation defines the viewing angle that is used
 * in the Y axis - definition the minimum and maximum Y extents for the view
 * frustum. If an explicit aspect ratio is set then the X extents are
 * calculated using that, otherwise the dimensions of the viewport are
 * used to calculate an aspect ratio, and finally the X extents.
 *
 * @author Justin Couch
 * @version $Revision: 1.22 $
 */
public class ViewEnvironment extends SceneGraphObject
    implements ViewEnvironmentCullable
{
    /** Message when the projection type provided is invalid */
    private static final String INVALID_PROJECTION_MSG =
        "Invalid projection type provided. Must be one of " +
        "ORTHOGRAPHIC_PROJECTION or PERSPECTIVE_PROJECTION";

    private static final String INVALID_FOV_MSG =
        "The field of view angle provided is invalid. Must be between 0 and 180";

    /** Invalid clip distance. It is negative */
    private static final String NEGATIVE_CLIP_MSG =
        "The clip distance provided is negative, which is not allowed for " +
        "perspective projection.";

    /** The far clip distance is less than the near clip distance */
    private static final String INVERT_CLIP_MSG =
        "The near clip plane is greater than the far clip plane";

    /** The projection type is perspective mode */
    public static final int PERSPECTIVE_PROJECTION =
        ViewEnvironmentCullable.PERSPECTIVE_PROJECTION;

    /** The projection type is perspective mode */
    public static final int ORTHOGRAPHIC_PROJECTION =
        ViewEnvironmentCullable.ORTHOGRAPHIC_PROJECTION;

    /** Index into the viewport size array for the X position */
    public static final int VIEW_X = ViewEnvironmentCullable.VIEW_X;

    /** Index into the viewport size array for the Y position */
    public static final int VIEW_Y = ViewEnvironmentCullable.VIEW_Y;

    /** Index into the viewport size array for the width */
    public static final int VIEW_WIDTH = ViewEnvironmentCullable.VIEW_WIDTH;

    /** Index into the viewport size array for the height */
    public static final int VIEW_HEIGHT = ViewEnvironmentCullable.VIEW_HEIGHT;

    /** The stereo setting for this environment */
    private boolean useStereo;

    /** The perspective/orthographic setting for this environment */
    private int projectionType;

    /** The field of view used */
    private double fov;

    /** Current near clip plane setting */
    private double nearClip;

    /** Current far clip plane setting */
    private double farClip;

    /** The aspectRatio ratio */
    private double aspectRatio;

    /** If defined, the explicit viewport to use */
    private int[] viewportSize;

    /** The currently calculated view frustum */
    private double[] viewFrustum;

    /** The ortho left plane coordinate */
    private double orthoLeft;

    /** The ortho right plane coordinate */
    private double orthoRight;

    /** The ortho bottom plane coordinate */
    private double orthoBottom;

    /** The ortho top plane coordinate */
    private double orthoTop;

    /**
     * Flag indicating that there have been some changes and the frustum needs
     * to be recalculated the next time it is asked for.
     */
    private boolean frustumChanged;

    /**
     * Create a default instance of this class with stereo false,
     * perspective projection and field of view set to 45 degrees.
     */
    ViewEnvironment()
    {
        useStereo = false;
        projectionType = PERSPECTIVE_PROJECTION;
        fov = 45;

        nearClip = 0.01;
        farClip = 1000;
        aspectRatio = 0;
        viewportSize = new int[4];
        viewFrustum = new double[6];
        frustumChanged = true;

        orthoLeft = -1;
        orthoRight = 1;
        orthoBottom = -1;
        orthoTop = 1;
    }

    //----------------------------------------------------------
    // Methods defined by ViewEnvironmentCullable
    //----------------------------------------------------------

    /**
     * Check to see if stereo has been enabled for this environment.
     *
     * @return true if stereo rendering is to be used
     */
    public boolean isStereoEnabled()
    {
        return useStereo;
    }

    /**
     * Check to see if stereo has been enabled for this environment.
     *
     * @return true if stereo rendering is to be used
     */
    public int getProjectionType()
    {
        return projectionType;
    }

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
    public int[] getViewportDimensions()
    {
        return viewportSize;
    }

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
    public void getViewFrustum(double[] frustum)
    {
        generateViewFrustum(frustum);
    }

    /**
     * Get the currently set field of view. The field of view is specified in
     * degrees.
     *
     * @return A value 0 <= x <= 180;
     */
    public double getFieldOfView()
    {
        return fov;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the stereo flag used for this environment.
     *
     * @param stereo True if stereo should be rendered
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setStereoEnabled(boolean stereo)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        useStereo = stereo;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set the perspective projection flag used for this environment.
     *
     * @param type One of ORTHOGRAPHIC_PROJECTION or PERSPECTIVE_PROJECTION
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     * @throws IllegalArgumentException The type is not valid
     */
    public void setProjectionType(int type)
        throws IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if(type < PERSPECTIVE_PROJECTION || type > ORTHOGRAPHIC_PROJECTION)
            throw new IllegalArgumentException(INVALID_PROJECTION_MSG);

        projectionType = type;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set the field of view to be used. The value supplied must be in degrees.
     *
     * @param angle The angle in degress
     * @throws IllegalArgumentException The angle is less than or equal to zero
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setFieldOfView(double angle)
        throws IllegalArgumentException
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if((angle <= 0) || (angle > 180))
            throw new IllegalArgumentException(INVALID_FOV_MSG);

        fov = angle;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set aspect ratio, which is the ratio of window Width / Height.
     * An aspect ratio of <= 0 will be calculated from the current
     * window or viewport dimensions.
     *
     * @param aspect The new aspectRatio ratio.
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setAspectRatio(double aspect)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        aspectRatio = aspect;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Get the currently set aspect ratio.  Width / Height.
     *
     * @return The aspect ratio
     */
    public double getAspectRatio()
    {
        return aspectRatio;
    }

    /**
     * Set the near clipping distance to be used by the application.
     *
     * @param d The distance to set the near clip plane to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setNearClipDistance(double d)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if((projectionType == PERSPECTIVE_PROJECTION) && d < 0)
            throw new IllegalArgumentException(NEGATIVE_CLIP_MSG);

        if(d >= farClip)
            throw new IllegalArgumentException(INVERT_CLIP_MSG);

        nearClip = d;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Get the current setting of the far clip plane.
     *
     * @return The current value, which is less than the far plane
     */
    public double getNearClipDistance()
    {
        return nearClip;
    }

    /**
     * Set the near clipping distance to be used by the application.
     *
     * @param near The distance to set the near clip plane
     * @param far The distance to the far clip plane
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setClipDistance(double near, double far)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if((projectionType == PERSPECTIVE_PROJECTION) && near < 0)
            throw new IllegalArgumentException(NEGATIVE_CLIP_MSG);

        if((projectionType == PERSPECTIVE_PROJECTION) && far < 0)
            throw new IllegalArgumentException(NEGATIVE_CLIP_MSG);

        if(near >= far)
            throw new IllegalArgumentException(INVERT_CLIP_MSG);

        nearClip = near;
        farClip = far;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Set the far clipping distance to be used by the application.
     *
     * @param d The distance to set the near clip plane to
     * @throws InvalidWriteTimingException An attempt was made to write outside
     *   of the ApplicationUpdateObserver callback method
     */
    public void setFarClipDistance(double d)
    {
        if(isLive() && updateHandler != null &&
           !updateHandler.isPickingPermitted())
            throw new InvalidWriteTimingException(WRITE_TIMING_MSG);

        if((projectionType == PERSPECTIVE_PROJECTION) && d < 0)
            throw new IllegalArgumentException(NEGATIVE_CLIP_MSG);

        if(d <= nearClip)
            throw new IllegalArgumentException(INVERT_CLIP_MSG);

        farClip = d;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Get the current setting of the far clip plane.
     *
     * @return The current value, which is greater than the near plane
     */
    public double getFarClipDistance()
    {
        return farClip;
    }

    /**
     * Set the Orthographic view parameters.
     *
     * @param left The left plane coordinate
     * @param right The right plane coordinate
     * @param bottom The bottom plane coordinate
     * @param top The top plane coordinate
     */
    public void setOrthoParams(double left,
                               double right,
                               double bottom,
                               double top)
    {
        orthoLeft = left;
        orthoRight = right;
        orthoBottom = bottom;
        orthoTop = top;

        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Generate a frustum based on the projectionType.  Perspective used
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
    public void generateViewFrustum(double[] frustum)
    {
        if(frustumChanged)
            recalcFrustum();

        frustum[0] = viewFrustum[0];
        frustum[1] = viewFrustum[1];
        frustum[2] = viewFrustum[2];
        frustum[3] = viewFrustum[3];
        frustum[4] = viewFrustum[4];
        frustum[5] = viewFrustum[5];
    }

    /**
     * Convert a pixel location to surface coordinates. This uses the current
     * view frustum calculation.
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param position The converted position.  It must be preallocated.
     */
    public void getPixelLocationInSurface(float x, float y, Point3f position)
    {
        getPixelLocationInSurface(x, y, null, position);
    }

    /**
     * Convert a pixel location to surface coordinates. This uses the current
     * view frustum calculation and an optional amount of eye offset (which
     * is particularly useful for stereo calculations).
     *
     * @param x The X coordinate
     * @param y The Y coordinate
     * @param offset Any Eye offset amount needed for a canvas offset or null
     * @param position The converted position.  It must be preallocated.
     */
    public void getPixelLocationInSurface(float x,
                                          float y,
                                          Point3f offset,
                                          Point3f position)
    {
        if(viewportSize[VIEW_WIDTH] == 0)
            return;

        if(frustumChanged)
            recalcFrustum();

        if(offset != null)
        {
            double vmx1 = viewFrustum[0] - offset.x;
            double vmx2 = viewFrustum[1] - offset.x;
            double vmy1 = viewFrustum[2] - offset.y;
            double vmy2 = viewFrustum[3] - offset.y;
            double vmd1 = viewFrustum[4] - offset.z;

            position.x = (float)((vmx2 - vmx1) *
                           (x / viewportSize[VIEW_WIDTH] - 0.5f));
            position.y = (float)((vmy2 - vmy1) *
                           ((viewportSize[VIEW_HEIGHT] - y) /
                            viewportSize[VIEW_HEIGHT] - 0.5f));
            position.z = (float) -vmd1;
        }
        else
        {
            position.x = (float)((viewFrustum[1] - viewFrustum[0]) *
                           (x / viewportSize[VIEW_WIDTH] - 0.5f));
            position.y = (float)((viewFrustum[3] - viewFrustum[2]) *
                           ((viewportSize[VIEW_HEIGHT] - y) /
                            viewportSize[VIEW_HEIGHT] - 0.5f));
            position.z = (float) -viewFrustum[4];
        }
    }

    /**
     * Set the viewport dimensions from the parent viewport. These dimensions
     * are pushed down through the scene to the viewport.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    void setViewportDimensions(int x, int y, int width, int height)
    {
        viewportSize[VIEW_X] = x;
        viewportSize[VIEW_Y] = y;
        viewportSize[VIEW_WIDTH] = width;
        viewportSize[VIEW_HEIGHT] = height;
        frustumChanged = true;

        if(isLive() && updateHandler != null)
            updateHandler.notifyUpdateRequired();
    }

    /**
     * Internal convenience method to recalculate the view frustum when
     * needed.
     */
    private void recalcFrustum()
    {
        if(projectionType == PERSPECTIVE_PROJECTION)
        {
            double xmin = 0;
            double xmax = 0;
            double ymin = 0;
            double ymax = 0;

            ymax = nearClip * Math.tan(fov * Math.PI / 360.0);
            ymin = -ymax;

            if(aspectRatio <= 0)
            {
                double ar = (double)viewportSize[VIEW_WIDTH] /
                            viewportSize[VIEW_HEIGHT];
                xmin = ymin * ar;
                xmax = ymax * ar;
            }
            else
            {
                xmin = ymin * aspectRatio;
                xmax = ymax * aspectRatio;
            }

            viewFrustum[0] = xmin;
            viewFrustum[1] = xmax;
            viewFrustum[2] = ymin;
            viewFrustum[3] = ymax;
            viewFrustum[4] = nearClip;
            viewFrustum[5] = farClip;

        }
        else
        {
            viewFrustum[0] = orthoLeft;
            viewFrustum[1] = orthoRight;
            viewFrustum[2] = orthoBottom;
            viewFrustum[3] = orthoTop;
            viewFrustum[4] = nearClip;
            viewFrustum[5] = farClip;
        }

        frustumChanged = false;
    }
}
