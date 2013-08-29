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

package org.web3d.vrml.renderer.ogl.nodes.lighting;

// External imports
import org.j3d.aviatrix3d.*;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.renderer.common.nodes.lighting.BaseDirectionalLight;
import org.web3d.vrml.renderer.ogl.nodes.OGLLightNodeType;

/**
 * OpenGL implementation of a directional light.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.8 $
 */
public class OGLDirectionalLight extends BaseDirectionalLight
    implements OGLLightNodeType, NodeUpdateListener  {

    /** Holds the OGL impl for the other light */
    private DirectionalLight implLight;

    /** Performace vars for local usage */
    private float[] flScratch;

    /**
     * Construct a new default instance of this class.
     */
    public OGLDirectionalLight() {
        super();

        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public OGLDirectionalLight(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods defined by OGLLightNodeType
    //-------------------------------------------------------------

    /**
     * Get the light making up this LightNode.
     *
     * @return The OGL light instance
     */
    public Light getLight() {
        return implLight;
    }

    //----------------------------------------------------------
    // Methods defined by UpdateListener
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

        flScratch[0] = vfColor[0] * vfIntensity;
        flScratch[1] = vfColor[1] * vfIntensity;
        flScratch[2] = vfColor[2] * vfIntensity;

        implLight.setDiffuseColor(flScratch);
        implLight.setSpecularColor(flScratch);

        flScratch[0] = vfColor[0] * vfAmbientIntensity;
        flScratch[1] = vfColor[1] * vfAmbientIntensity;
        flScratch[2] = vfColor[2] * vfAmbientIntensity;
        implLight.setAmbientColor(flScratch);

        implLight.setEnabled(vfOn);
        implLight.setGlobalOnly(vfGlobal);

        flScratch[0] = -vfDirection[0];
        flScratch[1] = -vfDirection[1];
        flScratch[2] = -vfDirection[2];

        implLight.setDirection(flScratch);
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLLightNodeType
    //-------------------------------------------------------------

    /**
     * Accessor method to get current value of field ambientIntensity.
     *
     * @param intensity The new value
     */
    public void setAmbientIntensity(float intensity)
        throws InvalidFieldValueException {

        super.setAmbientIntensity(intensity);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Accessor method to get current value of field color.
     *
     * @param newColor The new value
     */
    public void setColor(float[] newColor)
        throws InvalidFieldValueException {

        super.setColor(newColor);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Accessor method to get current value of field Intensity.
     *
     * @param intensity The new value
     */
    public void setIntensity(float intensity)
        throws InvalidFieldValueException {

        super.setIntensity(intensity);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Turn the light on or off.
     *
     * @param state The new value
     */
    public void setOn(boolean state) {
        super.setOn(state);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Get the Java3D scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used.
     *
     * @return The J3D representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return implLight;
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

        // cheat, and call it directly to do the updates.
        updateNodeDataChanges(null);
    }

    /**
     * Set the current value of the global field.
     *
     * @param global true if this should have global effect, false for scoped
     * @throws InvalidFieldException Called on a node that belongs to VRML or
     *    X3D 3.0.
     */
    public void setGlobal(boolean global)
        throws InvalidFieldException {

        super.setGlobal(global);

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    //----------------------------------------------------------
    // Methods defined by BaseDirectionalLight
    //----------------------------------------------------------

    /**
     * Set the direction ofthe spot light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param dir The new direction vector to use
     */
    protected void setDirection(float[] dir) {
        super.setDirection(dir);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Private, common initialisation method for the constructors.
     */
    private void init() {
        flScratch = new float[3];
        implLight = new DirectionalLight();
    }
}
