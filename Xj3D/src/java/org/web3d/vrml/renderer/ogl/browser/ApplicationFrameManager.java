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
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.EventModelStatusListener;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLTransformNodeType;

import org.xj3d.core.eventmodel.EventModelEvaluator;

/**
 * Per-frame manager that uses the Aviatrix3D ApplicationUpdateObserver
 * to clock the scene graph time with.
 * <p>
 *
 * Even as this is a thread, it is not automatically started. The user is
 * required to start it separately.
 *
 * @author Justin Couch
 * @version $Revision: 1.17 $
 */
public class ApplicationFrameManager extends PerFrameManager
    implements ApplicationUpdateObserver {

    /** The time increment when all else fails */
    private static final int TIME_FUDGE_FACTOR = 1;

    /** Viewport resize management */
    private ViewportResizeManager viewportResizeManager;

    /** Viewport resize management */
    private ViewpointResizeManager viewpointResizeManager;

    /**
     * Construct a new manager for the given scene. The manager starts with
     * everything disabled.
     *
     * @param eme The event model evaluator to use.
     * @param core The internal representation of the browser
     * @param viewportResizer Manager for viewport resize handling
     * @param viewpointResizer Manager for viewport resize handling
     */
    public ApplicationFrameManager(EventModelEvaluator eme,
                                   BrowserCore core,
                                   ViewportResizeManager viewportResizer,
                                   ViewpointResizeManager viewpointResizer)
        throws IllegalArgumentException {

        super(eme, core, System.currentTimeMillis());

        viewportResizeManager = viewportResizer;
        viewpointResizeManager = viewpointResizer;
    }

    //---------------------------------------------------------------
    // Methods defined by ApplicationUpdateObserver
    //---------------------------------------------------------------

    /**
     * Notification that now is a good time to update the scene graph.
     */
    public void updateSceneGraph() {
        long startTime = System.nanoTime();

        viewportResizeManager.sendResizeUpdates();
        viewpointResizeManager.sendResizeUpdates();

        browser.syncUIUpdates();

        long cur_time = System.currentTimeMillis();
        if((cur_time - lastWallTime) <= 0)
            cur_time += TIME_FUDGE_FACTOR;

        clockTick(cur_time);

        long appTime = (System.nanoTime() - startTime) / 1000000;
    }

    /**
     * Notification that the AV3D internal shutdown handler has detected a
     * system-wide shutdown. The aviatrix code has already terminated rendering
     * at the point this method is called, only the user's system code needs to
     * terminate before exiting here.
     */
    public void appShutdown() {
        // No idea what to do with this yet.
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------
}
