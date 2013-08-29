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

package org.web3d.vrml.renderer.j3d.nodes.environment;

// Standard imports
import javax.media.j3d.*;

import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.AxisAngle4d;

import org.j3d.geom.GeometryData;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLClock;

import org.web3d.vrml.renderer.j3d.nodes.J3DVisibilityListener;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;
import org.web3d.vrml.renderer.common.nodes.environment.BaseVisibilitySensor;

/**
 * Java3D implementation of a VisibilitySensor node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public class J3DVisibilitySensor extends BaseVisibilitySensor
    implements J3DVRMLNode,
               J3DVisibilityListener {

    /** BranchGroup for containing everything */
    private BranchGroup implGroup;

    /** The basic shape object used for this node */
    private Shape3D shape;

    /** The geometry instance we use to represent the box */
    private QuadArray geometry;

    /** Scratch var for transfering size(scale) values */
    private Vector3d scale;

    /** Scratch var for transfering center(translation) values */
    private Vector3d translation;

    /** Geometry data associated with the basic box */
    private GeometryData geomData;

    /**
     * Construct a new visibility sensor object
     */
    public J3DVisibilitySensor() {
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
    public J3DVisibilitySensor(VRMLNodeType node) {
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
        implGroup.setPickable(state);
    }

    //-------------------------------------------------------------
    // Methods from VRMLNodeType
    //-------------------------------------------------------------

    /**
     * Set the version of VRML that this node should represent. Different
     * versions have different capabilities, even within the same node.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     * @param isStatic true if this node is under a static group and won't
     *    change after the setup is finished
     */
    public void setVersion(int major, int minor, boolean isStatic) {
        super.setVersion(major, minor, isStatic);

        if(isStatic)
            return;

        geometry.setCapability(QuadArray.ALLOW_COORDINATE_WRITE);
        geometry.clearCapabilityIsFrequent(QuadArray.ALLOW_COORDINATE_WRITE);

        implGroup.setCapability(BranchGroup.ALLOW_DETACH);
        implGroup.setCapability(BranchGroup.ALLOW_PICKABLE_WRITE);
    }

    //-------------------------------------------------------------
    // Methods from J3DVRMLNode
    //-------------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implGroup;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Ignored by this implementation currently.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            if(capBits.containsKey(BranchGroup.class)) {
                bits = (int[])capBits.get(BranchGroup.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        implGroup.clearCapability(bits[i]);
                } else if(!isStatic) {
                    implGroup.clearCapability(BranchGroup.ALLOW_DETACH);
                    implGroup.clearCapability(BranchGroup.ALLOW_PICKABLE_WRITE);
                }
            }

            if(capBits.containsKey(QuadArray.class)) {
                bits = (int[])capBits.get(QuadArray.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        implGroup.clearCapability(bits[i]);
                } else if(!isStatic) {
                    geometry.clearCapability(
                        QuadArray.ALLOW_COORDINATE_WRITE);
                }
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        if(freqBits.containsKey(BranchGroup.class)) {
            bits = (int[])freqBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implGroup.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                implGroup.clearCapabilityIsFrequent(BranchGroup.ALLOW_DETACH);
                implGroup.clearCapabilityIsFrequent(
                    BranchGroup.ALLOW_PICKABLE_WRITE);
            }
        }

        if(freqBits.containsKey(QuadArray.class)) {
            bits = (int[])freqBits.get(QuadArray.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    implGroup.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                geometry.clearCapabilityIsFrequent(
                    QuadArray.ALLOW_COORDINATE_WRITE);
            }
        }
    }

    /**
     * Set the mapping of capability bits that the user would like to
     * make sure is set. Ignored by this implementation currently.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            bits = (int[])capBits.get(Shape3D.class);
            size = (bits == null) ? 0 : bits.length;

            for(i = 0; i < size; i++)
                shape.setCapability(bits[i]);

            bits = (int[])capBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            for(i = 0; i < size; i++)
                implGroup.setCapability(bits[i]);

            bits = (int[])capBits.get(QuadArray.class);
            size = (bits == null) ? 0 : bits.length;

            for(i = 0; i < size; i++)
                geometry.setCapability(bits[i]);
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        bits = (int[])freqBits.get(Shape3D.class);
        size = (bits == null) ? 0 : bits.length;

        for(i = 0; i < size; i++)
            shape.setCapabilityIsFrequent(bits[i]);

        bits = (int[])freqBits.get(BranchGroup.class);
        size = (bits == null) ? 0 : bits.length;

        for(i = 0; i < size; i++)
            implGroup.setCapabilityIsFrequent(bits[i]);

        bits = (int[])freqBits.get(QuadArray.class);
        size = (bits == null) ? 0 : bits.length;

        for(i = 0; i < size; i++)
            geometry.setCapabilityIsFrequent(bits[i]);
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
        implGroup.setPickable(false);
    }

    //-------------------------------------------------------------
    // Methods from J3DVisibilityListener
    //-------------------------------------------------------------

    /**
     * Invoked when the user enters or leaves an area.
     *
     * @param visible true when the user enters the area
     * @param position The position of the user on entry/exit
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void visibilityStateChange(boolean visible,
                                      Point3d position,
                                      AxisAngle4d orientation,
                                      Transform3D localPosition) {

        if(visible) {
            vfEnterTime = vrmlClock.getTime();

            hasChanged[FIELD_ENTER_TIME] = true;
            fireFieldChanged(FIELD_ENTER_TIME);

            vfIsActive = true;
            hasChanged[FIELD_IS_ACTIVE] = true;
        } else {
            vfExitTime = vrmlClock.getTime();

            hasChanged[FIELD_EXIT_TIME] = true;
            fireFieldChanged(FIELD_EXIT_TIME);

            vfIsActive = false;
            hasChanged[FIELD_IS_ACTIVE] = true;
        }

        fireFieldChanged(FIELD_IS_ACTIVE);
    }

    /**
     * Notification that the object is still visible, but that the
     * viewer reference point has changed. Ignored for this implementation.
     *
     * @param position The new position of the user
     * @param orientation The orientation of the user there
     * @param localPosition The vworld transform object for the class
     *   that implemented this listener
     */
    public void viewPositionChanged(Point3d position,
                                      AxisAngle4d orientation,
                                     Transform3D localPosition) {
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
        implGroup.setPickable(vfEnabled);
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
    // Local convenience methods
    //----------------------------------------------------------

    /**
     * Commmon internal setup code.
     */
    private void init() {
        scale = new Vector3d();
        translation = new Vector3d();

        // setup the local user data to be not part of the collision
        // system.
        geomData = new GeometryData();
        geomData.geometryType = GeometryData.QUADS;
        geomData.coordinates = new float[4 * 6 * 3];
        geomData.vertexCount = 4 * 6;

        // setup the local user data to be not part of the collision
        // system.
        J3DUserData user_data = new J3DUserData();
        user_data.geometryData = geomData;
        user_data.collidable = false;
        user_data.isTerrain = false;
        user_data.visibilityListener = this;

        geometry = new QuadArray(24, QuadArray.COORDINATES);

        shape = new Shape3D();
        shape.setAppearance(J3DGlobalStatus.invisibleAppearance);
        shape.setGeometry(geometry);
        shape.setUserData(user_data);

        implGroup = new BranchGroup();
        implGroup.addChild(shape);
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

        geometry.setCoordinates(0, geomData.coordinates);
    }
}
