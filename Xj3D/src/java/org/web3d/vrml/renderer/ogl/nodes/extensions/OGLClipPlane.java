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

package org.web3d.vrml.renderer.ogl.nodes.extensions;

// External imports
import org.j3d.aviatrix3d.ClipPlane;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.NodeUpdateListener;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.extensions.BaseClipPlane;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * Null renderer implementation of a ClipPlane node.
 * <p>
 *
 * This node is purely informational within the scenegraph. It does not have
 * a renderable representation.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class OGLClipPlane extends BaseClipPlane
    implements OGLVRMLNode, NodeUpdateListener {

    /** The aviatix3d representation of a single clipping plane */
    private ClipPlane implClipPlane;

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public OGLClipPlane() {
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
    public OGLClipPlane(VRMLNodeType node) {
        super(node);
        init();
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
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {
        implClipPlane.setEnabled(vfEnabled);
        implClipPlane.setPlaneEquation(vfPlane);
    }

    //----------------------------------------------------------
    // Methods defined by BaseClipPlane
    //----------------------------------------------------------

    /**
     * Set a new state for the enabled field.
     *
     * @param state True if this sensor is to be enabled
     */
    protected void setEnabled(boolean state) {
        super.setEnabled(state);

        if (inSetup)
            return;

        if (implClipPlane.isLive())
            implClipPlane.dataChanged(this);
        else
            updateNodeDataChanges(implClipPlane);
    }

    /**
     * Set a the new plane equation values. This is a 4-dimensional vector
     * indicating the valid half-space for the plane.
     *
     * @param equation The new values for the equation
     * @throws InvalidFieldValueException The first 3 components of the field
     *   do not describe a unit vector
     */
    protected void setPlane(double[] equation)
        throws InvalidFieldValueException {

        super.setPlane(equation);

        if (inSetup)
            return;

        if (implClipPlane.isLive())
            implClipPlane.dataChanged(this);
        else
            updateNodeDataChanges(implClipPlane);
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
        return implClipPlane;
    }

    //----------------------------------------------------------
    // Methods overriding BaseGroup class.
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

        implClipPlane.setEnabled(vfEnabled);
        implClipPlane.setPlaneEquation(vfPlane);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Internal convenience method to initialise the OpenGL data structures.
     */
    private void init() {
        implClipPlane = new ClipPlane();
        implClipPlane.setEnabled(vfEnabled);
        implClipPlane.setPlaneEquation(vfPlane);
    }
}
