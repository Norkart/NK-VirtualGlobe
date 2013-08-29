/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
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

// External imports
// None

// Local imports
// None

/**
 * <p>
 * Nodes which can be activated by clicking with a pointing device sensor
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public interface VRMLTouchSensorNodeType
    extends VRMLPointingDeviceSensorNodeType {

    /**
     * Notify the node that a button was pushed down.
     *
     * @param button The button that was pressed
     * @param simTime The VRML simulation time it happened
     * @param hitPoint The location clicked in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyPressed(int button,
                              double simTime,
                              float[] hitPoint,
                              float[] hitNormal,
                              float[] hitTexCoord);

    /**
     * Notify the node that a button was released.
     *
     * @param button The button that was released
     * @param simTime The VRML simulation time it happened
     * @param hitPoint The location clicked in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyReleased(int button,
                               double simTime,
                               float[] hitPoint,
                               float[] hitNormal,
                               float[] hitTexCoord);

    /**
     * Notify the node that the device moved while pressed.
     *
     * @param hitPoint The current location in object space coordinates
     * @param hitNormal Surface normal vector at the intersection point
     * @param hitTexCoord The texture coordinate at the intersection point
     */
    public void notifyHitChanged(float[] hitPoint,
                                 float[] hitNormal,
                                 float[] hitTexCoord);
}
