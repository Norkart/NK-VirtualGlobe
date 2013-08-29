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

package org.web3d.vrml.renderer.common.nodes.texture;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseTexture3DNode;

/**
 * Base implementation of a ComposedTexture3D node.
 * <p>
 *
 * The number of textures in the depth must be an even number. If the
 * value is not an even number, an error message is issued, but the set is
 * allowed to continue. Renderer-specific derived classes should work out
 * what to do with this provided texture values.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class BaseComposedTexture3D extends BaseTexture3DNode
    implements VRMLTextureListener, VRMLComposedTextureNodeType {

    /** Texture children Index */
    protected static final int FIELD_TEXTURE = LAST_3DTEXTURE_INDEX + 1;

    /** Index of the last field of this node */
    protected static final int LAST_COMPOSED_3DTEXTURE_INDEX = FIELD_TEXTURE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_COMPOSED_3DTEXTURE_INDEX + 1;

    /** Message for when the proto is not a Texture */
    private static final String TEXTURE_PROTO_MSG =
        "Proto does not describe a Texture object";

    /** Message for when the node in setValue() is not a Texture */
    private static final String TEXTURE_NODE_MSG =
        "Node does not describe a Texture object";

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // Field values

    /** exposedField MFNode texture [] */
    protected ArrayList vfTexture;

    /**
     * Temporary array used when sending a route out of the node
     * or someone is asking for the values of the field. Never
     * instantiated unless needed.
     */
    private VRMLNodeType[] kids;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_TEXTURE, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "texture");
        fieldDecl[FIELD_REPEATS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatS");
        fieldDecl[FIELD_REPEATT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatT");
        fieldDecl[FIELD_REPEATR] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatR");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_TEXTURE);
        fieldMap.put("texture", idx);
        fieldMap.put("set_texture", idx);
        fieldMap.put("texture_changed", idx);

        fieldMap.put("repeatS", new Integer(FIELD_REPEATS));
        fieldMap.put("repeatT", new Integer(FIELD_REPEATT));
        fieldMap.put("repeatR", new Integer(FIELD_REPEATR));
    }

    /**
     * Default constructor.
     */
    protected BaseComposedTexture3D() {
        super("ComposedTexture3D");
        vfTexture = new ArrayList();

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
    protected BaseComposedTexture3D(VRMLNodeType node) {
        this();

        copy((VRMLTexture3DNodeType)node);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
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

        int len = vfTexture.size();
        VRMLTextureNodeType tex;

        for(int i=0; i < len; i++) {
            tex = (VRMLTextureNodeType) vfTexture.get(i);
            tex.setupFinished();
        }
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
        if(index < 0 || index > LAST_COMPOSED_3DTEXTURE_INDEX)
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
        return TypeConstants.Texture3DNodeType;
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
            case FIELD_TEXTURE:
                if(kids == null || kids.length != vfTexture.size())
                    kids = new VRMLNodeType[vfTexture.size()];
                vfTexture.toArray(kids);
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
                case FIELD_TEXTURE:
                    if(kids == null || kids.length != vfTexture.size())
                        kids = new VRMLNodeType[vfTexture.size()];

                    vfTexture.toArray(kids);
                    destNode.setValue(destIndex, kids, kids.length);
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
            case FIELD_TEXTURE:
                if(!inSetup) {
                    vfTexture.clear();
                    textureDepth = 0;
                }

                if(child != null) {
                    textureDepth++;
                    addTextureNode(child);
                }

                if(!inSetup) {
                    hasChanged[FIELD_TEXTURE] = true;
                    fireFieldChanged(FIELD_TEXTURE);
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
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TEXTURE:
                if(!inSetup) {
                    vfTexture.clear();
                    textureDepth = 0;
                }

                if((numValid % 2) != 0)
                    System.out.println("ComposedTexture3D: Number of " +
                                       "textures not multiple of 2");

                for(int i = 0; i < numValid; i++ ) {
                    textureDepth++;
                    addTextureNode(children[i]);
                }

                if(!inSetup) {
                    hasChanged[FIELD_TEXTURE] = true;
                    fireFieldChanged(FIELD_TEXTURE);
                }

                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureNodeType
    //----------------------------------------------------------

    /**
     * Get a string for cacheing this object.  Null means do not cache this
     * texture.
     *
     * @param stage The stage number,  0 for all single stage textures.
     * @return A string to use in lookups.  Typically the url loaded.
     */
    public String getCacheString(int stage) {
        VRMLTextureNodeType tex = (VRMLTextureNodeType)vfTexture.get(stage);

        if(tex == null)
            return null;

        return tex.getCacheString(0);
    }

    //---------------------------------------------------------------
    // Methods defined by VRMLComposedTextureNodeType
    //---------------------------------------------------------------

    /**
     * Get the number of textures in this ComposedTexture node.
     *
     * @return The number of active textures.
     */
    public int getNumberTextures() {
        return vfTexture.size();
    }

    /**
     * Get the textures which make up this composed texture.
     *
     * @param start Where in the array to start filling in textures.
     * @param texs The preallocated array to return texs in.  Error if too small.
     */
    public void getTextures(int start, VRMLTextureNodeType[] texs) {
        int len = vfTexture.size();
        for(int i=0; i < len; i++) {
            texs[i + start] = (VRMLTextureNodeType) vfTexture.get(i);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureListener
    //----------------------------------------------------------

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The image idx which changed.
     * @param node The texture which changed.
     * @param image The image for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    NIOBufferImage image,
                                    String url) {
        // Find the appropriate sub-texture that got loaded here.
        // Do a simple source match
        VRMLTextureNodeType tex;

        int found_idx = vfTexture.indexOf(node);

        if(found_idx != -1)
            fireTextureImageChanged(found_idx, node, image, url);
    }

    /**
     * Invoked when all of the underlying images have changed.
     *
     * @param len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images as data buffers for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    NIOBufferImage[] image,
                                    String[] url) {
        // Not implemented yet.
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * efficient route is to set the parameters before the image.
     *
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  An array of 3
     *    component colors.
     */
    public void textureParamsChanged(int idx, int mode,
        int source, int function, float alpha, float[] color) {

        // Will never be called by children of multitexture
    }

    /**
     * Invoked when the texture parameters have changed.  The most
     * efficient route is to set the parameters before the image.
     *
     * @len The number of valid entries in the arrays.
     * @param idx The texture index which changed.
     * @param mode The mode for the stage.
     * @param source The source for the stage.
     * @param function The function to apply to the stage values.
     * @param alpha The alpha value to use for modes requiring it.
     * @param color The color to use for modes requiring it.  An array of 3
     *    component colors.
     */
    public void textureParamsChanged(int len, int mode[],
        int[] source, int[] function, float alpha, float[] color) {

        // Will never be called by children of multitexture
    }

    /**
     * Add a single texture node to the list of textures.  Override this
     * to add render-specific behavior, but remember to call this method.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addTextureNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl = (VRMLNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            if (!(impl instanceof VRMLTextureNodeType))
                throw new InvalidFieldValueException(TEXTURE_PROTO_MSG);

            vfTexture.add(impl);
            ((VRMLTextureNodeType)node).addTextureListener(this);
        } else {
            if (!(node instanceof VRMLTextureNodeType))
                throw new InvalidFieldValueException(TEXTURE_NODE_MSG);

            vfTexture.add(node);
            ((VRMLTextureNodeType)node).addTextureListener(this);
        }
    }
}
