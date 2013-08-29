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

// Standard imports
import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Transform3D;

import java.util.ArrayList;
import java.util.Map;

import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4f;

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.texture.BaseTextureTransform3D;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.j3d.nodes.J3DTextureCoordinateTransformNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DTextureTransformListener;

/**
 * Java3D implementation of a texture transform.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class J3DTextureTransform3D extends BaseTextureTransform3D
  implements J3DTextureCoordinateTransformNodeType {

    /** J3D Impl node */
    private Transform3D j3dImplNode;

    /** List of those want to know about TextureTransform changes */
    private ArrayList listenerList;

    /** Class Vars for speed */
    private Vector3d v1;
    private Vector3d v2;
    private Vector3d v3;
    private Transform3D T;
    private Transform3D C;
    private Transform3D R;
    private Transform3D S;
    private AxisAngle4f al;

    /** List of changed transforms for the listener callback */
    private Transform3D[] changedTransform;

    /**
     * Construct a new default instance of this class.
     */
    public J3DTextureTransform3D() {
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
    public J3DTextureTransform3D(VRMLNodeType node) {
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
            // may want to make use of the frame state manager end of
            // frame callback for this
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
            // may want to make use of the frame state manager end of
            // frame callback for this
            updateTransform();
            fireTransformChanged();
        }
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
        return changedTransform;
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

    //----------------------------------------------------------
    // Methods internal to J3DTextureTransform
    //----------------------------------------------------------

    /**
     * Recalculate the transform to be sent.
     */
    private void updateTransform() {
        v1.set(vfTranslation[0],vfTranslation[1], vfTranslation[2]);
        T.set(v1);

        v2.set(vfCenter[0],vfCenter[1], vfCenter[2]);
        C.set(v2);

        al.set(vfOrientation);
        R.setRotation(al);

        v3.set(vfScale[0],vfScale[1], vfScale[2]);
        S.setScale(v3);

        j3dImplNode.setIdentity();
        j3dImplNode.mul(T);
        j3dImplNode.mul(C);
        j3dImplNode.mul(R);
        j3dImplNode.mul(S);
        v2.negate();
        C.set(v2);
        j3dImplNode.mul(C);
    }

    /**
     * Notify listeners that the transform has changed.
     */
    private void fireTransformChanged() {
        // Notify listeners of new value
        int size = listenerList.size();

        for(int i=0; i < size; i++) {
            J3DTextureTransformListener l =
                (J3DTextureTransformListener)listenerList.get(i);

            try {
                l.textureTransformChanged(changedTransform);
            } catch(Exception e) {
                System.out.println("Error sending Texture transform");
                e.printStackTrace();
            }
        }
    }

    /**
     * Common initialisation functionality.
     */
    private void init() {
        j3dImplNode = new Transform3D();
        listenerList = new ArrayList(1);

        v1 = new Vector3d();
        v2 = new Vector3d();
        v3 = new Vector3d();
        T = new Transform3D();
        C = new Transform3D();
        R = new Transform3D();
        S = new Transform3D();
        al = new AxisAngle4f();

        changedTransform = new Transform3D[1];
        changedTransform[0] = j3dImplNode;
    }
}
