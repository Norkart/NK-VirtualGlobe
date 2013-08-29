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

package org.web3d.vrml.renderer.mobile.browser;

// External imports
// none

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.xj3d.core.eventmodel.EventModelEvaluator;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * Manages the VRML event model.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.3 $
 */
public class PerFrameManager {

    /** The time increment when all else fails */
    private static final int TIME_FUDGE_FACTOR = 1;

    /** The amount of time (ms) to sleep between frames if nothing happening */
    private static final long INACTIVE_TIME = 500;

    /** Messge when the scene manager is null */
    private static final String NULL_SCENE_MSG =
        "The SGManager instance provided is null";

    /** Messge when the scene manager is null */
    private static final String NULL_EVENT_MSG =
        "The event model instance provided is null";

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The scene manager used to manage the paint */
    private SGManager sceneManager;

    /** The event model handler */
    private EventModelEvaluator eventModel;

    /** Flag for the activity state of the manager */
    private boolean enabled;

    /** Flag to indicate it's time to die */
    private boolean shutdown;

    /** The current wall clock time that we are sending to people */
    private long currentWallTime;

    /** The last clock tick. We don't bother sending if the diff is zero */
    private long lastWallTime;

    /**
     * Construct a new manager for the given scene. The manager starts with
     * everything disabled.
     *
     * @param sgm The scene manager to use
     * @param eme The evaluator to use.
     */
    public PerFrameManager(SGManager sgm, EventModelEvaluator eme)
        throws IllegalArgumentException {

        if(sgm == null)
            throw new IllegalArgumentException(NULL_SCENE_MSG);

        if(eme == null)
            throw new IllegalArgumentException(NULL_EVENT_MSG);

        sceneManager = sgm;
        eventModel = eme;

        lastWallTime = System.currentTimeMillis();
        currentWallTime = lastWallTime;

        enabled = false;
        shutdown = false;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Run method from the thread.
     */
    public void simTick() {
        if (!enabled)
            return;

        currentWallTime = System.currentTimeMillis();

        // This is to deal with the fucked Win32 system clock.
        if((currentWallTime - lastWallTime) <= 0)
            currentWallTime += TIME_FUDGE_FACTOR;

        lastWallTime = currentWallTime;

        eventModel.evaluate(currentWallTime);
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
     * Marker to say that the manager should be running a real scene or
     * should just idly cycle away.
     *
     * @param state Whether to be enabled or not
     */
    public void setEnable(boolean state) {
        enabled = state;
    }
}
