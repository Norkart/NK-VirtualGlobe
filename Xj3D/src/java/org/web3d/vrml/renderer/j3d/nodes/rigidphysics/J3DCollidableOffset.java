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

package org.web3d.vrml.renderer.j3d.nodes.rigidphysics;

// External imports
import javax.media.j3d.*;

import java.util.Map;

// Local imports
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.rigidphysics.BaseCollidableOffset;
import org.web3d.vrml.renderer.j3d.nodes.J3DUserData;
import org.web3d.vrml.renderer.j3d.nodes.J3DVRMLNode;

/**
 * Java3D Implementation of a CollidableOffset node.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class J3DCollidableOffset extends BaseCollidableOffset
    implements J3DVRMLNode {

    /** The group holding the children */
    private TransformGroup implGroup;

    /** The group returned to the scene graph */
    private SharedGroup sharedNode;

    /** Transform for setting the matrix into the TransformGroup */
    private Transform3D transform;

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    public J3DCollidableOffset() {
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
    public J3DCollidableOffset(VRMLNodeType node) {
        super(node);
        init();
    }

    //----------------------------------------------------------
    // Methods defined by BaseCollidableNode
    //----------------------------------------------------------

    /**
     * ODE computation has finished, so go update the field values and the
     * rendering API structures with the final computed values.
     */
    public void updateFromODE() {
        super.updateFromODE();

        transform.set(tmatrix);
        implGroup.setTransform(transform);
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * The end of the frame has been reached, update the matrix now.
     */
    public void allEventsComplete() {
        super.allEventsComplete();

        transform.set(tmatrix);
        implGroup.setTransform(transform);
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNode interface.
    //----------------------------------------------------------

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return sharedNode;
    }

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
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
     * of what the loader wants and what the user wants. Default implementation
     * does nothing.
     * <p>
     * If the map contains a key, but the value is  null or zero length, the
     * request is ignored.
     *
     * @param capBits The capability bits to be set
     * @param freqBits The frequency bits to be set
     */
    public void setCapabilityRequiredMap(Map capBits, Map freqBits) {
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

        if(vfCollidable != null) {
            SceneGraphObject sgo = ((J3DVRMLNode)vfCollidable).getSceneGraphObject();
            if(sgo != null) {
                if(sgo instanceof SharedGroup) {
                    Link link = new Link((SharedGroup)sgo);
                    implGroup.addChild(link);
                } else {
                    implGroup.addChild((Node)sgo);
                }
            }
        }

        // Grab the user data that should have been created in the superclass
        // and tell it that this is a transform.
        J3DUserData data = new J3DUserData();
        implGroup.setUserData(data);

        data.isTransform = true;

        transform.set(tmatrix);
        implGroup.setTransform(transform);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Common initialisation of this node.
     */
    private void init() {
        implGroup = new TransformGroup();
        implGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        implGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        implGroup.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

        sharedNode = new SharedGroup();
        sharedNode.addChild(implGroup);

        transform = new Transform3D();
    }
}
