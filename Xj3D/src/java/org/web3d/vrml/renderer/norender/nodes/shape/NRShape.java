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

package org.web3d.vrml.renderer.norender.nodes.shape;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;
import org.web3d.vrml.nodes.VRMLBoundedNodeType;

import org.web3d.vrml.renderer.common.nodes.shape.BaseShape;
import org.web3d.vrml.renderer.norender.nodes.NRVRMLNode;

/**
 * Null renderer implementation of a shape node.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class NRShape extends BaseShape
    implements NRVRMLNode {

    /**
     * Construct a new default shape node implementation.
     */
    public NRShape() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Shape node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect node type
     */
    public NRShape(VRMLNodeType node) {
        super(node);
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLAppearanceNodeType interface.
    //-------------------------------------------------------------

    /**
     * Set node content as replacement for <code>appearance</code>.
     *
     * @param newAppearance The new appearance.  null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setAppearance(VRMLNodeType newAppearance)
        throws InvalidFieldValueException {

        if(!(newAppearance instanceof NRVRMLNode) &&
           !(newAppearance instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(APPEARANCE_NODE_MSG);

        super.setAppearance(newAppearance);
    }

    /**
     * Set node content as replacement for <code>geometry</code>.
     *
     * @param newGeomtry The new value for geometry.  Null will act like delete
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setGeometry(VRMLNodeType newGeometry)
        throws InvalidFieldValueException {

        if(!(newGeometry instanceof NRVRMLNode) &&
           !(newGeometry instanceof VRMLProtoInstance))
            throw new InvalidFieldValueException(GEOMETRY_NODE_MSG);

        super.setGeometry(newGeometry);
    }
}
