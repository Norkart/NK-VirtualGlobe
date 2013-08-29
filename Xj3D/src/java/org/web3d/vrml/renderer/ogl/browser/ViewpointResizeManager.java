/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.browser;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

import org.j3d.aviatrix3d.ViewEnvironment;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

// Local imports
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;
import org.web3d.vrml.nodes.ViewpointListener;

/**
 * Convenience class for managing the resizing of Viewpoints based on
 * listener feedback from the surface.  This handles aspect ratio changes.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class ViewpointResizeManager
    implements GraphicsResizeListener, ViewpointListener {

    /** Const to convert radians to degrees */
    private static final float DEG_TO_RAD = (float) (180 / Math.PI);

    /** The list of fullscreen viewports to manage. */
    private ArrayList perspectiveViewpoints;

    /** The list of fixed size viewports to manage */
    private ArrayList orthoViewpoints;

    /** The mapping of a node to its ViewportData type. */
    private HashMap nodeDataMap;

    /** Flag saying that an update is needed */
    private boolean updateRequired;

    /** The lower left X position of the viewport, in pixels */
    private int viewX;

    /** The lower left Y position of the viewport, in pixels */
    private int viewY;

    /** The width of the viewport in pixels */
    private int viewWidth;

    /** The height of the viewport in pixels */
    private int viewHeight;

    /** The current aspect ratio */
    private float aspectRatio;

    /** Have we got any valid size set yet. */
    private boolean validSizeSet;

    /**
     * Does the device require a field of view. 0 = no, otherwise use this
     * instead of content
     */
    private float hardwareFOV;

    /**
     * Create a new instance of this manager now.
     */
    ViewpointResizeManager() {
        perspectiveViewpoints = new ArrayList();
        orthoViewpoints = new ArrayList();

        nodeDataMap = new HashMap();

        updateRequired = false;
        validSizeSet = false;
    }

    //---------------------------------------------------------------
    // Methods defined by GraphicsResizeListener
    //---------------------------------------------------------------

    /**
     * Notification that the graphics output device has changed dimensions to
     * the given size. Dimensions are in pixels.
     *
     * @param x The lower left x coordinate for the view
     * @param y The lower left y coordinate for the view
     * @param width The width of the viewport in pixels
     * @param height The height of the viewport in pixels
     */
    public void graphicsDeviceResized(int x, int y, int width, int height) {
        viewX = x;
        viewY = y;
        viewWidth = width;
        viewHeight = height;

        aspectRatio = (float) width / height;

        updateRequired = true;
        validSizeSet = true;
    }

    //---------------------------------------------------------------
    // Methods defined by ViewpointListener
    //---------------------------------------------------------------

    /**
     * The center of rotation has changed.
     *
     * @param val The new value
     */
    public void centerOfRotationChanged(float[] val) {
    }

    /**
     * The field of view has changed.
     *
     * @param val The new value
     */
    public void fieldOfViewChanged(float[] val) {
        // just force a change on all layers

        updateRequired = true;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Override the file field of view values with a value that suits
     * the given output device. A value of 0 = no, otherwise use this
     * instead of content
     *
     * @param fov The fov in degrees.
     */
    protected void setHardwareFOV(float fov) {
        hardwareFOV = fov;

        updateRequired = true;
    }

    /**
     * Clock the updates that should be sent now. This should only be called
     * during the application update cycle callbacks. Ideally this should be
     * called as the first thing in updateSceneGraph() method so that anything
     * else that relies on view frustum projection will have the latest
     * information.
     */
    void sendResizeUpdates() {
        if(!updateRequired)
            return;

        for(int i = 0; i < perspectiveViewpoints.size(); i++) {
            processPerspectiveViewpointChange(
               (ViewpointData) perspectiveViewpoints.get(i));
        }

        for(int i = 0; i < orthoViewpoints.size(); i++) {
            processOrthoViewpointChange(
               (ViewpointData) orthoViewpoints.get(i));
        }

        updateRequired = false;
    }

    /**
     * Add a viewpoint to be managed. Duplicate requests are ignored, as are
     * null values.
     *
     * @param node The X3D viewpoint node
     * @param view The Aviatrix3D ViewEnvironment
     */
    void addViewpoint(VRMLViewpointNodeType node, ViewEnvironment view) {
        if((view == null) || nodeDataMap.containsKey(view))
            return;

        ViewpointData data = new ViewpointData();
        data.viewpoint = node;
        data.viewEnvironment = view;

        nodeDataMap.put(node, data);

        int type = node.getProjectionType();
        if (type == VRMLViewpointNodeType.PROJECTION_PERSPECTIVE) {
            perspectiveViewpoints.add(data);

            if(validSizeSet)
                processPerspectiveViewpointChange(data);
        } else if (type == VRMLViewpointNodeType.PROJECTION_ORTHO) {
            orthoViewpoints.add(data);

            if(validSizeSet)
                processOrthoViewpointChange(data);
        } else {
            System.out.println("Unknown projection type in ViewpointResizeManager");
        }

        node.addViewpointListener(this);
    }

    /**
     * Remove a viewport that is being managed. If the viewport is not
     * currently registered, the request is silently ignored.
     *
     * @param node The viewpoint instance to remove
     */
    void removeViewpoint(VRMLViewpointNodeType node) {

        if (node == null)
            return;

        ViewpointData data = (ViewpointData) nodeDataMap.get(node);
        nodeDataMap.remove(node);

        int type = node.getProjectionType();
        if (type == VRMLViewpointNodeType.PROJECTION_PERSPECTIVE) {
            perspectiveViewpoints.remove(data);
        } else if (type == VRMLViewpointNodeType.PROJECTION_ORTHO) {
            orthoViewpoints.remove(data);
        } else {
            System.out.println("Unknown projection type in ViewpointResizeManager");
        }

        node.removeViewpointListener(this);
    }

    /**
     * Clear all of the current fullscreenViewports from the manager. Typically used when
     * you want to completely change the scene.
     */
    void clear() {
        perspectiveViewpoints.clear();
        orthoViewpoints.clear();
        nodeDataMap.clear();
    }

    /**
     * Process a single perspective viewpoint reference.
     *
     * @param data The data holder that this viewport is in
     */
    private void processPerspectiveViewpointChange(ViewpointData data) {
        // Handle dynamic changes to fieldOfView here
        // Handle spec language for correct aspectRatio handling

        float fieldOfView;

        if(hardwareFOV != 0)
            fieldOfView = hardwareFOV;
        else {
            fieldOfView = data.viewpoint.getFieldOfView()[0] * DEG_TO_RAD;
        }

        data.viewEnvironment.setFieldOfView(fieldOfView);
    }

    /**
     * Process a single ortho viewpoint reference.
     *
     * @param data The data holder that this viewport is in
     */
    private void processOrthoViewpointChange(ViewpointData data) {
        // Handle changes to fieldOfView
        boolean sameX = false;
        boolean sameY = false;

        float[] fieldOfView = data.viewpoint.getFieldOfView();
        double left = -1;
        double right = -1;
        double bottom = 1;
        double top = 1;
        double ratio;

        if (Math.abs(fieldOfView[0] - fieldOfView[2]) < 0.001) {
            sameX = true;
        }
        if (Math.abs(fieldOfView[1] - fieldOfView[3]) < 0.001) {
            sameY = true;
        }

        if (!sameX && !sameY) {
            left = fieldOfView[0];
            right =  fieldOfView[2];
            bottom = fieldOfView[1];
            top =  fieldOfView[3];
        } else if (sameX && sameY) {
            // find smallest dimension, make it 1
            // the other dimension big / small ratio

            if (viewWidth > viewHeight) {
                bottom = -1;
                top = 1;
                ratio = (double) viewWidth / viewHeight;
                left = -ratio;
                right = ratio;

                //System.out.println("Width left: " + left + " r: " + right + " bottom: " + bottom + " top: " + top);
            } else {
                left = -1;
                right = 1;
                ratio = (double) viewHeight / viewWidth;
                bottom = -ratio;
                top = ratio;

                //System.out.println("Height left: " + left + " r: " + right + " bottom: " + bottom + " top: " + top);
            }
        } else if (sameX) {
            bottom = -1;
            top = 1;
            ratio = (double) viewWidth / viewHeight;
            left = -ratio;
            right = ratio;

            //System.out.println("SameX left: " + left + " r: " + right + " bottom: " + bottom + " top: " + top);
        } else if (sameY) {
            left = -1;
            right = 1;
            ratio = (double) viewHeight / viewWidth;
            bottom = -ratio;
            top = ratio;

            //System.out.println("SameY left: " + left + " r: " + right + " bottom: " + bottom + " top: " + top);
        }

        data.viewEnvironment.setOrthoParams(left, right, bottom, top);
    }
}
