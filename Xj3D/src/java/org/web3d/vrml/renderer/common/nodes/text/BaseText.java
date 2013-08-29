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

package org.web3d.vrml.renderer.common.nodes.text;

// External imports
import java.awt.*;

import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Common base implementation of a Text node.
 * <p>
 *
 * This base class provides a number of convenience methods for generating the
 * visible text. It is not required that the derived implementation make use of
 * them, but it assumes that you know when you create the class whether you will
 * need them or not.
 * <p>
 *
 * Text generation is provided by a texture that is an alpha mask over the stock
 * texture and geometry. VRML requires that a text object be fully textured
 * using the normal appearance node information, so this is just used as an alpha
 * mask to control what geometry is actually visible.
 * <p>
 *
 * The texture generation does not do the full rendering of the text. It
 * assumes that the derived class will also contribute to the process by
 * playing with texture coordinates. The following fields from the FontStyle
 * require the derived class to do something.
 *
 * <ul>
 * <li><i>leftToRight</i>: If set to FALSE, then reverse the texture
 *     coordinates to render in the mirror image.
 * </li>
 * </ul>
 *
 * The image generated is drawn from the bottom of the image upwards, rather
 * that from the top (0, 0 in image coordinates). This is so that texture
 * coordinates look sane and you can take the right part of the image as
 * needed without any extra calculation.
 *
 * <p>
 * <b>Properties</b>
 * <p>
 * The following properties are used by this class
 * <ul>
 * <li><code>org.web3d.vrml.nodes.fontstyle.font.antialiased</code> Boolean
 *     to indicate the rendering should use antialiased text rather than
 *     normal rendering. The default is false.
 * </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.26 $
 */
