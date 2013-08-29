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
 * A listener for changes in the values of a VRMLNavigationInfo node.
 * <p>
 * Using a specific interface rather than force the interested subsystems to
 * deal with field processing logic.
 *
 * @author Brad Vender
 * @version $Revision: 1.3 $
 *
 */
public interface NavigationInfoChangeListener {

    /**
     * Notification that the navigation modes allowed has changed
     * on the current NavigationInfo node.
     *
     * @param newModes The new allowed navigation modes
     * @param numModes number of valid modes in array
     */
    public void notifyNavigationModesChanged(String[] newModes, int numModes);

    /**
     * Notification that the avatar size has changed
     * on the current NavigationInfo node.
     *
     * @param size The size parameters for the avatar
     * @param dimensions The number of valid avatar dimensions
     */
    public void notifyAvatarSizeChanged(float[] size, int dimensions);

    /**
     * Notification that the navigation speed has changed on the
     * current NavigationInfo node.
     *
     * @param newSpeed The new navigation speed.
     */
    public void notifyNavigationSpeedChanged(float newSpeed);

    /**
     * Notification that the visibility limit has been changed.
     *
     * @param distance The new distance value to use
     */
    public void notifyVisibilityLimitChanged(float distance);

    /**
     * Notification that headlight state has changed.
     *
     * @param enable true if the headlight should now be on
     */
    public void notifyHeadlightChanged(boolean enable);
}
