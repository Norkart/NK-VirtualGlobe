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

package org.web3d.vrml.renderer.mobile.nodes.navigation;

// Standard imports
import javax.vecmath.Vector4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

// Application specific imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.navigation.BaseViewpoint;
import org.web3d.vrml.renderer.mobile.nodes.MobileViewpointNodeType;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;
import org.web3d.vrml.renderer.mobile.sg.TransformGroup;
import org.web3d.vrml.renderer.mobile.sg.Viewpoint;

/**
 * Mobile implementation of a Viewpoint node.
 * <p>
 *
 * Viewpoints cannot be shared using DEF/USE. They may be named as such for
 * Anchor purposes, but attempting to reuse them will cause an error. This
 * implementation does not provide any protection against USE of this node
 * and attempting to do so will result in exceptions - most
 * probably in the grouping node that includes this node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.4 $
 */
public class MobileViewpoint extends BaseViewpoint
    implements MobileViewpointNodeType {

    /** The transform group that holds the viewpoint */
    private TransformGroup transform;

    /** The impl node */
    private Viewpoint impl;

    // Use J3D classes for now till matrix classes are finished
    private Vector4f axis;
    private Vector3f trans;
    private Matrix4f implTrans;
    private Matrix4f mat;

    /**
     * Construct a default viewpoint instance
     */
    public MobileViewpoint() {
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
    public MobileViewpoint(VRMLNodeType node) {
        super(node);

        init();
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
        return transform;
    }

    //----------------------------------------------------------
    // Methods overriding BaseViewpoint
    //----------------------------------------------------------

    /**
     * Convenience method to set the position of the viewpoint.
     *
     * @param pos The position vector to use
     */
    protected void setPosition(float[] pos) {
        super.setPosition(pos);

        if (!inSetup)
            updateViewTrans();
    }

    /**
     * Convenience method to set the orientation of the viewpoint.
     *
     * @param dir The orientation quaternion to use
     */
    protected void setOrientation(float[] dir) {
        super.setOrientation(dir);

        if (!inSetup)
            updateViewTrans();
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

        transform = new TransformGroup();
        updateViewTrans();

        transform.addChild(impl);

        inSetup = false;
    }
    //----------------------------------------------------------
    // Methods internal to MobileViewpoint
    //----------------------------------------------------------

    /**
     * Updates the TransformGroup fields from the VRML fields
     */
    private void updateViewTrans() {
        axis = new Vector4f(vfOrientation[0], vfOrientation[1], vfOrientation[2],0);
        axis.rotationNormalize();

        implTrans.identity();
        implTrans.rotation(axis);

        trans = new Vector3f(vfPosition[0], vfPosition[1], -vfPosition[2]);

        implTrans.translate(trans);

        transform.setTransform(implTrans);
    }

    /**
     * Private, internal, common iniitialisation.
     */
    private void init() {
        impl = new Viewpoint();

        axis = new Vector4f();
        trans = new Vector3f();
        implTrans = new Matrix4f();
        mat = new Matrix4f();
    }

    // This should be part of the MobileViewpointNodeType
    public Viewpoint getView() {
        return impl;
    }
}
