/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.sensor;

// External imports
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLDragSensorNodeType;

import org.web3d.vrml.renderer.common.nodes.BaseDragSensorNode;

/**
 * Java3D implementation of a CylinderSensor node.
 * <p>
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.16 $
 */
public abstract class BaseCylinderSensor extends BaseDragSensorNode {
	
	// Field index constants
	
	/** The field index for maxAngle */
	protected static final int FIELD_MAXANGLE = LAST_DRAG_SENSOR_INDEX + 1;
	
	/** The field index for minAngle */
	protected static final int FIELD_MINANGLE = LAST_DRAG_SENSOR_INDEX + 2;
	
	/** The field index for diskAngle */
	protected static final int FIELD_DISKANGLE = LAST_DRAG_SENSOR_INDEX + 3;
	
	/** The field index for translation_changed */
	protected static final int FIELD_ROTATION_CHANGED = LAST_DRAG_SENSOR_INDEX + 4;
	
	/** The field index for offset */
	protected static final int FIELD_OFFSET = LAST_DRAG_SENSOR_INDEX + 5;
	
	/** The last field index used by this class */
	protected static final int LAST_PLANESENSOR_INDEX = FIELD_OFFSET;
	
	/** Number of fields constant */
	protected static final int NUM_FIELDS = LAST_PLANESENSOR_INDEX + 1;
	
	/** Empty origin used for cylinder center location representation */
	private static final float[] ORIGIN;
	
	/** Array of VRMLFieldDeclarations */
	private static VRMLFieldDeclaration[] fieldDecl;
	
	/** Hashmap between a field name and its index */
	private static HashMap fieldMap;
	
	/** Listing of field indexes that have nodes */
	private static int[] nodeFields;
	
	// The VRML field values
	
	/** The value of the maxAngle field */
	protected float vfMaxAngle;
	
	/** The value of the minAngle field */
	protected float vfMinAngle;
	
	/** The value of the diskAngle field */
	protected float vfDiskAngle;
	
	/** The value of the offset field */
	protected float vfOffset;
	
	/** The value of the translation_changed field*/
	protected float[] vfRotationChanged;
	
	/** Flag to say if the output should be disk or side */
	private boolean useDiskCalc;
	
	/** Initial rotation value on first click */
	private double initialRotation;
	
	/** The radius of the initial intersection from the Y axis */
	private float radius;
	
