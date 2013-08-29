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

package org.web3d.vrml.renderer.ogl.nodes.environment;

// External imports
import org.j3d.aviatrix3d.*;

import java.util.Map;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.j3d.geom.GeometryData;
import org.j3d.util.MatrixUtils;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLClock;

import org.web3d.vrml.renderer.ogl.nodes.OGLAreaListener;
import org.web3d.vrml.renderer.ogl.nodes.OGLGlobalStatus;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.common.nodes.environment.BaseProximitySensor;

/**
 * OpenGL implementation of a ProximitySensor node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public class OGLProximitySensor extends BaseProximitySensor
    implements OGLVRMLNode, OGLAreaListener, NodeUpdateListener {

    /** The basic shape object used for this node */
    private Shape3D shape;

    /** The geometry instance we use to represent the box */
    private QuadArray geometry;

    /** Scratch var for transfering size(scale) values */
    private Matrix3f scale;

    /** Scratch var for transfering center(translation) values */
    private Vector3f translation;

    /** Scratch var for accessing the orientation of the local space */
    private Vector3f direction;

    /** Scratch var for determining local orientation */
    private AxisAngle4f axisAngle;

    /** Geometry data associated with the basic box */
    private GeometryData geomData;

    /**
     * Last position in local coordinates that we had to compare against
     * the current one. Used to decide when we should generate new eventOuts.
     */
    private Vector3f lastPosition;

    /**
     * Last position in local coordinates that we had to compare against
     * the current one. Used to decide when we should generate new eventOuts.
     */
    private AxisAngle4f lastOrientation;

    /** MatrixUtils for gc free inversion */
    private static MatrixUtils matrixUtils;

    /** Scratch var for multiplying matrices */
    private static Matrix4f tmpMatrix;

    static {
        matrixUtils = new MatrixUtils();
        tmpMatrix = new Matrix4f();
    }

    /**
     * Construct a new proximity sensor object
     */
    public OGLProximitySensor() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type
     */
    public OGLProximitySensor(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods from VRMLSensorNodeType
    //-------------------------------------------------------------

    /**
     * Set the sensor enabled or disabled.
     *
     * @param state true if the sensor is to be enabled
     */
    public void setEnabled(boolean state) {
        super.setEnabled(state);

        // turn pickable on or off based on the enabled state. If the node
        // is not enabled then by turning picking off, it won't be found during
        // each frame's iteration.

        if(state)
            shape.setPickMask(Shape3D.PROXIMITY_OBJECT);
        else
            shape.setPickMask(0);
    }

    //-------------------------------------------------------------
    // Methods from OGLVRMLNode
    //-------------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The OGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return shape;
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

        updateShape();

        // Wait till the sensor is added to SG
        shape.setPickMask(0);
    }

    //-------------------------------------------------------------
    // Methods from OGLAreaListener
    //-------------------------------------------------------------

    /**
     * Invoked when the user enters an area.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void areaEntry(Point3f position,
                          Vector3f orientation,
                          Matrix4f vpMatrix,
                          Matrix4f localPosition) {

        vfEnterTime = vrmlClock.getTime();
        updatePositionOutputs(position, orientation, vpMatrix, localPosition, false);

        hasChanged[FIELD_ENTER_TIME] = true;
        fireFieldChanged(FIELD_ENTER_TIME);

        vfIsActive = true;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
    }

    /**
     * Notification that the user is still in the area, but that the
     * viewer reference point has changed.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void userPositionChanged(Point3f position,
                                    Vector3f orientation,
                                    Matrix4f vpMatrix,
                                    Matrix4f localPosition) {
        updatePositionOutputs(position, orientation, vpMatrix, localPosition, true);
    }

    /**
     * Invoked when the tracked object exits on area.
     */
    public void areaExit() {
        vfExitTime = vrmlClock.getTime();

        hasChanged[FIELD_EXIT_TIME] = true;
        fireFieldChanged(FIELD_EXIT_TIME);

        vfIsActive = false;
        hasChanged[FIELD_IS_ACTIVE] = true;
        fireFieldChanged(FIELD_IS_ACTIVE);
    }

    //-------------------------------------------------------------------
    // Methods overriden from VRMLTimeDependentNodeType
    //-------------------------------------------------------------------

    /**
     * Set the vrmlClock that this time dependent node will be running with.
     *
     * @param clk The vrmlClock to use for this node
     */
    public void setVRMLClock(VRMLClock clk) {
        super.setVRMLClock(clk);

        if(vfEnabled)
            shape.setPickMask(Shape3D.PROXIMITY_OBJECT);
        else
            shape.setPickMask(0);
    }

    //-------------------------------------------------------------
    // Methods defined by BaseProximitySensor
    //-------------------------------------------------------------

    /**
     * Update the size of the sensor. May be overridden by derived classes
     * but should make sure to call this as well
     *
     * @param val The new size to use
     */
    protected void setSize(float[] val) {
        super.setSize(val);

        if (!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Update the center of the sensor. May be overridden by derived classes
     * but should make sure to call this as well
     *
     * @param val The new center to use
     */
    protected void setCenter(float[] val) {
        super.setCenter(val);

        if (!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame.
     */
    public void allEventsComplete() {
        updateShape();
    }

    //----------------------------------------------------------
    // Methods required by the NodeUpdateListener interface.
    //----------------------------------------------------------

    /**
     * Notification that its safe to update the node now with any operations
     * that could potentially effect the node's bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeBoundsChanges(Object src) {
        geometry.setVertices(QuadArray.COORDINATE_3,
                             geomData.coordinates,
                             geomData.vertexCount);
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
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Commmon internal setup code.
     */
    private void init() {
        scale = new Matrix3f();
        translation = new Vector3f();
        direction = new Vector3f();
        axisAngle = new AxisAngle4f();
        lastPosition = new Vector3f();
        lastOrientation = new AxisAngle4f();

        // setup the local user data to be not part of the collision
        // system.
        geomData = new GeometryData();
        geomData.geometryType = GeometryData.QUADS;
        geomData.coordinates = new float[4 * 6 * 3];
        geomData.vertexCount = 4 * 6;

        OGLUserData user_data = new OGLUserData();
        user_data.geometryData = geomData;
        user_data.collidable = false;
        user_data.isTerrain = false;
        user_data.areaListener = this;

        geometry = new QuadArray();

        shape = new Shape3D();
        shape.setAppearance(OGLGlobalStatus.invisibleAppearance);
        shape.setGeometry(geometry);
        shape.setUserData(user_data);

    }

    /**
     * Update the transformation on the box now.
     */
    private void updateShape() {
        float[] coord = geomData.coordinates;
        float x = vfSize[0] * 0.5f;
        float y = vfSize[1] * 0.5f;
        float z = vfSize[2] * 0.5f;

        // face 1: +ve Z axis
        coord[0] =  x + vfCenter[0];
        coord[1] = -y + vfCenter[1];
        coord[2] =  z + vfCenter[2];

        coord[3] =  x + vfCenter[0];
        coord[4] =  y + vfCenter[1];
        coord[5] =  z + vfCenter[2];

        coord[6] = -x + vfCenter[0];
        coord[7] =  y + vfCenter[1];
        coord[8] =  z + vfCenter[2];

        coord[9] = -x + vfCenter[0];
        coord[10] = -y + vfCenter[1];
        coord[11] =  z + vfCenter[2];

        // face 2: +ve X axis
        coord[12] =  x + vfCenter[0];
        coord[13] = -y + vfCenter[1];
        coord[14] = -z + vfCenter[2];

        coord[15] =  x + vfCenter[0];
        coord[16] =  y + vfCenter[1];
        coord[17] = -z + vfCenter[2];

        coord[18] =  x + vfCenter[0];
        coord[19] =  y + vfCenter[1];
        coord[20] =  z + vfCenter[2];

        coord[21] =  x + vfCenter[0];
        coord[22] = -y + vfCenter[1];
        coord[23] =  z + vfCenter[2];

        // face 3: -ve Z axis
        coord[24] = -x + vfCenter[0];
        coord[25] = -y + vfCenter[1];
        coord[26] = -z + vfCenter[2];

        coord[27] = -x + vfCenter[0];
        coord[28] =  y + vfCenter[1];
        coord[29] = -z + vfCenter[2];

        coord[30] =  x + vfCenter[0];
        coord[31] =  y + vfCenter[1];
        coord[32] = -z + vfCenter[2];

        coord[33] =  x + vfCenter[0];
        coord[34] = -y + vfCenter[1];
        coord[35] = -z + vfCenter[2];

        // face 4: -ve X axis
        coord[36] = -x + vfCenter[0];
        coord[37] = -y + vfCenter[1];
        coord[38] =  z + vfCenter[2];

        coord[39] = -x + vfCenter[0];
        coord[40] =  y + vfCenter[1];
        coord[41] =  z + vfCenter[2];

        coord[42] = -x + vfCenter[0];
        coord[43] =  y + vfCenter[1];
        coord[44] = -z + vfCenter[2];

        coord[45] = -x + vfCenter[0];
        coord[46] = -y + vfCenter[1];
        coord[47] = -z + vfCenter[2];

        // face 5: +ve Y axis
        coord[48] =  x + vfCenter[0];
        coord[49] =  y + vfCenter[1];
        coord[50] =  z + vfCenter[2];

        coord[51] =  x + vfCenter[0];
        coord[52] =  y + vfCenter[1];
        coord[53] = -z + vfCenter[2];

        coord[54] = -x + vfCenter[0];
        coord[55] =  y + vfCenter[1];
        coord[56] = -z + vfCenter[2];

        coord[57] = -x + vfCenter[0];
        coord[58] =  y + vfCenter[1];
        coord[59] =  z + vfCenter[2];

        // face 6: -ve Y axis
        coord[60] = -x + vfCenter[0];
        coord[61] = -y + vfCenter[1];
        coord[62] = -z + vfCenter[2];

        coord[63] = -x + vfCenter[0];
        coord[64] = -y + vfCenter[1];
        coord[65] =  z + vfCenter[2];

        coord[66] =  x + vfCenter[0];
        coord[67] = -y + vfCenter[1];
        coord[68] =  z + vfCenter[2];

        coord[69] =  x + vfCenter[0];
        coord[70] = -y + vfCenter[1];
        coord[71] = -z + vfCenter[2];

        if(geometry.isLive()) {
            geometry.boundsChanged(this);
        } else {
            geometry.setVertices(QuadArray.COORDINATE_3,
                                 geomData.coordinates,
                                 geomData.vertexCount);
        }
    }

    /**
     * Calculate the user position and orientation relative to this sensor
     * from the given world coordinate values.
     *
     * @param vpos The position of the user in vworld coords
     * @param vorient The orientation of the user in vworld coords
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     * @param checkLast true if this should check against the last values
     *    before issuing events
     */
    private void updatePositionOutputs(Point3f vpos,
                                       Vector3f vorient,
                                       Matrix4f vpMatrix,
                                       Matrix4f localPosition,
                                       boolean checkLast) {

        tmpMatrix.mul(vpMatrix, localPosition);
        matrixUtils.inverse(tmpMatrix, tmpMatrix);
        tmpMatrix.get(translation);
        axisAngle.set(tmpMatrix);

        if(!checkLast || !axisAngle.equals(lastOrientation)) {
            vfOrientationChanged[0] = axisAngle.x;
            vfOrientationChanged[1] = axisAngle.y;
            vfOrientationChanged[2] = axisAngle.z;
            vfOrientationChanged[3] = axisAngle.angle;

            hasChanged[FIELD_ORIENTATION_CHANGED] = true;
            fireFieldChanged(FIELD_ORIENTATION_CHANGED);

            lastOrientation.set(axisAngle);
        }

        if(!checkLast || !translation.equals(lastPosition)) {
            vfPositionChanged[0] = translation.x;
            vfPositionChanged[1] = translation.y;
            vfPositionChanged[2] = translation.z;

            hasChanged[FIELD_POSITION_CHANGED] = true;
            fireFieldChanged(FIELD_POSITION_CHANGED);

            lastPosition.set(translation);
        }
    }
}
