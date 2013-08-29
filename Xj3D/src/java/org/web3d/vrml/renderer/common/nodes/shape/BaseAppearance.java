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

package org.web3d.vrml.renderer.common.nodes.shape;

// External imports
import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.image.NIOBufferImage;

import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.*;

import org.web3d.util.MathUtils;
import org.web3d.util.PropertyTools;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of an Appearance node.
 * <p>
 *
 *
 * <b>Properties</b>
 * <p>
 * The properties for this file are loaded once on startup so they cannot be
 * initialized per run of loader.
 *
 * The following properties are used by this class:
 * <ul>
 * <li><code>org.web3d.vrml.renderer.common.nodes.shape.useTextureCache</code> Whether
 *    to cache textures for later reuse.  This caches by URL which might be incorrect
 *    for some dynamic systems.  It improve performance for files which don't use
 *    DEF/USE for textures.</li>
 * <li><code>org.web3d.vrml.renderer.common.nodes.shape.useMipMaps</code> Force the
 *    use of mipmaps on nodes without TextureProperties.</li>
 * </ul>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.43 $
 */
public abstract class BaseAppearance extends AbstractNode
    implements VRMLAppearanceNodeType, VRMLTextureListener {

    /** Index of the Material field value */
    protected static final int FIELD_MATERIAL = LAST_NODE_INDEX + 1;

    /** Index of the Texture field value */
    protected static final int FIELD_TEXTURE = LAST_NODE_INDEX + 2;

    /** Index of the TextureTransform field value */
    protected static final int FIELD_TEXTURE_TRANSFORM = LAST_NODE_INDEX + 3;

    /** Index of the lineProperties field value */
    protected static final int FIELD_LINE_PROPERTIES = LAST_NODE_INDEX + 4;

    /** Index of the pointProperties field value */
    protected static final int FIELD_POINT_PROPERTIES = LAST_NODE_INDEX + 5;

    /** Index of the fillProperties field value */
    protected static final int FIELD_FILL_PROPERTIES = LAST_NODE_INDEX + 6;

    /** Index of the fillProperties field value */
    protected static final int FIELD_TEXTURE_PROPERTIES = LAST_NODE_INDEX + 7;

    /** The default useTextureCache value */
    protected static final boolean DEFAULT_USETEXTURECACHE = true;

    /** Property describing the rescalling method to use */
    protected static final String USETEXTURECACHE_PROP =
        "org.web3d.vrml.renderer.common.nodes.shape.useTextureCache";

    /** The value read from the system property for TEXTURECACHE */
    protected static final boolean useTextureCache;

    /** The default useMipMaps value */
    protected static final boolean DEFAULT_USE_MIPMAPS = false;

    /** Property describing the mipmap useage */
    protected static final String USE_MIPMAPS_PROP =
        "org.web3d.vrml.renderer.common.nodes.shape.useMipMaps";

    /** The value read from the system property for MIPMAPS */
    protected static boolean useMipMaps;

    /** The default anisotropicDegree value */
    protected static final int DEFAULT_ANISOTROPIC_DEGREE = 1;

    /** Property describing the anisotropicDegree value */
    protected static final String ANISOTROPIC_DEGREE_PROP =
        "org.web3d.vrml.renderer.common.nodes.shape.anisotropicDegree";

    /** The anisotropicDegree value */
    protected static final int anisotropicDegree;

    // VRML Field declarations

    /** Number of fields constant */
    private static final int NUM_FIELDS = FIELD_TEXTURE_PROPERTIES + 1;

    /** Message for when the proto is not a Material */
    protected static final String MATERIAL_PROTO_MSG =
        "Proto does not describe a Material object";

    /** Message for when the node in setValue() is not a Material */
    protected static final String MATERIAL_NODE_MSG =
        "Node does not describe a Material object";

    /** Message for when the proto is not a Texture */
    protected static final String TEXTURE_PROTO_MSG =
        "Proto does not describe a Texture object";

    /** Message for when the node in setValue() is not a Texture */
    protected static final String TEXTURE_NODE_MSG =
        "Node does not describe a Texture object";

    /** Message for when the proto is not a TextureTransform */
    protected static final String TRANSFORM_PROTO_MSG =
        "Proto does not describe a TextureTransform object";

    /** Message for when the node in setValue() is not a TextureTransform */
    protected static final String TRANSFORM_NODE_MSG =
        "Node does not describe a TextureTransform object";

    /** Message for when the proto is not a FillProperties */
    protected static final String FILL_PROP_PROTO_MSG =
        "Proto does not describe a FillProperties object";

    /** Message for when the node in setValue() is not a FillProperties */
    protected static final String FILL_PROP_NODE_MSG =
        "Node does not describe a FillProperties object";

    /** Message for when the proto is not a LineProperties */
    protected static final String LINE_PROP_PROTO_MSG =
        "Proto does not describe a LineProperties object";

    /** Message for when the node in setValue() is not a LineProperties */
    protected static final String LINE_PROP_NODE_MSG =
        "Node does not describe a LineProperties object";

    /** Message for when the proto is not a PointProperties */
    protected static final String POINT_PROP_PROTO_MSG =
        "Proto does not describe a PointProperties object";

    /** Message for when the node in setValue() is not a PointProperties */
    protected static final String POINT_PROP_NODE_MSG =
        "Node does not describe a PointProperties object";


    /** Message for when the node in setValue() is not a TextureProperties */
    protected static final String TEXTURE_PROP_NODE_MSG =
        "Node does not describe a TextureProperties object";

    /** Message for when the proto is not a TextureProperties */
    protected static final String TEXTURE_PROP_PROTO_MSG =
        "Proto does not describe a TextureProperties object";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Proto version of the material */
    protected VRMLProtoInstance pMaterial;

    /** SFNode material */
    protected VRMLMaterialNodeType vfMaterial;

    /** Proto version of the texture */
    protected VRMLProtoInstance pTexture;

    /** SFNode texture. */
    protected VRMLTextureNodeType vfTexture;

    /** Proto version of the texture transform */
    protected VRMLProtoInstance pTextureTransform;

    /** SFNode textureTransform */
    protected VRMLTextureTransformNodeType vfTextureTransform;

    /** Proto version of the fillProperties */
    protected VRMLProtoInstance pFillProperties;

    /** SFNode fillProperties */
    protected VRMLAppearanceChildNodeType vfFillProperties;

    /** Proto version of the lineProperties */
    protected VRMLProtoInstance pLineProperties;

    /** SFNode lineProperties. */
    protected VRMLAppearanceChildNodeType vfLineProperties;

    /** Proto version of the pointProperties */
    protected VRMLProtoInstance pPointProperties;

    /** SFNode pointProperties. */
    protected VRMLPointPropertiesNodeType vfPointProperties;

    /** Proto version of the textureProperties */
    protected VRMLProtoInstance pTextureProperties;

    /** SFNode textureProperties.  */
    protected VRMLTextureProperties2DNodeType vfTextureProperties;

    /**
     * Color listener for passing back emissive changes. Keep a local
     * collection of them here so that we can add and remove them as a group
     * as the material node instance changes.
     */
    private ArrayList colorListeners;

    protected int numStages;
    protected TextureStage[] stages;
    protected String[] urls;

    /** Texture Statistics */

    /** Total size of textures (K bytes) */
    protected static float totalTextureSize;

    // Static constructor
    static {
        nodeFields = new int[] {
            FIELD_MATERIAL,
            FIELD_TEXTURE,
            FIELD_TEXTURE_TRANSFORM,
            FIELD_LINE_PROPERTIES,
            FIELD_POINT_PROPERTIES,
            FIELD_FILL_PROPERTIES,
            FIELD_TEXTURE_PROPERTIES,
            FIELD_METADATA
            };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "metadata");
        fieldDecl[FIELD_MATERIAL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "material");
        fieldDecl[FIELD_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "texture");
        fieldDecl[FIELD_TEXTURE_TRANSFORM] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "textureTransform");
        fieldDecl[FIELD_LINE_PROPERTIES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "lineProperties");
        fieldDecl[FIELD_POINT_PROPERTIES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "pointProperties");
        fieldDecl[FIELD_FILL_PROPERTIES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "fillProperties");
        fieldDecl[FIELD_TEXTURE_PROPERTIES] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
            "SFNode",
            "textureProperties");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_MATERIAL);
        fieldMap.put("material", idx);
        fieldMap.put("set_material", idx);
        fieldMap.put("material_changed", idx);

        idx = new Integer(FIELD_TEXTURE);
        fieldMap.put("texture", idx);
        fieldMap.put("set_texture", idx);
        fieldMap.put("texture_changed", idx);

        idx = new Integer(FIELD_TEXTURE_TRANSFORM);
        fieldMap.put("textureTransform", idx);
        fieldMap.put("set_textureTransform", idx);
        fieldMap.put("textureTransform_changed", idx);

        idx = new Integer(FIELD_LINE_PROPERTIES);
        fieldMap.put("lineProperties", idx);
        fieldMap.put("set_lineProperties", idx);
        fieldMap.put("lineProperties_changed", idx);

        idx = new Integer(FIELD_POINT_PROPERTIES);
        fieldMap.put("pointProperties", idx);
        fieldMap.put("set_pointProperties", idx);
        fieldMap.put("pointProperties_changed", idx);

        idx = new Integer(FIELD_FILL_PROPERTIES);
        fieldMap.put("fillProperties", idx);
        fieldMap.put("set_fillProperties", idx);
        fieldMap.put("fillProperties_changed", idx);

        idx = new Integer(FIELD_TEXTURE_PROPERTIES);
        fieldMap.put("textureProperties", idx);
        fieldMap.put("set_textureProperties", idx);
        fieldMap.put("textureProperties_changed", idx);

        useMipMaps = PropertyTools.fetchSystemProperty(USE_MIPMAPS_PROP,
            DEFAULT_USE_MIPMAPS);

        anisotropicDegree = PropertyTools.fetchSystemProperty(ANISOTROPIC_DEGREE_PROP,
            DEFAULT_ANISOTROPIC_DEGREE);

        useTextureCache = PropertyTools.fetchSystemProperty(USETEXTURECACHE_PROP,
            DEFAULT_USETEXTURECACHE);
    }

    /**
     * Default constructor that initialises all fields to the spec defaults.
     */
    protected BaseAppearance() {
        super("Appearance");

        stages = new TextureStage[0];
        urls = new String[0];
        numStages = 0;
        totalTextureSize = 0;

        hasChanged = new boolean[NUM_FIELDS];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Appearance node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseAppearance(VRMLNodeType node) {
        this();

        checkNodeType(node);
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
    public void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

        if (inSetup)
            return;

        super.notifyExternProtoLoaded(index, node);
        int[] stypes = node.getSecondaryType();
        boolean ok = false;

        switch(index) {
        case FIELD_MATERIAL:
            checkSecondaryType(node,
                TypeConstants.MaterialNodeType,
                MATERIAL_PROTO_MSG);

            pMaterial = (VRMLProtoInstance) node;
            vfMaterial = (VRMLMaterialNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            if((vfMaterial != null) && (colorListeners != null)) {
                for(int i = 0; i < colorListeners.size(); i++) {
                    MaterialColorListener l =
                        (MaterialColorListener)colorListeners.get(i);

                    vfMaterial.addMaterialColorListener(l);
                }
            }

            break;

        case FIELD_LINE_PROPERTIES:
            checkSecondaryType(node,
                TypeConstants.AppearanceChildNodeType,
                LINE_PROP_PROTO_MSG);

            pLineProperties = (VRMLProtoInstance) node;
            vfLineProperties = (VRMLAppearanceChildNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            break;
        case FIELD_POINT_PROPERTIES:
            checkSecondaryType(node,
                TypeConstants.PointPropertiesNodeType,
                POINT_PROP_PROTO_MSG);

            pPointProperties = (VRMLProtoInstance) node;
            vfPointProperties = (VRMLPointPropertiesNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            break;

        case FIELD_FILL_PROPERTIES:
            checkSecondaryType(node,
                TypeConstants.AppearanceChildNodeType,
                FILL_PROP_PROTO_MSG);

            pFillProperties = (VRMLProtoInstance) node;
            vfFillProperties = (VRMLAppearanceChildNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            break;

        case FIELD_TEXTURE:
            checkSecondaryType(node,
                TypeConstants.TextureNodeType,
                TEXTURE_PROTO_MSG);

            pTexture = (VRMLProtoInstance) node;
            vfTexture = (VRMLTextureNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            break;

        case FIELD_TEXTURE_TRANSFORM:
            for(int i = 0; i < stypes.length; i++) {
                if(stypes[i] == TypeConstants.TextureTransformNodeType) {
                    ok = true;
                    break;
                }
            }

            if(!ok)
                throw new InvalidFieldValueException(TRANSFORM_PROTO_MSG);

            pTextureTransform = (VRMLProtoInstance) node;
            vfTextureTransform = (VRMLTextureTransformNodeType)
                ((VRMLProtoInstance)node).getImplementationNode();

            break;

        default:
            System.out.println("BaseAppearance: " +
                "Unknown field for notifyExternProtoLoaded");
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextureListener
    //----------------------------------------------------------

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The stage which changed.
     * @param node The texture which changed.
     * @param image The image for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(
        int idx,
        VRMLNodeType node,
        NIOBufferImage image,
        String url ) {

        insureStageSize(idx + 1, true);

        int type = vfTexture.getTextureType();
        switch(type) {
        case TextureConstants.TYPE_SINGLE_2D:
        case TextureConstants.TYPE_MULTI:

            stages[idx].images = processImage( idx, image, url );
            urls[idx] = url;
            break;

        case TextureConstants.TYPE_SINGLE_3D:

            // TODO: Need to account for alpha texture
            // int i = (alphaTexture == null) ? 0 : 1;
            int i = 0;
            if (stages[i].images.length < idx+1) {
                stages[i].images = new NIOBufferImage[idx+1];
            }
            NIOBufferImage[] img = processImage( idx, image, url );

            if((img != null) && (img.length != 0)) {
                stages[i].images[idx] = img[0];
            }
            urls[i] = null;
            break;
        }
    }

    /**
     * Invoked when all of the underlying images have changed.
     *
     * @len The number of valid entries in the image array.
     * @param node The textures which changed.
     * @param image The images for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(
        int len,
        VRMLNodeType[] node,
        NIOBufferImage[] image,
        String[] url ) {

        insureStageSize( len, true );

        for( int i=0; i < len; i++ ) {
            stages[i].images = processImage( i, image[i], url[i] );
            urls[i] = url[i];
        }
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
     * @param color The color to use for modes requiring it.  3 Component color.
     */
    public void textureParamsChanged(int idx, int mode,
        int source, int function, float alpha, float[] color) {

        insureStageSize(idx + 1, false);
        stages[idx].mode = mode;
        stages[idx].source = source;
        stages[idx].function = function;
        stages[idx].alpha = alpha;
        stages[idx].color[0] = color[0];
        stages[idx].color[1] = color[1];
        stages[idx].color[2] = color[2];
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

        insureStageSize(len, false);

        for(int i=0; i < len; i++) {
            stages[i].mode = mode[i];
            stages[i].source = source[i];
            stages[i].function = function[i];
            stages[i].alpha = alpha;
            stages[i].color[0] = color[0];
            stages[i].color[1] = color[1];
            stages[i].color[2] = color[2];
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLAppearanceNodeType
    //----------------------------------------------------------

    /**
     * Set the material that should be used for this appearance. Setting a
     * value of null will clear the current material.
     *
     * @param mat The new material instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setMaterial(VRMLNodeType mat)
        throws InvalidFieldValueException {

        VRMLMaterialNodeType node;
        VRMLNodeType old_node;

        if(pMaterial != null)
            old_node = pMaterial;
        else
            old_node = vfMaterial;

        if (mat instanceof VRMLProtoInstance) {
            node = (VRMLMaterialNodeType)
                ((VRMLProtoInstance)mat).getImplementationNode();
            pMaterial = (VRMLProtoInstance) mat;
            if ((node != null) && !(node instanceof VRMLMaterialNodeType)) {
                throw new InvalidFieldValueException(MATERIAL_PROTO_MSG);
            }
        }
        else if (mat != null &&
            (!(mat instanceof VRMLMaterialNodeType))) {
            throw new InvalidFieldValueException(MATERIAL_NODE_MSG);
        }
        else {
            pMaterial = null;
            node = (VRMLMaterialNodeType) mat;
        }

        vfMaterial = (VRMLMaterialNodeType)node;

        if(mat != null)
            updateRefs(mat, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null) {
                stateManager.registerRemovedNode(old_node);

                if(colorListeners != null) {
                    for(int i = 0; i < colorListeners.size(); i++) {
                        MaterialColorListener l =
                            (MaterialColorListener)colorListeners.get(i);

                        vfMaterial.removeMaterialColorListener(l);
                    }
                }
            }

            if(mat != null)
                stateManager.registerAddedNode(mat);

            hasChanged[FIELD_MATERIAL] = true;
            fireFieldChanged(FIELD_MATERIAL);
        }

        if((vfMaterial != null) && (colorListeners != null)) {
            for(int i = 0; i < colorListeners.size(); i++) {
                MaterialColorListener l =
                    (MaterialColorListener)colorListeners.get(i);

                vfMaterial.addMaterialColorListener(l);
            }
        }
    }

    /**
     * Get the current material used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set Material
     */
    public VRMLNodeType getMaterial() {
        if (pMaterial != null)
            return pMaterial;
        else
            return vfMaterial;
    }

    /**
     * Set the texture that should be used for this appearance. Setting a
     * value of null will clear the current texture.
     *
     * @param tex The new texture instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setTexture(VRMLNodeType tex)
        throws InvalidFieldValueException {

        VRMLTextureNodeType node;
        VRMLNodeType old_node;

        if(pTexture != null)
            old_node = pTexture;
        else
            old_node = vfTexture;

        if (tex instanceof VRMLProtoInstance) {
            node = (VRMLTextureNodeType)
                ((VRMLProtoInstance)tex).getImplementationNode();
            pTexture = (VRMLProtoInstance) tex;
            if ((node != null) && !(node instanceof VRMLTextureNodeType)) {
                throw new InvalidFieldValueException(TEXTURE_PROTO_MSG);
            }
        }
        else if (tex != null &&
            (!(tex instanceof VRMLTextureNodeType))) {
            throw new InvalidFieldValueException(TEXTURE_NODE_MSG);
        }
        else {
            pTexture = null;
            node = (VRMLTextureNodeType) tex;
        }

        vfTexture = (VRMLTextureNodeType)node;

        if(tex != null)
            updateRefs(tex, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if (vfTexture != null) {
                switch(vfTexture.getTextureType()) {
                case TextureConstants.TYPE_SINGLE_2D:

                    NIOBufferImage niobi = ((VRMLTexture2DNodeType)vfTexture).getImage();
                    if ( niobi != null ) {
                        insureStageSize( 1, true );
                        stages[0].images = processImage( 0, niobi, null );
                        urls[0] = null;
                    }
                    break;
                }
            }

            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(tex != null)
                stateManager.registerAddedNode(tex);

            hasChanged[FIELD_TEXTURE] = true;
            fireFieldChanged(FIELD_TEXTURE);
        }
    }

    /**
     * Get the current texture used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set Texture
     */
    public VRMLNodeType getTexture() {
        if (pTexture != null)
            return pTexture;
        else
            return vfTexture;
    }

    /**
     * Set the texture transform that should be used for this appearance.
     * Setting a value of null will clear the current texture transform.
     *
     * @param trans The new texture transform instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type
     */
    public void setTextureTransform(VRMLNodeType trans)
        throws InvalidFieldValueException {

        VRMLTextureTransformNodeType node;
        VRMLNodeType old_node;

        if(pTextureTransform != null)
            old_node = pTextureTransform;
        else
            old_node = vfTextureTransform;

        if (trans instanceof VRMLProtoInstance) {
            node = (VRMLTextureTransformNodeType)
                ((VRMLProtoInstance)trans).getImplementationNode();
            pTextureTransform = (VRMLProtoInstance) trans;
            if ((node != null) && !(node instanceof VRMLTextureTransformNodeType)) {
                throw new InvalidFieldValueException(TRANSFORM_PROTO_MSG);
            }
        }
        else if (trans != null &&
            (!(trans instanceof VRMLTextureTransformNodeType))) {
            throw new InvalidFieldValueException(TRANSFORM_NODE_MSG);
        }
        else {
            pTextureTransform = null;
            node = (VRMLTextureTransformNodeType) trans;
        }

        vfTextureTransform = (VRMLTextureTransformNodeType)node;

        if(trans != null)
            updateRefs(trans, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(trans != null)
                stateManager.registerAddedNode(trans);

            hasChanged[FIELD_TEXTURE_TRANSFORM] = true;
            fireFieldChanged(FIELD_TEXTURE_TRANSFORM);
        }
    }

    /**
     * Get the current texture transform used by the appearance. If none has
     * been set this will return null.
     *
     * @return The currently set TextureTransform
     */
    public VRMLNodeType getTextureTransform() {
        if (pTextureTransform != null)
            return pTextureTransform;
        else
            return vfTextureTransform;
    }

    /**
     * Set the line properties that should be used for this appearance. Setting a
     * value of null will clear the current property.
     *
     * @param prop The new property instance instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setLineProperties(VRMLNodeType prop)
        throws InvalidFieldValueException {

        VRMLAppearanceChildNodeType node;
        VRMLNodeType old_node;

        if(pLineProperties != null)
            old_node = pLineProperties;
        else
            old_node = vfLineProperties;

        if (prop instanceof VRMLProtoInstance) {
            node = (VRMLAppearanceChildNodeType)
                ((VRMLProtoInstance)prop).getImplementationNode();
            pLineProperties = (VRMLProtoInstance) prop;
            if ((node != null) && !(node instanceof VRMLAppearanceChildNodeType)) {
                throw new InvalidFieldValueException(LINE_PROP_PROTO_MSG);
            }
        }
        else if (prop != null &&
            (!(prop instanceof VRMLAppearanceChildNodeType))) {
            throw new InvalidFieldValueException(LINE_PROP_NODE_MSG);
        }
        else {
            pLineProperties = null;
            node = (VRMLAppearanceChildNodeType) prop;
        }

        vfLineProperties = (VRMLAppearanceChildNodeType)node;

        if(prop != null)
            updateRefs(prop, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(prop != null)
                stateManager.registerAddedNode(prop);

            hasChanged[FIELD_LINE_PROPERTIES] = true;
            fireFieldChanged(FIELD_LINE_PROPERTIES);
        }
    }

    /**
     * Get the current line properties used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set LineProperties
     */
    public VRMLNodeType getLineProperties() {
        if (pLineProperties != null)
            return pLineProperties;
        else
            return vfLineProperties;
    }

    /**
     * Set the point properties that should be used for this appearance. Setting a
     * value of null will clear the current property.
     *
     * @param prop The new property instance instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setPointProperties(VRMLNodeType prop)
        throws InvalidFieldValueException {

        VRMLAppearanceChildNodeType node;
        VRMLNodeType old_node;

        if(pPointProperties != null)
            old_node = pPointProperties;
        else
            old_node = vfPointProperties;

        if (prop instanceof VRMLProtoInstance) {
            node = (VRMLAppearanceChildNodeType)
                ((VRMLProtoInstance)prop).getImplementationNode();
            pPointProperties = (VRMLProtoInstance) prop;
            if ((node != null) && !(node instanceof VRMLAppearanceChildNodeType)) {
                throw new InvalidFieldValueException(POINT_PROP_PROTO_MSG);
            }
        }
        else if (prop != null &&
            (!(prop instanceof VRMLAppearanceChildNodeType))) {
            throw new InvalidFieldValueException(POINT_PROP_NODE_MSG);
        }
        else {
            pPointProperties = null;
            node = (VRMLAppearanceChildNodeType) prop;
        }

        vfPointProperties = (VRMLPointPropertiesNodeType)node;

        if(prop != null)
            updateRefs(prop, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(prop != null)
                stateManager.registerAddedNode(prop);

            hasChanged[FIELD_POINT_PROPERTIES] = true;
            fireFieldChanged(FIELD_POINT_PROPERTIES);
        }
    }

    /**
     * Get the current point properties used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set PointProperties
     */
    public VRMLNodeType getPointProperties() {
        if (pPointProperties != null)
            return pPointProperties;
        else
            return vfPointProperties;
    }

    /**
     * Set the textureProperties that should be used for this appearance. Setting a
     * value of null will clear the current texture Properties.
     *
     * @param prop The new textureProperties instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setTextureProperties(VRMLNodeType prop)
        throws InvalidFieldValueException {

        VRMLAppearanceChildNodeType node;
        VRMLNodeType old_node;

        if(pTextureProperties != null)
            old_node = pTextureProperties;
        else
            old_node = vfTextureProperties;

        if (prop instanceof VRMLProtoInstance) {
            node = (VRMLAppearanceChildNodeType)
                ((VRMLProtoInstance)prop).getImplementationNode();
            pTextureProperties = (VRMLProtoInstance) prop;
            if ((node != null) && !(node instanceof VRMLTextureProperties2DNodeType)) {
                throw new InvalidFieldValueException(TEXTURE_PROP_PROTO_MSG);
            }
        }
        else if (prop != null &&
            (!(prop instanceof VRMLTextureProperties2DNodeType))) {
            throw new InvalidFieldValueException(TEXTURE_PROP_NODE_MSG);
        }
        else {
            pTextureProperties = null;
            node = (VRMLTextureProperties2DNodeType) prop;
        }

        vfTextureProperties = (VRMLTextureProperties2DNodeType)node;

        if(prop != null)
            updateRefs(prop, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(prop != null)
                stateManager.registerAddedNode(prop);

            hasChanged[FIELD_TEXTURE_PROPERTIES] = true;
            fireFieldChanged(FIELD_TEXTURE_PROPERTIES);
        }
    }

    /**
     * Get the current texture properties used by the appearance. If none has
     * been set this will return null.
     *
     * @return The currently set TextureProperties
     */
    public VRMLNodeType getTextureProperties() {
        if (pTextureProperties != null)
            return pTextureProperties;
        else
            return vfTextureProperties;
    }

    /**
     * Set the fillProperties that should be used for this appearance. Setting a
     * value of null will clear the current property.
     *
     * @param prop The new fillProperties instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setFillProperties(VRMLNodeType prop)
        throws InvalidFieldValueException {

        VRMLAppearanceChildNodeType node;
        VRMLNodeType old_node;

        if(pFillProperties != null)
            old_node = pFillProperties;
        else
            old_node = vfFillProperties;

        if (prop instanceof VRMLProtoInstance) {
            node = (VRMLAppearanceChildNodeType)
                ((VRMLProtoInstance)prop).getImplementationNode();
            pFillProperties = (VRMLProtoInstance) prop;
            if ((node != null) && !(node instanceof VRMLAppearanceChildNodeType)) {
                throw new InvalidFieldValueException(FILL_PROP_PROTO_MSG);
            }
        }
        else if (prop != null &&
            (!(prop instanceof VRMLAppearanceChildNodeType))) {
            throw new InvalidFieldValueException(FILL_PROP_NODE_MSG);
        }
        else {
            pFillProperties = null;
            node = (VRMLAppearanceChildNodeType) prop;
        }

        vfFillProperties = (VRMLAppearanceChildNodeType)node;

        if(prop != null)
            updateRefs(prop, true);

        if(old_node != null)
            updateRefs(old_node, false);

        if(!inSetup) {
            if(old_node != null)
                stateManager.registerRemovedNode(old_node);

            if(prop != null)
                stateManager.registerAddedNode(prop);

            hasChanged[FIELD_FILL_PROPERTIES] = true;
            fireFieldChanged(FIELD_FILL_PROPERTIES);
        }
    }

    /**
     * Get the current texture used by the appearance. If none has been set
     * this will return null.
     *
     * @return The currently set FillProperties
     */
    public VRMLNodeType getFillProperties() {
        if (pFillProperties != null)
            return pFillProperties;
        else
            return vfFillProperties;
    }

    /**
     * Specify whether an object is solid. The default is true. This will
     * determine if we do backface culling and flip backface normals.
     * Can only be set during setup
     *
     * @param solid Whether the object is solid
     */
    public void setSolid(boolean solid) {
    }

    /**
     * Set whether lighting will be used for this appearance.  In general
     * you should let the material node decide this.  Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting
     * be turned off.  This method will notify the related Material node for
     * this Appearance.
     *
     * @param enable Whether lighting is enabled
     */
    public void setLightingEnabled(boolean enable) {
    }

    /**
     * Specify whether the geometry's triangles are in counter clockwise order
     * (the default) or clockwise. The default is true. This will
     * determine if we do backface culling and flip backface normals.
     * Can only be set during setup
     *
     * @param ccw True for counter-clockwise ordering
     */
    public void setCCW(boolean ccw) {
    }

    /**
     * Set whether the geometry has local colors to override the diffuse color.
     * If the local color contains alpha values we want to ignore the
     * material's transparency values.
     *
     * @param enable Whether local color is enabled
     * @param hasAlpha true with the local color also contains alpha valuess
     */
    public void setLocalColor(boolean enable, boolean hasAlpha) {
        // Ignored as the default
    }

    /**
     * Add a listener instance for the material color change notifications.
     * Adding the same instance more than once is ignored. Adding null values
     * are ignored.
     *
     * @param l The new instance to add
     */
    public void addMaterialColorListener(MaterialColorListener l) {
        if(vfMaterial != null)
            vfMaterial.addMaterialColorListener(l);

        if(colorListeners == null)
            colorListeners = new ArrayList();

        if(!colorListeners.contains(l))
            colorListeners.add(l);
    }

    /**
     * Remove a listener instance from this node. If the listener is not
     * currently registered, the request is silently ignored.
     *
     * @param l The new instance to remove
     */
    public void removeMaterialColorListener(MaterialColorListener l) {
        if(vfMaterial != null)
            vfMaterial.removeMaterialColorListener(l);

        colorListeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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

        if(pMaterial != null)
            pMaterial.setupFinished();
        else if(vfMaterial != null)
            vfMaterial.setupFinished();

        if(pTexture != null)
            pTexture.setupFinished();
        else if(vfTexture != null)
            vfTexture.setupFinished();

        if(pTextureTransform != null)
            pTextureTransform.setupFinished();
        else if(vfTextureTransform != null)
            vfTextureTransform.setupFinished();

        if(pLineProperties != null)
            pLineProperties.setupFinished();
        else if(vfLineProperties != null)
            vfLineProperties.setupFinished();

        if(pPointProperties != null)
            pPointProperties.setupFinished();
        else if(vfPointProperties != null)
            vfPointProperties.setupFinished();

        if(pFillProperties != null)
            pFillProperties.setupFinished();
        else if(vfFillProperties != null)
            vfFillProperties.setupFinished();

        if(pTextureProperties != null)
            pTextureProperties.setupFinished();
        else if(vfTextureProperties != null)
            vfTextureProperties.setupFinished();

        if (vfTexture != null)
            processImages(0,vfTexture);
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
        if (index < 0  || index > NUM_FIELDS - 1)
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
        return TypeConstants.AppearanceNodeType;
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

        VRMLFieldData fieldData = (VRMLFieldData) fieldLocalData.get();

        switch(index) {
        case FIELD_MATERIAL:
            fieldData.clear();
            if(pMaterial != null)
                fieldData.nodeValue = pMaterial;
            else
                fieldData.nodeValue = vfMaterial;
            fieldData.dataType = VRMLFieldData.NODE_DATA;
            break;

        case FIELD_TEXTURE:
            fieldData.clear();
            if(pTexture != null)
                fieldData.nodeValue = pTexture;
            else
                fieldData.nodeValue = vfTexture;
            fieldData.dataType = VRMLFieldData.NODE_DATA;
            break;

        case FIELD_TEXTURE_TRANSFORM:
            fieldData.clear();
            if(pTextureTransform != null)
                fieldData.nodeValue = pTextureTransform;
            else
                fieldData.nodeValue = vfTextureTransform;
            fieldData.dataType = VRMLFieldData.NODE_DATA;
            break;

        case FIELD_LINE_PROPERTIES:
            fieldData.clear();
            if(pLineProperties != null)
                fieldData.nodeValue = pLineProperties;
            else
                fieldData.nodeValue = vfLineProperties;
            fieldData.dataType = VRMLFieldData.NODE_DATA;
            break;

        case FIELD_POINT_PROPERTIES:
            fieldData.clear();
            if(pPointProperties != null)
                fieldData.nodeValue = pPointProperties;
            else
                fieldData.nodeValue = vfPointProperties;
            fieldData.dataType = VRMLFieldData.NODE_DATA;
            break;

        case FIELD_TEXTURE_PROPERTIES:
            fieldData.clear();
            if(pTextureProperties != null)
                fieldData.nodeValue = pTextureProperties;
            else
                fieldData.nodeValue = vfTextureProperties;
            fieldData.dataType = VRMLFieldData.NODE_DATA;
            break;

        case FIELD_FILL_PROPERTIES:
            fieldData.clear();
            if(pFillProperties != null)
                fieldData.nodeValue = pFillProperties;
            else
                fieldData.nodeValue = vfFillProperties;
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
            case FIELD_MATERIAL:
                if(pMaterial != null)
                    destNode.setValue(destIndex, pMaterial);
                else
                    destNode.setValue(destIndex, vfMaterial);
                break;

            case FIELD_TEXTURE:
                if(pTexture != null)
                    destNode.setValue(destIndex, pTexture);
                else
                    destNode.setValue(destIndex, vfTexture);
                break;

            case FIELD_TEXTURE_TRANSFORM:
                if(pTextureTransform != null)
                    destNode.setValue(destIndex, pTextureTransform);
                else
                    destNode.setValue(destIndex, vfTextureTransform);
                break;

            case FIELD_LINE_PROPERTIES:
                if(pLineProperties != null)
                    destNode.setValue(destIndex, pLineProperties);
                else
                    destNode.setValue(destIndex, vfLineProperties);
                break;

            case FIELD_POINT_PROPERTIES:
                if(pPointProperties != null)
                    destNode.setValue(destIndex, pPointProperties);
                else
                    destNode.setValue(destIndex, vfPointProperties);
                break;

            case FIELD_TEXTURE_PROPERTIES:
                if(pTextureProperties != null)
                    destNode.setValue(destIndex, pTextureProperties);
                else
                    destNode.setValue(destIndex, vfTextureProperties);
                break;

            case FIELD_FILL_PROPERTIES:
                if(pFillProperties != null)
                    destNode.setValue(destIndex, pFillProperties);
                else
                    destNode.setValue(destIndex, vfFillProperties);
                break;

            default:
                super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
        case FIELD_MATERIAL :
            setMaterial(node);
            break;

        case FIELD_TEXTURE:
            setTexture(node);
            break;

        case FIELD_TEXTURE_TRANSFORM:
            setTextureTransform(node);
            break;

        case FIELD_LINE_PROPERTIES:
            setLineProperties(node);
            break;

        case FIELD_POINT_PROPERTIES:
            setPointProperties(node);
            break;

        case FIELD_TEXTURE_PROPERTIES:
            setTextureProperties(node);
            break;

        case FIELD_FILL_PROPERTIES:
            setFillProperties(node);
            break;

        default:
            super.setValue(index, child);
        }

        if(!inSetup) {
            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Get the total texture size of all textured loaded to date.
     *
     * @return The total(K bytes) of textures loaded.
     */
    public static float getTotalTextureSize() {
        return totalTextureSize;
    }

    /**
     * Setup TextureStage parameters
     *
     * @param stage The TextureStage index
     * @param image The image object
     * @param url The source of the image
     * @return The array of images
     */
    protected NIOBufferImage[] processImage(int stage, NIOBufferImage niobi, String url) {

        int width = niobi.getWidth();
        int height = niobi.getHeight();
        if (width > 32 || height > 32) {
            // rem: note that pixel textures - as presently implemented - will not
            // generate mipmaps - even if they are 'enabled' by the system property.
            // therefore, ckeck that the image actually has mipmaps before setting a
            // filter that would use them
            if ( useMipMaps && ( niobi.getLevels( ) > 1 ) ) {
                stages[stage].minFilter = TextureConstants.MINFILTER_MULTI_LEVEL_LINEAR;
                stages[stage].magFilter = TextureConstants.MAGFILTER_BASE_LEVEL_LINEAR;
            }
        } else {
            // rem: note that pixel textures - as presently implemented - will not
            // generate mipmaps - even if they are 'enabled' by the system property.
            // therefore, ckeck that the image actually has mipmaps before setting a
            // filter that would use them
            if ( useMipMaps && ( niobi.getLevels( ) > 1 ) ) {
                stages[stage].minFilter = TextureConstants.MINFILTER_MULTI_LEVEL_POINT;
                stages[stage].magFilter = TextureConstants.MAGFILTER_BASE_LEVEL_POINT;
            } else {
                // No filtering
                stages[stage].minFilter = TextureConstants.MINFILTER_FASTEST;
                stages[stage].magFilter = TextureConstants.MAGFILTER_FASTEST;
            }
        }

        totalTextureSize += width * height * niobi.getType( ).size / 1024
           * (( useMipMaps && ( niobi.getLevels( ) > 1 ) ) ? 1.33333f : 1.0f);

        return( new NIOBufferImage[]{ niobi } );
    }

    /**
     * Insures that the Texture Stage array and associated arrays are at least
     * this big.  Sets the numStages variable to the largest length.
     *
     * @param newLen The new length
     * @param inc Should this increase the number of valid stages
     */
    protected void insureStageSize(int newLen, boolean inc) {
        if (newLen > stages.length) {
            int len = stages.length;

            TextureStage[] newStages = new TextureStage[newLen];

            String[] newUrls = new String[newLen];
            for(int i = 0; i < len; i++) {
                newStages[i] = stages[i];
                newUrls[i] = urls[i];
            }

            for(int i = len; i < newLen; i++)
                newStages[i] = new TextureStage(i);

            stages = newStages;
            urls = newUrls;

            if (inc && newLen > numStages)
                numStages = newLen;
        }
    }

    /**
     * Process the images in this texture.  Recursely follow any
     * multitexture references.
     *
     * @param stage The TextureStage index
     * @param tex The texture to process.
     */
    protected void processImages(int stage, VRMLTextureNodeType tex) {

        NIOBufferImage niobi;
        VRMLTextureNodeType[] texs;
        int numTexs;

        switch(tex.getTextureType()) {
        case TextureConstants.TYPE_SINGLE_2D:

            niobi = ((VRMLTexture2DNodeType)tex).getImage();
            if (urls.length >= stage + 1) {
                urls[stage] = tex.getCacheString(0);
            }
            if ( niobi != null ) {
                insureStageSize(1, true);
                stages[stage].images = processImage(stage, niobi, null);
            }
            break;

        case TextureConstants.TYPE_SINGLE_3D:
            VRMLComposedTextureNodeType ctex = (VRMLComposedTextureNodeType) tex;

            numTexs = ctex.getNumberTextures();
            texs = new VRMLTextureNodeType[numTexs];
            ctex.getTextures(0,texs);

            insureStageSize(1, true);

            stages[stage].images = new NIOBufferImage[numTexs];
            for(int i=0; i < numTexs; i++) {
                niobi = ((VRMLTexture2DNodeType)texs[i]).getImage();

                NIOBufferImage[] images;
                if ( niobi != null ) {
                    // Should only be one
                    images = processImage( stage, niobi, null );

                    if((images != null) && (images.length != 0)) {
                        stages[stage].images[i] = images[0];
                    }
                    urls[stage] = null;
                }

            }
            break;

        case TextureConstants.TYPE_MULTI:
            VRMLMultiTextureNodeType mtex = (VRMLMultiTextureNodeType)
                tex;

            numTexs = mtex.getNumberTextures();
            texs = new VRMLTextureNodeType[numTexs];

            mtex.getTextures(0,texs);

            insureStageSize(numTexs, true);

            for(int i=0; i < numTexs; i++) {
                processImages(i, texs[i]);
            }
        }
    }
}
