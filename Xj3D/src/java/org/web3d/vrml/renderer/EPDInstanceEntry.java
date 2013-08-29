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
package org.web3d.vrml.renderer;

// Standard imports

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * An External Prototype Declaration helper class for containing instance entries.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
class EPDInstanceEntry {
    VRMLNodeType parent;
    int idx;
    VRMLNodeType instance;

    EPDInstanceEntry(VRMLNodeType parent, int idx, VRMLNodeType instance) {
        this.parent = parent;
        this.idx = idx;
        this.instance = instance;
    }
}
