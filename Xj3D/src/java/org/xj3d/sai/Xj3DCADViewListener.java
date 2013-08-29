/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.X3DNode;

/**
 * Listener for changes of structure related to CAD-specific content.
 * <p>
 *
 * These callback methods provide information for changes within an existing
 * scene. For bulk changes, such as the change of scene, please use the base
 * browser listener interface from the SAI.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface Xj3DCADViewListener {

    /**
     * Notification that an assembly was added to the root of the world.
     *
     * @param assembly The node instance that was added
     */
    public void assemblyAdded(X3DNode assembly);

    /**
     * Notification that an assembly was removed from the root of the world.
     *
     * @param assembly The node instance that was removed
     */
    public void assemblyRemoved(X3DNode assembly);

    /**
     * Notification that a CADLayer was added to the root of the world.
     *
     * @param assembly The node instance that was added
     */
    public void layerAdded(X3DNode layer);

    /**
     * Notification that a CADLayer was added to the root of the world.
     *
     * @param assembly The node instance that was removed
     */
    public void layerRemoved(X3DNode layer);
}
