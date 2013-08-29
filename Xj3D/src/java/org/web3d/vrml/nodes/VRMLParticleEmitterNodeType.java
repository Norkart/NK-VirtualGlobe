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
package org.web3d.vrml.nodes;

// External imports
import org.j3d.geom.particle.ParticleInitializer;

// Local imports
import org.web3d.vrml.lang.InvalidFieldValueException;


/**
 * A node which specifies an emitter of particles for a particle system.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface VRMLParticleEmitterNodeType extends VRMLNodeType {

    /**
     * Get the internal initialiser model that will be used for this particle
     * system implementation. This may not be available until after
     * setupFinished() has been called.
     *
     * @return The initialiser instance to use
     */
    public ParticleInitializer getInitializer();

    /**
     * Get the current value of the speed field.
     *
     * @return the current speed value 0 - inf.
     */
    public float getSpeed();

    /**
     * Set the position field value to the new value.
     *
     * @param speed The new position to set
     */
    public void setSpeed(float speed) throws InvalidFieldValueException;

    /**
     * Get the current value of the mass field.
     *
     * @return the current mass value 0 - inf.
     */
    public float getMass();

    /**
     * Set the position field value to the new value.
     *
     * @param mass The new mass to set
     * @throws InvalidFieldValueException The mass value was negative
     */
    public void setMass(float mass)
        throws InvalidFieldValueException;

    /**
     * Get the current value of the surfaceArea field.
     *
     * @return the current surfaceArea value 0 - inf.
     */
    public float getSurfaceArea();

    /**
     * Set the position field value to the new value.
     *
     * @param surfaceArea The new amount of surface area to set
     * @throws InvalidFieldValueException The area value was negative
     */
    public void setSurfaceArea(float surfaceArea)
        throws InvalidFieldValueException;

    /**
     * Get the current value of the variation field.
     *
     * @return The current variation value
     */
    public float getVariation();

    /**
     * Set the variation field value to the new value. If it is out of
     * range the throw an exception.
     *
     * @param variation The new amount of variation to set
     * @throws InvalidFieldValueException The field is out of range
     */
    public void setVariation(float variation)
        throws InvalidFieldValueException;
}
