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
// none

// Application specific imports
// none

/**
 * An listener for changes in the node.
 * <p>
 *
 * The listener does simple notifications. The idea is to minimise the amount
 * of data that gets shuffled around in method calls.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLNodeListener {

    /**
     * Notification that the field represented by the given index has changed.
     *
     * @param index The index of the field that has changed
     */
    public void fieldChanged(int index);
}
