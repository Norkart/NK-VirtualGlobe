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

package org.xj3d.loaders.ogl;

// External imports
// None

// Local imports
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLTimeListener;

/**
 * Static representation of the System clock for time-zero loaders.
 * <p>
 *
 * The clock only updates the time when resetTimeZero() is called. Listeners
 * are ignored for this implementation as the timesensors will never get their
 * first tick.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class StaticClock implements VRMLClock {

    /** The "current" time in VRML values */
    private double currentTime;

    /** The "current" time in wall clock values */
    private long currentWallTime;


    StaticClock() {
        currentWallTime = System.currentTimeMillis();
        currentTime = currentWallTime * 0.001;
    }

    /**
     * Request the current time from the system clock. The time returned
     * is the VRML scene time which is in seconds.
     *
     * @return The current time in VRML coodinates
     */
    public double getTime() {
        return currentTime;
    }

    /**
     * Request the time in wall-clock coordinates. This is standard Unix
     * epoch time.
     *
     * @return The current wall-clock time
     */
    public long getWallTime() {
        return currentWallTime;
    }

    /**
     * Reset the clock to the current time as time zero. This can only be
     * called when the timer is not enabled. The last wall clock time is set
     * to now so that fraction information will be correctly oriented for
     * the next time it is enabled.
     */
    public void resetTimeZero() {
        currentWallTime = System.currentTimeMillis();

        // VRML time is in seconds. Yes, type conversion long->double
        currentTime = currentWallTime * 0.001;
    }

    /**
     * Add a time listener to this clock. Only one instance of each listener
     * will be registered and null references are ignored.
     *
     * @param l The listener instance to add
     */
    public void addTimeListener(VRMLTimeListener l) {
        // ignored.
    }

    /**
     * Remove a time listener to this clock. If the listener is not known to
     * this implementation, it is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void removeTimeListener(VRMLTimeListener l) {
        // ignored
    }
}
