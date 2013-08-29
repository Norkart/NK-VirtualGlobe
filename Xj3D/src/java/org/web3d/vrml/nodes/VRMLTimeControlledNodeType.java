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
 * Nodes which can be controlled by a set of VCR-like commands.
 * <p>
 *
 * Time information is provided by the clock that can be set by the
 * implementing runtime system. With the clock, the node can then register
 * clock ticks listeners for timing update information.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public interface VRMLTimeControlledNodeType extends VRMLTimeDependentNodeType {

    /**
     * Accessor method to set a new value for field attribute loop.
     *
     * @param newLoop Whether this field loops or not
     */
    public void setLoop(boolean newLoop);

    /**
     * Accessor method to get current value of field loop.
     * default value is <code>false</code>
     *
     * @return The value of the loop field
     */
    public boolean getLoop();

    /**
     * Accessor method to set a new value for field attribute startTime.
     *
     * @param newStartTime The new start time
     */
    public void setStartTime(double newStartTime);

    /**
     * Accessor method to get current value of field startTime.
     * Default value is <code>0</code>.
     *
     * @return The current startTime
     */
    public double getStartTime();

    /**
     * Accessor method to set a new value for field attribute stopTime.
     *
     * @param newStopTime The new stop time
     */
    public void setStopTime(double newStopTime);

    /**
     * Accessor method to get current value of field stopTime.
     * Default value is <code>0</code>
     *
     * @return The current stop Time
     */
    public double getStopTime();
}
