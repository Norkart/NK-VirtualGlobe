/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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
import org.web3d.vrml.lang.VRMLNode;

/**
 * Thread local version of VRMLFieldData to make getFieldValue calls thread safe.
 *
 * @author Alan Hudson
 * @version $Revision: 1.1 $
 */
public class VRMLFieldDataThreaded extends ThreadLocal<VRMLFieldData> {
    /**
     * Initialize the thread specific instance.
     */
    protected synchronized VRMLFieldData initialValue() {
        return new VRMLFieldData();
    }
}
