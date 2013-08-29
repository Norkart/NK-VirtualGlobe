/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.ecmascript.builtin;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * Extension of the basic scriptable object with some specifics for handling
 * nested field data.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class NodeFieldObject extends FieldScriptableObject {

    /** The parent node of this field */
    protected VRMLNodeType parentNode;

    /** The field index in the parentNode that this belongs to */
    protected int parentFieldIndex;

    /**
     * Construct a new instance of this object using the given name.
     *
     * @param name The name of the scriptable object
     */
    protected NodeFieldObject(String name) {
        super(name);
    }

    /**
     * Set the container node and field information for this node.
     *
     * @param parent The parent node of this field
     * @param fieldIndex The index of the field that this field wraps
     */
    public void setParentField(VRMLNodeType parent, int fieldIndex) {
        parentNode = parent;
        parentFieldIndex = fieldIndex;
    }

    /**
     * Get the list of fields that have changed. The return value may be
     * either a single {@link NodeFieldData} instance or an
     * {@link java.util.ArrayList} of field data instances if more than one
     * has changed. When called, this is recursive so that all fields and
     * nodes referenced by this node field will be included. If no fields have
     * changed, this will return null. However, that should never happen as the
     * user should always check {@link FieldScriptableObject#hasChanged()} which
     * would return false before calling this method.
     *
     * @return A single {@link NodeFieldData}, {@link java.util.ArrayList}
     *   or null
     */
    public abstract Object getChangedFields();

    /**
     * If the node contains a node instance, check and call its setupFinished
     * if needed.
     */
    public abstract void realize();
}
