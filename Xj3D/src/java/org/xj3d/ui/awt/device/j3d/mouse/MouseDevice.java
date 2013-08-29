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

package org.xj3d.ui.awt.device.j3d.mouse;

// External imports
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// Local imports
import org.xj3d.device.Tracker;
import org.xj3d.device.TrackerDevice;

/**
 * A mouse device implementation.  This mouse is a typical picking and
 * navigation style mouse.
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class MouseDevice
    implements TrackerDevice,
               MouseListener,
               MouseMotionListener {

    /** The sensors for this device */
    private MouseTracker[] trackers;

    /** The name of this device */
    private String name;

    public MouseDevice(String name) {
        this.name = name;
        trackers = new MouseTracker[1];
        trackers[0] = new MouseTracker();
    }

    //------------------------------------------------------------------------
    // Methods for InputDevice interface
    //------------------------------------------------------------------------

    /**
     * Get the name of this device.  Names are of the form class-#.  Valid
     * classes are Gamepad, Joystick, Wheel, Midi, GenericHID.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    //------------------------------------------------------------------------
    // Methods for TrackerDevice interface
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
