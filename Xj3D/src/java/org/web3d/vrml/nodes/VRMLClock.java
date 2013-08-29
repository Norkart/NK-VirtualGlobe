/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

/**
 * Abstract representation of the System clock for various informational
 * purposes.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface VRMLClock {

    /**
     * Request the current time from the system clock. The time returned
     * is the VRML scene time which is in seconds.
     *
     * @return The current time in VRML coodinates
     */
    public double getTime();

    /**
     * Request the time in wall-clock coordinates. This is standard Unix
     * epoch time.
     *
     * @return The current wall-clock time
     */
    public long getWallTime();

    /**
     * Reset the clock to the current time as time zero. This can only be
     * called when the timer is not enabled. The last wall clock time is set
     * to now so that fraction information will be correctly oriented for
     * the next time it is enabled.
     */
    public void resetTimeZero();

    /**
     * Add a time listener to this clock. Only one instance of each listener
     * will be registered and null references are ignored.
     *
     * @param l The listener instance to add
     */
    public void addTimeListener(VRMLTimeListener l);

    /**
     * Remove a time listener to this clock. If the listener is not known to
     * this implementation, it is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void removeTimeListener(VRMLTimeListener l);
}
