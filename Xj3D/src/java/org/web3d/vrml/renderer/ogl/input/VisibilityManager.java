/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
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

// External imports
import org.j3d.aviatrix3d.*;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.AxisAngle4f;

import org.j3d.aviatrix3d.picking.PickRequest;
import org.j3d.util.MatrixUtils;

// Local imports
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLSensorNodeType;
import org.web3d.vrml.renderer.ogl.nodes.OGLVisibilityListener;
import org.web3d.vrml.renderer.ogl.nodes.OGLUserData;

/**
 * Handler for all nodes that require visibility information to render
 * correctly.
 * <p>
 *
 * The manager works for all nodes that require visibility information. This
 * can be view-dependent nodes like LOD and Billboard or sensor nodes like
 * VisibilitySensor. All nodes that implement the OGLVisibilityListener
 * interface are directly managed by this class.
 * <p>
 *
 * The implementation uses a single pick to find all the nodes that are
 * currently visible from the user's position and orientation. To do this
 * it uses a cone that is either bounded or unbounded depending on the current
 * visibility limit given by the currently bound NavigationInfo node type.
 * If the vis limit is 0 then an unbounded pick is used. If the scene is really
 * large then anything in that cone of view will be detected and asked to
 * update. This could have quite a large performance impact, so user content
 * should always make sure to set the field value if they have really large
 * content to work with.
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
class VisibilityManager {

    /** Vector that always points along the -Z axis */
    private static final Vector3f NEG_Z_VEC = new Vector3f(0, 0, -1);

    /** The initial size of the children list */
    private static final int LIST_START_SIZE = 25;

    /** The increment size of the list if it gets overflowed */
    private static final int LIST_INCREMENT = 25;

    /** ZEROISH */
    private static final float ZEROISH = 0.000001f;

    /** The root of the SG */
    private Group worldRoot;

    /**
     * View environment data for fetching the current view frustum
     * setup from.
     */
    private ViewEnvironment viewEnv;

    /** Temporary array for fetching the frustum with */
    private double[] viewFrustum;

    /** Two sets used to determine when a viewSensor becomes inactive */
    private HashSet activeVisSensors;

    /** The newly visible sensors this frame. */
    private HashSet newVisSensors;

    /** A scratch list used to avoid garbage generation */
    private OGLVisibilityListener[] list;

    /** Last item in the list */
    private int lastListener;

    /** Pick object for no defined visibilityLimit */
    private PickRequest picker;

    /** Util class representing the end of the picker */
    private Point3f endPoint;

    /** Temp variable for fetching the local transformation values */
    private Matrix4f localTx;

    /** Another temporary matrix while generating the frustum planes*/
    private Matrix4f prjMatrix;

    /** Temp variable used to calculate the orientation input for prjMatrix */
    private AxisAngle4f viewAngle;

    /** Utilities for doing matrix functions */
    private MatrixUtils matrixUtils;

    private float invWorldScale;

    /**
     * Construct a new manager for visiblity sensors.
     */
    VisibilityManager() {

        picker = new PickRequest();
        picker.pickType = PickRequest.FIND_VISIBLES;
        picker.pickGeometryType = PickRequest.PICK_FRUSTUM;
        picker.pickSortType = PickRequest.SORT_ALL;
        picker.generateVWorldMatrix = true;
        picker.origin = new float[24];

        viewFrustum = new double[6];

        endPoint = new Point3f();
        viewAngle = new AxisAngle4f();
        localTx = new Matrix4f();
        prjMatrix = new Matrix4f();

        activeVisSensors = new HashSet();
        newVisSensors = new HashSet();

        matrixUtils = new MatrixUtils();
        invWorldScale = 1;

        list = new OGLVisibilityListener[LIST_START_SIZE];
        lastListener = 0;
    }

    /**
     * Add a new sensor node instance to this handler.
     *
     * @param node The node instance to add
     */
    void addSensor(VRMLSensorNodeType node) {
    }

    /**
     * Remove a sensor node instance from this handler.
     *
     * @param node The node instance to remove
     */
    void removeSensor(VRMLSensorNodeType node) {
    }

    /**
     * Set the branchgroup that acts as the world root for doing picking
     * against.
     *
     * @param root The world root to pick against
     */
    void setWorldRoot(Group root) {
        worldRoot = root;
    }

    /**
     * Set the container for the view environment data. Used to process the
     * view frustum when doing visibilty sensor handling.
     *
     * @param data The current env data to use
     */
    void setViewEnvironment(ViewEnvironment data) {
        viewEnv = data;
    }

    /**
     * Clear the current list of items internally. This is because the world
     * has changed and that the currently cached data is no longer usable.
     */
    void clear() {
        activeVisSensors.clear();
        newVisSensors.clear();
		ArrayList pick_list = (ArrayList)picker.foundPaths;
		if ( pick_list != null ) {
			pick_list.clear();
		}
    }

    /**
     * Process this frame using the given position and orientation of the
     * user position in Virtual world coordinates.
     *
     * @param pos The position of the user
     * @param orient The orientation of the user
     * @param limit The visibility limit
     */
    void processFrame(Point3f pos, AxisAngle4f orient, Matrix4f vpMatrix) {

        if(worldRoot == null)
            return;

        // This is required due to pipeline nature of aviatrix, do not remove
        generateFrustumPlanes(vpMatrix);
		ArrayList pick_list = (ArrayList)picker.foundPaths;
		if ( pick_list != null ) {
			pick_list.clear();
		}
        worldRoot.pickSingle(picker);

        if(picker.pickCount != 0) {
            // Clear the visited entries for active VisSensors
            newVisSensors.clear();
            //ArrayList pick_list = (ArrayList)picker.foundPaths;
			pick_list = (ArrayList)picker.foundPaths;

            for(int i = 0; i < picker.pickCount; i++) {
                SceneGraphPath found = (SceneGraphPath)pick_list.get(i);

                Node n = found.getTerminalNode();
                OGLUserData user_data = (OGLUserData)n.getUserData();

                if((user_data != null) &&
                   (user_data.visibilityListener != null)) {

                    OGLVisibilityListener l = user_data.visibilityListener;

                    found.getTransform(localTx);

                    if(activeVisSensors.contains(l)) {
                        l.viewPositionChanged(pos, orient, localTx);
                        activeVisSensors.remove(l);
                    } else
                        l.visibilityStateChange(true, pos, orient, localTx);

                    newVisSensors.add(l);
                }
            }
        }

        // Remove unvisited entries
        int size = activeVisSensors.size();

        if(size != 0) {
            resizeList(size);
            activeVisSensors.toArray(list);

            for(int i = 0; i < size; i++) {
                if(list[i] == null)
                    break;

                OGLVisibilityListener l = (OGLVisibilityListener)list[i];
                l.visibilityStateChange(false, pos, orient, null);
                activeVisSensors.remove(l);
            }
		} else {
			java.util.Arrays.fill(list, null);
		}

        // Swap over lists
        HashSet tmp = activeVisSensors;

        activeVisSensors = newVisSensors;
        newVisSensors = tmp;
    }

    /**
     * Set the world scale applied.  This will scale down navinfo parameters
     * to fit into the world.
     *
     * @param scale The new world scale.
     */
    void setWorldScale(float scale) {
        invWorldScale = 1f / scale;
    }

    //----------------------------------------------------------
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Resize the list if needed. Marked as final in order to encourage the
     * compiler to inline the code for faster execution
     */
    private final void resizeList(int maxSize) {

        if(list.length < maxSize) {
            int old_size = list.length;
            int new_size = old_size + LIST_INCREMENT;

            OGLVisibilityListener[] tmp = new OGLVisibilityListener[new_size];

            System.arraycopy(list, 0, tmp, 0, old_size);

            list = tmp;
		} else {
			java.util.Arrays.fill(list, (maxSize-1), (list.length-1), null);
		}
    }

    /**
     * Generate the frustum planes that will be used for the frustum picking.
     */
    private void generateFrustumPlanes(Matrix4f vpMatrix) {
        float[] frustumPlanes = picker.origin;

        // Create projection matrix
        viewEnv.generateViewFrustum(viewFrustum);

        float left = (float)viewFrustum[0];
        float right = (float)viewFrustum[1];
        float bottom = (float)viewFrustum[2];
        float top = (float)viewFrustum[3];
        float nearval = (float)viewFrustum[4];
        float farval = (float)viewFrustum[5];

        float x, y, z, w;
        float a, b, c, d;

        x = (2.0f * nearval) / (right - left);
        y = (2.0f * nearval) / (top - bottom);
        a = (right + left) / (right - left);
        b = (top + bottom) / (top - bottom);
        c = -(farval + nearval) / ( farval - nearval);
        d = -(2.0f * farval * nearval) / (farval - nearval);

        prjMatrix.m00 = x;
        prjMatrix.m01 = 0;
        prjMatrix.m02 = a;
        prjMatrix.m03 = 0;
        prjMatrix.m10 = 0;
        prjMatrix.m11 = y;
        prjMatrix.m12 = b;
        prjMatrix.m13 = 0;
        prjMatrix.m20 = 0;
        prjMatrix.m21 = 0;
        prjMatrix.m22 = c;
        prjMatrix.m23 = d;
        prjMatrix.m30 = 0;
        prjMatrix.m31 = 0;
        prjMatrix.m32 = -1;
        prjMatrix.m33 = 0;

        matrixUtils.inverse(vpMatrix, localTx);
        localTx.mul(prjMatrix, localTx);

        prjMatrix.m00 = localTx.m00;
        prjMatrix.m01 = localTx.m10;
        prjMatrix.m02 = localTx.m20;
        prjMatrix.m03 = localTx.m30;
        prjMatrix.m10 = localTx.m01;
        prjMatrix.m11 = localTx.m11;
        prjMatrix.m12 = localTx.m21;
        prjMatrix.m13 = localTx.m31;
        prjMatrix.m20 = localTx.m02;
        prjMatrix.m21 = localTx.m12;
        prjMatrix.m22 = localTx.m22;
        prjMatrix.m23 = localTx.m32;
        prjMatrix.m30 = localTx.m03;
        prjMatrix.m31 = localTx.m13;
        prjMatrix.m32 = localTx.m23;
        prjMatrix.m33 = localTx.m33;

        float t;
        float len;
        // Extract the numbers for the RIGHT plane
        x = prjMatrix.m03 - prjMatrix.m00;
        y = prjMatrix.m13 - prjMatrix.m10;
        z = prjMatrix.m23 - prjMatrix.m20;
        w = prjMatrix.m33 - prjMatrix.m30;

        // Normalize the result
        len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > ZEROISH)
            t = 1 / len;
        else
            t = 1;

        frustumPlanes[0] = x * t;
        frustumPlanes[1] = y * t;
        frustumPlanes[2] = z * t;
        frustumPlanes[3] = w * t;

        // Extract the numbers for the LEFT plane
        x = prjMatrix.m03 + prjMatrix.m00;
        y = prjMatrix.m13 + prjMatrix.m10;
        z = prjMatrix.m23 + prjMatrix.m20;
        w = prjMatrix.m33 + prjMatrix.m30;

        len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > ZEROISH)
            t = 1 / len;
        else
            t = 1;

        frustumPlanes[4] = x * t;
        frustumPlanes[5] = y * t;
        frustumPlanes[6] = z * t;
        frustumPlanes[7] = w * t;

        // Extract the BOTTOM plane
        x = prjMatrix.m03 + prjMatrix.m01;
        y = prjMatrix.m13 + prjMatrix.m11;
        z = prjMatrix.m23 + prjMatrix.m21;
        w = prjMatrix.m33 + prjMatrix.m31;

        len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > ZEROISH)
            t = 1 / len;
        else
            t = 1;

        frustumPlanes[8] = x * t;
        frustumPlanes[9] = y * t;
        frustumPlanes[10] = z * t;
        frustumPlanes[11] = w * t;

        // Extract the TOP plane
        x = prjMatrix.m03 - prjMatrix.m01;
        y = prjMatrix.m13 - prjMatrix.m11;
        z = prjMatrix.m23 - prjMatrix.m21;
        w = prjMatrix.m33 - prjMatrix.m31;

        len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > ZEROISH)
            t = 1 / len;
        else
            t = 1;

        frustumPlanes[12] = x * t;
        frustumPlanes[13] = y * t;
        frustumPlanes[14] = z * t;
        frustumPlanes[15] = w * t;

        // Extract the FAR plane
        x = prjMatrix.m03 - prjMatrix.m02;
        y = prjMatrix.m13 - prjMatrix.m12;
        z = prjMatrix.m23 - prjMatrix.m22;
        w = prjMatrix.m33 - prjMatrix.m32;

        len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > ZEROISH)
            t = 1 / len;
        else
            t = 1;

        frustumPlanes[16] = x * t;
        frustumPlanes[17] = y * t;
        frustumPlanes[18] = z * t;
        frustumPlanes[19] = w * t;

        // Extract the NEAR plane
        x = prjMatrix.m03 + prjMatrix.m02;
        y = prjMatrix.m13 + prjMatrix.m12;
        z = prjMatrix.m23 + prjMatrix.m22;
        w = prjMatrix.m33 + prjMatrix.m32;

        len = (float) Math.sqrt(x * x + y * y + z * z);
        if (len > ZEROISH)
            t = 1 / len;
        else
            t = 1;

        frustumPlanes[20] = x * t;
        frustumPlanes[21] = y * t;
        frustumPlanes[22] = z * t;
        frustumPlanes[23] = w * t;

/*
System.out.println("Frustum Planes:");
for(int i=0; i < 24; i++)
   System.out.println(frustumPlanes[i]);
*/
    }
}