	/**
	 * Static constructor to build the field representations of this node
	 * once for all users.
	 */
	static {
		ORIGIN = new float[3];
		
		nodeFields = new int[] { FIELD_METADATA };
		
		fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
		fieldMap = new HashMap(NUM_FIELDS * 3);
		
		fieldDecl[FIELD_METADATA] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFNode",
			"metadata");
		fieldDecl[FIELD_ENABLED] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFBool",
			"enabled");
		fieldDecl[FIELD_AUTOOFFSET] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFBool",
			"autoOffset");
		fieldDecl[FIELD_TRACKPOINT_CHANGED] =
			new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
			"SFVec3f",
			"trackPoint_changed");
		fieldDecl[FIELD_IS_ACTIVE] =
			new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
			"SFBool",
			"isActive");
		fieldDecl[FIELD_IS_OVER] =
			new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
			"SFBool",
			"isOver");
		fieldDecl[FIELD_ROTATION_CHANGED] =
			new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
			"SFRotation",
			"rotation_changed");
		fieldDecl[FIELD_MINANGLE] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFFloat",
			"minAngle");
		fieldDecl[FIELD_MAXANGLE] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFFloat",
			"maxAngle");
		fieldDecl[FIELD_OFFSET] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFFloat",
			"offset");
		fieldDecl[FIELD_DISKANGLE] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFFloat",
			"diskAngle");
		
		fieldDecl[FIELD_DESCRIPTION] =
			new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
			"SFString",
			"description");
		
		Integer idx = new Integer(FIELD_METADATA);
		fieldMap.put("metadata", idx);
		fieldMap.put("set_metadata", idx);
		fieldMap.put("metadata_changed", idx);
		
		idx = new Integer(FIELD_ENABLED);
		fieldMap.put("enabled", idx);
		fieldMap.put("set_enabled", idx);
		fieldMap.put("enabled_changed", idx);
		
		idx = new Integer(FIELD_DESCRIPTION);
		fieldMap.put("description", idx);
		fieldMap.put("set_description", idx);
		fieldMap.put("description_changed", idx);
		
		fieldMap.put("isActive",new Integer(FIELD_IS_ACTIVE));
		fieldMap.put("isOver",new Integer(FIELD_IS_OVER));
		fieldMap.put("trackPoint_changed",new Integer(FIELD_TRACKPOINT_CHANGED));
		
		idx = new Integer(FIELD_AUTOOFFSET);
		fieldMap.put("autoOffset", idx);
		fieldMap.put("set_autoOffset", idx);
		fieldMap.put("autoOffset_changed", idx);
		
		idx = new Integer(FIELD_OFFSET);
		fieldMap.put("offset", idx);
		fieldMap.put("set_offset", idx);
		fieldMap.put("offset_changed", idx);
		
		idx = new Integer(FIELD_MAXANGLE);
		fieldMap.put("maxAngle", idx);
		fieldMap.put("set_maxAngle", idx);
		fieldMap.put("maxAngle_changed", idx);
		
		idx = new Integer(FIELD_MINANGLE);
		fieldMap.put("minAngle", idx);
		fieldMap.put("set_minAngle", idx);
		fieldMap.put("minAngle_changed", idx);
		
		idx = new Integer(FIELD_DISKANGLE);
		fieldMap.put("diskAngle", idx);
		fieldMap.put("set_diskAngle", idx);
		fieldMap.put("diskAngle_changed", idx);
		
		fieldMap.put("rotation_changed",new Integer(FIELD_ROTATION_CHANGED));
	}
	
	/**
	 * Construct a new time sensor object
	 */
	public BaseCylinderSensor() {
		super("CylinderSensor");
		
		hasChanged = new boolean[NUM_FIELDS];
		
		vfRotationChanged = new float[] { 0, 0, 1, 0 };
		vfMaxAngle = -1;
		vfDiskAngle = (float)Math.PI/12;
	}
	
	/**
	 * Construct a new instance of this node based on the details from the
	 * given node. If the node is not the same type, an exception will be
	 * thrown.
	 *
	 * @param node The node to copy
	 * @throws IllegalArgumentException The node is not the same type
	 */
	public BaseCylinderSensor(VRMLNodeType node) {
		this();
		
		checkNodeType(node);
		
		copy((VRMLDragSensorNodeType)node);
		
		try {
			int index = node.getFieldIndex("offset");
			VRMLFieldData field = node.getFieldValue(index);
			
			vfOffset = field.floatValue;
			
			index = node.getFieldIndex("minAngle");
			field = node.getFieldValue(index);
			vfMinAngle = field.floatValue;
			
			index = node.getFieldIndex("maxAngle");
			field = node.getFieldValue(index);
			vfMaxAngle = field.floatValue;
			
			index = node.getFieldIndex("diskAngle");
			field = node.getFieldValue(index);
			vfDiskAngle = field.floatValue;
		} catch(VRMLException ve) {
			throw new IllegalArgumentException(ve.getMessage());
		}
	}
	
	//--------------------------------------------------------------------
	// Methods defined by VRMLPointingDeviceSensorNodeType
	//--------------------------------------------------------------------
	
	/**
	 * Notification that this sensor has just been clicked on to start a drag
	 * action.
	 *
	 * @param hitPoint Where the input device intersected the object sensor
	 * @param location Where the sensor origin is in local coordinates
	 */
	public void notifySensorDragStart(float[] hitPoint, float[] location) {
		
		super.notifySensorDragStart(hitPoint, location);
		
		// Work out if we're using the end or the side of the cylinder.
		// The calc is based on the angle for the Y axis only. First need to
		// work out the distance from the Y axis in the X-Z plane.
		radius = (float)Math.sqrt(hitPoint[0] * hitPoint[0] +
			hitPoint[2] * hitPoint[2]);
		
		double hit_angle = Math.abs(Math.atan(radius / hitPoint[1]));
		
		// TODO: the hit_angle is not the correct value to use to compare
		// with the diskAngle. per the spec the angle to use is the "initial
		// acute angle between the bearing vector and the local Y-axis of the
		// CylinderSensor node".
		useDiskCalc = hit_angle < vfDiskAngle;
		
		initialRotation = calcAngle(hitPoint[0], hitPoint[2]);
	}
	
	/**
	 * Notification that this sensor has finished a drag action.
	 *
	 * @param position Where the sensor origin is in local coordinates
	 * @param direction Vector showing the direction the sensor is pointing
	 */
	public void notifySensorDragEnd(float[] position, float[] direction) {
		
		super.notifySensorDragEnd(position, direction);
		
		if(vfAutoOffset) {
			vfOffset = vfRotationChanged[3];
			
			hasChanged[FIELD_OFFSET] = true;
			fireFieldChanged(FIELD_OFFSET);
		}
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
		if(index < 0  || index > LAST_PLANESENSOR_INDEX)
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
		case FIELD_ROTATION_CHANGED:
			fieldData.clear();
			fieldData.floatArrayValue = vfRotationChanged;
			fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
			fieldData.numElements = 1;
			break;
			
		case FIELD_MAXANGLE:
			fieldData.clear();
			fieldData.floatValue = vfMaxAngle;
			fieldData.dataType = VRMLFieldData.FLOAT_DATA;
			break;
			
		case FIELD_MINANGLE:
			fieldData.clear();
			fieldData.floatValue = vfMinAngle;
			fieldData.dataType = VRMLFieldData.FLOAT_DATA;
			break;
			
		case FIELD_DISKANGLE:
			fieldData.clear();
			fieldData.floatValue = vfDiskAngle;
			fieldData.dataType = VRMLFieldData.FLOAT_DATA;
			break;
			
		case FIELD_OFFSET:
			fieldData.clear();
			fieldData.floatValue = vfOffset;
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
			case FIELD_ROTATION_CHANGED:
				destNode.setValue(destIndex, vfRotationChanged, 4);
				break;
				
			case FIELD_MAXANGLE:
				destNode.setValue(destIndex, vfMaxAngle);
				break;
				
			case FIELD_MINANGLE:
				destNode.setValue(destIndex, vfMinAngle);
				break;
				
			case FIELD_DISKANGLE:
				destNode.setValue(destIndex, vfDiskAngle);
				break;
				
			case FIELD_OFFSET:
				destNode.setValue(destIndex, vfOffset);
				break;
				
			default:
				super.sendRoute(time, srcIndex, destNode, destIndex);
			}
		} catch(InvalidFieldException ife) {
			System.err.println("BaseCylinderSensor.sendRoute: No field! " + srcIndex);
			ife.printStackTrace();
		} catch(InvalidFieldValueException ifve) {
			System.err.println("BaseCylinderSensor.sendRoute: Invalid field value: " +
				ifve.getMessage());
		}
	}
	
	/**
	 * Set the value of the field at the given index as a float array. This is
	 * be used to set SFFloat field types.
	 *
	 * @param index The index of destination field to set
	 * @param value The new value to use for the node
	 * @throws InvalidFieldException The field index is not known
	 */
	public void setValue(int index, float value)
		throws InvalidFieldException, InvalidFieldValueException {
		
		switch(index) {
		case FIELD_MINANGLE:
			vfMinAngle = value;
			break;
			
		case FIELD_MAXANGLE:
			vfMaxAngle = value;
			break;
			
		case FIELD_DISKANGLE:
			vfDiskAngle = value;
			break;
			
		case FIELD_OFFSET:
			vfOffset = value;
			break;
			
		default:
			super.setValue(index, value);
		}
		
		if(!inSetup) {
			hasChanged[index] = true;
			fireFieldChanged(index);
		}
	}
	
	/**
	 * Convenience method to generate the tracking output based on
	 * the input hit position.
	 *
	 * @param location The position of the mouse locally
	 * @param direction Vector showing the direction the sensor is pointing
	 */
	protected void processDrag(float[] location, float[] direction) {
		
		if ( Arrays.equals( ORIGIN, location ) && Arrays.equals( ORIGIN, direction ) ) {
			// location and direction are all zeros when the mouse is released outside
			// the browser window, causing the intersection check to return NaN's for
			// the working point - and the geometry disappears. Therefore - ignore.
			return;
		}
		// Intersect the ray with the geometry to work out where the object
		// virtual geometry has been hit. If the geometry has not been hit
		// then don't generate an event. Just leave it as the last one.
		if(!intersectionUtils.rayCylinder(location,
			                              direction,
			                              intersectionUtils.Y_AXIS,
			                              ORIGIN,
			                              radius,
			                              wkPoint)) {
			return;
		}
		
		double angle = calcAngle(wkPoint[0], wkPoint[2]) - initialRotation;
		
		// add the offset then clamp.
		angle += vfOffset;
		
		// See if we need to clamp. One component at a time
		if(vfMaxAngle >= vfMinAngle) {
			if(angle >= vfMaxAngle)
				angle = vfMaxAngle;
			
			if(angle < vfMinAngle)
				angle = vfMinAngle;
		}
		
		vfRotationChanged[0] = 0;
		vfRotationChanged[1] = 1;
		vfRotationChanged[2] = 0;
		vfRotationChanged[3] = (float)angle;
		
		hasChanged[FIELD_ROTATION_CHANGED] = true;
		fireFieldChanged(FIELD_ROTATION_CHANGED);
		
		if(useDiskCalc) {
			vfTrackPointChanged[0] = wkPoint[0];
			vfTrackPointChanged[1] = 0;
			vfTrackPointChanged[2] = wkPoint[2];
		} else {
			
			float x = wkPoint[0];
			float z = wkPoint[2];
			
			// Normalise the x-z plane vectors then multiply by the radius
			double d = Math.sqrt(x * x + z * z);
			
			if(d != 0) {
				x /= d;
				z /= d;
			}
			
			x *= radius;
			z *= radius;
			
			// Probably should project the location Y onto the local sphere.
			vfTrackPointChanged[0] = x;
			vfTrackPointChanged[1] = wkPoint[1];
			vfTrackPointChanged[2] = z;
		}
		
		hasChanged[FIELD_TRACKPOINT_CHANGED] = true;
		fireFieldChanged(FIELD_TRACKPOINT_CHANGED);
	}
	
	/**
	 * Calculate the current angle given the x and z position.
	 *
	 * @param x The x coordinate value
	 * @param z The z coordinate value
	 * @return The actual angle
	 */
	private double calcAngle(float x, float z) {
	
		double angle = -Math.atan2( z, x );
		return angle;
	}
}
