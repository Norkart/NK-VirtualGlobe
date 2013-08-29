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

package org.web3d.vrml.renderer.common.nodes.particle;

// External imports
import org.j3d.geom.particle.ParticleInitializer;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common implementation of all Emitter nodes.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 2.1 $
 */
public abstract class BaseEmitter extends AbstractNode
    implements VRMLParticleEmitterNodeType {

    // Field index constants

    /** The field index for speed */
    protected static final int FIELD_SPEED = LAST_NODE_INDEX + 1;

    /** The field index for mass */
    protected static final int FIELD_MASS = LAST_NODE_INDEX + 2;

    /** The field index for surfaceArea */
    protected static final int FIELD_SURFACE_AREA = LAST_NODE_INDEX + 3;

    /** The field index for variation */
    protected static final int FIELD_VARIATION = LAST_NODE_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_EMITTER_INDEX = FIELD_VARIATION;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_EMITTER_INDEX + 1;

    /** Message when the mass value is negative */
    private static final String NEG_MASS_ERR = "Mass must be >= zero";

    /** Message when the surface area set is negative */
    private static final String NEG_AREA_ERR = "Surface area must be >= zero";

    // The VRML field values

    /** The value of the velocity field */
    protected float vfSpeed;

    /** The value of the variation field */
    protected float vfVariation;

    /** The value of the mass field */
    protected float vfMass;

    /** The value of the surfaceArea field */
    protected float vfSurfaceArea;

    /** The particle initializer corresponding to this emitter type */
    protected ParticleInitializer initializer;

    /**
     * Construct a new time sensor object
     */
    protected BaseEmitter(String type) {
        super(type);

        vfSpeed = 0;
        vfMass = 0;
        vfSurfaceArea = 0;
        vfVariation = 0.25f;
    }

    /**
     * Copy the values of the given node to this one.
     *
     * @param node The emitter to use
     */
    protected void copy(VRMLParticleEmitterNodeType node) {

        try {
            int index = node.getFieldIndex("speed");
            VRMLFieldData field = node.getFieldValue(index);
            vfSpeed = field.floatValue;

            index = node.getFieldIndex("mass");
            field = node.getFieldValue(index);
            vfMass = field.floatValue;

            index = node.getFieldIndex("surfaceArea");
            field = node.getFieldValue(index);
            vfSurfaceArea = field.floatValue;

            index = node.getFieldIndex("variation");
            field = node.getFieldValue(index);
            vfVariation = field.floatValue;

            initializer.setMass(vfMass);
            initializer.setSurfaceArea(vfSurfaceArea);
            initializer.setSpeed(vfSpeed);
            initializer.setParticleVariation(vfVariation);
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLParticleEmitterNodeType
    //-------------------------------------------------------------

    /**
     * Get the internal initialiser model that will be used for this particle
     * system implementation. This may not be available until after
     * setupFinished() has been called.
     *
     * @return The initialiser instance to use
     */
    public ParticleInitializer getInitializer() {
        return initializer;
    }

    /**
     * Get the current value of the speed field.
     *
     * @return the current speed value 0 - inf.
     */
    public float getSpeed() {
        return vfSpeed;
    }

    /**
     * Set the position field value to the new value.
     *
     * @param speed The new position to set
     */
    public void setSpeed(float speed)
        throws InvalidFieldValueException {

        vfSpeed = speed;
        initializer.setSpeed(vfSpeed);

        if(!inSetup) {
            hasChanged[FIELD_SPEED] = true;
            fireFieldChanged(FIELD_SPEED);
        }
    }

    /**
     * Get the current value of the mass field.
     *
     * @return the current mass value 0 - inf.
     */
    public float getMass() {
        return vfMass;
    }

    /**
     * Set the position field value to the new value.
     *
     * @param mass The new mass to set
     * @throws InvalidFieldValueException The mass value was negative
     */
    public void setMass(float mass)
        throws InvalidFieldValueException {

        if(mass < 0)
            throw new InvalidFieldValueException(NEG_MASS_ERR);

        vfMass = mass;
        initializer.setMass(vfMass);

        if(!inSetup) {
            hasChanged[FIELD_MASS] = true;
            fireFieldChanged(FIELD_MASS);
        }
    }

    /**
     * Get the current value of the surfaceArea field.
     *
     * @return the current surfaceArea value 0 - inf.
     */
    public float getSurfaceArea() {
        return vfSurfaceArea;
    }

    /**
     * Set the position field value to the new value.
     *
     * @param surfaceArea The new amount of surface area to set
     * @throws InvalidFieldValueException The area value was negative
     */
    public void setSurfaceArea(float surfaceArea)
        throws InvalidFieldValueException {

        if(surfaceArea < 0)
            throw new InvalidFieldValueException(NEG_AREA_ERR);

        vfSurfaceArea = surfaceArea;
        initializer.setSurfaceArea(vfSurfaceArea);

        if(!inSetup) {
            hasChanged[FIELD_SURFACE_AREA] = true;
            fireFieldChanged(FIELD_SURFACE_AREA);
        }
    }

    /**
     * Get the current value of the variation field.
     *
     * @return The current variation value
     */
    public float getVariation() {
        return vfVariation;
    }

    /**
     * Set the variation field value to the new value. If it is out of
     * range the throw an exception.
     *
     * @param variation The new amount of variation to set
     * @throws InvalidFieldValueException The field is out of range
     */
    public void setVariation(float variation)
        throws InvalidFieldValueException {

        vfVariation = variation;
        initializer.setParticleVariation(vfVariation);

        if(!inSetup) {
            hasChanged[FIELD_VARIATION] = true;
            fireFieldChanged(FIELD_VARIATION);
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ParticleEmitterNodeType;
    }

    /**
     * Get the value of a field. If the field is a primitive type, it will
     * return a class representing the value. For arrays or nodes it will
     * return the instance directly.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value
     * @throws InvalidFieldException The field index is not known
     */
   public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {

        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_SPEED:
                fieldData.clear();
                fieldData.floatValue = vfSpeed;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_MASS:
                fieldData.clear();
                fieldData.floatValue = vfMass;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SURFACE_AREA:
                fieldData.clear();
                fieldData.floatValue = vfSurfaceArea;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_VARIATION:
                fieldData.clear();
                fieldData.floatValue = vfVariation;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Send a routed value from this node to the given destination node. The
     * route should use the appropriate setValue() method of the destination
     * node. It should not attempt to cast the node up to a higher level.
     * Routing should also follow the standard rules for the loop breaking and
     * other appropriate rules for the specification.
     *
     * @param time The time that this route occurred (not necessarily epoch
     *   time. Should be treated as a relative value only)
     * @param srcIndex The index of the field in this node that the value
     *   should be sent from
     * @param destNode The node reference that we will be sending the value to
     * @param destIndex The index of the field in the destination node that
     *   the value should be sent to.
     */
    public void sendRoute(double time,
                          int srcIndex,
                          VRMLNodeType destNode,
                          int destIndex) {

        // Simple impl for now.  ignores time and looping

        try {
            switch(srcIndex) {
                case FIELD_SPEED:
                    destNode.setValue(destIndex, vfSpeed);
                    break;

                case FIELD_MASS:
                    destNode.setValue(destIndex, vfMass);
                    break;

                case FIELD_SURFACE_AREA:
                    destNode.setValue(destIndex, vfSurfaceArea);
                    break;

                case FIELD_VARIATION:
                    destNode.setValue(destIndex, vfVariation);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field! " + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as a float for the
     * SFFloat fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SPEED:
                setSpeed(value);
                break;

            case FIELD_MASS:
                setMass(value);
                break;

            case FIELD_SURFACE_AREA:
                setSurfaceArea(value);
                break;

            case FIELD_VARIATION:
                setVariation(value);
                break;

            default:
                super.setValue(index, value);
        }
    }
}
