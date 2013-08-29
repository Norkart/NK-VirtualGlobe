/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2004
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
// None

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
 * The thread that looks after the update issues of every frame.
 * <p>
 *
 * Even as this is a thread, it is not automatically started. The user is
 * required to start it separately.
 *
 * @author Justin Couch
 * @version $Revision: 1.24 $
 */
public abstract class PerFrameManager {

    /** Messge when the scene manager is null */
    private static final String NULL_EVENT_MSG =
        "The event model instance provided is null";

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** The event model handler */
    protected EventModelEvaluator eventModel;

    /** The current wall clock time that we are sending to people */
    protected long currentWallTime;

    /** The last clock tick. We don't bother sending if the diff is zero */
    protected long lastWallTime;

    /** The BrowserCore holding this manager. */
    protected BrowserCore browser;

    /** The Event model status listener. */
    private EventModelStatusListener eventModelStatusListener;

    /** The current total of timings */
    private long timingTotal;

    /** The current count */
    private int timingCount;

    /**
     * Construct a new manager for the given scene. The manager starts with
     * everything disabled.
     *
     * @param core The browser representation to send events to
     * @param eme The evaluator to use
     * @param timeZero The time to set as the initial zero time
     */
    public PerFrameManager(EventModelEvaluator eme,
                           BrowserCore core,
                           long timeZero)

        throws IllegalArgumentException {

        if(eme == null)
            throw new IllegalArgumentException(NULL_EVENT_MSG);

        eventModel = eme;
        browser = core;

        lastWallTime = timeZero;
        currentWallTime = lastWallTime;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Set the eventModelStatus listener.
     *
     * @param l The listener.  Null will clear it.
     */
    public void setEventModelStatusListener(EventModelStatusListener l) {
        eventModelStatusListener = l;
    }

    /**
     * Notification that now is the time to evaluate the next time stamp.
     * to be run.
     *
     * @param time The timestamp to use in milliseconds
     */
    public void clockTick(long time)
    {
        if(eventModelStatusListener != null)
            eventModelStatusListener.preEventEvaluation();

        // Run event cascade
        currentWallTime = time;

        timingTotal += (currentWallTime - lastWallTime);
        timingCount++;

        if(timingCount % 10 == 0) {
            browser.setLastRenderTime(timingTotal / 10);
            timingTotal = 0;
            timingCount = 0;
        }

        lastWallTime = currentWallTime;

        eventModel.evaluate(currentWallTime);

        if(eventModelStatusListener != null)
            eventModelStatusListener.postEventEvaluation();
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Shut down the frame thread so that the code may exit.
     */
    public void shutdown() {
    }
}
