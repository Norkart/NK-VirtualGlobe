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

package org.web3d.vrml.renderer.common.nodes.surface;

// Standard imports
import java.awt.Rectangle;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;


/**
 * Common implementation of a Image2D node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.12 $
 */
public abstract class BaseImage2D extends BaseSurfaceChildNode
    implements VRMLSurfaceChildNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.SensorNodeType, TypeConstants.TimeDependentNodeType };

    // Field index constants

    /** The field index for fixedSize. */
    protected static final int FIELD_FIXED_SIZE = LAST_SURFACE_CHILD_INDEX + 1;

    /** The field index for texture. */
    protected static final int FIELD_TEXTURE = LAST_SURFACE_CHILD_INDEX + 2;

    /** The field index for isActive. */
    protected static final int FIELD_ISACTIVE = LAST_SURFACE_CHILD_INDEX + 3;

    /** The field index for enabled. */
    protected static final int FIELD_ENABLED = LAST_SURFACE_CHILD_INDEX + 4;

    /** The field index for isOvery. */
    protected static final int FIELD_ISOVER = LAST_SURFACE_CHILD_INDEX + 5;

    /** The field index for touchTime. */
    protected static final int FIELD_TOUCHTIME = LAST_SURFACE_CHILD_INDEX + 6;

    /** The field index for trackPoint_changed. */
    protected static final int FIELD_TRACKPOINT_CHANGED =
        LAST_SURFACE_CHILD_INDEX + 7;

    /** The field index for windowRelative. */
    protected static final int FIELD_WINDOW_RELATIVE =
        LAST_SURFACE_CHILD_INDEX + 8;


    /** The last field index used by this class */
    protected static final int LAST_IMAGE2D_INDEX = FIELD_WINDOW_RELATIVE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_IMAGE2D_INDEX + 1;

    /** Message for when the proto is not a Appearance */
    protected static final String TEXTURE_PROTO_MSG =
        "Proto does not describe a Texture2D object";

    /** Message for when the node in setValue() is not a Appearance */
    protected static final String TEXTURE_NODE_MSG =
        "Node does not describe a Texture2D object";



    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** The value of the fixedSize field. */
    protected boolean vfFixedSize;

    /** The value of the texture exposedField. */
    protected VRMLTexture2DNodeType vfTexture;

    /** Proto version of the texture */
    protected VRMLProtoInstance pTexture;

    /** The value of the isActive eventOut */
    protected boolean vfIsActive;

    /** The value of the windowRelative field */
    protected boolean vfWindowRelative;

    /** The value of the enabled exposedField */
    protected boolean vfEnabled;

    /** The value of the isOver eventOut */
    protected boolean vfIsOver;

    /** The value of the touchTime eventOut */
    protected double vfTouchTime;

    /** The value of th trackPoint_changed eventOut */
    protected float[] vfTrackPoint;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_TEXTURE, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_VISIBLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFBool",
                                    "visible");
        fieldDecl[FIELD_BBOX_SIZE] =
           new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFVec2f",
                                    "bboxSize");
        fieldDecl[FIELD_FIXED_SIZE] =
           new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFBool",
                                    "fixedSize");
        fieldDecl[FIELD_WINDOW_RELATIVE] =
           new VRMLFieldDeclaration(FieldConstants.FIELD,
                                    "SFBool",
                                    "windowRelative");
        fieldDecl[FIELD_TEXTURE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFNode",
                                    "texture");
        fieldDecl[FIELD_ENABLED] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFBool",
                                    "enabled");
        fieldDecl[FIELD_ISACTIVE] =
           new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                    "SFBool",
                                    "isActive");
        fieldDecl[FIELD_ISOVER] =
           new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                    "SFBool",
                                    "isOver");
        fieldDecl[FIELD_TOUCHTIME] =
           new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                    "SFTime",
                                    "touchTime");
        fieldDecl[FIELD_TRACKPOINT_CHANGED] =
           new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                    "SFVec2f",
                                    "trackPoint_changed");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_VISIBLE);
        fieldMap.put("visible", idx);
        fieldMap.put("set_visible", idx);
        fieldMap.put("visible_changed", idx);

        idx = new Integer(FIELD_TEXTURE);
        fieldMap.put("texture", idx);
        fieldMap.put("set_texture", idx);
        fieldMap.put("texture_changed", idx);

        idx = new Integer(FIELD_ENABLED);
        fieldMap.put("enabled", idx);
        fieldMap.put("set_enabled", idx);
        fieldMap.put("enabled_changed", idx);

        fieldMap.put("bboxSize", new Integer(FIELD_BBOX_SIZE));
        fieldMap.put("fixedSize", new Integer(FIELD_FIXED_SIZE));
        fieldMap.put("windowRelative", new Integer(FIELD_WINDOW_RELATIVE));
        fieldMap.put("isActive", new Integer(FIELD_ISACTIVE));
        fieldMap.put("isOver", new Integer(FIELD_ISOVER));
        fieldMap.put("touchTime", new Integer(FIELD_TOUCHTIME));
        fieldMap.put("trackPoint_changed",
                     new Integer(FIELD_TRACKPOINT_CHANGED));
    }

    /**
     * Construct a new default Overlay object
     */
    protected BaseImage2D() {
        super("Image2D");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfFixedSize = true;
        vfIsActive = false;
        vfEnabled = true;
        vfIsOver = false;
        vfTouchTime = 0;
        vfWindowRelative = false;
        vfTrackPoint = new float[2];
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseImage2D(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSurfaceChildNodeType)node);

        try {
            int index = node.getFieldIndex("fixedSize");
            VRMLFieldData data = node.getFieldValue(index);
            vfFixedSize = data.booleanValue;

            index = node.getFieldIndex("enabled");
            data = node.getFieldValue(index);
            vfEnabled = data.booleanValue;

            index = node.getFieldIndex("windowRelative");
            data = node.getFieldValue(index);
            vfWindowRelative = data.booleanValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLSensorNodeType interface.
    //----------------------------------------------------------

    /**
     * Accessor method to set a new value for the enabled field
     *
     * @param state The new enabled state
     */
    public void setEnabled(boolean state) {
        if(state != vfEnabled) {
            vfEnabled = state;
            hasChanged[FIELD_ENABLED] = true;
            fireFieldChanged(FIELD_ENABLED);
        }
    }

    /**
     * Accessor method to get current value of the enabled field.
     * The default value is <code>true</code>.
     *
     * @return The value of the enabled field
     */
    public boolean getEnabled() {
        return vfEnabled;
    }

    /**
     * Accessor method to get current value of the isActive field.
     *
     * @return The current value of isActive
     */
    public boolean getIsActive () {
        return vfIsActive;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if (pTexture != null)
            pTexture.setupFinished();
        if (vfTexture != null)
            vfTexture.setupFinished();
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
        if(index < 0  || index > LAST_IMAGE2D_INDEX)
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
     * Get the secondary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The secondary type
     */
    public int[] getSecondaryType() {
        return SECONDARY_TYPE;
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
            case FIELD_FIXED_SIZE:
                fieldData.clear();
                fieldData.booleanValue = vfFixedSize;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_WINDOW_RELATIVE:
                fieldData.clear();
                fieldData.booleanValue = vfWindowRelative;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ENABLED:
                fieldData.clear();
                fieldData.booleanValue = vfEnabled;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ISACTIVE:
                fieldData.clear();
                fieldData.booleanValue = vfIsActive;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_ISOVER:
                fieldData.clear();
                fieldData.booleanValue = vfIsOver;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_TOUCHTIME:
                fieldData.clear();
                fieldData.doubleValue = vfTouchTime;
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FIELD_TRACKPOINT_CHANGED:
                fieldData.clear();
                fieldData.floatArrayValue = vfTrackPoint;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_TEXTURE:
                fieldData.clear();
                if(pTexture == null)
                    fieldData.nodeValue = vfTexture;
                else
                    fieldData.nodeValue = pTexture;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
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
                case FIELD_FIXED_SIZE:
                    destNode.setValue(destIndex, vfFixedSize);
                    break;

                case FIELD_WINDOW_RELATIVE:
                    destNode.setValue(destIndex, vfWindowRelative);
                    break;

                case FIELD_ENABLED:
                    destNode.setValue(destIndex, vfEnabled);
                    break;

                case FIELD_ISACTIVE:
                    destNode.setValue(destIndex, vfIsActive);
                    break;

                case FIELD_ISOVER:
                    destNode.setValue(destIndex, vfIsOver);
                    break;

                case FIELD_TOUCHTIME:
                    destNode.setValue(destIndex, vfTouchTime);
                    break;

                case FIELD_TRACKPOINT_CHANGED:
                    destNode.setValue(destIndex, vfTrackPoint, 2);
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
     * Set the value of the field from the raw string. This requires the
     * implementation to parse the string in the given format for the field
     * type. If the field type does not match the requirements for that index
     * then an exception will be thrown. If the destination field is a string,
     * then the leading and trailing quote characters will be stripped before
     * calling this method.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @throws InvalidFieldFormatException The string was not in a correct form
     *    for this field.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldFormatException, InvalidFieldValueException,
               InvalidFieldException {

        switch(index) {
            case FIELD_FIXED_SIZE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "fixedSize is an initializeOnly field");

                vfFixedSize = value;
                break;

            case FIELD_WINDOW_RELATIVE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(
                        "windowRelative is an initializeOnly field");

                vfWindowRelative = value;
                break;

            case FIELD_ENABLED:
                vfEnabled = value;
                if(!inSetup) {
                    hasChanged[FIELD_ENABLED] = true;
                    fireFieldChanged(FIELD_ENABLED);
                }

                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_TEXTURE:
                setTextureNode(child);

                if(!inSetup) {
                    hasChanged[FIELD_TEXTURE] = true;
                    fireFieldChanged(FIELD_TEXTURE);
                }
                break;

            default:
                super.setValue(index, child);
        }

    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Called to set the texture node to be used. May be overridden by the
     * derived class, but must also call this version first to ensure
     * everything is valid node types and the fields correctly set.
     *
     * @param texture The new texture node instance to use
     * @throws InvalidFieldValueException The node is not the required type
     */
    protected void setTextureNode(VRMLNodeType texture)
        throws InvalidFieldValueException {

        if(texture == null) {
            vfTexture = null;
        } else {

            VRMLNodeType node;

            if(texture instanceof VRMLProtoInstance) {
                pTexture = (VRMLProtoInstance)texture;
                node = pTexture.getImplementationNode();
                if(!(node instanceof VRMLTexture2DNodeType)) {
                    throw new InvalidFieldValueException(TEXTURE_PROTO_MSG);
                }
            } else if(texture != null &&
                (!(texture instanceof VRMLTexture2DNodeType))) {
                throw new InvalidFieldValueException(TEXTURE_NODE_MSG);
            } else {
                pTexture = null;
                node = texture;
            }

            vfTexture = (VRMLTexture2DNodeType)node;
        }

        if (!inSetup) {
            hasChanged[FIELD_TEXTURE] = true;
            fireFieldChanged(FIELD_TEXTURE);
        }
    }

    /**
     * Send notification that the track point has changed. The values passed
     * should be in surface coordinates and this will adjust as necessary for
     * the windowRelative field setting when generating the eventOut. Assumes
     * standard window coordinates with X across and Y down.
     *
     * @param x The x position of the mouse
     * @param h The y position of the mouse
     */
    protected void setTrackPoint(int x, int y) {

        if(!vfWindowRelative) {
            vfTrackPoint[0] = x;
            vfTrackPoint[1] = y;
        } else {
            vfTrackPoint[0] = x - screenLocation[0];
            vfTrackPoint[1] = y - screenLocation[1];
        }

        hasChanged[FIELD_TRACKPOINT_CHANGED] = true;
        fireFieldChanged(FIELD_TRACKPOINT_CHANGED);
    }

}
