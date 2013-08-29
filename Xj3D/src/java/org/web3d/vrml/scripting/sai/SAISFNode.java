/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
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
 * Representation of a SFNode field.
 * <P>
 * Get the value of a node. The java <CODE>null</CODE> reference is treated to
 * be equivalent to the VRML <CODE>NULL</CODE> field values. If the node field
 * contains a NULL reference then reading this eventOut will result in a
 * java null being returned.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
class SAISFNode extends BaseField
    implements SFNode, NodeField {

    /** The local field value */
    private VRMLNodeType localValue;

    /** The SAI wrapper for the node */
    private BaseNode saiNode;

    /** Factory used for field generation */
    private FieldFactory fieldFactory;

    /** Reference queue used for keeping track of field object instances */
    private ReferenceQueue fieldQueue;

    /** The BaseNode factory */
    private BaseNodeFactory baseNodeFactory;
    
    /**
     * Create a new instance of the field class.
     *
     * @param n The node the field belongs to
     * @param field The field of the node this field instance represents
     * @param internal true if this represents an internal field definition
     * @param layerId The layer this node instance belongs to
     */
    SAISFNode(VRMLNodeType n, int field, boolean internal) {
        super(n, field, internal);
    }

    //----------------------------------------------------------
    // Methods defined by SFNode
    //----------------------------------------------------------

    /**
     * Get the node value in the given eventOut. If no node reference is set then
     * null is returned to the user.
     *
     * @return The new node reference set.
     */
    public X3DNode getValue() {
        checkAccess(false);

        return saiNode;
    }

    /**
     * Set the node value in the given eventIn.
     * <P>
     * If the node reference passed to this method has already had the dispose
     * method called then an InvalidNodeException will be generated.
     *
     * @param value The new node reference to be used.
     *
     * @exception InvalidNodeException The node reference passed has already
     *    been disposed.
     */
    public void setValue(X3DNode value)
        throws InvalidNodeException {

        checkAccess(true);

        if((value != null) && !value.isRealized())
            value.realize();

        saiNode = (BaseNode)value;
        localValue = (value != null) ? ((BaseNode)value).getImplNode() : null;

        dataChanged = true;
    }

    //----------------------------------------------------------
    // Methods defined by NodeField
    //----------------------------------------------------------

    /**
     * A recursive check if this node field or any of the children nodes have
     * changed. Used when directOutput is true on the script node and we
     * need to check if the children have updated, regardless of whether
     * we have. If anything has changed, update it during this call.
     */
    public void updateNodeAndChildren() {

        if(saiNode != null)
            saiNode.updateNodeAndChildren();

        if(dataChanged) {
            node.setValue(fieldIndex, localValue);
            dataChanged = false;
        }
    }

    /**
     * A recursive check if the underlying node has changed any values from
     * it's current wrapper values and update the wrapper with the latest.
     * Used when directOutput is true on the script node and we
     * need to check if the children have updated, regardless of whether
     * we have. If anything has changed, update it during this call.
     */
    public void updateFieldAndChildren() {
        updateField();

        if(saiNode != null)
            saiNode.updateFields();
    }

    /**
     * Control whether operations are valid on this field instance right
     * now. Overrides base class method to pass the instruction on to the
     * children field that have been fetched from the base node.
     *
     * @param valid True if access operations are now permitted.
     */
    public void setAccessValid(boolean valid) {
        super.setAccessValid(valid);

        if(saiNode != null)
            saiNode.setAccessValid(valid);
    }

    /**
     * Set the field factory used to create field instances for the
     * X3DNode implementation.
     *
     * @param fac The factory to use for the field generation
     */
    public void setFieldFactory(FieldFactory fac) {
        fieldFactory = fac;
    }

    /**
     * Set the reference queue used for managing fields.
     *
     * @param queue The queue to use for each field
     */
    public void setFieldReferenceQueue(ReferenceQueue queue) {
        fieldQueue = queue;
    }

    /**
     * Set the node factory used to create node wrapper instances
     *
     * @param fac The factory to use for node wrapper generation
     */
    public void setNodeFactory(BaseNodeFactory fac) {
        baseNodeFactory = fac;
    }

    //----------------------------------------------------------
    // Methods defined by BaseField
    //----------------------------------------------------------

    /**
     * Notification to the field instance to update the value in the
     * underlying node now.
     */
    void updateNode() {
        node.setValue(fieldIndex, localValue);
        dataChanged = false;
    }

    /**
     * Notification to the field to update its field values from the
     * underlying node.
     */
    void updateField() {
        if(!isReadable())
            return;

        VRMLFieldData data = node.getFieldValue(fieldIndex);

        if(localValue != data.nodeValue) {
            localValue = (VRMLNodeType)data.nodeValue;

            if(localValue == null) {
                saiNode = null;
            } else {
                //saiNode = new BaseNode(localValue,
                //                       fieldQueue,
                //                       fieldFactory,
                 //                      fieldAccessListener);
                saiNode = (BaseNode)baseNodeFactory.getBaseNode( localValue );
                saiNode.setAccessValid(accessPermitted);
            }
        }

        dataChanged = false;
    }
}
