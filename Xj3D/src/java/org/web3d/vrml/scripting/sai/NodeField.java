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

package org.web3d.vrml.scripting.sai;

// Standard imports
import java.lang.ref.ReferenceQueue;

// Application specific imports
import org.web3d.x3d.sai.SFNode;
import org.web3d.x3d.sai.X3DNode;
import org.web3d.x3d.sai.InvalidNodeException;

import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Marker interface for fields that handle X3DNode information -
 * principally SFNode/MFNode.
 * <P>
 *
 * A collection of internal convenience methods are provided that
 * will be used to pass commands down the heirarchy.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
interface NodeField {

    /**
     * A recursive check if this node field or any of the children nodes have
     * changed. Used when directOutput is true on the script node and we
     * need to check if the children have updated, regardless of whether
     * we have. If anything has changed, update it during this call.
     */
    void updateNodeAndChildren();

    /**
     * A recursive check if the underlying node has changed any values from
     * it's current wrapper values and update the wrapper with the latest.
     * Used when directOutput is true on the script node and we
     * need to check if the children have updated, regardless of whether
     * we have. If anything has changed, update it during this call.
     */
    void updateFieldAndChildren();

    /**
     * Control whether operations are valid on this field instance right
     * now. Overrides base class method to pass the instruction on to the
     * children field that have been fetched from the base node.
     *
     * @param valid True if access operations are now permitted.
     */
    void setAccessValid(boolean valid);

    /**
     * Set the field factory used to create field instances for the
     * X3DNode implementation.
     *
     * @param fac The factory to use for the field generation
     */
    void setFieldFactory(FieldFactory fac);

    /**
     * Set the reference queue used for managing fields.
     *
     * @param queue The queue to use for each field
     */
    void setFieldReferenceQueue(ReferenceQueue queue);
    
    /**
     * Set the node factory used to create node wrapper instances
     *
     * @param fac The factory to use for node wrapper generation
     */
    void setNodeFactory(BaseNodeFactory fac);

}
