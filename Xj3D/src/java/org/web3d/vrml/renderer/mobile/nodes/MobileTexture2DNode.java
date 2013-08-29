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

package org.web3d.vrml.renderer.mobile.nodes;

// Standard imports
import java.awt.image.*;

import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;

import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * Abstract implementation of a 2D texture object.
 * <p>
 * This implementation will not contain the impl object Texture2D.
 *
 * @author Alan Hudson
 * @version $Revision: 1.5 $
 */
public abstract class MobileTexture2DNode extends MobileTextureNode
    implements MobileTexture2DNodeType {

    /** Field Index */
    protected static final int FIELD_REPEATS = LAST_NODE_INDEX + 1;
    protected static final int FIELD_REPEATT = LAST_NODE_INDEX + 2;

    protected static final int LAST_TEXTURENODETYPE_INDEX = FIELD_REPEATT;

    // VRML Field declarations

    /** field SFBool repeatS TRUE */
    protected boolean vfRepeatS;

    /** field SFBool repeatT TRUE */
    protected boolean vfRepeatT;


    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Default constructor for the node. The repeat flags are set to TRUE.
     *
     * @param name The name of the type of node
     */
    public MobileTexture2DNode(String name) {
        super(name);

        vfRepeatS = true;
        vfRepeatT = true;
    }

    /**
     * Set the fields of the grouping node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The grouping node to copy info from
     */
    protected void copy(VRMLTexture2DNodeType node) {
        boolean field = node.getRepeatS();
        vfRepeatS = field;

        field = node.getRepeatT();
        vfRepeatT = field;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLTexture2DNodeType interface.
    //-------------------------------------------------------------

    /**
     * Accessor method to set a new value for field attribute <b>repeatS</b>
     *
     * @param The new value for repeatS
     */
    public void setRepeatS(boolean newRepeatS) {
        vfRepeatS = newRepeatS;
        hasChanged[FIELD_REPEATS] = true;
        fireFieldChanged(FIELD_REPEATS);
    }

    /**
     * Accessor method to get current value of field <b>repeatS</b>,
     * default value is <code>true</code>
     *
     * @return The current value of repeatS
     */

    public boolean getRepeatS() {
        return vfRepeatS;
    }

    /**
     * Accessor method to set a new value for field attribute <b>repeatT</b>
     *
     * @param The new value of repeatT
     */
    public void setRepeatT(boolean newRepeatT) {
        vfRepeatT = newRepeatT;
        hasChanged[FIELD_REPEATT] = true;
        fireFieldChanged(FIELD_REPEATT);
    }

    /**
     * Accessor method to get current value of field <b>repeatT</b>,
     * default value is <code>true</code>
     *
     * @return The current value of repeatS
     */
    public boolean getRepeatT() {
        return vfRepeatT;
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNode interface.
    //----------------------------------------------------------

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

        switch(index) {
            case FIELD_REPEATS:
                fieldData.clear();
                fieldData.booleanValue = vfRepeatT;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_REPEATT:
                fieldData.clear();
                fieldData.booleanValue = vfRepeatT;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            default:
                return(super.getFieldValue(index));
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

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_REPEATS :
                    destNode.setValue(destIndex, vfRepeatS);
                    break;
                case FIELD_REPEATT :
                    destNode.setValue(destIndex, vfRepeatT);
                    break;
                default: System.err.println("sendRoute: No index: " + srcIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_REPEATS:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "repeatS is initializeOnly");

                vfRepeatS = value;
                break;

            case FIELD_REPEATT:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "repeatT is initializeOnly");

                vfRepeatT = value;
                break;

            default:
                super.setValue(index, value);
        }
    }
}
