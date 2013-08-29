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

package org.xj3d.ui.awt.device.j3d.elumens;

// External imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Local imports
import org.j3d.device.output.elumens.MouseCoordinateConverter;

import org.web3d.j3d.device.mouse.MouseDevice;
import org.xj3d.device.InputDevice;
import org.xj3d.device.Tracker;

public class ElumensMouseDevice implements InputDevice, MouseListener, MouseMotionListener {
    private ElumensMouseTracker[] trackers;
    private MouseCoordinateConverter mcc;

    public ElumensMouseDevice(MouseCoordinateConverter conv) {
        mcc = conv;
        trackers = new ElumensMouseTracker[1];

        trackers[0] = new ElumensMouseTracker(conv);
    }

    //------------------------------------------------------------------------
    // Methods for InputDevice interface
    //------------------------------------------------------------------------
    public Tracker[] getTrackers() {
        return trackers;
    }

    /**
     * Get a count of the number of trackers this device has.  This cannot
     * change during the life of a device.
     */
    public int getTrackerCount() {
        return 1;
    }

    //------------------------------------------------------------------------
    // Methods for MouseListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mousePressed(MouseEvent evt) {
        trackers[0].mousePressed(evt);
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseReleased(MouseEvent evt) {
        trackers[0].mouseReleased(evt);
    }

    /**
     * Process a mouse click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseClicked(MouseEvent evt) {
        trackers[0].mouseClicked(evt);
    }

    /**
     * Process a mouse enter event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseEntered(MouseEvent evt) {
        trackers[0].mouseEntered(evt);
    }

    /**
     * Process a mouse exited event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseExited(MouseEvent evt) {
        trackers[0].mouseExited(evt);
    }

    //------------------------------------------------------------------------
    // Methods for MouseMotionListener events
    //------------------------------------------------------------------------

    /**
     * Process a mouse drag event
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDragged(MouseEvent evt) {
        trackers[0].mouseDragged(evt);
    }

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMoved(MouseEvent evt) {
        trackers[0].mouseMoved(evt);
    }
}
