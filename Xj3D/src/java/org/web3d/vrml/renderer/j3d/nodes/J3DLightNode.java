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

package org.web3d.vrml.renderer.j3d.nodes;

// Standard imports
import javax.media.j3d.*;

import java.util.HashMap;
import java.util.ArrayList;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

// Application specific imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.InvalidFieldFormatException;
import org.web3d.vrml.lang.InvalidFieldException;
import org.web3d.vrml.lang.InvalidFieldValueException;
import org.web3d.vrml.lang.FieldConstants;
import org.web3d.vrml.renderer.common.nodes.BaseLightNode;
import org.web3d.vrml.util.FieldValidator;

/**
 * Abstract Java3D implementation of a light.
 * <p>
 * This base class will implement a light grouping.  VRML spec for lights
 * needs two lights in Java3D to cover the Ambient portion.
 *
 * @author Alan Hudson
 * @version $Revision: 1.7 $
 */
public abstract class J3DLightNode extends BaseLightNode
    implements J3DLightNodeType {

    // Class Constants

    /** Infinite bounds for light scope */
    private static final Bounds infiniteBounds =
        new BoundingSphere(new Point3d(), Double.MAX_VALUE);

    // Class vars

    /** Holds the j3d impl for a AmbientLight */
    private AmbientLight implAmbLight;

    /** Holds the j3d impl for the other light */
    protected Light implLight;

    /** The group that holds these lights */
    private Group j3dImplNode;

    /** Performace vars for local usage */
    private float[] flScratch;

    /** Working var used to set the light colour at the J3D level */
    private Color3f lightColor;

    //----------------------------------------------------------
    // Constructors
    //----------------------------------------------------------

    /**
     * Construct a new default instance of this class.
     *
     * @param name The name of the type of node
     */
    public J3DLightNode(String name) {
        super(name);

        flScratch = new float[3];
        lightColor = new Color3f();

        j3dImplNode = new BranchGroup();
        j3dImplNode.setCapability(BranchGroup.ALLOW_DETACH);

        implAmbLight = new AmbientLight();
        implAmbLight.setCapability(Light.ALLOW_COLOR_WRITE);
        implAmbLight.setCapability(Light.ALLOW_STATE_WRITE);

        implAmbLight.setInfluencingBounds(infiniteBounds);
    }

    //-------------------------------------------------------------
    // Methods required by the J3DLightNodeType interface.
    //-------------------------------------------------------------

    /**
     * Get the lights making up this LightNode.
     * Java3D lights are different then VRML lights, ie they
     * seperate out the ambient component.  So our lights may
     * be composed of several J3D lights
     *
     * @return A list of J3D lights making up this Light
     */
    public Light[] getLights() {
        Light list[] = new Light[2];

        list[0] = implAmbLight;
        list[1] = implLight;

        return list;
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLLightNodeType interface.
    //-------------------------------------------------------------

    /**
     * Accessor method to get current value of field ambientIntensity.
     *
     * @param newAmbientIntensity The new value
     */
    public void setAmbientIntensity(float newAmbientIntensity)
        throws InvalidFieldValueException {

        super.setAmbientIntensity(newAmbientIntensity);

        implAmbLight.setColor(lightColor);
    }

    /**
     * Accessor method to get current value of field color.
     *
     * @param newColor The new value
     */
    public void setColor(float[] newColor)
        throws InvalidFieldValueException {

        super.setColor(newColor);

        lightColor.x = vfColor[0] * vfAmbientIntensity;
        lightColor.y = vfColor[1] * vfAmbientIntensity;
        lightColor.z = vfColor[2] * vfAmbientIntensity;
        implAmbLight.setColor(lightColor);

        lightColor.x = vfColor[0] * vfIntensity;
        lightColor.y = vfColor[1] * vfIntensity;
        lightColor.z = vfColor[2] * vfIntensity;
        implLight.setColor(lightColor);
    }

    /**
     * Accessor method to get current value of field Intensity.
     *
     * @param newIntensity The new value
     */
    public void setIntensity(float newIntensity)
        throws InvalidFieldValueException {

        super.setIntensity(newIntensity);

        lightColor.x = vfColor[0] * vfIntensity;
        lightColor.y = vfColor[1] * vfIntensity;
        lightColor.z = vfColor[2] * vfIntensity;
        implLight.setColor(lightColor);
    }

    /**
     * Accessor method to get current value of field On.
     *
     * @param state The new value
     */
    public void setOn(boolean state) {

        super.setOn(state);

        implLight.setEnable(vfOn);
        implAmbLight.setEnable(vfOn);
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeType interface.
    //----------------------------------------------------------

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

        lightColor.x = vfColor[0] * vfAmbientIntensity;
        lightColor.y = vfColor[1] * vfAmbientIntensity;
        lightColor.z = vfColor[2] * vfAmbientIntensity;

        implAmbLight.setEnable(vfOn);
        implAmbLight.setColor(lightColor);

        j3dImplNode.addChild(implLight);
        j3dImplNode.addChild(implAmbLight);
    }

    /**
     * Notify this node that is has been DEFd. This method shall only be
     * called before setupFinished(). It is an error to call it any other
     * time. It is also guaranteed that this call will be made after
     * construction, but before any of the setValue() methods have been called.
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
        super.setDEF();

        j3dImplNode = new SharedGroup();
    }
}
