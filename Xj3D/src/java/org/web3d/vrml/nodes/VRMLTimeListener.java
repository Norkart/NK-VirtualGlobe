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
 * Interface for System clock ticks so that we can drive things like
 * routes, TimeSensors etc.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public interface VRMLTimeListener {

    /**
     * Notify the listener that the time is now this value. The time is
     * wall-clock time, not VRML time to give low-level accuracy.
     *
     * @param time The current time
     */
    public void timeClick(long time);
}
