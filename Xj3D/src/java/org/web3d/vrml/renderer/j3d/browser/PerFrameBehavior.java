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

package org.web3d.vrml.renderer.j3d.browser;

// External imports
import java.util.Enumeration;

import javax.media.j3d.*;

import javax.vecmath.Point3d;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.EventModelStatusListener;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.xj3d.core.eventmodel.EventModelEvaluator;

/**
 * A class to handle all functions that need to be executed every frame.
 * <p>
 *
 * Another problem is dealing with Java time. On Win32 boxes the accuracy is
 * pretty fucked - 10ms at best on an NT/2K box. The result is that we get this
 * behaviour called every frame and we have no way of dealing with the fact that
 * time has not advanced at all according to the system clock. One way of doing
 * this is looking for the last time, checking the difference and then
 * incrementing by a very small amount (say 1ms). This should give accuracy up
 * to about 100Hz frame rate. How we go when it runs faster than that, I have
 * no idea. Jump up and down and bitch to Sun about it.
 * <p>
 *
 * In order for time to always run, we internally create a schedule bounds that
 * is infinite.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public class PerFrameBehavior extends Behavior {

    /** ID of the criteria to wake up with */
    private static final int START_TICK_ID = 101;

    /** The time increment when all else fails */
    private static final int TIME_FUDGE_FACTOR = 1;

    /** The conditions that we always use for time clicks */
    private WakeupOnElapsedTime tickCritter;

    /** Condition to wake up a sleeping behaviour */
    private WakeupOnBehaviorPost sleepCritter;

    /** Condition to wake up on the next frame */
    private WakeupOnElapsedFrames frameCritter;

    /** The event model handler */
    private EventModelEvaluator eventModel;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Flag to say whether this behaviour is currently enabled */
    private boolean enabled;

    /** The current wall clock time that we are sending to people */
    private long currentWallTime;

    /** The last clock tick. We don't bother sending if the diff is zero */
    private long lastWallTime;

    /** The BrowserCore holding this manager. */
    private BrowserCore browser;

    /** The Event model status listener. */
    private EventModelStatusListener eventModelStatusListener;

    /** The current total of timings */
    private long timingTotal;

    /** The current count */
    private int timingCount;

    /** The last time */
    private long timingLast;

    /**
     * Create a new instance of the behaviour that is used to drive the
     * given event model handler.
     *
     * @param eme The evaluator to use.
     * @throws NullPointerException The evaluator was null
     */
    public PerFrameBehavior(EventModelEvaluator eme, BrowserCore core) {
        if(eme == null)
            throw new NullPointerException("Evaluator was null");

        eventModel = eme;
        errorReporter = DefaultErrorReporter.getDefaultReporter();
        browser = core;

        tickCritter = new WakeupOnElapsedTime(25);
        frameCritter = new WakeupOnElapsedFrames(0);

        sleepCritter =
            new WakeupOnBehaviorPost(this, START_TICK_ID);

        BoundingSphere inf =
            new BoundingSphere(new Point3d(), Double.POSITIVE_INFINITY);
        setSchedulingBounds(inf);

        enabled = false;
    }

    /**
     * Set the eventModelStatus listener.
     *
     * @param l The listener.  Null will clear it.
     */
    public void setEventModelStatusListener(EventModelStatusListener l) {
        eventModelStatusListener = l;
    }

    //----------------------------------------------------------
    // Methods required by the Behavior
    //----------------------------------------------------------

    /**
     * Initialise the behaviour IAW the Java3D specs
     */
    public void initialize() {

        lastWallTime = System.currentTimeMillis();
        currentWallTime = System.currentTimeMillis();

        // sometimes we get the setEnable call before this call. This is
        // probably because we're in the loader code and the loader has
        // pre-constructed the scene before everything has been added to the
        // VRMLBranchGroup. Thus, we sleep waiting for a postID that happened
        // before we go here and never wake up. So, first check to see if
        // we're enabled already and then set the appropriate flag.

        if(enabled)
            wakeupOn(tickCritter);
        else
            wakeupOn(sleepCritter);
    }

    /**
     * Process the condition now.
     *
     * @param conditions List of conditions why this woke up
     */
    public void processStimulus(Enumeration conditions) {

        if (eventModelStatusListener != null)
            eventModelStatusListener.preEventEvaluation();

        currentWallTime = System.currentTimeMillis();

        timingTotal += (currentWallTime - timingLast);
        timingLast = currentWallTime;
        timingCount++;

        if (timingCount % 10 == 0) {
            browser.setLastRenderTime(timingTotal / 10);
            timingTotal = 0;
            timingCount = 0;
        }

        // This is to deal with the fucked Win32 system clock.
        if((currentWallTime - lastWallTime) <= 0)
            currentWallTime += TIME_FUDGE_FACTOR;

        WakeupCondition wc=null;
        int wait;

        eventModel.evaluate(currentWallTime);

        if(enabled)
            wc = frameCritter;
        else
            wc = sleepCritter;

        if (eventModelStatusListener != null)
            eventModelStatusListener.postEventEvaluation();

        wakeupOn(wc);
    }

    //----------------------------------------------------------
    // Public local methods
    //----------------------------------------------------------

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
     * Set the enabled state of this frame ticker. If the action is to start
     * running and it is not currently running, it will post it's own internal
     * ID to start running. After the state is set to false, it will generate
     * one more clock tick before sleeping.
     *
     * @param state true to start this ticker running, false to stop.
     */
    public void setEnable(boolean state) {
        super.setEnable(state);

        if(state && !enabled) {
            enabled = state;

            postId(START_TICK_ID);
        }
    }
}
