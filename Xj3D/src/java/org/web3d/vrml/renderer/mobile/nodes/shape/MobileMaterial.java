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

package org.web3d.vrml.renderer.mobile.nodes.shape;

// Standard imports
// None

// Application specific imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLMaterialNodeType;

import org.web3d.vrml.renderer.common.nodes.shape.BaseMaterial;
import org.web3d.vrml.renderer.mobile.nodes.MobileVRMLNode;
import org.web3d.vrml.renderer.mobile.nodes.MobileMaterialNodeType;

import org.web3d.vrml.renderer.mobile.sg.Material;
import org.web3d.vrml.renderer.mobile.sg.SceneGraphObject;
import org.web3d.vrml.renderer.mobile.sg.SGManager;

/**
 * OpenGL implementation of a material node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class MobileMaterial extends BaseMaterial
    implements MobileMaterialNodeType {

    /** Local version of the ambient colour that is the intensity field */
    private float[] lfAmbientColor;

    /** The OpenGL material node */
    private Material material;

    /**
     * Construct a default instance of the material
     */
    public MobileMaterial() {
        lfAmbientColor = new float[3];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public MobileMaterial(VRMLNodeType node) {
        super(node);

        lfAmbientColor = new float[3];
    }


    //----------------------------------------------------------
    // Methods required by the MobileMaterialNodeType interface.
    //----------------------------------------------------------

    /**
     * Request the OpenGL material node structure.
     *
     * @return The material node
     */
    public Material getMaterial() {
        return material;
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

        // Null the reference so we can reclaim memory
        inSetup = false;

        material = new Material(lfAmbientColor,
                                vfEmissiveColor,
                                vfDiffuseColor,
                                vfSpecularColor,
                                vfShininess);

    }

    //----------------------------------------------------------
    // Methods overriding BaseMaterial
    //----------------------------------------------------------

    /**
     * Accessor method to set a new value for field attribute
     * <b>ambientIntensity</b>. How much ambient omnidirectional light is
     * reflected from all light sources.
     *
     * @param newAmbientIntensity The new intensity value
     */
    public void setAmbientIntensity(float newAmbientIntensity)
        throws InvalidFieldValueException {

        super.setAmbientIntensity(newAmbientIntensity);

        lfAmbientColor[0] = vfAmbientIntensity;
        lfAmbientColor[1] = vfAmbientIntensity;
        lfAmbientColor[2] = vfAmbientIntensity;

        if (!inSetup)
            material.setAmbientColor(lfAmbientColor);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>diffuseColor</b>.  How much direct, angle-dependent light is
     * reflected from all light sources.
     *
     * @param newDiffuseColor The new value of diffuseColor
     */
    public void setDiffuseColor(float[] newDiffuseColor)
        throws InvalidFieldValueException {

        super.setDiffuseColor(newDiffuseColor);

        if (!inSetup)
            material.setDiffuseColor(vfDiffuseColor);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>emissiveColor</b>. How much glowing light is emitted from this object.
     *
     * @param newEmissiveColor The new value of EmissiveColor
     */
    public void setEmissiveColor(float[] newEmissiveColor)
        throws InvalidFieldValueException {

        super.setEmissiveColor(newEmissiveColor);

        if (!inSetup)
            material.setEmissiveColor(vfEmissiveColor);
    }

    /**
     * Accessor method to set a new value for field attribute <b>shininess</b>.
     * Low values provide soft specular glows, high values provide sharper,
     * smaller highlights.
     *
     * @param newShininess The new value of Shininess
     */
    public void setShininess(float newShininess)
        throws InvalidFieldValueException  {

        super.setShininess(newShininess);

        if (!inSetup)
            material.setShininess(vfShininess);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>specularColor</b>. Specular highlights are brightness
     * reflections (example:  shiny spots on an apple).
     *
     * @param newSpecularColor The new value of SpecularColor
     */
    public void setSpecularColor (float[] newSpecularColor)
        throws InvalidFieldValueException {

        super.setSpecularColor(newSpecularColor);

        if (!inSetup)
            material.setSpecularColor(vfSpecularColor);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>transparency</b>.  How "clear" an object is:  1.0 is completely
     * transparent, 0.0 is completely opaque .
     *
     * @param newTransparency The new value of Transparency
     */
    public void setTransparency(float newTransparency)
        throws InvalidFieldValueException {

        super.setTransparency(newTransparency);

        // Need todo something with transparency
    }

    //----------------------------------------------------------
    // Methods from MobileVRMLNode class.
    //----------------------------------------------------------

    /**
     * Get the OpenGL scene graph object representation of this node. This will
     * need to be cast to the appropriate parent type when being used. Default
     * implementation returns null.
     *
     * @return The OpenGL representation.
     */
    public SceneGraphObject getSceneGraphObject() {
        return material;
    }

}
