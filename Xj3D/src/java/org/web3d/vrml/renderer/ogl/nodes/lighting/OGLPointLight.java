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
import org.web3d.vrml.renderer.common.nodes.lighting.BasePointLight;
import org.web3d.vrml.renderer.ogl.nodes.OGLLightNodeType;

/**
 * OpenGL implementation of a pointlight.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
public class OGLPointLight extends BasePointLight
    implements OGLLightNodeType, NodeUpdateListener  {

    /** Holds the OGL impl for the other light */
    private PointLight implLight;

    /** Performace vars for local usage */
    private float[] flScratch;

    /** The bounding volume that determines the radius of the light */
    private BoundingSphere radiusBounds;

    /**
     * Construct a new default instance of this class.
     */
    public OGLPointLight() {
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
    public OGLPointLight(VRMLNodeType node) {
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
        radiusBounds.setRadius(vfRadius);
        implLight.setEffectBounds(radiusBounds);
    }

    /**
     * Notification that its safe to update the node now with any operations
     * that only change the node's properties, but do not change the bounds.
     *
     * @param src The node or Node Component that is to be updated.
     */
    public void updateNodeDataChanges(Object src) {

        flScratch[0] = vfColor[0] * vfAmbientIntensity;
        flScratch[1] = vfColor[1] * vfAmbientIntensity;
        flScratch[2] = vfColor[2] * vfAmbientIntensity;
        implLight.setAmbientColor(flScratch);

        flScratch[0] = vfColor[0] * vfIntensity;
        flScratch[1] = vfColor[1] * vfIntensity;
        flScratch[2] = vfColor[2] * vfIntensity;
        implLight.setDiffuseColor(flScratch);
        implLight.setSpecularColor(flScratch);

        implLight.setEnabled(vfOn);
        implLight.setGlobalOnly(vfGlobal);

        implLight.setPosition(vfLocation);
        implLight.setAttenuation(vfAttenuation);

        //radiusBounds.setRadius(vfRadius);
        //implLight.setEffectBounds(radiusBounds);
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
    // Methods defined by OGLVRMLNode
    //----------------------------------------------------------

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
        if (!inSetup)
            return;

        super.setupFinished();

        if ((vrmlMajorVersion < 3) ||
            ((vrmlMajorVersion == 3) && (vrmlMinorVersion == 0)))
            vfGlobal = true;

        // cheat, and call it directly to do the updates.
        updateNodeDataChanges(null);
		updateNodeBoundsChanges(null);
    }

    //----------------------------------------------------------
    // Methods defined by BaseDirectionalLight
    //----------------------------------------------------------

    /**
     * Set the location ofthe point light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param loc The new location to use
     */
    protected void setLocation(float[] loc) {

        super.setLocation(loc);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Set the radius of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param radius The new radius to use
     * @throws InvalidFieldValueException Radius value was negative
     */
    protected void setRadius(float radius)
        throws InvalidFieldValueException {

        super.setRadius(radius);

        if (inSetup)
            return;

        if (implLight.isLive())
			implLight.boundsChanged(this);
        else
			updateNodeBoundsChanges(implLight);
    }

    /**
     * Set the attenuation factor of the light. Should be overridden by derived
     * classes for implementation-specific additions.
     *
     * @param factor The new attenuation factor to use
     * @throws InvalidFieldValueException Radius value was negative
     */
    protected void setAttenuation(float[] factor) {
        super.setAttenuation(factor);

        if (inSetup)
            return;

        if (implLight.isLive())
            implLight.dataChanged(this);
        else
            updateNodeDataChanges(implLight);
    }

    /**
     * Private, common initialisation method for the constructors.
     */
    private void init() {
        flScratch = new float[3];
        implLight = new PointLight();
        radiusBounds = new BoundingSphere(vfRadius);
    }
}
