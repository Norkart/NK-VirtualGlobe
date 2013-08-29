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

package org.web3d.vrml.renderer.common.nodes;

// External imports
// none

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

/**
 * Base implementation of a 3D texture object.
 * <p>
 * Performs the VRML field handling for REPEAT_S, REPEAT_T and REPEAT_R
 * <p>
 *
 * The node is defined by the X3DTexture3DNode type, which is defined as:
 * <pre>
 * X3DTexture3DNode : X3DTextureNode {
 *   SFNode [in,out] metadata NULL [X3DMetadataObject]
 *   SFBool []       repeatS  FALSE
 *   SFBool []       repeatT  FALSE
 *   SFBool []       repeatR  FALSE
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public abstract class BaseTexture3DNode extends BaseTextureNode
    implements VRMLTexture3DNodeType {

    /** Index of the repeatS field*/
    protected static final int FIELD_REPEATS = LAST_NODE_INDEX + 1;

    /** Index of the repeatT field*/
    protected static final int FIELD_REPEATT = LAST_NODE_INDEX + 2;

    /** Index of the repeatR field*/
    protected static final int FIELD_REPEATR = LAST_NODE_INDEX + 3;

    /** Last valid index used in this class */
    protected static final int LAST_3DTEXTURE_INDEX = FIELD_REPEATR;

    // VRML Field declarations

    /** field SFBool repeatS TRUE */
    protected boolean vfRepeatS;

    /** field SFBool repeatT TRUE */
    protected boolean vfRepeatT;

    /** field SFBool repeatR TRUE */
    protected boolean vfRepeatR;

    /** The number of 2D image slices that the user has defined */
    protected int textureDepth;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Default constructor for the node. The repeat flags are set to TRUE.
     *
     * @param name The name of the type of node
     */
    public BaseTexture3DNode(String name) {
        super(name);

        vfRepeatS = false;
        vfRepeatT = false;
        vfRepeatR = false;

        textureDepth = 0;
    }

    /**
     * Set the fields of the grouping node that has the fields set
     * based on the fields of the passed in node. This will not copy any
     * children nodes, only the local fields.
     *
     * @param node The grouping node to copy info from
     */
    protected void copy(VRMLTexture3DNodeType node) {
        boolean field = node.getRepeatS();
        vfRepeatS = field;

        field = node.getRepeatT();
        vfRepeatT = field;

        field = node.getRepeatR();
        vfRepeatR = field;

        textureDepth = node.getDepth();
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLTexture3DNodeType interface.
    //-------------------------------------------------------------

    /**
     * Get the depth of the texture. This is the number of 2D slices that are
     * provided (by the user) and should always be a multiple of 2.
     *
     * @return A positive multiple of 2 or zero if none defined.
     */
    public int getDepth() {
        return textureDepth;
    }

    /**
     * Get the texture type of this texture.  Valid entries are defined
     * in the vrml.lang.TextureConstants.
     *
     * @return TextureConstants.TYPE_SINGLE_3D
     */
    public int getTextureType() {
        return TextureConstants.TYPE_SINGLE_3D;
    }

    /**
     * Get the value of field repeatS. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatS
     */
    public boolean getRepeatS() {
        return vfRepeatS;
    }

    /**
     * Get the value of field repeatT. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatT
     */
    public boolean getRepeatT() {
        return vfRepeatT;
    }

    /**
     * Get the value of field repeatR. The field is not writable.
     * Default value is <code>true</code>.
     *
     * @return The current value of repeatR
     */
    public boolean getRepeatR() {
        return vfRepeatR;
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

            case FIELD_REPEATR:
                fieldData.clear();
                fieldData.booleanValue = vfRepeatR;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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

        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG);

        switch(index) {
            case FIELD_REPEATS:
                vfRepeatS = value;
                break;

            case FIELD_REPEATT:
                vfRepeatT = value;
                break;

            case FIELD_REPEATR:
                vfRepeatR = value;
                break;

            default:
                super.setValue(index, value);
        }
    }
}
