/*****************************************************************************
 *                        Web3d.org Copyright (c) 2003
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.texture;

// Standard imports
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLTextureProperties2DNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseTextureProperties;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;


/**
 * OpenGL implementation of a texture properties.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class OGLTextureProperties extends BaseTextureProperties
    implements OGLVRMLNode, VRMLTextureProperties2DNodeType {

    /** Boundary Mode mapping from X3D String to Texture Constants */
    private static HashMap bmMap;

    /** MaxificationFilter mapping from X3D String to Texture Constants */
    private static HashMap maxfMap;

    /** MinificationFilter mapping from X3D String to Texture Constants */
    private static HashMap minfMap;

    /** AnistoropicMode mapping from X3D String to Texture Constants */
    private static HashMap aniMap;

    static {
        bmMap = new HashMap();
        bmMap.put("CLAMP", new Integer(TextureConstants.BM_CLAMP));
        bmMap.put("WRAP", new Integer(TextureConstants.BM_WRAP));
        bmMap.put("CLAMP_EDGE", new Integer(TextureConstants.BM_CLAMP_EDGE));
        bmMap.put("CLAMP_BOUNDARY", new Integer(TextureConstants.BM_CLAMP_BOUNDARY));

        maxfMap = new HashMap();
        maxfMap.put("FASTEST", new Integer(TextureConstants.MAGFILTER_FASTEST));
        maxfMap.put("NICEST", new Integer(TextureConstants.MAGFILTER_NICEST));
        maxfMap.put("BASE_LEVEL_POINT", new Integer(TextureConstants.MAGFILTER_BASE_LEVEL_POINT));
        maxfMap.put("BASE_LEVEL_LINEAR", new Integer(TextureConstants.MAGFILTER_BASE_LEVEL_LINEAR));
        maxfMap.put("LINEAR_DETAIL", new Integer(TextureConstants.MAGFILTER_LINEAR_DETAIL));
        maxfMap.put("LINEAR_DETAIL_RGB", new Integer(TextureConstants.MAGFILTER_LINEAR_DETAIL_RGB));
        maxfMap.put("LINEAR_DETAIL_ALPHA", new Integer(TextureConstants.MAGFILTER_LINEAR_DETAIL_ALPHA));

        minfMap = new HashMap();
        minfMap.put("FASTEST", new Integer(TextureConstants.MINFILTER_FASTEST));
        minfMap.put("NICEST", new Integer(TextureConstants.MINFILTER_NICEST));
        minfMap.put("BASE_LEVEL_POINT", new Integer(TextureConstants.MINFILTER_BASE_LEVEL_POINT));
        minfMap.put("BASE_LEVEL_LINEAR", new Integer(TextureConstants.MINFILTER_BASE_LEVEL_LINEAR));
        minfMap.put("MULTI_LEVEL_POINT", new Integer(TextureConstants.MINFILTER_MULTI_LEVEL_POINT));
        minfMap.put("MULTI_LEVEL_LINEAR", new Integer(TextureConstants.MINFILTER_MULTI_LEVEL_LINEAR));

        aniMap = new HashMap();
        aniMap.put("NONE", new Integer(TextureConstants.ANISOTROPIC_MODE_NONE));
        aniMap.put("SINGLE", new Integer(TextureConstants.ANISOTROPIC_MODE_SINGLE));
    }

    /**
     * Construct a new default instance of this class.
     */
    public OGLTextureProperties() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public OGLTextureProperties(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods required by the VRMLTextureProperties2DNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the boundary color.  This is a 4 component color.
     *
     * @param color A preallocated 4 component color array;
     */
    public void getBoundaryColor(float[] color) {
        color[0] = vfBoundaryColor[0];
        color[1] = vfBoundaryColor[1];
        color[2] = vfBoundaryColor[2];
        color[3] = vfBoundaryColor[3];
    }

    /**
     * Get the boundary width.
     *
     * @return The boundary width
     */
     public int getBoundaryWidth() {
        return vfBoundaryWidth;
     }

     /**
      * Get the boundary mode for S.
      *
      * @return The boundary mode.  Defined in TextureConstants.
      */
     public int getBoundaryModeS() {
        return ((Integer)bmMap.get(vfBoundaryModeS)).intValue();
     }

     /**
      * Get the boundary mode for T.
      *
      * @return The boundary mode.  Defined in TextureConstants.
      */
     public int getBoundaryModeT() {
        return ((Integer)bmMap.get(vfBoundaryModeT)).intValue();
     }

     /**
      * Get the magnigification filter.
      *
      * @return The mag filter.  Defined in TextureConstants.
      */
     public int getMagnificationFilter() {
        return ((Integer)maxfMap.get(vfMagnificationFilter)).intValue();
     }

     /**
      * Get the minification filter.
      *
      * @return The min filter.  Defined in TextureConstants.
      */
     public int getMinificationFilter() {
        return ((Integer)minfMap.get(vfMinificationFilter)).intValue();
     }

     /**
      * Get the generateMipsMaps field.
      *
      * @return Should mips be generated for this object.
      */
     public boolean getGenerateMipMaps() {
        return vfGenerateMipMaps;
     }

     /**
      * Get the Anisotropic Mode.
      *
      * @return The anisotropic mode.  Defined in TextureConstants.
      */
     public int getAnisotropicMode() {
        return ((Integer)aniMap.get(vfAnisotropicMode)).intValue();
     }

     /**
      * Get the AnistropicFilter Degree.
      *
      * @return The anistropic degree.
      */
     public float getAnisotropicDegree() {
        return vfAnisotropicFilterDegree;
     }

    //----------------------------------------------------------
    // Methods required by the OGLVRMLNode interface.
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
}
