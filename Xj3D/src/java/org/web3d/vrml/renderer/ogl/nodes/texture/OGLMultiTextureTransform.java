/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004
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

import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;

import org.j3d.aviatrix3d.SceneGraphObject;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLTextureTransformNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseMultiTextureTransform;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureCoordinateTransformNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureTransformListener;
import org.web3d.vrml.util.FieldValidator;

/**
 * Java3D implementation of a multi texture transform.
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.2 $
 */
public class OGLMultiTextureTransform extends BaseMultiTextureTransform
  implements OGLTextureCoordinateTransformNodeType,
             OGLTextureTransformListener {

    /** Message when we get an exception sending out the transform message */
    private static final String TX_SEND_ERROR =
        "There was an exception generation sending a texture transform " +
        "listener event in OGLMultiTextureTransform";


    /** List of those want to know about TextureTransform changes */
    private ArrayList listenerList;

    /** Flag list of which child TX changed */
    private boolean[] changeFlags;


    /**
     * Construct a new default instance of this class.
     */
    public OGLMultiTextureTransform() {
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
    public OGLMultiTextureTransform(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods required by OGLVRMLNode
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return null;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        changeFlags = new boolean[vfTextureTransform.size()];
    }

    //----------------------------------------------------------
    // Methods required by OGLTextureTransformListener
    //----------------------------------------------------------

    /**
     * Invoked when a textureTransform has changed.
     *
     * @param src The node instance that was the source of this change
     * @param tmatrix The new TransformMatrix array
     * @param updated Flag for each index illustrating whether it has
     *   been updated or not.
     */
    public void textureTransformChanged(OGLVRMLNode src,
                                        Matrix4f[] tmatrix,
                                        boolean[] updated) {

        fireTextureTransformChanged(src, getTransformMatrix());
    }

    //----------------------------------------------------------
    // Methods required by OGLTextureCoordinateTransformNodeType
    //----------------------------------------------------------

    /**
     * Request the Transformation used to represent a texture transformation.
     * The transform will contain all of the warp, scale and rotation
     *
     * @return The transform used to modify this texture
     */
    public Matrix4f[] getTransformMatrix() {

        OGLTextureCoordinateTransformNodeType kids[] =
            new OGLTextureCoordinateTransformNodeType[vfTextureTransform.size()];
        vfTextureTransform.toArray(kids);

        Matrix4f ret[] = new Matrix4f[kids.length];
        Matrix4f tmp[];


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
    public void addTransformListener(OGLTextureTransformListener tl) {
        if (!listenerList.contains(tl)) {
            listenerList.add(tl);
        }
    }

    /**
     * Remove a listener for textureTransform changes.
     *
     * @param tl The listener to remove
     */
    public void removeTransformListener(OGLTextureTransformListener tl) {
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
        ((OGLTextureCoordinateTransformNodeType)node).addTransformListener(this);

        if(!inSetup) {
            if(changeFlags.length < vfTextureTransform.size())
                changeFlags = new boolean[vfTextureTransform.size()];
        }
    }

    /**
     * Fire a textureTransformChanged event to the listeners.
     *
     * @param src The node instance that was the source of this change
     * @param tmatrix The new TransformMatrix array
     */
    protected void fireTextureTransformChanged(OGLVRMLNode src,
                                               Matrix4f[] tmatrix) {

        // find out which of the sources changed.
        int change_idx = vfTextureTransform.indexOf(src);
        changeFlags[change_idx] = true;

        int size = listenerList.size();
        OGLTextureTransformListener l;

        for(int i = 0; i < size; i++) {
            try {
                l = (OGLTextureTransformListener)listenerList.get(i);
                l.textureTransformChanged(this, tmatrix, changeFlags);
            } catch(Exception e) {
                errorReporter.errorReport(TX_SEND_ERROR, e);
            }
        }

        changeFlags[change_idx] = false;
    }

    /**
     * Common internal initialisation.
     */
    private void init() {
        listenerList = new ArrayList();
    }
}
