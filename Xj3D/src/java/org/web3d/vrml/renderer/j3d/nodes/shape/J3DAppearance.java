/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.j3d.nodes.shape;

// External imports
import javax.media.j3d.*;

import java.awt.image.*;

import java.io.IOException;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.j3d.renderer.java3d.texture.J3DTextureCache;
import org.j3d.renderer.java3d.texture.J3DTextureCacheFactory;
import org.j3d.util.ImageUtils;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.renderer.j3d.nodes.*;

import org.web3d.vrml.renderer.common.nodes.shape.BaseAppearance;
import org.web3d.vrml.renderer.common.nodes.shape.TextureStage;

/**
 * Java3D implementation of an Appearance node.
 * <p>
 *
 * Cacheing:
 *    Still cacheing only by URL, need to account for TextureProperties
 * <p>
 * MultiTexture Notes:
 *    function and source not mapped
 * <p>
 *
 * 3D Texture Notes:
 *    Getting a rescale problem, not working.
 * <p>
 *
 * Cubic Environment Notes:
 *    Not implemented.
 *
 * @author Alan Hudson
 * @version $Revision: 1.35 $
 */
public class J3DAppearance extends BaseAppearance
    implements J3DAppearanceNodeType,
               J3DTransparencyListener,
               J3DTextureTransformListener {

    /** Transparency Mode to use */
    private static final int TRANSPMODE = TransparencyAttributes.BLENDED;

    /** The Java 3D Implementation node */
    private Appearance j3dImplNode;

    /** Transparency impl */
    private TransparencyAttributes implTransp;

    /** PolygonAttributes impl */
    private PolygonAttributes implPA;

    /** The current material Transparency */
    private float materialTransparency;

    /** Does the texture contain an alpha channel */
    private boolean textureAlpha;

    /** The current Texture attribute information */
    private TextureAttributes texAttrs[];

    /** The TextureUnitStates used for multitexturing */
    private TextureUnitState texUnits[];

    /** A map between texture unit and texture gen mode */
    private HashMap texGenMap;

    /** A map between TexCoordGen modes and Java3D modes */
    private static HashMap texGenModeMap;

    /** Local Alpha texture passed in*/
    private Texture2D alphaTexture;
    private TextureAttributes alphaTA;

    /** The textureAlpha for each cached entry */
    protected static final HashMap alphaMap;

    /** The array of listeners registered with this node */
    private ArrayList listeners;

    /** The J3D texture objects */
    private Texture[] texObjs;

    /** The vrml texture objects */
    private VRMLTextureNodeType[] vrmlTexs;

    /** The mode for each stage */
    private int[] modes = null;

    /** The function for each stage */
    private int[] functions = null;

    /** The source for each stage */
    private int[] sources = null;

    /** Flag controlling the lighting state */
    private boolean lightingState;

    // These two fields are used to work out how to set up the polygon
    // attributes since Java3D does not have a way of reversing the polygon
    // winding order with an API call. After setupFinished, these are ignored.

    /** Is this object marked as solid */
    private boolean solid;

    /** Is this object marked as CCW */
    private boolean ccw;

    static {
        texGenModeMap = new HashMap(3);
        texGenModeMap.put("SPHERE", new Integer(TexCoordGeneration.SPHERE_MAP));
        texGenModeMap.put("CAMERASPACENORMAL", new Integer(TexCoordGeneration.NORMAL_MAP));
        texGenModeMap.put("CAMERASPACEREFLECTIONVECTOR", new Integer(TexCoordGeneration.REFLECTION_MAP));

        alphaMap = new HashMap();
    }

    /**
     * Empty constructor
     */
    public J3DAppearance() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Appearance node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DAppearance(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods required by the J3DTransparencyListener interface.
    //----------------------------------------------------------
    /**
     * Invoked when a transparency value has changed
     *
     * @param tex The new transparency value
     */
    public void transparencyChanged(float transp) {
        int mode;

        materialTransparency = transp;

        implTransp.setTransparency(transp);
        updateTransparencyState();
    }

    //----------------------------------------------------------
    // Methods required by the J3DAppearanceNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the material that should be used for this appearance. Setting a
     * value of null will clear the current material.
     *
     * @param newMaterial The new material instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setMaterial(VRMLNodeType newMaterial)
        throws InvalidFieldValueException {

        J3DMaterialNodeType old_node = (J3DMaterialNodeType)vfMaterial;

        super.setMaterial(newMaterial);

        if(old_node != null)
            old_node.removeTransparencyListener(this);

        if(vfMaterial != null)
            materialTransparency = vfMaterial.getTransparency();

        updateTransparencyState();

        if(!inSetup)
            createMaterial();
    }

    /**
     * Specify whether an object is solid. The default is true. This will
     * determine if we do backface culling and flip backface normals.
     * Can only be set during setup
     *
     * @param solid Whether the object is solid
     */
    public void setSolid(boolean solid) {
        this.solid = solid;

        // TODO: This must be dynamic as Shape sleps the value from the geometry
        if(solid) {
            if(ccw) {
                implPA.setCullFace(PolygonAttributes.CULL_BACK);
                implPA.setBackFaceNormalFlip(false);
            } else {
                implPA.setCullFace(PolygonAttributes.CULL_FRONT);
                implPA.setBackFaceNormalFlip(true);
            }
        } else {
            implPA.setCullFace(PolygonAttributes.CULL_NONE);
            implPA.setBackFaceNormalFlip(true);
        }
    }

    /**
     * Set whether lighting will be used for this appearance.  In general
     * you should let the material node decide this.  Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting
     * be turned off.  This method will notify the related Material node for
     * this Appearance.  Only used to turn lighting off.
     *
     * @param enable Whether lighting is enabled
     */
    public void setLightingEnabled(boolean enable) {
        lightingState = enable;

        if(vfMaterial != null)
            ((J3DMaterialNodeType)vfMaterial).setLightingEnable(enable);
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
        this.ccw = ccw;

        // TODO: This must be dynamic as Shape sleps the value from the geometry
        if(solid) {
            if(ccw) {
                implPA.setCullFace(PolygonAttributes.CULL_BACK);
                implPA.setBackFaceNormalFlip(false);
            } else {
                implPA.setCullFace(PolygonAttributes.CULL_FRONT);
                implPA.setBackFaceNormalFlip(true);
            }
        } else {
            implPA.setCullFace(PolygonAttributes.CULL_NONE);
            implPA.setBackFaceNormalFlip(true);
        }
    }

    /**
     * Set the texture coordinate generation mode for a texture set.  If
     * its not set then texture coordinates will be used.  A value of
     * null will clear the setting.
     *
     * @param setNum The set which this tex gen mode refers
     * @param mode The mode to use.  Straight VRML field value
     */
    public void setTexCoordGenMode(int setNum, String mode) {
        texGenMap.put(new Integer(setNum), mode);
    }

    /**
     * Set the texture that should be used for this appearance. Setting a
     * value of null will clear the current texture.
     *
     * @param newTexture The new texture instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setTexture(VRMLNodeType newTexture) throws
        InvalidFieldValueException {

        VRMLTextureNodeType old_node = vfTexture;

        super.setTexture(newTexture);

        if(old_node != null)
            old_node.removeTextureListener(this);

        if (!inSetup) {
            processImages(0, (VRMLTextureNodeType)newTexture);

            createTextureUnits();
        }
        vfTexture.addTextureListener(this);
    }

    /**
     * Set the texture transform that should be used for this appearance.
     * Setting a value of null will clear the current texture transform.
     *
     * @param newTransform The new texture transform instance to be used.
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setTextureTransform(VRMLNodeType newTransform)  throws
        InvalidFieldValueException {

        J3DTextureCoordinateTransformNodeType old_node =
            (J3DTextureCoordinateTransformNodeType)vfTextureTransform;

        super.setTextureTransform(newTransform);

        J3DTextureCoordinateTransformNodeType node =
                (J3DTextureCoordinateTransformNodeType)vfTextureTransform;

        if (!inSetup) {
            createTextureTransform();
        }

        node.addTransformListener(this);

        if(old_node != null)
            old_node.removeTransformListener(this);
    }

    //----------------------------------------------------------
    // Methods required by the J3DAppearanceNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the appearance instance used to represent this object.
     *
     * @param The appearance instance
     */
    public Appearance getAppearance() {
        return j3dImplNode;
    }

    /**
     * Set or clear the current alpha mask texture. Used for setting an alpha
     * mask that comes from another node instance, such as Text. A value of
     * null will clear the current alpha mask.
     *
     * @param texture The alpha texture to use or null
     */
    public void setAlphaTexture(Texture2D texture) {
        if (alphaTexture != texture) {
            if (alphaTexture == null && texture != null) {
                insureStageSize(numStages+1, true);

                for(int i=0; i < numStages - 1; i++) {
                    stages[i+1] = stages[i];
                    urls[i+1] = urls[i+1];
                }
            } else if (texture == null) {
                numStages--;
            }

            alphaTexture = texture;

            if (alphaTA == null) {
                alphaTA = new TextureAttributes();
                alphaTA.setCapability(TextureAttributes.ALLOW_COMBINE_WRITE);
                alphaTA.setCapability(TextureAttributes.ALLOW_MODE_WRITE);
                alphaTA.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);
            }

            createTextureUnits();
        }
    }

    //----------------------------------------------------------
    // Methods required by the J3DTextureTransformListener interface.
    //----------------------------------------------------------

    /**
     * Invoked when a textureTransform has changed
     *
     * @param tmatrix The new TransformMatrix array
     */
    public void textureTransformChanged(Transform3D[] tmatrix) {
        int cnt=tmatrix.length;

        insureStageSize(cnt,false);

        for(int i=0; i < cnt; i++) {
            stages[i].transform = tmatrix[i];

            texAttrs[i].setTextureTransform(tmatrix[i]);
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureListener interface.
    //----------------------------------------------------------

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The image idx which changed.
     * @param image The image for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    RenderedImage image,
                                    String url) {

        super.textureImageChanged(idx, node, image, url);

        if (idx < numStages) {
            int type = vfTexture.getTextureType();
            switch(type) {
                case TextureConstants.TYPE_SINGLE_2D:
                case TextureConstants.TYPE_MULTI:
                    texObjs[idx] = createTexture(idx,
                                                 stages[idx],
                                                 url,
                                                 vrmlTexs[idx].getTextureType());


                    texUnits[idx].setTexture(texObjs[idx]);
                    break;
                case TextureConstants.TYPE_SINGLE_3D:
                    int alpha_idx = (alphaTexture == null)? 0 : 1;

                    // TODO: wrong way to determine last texture

                    texObjs[alpha_idx] = createTexture(alpha_idx,
                                                       stages[alpha_idx],
                                                       url,
                                                       type);

                    if(texObjs[alpha_idx] != null)
                        texUnits[alpha_idx].setTexture(texObjs[alpha_idx]);

                    break;
            }
        } else
            createTextureUnits();
    }

    /**
     * Invoked when an all of the underlying images have changed.
     *
     * @len The number of valid entries in the image array.
     * @param image The images for this texture.
     * @param url The urls used to load these images.
     */
    public void textureImageChanged(int len,
                                    VRMLNodeType[] node,
                                    RenderedImage[] image,
                                    String[] url) {
        super.textureImageChanged(len, node, image, url);

        if (len <= numStages) {
            int type = vfTexture.getTextureType();

            if (type == TextureConstants.TYPE_SINGLE_3D) {
                int i = (alphaTexture == null)? 0:1;
                stages[i].mode = getMode(image[0], true);
                texObjs[i] = createTexture(i,stages[i], null, type);
                texUnits[i].setTexture(texObjs[i]);
            } else {
                for(int idx=0; idx < len; idx++) {
                    stages[idx].mode = getMode(image[idx], idx == 0 ? true : false);
                    texObjs[idx] = createTexture(idx,
                                                 stages[idx],
                                                 url[idx],
                                                 vrmlTexs[idx].getTextureType());
                    texUnits[idx].setTexture(texObjs[idx]);
                }
            }
        } else
            createTextureUnits();
    }

    /**
     * Invoked when an underlying image has changed.
     *
     * @param idx The stage which changed.
     * @param node The texture which changed.
     * @param image The image as a data buffer for this texture.
     * @param url The url used to load this image.
     */
    public void textureImageChanged(int idx,
                                    VRMLNodeType node,
                                    Buffer image,
                                    String url) {
        // Not implemented yet
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
                                    Buffer[] image,
                                    String[] url) {
        // Not implemented yet
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

        super.textureParamsChanged(idx, mode, source, function, alpha, color);
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
     * @param color The color to use for modes requiring it.  An array of 3 component colors.
     */
    public void textureParamsChanged(int len, int mode[],
        int[] source, int[] function, float alpha, float[] color) {

        super.textureParamsChanged(len, mode, source, function, alpha, color);

        if (len <= numStages) {
            for(int idx=0; idx < len; idx++) {
                stages[idx].mode = mode[idx];
                setTextureMode(idx,stages[idx], texAttrs[idx]);
            }
        } else
            createTextureUnits();
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own animation engine.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            if(capBits.containsKey(Appearance.class)) {
                bits = (int[])capBits.get(Appearance.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        j3dImplNode.clearCapability(bits[i]);
                } else if(!isStatic) {
                    // unset the cap bits that would have been set in setVersion()
                    j3dImplNode.clearCapability(Appearance.ALLOW_TEXTURE_WRITE);
                    j3dImplNode.clearCapability(Appearance.ALLOW_MATERIAL_WRITE);
                    j3dImplNode.clearCapability(
                        Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
                    j3dImplNode.clearCapability(
                        Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
                    j3dImplNode.clearCapability(
                        Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
                }
            }

            if(capBits.containsKey(TransparencyAttributes.class)) {
                bits = (int[])capBits.get(TransparencyAttributes.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        implTransp.clearCapability(bits[i]);
                } else if(!isStatic) {
                    implTransp.clearCapability(
                        TransparencyAttributes.ALLOW_VALUE_WRITE);
                    implTransp.clearCapability(
                        TransparencyAttributes.ALLOW_MODE_WRITE);
                }
            }

            if(capBits.containsKey(TextureAttributes.class)) {
                bits = (int[])capBits.get(TextureAttributes.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        texAttrs[0].clearCapability(bits[i]);
                } else if(!isStatic) {
                    texAttrs[0].clearCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);
                }
            }

            if(capBits.containsKey(PolygonAttributes.class)) {
                bits = (int[])capBits.get(PolygonAttributes.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        implPA.clearCapability(bits[i]);
                } else if(!isStatic) {
                    implPA.clearCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
                    implPA.clearCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
                }
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        if(freqBits.containsKey(Appearance.class)) {
            bits = (int[])freqBits.get(Appearance.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    j3dImplNode.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                // unset the cap bits that would have been set in setVersion()
                j3dImplNode.clearCapabilityIsFrequent(
                    Appearance.ALLOW_TEXTURE_WRITE);
                j3dImplNode.clearCapabilityIsFrequent(
                    Appearance.ALLOW_MATERIAL_WRITE);
                j3dImplNode.clearCapabilityIsFrequent(
                    Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
                j3dImplNode.clearCapabilityIsFrequent(
                    Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
                j3dImplNode.clearCapabilityIsFrequent(
                    Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
            }
        }

        if(freqBits.containsKey(TransparencyAttributes.class)) {
            bits = (int[])freqBits.get(TransparencyAttributes.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implTransp.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                implTransp.clearCapabilityIsFrequent(
                    TransparencyAttributes.ALLOW_VALUE_WRITE);
                implTransp.clearCapabilityIsFrequent(
                    TransparencyAttributes.ALLOW_MODE_WRITE);
            }
        }

        if(freqBits.containsKey(TextureAttributes.class)) {
            bits = (int[])freqBits.get(TextureAttributes.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    texAttrs[0].clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                texAttrs[0].clearCapabilityIsFrequent(
                    TextureAttributes.ALLOW_TRANSFORM_WRITE);
            }
        }

        if(freqBits.containsKey(PolygonAttributes.class)) {
            bits = (int[])freqBits.get(PolygonAttributes.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implPA.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                implPA.clearCapabilityIsFrequent(
                    PolygonAttributes.ALLOW_CULL_FACE_WRITE);
                implPA.clearCapabilityIsFrequent(
                    PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
            }
        }
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            bits = (int[])capBits.get(Appearance.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    j3dImplNode.setCapability(bits[i]);
            }

            bits = (int[])capBits.get(TransparencyAttributes.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implTransp.setCapability(bits[i]);
            }

            bits = (int[])capBits.get(TextureAttributes.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    texAttrs[0].setCapability(bits[i]);
            }

            bits = (int[])capBits.get(PolygonAttributes.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implPA.setCapability(bits[i]);
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        bits = (int[])freqBits.get(Appearance.class);

        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                j3dImplNode.setCapabilityIsFrequent(bits[i]);
        }

        bits = (int[])freqBits.get(TransparencyAttributes.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                implTransp.setCapabilityIsFrequent(bits[i]);
        }

        bits = (int[])freqBits.get(TextureAttributes.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                texAttrs[0].setCapabilityIsFrequent(bits[i]);
        }

        bits = (int[])freqBits.get(PolygonAttributes.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                implPA.setCapabilityIsFrequent(bits[i]);
        }
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return j3dImplNode;
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

        if(solid) {
            if(ccw) {
                implPA.setCullFace(PolygonAttributes.CULL_BACK);
                implPA.setBackFaceNormalFlip(false);
            } else {
                implPA.setCullFace(PolygonAttributes.CULL_FRONT);
                implPA.setBackFaceNormalFlip(true);
            }
        } else {
            implPA.setCullFace(PolygonAttributes.CULL_NONE);
            implPA.setBackFaceNormalFlip(true);
        }

        createMaterial();
        createTextureTransform();
        createTextureUnits();

        if(vfLineProperties != null)
            j3dImplNode.setLineAttributes(((J3DLinePropertiesNodeType)
                vfLineProperties).getLineAttributes());
    }

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        // Have to enable this for loading of textures via the ContentLoader
        j3dImplNode.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        j3dImplNode.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE);
        for(int i=0; i < texUnits.length; i++) {
            texUnits[i].setCapability(TextureUnitState.ALLOW_STATE_WRITE);
            texAttrs[i].setCapability(TextureAttributes.ALLOW_MODE_WRITE);
        }

        implTransp.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
        implTransp.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
        implTransp.setCapability(TransparencyAttributes.ALLOW_BLEND_FUNCTION_WRITE);
        implPA.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
        implPA.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);

        if(isStatic)
            return;

        j3dImplNode.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        j3dImplNode.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
        j3dImplNode.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        j3dImplNode.setCapability(
            Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
    }

    /**
     * Internal convenient method to do common initialisation.
     */
    private void init() {
        materialTransparency = 0;
        textureAlpha = false;
        lightingState = true;

        j3dImplNode = new Appearance();
        implTransp = new TransparencyAttributes();
        implPA = new PolygonAttributes();
        texAttrs = new TextureAttributes[1];
        texAttrs[0] = new TextureAttributes();
        texUnits = new TextureUnitState[1];
        texUnits[0] = new TextureUnitState();
        texGenMap = new HashMap(2);

        solid = false;
        ccw = true;

        j3dImplNode.setTransparencyAttributes(implTransp);
        j3dImplNode.setPolygonAttributes(implPA);

        listeners = new ArrayList(1);

        if(useTextureCache) {
            J3DTextureCache cache =
                J3DTextureCacheFactory.getCache(J3DTextureCacheFactory.WEAKREF_CACHE);

            setTextureCache(cache);
        }
    }

// Methods added to workaround Java texture bug

    /**
     * Add a listener for appearance changed events. If the listener is null or
     * already registered it will silently ignore the requests.
     *
     * @param l The listener to add
     */
    public void addAppearanceListener(J3DAppearanceListener l) {
        if((l == null) || listeners.contains(l))
            return;

        listeners.add(l);
    }

    /**
     * Remove a listener for appearance changed events. If the listener is null
     * or has not been registered it will silently ignore the requests.
     *
     * @param l The listener to remove
     */
    public void removeAppearanceListener(J3DAppearanceListener l) {
        if((l == null) || !listeners.contains(l))
            return;

        listeners.remove(l);
    }

    /**
     * fire a appearance changed event to the listeners.
     *
     * @param app The new appearance node
     */
    protected void fireAppearanceChanged(Appearance app) {
        int size = listeners.size();
        J3DAppearanceListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DAppearanceListener)listeners.get(i);
                l.appearanceChanged(app);
            } catch(Exception e) {
                System.out.println("Error sending appearance changed message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Recreates the j3d appearance representation to get around a
     * Java3D bug in 1.3 that retains texture memory
     */
/*
    private void recreateAppearance() {
        int mode = TransparencyAttributes.NONE;
        float trans = 0;

        j3dImplNode = new Appearance();
        // Need to handle user specified attributes
        j3dImplNode.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
        j3dImplNode.setCapability(Appearance.ALLOW_TEXTURE_UNIT_STATE_WRITE);
        j3dImplNode.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
        j3dImplNode.setCapability(Appearance.ALLOW_TEXTURE_ATTRIBUTES_WRITE);
        j3dImplNode.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
        j3dImplNode.setCapability(
            Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);

        if(vfMaterial != null) {
            J3DMaterialNodeType node = (J3DMaterialNodeType)vfMaterial;
            j3dImplNode.setMaterial(node.getMaterial());

            trans = node.getTransparency();

            if (trans > 0.0f || textureAlpha) {
                mode = TRANSPMODE;
            } else {
                mode = TransparencyAttributes.NONE;
            }

            node.addTransparencyListener(this);
        } else {
            j3dImplNode.setMaterial(null);
            mode = TransparencyAttributes.NONE;
        }

        implTransp.setTransparency(trans);
        implTransp.setTransparencyMode(mode);

        j3dImplNode.setTransparencyAttributes(implTransp);

        j3dImplNode.setPolygonAttributes(implPA);

        if(vfMaterial != null) {
            ((J3DMaterialNodeType)vfMaterial).setLightingEnable(saveLightingEnable);
        }

        fireAppearanceChanged(j3dImplNode);
    }
*/
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

        switch(index) {
            case FIELD_MATERIAL:
                createMaterial();
                break;
            case FIELD_TEXTURE:
                createTextureUnits();
                break;
            case FIELD_TEXTURE_TRANSFORM:
                createTextureTransform();
                break;

            default:
                System.out.println("J3DAppearance: Unknown field for notifyExternProtoLoaded");
        }
    }

    //----------------------------------------------------------
    // Private methods
    //----------------------------------------------------------

    /**
     * Create the render specific structures for this field.
     */
    private void createMaterial() {
        int mode = TransparencyAttributes.NONE;

        if(vfMaterial != null) {
            J3DMaterialNodeType node = (J3DMaterialNodeType)vfMaterial;
            node.setLightingEnable(lightingState);

            j3dImplNode.setMaterial(node.getMaterial());

            materialTransparency = node.getTransparency();

            if (materialTransparency > 0.0f || textureAlpha) {
                mode = TRANSPMODE;
            } else {
                mode = TransparencyAttributes.NONE;
            }

            node.addTransparencyListener(this);
        } else {
            j3dImplNode.setMaterial(null);
            mode = TransparencyAttributes.NONE;
        }

        implTransp.setTransparency(materialTransparency);
        implTransp.setTransparencyMode(mode);
    }

    /**
     * Create the render specific structures for this field.
     */
    private void createTextureUnits() {
        // Find out how many stages to setup
        numStages = (alphaTexture != null) ? 1 : 0;
        int idx_start = numStages;

        if(vfTexture != null) {
            switch(vfTexture.getTextureType()) {
                case TextureConstants.TYPE_SINGLE_2D:
                    numStages++;
                    vrmlTexs = new VRMLTextureNodeType[numStages];
                    modes = new int[numStages];
                    sources = new int[numStages];
                    functions = new int[numStages];
                    vrmlTexs[numStages - 1] = vfTexture;

                    VRMLTexture2DNodeType tex2 =
                        (VRMLTexture2DNodeType)vfTexture;

                    modes[numStages-1] = getMode(tex2.getImage(), true);
                    // TODO: Fill in source and function

                    break;

                case TextureConstants.TYPE_MULTI:
                    VRMLMultiTextureNodeType mtex =
                        ((VRMLMultiTextureNodeType)vfTexture);
                    numStages += mtex.getNumberTextures();
                    vrmlTexs = new VRMLTextureNodeType[numStages];
                    modes = new int[numStages];
                    sources = new int[numStages];
                    functions = new int[numStages];

                    mtex.getTextures(idx_start,vrmlTexs);
                    mtex.getTextureParams(idx_start,
                                          modes,
                                          functions,
                                          sources);
                    break;
                case TextureConstants.TYPE_SINGLE_3D:
                    VRMLComposedTextureNodeType ctex =
                        (VRMLComposedTextureNodeType)vfTexture;
                    int ct_size = numStages + ctex.getNumberTextures();
                    vrmlTexs = new VRMLTextureNodeType[ct_size];
                    numStages++;
                    modes = new int[numStages];
                    sources = new int[numStages];
                    functions = new int[numStages];

                    ctex.getTextures(idx_start,vrmlTexs);
                    break;

                case TextureConstants.TYPE_CUBIC_ENVIRONMAP:
                    break;
            }
        }

        texObjs = new Texture[numStages];
        texUnits = new TextureUnitState[numStages];
        texAttrs = new TextureAttributes[numStages];

        insureStageSize(numStages, true);

        int currStage = 0;

        if (alphaTexture != null) {
            stages[currStage].source = TextureConstants.SRC_COMBINE_PREVIOUS;

            switch(alphaTexture.getFormat()) {
                case Texture.INTENSITY :
                case Texture.LUMINANCE :
                case Texture.ALPHA :
                case Texture.LUMINANCE_ALPHA :
                    stages[currStage].mode = TextureConstants.MODE_MODULATE;
                    alphaTA.setCombineRgbMode(TextureAttributes.MODULATE);
                    break;
                default :
                    stages[currStage].mode = TextureConstants.MODE_REPLACE;
                    alphaTA.setCombineRgbMode(TextureAttributes.REPLACE);
            }

            alphaTA.setTextureMode(TextureAttributes.COMBINE);
            alphaTA.setCombineRgbSource(1, TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE);
            textureAlpha = true;
            updateTransparencyState();
            texObjs[currStage] = alphaTexture;
            texAttrs[currStage] = alphaTA;

            texUnits[currStage] = new TextureUnitState(texObjs[currStage],
                    texAttrs[currStage], null);
            texUnits[currStage].setCapability(TextureUnitState.ALLOW_STATE_WRITE);

            currStage++;
        }

        // Setup the TextureStage variables
        for(int i=currStage; i < numStages; i++) {
            TextureStage tstage = stages[i];
            TexCoordGeneration tcg = null;

            // Common variables
            if (vfTextureProperties != null) {
                // Only use 2D properties
                VRMLTextureProperties2DNodeType tp =
                    (VRMLTextureProperties2DNodeType)vfTextureProperties;
                tstage.boundaryModeS = tp.getBoundaryModeS();
                tstage.boundaryModeT = tp.getBoundaryModeT();
                tstage.boundaryColor = new float[4];
                tp.getBoundaryColor(tstage.boundaryColor);
                tstage.minFilter = tp.getMinificationFilter();
                tstage.magFilter = tp.getMagnificationFilter();
            }

            tstage.yUp = vrmlTexs[i].getYUp();

            // Texture Type specific
            int type = vfTexture.getTextureType();
            switch(type) {
                case TextureConstants.TYPE_SINGLE_2D:
                case TextureConstants.TYPE_MULTI:
                    type = vrmlTexs[i].getTextureType();
                    tstage.mode = modes[i];
                    if (vfTextureProperties == null) {
                        VRMLTexture2DNodeType sn = (VRMLTexture2DNodeType)vrmlTexs[i];
                        boolean repeatS = sn.getRepeatS();
                        if (repeatS)
                            tstage.boundaryModeS = TextureConstants.BM_WRAP;
                        else
                            tstage.boundaryModeS = TextureConstants.BM_CLAMP;

                        boolean repeatT = sn.getRepeatT();
                        if (repeatT)
                            tstage.boundaryModeT = TextureConstants.BM_WRAP;
                        else
                            tstage.boundaryModeT = TextureConstants.BM_CLAMP;
                    }

                    int texGenStage = (alphaTexture == null ? currStage : currStage - 1);

                    String modeString = (String) texGenMap.get(new Integer(texGenStage));
                    if (modeString != null) {
                        Integer mode = (Integer) texGenModeMap.get(modeString);
                        if (mode != null) {
                            tcg = new TexCoordGeneration(mode.intValue(),
                                TexCoordGeneration.TEXTURE_COORDINATE_2);
                        } else {
                            System.out.println("TexCoordGen mode not found: " + mode);
                        }

                    }

                    break;

                case TextureConstants.TYPE_SINGLE_3D:

                    // TODO: need to switch to 3D properties
                    if (vfTextureProperties != null) {
                        VRMLTextureProperties2DNodeType tp =
                            (VRMLTextureProperties2DNodeType)vfTextureProperties;
                        //tstage.boundaryModeR = tp.getBoundaryModeR();
                    } else {
                        VRMLTexture3DNodeType sn =
                            (VRMLTexture3DNodeType)vfTexture;

                        if (sn.getRepeatS())
                            tstage.boundaryModeS = TextureConstants.BM_WRAP;
                        else
                            tstage.boundaryModeS = TextureConstants.BM_CLAMP;

                        if (sn.getRepeatT())
                            tstage.boundaryModeT = TextureConstants.BM_WRAP;
                        else
                            tstage.boundaryModeT = TextureConstants.BM_CLAMP;

                        if (sn.getRepeatR())
                            tstage.boundaryModeR = TextureConstants.BM_WRAP;
                        else
                            tstage.boundaryModeR = TextureConstants.BM_CLAMP;

                        tstage.depth = sn.getDepth();
                    }

                    texGenStage = (alphaTexture == null ? currStage : currStage - 1);

                    modeString = (String) texGenMap.get(new Integer(texGenStage));

                    if (modeString != null) {
                        Integer mode = (Integer) texGenModeMap.get(modeString);
                        if (mode != null) {
                            tcg = new TexCoordGeneration(mode.intValue(),
                                TexCoordGeneration.TEXTURE_COORDINATE_3);
                        } else {
                            System.out.println("TexCoordGen mode not found: " + mode);
                        }
                    }

                    break;

                case TextureConstants.TYPE_CUBIC_ENVIRONMAP:
                    break;
            }


            texObjs[i] = createTexture(i,tstage, urls[i], type);
            texAttrs[i] = createTextureAttributes(i,tstage);

            texUnits[i] = new TextureUnitState(texObjs[i], texAttrs[i], tcg);
            texUnits[i].setCapability(TextureUnitState.ALLOW_STATE_WRITE);
        }

        j3dImplNode.setTextureUnitState(texUnits);
    }

    /**
     * Create the render specific structures for this field.
     */
    private void createTextureTransform() {
        if(vfTextureTransform != null) {
            J3DTextureCoordinateTransformNodeType jtct =
                (J3DTextureCoordinateTransformNodeType)vfTextureTransform;
            Transform3D[] tt = jtct.getTransformMatrix();
            int cnt = tt.length;

            // Hold all texture transforms, but don't increase valid stages(numStages)
            insureStageSize(cnt, false);

            // TODO: Why do we have to do this now?
            if (cnt > texAttrs.length) {
                texAttrs = new TextureAttributes[cnt];
                for(int i=0; i < cnt; i++) {
                    texAttrs[i] = new TextureAttributes();
                }
            }

            for(int i=0; i < cnt; i++) {
                stages[i].transform = tt[i];
                texAttrs[i].setTextureTransform(tt[i]);
            }
        }
    }

    /**
     * Create the Texture object for this stage.
     */
    private Texture createTexture(int stage,
                                  TextureStage tstage,
                                  String url,
                                  int type) {

        Texture ret_val = null;

        // Check the texture cache
        // TODO: How to deal with TextureProperties?  Compare them?
        if (useTextureCache && cache.checkTexture(url) == true) {
            try {
                ret_val = (Texture2D)((J3DTextureCache)cache).fetchTexture(url);

                Boolean val = (Boolean)alphaMap.get(ret_val);
                textureAlpha = val.booleanValue();
                updateTransparencyState();

                return ret_val;
            } catch(IOException io) {
                // ignore and reload
            }
        }

        // If we have no image in the first place, just return with nothing
        // created.
        if(tstage.images == null ||
           tstage.images.length < 1 ||
           tstage.images[0] == null) {

            return null;
        }

        int format = getFormat(tstage.images[0]);

        int len;
        int texType=0;
        int width;
        int height;

        // Setup texture stage variables
        if (vfTextureProperties != null) {
            VRMLTextureProperties2DNodeType tps =
                (VRMLTextureProperties2DNodeType)vfTextureProperties;

            tstage.generateMipMaps = tps.getGenerateMipMaps();
            tstage.boundaryModeS = tps.getBoundaryModeS();
            tstage.boundaryModeT = tps.getBoundaryModeT();
            tstage.minFilter = tps.getMinificationFilter();
            tstage.magFilter = tps.getMagnificationFilter();
            tstage.anisotropicMode = tps.getAnisotropicMode();
            tstage.anisotropicDegree = tps.getAnisotropicDegree();
        } else {
            switch(vfTexture.getTextureType()) {
                case TextureConstants.TYPE_SINGLE_2D:
                    VRMLTexture2DNodeType tex2d = (VRMLTexture2DNodeType) vfTexture;
                    if (tex2d.getRepeatS())
                        tstage.boundaryModeS = TextureConstants.BM_WRAP;
                    else
                        tstage.boundaryModeS = TextureConstants.BM_CLAMP;

                    if (tex2d.getRepeatT())
                        tstage.boundaryModeT = TextureConstants.BM_WRAP;
                    else
                        tstage.boundaryModeT = TextureConstants.BM_CLAMP;
                    break;

                case TextureConstants.TYPE_SINGLE_3D:

                    VRMLTexture3DNodeType tex3d = (VRMLTexture3DNodeType) vfTexture;
                    if (tex3d.getRepeatS())
                        tstage.boundaryModeS = TextureConstants.BM_WRAP;
                    else
                        tstage.boundaryModeS = TextureConstants.BM_CLAMP;

                    if (tex3d.getRepeatT())
                        tstage.boundaryModeT = TextureConstants.BM_WRAP;
                    else
                        tstage.boundaryModeT = TextureConstants.BM_CLAMP;

                    if (tex3d.getRepeatR())
                        tstage.boundaryModeR = TextureConstants.BM_WRAP;
                    else
                        tstage.boundaryModeR = TextureConstants.BM_CLAMP;
                    break;
            }

            if (useMipMaps)
                tstage.minFilter = TextureConstants.MINFILTER_MULTI_LEVEL_LINEAR;

            if (anisotropicDegree > 1) {
                tstage.anisotropicMode = TextureConstants.ANISOTROPIC_MODE_SINGLE;
                tstage.anisotropicDegree = anisotropicDegree;
            }
        }

        // Create the Texture object
        try {
            switch(type) {
                case TextureConstants.TYPE_SINGLE_2D:
                    len = tstage.images.length;

                    if (containsAlpha(tstage.images[0]))
                        textureAlpha = true;

                    ImageComponent2D[] comps_2 = new ImageComponent2D[len];

                    for(int i=0; i < len; i++) {
                        // Force a copy so we know we can release the memory

                        comps_2[i] = new ImageComponent2D(format,
                                                          tstage.images[i],
                                                          false,
                                                          tstage.yUp);

                        comps_2[i].setCapability(ImageComponent.ALLOW_FORMAT_READ);
                        comps_2[i].setCapability(ImageComponent.ALLOW_SIZE_READ);
                    }

                    width = comps_2[0].getWidth();
                    height = comps_2[0].getHeight();
                    texType = getTextureFormat(comps_2[0]);

                    if (!tstage.generateMipMaps && !useMipMaps) {
                        ret_val = new Texture2D(Texture2D.BASE_LEVEL,
                                                texType,
                                                width,
                                                height);
                    } else {
                        ret_val = new Texture2D(Texture2D.MULTI_LEVEL_MIPMAP,
                                                texType,
                                                width,
                                                height);
                    }

                    int val = J3DTextureConstConverter.convertAnisotropicMode(tstage.anisotropicMode);
                    ret_val.setAnisotropicFilterMode(val);
                    ret_val.setAnisotropicFilterDegree(tstage.anisotropicDegree);

                    val = J3DTextureConstConverter.convertBoundary(tstage.boundaryModeS);
                    ret_val.setBoundaryModeS(val);

                    val = J3DTextureConstConverter.convertBoundary(tstage.boundaryModeT);
                    ret_val.setBoundaryModeT(val);

                    val = J3DTextureConstConverter.convertMinFilter(tstage.minFilter);
                    ret_val.setMinFilter(val);

                    val = J3DTextureConstConverter.convertMagFilter(tstage.magFilter);
                    ret_val.setMagFilter(val);

                    ret_val.setImages(comps_2);
                    ret_val.setCapability(Texture.ALLOW_FORMAT_READ);

/*
TODO: This seems to crash sometimes, why?
                    // Release reference to save memory
                    for(int i=0; i < len; i++) {
                        if (tstage.images[i] instanceof BufferedImage)
                            ((BufferedImage)tstage.images[i]).flush();
                    }
*/
                    tstage.images = null;
                    break;

                case TextureConstants.TYPE_SINGLE_3D:
                    // Not enough images in the array yet.
                    if(tstage.images.length < tstage.depth)
                        return null;

                    // Don't create a texture until we have all the images
                    // specified.

                    for(int i = 0; i < tstage.depth; i++) {
                        if(tstage.images[i] == null)
                            return null;

                        if(getFormat(tstage.images[i]) != format)
                            return null;
                    }

                    width = tstage.images[0].getWidth();
                    height = tstage.images[0].getHeight();


                    ImageComponent3D comp_3 =
                        new ImageComponent3D(format,
                                             width,
                                             height,
                                             tstage.depth,
                                             false,
                                             tstage.yUp);

                    for(int i = 0; i < tstage.depth; i++) {
                        if (containsAlpha(tstage.images[i]))
                            textureAlpha = true;
                    }

                    comp_3.set(tstage.images);
                    comp_3.setCapability(ImageComponent.ALLOW_FORMAT_READ);
                    comp_3.setCapability(ImageComponent.ALLOW_SIZE_READ);

                    texType = getTextureFormat(comp_3);

                    if (!tstage.generateMipMaps) {
                        ret_val = new Texture3D(Texture3D.BASE_LEVEL,
                                                texType,
                                                width,
                                                height,
                                                tstage.depth);
                    } else {
                        ret_val = new Texture3D(Texture3D.MULTI_LEVEL_MIPMAP,
                                                texType,
                                                width,
                                                height,
                                                tstage.depth);
                    }

                    val = J3DTextureConstConverter.convertAnisotropicMode(tstage.anisotropicMode);
                    ret_val.setAnisotropicFilterMode(Texture.ANISOTROPIC_SINGLE_VALUE);
                    ret_val.setAnisotropicFilterDegree(tstage.anisotropicDegree);

                    val = J3DTextureConstConverter.convertBoundary(tstage.boundaryModeS);
                    ret_val.setBoundaryModeS(val);

                    val = J3DTextureConstConverter.convertBoundary(tstage.boundaryModeT);
                    ret_val.setBoundaryModeT(val);

                    val = J3DTextureConstConverter.convertBoundary(tstage.boundaryModeR);
                    ((Texture3D)ret_val).setBoundaryModeR(val);

                    val = J3DTextureConstConverter.convertMinFilter(tstage.minFilter);
                    ret_val.setMinFilter(val);

                    val = J3DTextureConstConverter.convertMagFilter(tstage.magFilter);
                    ret_val.setMagFilter(val);

                    ret_val.setImage(0, comp_3);
                    ret_val.setCapability(Texture.ALLOW_FORMAT_READ);

                    tstage.images = null;
                    break;

            }
        } catch (Exception e) {
            System.out.println("Error creating image: " + url);
            e.printStackTrace();
            return ret_val;
        }

        if (useTextureCache && url != null) {
            ((J3DTextureCache)cache).registerTexture(ret_val, url);
            alphaMap.put(ret_val, textureAlpha ? Boolean.TRUE : Boolean.FALSE);
        }

        updateTransparencyState();

        return ret_val;
    }

    /**
     * Create the textureAttributes for a texture stage.
     */
    private TextureAttributes createTextureAttributes(int stage, TextureStage tstage) {
        TextureAttributes ret_val = new TextureAttributes();

        setTextureMode(stage, tstage, ret_val);

        if (tstage.transform != null) {
            ret_val.setTextureTransform((Transform3D)tstage.transform);
        }

        ret_val.setCapability(TextureAttributes.ALLOW_COMBINE_WRITE);
        ret_val.setCapability(TextureAttributes.ALLOW_MODE_WRITE);
        ret_val.setCapability(TextureAttributes.ALLOW_TRANSFORM_WRITE);
        return ret_val;
    }

    /**
     * Set the texture mode based on stage information.
     */
    private void setTextureMode(int stage,
                                TextureStage tstage,
                                TextureAttributes atts) {
        switch(tstage.mode) {
            case TextureConstants.MODE_REPLACE:
                atts.setTextureMode(TextureAttributes.REPLACE);
                break;
            case TextureConstants.MODE_MODULATE:
//                atts.setTextureMode(TextureAttributes.MODULATE);
                atts.setTextureMode(TextureAttributes.COMBINE);
                atts.setCombineRgbMode(TextureAttributes.COMBINE_MODULATE);

                atts.setCombineRgbSource(1, TextureAttributes.COMBINE_TEXTURE_COLOR);
                atts.setCombineRgbSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);
//                atts.setCombineRgbSource(0, TextureAttributes.COMBINE_OBJECT_COLOR);

                break;
            case TextureConstants.MODE_MODULATE_2X:
                atts.setTextureMode(TextureAttributes.MODULATE);
                atts.setCombineRgbScale(2);
                break;
            case TextureConstants.MODE_MODULATE_4X:
                atts.setTextureMode(TextureAttributes.MODULATE);
                atts.setCombineRgbScale(4);
                break;
            case TextureConstants.MODE_ADD :
                atts.setTextureMode(TextureAttributes.COMBINE);
                atts.setCombineRgbMode(TextureAttributes.COMBINE_ADD);
                atts.setCombineRgbSource(1, TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE);
                break;
            case TextureConstants.MODE_DOTPRODUCT3:
                atts.setTextureMode(TextureAttributes.COMBINE);
                atts.setCombineRgbMode(TextureAttributes.COMBINE_DOT3);
                atts.setCombineRgbSource(1, TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE);
                break;
            case TextureConstants.MODE_SUBTRACT:
                atts.setTextureMode(TextureAttributes.COMBINE);
                atts.setCombineRgbMode(TextureAttributes.COMBINE_SUBTRACT);
                atts.setCombineRgbSource(1, TextureAttributes.COMBINE_PREVIOUS_TEXTURE_UNIT_STATE);
                break;
            case TextureConstants.MODE_ADD_SIGNED:
                atts.setTextureMode(TextureAttributes.COMBINE);
                atts.setCombineRgbMode(TextureAttributes.COMBINE_ADD_SIGNED);
                break;
            case TextureConstants.MODE_ADD_SIGNED_2X:
                atts.setTextureMode(TextureAttributes.COMBINE);
                atts.setCombineRgbMode(TextureAttributes.COMBINE_ADD_SIGNED);
                atts.setCombineRgbScale(2);
                break;
            case TextureConstants.MODE_OFF:
                if (texObjs[stage] != null)
                    texUnits[stage].setTexture(null);
                break;
            default:
                System.out.println("Unknown TextureConstants.Mode: " + tstage.mode);

        }
    }

    /**
      * From the image component format, generate the appropriate texture
      * format.
      *
      * @param comp The image component to get the value from
      * @return The appropriate corresponding texture format value
      */
     protected int getTextureFormat(ImageComponent comp)
     {
         int ret_val = Texture.RGB;

         switch(comp.getFormat())
         {
             case ImageComponent.FORMAT_CHANNEL8:
                 ret_val = Texture.INTENSITY;
                 break;

             case ImageComponent.FORMAT_LUM4_ALPHA4:
             case ImageComponent.FORMAT_LUM8_ALPHA8:
                 ret_val = Texture.LUMINANCE_ALPHA;
                 break;

             case ImageComponent.FORMAT_R3_G3_B2:
             case ImageComponent.FORMAT_RGB:
             case ImageComponent.FORMAT_RGB4:
             case ImageComponent.FORMAT_RGB5:
                ret_val = Texture.RGB;
                break;

             case ImageComponent.FORMAT_RGB5_A1:
 //            case ImageComponent.FORMAT_RGB8:
             case ImageComponent.FORMAT_RGBA:
             case ImageComponent.FORMAT_RGBA4:
 //            case ImageComponent.FORMAT_RGBA8:
                 ret_val = Texture.RGBA;
                 break;
         }

         return ret_val;
    }

    /**
      * From the image information, generate the appropriate ImageComponent type.
      *
      *
      * @param comp The image component to get the value from
      * @return The appropriate corresponding texture format value
      */
     protected int getFormat(RenderedImage image) {
        int format=0;

        if (image instanceof BufferedImage) {
            BufferedImage buffImage = (BufferedImage) image;

            switch(buffImage.getType()) {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_BYTE_BINARY:
                case BufferedImage.TYPE_INT_BGR:
                case BufferedImage.TYPE_INT_RGB:
                    format = ImageComponent.FORMAT_RGB;
                    break;

                case BufferedImage.TYPE_CUSTOM:
                    // no idea what this should be, so default to RGBA

                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE:
                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                    format = ImageComponent.FORMAT_RGBA;
                    break;

                case BufferedImage.TYPE_BYTE_GRAY:
                case BufferedImage.TYPE_USHORT_GRAY:
                    format = ImageComponent.FORMAT_CHANNEL8;
                    break;

                case BufferedImage.TYPE_BYTE_INDEXED:
                    // Determine Alpha
                    ColorModel cm = buffImage.getColorModel();

                    if (cm.hasAlpha())
                        format = ImageComponent.FORMAT_RGBA;
                    else
                        format = ImageComponent.FORMAT_RGB;

                    break;

                case BufferedImage.TYPE_USHORT_555_RGB:
                    format = ImageComponent.FORMAT_RGB5;
                    break;

                case BufferedImage.TYPE_USHORT_565_RGB:
                    format = ImageComponent.FORMAT_RGB5;
                    break;
                default:
                    System.out.println("Unknown FORMAT for image: " + buffImage);
            }
        } else {
System.out.println("RenderedImage assumed to be RGBA: " + image);
            format = ImageComponent.FORMAT_RGBA;
        }

        return format;
    }

    /**
      * Does this image contain an alpha component.
      *
      * @param comp The image component to get the value from
      * @return Contains alpha?
      */
     protected boolean containsAlpha(RenderedImage image) {
        boolean alpha=false;

        if (image instanceof BufferedImage) {
            BufferedImage buffImage = (BufferedImage) image;

            // Determine Alpha
            ColorModel cm = buffImage.getColorModel();

            alpha = cm.hasAlpha();
        } else if (image instanceof RenderedImage) {
            System.out.println("RenderedImage assumed to be RGBA");
            alpha = true;
        } else {
            alpha = false;
        }

        return alpha;
    }

    /**
     * Given the image format return the textureing mode to use.  For the
     * X3D lighting model, whether to use MODULATE or REPLACE.
     *
     * @param ignoreDiffuse Should this affect the material.setIgnoreDiffuse
     */
    private int getMode(RenderedImage image, boolean ignoreDiffuse) {
        int format;
        boolean newIgnoreDiffuse = false;

        if (image instanceof BufferedImage) {
            BufferedImage buffImage = (BufferedImage) image;

            switch(buffImage.getType()) {
                case BufferedImage.TYPE_3BYTE_BGR:
                case BufferedImage.TYPE_BYTE_BINARY:
                case BufferedImage.TYPE_INT_BGR:
                case BufferedImage.TYPE_INT_RGB:
                case BufferedImage.TYPE_CUSTOM:
                case BufferedImage.TYPE_INT_ARGB:
                case BufferedImage.TYPE_INT_ARGB_PRE:
                case BufferedImage.TYPE_4BYTE_ABGR:
                case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                case BufferedImage.TYPE_BYTE_INDEXED:
                case BufferedImage.TYPE_USHORT_555_RGB:
                case BufferedImage.TYPE_USHORT_565_RGB:
                    format = TextureConstants.MODE_MODULATE;
                    ignoreDiffuse = true;
                    break;
                case BufferedImage.TYPE_BYTE_GRAY:
                case BufferedImage.TYPE_USHORT_GRAY:
                    format = TextureConstants.MODE_MODULATE;
                    break;
                default:
                    System.out.println("Unknown FORMAT for image: " + buffImage);
                    format = TextureConstants.MODE_MODULATE;
                    ignoreDiffuse = true;
            }
        } else if (image instanceof RenderedImage) {
            System.out.println("RenderedImage assumed to be RGBA");
            format = TextureConstants.MODE_MODULATE;
            ignoreDiffuse = true;
        } else {
            format = TextureConstants.MODE_MODULATE;
            ignoreDiffuse = true;
        }

        if (ignoreDiffuse && vfMaterial != null) {
            vfMaterial.setIgnoreDiffuse(ignoreDiffuse);
        }

        return format;
    }

    /**
     * Update the transparency state based on the current alpha and material values.
     */
    private void updateTransparencyState() {
        if (textureAlpha || materialTransparency > 0.0f) {
            implTransp.setTransparencyMode(TRANSPMODE);
        }
        else {
            implTransp.setTransparencyMode(TransparencyAttributes.NONE);
        }
    }
}
