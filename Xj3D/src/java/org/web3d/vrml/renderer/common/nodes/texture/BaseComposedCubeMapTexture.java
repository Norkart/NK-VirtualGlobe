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

package org.web3d.vrml.renderer.common.nodes.texture;

// External imports
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.ArrayList;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseTextureNode;

/**
 * Base implementation of a ComposedCubeMapTexture node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class BaseComposedCubeMapTexture extends BaseTextureNode
    implements VRMLEnvironmentTextureNodeType,
               VRMLComposedTextureNodeType {

    /** front texture children Index */
    protected static final int FIELD_FRONT = LAST_NODE_INDEX + 1;

    /** back texture children Index */
    protected static final int FIELD_BACK = LAST_NODE_INDEX + 2;

    /** left texture children Index */
    protected static final int FIELD_LEFT = LAST_NODE_INDEX + 3;

    /** right texture children Index */
    protected static final int FIELD_RIGHT = LAST_NODE_INDEX + 4;

    /** top texture children Index */
    protected static final int FIELD_TOP = LAST_NODE_INDEX + 5;

    /** bottom texture children Index */
    protected static final int FIELD_BOTTOM = LAST_NODE_INDEX + 6;


    /** Index of the last field of this node */
    protected static final int LAST_COMPOSED_ENVTEXTURE_INDEX = FIELD_BOTTOM;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_COMPOSED_ENVTEXTURE_INDEX + 1;

    /** Message for when the proto is not a Texture */
    private static final String TEXTURE_PROTO_MSG =
        "Proto does not describe a Texture object";

    /** Message for when the node in setValue() is not a Texture */
    private static final String TEXTURE_NODE_MSG =
        "Node does not describe a Texture object";

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // Field values

    /** exposedField SFNode front NULL */
    protected VRMLTexture2DNodeType vfFront;

    /** proto version of front field */
    protected VRMLProtoInstance pFront;

    /** exposedField SFNode back NULL */
    protected VRMLTexture2DNodeType vfBack;

    /** proto version of back field */
    protected VRMLProtoInstance pBack;

    /** exposedField SFNode left NULL */
    protected VRMLTexture2DNodeType vfLeft;

    /** proto version of left field */
    protected VRMLProtoInstance pLeft;

    /** exposedField SFNode right NULL */
    protected VRMLTexture2DNodeType vfRight;

    /** proto version of right field */
    protected VRMLProtoInstance pRight;

    /** exposedField SFNode top NULL */
    protected VRMLTexture2DNodeType vfTop;

    /** proto version of top field */
    protected VRMLProtoInstance pTop;

    /** exposedField SFNode bottom NULL */
    protected VRMLTexture2DNodeType vfBottom;

    /** proto version of right field */
    protected VRMLProtoInstance pBottom;


    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_BACK,
            FIELD_FRONT,
            FIELD_LEFT,
            FIELD_RIGHT,
            FIELD_TOP,
            FIELD_BOTTOM,
            FIELD_METADATA
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_FRONT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "front");
        fieldDecl[FIELD_BACK] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "back");
        fieldDecl[FIELD_LEFT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "left");
        fieldDecl[FIELD_RIGHT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "right");
        fieldDecl[FIELD_TOP] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "top");
        fieldDecl[FIELD_BOTTOM] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "bottom");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_FRONT);
        fieldMap.put("front", idx);
        fieldMap.put("set_front", idx);
        fieldMap.put("front_changed", idx);

        idx = new Integer(FIELD_BACK);
        fieldMap.put("back", idx);
        fieldMap.put("set_back", idx);
        fieldMap.put("back_changed", idx);

        idx = new Integer(FIELD_LEFT);
        fieldMap.put("left", idx);
        fieldMap.put("set_left", idx);
        fieldMap.put("left_changed", idx);

        idx = new Integer(FIELD_BOTTOM);
        fieldMap.put("bottom", idx);
        fieldMap.put("set_bottom", idx);
        fieldMap.put("bottom_changed", idx);

        idx = new Integer(FIELD_RIGHT);
        fieldMap.put("right", idx);
        fieldMap.put("set_right", idx);
        fieldMap.put("right_changed", idx);

        idx = new Integer(FIELD_TOP);
        fieldMap.put("top", idx);
        fieldMap.put("set_top", idx);
        fieldMap.put("top_changed", idx);
    }

    /**
     * Default constructor.
     */
    protected BaseComposedCubeMapTexture() {
        super("ComposedCubeMapTexture");

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseComposedCubeMapTexture(VRMLNodeType node) {
        this();

        // nothing else to do. All the node fields are copied as part
        // of the scene graph traversal steps.
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the texture type of this texture.  Valid entries are defined
     * in the vrml.lang.TextureConstants.
     */
    public int getTextureType() {
        return TextureConstants.TYPE_MULTI;
    }

    //---------------------------------------------------------------
    // Methods required by the VRMLComposedTextureNodeType interface.
    //---------------------------------------------------------------

    /**
     * Get the number of textures in this ComposedTexture node. Always return
     * a value of 6.
     *
     * @return The number of active textures.
     */
    public int getNumberTextures() {
        return 6;
    }

    /**
     * Get the textures which make up this composed texture.
     *
     * @param start Where in the array to start filling in textures.
     * @param texs The preallocated array to return texs in.  Error if too small.
     */
    public void getTextures(int start, VRMLTextureNodeType[] texs) {
        texs[start] = vfFront;
        texs[start + 1] = vfBack;
        texs[start + 2] = vfLeft;
        texs[start + 3] = vfRight;
        texs[start + 4] = vfTop;
        texs[start + 5] = vfBottom;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLTexture2DNodeType interface.
    //-------------------------------------------------------------
    /**
     * Get the image representation of this texture.
     *
     * @return The image.
     */
    public NIOBufferImage getImage() {
        return null;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0 || index > LAST_COMPOSED_ENVTEXTURE_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.EnvironmentTextureNodeType;
    }

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
            case FIELD_FRONT:
                fieldData.clear();
                if(pFront != null)
                    fieldData.nodeValue = pFront;
                else
                    fieldData.nodeValue = vfFront;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_BACK:
                fieldData.clear();
                if(pBack != null)
                    fieldData.nodeValue = pBack;
                else
                    fieldData.nodeValue = vfBack;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_LEFT:
                fieldData.clear();
                if(pLeft != null)
                    fieldData.nodeValue = pLeft;
                else
                    fieldData.nodeValue = vfLeft;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_RIGHT:
                fieldData.clear();
                if(pRight != null)
                    fieldData.nodeValue = pRight;
                else
                    fieldData.nodeValue = vfRight;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_TOP:
                fieldData.clear();
                if(pTop != null)
                    fieldData.nodeValue = pTop;
                else
                    fieldData.nodeValue = vfTop;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_BOTTOM:
                fieldData.clear();
                if(pBottom != null)
                    fieldData.nodeValue = pBottom;
                else
                    fieldData.nodeValue = vfBottom;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
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

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_FRONT:
                    if(pFront != null)
                        destNode.setValue(destIndex, pFront);
                    else
                        destNode.setValue(destIndex, vfFront);
                    break;

                case FIELD_BACK:
                    if(pBack != null)
                        destNode.setValue(destIndex, pBack);
                    else
                        destNode.setValue(destIndex, vfBack);
                    break;

                case FIELD_LEFT:
                    if(pLeft != null)
                        destNode.setValue(destIndex, pLeft);
                    else
                        destNode.setValue(destIndex, vfLeft);
                    break;

                case FIELD_RIGHT:
                    if(pRight != null)
                        destNode.setValue(destIndex, pRight);
                    else
                        destNode.setValue(destIndex, vfRight);
                    break;

                case FIELD_TOP:
                    if(pTop != null)
                        destNode.setValue(destIndex, pTop);
                    else
                        destNode.setValue(destIndex, vfTop);
                    break;

                case FIELD_BOTTOM:
                    if(pBottom != null)
                        destNode.setValue(destIndex, pBottom);
                    else
                        destNode.setValue(destIndex, vfBottom);
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
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FRONT:
            case FIELD_BACK:
            case FIELD_LEFT:
            case FIELD_RIGHT:
            case FIELD_TOP:
            case FIELD_BOTTOM:
                setTextureNode(index, child);
                if(!inSetup) {
                    hasChanged[index] = true;
                    fireFieldChanged(index);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }

    /**
     * Add a single texture node to the list of textures.  Override this
     * to add render-specific behavior, bu~t remember to call this method.
     *
     * @param node The node to add
     * @param index The index of the field
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void setTextureNode(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        VRMLProtoInstance proto = null;
        VRMLTexture2DNodeType texture = null;

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl = (VRMLNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            if (!(impl instanceof VRMLTexture2DNodeType))
                throw new InvalidFieldValueException(TEXTURE_PROTO_MSG);

            proto = (VRMLProtoInstance)node;
            texture = (VRMLTexture2DNodeType)impl;
        } else {
            if (!(node instanceof VRMLTexture2DNodeType))
                throw new InvalidFieldValueException(TEXTURE_NODE_MSG);

            texture = (VRMLTexture2DNodeType)node;
        }

        switch(index) {
            case FIELD_FRONT:
                pFront = proto;
                vfFront = texture;
                break;

            case FIELD_BACK:
                pBack = proto;
                vfBack = texture;
                break;

            case FIELD_LEFT:
                pBack = proto;
                vfBack = texture;
                break;

            case FIELD_RIGHT:
                pRight = proto;
                vfRight = texture;
                break;

            case FIELD_TOP:
                pTop = proto;
                vfTop = texture;
                break;

            case FIELD_BOTTOM:
                pBottom = proto;
                vfBottom = texture;
                break;
        }
    }
}