public abstract class BaseText extends AbstractNode
    implements VRMLTextNodeType {

    /** Property describing the text antialiasing */
    private static final String ANTIALIAS_PROP =
        "org.web3d.vrml.nodes.fontstyle.font.antialiased";

    /** Message for when the proto is not a Geometry */
    private static final String FONTSTYLE_PROTO_MSG =
        "Proto does not describe a Text object";

    /** Message for when the node in setValue() is not a Geometry */
    private static final String FONTSTYLE_NODE_MSG =
        "Node does not describe a Text object";

    /** Full transparent colour for the background of the texture. */
    private static final Color CLEAR_COLOR = Color.black;

    /** Colour of the text. */
    private static final Color TEXT_COLOR = Color.white;

    /** The set, working font size. Set in the static constructor */
    private static final boolean ANTIALIAS;


    /** Index of the string field */
    protected static final int FIELD_STRING = LAST_NODE_INDEX + 1;

    /** Index of the fontstyle node field */
    protected static final int FIELD_FONTSTYLE = LAST_NODE_INDEX + 2;

    /** Index of the length field */
    protected static final int FIELD_LENGTH = LAST_NODE_INDEX + 3;

    /** Index of the maxExtents field */
    protected static final int FIELD_MAXEXTENT = LAST_NODE_INDEX + 4;

    /** Index of the solid field */
    protected static final int FIELD_SOLID = LAST_NODE_INDEX + 5;

    /** The last field index used by this class */
    protected static final int LAST_TEXT_INDEX = FIELD_SOLID;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_TEXT_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** exposedField MFString string [] */
    protected String[] vfString;

    /** Number of valid values in vfString */
    protected int numString;

    /** exposedField SFNode fontStyle NULL */
    protected VRMLFontStyleNodeType vfFontStyle;

    /** The proto version of the fontstyle */
    protected VRMLProtoInstance pFontStyle;

    /** exposedField MFFloat length [] */
    protected float[] vfLength;

    /** Number of valid values in vfLength */
    protected int numLength;

    /** exposedField SFFloat maxExtent */
    protected float vfMaxExtent;

    /** field SFBool solid */
    protected boolean vfSolid;

    /** Flag to say if we need to generate images textures */
    private final boolean needsTexture;

    /** The image that we will write to next. If static, never set */
    private BufferedImage currentImage;

    /** The texture currently being displayed */
    protected BufferedImage texturedImage;

    /** The width of the texture image */
    protected int imgWidth;

    /** The height of the texture image */
    protected int imgHeight;

    /** The number of pixels actually used in the width of the texture. */
    protected int usedPixelWidth;

    /** The number of pixels actually used in the height of the texture. */
    protected int usedPixelHeight;

    /** The number of pixels a single line of text takes up on the image */
    protected int linePixelHeight;

    /**
     * The number of pixels of spacing between the bottom of one line and the
     * top of the next. Based on the bounding box and therefore the line
     * height.
     */
    protected int linePixelSpacing;

    /** Same as vfString but maybe reversed in order if topToBottom false */
    private String[] orderedText;

    /**
     * The layouts matching the appropriate string in the order list depending
     * on the fontstyle topToBottom field.
     */
    protected TextLayout[] layouts;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_FONTSTYLE, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS * 3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_STRING] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "string");
        fieldDecl[FIELD_FONTSTYLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "fontStyle");
        fieldDecl[FIELD_LENGTH] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "length");
        fieldDecl[FIELD_MAXEXTENT] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "maxExtent");
        fieldDecl[FIELD_SOLID] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "solid");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_STRING);
        fieldMap.put("string", idx);
        fieldMap.put("set_string", idx);
        fieldMap.put("string_changed", idx);

        idx = new Integer(FIELD_FONTSTYLE);
        fieldMap.put("fontStyle", idx);
        fieldMap.put("set_fontStyle", idx);
        fieldMap.put("fontStyle_changed", idx);

        idx = new Integer(FIELD_LENGTH);
        fieldMap.put("length", idx);
        fieldMap.put("set_length", idx);
        fieldMap.put("length_changed", idx);

        idx = new Integer(FIELD_MAXEXTENT);
        fieldMap.put("maxExtent", idx);
        fieldMap.put("set_maxExtent", idx);
        fieldMap.put("maxExtent_changed", idx);

        fieldMap.put("solid", new Integer(FIELD_SOLID));

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
     * Construct a new default instance of this class.
     *
     * @param useTexture True if the derived class wants textures generated
     */
    protected BaseText(boolean useTexture) {
        super("Text");

        hasChanged = new boolean[LAST_TEXT_INDEX + 1];

        needsTexture = useTexture;

        vfSolid = true;
        vfString = FieldConstants.EMPTY_MFSTRING;
        vfLength = FieldConstants.EMPTY_MFFLOAT;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a light node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @param useTexture True if the derived class wants textures generated
     * @throws IllegalArgumentException Incorrect Node Type
     */
    protected BaseText(VRMLNodeType node, boolean useTexture) {
        this(useTexture);

        checkNodeType(node);

        vfSolid = ((VRMLGeometryNodeType)node).isSolid();

        try {
            int index = node.getFieldIndex("string");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfString = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfString,
                                 0,
                                 field.numElements);
            }

            numString = field.numElements;

            index = node.getFieldIndex("length");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfLength = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,
                                 0,
                                 vfLength,
                                 0,
                                 field.numElements);
                numLength = field.numElements;
            }

            index = node.getFieldIndex("maxExtent");
            field = node.getFieldValue(index);
            vfMaxExtent = field.floatValue;
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }


    //----------------------------------------------------------
    // Methods defined by VRMLGeometryNodeType
    //----------------------------------------------------------

    /**
     * Specified whether this node has color information.  If so, then it
     * will be used for diffuse terms instead of materials.
     *
     * @return true Use local color information for diffuse lighting.
     */
    public boolean hasLocalColors() {
        return false;
    }

    /**
     * Specified whether this node has alpha values in the local colour
     * information. If so, then it will be used for to override the material's
     * transparency value.
     *
     * @return true when the local color value has inbuilt alpha
     */
    public boolean hasLocalColorAlpha() {
        return false;
    }

    /**
     * Add a listener for local color changes.  Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Remove a listener for local color changes.  Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeLocalColorsListener(LocalColorsListener l) {
    }

    /**
     * Add a listener for texture coordinate generation mode changes.
     * Nulls and duplicates will be ignored.
     *
     * @param l The listener.
     */
    public void addTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Remove a listener for texture coordinate generation mode changes.
     * Nulls will be ignored.
     *
     * @param l The listener.
     */
    public void removeTexCoordGenModeChanged(TexCoordGenModeListener l) {
    }

    /**
     * Get the texture coordinate generation mode.  NULL is returned
     * if the texture coordinates are not generated.
     *
     * @param setNum The set which this tex gen mode refers
     * @return The mode or NULL
     */
    public String getTexCoordGenMode(int setNum) {
        return null;
    }

    /**
     * Get the number of texture coordinate sets contained by this node
     *
     * @return the number of texture coordinate sets
     */
    public int getNumSets() {
        return 0;
    }

    /**
     * Specifies whether a geometry object is a solid opject.
     * If true, then back-face culling can be performed
     *
     * @return The current value of solid
     */
    public boolean isSolid() {
        return vfSolid;
    }

    /**
     * Get the value of the CCW field. If the node does not have one, this will
     * return true.
     *
     * @return true if the vertices are CCW ordered
     */
    public boolean isCCW() {
        return true;
    }

    /**
     * Specifies whether this node requires lighting.
     *
     * @return Should lighting be enabled
     */
    public boolean isLightingEnabled() {
        return true;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLTextNodeType
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
     * Convenience method to set the text information. May be overridden by
     * the derived class, but should call this method first to ensure the
     * field values are properly set.
     *
     * @param str The string(s) to set
     */
    public void setText(String[] str) {
        if(str == null)
            numString = 0;
        else {
            if(vfString == null || vfString.length < str.length)
                vfString = new String[str.length];

            numString = str.length;
            System.arraycopy(str, 0, vfString, 0, numString);
        }

        if (!inSetup)
            updateTexture();

        hasChanged[FIELD_STRING] = true;
        fireFieldChanged(FIELD_STRING);
    }

    /**
     * Get the current text being rendered. If no text is available, returns
     * null.
     *
     * @return The text being rendered
     */
    public String[] getText() {

        String[] ret_val;

        if(numString != vfString.length) {
            ret_val = new String[numString];
            System.arraycopy(vfString, 0, ret_val, 0, numString);
        } else
            ret_val = vfString;

        return ret_val;
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

        if(pFontStyle != null)
            pFontStyle.setupFinished();
        else if(vfFontStyle != null)
            vfFontStyle.setupFinished();

        if(needsTexture) {
            // find the size of the text and create the appropriate textures
            // from there.
/***************/

            currentImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
            texturedImage = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
            imgWidth = 1;
            imgHeight = 1;

            updateTexture();
        }
    }

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
        if(index < 0  || index > LAST_TEXT_INDEX)
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.GeometryNodeType;
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

        fieldData.clear();

        switch(index) {
            case FIELD_STRING:
                fieldData.stringArrayValue = vfString;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = numString;
                break;

            case FIELD_FONTSTYLE:
                if(pFontStyle != null)
                    fieldData.nodeValue = pFontStyle;
                else
                    fieldData.nodeValue = vfFontStyle;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_LENGTH:
                fieldData.floatArrayValue = vfLength;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements =
                    (vfLength == null) ? 0 : vfLength.length;
                break;

            case FIELD_MAXEXTENT:
                fieldData.floatValue = vfMaxExtent;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_SOLID:
                if(vrmlMajorVersion < 3)
                    throw new InvalidFieldException("Field solid not defined for VRML97");

                fieldData.booleanValue = vfSolid;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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
                case FIELD_STRING:
                    destNode.setValue(destIndex, vfString, numString);
                    break;

                case FIELD_FONTSTYLE:
                    if(pFontStyle != null)
                        destNode.setValue(destIndex, pFontStyle);
                    else
                        destNode.setValue(destIndex, vfFontStyle);
                    break;

                case FIELD_LENGTH:
                    destNode.setValue(destIndex, vfLength, numLength);
                    break;

                case FIELD_MAXEXTENT:
                    destNode.setValue(destIndex, vfMaxExtent);
                    break;
            }
        } catch(InvalidFieldException ife) {
            System.err.println("Text sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("Text sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
     * be used to set SFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_SOLID:
                if(vrmlMajorVersion < 3)
                    throw new InvalidFieldException("Field solid not defined for VRML97");

                if(!inSetup)
                    throw new InvalidFieldValueException(INIT_ONLY_WRITE_MSG + " solid");

                vfSolid = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a float.
     * This would be used to set SFFloat field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_MAXEXTENT:
                vfMaxExtent = value;
                if(!inSetup) {
                    hasChanged[FIELD_MAXEXTENT] = true;
                    fireFieldChanged(FIELD_MAXEXTENT);
                }
                break;

            default:
                super.setValue(index,value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFColor and SFVec3f field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is not in range
     *    or not appropriate for this field
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_LENGTH:
                if(numValid > vfLength.length)
                    vfLength = new float[numValid];

                System.arraycopy(value, 0, vfLength, 0, numValid);
                numLength = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_LENGTH] = true;
                    fireFieldChanged(FIELD_LENGTH);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The raw value string to be parsed
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_STRING:
                setText(new String[] { value });
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field types.
     *
     * @param index The index of destination field to set
     * @param value The strings to be rendered
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_STRING:
                setText(value);
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
     * Notification by the base class to the derived class that the texture
     * representing the text has been changed. The derived should now change
     * their texture representation.
     *
     * @param sizeChanged true if the underlying texture size changed
     */
    protected void textUpdated(boolean sizeChanged) {
    }

    /**
     * Update the textures used by this node. At the end it will call the
     * notification method textUpdated() so the derived class can update its
     * texture setup.
     */
    private void updateTexture() {
        // No point going any further. However, if someone sets the new
        // string to zero from a previously valid length, we should clear
        // the current texture. Not implemented yet.
        if(numString == 0)
// TODO: Stop text for now
//        if(numString >= 0)
            return;

        if (!needsTexture)
            return;

        Graphics2D g = currentImage.createGraphics();
        FontRenderContext frc = g.getFontRenderContext();

        VRMLFontStyleNodeType font_style =
            (vfFontStyle == null) ?
            DefaultFontStyle.getDefaultFontStyle() :
            vfFontStyle;

        Font font = font_style.getFont();
        float spacing = font_style.getSpacing();
        int i;

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           ANTIALIAS ?
                           RenderingHints.VALUE_ANTIALIAS_ON :
                           RenderingHints.VALUE_ANTIALIAS_OFF);

        layouts = new TextLayout[vfString.length];
        Rectangle2D char_bounds = font.getMaxCharBounds(frc);

        // Calculate the first line separately because there's no spacing
        // required and it sets up the variables for later use.
        boolean top_to_bottom = (vfFontStyle == null) || vfFontStyle.isTopToBottom();

        if(top_to_bottom) {
            if(vfString[0].length() == 0)
                layouts[0] = new TextLayout(" ", font, frc);
            else
                layouts[0] = new TextLayout(vfString[0], font, frc);
        } else {
            if(vfString[0].length() == 0)
                layouts[0] = new TextLayout(" ", font, frc);
            else
                layouts[0] = new TextLayout(vfString[numString - 1], font, frc);
        }

        Rectangle2D bounds = layouts[0].getBounds();

        int char_height = (int)char_bounds.getHeight();
        int max_len = (int)bounds.getWidth();
        int total_height = (int)(char_height * numString +
                           (char_height * spacing * (numString - 1)));

        if(top_to_bottom) {
            for(i = 1; i < numString; i++) {
                if(vfString[i].length() == 0) {
                    layouts[i] = new TextLayout(" ", font, frc);
                } else {
                    layouts[i] = new TextLayout(vfString[i], font, frc);

                    bounds = layouts[i].getBounds();
                    double w = bounds.getWidth();

                    if(w > max_len)
                        max_len = (int)w;
                }

            }
        } else {
            for(i = 1; i < numString; i++) {
                if(vfString[numString - i - 1].length() == 0)
                    layouts[i] = new TextLayout(" ", font, frc);
                else {
                    layouts[i] = new TextLayout(vfString[numString - i - 1], font, frc);
                    bounds = layouts[i].getBounds();
                    double w = bounds.getWidth();

                    if(w > max_len)
                        max_len = (int)w;
                }
            }
        }

        usedPixelHeight = total_height;
        usedPixelWidth = max_len;
        linePixelHeight = char_height;
        linePixelSpacing = (int)(char_height * spacing);

        // so now we have our max size of the string in texture coords. Let's
        // create the minimum size texture width

if (max_len > 1024) {
    System.out.println("Clamping len to 1024, was: " + max_len);
    max_len = 512;
}
if (total_height > 1024) {
System.out.println("Clamping height to 512 was: " + total_height);
    total_height = 512;
}
if (max_len < 0)
    max_len = 1;
if (total_height < 0)
    total_height = 1;

        // These are problematic since they lose the scaling of the data
        //int total_width = smallestPower(max_len);
        //boolean image_changed = false;

        // Calculate how big the texture must be to accomodat the size of
        // of the text.
        int texture_width = smallestPower(max_len);
        int texture_height = smallestPower(total_height);

        boolean image_changed = false;

        if((imgWidth < texture_width) || (imgHeight < total_height)) {
            total_height = smallestPower(total_height);
            currentImage = new BufferedImage(texture_width,
                                             texture_height,
                                             BufferedImage.TYPE_BYTE_GRAY);

            imgWidth = texture_width;
            imgHeight = texture_height;
            image_changed = true;

            // Update the settings
            g = currentImage.createGraphics();
        }

        // clear the background to transparent again
        g.setColor(CLEAR_COLOR);
        g.fillRect(0, 0, imgWidth, imgHeight);
        g.setColor(TEXT_COLOR);

        float start_height;
        float start_x;

        // Add the height of the last item? to make sure you get the
        // full offset work with the first line separate from the others
        // then Calculate the spacing value in pixels between the bottom
        // of the previous box and the top of this box.

        switch(font_style.getHorizontalJustification()) {

            case VRMLFontStyleNodeType.BEGIN_JUSTIFY:
            case VRMLFontStyleNodeType.FIRST_JUSTIFY:
                bounds = layouts[numString - 1].getBounds();
                // The start_height for a single line of text to be positioned
                // as ParallelGrpahics does is
                // 44, but its being calculated as 64,
                // and the start_x is too small
                start_height = imgHeight; // - layouts[numString - 1].getDescent();

                layouts[numString - 1].draw(g,
                                            0 /*(float)bounds.getX()*/,
                                            start_height);
                for(i = numString - 2; i >= 0; i--) {

                    start_height = imgHeight - char_height * spacing * (numString - i - 1);
                    start_height -= layouts[i].getDescent();

                    bounds = layouts[i].getBounds();
                    layouts[i].draw(g, (float)bounds.getX(), start_height);
                }

                break;

            case VRMLFontStyleNodeType.MIDDLE_JUSTIFY:
                bounds = layouts[numString - 1].getBounds();

                start_height = imgHeight - layouts[numString - 1].getDescent();
                start_x = (usedPixelWidth >> 1) - ((float)bounds.getWidth() / 2);

                layouts[numString - 1].draw(g, start_x, start_height);

                for(i = numString - 2; i >= 0; i--) {
                    start_height = imgHeight - char_height * spacing * (numString - i - 1);
                    start_height -= layouts[i].getDescent();

                    bounds = layouts[i].getBounds();

                    start_x = (usedPixelWidth >> 1) - ((float)bounds.getWidth() / 2);
                    layouts[i].draw(g, start_x, start_height);
                }

                break;

            case VRMLFontStyleNodeType.END_JUSTIFY:

                bounds = layouts[numString - 1].getBounds();
                start_x = usedPixelWidth - (float)bounds.getWidth();
                start_height = imgHeight - layouts[numString - 1].getDescent();

                layouts[numString - 1].draw(g, start_x, start_height);

                for(i = numString - 2; i >= 0; i--) {

                    start_height = imgHeight - char_height * spacing * (numString - i - 1);
                    start_height -= layouts[i].getDescent();

                    bounds = layouts[i].getBounds();

                    start_x = usedPixelWidth - (float)bounds.getWidth();
                    layouts[i].draw(g, start_x, start_height);
                }

                break;
        }

        g.dispose();

        BufferedImage tmp = currentImage;
        currentImage = texturedImage;
        texturedImage = tmp;

        textUpdated(image_changed);
    }

    /**
     * Return the smallest power of 2 greater than value provided
     *
     * @param value The value to find the nearest power to
     * @return The closest power of 2
     */
    private int smallestPower(int value) {
        int n = 1;
        while (n < value)
            n <<= 1;

        return n;
    }
}
