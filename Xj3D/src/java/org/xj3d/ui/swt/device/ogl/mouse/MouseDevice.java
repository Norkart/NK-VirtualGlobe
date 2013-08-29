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

package org.xj3d.ui.swt.device.ogl.mouse;

// External imports
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.j3d.aviatrix3d.pipeline.graphics.GraphicsOutputDevice;

// Local imports
import org.xj3d.device.Tracker;
import org.xj3d.device.TrackerDevice;

/**
 * A mouse device implementation.  This mouse is a typical picking and
 * navigation style mouse.
 *
 * @author Rex Melton
 * @version $Revision: 1.4 $
 */
public class MouseDevice
    implements TrackerDevice,
               MouseListener,
               MouseMoveListener,
               Listener {

    /** The sensors for this device */
    private MouseTracker[] trackers;

    /** The name of this device */
    private String name;

    /** The drawable surface */
    private GraphicsOutputDevice surface;

    /**
     * Construct a new mouse device that interacts with the given surface,
     * and is named.
     *
     * @param surface The surface to track
     * @param name The device name string
     */
    public MouseDevice(GraphicsOutputDevice surface, String name) {
        this.surface = surface;
        this.name = name;

        trackers = new MouseTracker[1];
        trackers[0] = new MouseTracker(surface, name + "-Tracker-0");
    }

    //------------------------------------------------------------------------
    // Methods defined by InputDevice
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
    // Methods defined by MouseListener
    //------------------------------------------------------------------------

    /**
     * Process a mouse press event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDown(MouseEvent evt) {
        trackers[0].mouseDown(evt);
    }

    /**
     * Process a mouse release event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseUp(MouseEvent evt) {
        trackers[0].mouseUp(evt);
    }

    /**
     * Process a mouse double click event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseDoubleClick(MouseEvent evt) {
        trackers[0].mouseDoubleClick(evt);
    }

    //------------------------------------------------------------------------
    // Methods defined by MouseMoveListener
    //------------------------------------------------------------------------

    /**
     * Process a mouse movement event.
     *
     * @param evt The event that caused this method to be called
     */
    public void mouseMove(MouseEvent evt) {
        trackers[0].mouseMove(evt);
    }

    //------------------------------------------------------------------------
    // Method defined by Listener
    //------------------------------------------------------------------------

    /**
     * Process the mouse wheel event
     *
     * @param event The event that caused this method to be called
     */
    public void handleEvent(Event event) {
        trackers[0].handleEvent(event);
    }
}
