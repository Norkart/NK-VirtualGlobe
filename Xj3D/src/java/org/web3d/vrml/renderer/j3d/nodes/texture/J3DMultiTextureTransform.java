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

package org.web3d.vrml.renderer.j3d.nodes.texture;

// External imports
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Transform3D;

import java.util.ArrayList;
import java.util.Map;

import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4f;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLTextureTransformNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseMultiTextureTransform;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DTextureCoordinateTransformNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DTextureTransformListener;

/**
 * Java3D implementation of a multi texture transform.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class J3DMultiTextureTransform extends BaseMultiTextureTransform
  implements J3DTextureCoordinateTransformNodeType, J3DTextureTransformListener {

    /** List of those want to know about TextureTransform changes */
    private ArrayList listenerList;

    /**
     * Construct a new default instance of this class.
     */
    public J3DMultiTextureTransform() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public J3DMultiTextureTransform(VRMLNodeType node) {
        super(node);

        init();
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

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own internal animation engine, so be very
     * careful with this request.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. The end output is that the capabilities are the union
     * of what the loader wants and what the user wants.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
    }

    //----------------------------------------------------------
    // Methods required by J3DTextureTransformListener
    //----------------------------------------------------------

    /**
     * Invoked when a textureTransform has changed
     *
     * @param tmatrix The new TransformMatrix array
     */
    public void textureTransformChanged(Transform3D[] tmatrix) {
        fireTextureTransformChanged(getTransformMatrix());
    }

    //----------------------------------------------------------
    // Methods required by J3DTextureCoordinateTransformNodeType
    //----------------------------------------------------------

    /**
     * Request the Transformation used to represent a texture transformation.
     * The transform will contain all of the warp, scale and rotation
     *
     * @return The transform used to modify this texture
     */
    public Transform3D[] getTransformMatrix() {
        J3DTextureCoordinateTransformNodeType kids[] =
            new J3DTextureCoordinateTransformNodeType[vfTextureTransform.size()];
        vfTextureTransform.toArray(kids);

        Transform3D ret[] = new Transform3D[kids.length];
        Transform3D tmp[];

        for(int i=0; i < kids.length; i++) {
            tmp = kids[i].getTransformMatrix();
            ret[i] = tmp[0];
        }

        return ret;
    }

    /**
     * Add a listener for textureTransform changes.
     *
     * @param tl The listener to add
     */
    public void addTransformListener(J3DTextureTransformListener tl) {
        if (!listenerList.contains(tl)) {
            listenerList.add(tl);
        }
    }

    /**
     * Remove a listener for textureTransform changes.
     *
     * @param tl The listener to remove
     */
    public void removeTransformListener(J3DTextureTransformListener tl) {
        listenerList.remove(tl);
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

        super.addTextureTransformNode(node);
        // Need to handle protos
        ((J3DTextureCoordinateTransformNodeType)node).addTransformListener(this);
    }

    /**
     * Fire a textureImplChanged event to the listeners.
     *
     * @param tex The new texture impl
     * @param alpha Does this texture have an alpha channel
     * @param mode The mode to apply this texture
     */
    protected void fireTextureTransformChanged(Transform3D[] tmatrix) {
        int size = listenerList.size();
        J3DTextureTransformListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (J3DTextureTransformListener)listenerList.get(i);
                l.textureTransformChanged(tmatrix);
            } catch(Exception e) {
                System.out.println("Error sending textureImpl changed message: "
                                   + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Common internal initialisation stuff.
     */
    private void init() {
        listenerList = new ArrayList();
    }
}
