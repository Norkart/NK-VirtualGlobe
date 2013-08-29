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

package org.web3d.vrml.renderer.j3d.nodes.shape;

// Standard imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.SceneGraphObject;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.TransparencyAttributes;

import javax.vecmath.Color3f;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;

import org.web3d.vrml.util.FieldValidator;

import org.web3d.vrml.renderer.common.nodes.shape.BaseMaterial;
import org.web3d.vrml.renderer.j3d.nodes.J3DGlobalStatus;
import org.web3d.vrml.renderer.j3d.nodes.J3DMaterialNodeType;
import org.web3d.vrml.renderer.j3d.nodes.J3DTransparencyListener;

/**
 * Java3D implementation of a material node.
 * <p>
 *
 * @author Russell Dodds
 * @version $Revision: 1.7 $
 */
public class J3DMaterial extends BaseMaterial
    implements J3DMaterialNodeType {

    /** White color for ignoring diffuse */
    private static Color3f ignoreColor;

    /** The Java 3D node that we are acutally using */
    private Material j3dImplNode;

    /** Ambient Color, calculated from ambientIntensity and diffuseColor */
    private Color3f ambColor;

    /** diffuseColor in Java3d times */
    private Color3f diffuseColor;

    /** emissiveColor in Java3D times */
    private Color3f emissiveColor;

    /** specularColor in Java3D times */
    private Color3f specularColor;

    /** Listeners for material */
    private ArrayList listenerList;

    static {
        ignoreColor = new Color3f(1,1,1);
    }

    /**
     * Construct a default instance of the material
     */
    public J3DMaterial() {
        init();
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the right type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public J3DMaterial(VRMLNodeType node) {
        super(node);

        init();
    }

    //-------------------------------------------------------------
    // Methods required by the J3DMaterialNodeType interface.
    //-------------------------------------------------------------

    /*
     * Returns a J3D Material node representation of the contents
     *
     * @return The material used to render the object
     */
    public Material getMaterial() {
        return j3dImplNode;
    }

    /**
     * Add a listener for transparency changes
     *
     * @param tl The listener to add
     */
    public void addTransparencyListener(J3DTransparencyListener tl) {
        if (!listenerList.contains(tl)) {
            listenerList.add(tl);
        }
    }

    /**
     * Remove a listener for transparency changes
     *
     * @param tl The listener to remove
     */
    public void removeTransparencyListener(J3DTransparencyListener tl) {
        listenerList.remove(tl);
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
        j3dImplNode.setLightingEnable(enable);
    }

    //-------------------------------------------------------------
    // Methods required by the VRMLMaterialNodeType interface.
    //-------------------------------------------------------------

    /**
     * Accessor method to set a new value for field attribute
     * ambientIntensity. How much ambient omnidirectional light is
     * reflected from all light sources.
     *
     * @param intensity The new intensity value
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setAmbientIntensity(float intensity)
        throws InvalidFieldValueException {

        super.setAmbientIntensity(intensity);

        ambColor.set(vfAmbientIntensity * vfDiffuseColor[0],
                     vfAmbientIntensity * vfDiffuseColor[1],
                     vfAmbientIntensity * vfDiffuseColor[2]);

        j3dImplNode.setAmbientColor(ambColor);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>diffuseColor</b>.  How much direct, angle-dependent light is
     * reflected from all light sources.
     *
     * @param color The new value of diffuseColor
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setDiffuseColor(float[] color)
        throws InvalidFieldValueException {

        super.setDiffuseColor(color);

        diffuseColor.x = vfDiffuseColor[0];
        diffuseColor.y = vfDiffuseColor[1];
        diffuseColor.z = vfDiffuseColor[2];

        ambColor.set(vfAmbientIntensity * vfDiffuseColor[0],
                     vfAmbientIntensity * vfDiffuseColor[1],
                     vfAmbientIntensity * vfDiffuseColor[2]);

        j3dImplNode.setAmbientColor(ambColor);

        if (ignoreDiffuse)
            j3dImplNode.setDiffuseColor(ignoreColor);
        else
            j3dImplNode.setDiffuseColor(diffuseColor);
    }

    /**
     * Ignore the diffuseColor color term and use 1,1,1 for the diffuse color.
     *
     * @param ignore True to ignore the diffuse term
     */
    public void setIgnoreDiffuse(boolean ignore) {

        super.setIgnoreDiffuse(ignore);

        if (inSetup)
            return;

        if (ignoreDiffuse)
            j3dImplNode.setDiffuseColor(ignoreColor);
        else
            j3dImplNode.setDiffuseColor(diffuseColor);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>emissiveColor</b>. How much glowing light is emitted from this object.
     *
     * @param color The new value of EmissiveColor
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setEmissiveColor(float[] color)
        throws InvalidFieldValueException {

        super.setEmissiveColor(color);

        emissiveColor.x = vfEmissiveColor[0];
        emissiveColor.y = vfEmissiveColor[1];
        emissiveColor.z = vfEmissiveColor[2];

        j3dImplNode.setEmissiveColor(emissiveColor);
    }

    /**
     * Accessor method to set a new value for field attribute <b>shininess</b>.
     * Low values provide soft specular glows, high values provide sharper,
     * smaller highlights.
     *
     * @param newShininess The new value of Shininess
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setShininess(float newShininess)
        throws InvalidFieldValueException  {

        super.setShininess(newShininess);

        float val = (vfShininess * 127.0f) + 1.0f;
        j3dImplNode.setShininess(val);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>specularColor</b>. Specular highlights are brightness
     * reflections (example:  shiny spots on an apple).
     *
     * @param color The new value of SpecularColor
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setSpecularColor (float[] color)
        throws InvalidFieldValueException {

        super.setSpecularColor(color);

        specularColor.x = vfSpecularColor[0];
        specularColor.y = vfSpecularColor[1];
        specularColor.z = vfSpecularColor[2];

        j3dImplNode.setSpecularColor(specularColor);
    }

    /**
     * Accessor method to set a new value for field attribute
     * <b>transparency</b>.  How "clear" an object is:  1.0 is completely
     * transparent, 0.0 is completely opaque .
     *
     * @param trans The new value of Transparency
     * @throws InvalidFieldValueException The field value is not legal for
     *   the field specified.
     */
    public void setTransparency(float trans)
        throws InvalidFieldValueException {

        super.setTransparency(trans);

        if (!inSetup) {
            // Notify listeners of new value
            int num_listeners = listenerList.size();
            J3DTransparencyListener tl;

            for(int i = 0; i < num_listeners; i++) {
                tl = (J3DTransparencyListener)listenerList.get(i);
                tl.transparencyChanged(trans);
            }
        }
    }

    //----------------------------------------------------------
    // Methods required by the J3DVRMLNodeTypeType interface.
    //----------------------------------------------------------

    /**
     * Provide the set of mappings that override anything that the loader
     * might set. Default implementation does nothing.
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

        if(capBits != null && capBits.containsKey(Material.class)) {
            bits = (int[])capBits.get(Material.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    j3dImplNode.clearCapability(bits[i]);
            } else if(!isStatic) {
                // unset the cap bits that would have been set in setVersion()
                j3dImplNode.clearCapability(Material.ALLOW_COMPONENT_WRITE);
            }
        }

        // Now do the same for the frequency bit API
        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null ||
           !freqBits.containsKey(Material.class))
            return;

        bits = (int[])freqBits.get(Material.class);

        size = (bits == null) ? 0 : bits.length;

        if(size != 0) {
            for(i = 0; i < size; i++)
                j3dImplNode.clearCapabilityIsFrequent(bits[i]);
        } else if(!isStatic) {
            j3dImplNode.clearCapabilityIsFrequent(
                Material.ALLOW_COMPONENT_WRITE);
        }
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
        int[] bits;
        int i;
        int size;

        if(capBits != null) {
            bits = (int[])capBits.get(Material.class);
            size = (bits == null) ? 0 : bits.length;

            if(size != 0) {
                for(i = 0; i < size; i++)
                    j3dImplNode.setCapability(bits[i]);
            }
        }

        if(!J3DGlobalStatus.haveFreqBitsAPI || freqBits == null)
            return;

        bits = (int[])freqBits.get(Material.class);

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

        // reset the ambient color becuase the first time through might have
        // had the diffuse color not set yet and resulted in a value of
        // zero.
        ambColor.set(vfAmbientIntensity * vfDiffuseColor[0],
                     vfAmbientIntensity * vfDiffuseColor[1],
                     vfAmbientIntensity * vfDiffuseColor[2]);

        diffuseColor.x = vfDiffuseColor[0];
        diffuseColor.y = vfDiffuseColor[1];
        diffuseColor.z = vfDiffuseColor[2];

        emissiveColor.x = vfEmissiveColor[0];
        emissiveColor.y = vfEmissiveColor[1];
        emissiveColor.z = vfEmissiveColor[2];

        specularColor.x = vfSpecularColor[0];
        specularColor.y = vfSpecularColor[1];
        specularColor.z = vfSpecularColor[2];

        j3dImplNode.setDiffuseColor(diffuseColor);
        j3dImplNode.setEmissiveColor(emissiveColor);
        j3dImplNode.setSpecularColor(specularColor);
        j3dImplNode.setAmbientColor(ambColor);
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

        // Required for delayed appearance changes
        j3dImplNode.setCapability(Material.ALLOW_COMPONENT_WRITE);

        if(isStatic)
            return;
    }

    //----------------------------------------------------------
    // Methods internal to J3DMaterial
    //----------------------------------------------------------

    private void init() {
        float amb = 0.2f * 0.8f;
        float val = (vfShininess * 127.0f) + 1.0f;

        ambColor = new Color3f(amb, amb, amb);
        diffuseColor = new Color3f(0.8f,0.8f,0.8f);
        emissiveColor = new Color3f(0,0,0);
        specularColor = new Color3f(0,0,0);

        listenerList = new ArrayList();

        j3dImplNode = new Material(ambColor,
                                   emissiveColor,
                                   diffuseColor,
                                   specularColor,
                                   val);

        if (ignoreDiffuse)
            j3dImplNode.setDiffuseColor(ignoreColor);

        j3dImplNode.setLightingEnable(true);
    }
}
