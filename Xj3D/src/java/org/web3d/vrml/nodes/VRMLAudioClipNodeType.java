/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
 * A node which can play audio files
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public interface VRMLAudioClipNodeType
    extends VRMLTimeControlledNodeType,
            VRMLExternalNodeType {

    /**
     * Set a new value for the description field.
     *
     * @param newDescription The new description
     */
    public void setDescription(String newDescription);

    /**
     * Get current value of the description field.
     *
     * @return The description
     */
    public String getDescription();

    /**
     * Set a new value for the pitch field.
     *
     * @param newPitch The pitch
     */
    public void setPitch(float newPitch);

    /**
     * Accessor method to get current value of field <b>pitch</b>,
     * default value is <code>1</code>.
     *
     * @return The Pitch
     */
    public float getPitch();

    /**
     * Accessor method to set a new value for field attribute <b>url</b>
     *
     * @param newUrl Array of URL strings
     * @param numValid The number of valid items in the array
     */
    public void setUrl(String[] newUrl, int numValid);

    /**
     * Accessor method to get current value of field <b>url</b>.
     *
     * @return An Array of URL strings
     */
    public String[] getUrl();

    /**
     * Accessor method to set a new value for field attribute <b>duration</b>
     *
     * @param newDuration The new duration
     */
    public void setDuration(double newDuration);

    /**
     * Accessor method to get current value of field <b>duration</b>
     *
     * @return The duration
     */
    public double getDuration();

    /**
     * Accessor method to set a new value for field attribute <b>isActive</b>
     */
    public void setIsActive(boolean newIsActive);

    /**
     * Accessor method to get current value of field <b>isActive</b>
     *
     * @return The value of isActive
     */
    public boolean getIsActive();
}
