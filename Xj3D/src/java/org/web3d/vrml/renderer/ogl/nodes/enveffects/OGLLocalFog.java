/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.ogl.nodes.enveffects;

// External imports
import org.j3d.aviatrix3d.Fog;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.NodeUpdateListener;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.enveffects.BaseLocalFog;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;

/**
 * Null renderer implementation of a LocalFog node.
 * <p>
 *
 * This node is purely informational within the scenegraph. It does not have
 * a renderable representation.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public class OGLLocalFog extends BaseLocalFog
    implements OGLVRMLNode, NodeUpdateListener {

    /** The aviatix3d representation of a single clipping plane */
    private Fog implFog;

    /**
     * Construct a default node with an empty info array any the title set to
     * the empty string.
     */
    public OGLLocalFog() {
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
    public OGLLocalFog(VRMLNodeType node) {
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
        implFog.setEnabled(vfEnabled);
        implFog.setColor(vfColor);
        implFog.setLinearDistance(0, vfVisibilityRange);

        switch(fogType) {
            case FOG_TYPE_LINEAR:
                implFog.setMode(Fog.LINEAR);
                break;

            case FOG_TYPE_EXPONENTIAL:
                implFog.setMode(Fog.EXPONENTIAL);
                break;
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLFogNodeType
    //----------------------------------------------------------

    /**
     * Set the visibility limit on the fog to be viewed to a new value. The
     * value of zero will disable the fog. A negative number will generate an
     * exception.
     *
     * @param range A non-negative number indicating the distance
     * @throws InvalidFieldValueException The number was negative
     */
    public void setVisibilityRange(float range)
        throws InvalidFieldValueException {

        super.setVisibilityRange(range);
        if (inSetup)
            return;

        if (implFog.isLive())
            implFog.dataChanged(this);
        else
            updateNodeDataChanges(implFog);
    }

    /**
     * Set the color of the current fog. If the color values are out of range
     * or the array is invalid, an exception will be generated.
     *
     * @param color The new colors to set
     */
    public void setColor(float[] color)
        throws InvalidFieldValueException {

        super.setColor(color);
        if (inSetup)
            return;

        if (implFog.isLive())
            implFog.dataChanged(this);
        else
            updateNodeDataChanges(implFog);
    }

    //----------------------------------------------------------
    // Methods defined by BaseLocalFog
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

        if (implFog.isLive())
            implFog.dataChanged(this);
        else
            updateNodeDataChanges(implFog);
    }

    /**
     * Set the local fog type to a new value. Converts the string form to the
     * internal representation.
     *
     * @param type The type string indicating what needs to be set
     * @throws InvalidFieldValueException The fog type string is invalid
     */
    protected void setFogType(String type) throws InvalidFieldValueException {
        super.setFogType(type);
        if (inSetup)
            return;

        if (implFog.isLive())
            implFog.dataChanged(this);
        else
            updateNodeDataChanges(implFog);
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
        return implFog;
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

        implFog.setEnabled(vfEnabled);
        implFog.setColor(vfColor);
        implFog.setLinearDistance(0, vfVisibilityRange);

        switch(fogType) {
            case FOG_TYPE_LINEAR:
                implFog.setMode(Fog.LINEAR);
                break;

            case FOG_TYPE_EXPONENTIAL:
                implFog.setMode(Fog.EXPONENTIAL);
                break;
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Internal convenience method to initialise the OpenGL data structures.
     */
    private void init() {
        implFog = new Fog();
        implFog.setGlobalOnly(false);
    }
}
