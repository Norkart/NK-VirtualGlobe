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

package org.web3d.vrml.renderer.ogl.nodes.rigidphysics;

// External imports
import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.web3d.vrml.renderer.common.nodes.rigidphysics.BaseCollidableShape;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * Implementation of a CollidableShape.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class OGLCollidableShape extends BaseCollidableShape
    implements OGLVRMLNode, NodeUpdateListener {

    /** The group holding the children */
    private TransformGroup implGroup;

    /** The group returned to the scene graph */
    private SharedNode sharedNode;

    /**
     * Initialise the time dependent node and it's fields that are held
     * locally.
     */
    public OGLCollidableShape() {
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
    public OGLCollidableShape(VRMLNodeType node) {
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
        if (implGroup.isLive())
            implGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGroup);
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
        implGroup.setTransform(tmatrix);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        // do nothing
    }

    //-------------------------------------------------------------
    // Methods defined by FrameStateListener
    //-------------------------------------------------------------

    /**
     * The end of the frame has been reached, update the matrix now.
     */
    public void allEventsComplete() {
        super.allEventsComplete();

        if (implGroup.isLive())
            implGroup.boundsChanged(this);
        else
            updateNodeBoundsChanges(implGroup);
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

        if(vfShape != null) {
            SceneGraphObject sgo = ((OGLVRMLNode)vfShape).getSceneGraphObject();
            if(sgo != null)
                implGroup.addChild((Node)sgo);
        }

        implGroup.setTransform(tmatrix);
    }

    //----------------------------------------------------------
    // Methods from OGLVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return sharedNode;
    }

    /**
     * Set node content as replacement for the shape field.
     *
     * @param shape The new Shape node to use
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    protected void setShape(VRMLNodeType shape)
        throws InvalidFieldValueException {

        if(!(shape instanceof OGLVRMLNode))
            throw new InvalidFieldValueException("Not an OpenGL shape object");

        super.setShape(shape);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Common initialisation of this node.
     */
    private void init() {
        implGroup = new TransformGroup();
        sharedNode = new SharedNode();
        sharedNode.setChild(implGroup);
    }
}
