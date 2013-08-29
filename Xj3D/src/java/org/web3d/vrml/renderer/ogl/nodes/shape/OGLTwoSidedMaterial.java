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

package org.web3d.vrml.renderer.ogl.nodes.shape;

// External imports
import org.j3d.aviatrix3d.Material;
import org.j3d.aviatrix3d.SceneGraphObject;
import org.j3d.aviatrix3d.NodeUpdateListener;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLMaterialNodeType;

import org.web3d.vrml.renderer.common.nodes.shape.BaseTwoSidedMaterial;
import org.web3d.vrml.renderer.ogl.nodes.OGLVRMLNode;
import org.web3d.vrml.renderer.ogl.nodes.OGLMaterialNodeType;

/**
 * OpenGL implementation of a material node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class OGLTwoSidedMaterial extends BaseTwoSidedMaterial
    implements OGLMaterialNodeType, NodeUpdateListener {

    /** White color for ignoring diffuse */
    private static final float[] ignoreColor;

    /** Local version of the ambient colour that is the intensity field */
    private float[] lfAmbientColor;

    /** Local version of the ambient colour that is the intensity field */
    private float[] lfBackAmbientColor;

    /** The OpenGL material node */
    private Material material;

    /** Flag for the ambient colour changing */
    private boolean ambientChanged;

    /** Flag for the ambient colour changing */
    private boolean backAmbientChanged;

    /** Flag for the diffuse colour changing */
    private boolean diffuseChanged;

    /** Flag for the diffuse colour changing */
    private boolean backDiffuseChanged;

    /** Flag for the emissive colour changing */
    private boolean emissiveChanged;

    /** Flag for the emissive colour changing */
    private boolean backEmissiveChanged;

    /** Flag for the specular colour changing */
    private boolean specularChanged;

    /** Flag for the specular colour changing */
    private boolean backSpecularChanged;

    /** Flag for the shininess colour changing */
    private boolean shininessChanged;

    /** Flag for the shininess colour changing */
    private boolean backShininessChanged;

    /** Flag for the transparency value changing */
    private boolean transparencyChanged;

    /** Flag for the transparency value changing */
    private boolean backTransparencyChanged;

    /** Flag for the enable separate back colour changing */
    private boolean separateBackColorChanged;

    /** Flag controlling the lighting state */
    private boolean lightingState;

    /** Flag for the shininess colour changing */
    private boolean lightingChanged;

    /** Flaf controlling whether local color is used */
    private boolean localColor;

    /** Whether the local color also includes alpha values */
    private boolean localColorAlpha;

    /** Flag for the shininess colour changing */
    private boolean localColorChanged;


    static {
        ignoreColor = new float[] {1,1,1};
    }

    /**
     * Construct a default instance of the material
     */
    public OGLTwoSidedMaterial() {
        lfAmbientColor = new float[] {0.2f, 0.2f, 0.2f};
        lfBackAmbientColor = new float[] {0.2f, 0.2f, 0.2f};
        localColorAlpha = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public OGLTwoSidedMaterial(VRMLNodeType node) {
        super(node);

        lfAmbientColor = new float[] {0.2f, 0.2f, 0.2f};
        lfBackAmbientColor = new float[] {0.2f, 0.2f, 0.2f};
        localColorAlpha = false;
    }

    //----------------------------------------------------------
    // Methods defined by OGLMaterialNodeType
    //----------------------------------------------------------

    /**
     * Request the OpenGL material node structure.
     *
     * @return The material node
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Set whether lighting will be used for this material.  In general
     * you should let the material node decide this.  Needed to handle
     * IndexedLineSets or other geometry that specifically declares lighting
     * be turned off.
     *
     * @param enable Whether lighting is enabled
     */
    public void setLightingEnable(boolean enable) {
        lightingState = enable;

        if(inSetup)
            return;

        lightingChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
    }

    /**
     * Set whether the geometry has local colors to override the diffuse color.
     *
     * @param enabled Whether local color is enabled
     * @param hasAlpha true with the local color also contains alpha valuess
     */
    public void setLocalColor(boolean enable, boolean hasAlpha) {
        localColor = enable;
        localColorAlpha = hasAlpha;

        if(inSetup)
            return;

        localColorChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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

        material = new Material(lfAmbientColor,
                                vfEmissiveColor,
                                vfDiffuseColor,
                                vfSpecularColor,
                                vfShininess,
                                1.0f - vfTransparency);

        material.setSeparateTransparencyEnabled(localColorAlpha);
        material.setSeparateBackfaceEnabled(vfSeparateBackColor);
        material.setColorMaterialTarget(Material.DIFFUSE_TARGET);
        material.setBackColorMaterialTarget(Material.DIFFUSE_TARGET);
        material.setLightingEnabled(lightingState);

        if(ignoreDiffuse)
            material.setDiffuseColor(ignoreColor);
        else
            material.setDiffuseColor(vfDiffuseColor);

        if(vfSeparateBackColor) {
            if(ignoreDiffuse)
                material.setBackDiffuseColor(ignoreColor);
            else
                material.setBackDiffuseColor(vfBackDiffuseColor);

            material.setBackEmissiveColor(vfBackEmissiveColor);
            material.setBackAmbientColor(lfBackAmbientColor);
            material.setBackSpecularColor(vfBackSpecularColor);
            material.setBackShininess(vfBackShininess);
            material.setBackTransparency(1.0f - vfBackTransparency);
        }

        ambientChanged = false;
        backAmbientChanged = false;
        diffuseChanged = false;
        backDiffuseChanged = false;
        emissiveChanged = false;
        backEmissiveChanged = false;
        specularChanged = false;
        backSpecularChanged = false;
        shininessChanged = false;
        backShininessChanged = false;
        transparencyChanged = false;
        backTransparencyChanged = false;
        lightingChanged = false;
        localColorChanged = false;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLMaterialNodeType
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

        if(inSetup)
            return;

        ambientChanged = true;

        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
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

        if(inSetup)
            return;

        diffuseChanged = true;
        if(material != null) {
            if(material.isLive())
                material.dataChanged(this);
            else
                updateNodeDataChanges(material);
        }
    }

    /**
     * Ignore the diffuseColor color term and use 1,1,1 for the diffuse color.
     *
     * @param ignore True to ignore the diffuse term
     */
    public void setIgnoreDiffuse(boolean ignore) {

        super.setIgnoreDiffuse(ignore);

        if(inSetup)
            return;

        diffuseChanged = true;

        if(material != null) {
            if(material.isLive())
                material.dataChanged(this);
            else
                updateNodeDataChanges(material);
        }
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

        if(inSetup)
            return;

        emissiveChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
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

        if(inSetup)
            return;

        shininessChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
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

        if(inSetup)
            return;

        specularChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
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

        if(inSetup)
            return;

        transparencyChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
    }

    //----------------------------------------------------------
    // Methods defined by OGLVRMLNode
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

        if(ambientChanged) {
            material.setAmbientColor(lfAmbientColor);
            ambientChanged = false;
        }

        if(diffuseChanged) {
            if(ignoreDiffuse) {
                material.setDiffuseColor(ignoreColor);
            } else {
                material.setDiffuseColor(vfDiffuseColor);
            }
            diffuseChanged = false;
        }

        if(emissiveChanged) {
            material.setEmissiveColor(vfEmissiveColor);
            emissiveChanged = false;
        }

        if(specularChanged) {
            material.setSpecularColor(vfSpecularColor);
            specularChanged = false;
        }

        if(shininessChanged) {
            material.setShininess(vfShininess);
            shininessChanged = false;
        }

        if(transparencyChanged) {
            material.setTransparency(1.0f - vfTransparency);
            transparencyChanged = false;
        }


        if(separateBackColorChanged) {
            material.setSeparateBackfaceEnabled(vfSeparateBackColor);
            separateBackColorChanged = false;
        }

        // The AV3D Material class only allows setting these value if the
        // separated colour flag has been enabled. If disabled, we can't
        // write to the class, so only do these when allowed.
        if(vfSeparateBackColor) {
            if(backAmbientChanged) {
                material.setBackAmbientColor(lfBackAmbientColor);
                backAmbientChanged = false;
            }

            if(backDiffuseChanged) {
                if(ignoreDiffuse) {
                    material.setBackDiffuseColor(ignoreColor);
                } else {
                    material.setBackDiffuseColor(vfBackDiffuseColor);
                }
                backDiffuseChanged = false;
            }

            if(backEmissiveChanged) {
                material.setBackEmissiveColor(vfBackEmissiveColor);
                backEmissiveChanged = false;
            }

            if(backSpecularChanged) {
                material.setBackSpecularColor(vfBackSpecularColor);
                backSpecularChanged = false;
            }

            if(backShininessChanged) {
                material.setBackShininess(vfBackShininess);
                backShininessChanged = false;
            }

            if(backTransparencyChanged) {
                material.setBackTransparency(1.0f - vfBackTransparency);
                backTransparencyChanged = false;
            }
        }

        if(lightingChanged) {
            material.setLightingEnabled(lightingState);
            lightingChanged = false;
        }

        if(localColorChanged) {
            material.setColorMaterialEnabled(localColor);
            material.setSeparateTransparencyEnabled(localColorAlpha);
            localColorChanged = false;
        }
    }
    //----------------------------------------------------------
    // Methods defined by BaseTwoSidedMaterial
    //----------------------------------------------------------

    /**
     * Accessor method to set a new value for field attribute
     * <b>ambientIntensity</b>. How much ambient omnidirectional light is
     * reflected from all light sources.
     *
     * @param newAmbientIntensity The new intensity value
     */
    protected void setBackAmbientIntensity(float newAmbientIntensity)
        throws InvalidFieldValueException {

        super.setBackAmbientIntensity(newAmbientIntensity);

        lfBackAmbientColor[0] = vfBackAmbientIntensity;
        lfBackAmbientColor[1] = vfBackAmbientIntensity;
        lfBackAmbientColor[2] = vfBackAmbientIntensity;

        if(inSetup)
            return;

        backAmbientChanged = true;

        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>diffuseColor</b>.  How much direct, angle-dependent light is
     * reflected from all light sources.
     *
     * @param newDiffuseColor The new value of diffuseColor
     */
    protected void setBackDiffuseColor(float[] newDiffuseColor)
        throws InvalidFieldValueException {

        super.setBackDiffuseColor(newDiffuseColor);

        if(inSetup)
            return;

        backDiffuseChanged = true;
        if(material != null) {
            if(material.isLive())
                material.dataChanged(this);
            else
                updateNodeDataChanges(material);
        }
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>emissiveColor</b>. How much glowing light is emitted from this object.
     *
     * @param newEmissiveColor The new value of EmissiveColor
     */
    protected void setBackEmissiveColor(float[] newEmissiveColor)
        throws InvalidFieldValueException {

        super.setBackEmissiveColor(newEmissiveColor);

        if(inSetup)
            return;

        backEmissiveChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(material);
    }

    /**
     * Accessor method to set a new value for field attribute <b>shininess</b>.
     * Low values provide soft specular glows, high values provide sharper,
     * smaller highlights.
     *
     * @param newShininess The new value of Shininess
     */
    protected void setBackShininess(float newShininess)
        throws InvalidFieldValueException  {

        super.setBackShininess(newShininess);

        if(inSetup)
            return;

        backShininessChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>specularColor</b>. Specular highlights are brightness
     * reflections (example:  shiny spots on an apple).
     *
     * @param newSpecularColor The new value of SpecularColor
     */
    protected void setBackSpecularColor (float[] newSpecularColor)
        throws InvalidFieldValueException {

        super.setBackSpecularColor(newSpecularColor);

        if(inSetup)
            return;

        backSpecularChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>transparency</b>.  How "clear" an object is:  1.0 is completely
     * transparent, 0.0 is completely opaque .
     *
     * @param newTransparency The new value of Transparency
     */
    protected void setBackTransparency(float newTransparency)
        throws InvalidFieldValueException {

        super.setBackTransparency(newTransparency);

        if(inSetup)
            return;

        backTransparencyChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
    }

    /**
     * Set a new value for the separated back face colour state.
     *
     * @param state true to use different front from back colours
     */
    protected void setSeparateBackColor(boolean state)
        throws InvalidFieldValueException {

        super.setSeparateBackColor(state);

        if(inSetup)
            return;

        separateBackColorChanged = true;
        if(material.isLive())
            material.dataChanged(this);
        else
            updateNodeDataChanges(this);
    }

}
