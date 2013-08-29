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
package vrml.node;

// Standard imports
// none

// Application specific imports
import vrml.*;
import vrml.field.*;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * This is the basic interface for all VRML nodes.  All VRML fields are
 * accessed through the field methods.  New nodes are created through the
 * Browser using the load and create methods.
 *
 * @author Alan Hudson
 * @version $Revision: 1.8 $
 */
public abstract class Node extends BaseNode {

    /**
     * Create a field given a name.
     *
     * @param name The name of the field to fetch
     * @param checkEventIn true if we should check for an event in
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventIn
     * @throws InvalidExposedFieldException The field is not an exposedField
     */
    protected abstract Field createField(String name, boolean checkEventIn);

    /**
     * Create a constant field that represents an eventOut.
     *
     * @param name The name of the field to fetch
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventOut
     */
    protected abstract ConstField createConstField(String name);

    /**
     * Fetch the named exposed field from this node instance. This method
     * will accept all forms of the set_X, X and X_changed to access the
     * node.
     *
     * @param fieldName The name of the field to fetch
     * @return An instance of the field class representing the field
     * @throws InvalidExposedFieldException The field is not an exposedField
     *   or not known to this node
     */
    public final Field getExposedField(String fieldName)
        throws InvalidExposedFieldException {

        return createField(fieldName, false);
    }

    /**
     * Fetch the named exposed field from this node instance. This method
     * will accept all forms of the set_X, and X to access the node for the
     * field declaration being either a real eventOut or just a portion of
     * an exposedField.
     *
     * @param fieldName The name of the field to fetch
     * @return An instance of the field class representing the field
     * @throws InvalidEventInException The field is not an eventIn
     *   or not known to this node
     */
    public final Field getEventIn(String fieldName)
        throws InvalidEventInException {

        return createField(fieldName, true);
    }

    /**
     * Fetch the named eventOut field from this node instance. This method
     * will accept forms of the X and X_changed to access the node for the
     * field declaration being either a real eventOut or just a portion of
     * an exposedField.
     *
     * @param fieldName The name of the field to fetch
     * @return An instance of the field class representing the field
     * @throws InvalidEventOutException The field is not an eventOut
     *   or not known to this node
     */
    public final ConstField getEventOut(String fieldName)
        throws InvalidEventOutException {

        return createConstField(fieldName);
    }
}
