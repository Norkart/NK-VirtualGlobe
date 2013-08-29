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

package org.web3d.vrml.renderer.ogl.input;

// Standard imports
import javax.vecmath.Matrix4f;
import org.j3d.aviatrix3d.Texture2D;

// Application specific imports
import org.web3d.vrml.nodes.VRMLFogNodeType;

/**
 * A generic interface for the control of global effects like background fog
 * and view management for the OpenGL renderer.
 * <p>
 *
 * The input system takes care of the basic per-frame management, but needs to
 * tell the collection of objects that represent the global structure about the
 * new position or control settings. The architecture assumes that the actual
 * matrices responsible for the view handling and backgrounds are managed
 * separately from the VRML scene graph. This is to avoid issues with
 * nodes appearing under SharedGroups etc.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface OGLGlobalEffectsHandler {

    /** Constant used to set the fog state to disabled. */
    public static final int FOG_DISABLE = VRMLFogNodeType.FOG_TYPE_DISABLE;

    /** Constant used to set the fog state to linear. */
    public static final int FOG_LINEAR = VRMLFogNodeType.FOG_TYPE_LINEAR;

    /** Constant used to set the fog state to exponential. */
    public static final int FOG_EXPONENTIAL =
        VRMLFogNodeType.FOG_TYPE_EXPONENTIAL;

    /**
     * Set or reset the layer ID to the new ID value.
     *
     * @param id A non-negative ID for the layer
     */
    public void setLayerId(int id);

    /**
     * Update the view matrix to be this new matrix.
     *
     * @param transform The new view matrix settings
     */
    public void setViewMatrix(Matrix4f transform);

    /**
     * Set the background rotation matrix this new matrix.
     *
     * @param transform The new background matrix settings
     */
    public void setBackgroundMatrix(Matrix4f transform);

    /**
     * Set the fog coordinate matrix this new matrix.
     *
     * @param transform The new fog matrix settings
     */
    public void setFogMatrix(Matrix4f transform);

    /**
     * Update the background transparency to this new value. If the value is
     * 1.0, then disable all the background rendering. A value of 0 is
     * completely opaque and a value of 1 is clear.
     *
     * @param transparency A value between 0 and 1
     */
    public void updateBackgroundTransparency(float transparency);

    /**
     * Update the background textures to this new set.
     *
     * @param textures The list of textures to use
     * @param flags The list of flags indicating a texture change
     */
    public void updateBackgroundTextures(Texture2D[] textures,
                                         boolean[] flags);

    /**
     * Update the background ground color sphere to the new values.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    public void updateBackgroundGround(float[] color, float[] angles, int num);

    /**
     * Update the background sky color sphere to the new values.
     *
     * @param color The color values to use as a flat array
     * @param angles The angles to use at each colour boundary
     * @param num The number of color values to read from the arrays
     */
    public void updateBackgroundSky(float[] color, float[] angles, int num);

    /**
     * Update the fog type in use. Can be used to turn the current fog off
     * too.
     *
     * @param state The fog to use right now
     */
    public void enableFog(int state);

    /**
     * Update the fog with different setups. New colour and visibility range.
     *
     * @param visLimit The visibility limit. Zero to disable it
     * @param r The red component of the fog color
     * @param g The green component of the fog color
     * @param b The blue component of the fog color
     */
    public void setFogDetails(float visLimit, float r, float g, float b);
}
