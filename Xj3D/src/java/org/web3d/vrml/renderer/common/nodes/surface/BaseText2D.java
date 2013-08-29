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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.util.FieldValidator;

/**
 * Common implementation of a Text2D node.
 * <p>
 *
 * The transparency field of the text node follows the standard alpha channel
 * values for 3D colour. A value of 0 means opaque, and a value of 1 means
 * fully transparent. The value only applies to the background color and not
 * the text rendering. The default value is 1.
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public abstract class BaseText2D extends BaseSurfaceChildNode
    implements VRMLSurfaceChildNodeType {

    /** Property describing the text antialiasing */
    private static final String ANTIALIAS_PROP =
        "org.web3d.vrml.nodes.fontstyle.font.antialiased";

    /** Message for when the proto is not a Geometry */
    private static final String FONTSTYLE_PROTO_MSG =
        "Proto does not describe a Text object";

    /** Message for when the node in setValue() is not a Geometry */
    private static final String FONTSTYLE_NODE_MSG =
        "Node does not describe a Text object";

    /** The set, working font size. Set in the static constructor */
    protected static final boolean ANTIALIAS;

    // Field index constants

    /** The field index for fixedSize. */
    protected static final int FIELD_FIXED_SIZE = LAST_SURFACE_CHILD_INDEX + 1;

    /** The field index for texture. */
    protected static final int FIELD_STRING = LAST_SURFACE_CHILD_INDEX + 2;

    /** The field index for isActive. */
    protected static final int FIELD_TEXTCOLOR = LAST_SURFACE_CHILD_INDEX + 3;

    /** The field index for enabled. */
    protected static final int FIELD_BGCOLOR = LAST_SURFACE_CHILD_INDEX + 4;

    /** The field index for isOvery. */
    protected static final int FIELD_TRANSPARENCY = LAST_SURFACE_CHILD_INDEX + 5;

    /** Index of the fontstyle node field */
    protected static final int FIELD_FONTSTYLE = LAST_SURFACE_CHILD_INDEX + 6;


    /** The last field index used by this class */
    protected static final int LAST_TEXT2D_INDEX = FIELD_FONTSTYLE;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TEXT2D_INDEX + 1;


    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // The VRML field values

    /** field SFBool fixedSize */
    protected boolean vfFixedSize;

    /** exposedField SFString string */
    protected String vfString;

    /** exposedField SFColor textColor */
    protected float[] vfTextColor;

    /** exposedField SFColor backgroundColor */
    protected float[] vfBackgroundColor;

    /** exposedField SFFloat transparency */
    protected float vfTransparency;

    /** exposedField SFNode fontStyle NULL */
    protected VRMLFontStyleNodeType vfFontStyle;

    /** The proto version of the fontstyle */
    protected VRMLProtoInstance pFontStyle;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA, FIELD_FONTSTYLE };

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
        fieldDecl[FIELD_STRING] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFString",
                                    "string");
        fieldDecl[FIELD_TEXTCOLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFColor",
                                    "textColor");
        fieldDecl[FIELD_BGCOLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFColor",
                                    "backgroundColor");
        fieldDecl[FIELD_TRANSPARENCY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                    "SFFloat",
                                    "transparency");
        fieldDecl[FIELD_FONTSTYLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "fontStyle");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_BGCOLOR);
        fieldMap.put("backgroundColor", idx);
        fieldMap.put("set_backgroundColor", idx);
        fieldMap.put("backgroundColor_changed", idx);

        idx = new Integer(FIELD_VISIBLE);
        fieldMap.put("visible", idx);
        fieldMap.put("set_visible", idx);
        fieldMap.put("visible_changed", idx);

        idx = new Integer(FIELD_STRING);
        fieldMap.put("string", idx);
        fieldMap.put("set_string", idx);
        fieldMap.put("string_changed", idx);

        idx = new Integer(FIELD_TEXTCOLOR);
        fieldMap.put("textColor", idx);
        fieldMap.put("set_textColor", idx);
        fieldMap.put("textColor_changed", idx);

        idx = new Integer(FIELD_TRANSPARENCY);
        fieldMap.put("transparency", idx);
        fieldMap.put("set_transparency", idx);
        fieldMap.put("transparency_changed", idx);

        idx = new Integer(FIELD_FONTSTYLE);
        fieldMap.put("fontStyle", idx);
        fieldMap.put("set_fontStyle", idx);
        fieldMap.put("fontStyle_changed", idx);

        fieldMap.put("bboxSize", new Integer(FIELD_BBOX_SIZE));
        fieldMap.put("fixedSize", new Integer(FIELD_FIXED_SIZE));

        // fetch the system property defining the values
        Boolean prop = (Boolean)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    // privileged code goes here.
                    return new Boolean(Boolean.getBoolean(ANTIALIAS_PROP));
                }
            }
        );

        ANTIALIAS = prop.booleanValue();
    }

    /**
     * Construct a new default Overlay object
     */
    protected BaseText2D() {
        super("Text2D");

        hasChanged = new boolean[NUM_FIELDS];

        // Set the default values for the fields
        vfFixedSize = true;
        vfTextColor = new float[] { 1, 1, 1 };
        vfBackgroundColor = new float[3];
        vfTransparency = 1;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the same type
     */
    protected BaseText2D(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLSurfaceChildNodeType)node);

        try {
            int index = node.getFieldIndex("fixedSize");
            VRMLFieldData data = node.getFieldValue(index);
            vfFixedSize = data.booleanValue;

            index = node.getFieldIndex("string");
            data = node.getFieldValue(index);
            vfString = data.stringValue;

            index = node.getFieldIndex("textColor");
            data = node.getFieldValue(index);
            vfTextColor[0] = data.floatArrayValue[0];
            vfTextColor[1] = data.floatArrayValue[1];
            vfTextColor[2] = data.floatArrayValue[2];

            index = node.getFieldIndex("backgroundColor");
            data = node.getFieldValue(index);
            vfBackgroundColor[0] = data.floatArrayValue[0];
            vfBackgroundColor[1] = data.floatArrayValue[1];
            vfBackgroundColor[2] = data.floatArrayValue[2];

            index = node.getFieldIndex("transparency");
            data = node.getFieldValue(index);
            vfTransparency = data.floatValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_TEXT2D_INDEX)
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
            case FIELD_FIXED_SIZE:
                fieldData.clear();
                fieldData.booleanValue = vfFixedSize;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FIELD_STRING:
                fieldData.clear();
                fieldData.stringValue = vfString;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_TEXTCOLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfTextColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_BGCOLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfBackgroundColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FIELD_TRANSPARENCY:
                fieldData.clear();
                fieldData.floatValue = vfTransparency;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_FONTSTYLE:
                if(pFontStyle != null)
                    fieldData.nodeValue = pFontStyle;
                else
                    fieldData.nodeValue = vfFontStyle;
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

                case FIELD_STRING:
                    destNode.setValue(destIndex, vfString);
                    break;

                case FIELD_TEXTCOLOR:
                    destNode.setValue(destIndex, vfTextColor, 3);
                    break;

                case FIELD_BGCOLOR:
                    destNode.setValue(destIndex, vfBackgroundColor, 3);
                    break;

                case FIELD_TRANSPARENCY:
                    destNode.setValue(destIndex, vfTransparency);
                    break;

                case FIELD_FONTSTYLE:
                    if(pFontStyle != null)
                        destNode.setValue(destIndex, pFontStyle);
                    else
                        destNode.setValue(destIndex, vfFontStyle);
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
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pFontStyle != null)
            pFontStyle.setupFinished();
        else if(vfFontStyle != null)
            vfFontStyle.setupFinished();
    }

    /**
     * Set the value of the field at the given index as a boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FIXED_SIZE:
                if(!inSetup)
                    throw new InvalidFieldAccessException("fixedSize is initializeOnly");

                vfFixedSize = value;
                updateSurface(FIELD_FIXED_SIZE);
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float. This would
     * be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        if(index != FIELD_TRANSPARENCY) {
            super.setValue(index, value);
            return;
        }

        vfTransparency = value;
        updateSurface(FIELD_TRANSPARENCY);

        if(!inSetup) {
            hasChanged[FIELD_TRANSPARENCY] = true;
            fireFieldChanged(FIELD_TRANSPARENCY);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TEXTCOLOR:
                FieldValidator.checkColorVector("Text2D.textColor",value);
                vfTextColor[0] = value[0];
                vfTextColor[1] = value[1];
                vfTextColor[2] = value[2];
                break;

            case FIELD_BGCOLOR:
                FieldValidator.checkColorVector("Text2D.backgroundColor",value);
                vfBackgroundColor[0] = value[0];
                vfBackgroundColor[1] = value[1];
                vfBackgroundColor[2] = value[2];
                break;

            default:
                super.setValue(index, value, numValid);
        }

        if(!inSetup) {
            updateSurface(index);

            hasChanged[index] = true;
            fireFieldChanged(index);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        if(index != FIELD_STRING) {
            super.setValue(index, value);
            return;
        }

        vfString = value;
        updateSurface(FIELD_STRING);

        if(!inSetup) {
            hasChanged[FIELD_STRING] = true;
            fireFieldChanged(FIELD_STRING);
        }
    }

    /**
     * Set the value of the field at the given index as a node. This would be
     * used to set SFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FONTSTYLE:
                setFontStyle(child);
                break;

            default:
                super.setValue(index, child);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Convenience method to set a node child for the fontstyle.
     *
     * @param newFont the new node to set
     * @throws InvalidFieldValueException The node passed in is not a
     *    fontstyle or fontstyle proto.
     */
    public void setFontStyle(VRMLNodeType newFont)
        throws InvalidFieldValueException {

        VRMLFontStyleNodeType node;

        if(newFont instanceof VRMLProtoInstance) {
            node = (VRMLFontStyleNodeType)
                ((VRMLProtoInstance)newFont).getImplementationNode();
            pFontStyle = (VRMLProtoInstance)newFont;
            int[] type = newFont.getSecondaryType();

            boolean type_found = false;

            for(int i = 0; i < type.length && !type_found; i++) {
                if(type[i] == TypeConstants.FontStyleNodeType)
                    type_found = true;
            }

            if(!type_found)
                throw new InvalidFieldValueException(FONTSTYLE_PROTO_MSG);

        } else if (newFont != null &&
            (!(newFont instanceof VRMLFontStyleNodeType))) {
            throw new InvalidFieldValueException(FONTSTYLE_NODE_MSG);
        } else {
            pFontStyle = null;
            node = (VRMLFontStyleNodeType)newFont;
        }

        vfFontStyle = node;

        if(!inSetup) {
            hasChanged[FIELD_FONTSTYLE] = true;
            fireFieldChanged(FIELD_FONTSTYLE);
        }
    }

    /**
     * Get the currently set fontstyle. If none is set, null is returned.
     *
     * @return The current fontstyle information
     */
    public VRMLNodeType getFontStyle() {
        if(pFontStyle != null)
            return pFontStyle;
        else
            return vfFontStyle;
    }

    /**
     * Notification by the base class to the derived class that the node should
     * be updated because one of the fields changed. The index is the field
     * index of the value that changed.
     *
     * @param index The index of the field that changed
     */
    protected void updateSurface(int index) {
    }
}
