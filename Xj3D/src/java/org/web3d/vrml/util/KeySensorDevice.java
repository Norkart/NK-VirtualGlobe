/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.util;

// External imports - NONE
// Local imports - NONE

/**
 * Defines the requirements for retrieving an ordered set of key events.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public interface KeySensorDevice {
    /**
     * Return the ordered set of key events in the argument KeySequence
     * object that have occurred since the previous call to this method.
     *
     * @param seq - The KeySequence object to initialize with the set
     * of key events
     */
    public void getEvents( KeySequence seq );
}

