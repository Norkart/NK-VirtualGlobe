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
 * A node which generates events based on sensors
 * @author Alan Hudson
 * @version $Revision: 1.6 $
 */

public interface VRMLSensorNodeType extends VRMLChildNodeType {
    /**
     * Accessor method to set a new value for field attribute <b>enabled</b>
     *
     * @param newEnabled The new enabled value
     */
    public void setEnabled(boolean newEnabled);

    /**
     * Accessor method to get current value of field <b>enabled</b>,
     * default value is <code>true</code>.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled();

    /**
     * Get the current value of the isActive field
     *
     * @return True if currently active, false otherwise
     */
    public boolean getIsActive();
}
