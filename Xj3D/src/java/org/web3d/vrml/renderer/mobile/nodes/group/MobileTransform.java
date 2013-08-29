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

package org.web3d.vrml.renderer.mobile.nodes.group;

// Standard imports
import java.util.ArrayList;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLGroupingNodeType;
import org.web3d.vrml.nodes.VRMLPointingDeviceSensorNodeType;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.common.nodes.group.BaseTransform;

import org.web3d.vrml.renderer.mobile.sg.TransformGroup;
import org.web3d.vrml.renderer.mobile.sg.Node;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * OpenGL implementation of a Transform node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class MobileTransform extends BaseTransform
    implements MobileVRMLNode {

    /** The renderable scenegraph node */
    private TransformGroup implGroup;

    /** Mapping of the VRMLNodeType to the Mobile Group instance */
    private HashMap oglChildMap;

    protected javax.vecmath.Matrix4f matrix;

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public MobileTransform() {
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public MobileTransform(VRMLNodeType node) {
        super(node);
    }

    //----------------------------------------------------------
    // Methods overridden from the VRMLTransform interface.
    //----------------------------------------------------------

    /**
     * Set the rotation component of the of transform. Setting a value
     * of null is an error
     *
     * @param rot The new rotation component
     * @throws InvalidFieldValueException The rotation was null
     */
    public void setRotation(float[] rot)
        throws InvalidFieldValueException {

        super.setRotation(rot);
        if (!inSetup) {
            updateMatrix();
            implGroup.setTransform(matrix);
        }
    }

    /**
     * Set the translation component of the of transform. Setting a value
     * of null is an error
     *
     * @param tx The new translation component
     * @throws InvalidFieldValueException The translation was null
     */
    public void setTranslation(float[] tx)
        throws InvalidFieldValueException {

        super.setTranslation(tx);
        if (!inSetup) {
            updateMatrix();
            implGroup.setTransform(matrix);
        }
    }

    /**
     * Set the scale component of the of transform. Setting a value
     * of null is an error
     *
     * @param scale The new scale component
     * @throws InvalidFieldValueException The scale was null
     */
    public void setScale(float[] scale)
        throws InvalidFieldValueException {

        super.setScale(scale);

        if (!inSetup) {
            updateMatrix();
            implGroup.setTransform(matrix);
        }
    }

    /**
     * Set the scale orientation component of the of transform. Setting a value
     * of null is an error
     *
     * @param so The new scale orientation component
     * @throws InvalidFieldValueException The scale orientation was null
     */
    public void setScaleOrientation(float[] so)
        throws InvalidFieldValueException {

        super.setScaleOrientation(so);
        if (!inSetup) {
            updateMatrix();
            implGroup.setTransform(matrix);
        }
    }

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    public void setCenter(float[] center)
        throws InvalidFieldValueException {

        super.setCenter(center);
        if (!inSetup) {
            updateMatrix();
            implGroup.setTransform(matrix);
        }
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

        // Free up unneeded vars
        inSetup = false;

        oglChildMap = new HashMap();
        implGroup = new TransformGroup();

        for(int i = 0; i < childCount; i++) {
            MobileVRMLNode node = (MobileVRMLNode)vfChildren.get(i);

            Node ogl_node = (Node)node.getSceneGraphObject();
            if(ogl_node != null)
                implGroup.addChild(ogl_node);

            oglChildMap.put(node, ogl_node);
        }

        updateMatrix();
        implGroup.setTransform(matrix);
    }

    //----------------------------------------------------------
    // Methods from MobileVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGroup;
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children - both VRML and OpenGL.
     */
    protected void clearChildren() {

        implGroup.removeAllChildren();
        super.clearChildren();
    }

    /**
     * Add a single child node to the list of available children. This auto
     * matically deals with DEF/USE and adds links and branchgroups where
     * appropriate. When nodes are null, we do not add them to the GL
     * representation, only to the vfChildren list.
     *
     * @param node The node to view
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addChildNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        super.addChildNode(node);

        MobileVRMLNode n = (MobileVRMLNode)node;

        if(!inSetup) {
            Node ogl_node = (Node)n.getSceneGraphObject();
            implGroup.addChild(ogl_node);

            oglChildMap.put(node, ogl_node);
        }
    }

    /**
     * Remove the given node from this grouping node. If the node is not a
     * child of this node, the request is silently ignored.
     *
     * @param node The node to remove
     */
    protected void removeChildNode(VRMLNodeType node) {
        if(!oglChildMap.containsKey(node))
            return;

        if(!inSetup) {
            Node ogl_node = (Node)oglChildMap.get(node);
            implGroup.removeChild(ogl_node);
            oglChildMap.remove(node);
        }

        if(!inSetup)
            super.removeChildNode(node);
    }

    /**
     * Calculate transforms needed to handle VRML semantics and place the
     * results in the matrix variable of this class.
     *  formula: T x C x R x SR x S x -SR x -C
     */

     // TODO: Relook at this when we revive this.  It has some error in this logic or the vecmath package.
/*
    protected void updateMatrix() {
        // clear the tempVec
        tempVec.sub(tempVec);
        // offset it by vfCenter
        tempVec.sub(vfCenter);

        // set matrix to be a pure translation
        matrix.translation(tempVec);

        if (floatEq(vfScale[0], vfScale[1]) &&
            floatEq(vfScale[0], vfScale[2])) {

            // set the tempMtx1 to a pure scale matrix
            tempMtx1.scale(vfScale);
        } else {
            // non-uniform scale

            // clear the tempAxis
            tempAxis.sub(tempAxis);
            // set to vfScaleOrientation
            tempAxis.add(vfScaleOrientation);
            // normalize a rotation
            tempAxis.rotationNormalize();
            // set the tempMtx1 to be a rotation matrix
            tempMtx1.rotation(tempAxis);

            tempMtx2.mul(tempMtx1, matrix);

            // Set the matrix to be a pure scale from vfScale
            tempMtx1.scale(vfScale);

            matrix.mul(tempMtx1, tempMtx2);

            // clear the tempAxis
            tempAxis.sub(tempAxis);
            // introduce the vfScaleOrientation
            tempAxis.add(vfScaleOrientation);
            // set tempMtx1 to a be a rotation of tempAxis
            tempMtx1.rotation(tempAxis);
        }

        // Sx-C -> t2
        tempMtx1.mul(matrix,tempMtx2);
        // clear the tempAxis
        tempAxis.sub(tempAxis);
        // set it to the vfRotation
        tempAxis.add(vfRotation);
        // apply normalization rules for rotation (AxisAngle) vectors

        tempAxis.rotationNormalize();
        // set the tempMtx1 to be a pure rotation of tempAxis
        // R -> t1
        tempMtx1.rotation(tempAxis);
        // RxSx-C -> m
        tempMtx1.mul(tempMtx2,matrix);

        // set tempMtx1 to a pure translation from vfCenter
        // C -> t1
        tempMtx1.translation(vfCenter);

        // CxRxSx-C -> t2

        tempMtx1.mul(matrix,tempMtx2);
        tempMtx1.translation(vfTranslation);

        // TxCxRxSx-C -> matrix

        tempMtx1.mul(tempMtx2,matrix);
    }
*/
}
