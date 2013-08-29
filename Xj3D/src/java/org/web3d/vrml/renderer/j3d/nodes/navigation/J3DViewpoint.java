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

package org.web3d.vrml.renderer.j3d.nodes.navigation;

// Standard imports
import javax.media.j3d.*;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ObjectArray;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.renderer.common.nodes.navigation.BaseViewpoint;
import org.web3d.vrml.renderer.j3d.nodes.J3DViewpointNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DPathAwareNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DParentPathRequestHandler;

/**
 * Java3D implementation of a Viewpoint node.
 * <p>
 *
 * VRML requires the use of a headlight from the NavigationInfo node.
 * For convenience, we provide a headlight here that binds with the same
 * transform as the view platform.
 * <p>
 *
 * Viewpoints cannot be shared using DEF/USE. They may be named as such for
 * Anchor purposes, but attempting to reuse them will cause an error. This
 * implementation does not provide any protection against USE of this node
 * and attempting to do so will result in Java3D throwing exceptions - most
 * probably in the grouping node that includes this node.
 *
 * @author Alan Hudson
 * @version $Revision: 1.12 $
 */
public class J3DViewpoint extends BaseViewpoint
    implements J3DViewpointNodeType, J3DPathAwareNodeType {

    /** The transform group that holds the viewpoint */
    private TransformGroup transform;

    /** The implementation node that is returned to a caller */
    private Group j3dImplNode;

    /** This is the current parent path pointer used to construct the path */
    private J3DParentPathRequestHandler parentPathHandler;

    /** A listing of all path handlers registered */
    private ObjectArray allParentPaths;

    // Class variables for speed/memory usage
    private AxisAngle4f axis;
    private Vector3f trans;
    private Transform3D implTrans;

    // Temp arrays for copying stuff for the scene graph path
    private Object[] tmpPathArray;
    private Node[] tmpNodeArray;

    /**
     * Construct a default viewpoint instance.
     */
    public J3DViewpoint() {
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
    public J3DViewpoint(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods required by the J3DViewpointNodeType interface.
    //-------------------------------------------------------------

    /**
     * A check to see if the parent scene graph path has changed from last
     * time we checked for this node. Assumes that the call is being made on
     * a node that we checked on last frame. If this has been just changed with
     * a new binding call then the caller should just immediately request the
     * current path anyway.
     *
     * @return true if the parent path has changed since last frame
     */
    public boolean hasScenePathChanged() {
        if(parentPathHandler == null)
            return true;
        else
            return parentPathHandler.hasParentPathChanged();
    }

    /**
     * Fetch the scene graph path from the root of the scene to this node.
     * Typically used for the getLocalToVWorld transformation handling. If
     * the node returns null then there is no path to the root of the scene
     * ie this node is somehow orphaned during the last frame.
     *
     * @return The fully qualified path from the root to here or null
     */
    public SceneGraphPath getSceneGraphPath() {
        if(parentPathHandler == null) {
            if(allParentPaths.size() == 0)
                return null;
            else
                parentPathHandler =
                    (J3DParentPathRequestHandler)allParentPaths.get(0);
        }

        ObjectArray path_array = parentPathHandler.getParentPath(this);

        if(path_array == null)
            return null;

        int path_size = path_array.size();
        if((tmpPathArray == null) || tmpPathArray.length < path_size) {
            tmpPathArray = new Object[path_size];
            tmpNodeArray = new Node[path_size - 1];
        }

        path_array.toArray(tmpPathArray);
        Locale locale = (Locale)tmpPathArray[0];
        for(int i = 1; i < path_size; i++)
            tmpNodeArray[i - 1] = (Node)tmpPathArray[i];

        return new SceneGraphPath(locale, tmpNodeArray, transform);
    }

    /**
     * Get the default Transform representation of this viewpoint based on
     * its current position and orientation values. This is used to reset the
     * viewpoint to the original position after the user has moved around or
     * we transition between two viewpoints. It should remain independent of
     * the underlying TransformGroup.
     *
     * @return The default transform of this viewpoint
     */
    public Transform3D getViewTransform() {
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

    //----------------------------------------------------------
    // Methods from the J3DPathAwareNodeType interface.
    //----------------------------------------------------------

    /**
     * Add a handler for the parent path requesting. If the request is made
     * more than once, extra copies should be added (for example a  DEF and USE
     * of the same node in the same children field of a Group).
     *
     * @param h The new handler to add
     */
    public void addParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.add(h);
    }

    /**
     * Remove a handler for the parent path requesting. If there are multiple
     * copies of this handler registered, then the first one should be removed.
     *
     * @param h The new handler to add
     */
    public void removeParentPathListener(J3DParentPathRequestHandler h) {
        allParentPaths.remove(h);
        if(parentPathHandler == h)
            parentPathHandler = null;
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set.
     * <p>
     *
     * If the key is set, but the value is null or zero length, then all
     * capabilities on that node will be disabled. If the key is set the
     * values override all settings that the loader may wish to normally
     * make. This can be very dangerous if the loader is used for a file
     * format that includes its own animation engine.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityOverrideMap(Map capBits, Map freqBits) {
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            if(capBits.containsKey(TransformGroup.class)) {
                bits = (int[])capBits.get(TransformGroup.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        transform.clearCapability(bits[i]);
                } else if(!isStatic) {
                    // unset the cap bits that would have been set in setVersion()
                    transform.clearCapability(
                        TransformGroup.ALLOW_TRANSFORM_READ);
                    transform.clearCapability(
                        TransformGroup.ALLOW_TRANSFORM_WRITE);
                    transform.clearCapability(
                        TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
                    transform.clearCapability(Group.ALLOW_CHILDREN_EXTEND);
                    transform.clearCapability(Group.ALLOW_CHILDREN_WRITE);
                }
            }

            if(capBits.containsKey(BranchGroup.class)) {
                bits = (int[])capBits.get(BranchGroup.class);
                size = (bits == null) ? 0 : bits.length;

                if(size != 0) {
                    for(i = 0; i < size; i++)
                        j3dImplNode.clearCapability(bits[i]);
                } else if(!isStatic) {
                    // unset the cap bits that would have been set in setVersion()
                    j3dImplNode.clearCapability(BranchGroup.ALLOW_DETACH);
                }
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        if(freqBits.containsKey(TransformGroup.class)) {
            bits = (int[])freqBits.get(TransformGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    transform.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                // unset the cap bits that would have been set in setVersion()
                transform.clearCapabilityIsFrequent(
                    TransformGroup.ALLOW_TRANSFORM_READ);
                transform.clearCapabilityIsFrequent(
                    TransformGroup.ALLOW_TRANSFORM_WRITE);
                transform.clearCapabilityIsFrequent(
                    TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);

                transform.clearCapabilityIsFrequent(
                    Group.ALLOW_CHILDREN_EXTEND);
                transform.clearCapabilityIsFrequent(
                    Group.ALLOW_CHILDREN_WRITE);
            }
        }

        if(freqBits.containsKey(BranchGroup.class)) {
            bits = (int[])freqBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    j3dImplNode.clearCapabilityIsFrequent(bits[i]);
            } else if(!isStatic) {
                // unset the cap bits that would have been set in setVersion()
                j3dImplNode.clearCapabilityIsFrequent(BranchGroup.ALLOW_DETACH);
            }
        }
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
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            bits = (int[])capBits.get(TransformGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    transform.setCapability(bits[i]);
            }

            bits = (int[])capBits.get(BranchGroup.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    j3dImplNode.setCapability(bits[i]);
            }
        }

        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        bits = (int[])freqBits.get(TransformGroup.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                transform.setCapabilityIsFrequent(bits[i]);
        }

        bits = (int[])freqBits.get(BranchGroup.class);
        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                j3dImplNode.setCapabilityIsFrequent(bits[i]);
        }
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return j3dImplNode;
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

        updateViewTrans();
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

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

        transform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        transform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        transform.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
        transform.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        transform.setCapability(Group.ALLOW_CHILDREN_WRITE);

        j3dImplNode.setCapability(BranchGroup.ALLOW_DETACH);
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

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Convenience method to set the orientation of the viewpoint.
     *
     * @param dir The orientation quaternion to use
     */
    protected void setOrientation(float[] dir) {
        super.setOrientation(dir);

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Notification that the rendering of the event model is complete and that
     * rendering is about to begin. Used to update the transformation matrix
     * only once per frame. If the derived class needs to propogate the
     * changes then it should override the updateMatrix() method or this
     * and make sure this method is called first.
     */
    public void allEventsComplete() {
        updateViewTrans();
    }

    //----------------------------------------------------------
    // Methods internal to J3DViewpoint
    //----------------------------------------------------------

    /**
     * Updates the TransformGroup fields from the VRML fields
     */
    private void updateViewTrans() {
        axis.x = vfOrientation[0];
        axis.y = vfOrientation[1];
        axis.z = vfOrientation[2];
        double normalizer;

        normalizer = axis.x*axis.x+axis.y*axis.y+axis.z*axis.z;

        if(normalizer < 0.000001) { // Avoid div by zero
            axis.x = 0.0f;
            axis.y = 1.0f;
            axis.z = 0.0f;
        } else {
            normalizer = 1.0 / Math.sqrt(normalizer);
            axis.x *= normalizer;
            axis.y *= normalizer;
            axis.z *= normalizer;
        }

        axis.angle = (float)Math.IEEEremainder(vfOrientation[3], Math.PI * 2);

        implTrans.setIdentity();
        implTrans.set(axis);

        trans.x = vfPosition[0];
        trans.y = vfPosition[1];
        trans.z = vfPosition[2];

        implTrans.setTranslation(trans);

        if ((implTrans.getType() & Transform3D.CONGRUENT) != 0) {
            transform.setTransform(implTrans);
        } else {
            System.out.println("Trying to set non-congruent viewpoint for " +
                               "viewpoint '"+ vfDescription + "'");
        }
    }

    /**
     * Private, internal, common initialisation.
     */
    private void init() {
        allParentPaths = new ObjectArray();

        transform = new TransformGroup();

        BranchGroup bg = new BranchGroup();
        bg.addChild(transform);

        j3dImplNode = bg;

        axis = new AxisAngle4f();
        trans = new Vector3f();
        implTrans = new Transform3D();
        implTrans.setIdentity();
    }
}
