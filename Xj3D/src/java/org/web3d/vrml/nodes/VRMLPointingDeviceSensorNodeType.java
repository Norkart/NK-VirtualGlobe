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
 * A sensor driven by a pointing device.
 * <p>
 * @author Alan Hudson
 * @version $Revision: 1.11 $
 */
public interface VRMLPointingDeviceSensorNodeType extends VRMLSensorNodeType {

    /**
     * Flag to notify the user whether the node implementation only needs the
     * hit point information, or it needs everything else as well. This is an
     * optimisation method that allows the internals of the event model to
     * avoid doing unnecessary work. If the return value is true, then the
     * hitNormal and hitTexCoord parameter values will not be supplied (they'll
     * be null references).
     *
     * @return true if the node implementation only requires hitPoint information
     */
    public boolean requiresPointOnly();

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
     * Set the flag describing whether the pointing device is over this sensor.
     * The result should be that isOver SFBool output only field is set
     * appropriately at the node level.
     *
     * @param newIsOver The new value for isOver
     */
    public void setIsOver(boolean newIsOver);

    /**
     * Get the current value of the isOver field.
     *
     * @return The current value of isOver
     */
    public boolean getIsOver();
}
