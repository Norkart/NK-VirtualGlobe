/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

// Standard imports
// none

// Application specific imports
// none

/**
 * A listener to notify that a sensor's status has changed in relation to
 * an input device.  Any VRML/X3D node that interacts with a tracker will
 * issue these events.
 *
 * TODO: This interface does not allow you to seperate events when the user
 * has multiple picking devices like two gloves.  In the future we will
 * add parameters to determine which device was used.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface SensorStatusListener {
    /** The sensor is an Anchor */
    public static final int TYPE_ANCHOR = 0;

    /** The sensor is a TouchSensor */
    public static final int TYPE_TOUCH_SENSOR = 1;

    /** The sensor is a DragSensor  */
    public static final int TYPE_DRAG_SENSOR = 2;

    /**
     * Invoked when a sensor/anchor is in contact with a tracker capable of picking.
     *
     * @param type The sensor type
     * @param desc The sensor's description string
     */
    public void deviceOver(int type, String desc);

    /**
     * Invoked when a tracker leaves contact with a sensor.
     *
     * @param type The sensor type
     */
    public void deviceNotOver(int type);

    /**
     * Invoked when a tracker activates the sensor.  Anchors will not receive
     * this event, they get a linkActivated call.
     *
     * @param type The sensor type
     */
    public void deviceActivated(int type);

    /**
     * Invoked when a tracker follows a link.
     *
     * @param url The url to load.
     */
    public void linkActivated(String[] url);
}
