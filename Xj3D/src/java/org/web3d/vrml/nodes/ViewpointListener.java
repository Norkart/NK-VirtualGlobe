/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
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

// Application specific imports

/**
 * Notification that a viewpoints parameters have changed.  This currently
 * notifies on changes of centerOfRotation and fieldOfView.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public interface ViewpointListener {
    /**
     * The center of rotation has changed.
     *
     * @param val The new value
     */
    public void centerOfRotationChanged(float[] val);

    /**
     * The field of view has changed.
     *
     * @param val The new value
     */
    public void fieldOfViewChanged(float[] val);
}
