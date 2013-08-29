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

package org.web3d.vrml.renderer.ogl.nodes.texture;

// Standard imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.texture.BaseTextureTransform3D;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureCoordinateTransformNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLTextureTransformListener;

/**
 * Java3D implementation of a texture transform.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class OGLTextureTransform3D extends BaseTextureTransform3D
  implements OGLTextureCoordinateTransformNodeType {

    /** Message when we get an exception sending out the transform message */
    private static final String TX_SEND_ERROR =
        "There was an exception generation sending a texture transform " +
        "listener event in OGLTextureTransform";

    /** Index flag for the listener firing */
    private static final boolean[] UPDATE_FLAGS = { true };

    /** OGL Impl node */
    private Matrix4f oglMatrix;

    /** List of those want to know about TextureTransform changes */
    private ArrayList listenerList;

    /** Class Vars for speed */
    private Vector3f v1;
    private Vector3f v2;
    private Vector3f v3;
    private Matrix4f T;
    private Matrix4f C;
    private Matrix4f R;
    private Matrix4f S;
    private AxisAngle4f al;

    /** List of changed transforms to send along to the listeners */
    private Matrix4f[] changedTransforms;

    /**
     * Construct a new default instance of this class.
     */
    public OGLTextureTransform3D() {
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
    public OGLTextureTransform3D(VRMLNodeType node) {
        super(node);

        init();
    }

    //----------------------------------------------------------
    // Methods required by the OGLVRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
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

        updateTransform();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, value);

        if(!inSetup) {
            updateTransform();
            fireTransformChanged();
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFColor and SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The index does not match a known field
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        super.setValue(index, value, numValid);

        if(!inSetup) {
            updateTransform();
            fireTransformChanged();
        }
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
        return changedTransforms;
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

    //----------------------------------------------------------
    // Methods internal to OGLTextureTransform
    //----------------------------------------------------------

    private void updateTransform() {
        v1.set(vfTranslation[0],vfTranslation[1], vfTranslation[2]);
        T.set(v1);

        v2.set(vfCenter[0],vfCenter[1], vfCenter[2]);
        C.set(v2);

        al.set(vfOrientation);
        R.setRotation(al);

        S.m00 = vfScale[0];
        S.m11 = vfScale[1];
        S.m22 = vfScale[2];
        S.m33 = 1;

        oglMatrix.setIdentity();
        oglMatrix.mul(T);
        oglMatrix.mul(C);
        oglMatrix.mul(R);
        oglMatrix.mul(S);
        v2.negate();
        C.set(v2);
        oglMatrix.mul(C);

    }

    /**
     * Notify listeners that the transform has changed.
     */
    private void fireTransformChanged() {

        // Notify listeners of new value
        int size = listenerList.size();

        for(int i = 0; i < size; i++) {
            OGLTextureTransformListener l =
                (OGLTextureTransformListener)listenerList.get(i);

            try {
                if(changedTransforms == null)
                    changedTransforms = new Matrix4f[] { oglMatrix };

                l.textureTransformChanged(this, changedTransforms, UPDATE_FLAGS);

            } catch(Exception e) {
                errorReporter.errorReport(TX_SEND_ERROR, e);
            }
        }
    }

    /**
     * Common internal initialisation.
     */
    private void init() {
        listenerList = new ArrayList(1);
        oglMatrix = new Matrix4f();

        changedTransforms = new Matrix4f[1];
        changedTransforms[0] = oglMatrix;

        v1 = new Vector3f();
        v2 = new Vector3f();
        v3 = new Vector3f();
        T = new Matrix4f();
        C = new Matrix4f();
        R = new Matrix4f();
        S = new Matrix4f();
        al = new AxisAngle4f();
    }
}
