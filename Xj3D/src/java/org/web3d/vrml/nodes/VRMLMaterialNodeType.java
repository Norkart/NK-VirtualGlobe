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

package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * Node specifies colour properties for associated geometry.
 * <p>
 * Defines methods needed for standard VRML lighting model equations
 * <p>
 *
 * @author Alan Hudson
 * @version $Revision: 1.10 $
 */
public interface VRMLMaterialNodeType extends VRMLVisualMaterialNodeType {

    /**
     * Accessor method to set a new value for field attribute
     * ambientIntensity. How much ambient omnidirectional light is
     * reflected from all light sources.
     *
     * @param newAmbientIntensity The new intensity value
     */
    public void setAmbientIntensity (float newAmbientIntensity)
        throws InvalidFieldValueException;

    /**
     * Accessor method to get current value of field ambientIntensity.
     * Default value is <code>0.2</code>
     *
     * @return The current ambientIntensity
     */
    public float getAmbientIntensity ();

    /**
     * Accessor method to set a new value for field attribute
     * diffuseColor. How much direct, angle-dependent light is
     * reflected from all light sources.
     *
     * @param newDiffuseColor The new value of diffuseColor
     */
    public void setDiffuseColor (float[] newDiffuseColor)
        throws InvalidFieldValueException;

    /**
     * Accessor method to get current value of field diffuseColor.
     * Default value is <code>0.8 0.8 0.8</code>.
     *
     * @return The current value of diffuseColor
     */
    public float[] getDiffuseColor();

    /**
     * Accessor method to set a new value for field attribute
     * emissiveColor. How much glowing light is emitted from this object.
     *
     * @param newEmissiveColor The new value of EmissiveColor
     */
    public void setEmissiveColor(float[] newEmissiveColor)
        throws InvalidFieldValueException;

    /**
     * Accessor method to get current value of field emissiveColor.
     * Default value is <code>0 0 0</code>.
     *
     * @return The current value of EmissiveColor
     */
    public float[] getEmissiveColor();

    /**
     * Accessor method to set a new value for field attribute shininess.
     * Low values provide soft specular glows, high values provide sharper,
     * smaller highlights.
     *
     * @param newShininess The new value of Shininess
     */
    public void setShininess(float newShininess)
        throws InvalidFieldValueException;

    /**
     * Get current value of the shinines field. Default value is
     * <code>0.2</code>.
     *
     * @return The current value of Shininess
     */
    public float getShininess();

    /**
     * Accessor method to set a new value for field attribute
     * specularColor. Specular highlights are brightness
     * reflections (example:  shiny spots on an apple).
     *
     * @param newSpecularColor The new value of SpecularColor
     */
    public void setSpecularColor (float[] newSpecularColor)
        throws InvalidFieldValueException;

    /**
     * Get current value of the specularColor field.
     * Default value is <code>0 0 0</code>.
     *
     * @return The current value of SpecularColor
     */
    public float[] getSpecularColor ();

    /**
     * Set a new value for the transparency. 1.0 is completely
     * transparent, 0.0 is completely opaque.
     *
     * @param newTransparency The new value of Transparency
     */
    public void setTransparency(float newTransparency)
        throws InvalidFieldValueException;

    /**
     * Accessor method to get current value of field transparency.
     * Default value is <code>0</code>
     *
     * @return The current value of Transparency
     */
    public float getTransparency();

    /**
     * Ignore the diffuseColor color term and use 1,1,1 for the diffuse color.
     *
     * @param ignore True to ignore the diffuse term
     */
    public void setIgnoreDiffuse(boolean ignore);

    /**
     * Add a listener instance for the material color change notifications.
     * Adding the same instance more than once is ignored. Adding null values
     * are ignored.
     *
     * @param l The new instance to add
     */
    public void addMaterialColorListener(MaterialColorListener l);

    /**
     * Remove a listener instance from this node. If the listener is not
     * currently registered, the request is silently ignored.
     *
     * @param l The new instance to remove
     */
    public void removeMaterialColorListener(MaterialColorListener l);
}
