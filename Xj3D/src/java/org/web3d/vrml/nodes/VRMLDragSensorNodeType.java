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

// Standard imports
// None

// Application specific imports
// None

/**
 * A sensor that is driven by pointing device dragging.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.8 $
 */
public interface VRMLDragSensorNodeType extends VRMLPointingDeviceSensorNodeType {

    /**
     * Set a new value for the autoOffset field.
     *
     * @param state The new value for AutoOffset
     */
    public void setAutoOffset(boolean state);

    /**
     * Get the current state of the autoOffset field. The default value is
     * <code>true</code>.
     *
     * @return The current value of autoOffset
     */
    public boolean getAutoOffset();

    /**
     * Set a new value for the description field.
     *
     * @param desc The new value for the description field
     */
    public void setDescription(String desc);

    /**
     * Get the current state of the description field.
     *
     * @return The current value of description
     */
    public String getDescription();

    /**
     * Notification that this sensor has just been clicked on to start a drag
     * action.
     *
     * @param hitPoint Intersection between the sensor and the device geometry in
     *    local coordinate system origin
     * @param position Where the sensor origin is in local coordinates
     */
    public void notifySensorDragStart(float[] hitPoint, float[] position);

    /**
     * Notify the drag sensor that a sensor is currently dragging this device
     * and that it's position and orientation are as given.
     *
     * @param position Where the sensor origin is in local coordinates
     * @param direction Vector showing the direction the sensor is pointing
     */
    public void notifySensorDragChange(float[] position, float[] direction);

    /**
     * Notification that this sensor has finished a drag action.
     *
     * @param position Where the sensor origin is in local coordinates
     * @param direction Vector showing the direction the sensor is pointing
     */
    public void notifySensorDragEnd(float[] position, float[] direction);
}
