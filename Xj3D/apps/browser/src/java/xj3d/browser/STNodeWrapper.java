/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
 *                               Java Source
 *
 * This source is licensed under the GNU GPL v2.0
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.browser;

import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * A wrapper for Tree usage.
 *
 * @author Alan Hudson
 */

public class STNodeWrapper {
    /** The VRML Node */
    private VRMLNodeType node;

    /** The label to use */
    private String label;

    /**
     * Constructor.
     *
     * @param n The VRML node to wrap
     * @param l The label to show
     */
    public STNodeWrapper(VRMLNodeType n, String l) {
        node = n;
        label = l;
    }

    public String toString() {
        return label;
    }

    public VRMLNodeType getVRMLNode() {
        return node;
    }
}
