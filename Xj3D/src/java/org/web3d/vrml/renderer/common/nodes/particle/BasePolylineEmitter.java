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
import java.util.HashMap;

import org.j3d.geom.particle.PolylineEmitter;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

/**
 * Common implementation of a PolylineEmitter node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 2.3 $
 */
public abstract class BasePolylineEmitter extends BaseEmitter
    implements VRMLNodeComponentListener {

    // Field index constants

    /** The field index for the coordinate node */
    protected static final int FIELD_COORDS = LAST_EMITTER_INDEX + 1;

    /** The field index for the coordIndex field */
    protected static final int FIELD_COORDINDEX = LAST_EMITTER_INDEX + 2;

    /** The field index for the set_coordIndex field */
    protected static final int FIELD_SET_COORDINDEX = LAST_EMITTER_INDEX + 3;

    /** The field index for direction */
    protected static final int FIELD_DIRECTION = LAST_EMITTER_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_LINE_EMITTER_INDEX = FIELD_DIRECTION;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_LINE_EMITTER_INDEX + 1;

    /** Message for when the proto is not a Coord */
    protected static final String COORD_PROTO_MSG =
        "Proto does not describe a Coord object";

    /** Message for when the node in setValue() is not a Coord */
    protected static final String COORD_NODE_MSG =
        "Node does not describe a Coord object";


    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    // The VRML field values

    /** Proto version of the coord */
    protected VRMLProtoInstance pCoord;

    /** exposedField SFNode coord */
    protected VRMLCoordinateNodeType vfCoord;

    /** The value of the cycle time field */
    protected int[] vfCoordIndex;

    /** Number of valid values in vfColorIndex */
    protected int numCoordIndex;

    /** The value of the cycle interval field */
    protected float[] vfDirection;

    /** Internal array for generating coordinates for the polyline */
    private float[] coordTmp;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_COORDS, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_COORDS] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "coords");
        fieldDecl[FIELD_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFInt32",
                                     "coordIndex");
        fieldDecl[FIELD_SET_COORDINDEX] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFInt32",
                                     "set_coordIndex");
        fieldDecl[FIELD_DIRECTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFVec3f",
                                     "direction");
        fieldDecl[FIELD_VARIATION] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "variation");
        fieldDecl[FIELD_SPEED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "speed");
        fieldDecl[FIELD_MASS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "mass");
        fieldDecl[FIELD_SURFACE_AREA] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "surfaceArea");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_COORDS);
        fieldMap.put("coords", idx);
        fieldMap.put("set_coords", idx);
        fieldMap.put("coords_changed", idx);

        fieldMap.put("coordIndex", new Integer(FIELD_COORDINDEX));

        idx = new Integer(FIELD_DIRECTION);
        fieldMap.put("direction", idx);
        fieldMap.put("set_direction", idx);
        fieldMap.put("direction_changed", idx);

        idx = new Integer(FIELD_SPEED);
        fieldMap.put("speed", idx);
        fieldMap.put("set_speed", idx);
        fieldMap.put("speed_changed", idx);

        idx = new Integer(FIELD_VARIATION);
        fieldMap.put("variation", idx);
        fieldMap.put("set_variation", idx);
        fieldMap.put("variation_changed", idx);

        idx = new Integer(FIELD_MASS);
        fieldMap.put("mass", idx);
        fieldMap.put("set_mass", idx);
        fieldMap.put("mass_changed", idx);

        idx = new Integer(FIELD_SURFACE_AREA);
        fieldMap.put("surfaceArea", idx);
        fieldMap.put("set_surfaceArea", idx);
        fieldMap.put("surfaceArea_changed", idx);

        fieldMap.put("set_coordIndex", new Integer(FIELD_SET_COORDINDEX));
    }

    /**
     * Construct a new time sensor object
     */
    protected BasePolylineEmitter() {
        super("PolylineEmitter");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfDirection = new float[] {0, 1, 0};
        vfCoordIndex = FieldConstants.EMPTY_MFINT32;
        numCoordIndex = 0;

        initializer = new PolylineEmitter();
        ((PolylineEmitter)initializer).setDirection(0, 1, 0);
        initializer.setMass(vfMass);
        initializer.setSurfaceArea(vfSurfaceArea);
        initializer.setSpeed(vfSpeed);
        initializer.setParticleVariation(vfVariation);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    public BasePolylineEmitter(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLParticleEmitterNodeType)node);

        try {
            int index = node.getFieldIndex("direction");
            VRMLFieldData field = node.getFieldValue(index);
            vfDirection[0] = field.floatArrayValue[0];
            vfDirection[1] = field.floatArrayValue[1];
            vfDirection[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("coordIndex");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfCoordIndex = new int[field.numElements];
                numCoordIndex = field.numElements;
                System.arraycopy(field.intArrayValue,
                                 0,
                                 vfCoordIndex,
                                 0,
                                 field.numElements);
            }

            ((PolylineEmitter)initializer).setDirection(vfDirection[0],
                                                        vfDirection[1],
                                                        vfDirection[2]);

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by FrameStateListener
    //----------------------------------------------------------

    /**
     * Ignored by this base implementation. Any class that needs this method
     * may override this method as required.
     */
    public void allEventsComplete() {
        updateLineCoords();
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeComponentListener
    //----------------------------------------------------------

    /**
     * Notification that the field from the coordinate node has changed.
     *
     * @param node The source node that changed the field
     * @param index The field index that changed
     */
    public void fieldChanged(VRMLNodeType node, int index) {
        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer)fieldMap.get(fieldName);

        return (index == null) ? -1 : index.intValue();
    }

    /**
     * Get the list of indices that correspond to fields that contain nodes
     * ie MFNode and SFNode). Used for blind scene graph traversal without
     * needing to spend time querying for all fields etc. If a node does
     * not have any fields that contain nodes, this shall return null. The
     * field list covers all field types, regardless of whether they are
     * readable or not at the VRML-level.
     *
     * @return The list of field indices that correspond to SF/MFnode fields
     *    or null if none
     */
    public int[] getNodeFieldIndices() {
        return nodeFields;
    }

    /**
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_LINE_EMITTER_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        return fieldDecl.length;
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
            case FIELD_COORDS:
                fieldData.clear();
                if(pCoord != null)
                    fieldData.nodeValue = pCoord;
                else
                    fieldData.nodeValue = vfCoord;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_COORDINDEX:
                fieldData.clear();
                fieldData.intArrayValue = vfCoordIndex;
                fieldData.numElements = numCoordIndex;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                break;

            case FIELD_DIRECTION:
                fieldData.clear();
                fieldData.floatArrayValue = vfDirection;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
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

        if(pCoord != null)
            pCoord.setupFinished();
        else if(vfCoord != null)
            vfCoord.setupFinished();

        if(numCoordIndex != 0)
            updateLineCoords();
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
                case FIELD_COORDS:
                    destNode.setValue(destIndex, vfCoord);
                    break;

                case FIELD_DIRECTION:
                    destNode.setValue(destIndex, vfDirection, 3);
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
     * Set the value of the field at the given index as a int array for the
     * MFInt32 and SF/MFImage fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_SET_COORDINDEX:
                if(inSetup)
                    throw new InvalidFieldAccessException("Attempting to write " +
                        "an inputOnly field at setup time");

                setCoordIndex(value, numValid);
                break;

            case FIELD_COORDINDEX:
                if(!inSetup)
                    throw new InvalidFieldAccessException("Attempting to write " +
                        "an initializeOnly field at runtime");

                vfCoordIndex = value;
                numCoordIndex = numValid;
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a float for the
     * SFRotation fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_DIRECTION:
                setDirection(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_COORDS:
                setCoord(index, child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the coordIndex field. Override to provide.renderer-specific behaviour,
     * but remember to also call this implementation too.
     *
     * @param value The new set of coordinate index values
     * @param numValid The number of valid values to copy from the array
     */
    protected void setCoordIndex(int[] value, int numValid) {
        vfCoordIndex = value;
        numCoordIndex = numValid;

        if(!inSetup)
            stateManager.addEndOfThisFrameListener(this);
    }

    /**
     * Set the coordinate node that forms the base of the line emitter.
     *
     * @param coord The coordinate node to source vertex data from
     */
    protected void setCoord(int field, VRMLNodeType coord)
        throws InvalidFieldValueException {

        VRMLProtoInstance proto;
        VRMLNodeType node = coord;

        if(node instanceof VRMLProtoInstance) {
            proto = (VRMLProtoInstance) node;
            node = proto.getImplementationNode();
        } else {
            proto = null;
        }

        if(vfCoord != null)
            vfCoord.removeComponentListener(this);

        pCoord = (VRMLProtoInstance)proto;
        vfCoord = (VRMLCoordinateNodeType)node;
        if(vfCoord != null)
            vfCoord.addComponentListener(this);

        if(!inSetup) {
            stateManager.addEndOfThisFrameListener(this);
            hasChanged[FIELD_COORDS] = true;
            fireFieldChanged(FIELD_COORDS);
        }
    }


    /**
     * The direction value has just been set.
     *
     * @param dir The new direction of the emitter.
     */
    protected void setDirection(float[] dir) {
        vfDirection[0] = dir[0];
        vfDirection[1] = dir[1];
        vfDirection[2] = dir[2];

        ((PolylineEmitter)initializer).setDirection(dir[0], dir[1], dir[2]);

        if(!inSetup) {
            hasChanged[FIELD_DIRECTION] = true;
            fireFieldChanged(FIELD_DIRECTION);
        }
    }

    /**
     * Internal convenience method to update the coordinates being fed to the
     * polyine emitter.
     */
    private void updateLineCoords() {
        if(vfCoord == null)
            return;

        if(coordTmp == null || coordTmp.length < numCoordIndex * 3)
            coordTmp = new float[numCoordIndex * 3];

        float[] coords = vfCoord.getPointRef();

        for(int i = 0; i < numCoordIndex; i++) {
            coordTmp[i * 3]     = coords[vfCoordIndex[i] * 3];
            coordTmp[i * 3 + 1] = coords[vfCoordIndex[i] * 3 + 1];
            coordTmp[i * 3 + 2] = coords[vfCoordIndex[i] * 3 + 2];
        }

        ((PolylineEmitter)initializer).setEmitterLine(coordTmp, numCoordIndex);
    }
}
