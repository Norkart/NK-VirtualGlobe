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

import org.j3d.aviatrix3d.Viewport;
import org.j3d.aviatrix3d.pipeline.graphics.GraphicsResizeListener;

// Local imports
import org.web3d.vrml.nodes.VRMLViewportNodeType;

/**
 * Convenience class for managing the resizing of the fullscreenViewports based on
 * listener feedback from the surface.
 * <p>
 *
 * This class deals deals with each of the different viewport node types and
 * manages them appropriately. Note that the coordinate system defined by Xj3D
 * and by Aviatrix3D are different, so even fixed pixel-based fullscreenViewports need to
 * be updated each time the screen resizes.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class ViewportResizeManager
    implements GraphicsResizeListener {
    /** The list of fullscreen viewports to manage. */
    private ArrayList fullscreenViewports;

    /** The list of fixed size viewports to manage */
    private ArrayList fixedViewports;

    /** The list of custom size viewports to manage */
    private ArrayList customViewports;

    /** The list of proportional viewports to manage */
    private ArrayList proportionalViewports;

    /** The mapping of a view to its ViewportData type. */
    private HashMap viewDataMap;

    /** Flag saying that resizes have been recieved and not yet processed */
    private boolean newSizeSet;

    /** The lower left X position of the viewport, in pixels */
    private int viewX;

    /** The lower left Y position of the viewport, in pixels */
    private int viewY;

    /** The X coordinate of center of the  of the viewport, in pixels */
    private int viewCenterX;

    /** The Y coordinate of center of the  of the viewport, in pixels */
    private int viewCenterY;

    /** The width of the viewport in pixels */
    private int viewWidth;

    /** The height of the viewport in pixels */
    private int viewHeight;

    /** Have we got any valid size set yet. */
    private boolean validSizeSet;

    /**
     * Create a new instance of this manager now.
     */
    ViewportResizeManager() {
        customViewports = new ArrayList();
        fixedViewports = new ArrayList();
        fullscreenViewports = new ArrayList();
        proportionalViewports = new ArrayList();

        viewDataMap = new HashMap();

        newSizeSet = false;
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

        viewCenterX = (x + width) >> 1;
        viewCenterY = (y + height) >> 1;

        newSizeSet = true;
        validSizeSet = true;
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Clock the updates that should be sent now. This should only be called
     * during the application update cycle callbacks. Ideally this should be
     * called as the first thing in updateSceneGraph() method so that anything
     * else that relies on view frustum projection will have the latest
     * information.
     */
    void sendResizeUpdates() {
        if(!newSizeSet)
            return;

        for(int i = 0; i < fullscreenViewports.size(); i++) {
            Viewport vp = (Viewport)fullscreenViewports.get(i);
            vp.setDimensions(viewX, viewY, viewWidth, viewHeight);
        }

        for(int i = 0; i < fixedViewports.size(); i++) {
            FixedViewportData data = (FixedViewportData)fixedViewports.get(i);
            processViewportChange(data);
        }

        for(int i = 0; i < proportionalViewports.size(); i++) {
            ProportionalViewportData data =
                (ProportionalViewportData)proportionalViewports.get(i);
            processViewportChange(data);
        }

        for(int i = 0; i < customViewports.size(); i++) {
            CustomViewportData data =
                (CustomViewportData)customViewports.get(i);
            processViewportChange(data);
        }

        newSizeSet = false;
    }

    /**
     * Add a viewport to be managed. Duplicate requests are ignored, as are
     * null values.
     *
     * @param view The viewport instance to use
     */
    void addFullscreenViewport(Viewport view) {
        if((view == null) || fullscreenViewports.contains(view))
            return;

        fullscreenViewports.add(view);

        if(validSizeSet)
            view.setDimensions(viewX, viewY, viewWidth, viewHeight);
    }

    /**
     * Remove a viewport that is being managed. If the viewport is not
     * currently registered, the request is silently ignored.
     *
     * @param view The viewport instance to remove
     */
    void removeFullscreenViewport(Viewport view) {
        if(view == null)
            return;

        fullscreenViewports.remove(view);
    }

    /**
     * Add a viewport to be managed. Duplicate requests are ignored, as are
     * null values.
     *
     * @param view The viewport instance to use
     */
    void addFixedViewport(VRMLViewportNodeType node, Viewport view) {

        if((view == null) || viewDataMap.containsKey(view))
            return;

        FixedViewportData data = new FixedViewportData();
        data.viewport = view;
        data.viewX = (int)node.getViewX();
        data.viewY = (int)node.getViewY();
        data.viewWidth = (int)node.getViewWidth();
        data.viewHeight = (int)node.getViewHeight();

        fixedViewports.add(data);
        viewDataMap.put(view, data);

        if(validSizeSet)
            processViewportChange(data);
    }

    /**
     * Remove a viewport that is being managed. If the viewport is not
     * currently registered, the request is silently ignored.
     *
     * @param view The viewport instance to remove
     */
    void removeFixedViewport(VRMLViewportNodeType node, Viewport view) {
        if(view == null)
            return;

        Object data = viewDataMap.remove(view);
        fixedViewports.remove(data);
    }

    /**
     * Add a viewport to be managed. Duplicate requests are ignored, as are
     * null values.
     *
     * @param view The viewport instance to use
     */
    void addProportionalViewport(VRMLViewportNodeType node, Viewport view) {

        if((view == null) || viewDataMap.containsKey(view))
            return;

        ProportionalViewportData data = new ProportionalViewportData();
        data.viewport = view;
        data.viewX = node.getViewX();
        data.viewY = node.getViewY();
        data.viewWidth = node.getViewWidth();
        data.viewHeight = node.getViewHeight();

        proportionalViewports.add(data);
        viewDataMap.put(view, data);

        if(validSizeSet)
            processViewportChange(data);
    }

    /**
     * Remove a viewport that is being managed. If the viewport is not
     * currently registered, the request is silently ignored.
     *
     * @param view The viewport instance to remove
     */
    void removeProportionalViewport(VRMLViewportNodeType node, Viewport view) {
        if(view == null)
            return;

        Object data = viewDataMap.remove(view);
        proportionalViewports.remove(data);
    }

    /**
     * Add a viewport to be managed. Duplicate requests are ignored, as are
     * null values.
     *
     * @param view The viewport instance to use
     */
    void addCustomViewport(VRMLViewportNodeType node, Viewport view) {

        if((view == null) || viewDataMap.containsKey(view))
            return;

        CustomViewportData data = new CustomViewportData();
        data.viewport = view;
        data.fixedX = node.isFixedX();
        data.fixedY = node.isFixedY();
        data.fixedWidth = node.isFixedWidth();
        data.fixedHeight = node.isFixedHeight();
        data.viewX = node.getViewX();
        data.viewY = node.getViewY();
        data.viewWidth = node.getViewWidth();
        data.viewHeight = node.getViewHeight();

        customViewports.add(data);
        viewDataMap.put(view, data);

        if(validSizeSet)
            processViewportChange(data);
    }

    /**
     * Remove a viewport that is being managed. If the viewport is not
     * currently registered, the request is silently ignored.
     *
     * @param view The viewport instance to remove
     */
    void removeCustomViewport(VRMLViewportNodeType node, Viewport view) {
        if(view == null)
            return;

        Object data = viewDataMap.remove(view);
        customViewports.remove(data);
    }

    /**
     * Clear all of the current fullscreenViewports from the manager. Typically used when
     * you want to completely change the scene.
     */
    void clear() {
        customViewports.clear();
        fixedViewports.clear();
        fullscreenViewports.clear();
        proportionalViewports.clear();
        viewDataMap.clear();
    }

    /**
     * Process a single proportional viewport reference.
     *
     * @param data The data holder that this viewport is in
     */
    private void processViewportChange(ProportionalViewportData data) {
        int x = (int)(viewCenterX + data.viewX * viewWidth);
        int y = (int)(viewCenterY + data.viewY * viewHeight);
        int width = (int)(data.viewWidth * viewWidth);
        int height = (int)(data.viewHeight * viewHeight);

        if(width < 0) {
            x += width;
            width = -width;
        }

        if(height < 0) {
            y += height;
            height = -height;
        }

        data.viewport.setDimensions(x, y, width, height);
    }

    /**
     * Process a single fixed viewport reference.
     *
     * @param data The data holder that this viewport is in
     */
    private void processViewportChange(FixedViewportData data) {
        int x = viewCenterX + data.viewX;
        int y = viewCenterY + data.viewY;

        int width = (int)data.viewWidth;
        int height = (int)data.viewHeight;

        if(width < 0) {
            x += width;
            width = -width;
        }

        if(height < 0) {
            y += height;
            height = -height;
        }

        data.viewport.setDimensions(x, y, width, height);
    }

    /**
     * Process a single custom viewport reference.
     *
     * @param data The data holder that this viewport is in
     */
    private void processViewportChange(CustomViewportData data) {
        int x = data.fixedX ?
            (int)(viewCenterX + data.viewX) :
            (int)(viewCenterX + data.viewX * viewWidth);

        int y = data.fixedY ?
            (int)(viewCenterY + data.viewY) :
            (int)(viewCenterY + data.viewY * viewHeight);

        int width = data.fixedWidth ?
            (int)data.viewWidth :
            (int)(data.viewWidth * viewWidth);

        int height = data.fixedHeight ?
            (int)data.viewHeight :
            (int)(data.viewHeight * viewHeight);

        if(width < 0) {
            x += width;
            width = -width;
        }

        if(height < 0) {
            y += height;
            height = -height;
        }

        data.viewport.setDimensions(x, y, width, height);
    }
}
