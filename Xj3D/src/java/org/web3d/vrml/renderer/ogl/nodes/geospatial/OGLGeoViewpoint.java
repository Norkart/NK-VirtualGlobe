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

package org.web3d.vrml.renderer.ogl.nodes.geospatial;

// External imports
import javax.vecmath.AxisAngle4f;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Quat4d;

import org.j3d.aviatrix3d.BoundingVoid;
import org.j3d.aviatrix3d.NodeUpdateListener;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.TransformGroup;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.geospatial.BaseGeoViewpoint;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLViewpointNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLTransformNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;

/**
 * OpenGL implementation of an GeoViewpoint
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public class OGLGeoViewpoint extends BaseGeoViewpoint
    implements OGLViewpointNodeType, NodeUpdateListener, OGLTransformNodeType {

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Message when the mathTransform.transform() fails */
    private static final String TRANSFORM_ERR_MSG =
        "Unable to transform the geoCoord value for some reason.";

    /** The transform group that holds the viewpoint */
    private TransformGroup transform;

    /** Flag to say the tx matrix has changed */
    private boolean matrixChanged;

    private Vector3f trans;

    /** Matrix that represents the local offsets */
    private Matrix4f implTrans;

    /** A tmp matrix for reading back actual navigated values */
    private Matrix4f tmpMatrix;

    /** The world scale */
    private float worldScale;

    /** Has the worldScale changed */
    private boolean scaleChanged;

    /** Has the matrix changed */
    private boolean updateMatrix;

    /**
     * Construct a default geoviewpoint instance
     */
    public OGLGeoViewpoint() {
        super();
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLGeoViewpoint(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
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
    // Methods defined by BaseViewpoint
    //----------------------------------------------------------

    /**
     * Convenience method to set the position of the viewpoint.
     *
     * @param pos The position vector to use
     */
    protected void setPosition(double[] pos) {
        super.setPosition(pos);

        if (inSetup)
            return;

        updateMatrix = true;

        updateViewTrans();

        if (transform.isLive())
            transform.boundsChanged(this);
        else
            updateNodeBoundsChanges(transform);
    }

    /**
     * Convenience method to set the orientation of the viewpoint.
     *
     * @param dir The orientation quaternion to use
     */
    protected void setOrientation(float[] dir) {
        super.setOrientation(dir);

        if (inSetup)
            return;

        updateMatrix = true;

        updateViewTrans();

        if (transform.isLive())
            transform.boundsChanged(this);
        else
            updateNodeBoundsChanges(transform);
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

        OGLUserData data = new OGLUserData();
        transform.setUserData(data);
        data.owner = this;

        updateViewTrans();

        transform.setTransform(implTrans);
    }

    //----------------------------------------------------------
    // Methods defined by NodeUpdateListener
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        if (updateMatrix) {
            //updateViewTrans();
            transform.setTransform(implTrans);

            scaleChanged = false;
            updateMatrix = false;
        } else if (scaleChanged) {
            // TODO: Restore this logic when we fix
            scaleChanged = false;
        }
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
    }

    //----------------------------------------------------------
    // Methods defined by OGLTransformNodeType
    //----------------------------------------------------------

    /**
     * Get the transform matrix for this node.  A reference is ok as
     * the users of this method will not modify the matrix.
     *
     * @return The matrix.
     */
    public Matrix4f getTransform() {
        return implTrans;
    }

    //----------------------------------------------------------
    // Internal Methods
    //----------------------------------------------------------

    /**
     * Get the default Transform representation of this viewpoint based on
     * its current position and orientation values. This is used to reset the
     * viewpoint to the original position after the user has moved around or
     * we transition between two viewpoints. It should remain independent of
     * the underlying TransformGroup.
     *
     * @return The default transform of this viewpoint
     */
    public Matrix4f getViewTransform() {
        updateMatrix = true;
        updateViewTrans();

        return implTrans;
    }

    /**
     * Get the parent transform used to control the view platform. Used for
     * the navigation controls.
     *
     * @return The current view TransformGroup
     */
    public TransformGroup getPlatformGroup() {
        return transform;
    }

    /**
     * Set a new transform for this viewpoint.  Used to notify
     * vp of navigation changes.
     *
     * @param trans The view transform
     */
    public void setNavigationTransform(Matrix4f trans) {
        implTrans.set(trans);
    }

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    public void setWorldScale(float scale) {
        worldScale = scale;

        if(!inSetup) {
            scaleChanged = true;

            if (transform.isLive())
                transform.boundsChanged(this);
            else
                updateNodeBoundsChanges(transform);
        }
    }

    /**
     * Updates the TransformGroup fields from the VRML fields
     */
    private void updateViewTrans() {

        convOriToAxisAngle();
        implTrans.setIdentity();
        implTrans.set(axis);


        trans.x = (float)localPosition[0];
        trans.y = (float)localPosition[1];
        trans.z = (float)localPosition[2];

        implTrans.setTranslation(trans);
        implTrans.setScale(1f / worldScale);
    }

    /**
     * Private, internal, common iniitialisation.
     */
    private void init() {
        trans = new Vector3f();
        implTrans = new Matrix4f();
        tmpMatrix = new Matrix4f();
        implTrans.setIdentity();
        worldScale = 1;

        transform = new TransformGroup();
        transform.setPickMask(0);

        transform.setTransform(implTrans);
        transform.setBounds(new BoundingVoid());
    }
}
