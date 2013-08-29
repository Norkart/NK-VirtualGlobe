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

package org.web3d.vrml.scripting.jsai;

// Standard imports
// none

// Application specific imports
import vrml.BaseNode;
import vrml.Browser;
import vrml.node.Script;
import vrml.field.ConstSFNode;
import vrml.field.SFNode;

import org.web3d.vrml.lang.FieldException;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeListener;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Xj3D Specific implementation of the SFNode field when extracted from part
 * of a parentNode.
 * <p>
 *
 * The parentNode assumes that the index and parentNode have been checked before use by
 * this class. It also assumes internally that the only way a parentNode can be
 * passed to this class is through using an instance of the JSAINode where
 * possible.
 * <p>
 *
 * This class interprets setting a field value to <code>null</code> as
 * clearing the child reference
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
class JSAISFNode extends SFNode
    implements VRMLNodeListener, NodeField {

    /** The node that this field belongs to */
    private VRMLNodeType parentNode;

    /** The index of the field this representation belongs to */
    private int fieldIndex;

    /** The Xj3D child node that we keep locally */
    private VRMLNodeType childNode;

    /** The factory to create field references from */
    private FieldFactory fieldFactory;

    /** The browser instance we pass around the nodes */
    private Browser browser;

    /**
     * Create a new field that represents the underlying instance
     *
     * @param n The node to fetch information from
     * @param index The index of the field to use
     */
    JSAISFNode(VRMLNodeType n, int index) {
        parentNode = n;
        fieldIndex = index;

        parentNode.addNodeListener(this);
    }

    /**
     * Initialise this instance with the extra information needed.
     *
     * @param b The browser to use with this node
     * @param fac The Factory used to create fields
     */
    public void initialize(Browser b, FieldFactory fac) {
        browser = b;
        fieldFactory = fac;

        valueChanged = true;
        updateLocalData();
    }

    /**
     * Get the value of the field. Overrides the basic implementation to
     * make sure that it fetches new data each time.
     *
     * @return The value of the field
     */
    public BaseNode getValue() {
        updateLocalData();

        return data;
    }

    /**
     * Set the field to the new value
     *
     * @param val The new value to use
     */
    public void setValue(BaseNode val) {
        if(data == val)
            return;

        data = val;

        try {
            childNode = val.getImplNode();
            parentNode.setValue(fieldIndex, childNode);
        } catch(FieldException ife) {
        }
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    public void setValue(SFNode val) {

        BaseNode node = val.getValue();
        setValue(node);
    }

    /**
     * Set the value of the field to that of the given field
     *
     * @param val The field to use to set the value
     */
    public void setValue(ConstSFNode val) {
        BaseNode node = val.getValue();
        setValue(node);
    }

    /**
     * Create a cloned copy of this parentNode
     *
     * @return A copy of this field
     */
    public Object clone() {
        JSAISFNode ret_val = new JSAISFNode(parentNode, fieldIndex);
        ret_val.initialize(browser, fieldFactory);

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeListener interface.
    //----------------------------------------------------------

    /**
     * Notification that the field represented by the given index has changed.
     *
     * @param index The index of the field that has changed
     */
    public void fieldChanged(int index) {
        if(index != fieldIndex)
            return;

        valueChanged = true;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Fetch the field from the core and update any values internally.
     */
    private void updateLocalData() {
        if(!valueChanged)
            return;

        try {
            VRMLFieldData fd = parentNode.getFieldValue(fieldIndex);

            if (fd == null) return;

            if (fd.nodeValue == null) {
                data = null;
            }
            else if((childNode == null) || (childNode != fd.nodeValue)) {
                    childNode = (VRMLNodeType)fd.nodeValue;
                    data = new JSAINode(childNode, browser, fieldFactory);
            }

            valueChanged = false;
        } catch(FieldException ife) {
        }
    }
}
