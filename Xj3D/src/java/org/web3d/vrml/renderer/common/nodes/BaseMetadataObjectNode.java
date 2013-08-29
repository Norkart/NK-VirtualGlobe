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

package org.web3d.vrml.renderer.common.nodes;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * An abstract implementation of any node that uses component nodes to provide
 * coordinate, normal and texture information.
 * <p>
 *
 *
 * @author =Justin Couch
 * @version $Revision: 1.3 $
 */
public abstract class BaseMetadataObjectNode extends AbstractNode
    implements VRMLMetadataObjectNodeType {

    /** Index of the name field */
    protected static final int FIELD_NAME = LAST_NODE_INDEX + 1;

    /** Index of the reference field */
    protected static final int FIELD_REFERENCE = LAST_NODE_INDEX + 2;

    /** The last field index used by this class */
    protected static final int LAST_METADATA_INDEX = FIELD_REFERENCE;

    /** exposedField SFString name "" */
    protected String vfName;

    /** exposedField SFString reference "" */
    protected String vfReference;

    /**
     * Construct a default instance of this class with the bind flag set to
     * false and no time information set (effective value of zero).
     *
     * @param name The name of the type of node
     */
    protected BaseMetadataObjectNode(String name) {
        super(name);
    }

    /**
     * Copy the values of the metadata node into this node.
     *
     * @param node The node to copy details from
     */
    protected void copy(VRMLMetadataObjectNodeType node) {
        vfReference = node.getReference();
        vfName = node.getName();
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLMetadataObjectNodeType
    //-------------------------------------------------------------

    /**
     * Get the currently set name associated with this metadata object. If
     * none is set, this returns null.
     *
     * @return The current name
     */
    public String getName() {
        return vfName;
    }

    /**
     * Set the name value for the metadata object. Use null to clear the
     * currently set name.
     *
     * @param name The name to use
     */
    public void setName(String name) {
        vfName = name;

        if(!inSetup) {
            hasChanged[FIELD_NAME] = true;
            fireFieldChanged(FIELD_NAME);
        }
    }

    /**
     * Get the currently set reference associated with this metadata object.
     * If none is set, this returns null.
     *
     * @return The current reference
     */
    public String getReference() {
        return vfReference;
    }

    /**
     * Set the reference value for the metadata object. Use null to clear the
     * currently set reference.
     *
     * @param reference The reference to use
     */
    public void setReference(String reference) {
        vfReference = reference;

        if(!inSetup) {
            hasChanged[FIELD_REFERENCE] = true;
            fireFieldChanged(FIELD_REFERENCE);
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNode
    //-------------------------------------------------------------

    /**
     * Get the primary type of this node. Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.MetadataObjectNodeType;
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLNodeType
    //-------------------------------------------------------------

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        fieldData.clear();

        switch(index) {
            case FIELD_NAME:
                fieldData.stringValue = vfName;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_REFERENCE:
                fieldData.stringValue = vfReference;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {
        try {
            switch(srcIndex) {
                case FIELD_NAME:
                    destNode.setValue(destIndex, vfName);
                    break;

                case FIELD_REFERENCE:
                    destNode.setValue(destIndex, vfReference);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_NAME:
                setName(value);
                break;

            case FIELD_REFERENCE:
                setReference(value);
                break;

            default:
                super.setValue(index, value);
        }
    }
}
