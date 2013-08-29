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

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Base implementation of a multi texture transform.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public abstract class BaseMultiTextureTransform extends AbstractNode
  implements VRMLTextureTransformNodeType {
    /** Message for when the proto is not a TextureTransform */
    private static final String TRANSFORM_PROTO_MSG =
        "Proto does not describe a TextureTransform object";

    /** Message for when the node in setValue() is not a TextureTransform */
    private static final String TRANSFORM_NODE_MSG =
        "Node does not describe a TextureTransform object";

    /** Field index for center */
    protected static final int FIELD_TEXTURETRANSFORM = LAST_NODE_INDEX + 1;

    /** ID of the last field index in this class */
    protected static final int LAST_MULTITEXTURETRANSFORM_INDEX = FIELD_TEXTURETRANSFORM;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_MULTITEXTURETRANSFORM_INDEX + 1;

    /* VRML Field declarations */
    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** exposedField MFNode textureTransform */
    protected ArrayList vfTextureTransform;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_TEXTURETRANSFORM, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_TEXTURETRANSFORM] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "textureTransform");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_TEXTURETRANSFORM);
        fieldMap.put("textureTransform", idx);
        fieldMap.put("set_textureTransform", idx);
        fieldMap.put("textureTransform_changed", idx);
    }

    /**
     * Construct a new default instance of this class.
     */
    protected BaseMultiTextureTransform() {
        super("MultiTextureTransform");

        vfTextureTransform = new ArrayList();
        hasChanged = new boolean[LAST_MULTITEXTURETRANSFORM_INDEX + 1];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseMultiTextureTransform(VRMLNodeType node) {
        this();

        checkNodeType(node);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        VRMLNodeType[] kids = new VRMLNodeType[vfTextureTransform.size()];
        vfTextureTransform.toArray(kids);

        for(int i=0; i < kids.length; i++)
            kids[i].setupFinished();
    }

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
        if(index < 0  || index > LAST_MULTITEXTURETRANSFORM_INDEX)
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
        return TypeConstants.TextureTransformNodeType;
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

        fieldData.clear();

        switch(index) {
            case FIELD_TEXTURETRANSFORM:
                VRMLNodeType kids[] = new VRMLNodeType[vfTextureTransform.size()];
                vfTextureTransform.toArray(kids);
                fieldData.clear();
                fieldData.nodeArrayValue = kids;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = kids.length;
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
                case FIELD_TEXTURETRANSFORM:
                    VRMLNodeType kids[] = new VRMLNodeType[vfTextureTransform.size()];
                    vfTextureTransform.toArray(kids);
                    destNode.setValue(destIndex, kids, kids.length);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("TextureTransform sendRoute: No field!" +
                ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("TextureTransform sendRoute: Invalid field Value: " +
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
        throws InvalidFieldException {

        switch(index) {
            case FIELD_TEXTURETRANSFORM:
                if(!inSetup)
                    vfTextureTransform.clear();

                if(child != null)
                    addTextureTransformNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_TEXTURETRANSFORM] = true;
                    fireFieldChanged(FIELD_TEXTURETRANSFORM);
                }
                break;

            default:
                super.setValue(index, child);
        }
    }


    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_TEXTURETRANSFORM:
                if(!inSetup)
                    vfTextureTransform.clear();

                for(int i = 0; i < children.length; i++ )
                    addTextureTransformNode(children[i]);

                if(!inSetup) {
                    hasChanged[FIELD_TEXTURETRANSFORM] = true;
                    fireFieldChanged(FIELD_TEXTURETRANSFORM);
                }

                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    /**
     * Add a single texturetransform node to the list of textures.
     * Override this to add render-specific behavior, but remember to call
     * this method.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addTextureTransformNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl = (VRMLNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            if (!(impl instanceof VRMLTextureTransformNodeType))
                throw new InvalidFieldValueException(TRANSFORM_PROTO_MSG);

            vfTextureTransform.add(impl);
        } else {
            if (!(node instanceof VRMLTextureTransformNodeType))
                throw new InvalidFieldValueException(TRANSFORM_NODE_MSG);

            vfTextureTransform.add(node);
        }
    }
}
