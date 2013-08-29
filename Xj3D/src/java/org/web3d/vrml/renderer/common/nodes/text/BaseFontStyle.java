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

package org.web3d.vrml.renderer.common.nodes.text;

// Standard imports
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.TextAttribute;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.lang.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLFontStyleNodeType;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;

/**
 * Scene graph representation of a font style information.
 * <p>
 *
 * The fontstyle is a fixed, read-only node. Once created, you cannot change
 * the fields. Thus, all field setting code will throw an exception. Because
 * this class is fixed, once the Text node has fetched the font setup
 * information, this class could be thrown away.
 *
 * <b>Properties</b>
 * <p>
 * The following properties are used by this class
 * <ul>
 * <li><code>org.web3d.vrml.nodes.fontstyle.font.size</code> The font size in
 *     points. The default value is 36 point font.
 * </li>
 * </ul>
 *
 * @author Justin Couch
 * @version $Revision: 1.19 $
 */
public abstract class BaseFontStyle extends AbstractNode
    implements VRMLFontStyleNodeType {

    /** Error message for whn the justify major type is invalid */
    private static final String MAJOR_JUSTIFY_ERR_MSG =
        "Invalid justify enumerant value for the major type: ";

    /** Error message for whn the justify minor type is invalid */
    private static final String MINOR_JUSTIFY_ERR_MSG =
        "Invalid justify enumerant value for the minor type: ";

    /** Property describing the text font size to use */
    private static final String FONT_SIZE_PROP =
        "org.web3d.vrml.nodes.fontstyle.font.size";

    /** The default font size if none provided */
    private static final int DEFAULT_FONT_SIZE = 36;

    /** The set, working font size. Set in the static constructor */
    private static final int FONT_SIZE;

    /** Basic font styles as strings */
    private static final String DEFAULT_FAMILY = "SERIF";

    /** Name of the system font we want to use for SERIF style */
    private static final String SERIF_FONT = "Serif";

    /** Name of the system font we want to use for SERIF style */
    private static final String SANS_FONT = "SansSerif";

    /** Name of the system font we want to use for SERIF style */
    private static final String TYPEWRITER_FONT = "Monospaced";


    // Field index decls

    /** Index of the Family field */
    protected static final int FIELD_FAMILY = LAST_NODE_INDEX + 1;

    /** Index of the Horiztonal field */
    protected static final int FIELD_HORIZONTAL = LAST_NODE_INDEX + 2;

    /** Index of the justify field */
    protected static final int FIELD_JUSTIFY = LAST_NODE_INDEX + 3;

    /** Index of the language field */
    protected static final int FIELD_LANGUAGE = LAST_NODE_INDEX + 4;

    /** Index of the leftToRight field */
    protected static final int FIELD_LEFTTORIGHT = LAST_NODE_INDEX + 5;

    /** Index of the size field */
    protected static final int FIELD_SIZE = LAST_NODE_INDEX + 6;

    /** Index of the spacing field */
    protected static final int FIELD_SPACING = LAST_NODE_INDEX + 7;

    /** Index of the style field */
    protected static final int FIELD_STYLE = LAST_NODE_INDEX + 8;

    /** Index of the topToBottom field */
    protected static final int FIELD_TOPTOBOTTOM = LAST_NODE_INDEX + 9;

    /** The last field index used by this class */
    protected static final int LAST_FONTSTYLE_INDEX = FIELD_TOPTOBOTTOM;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = FIELD_TOPTOBOTTOM + 1;

    // Global shared vars for lookups and stuff

    /** Array of VRMLFieldDeclarations */
    protected static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    /** Mapping of the justify field string to the internal int type */
    protected static HashMap justifyMap;

    /** Mapping of the style field string to the internal int type */
    protected static HashMap styleMap;

    /** Mapping of the family field string to the internal Font name type */
    protected static HashMap fontMap;

    /**
     * The set of all availble font family names. Used for fast lookups when
     * creating new instances of this class.
     */
    private static HashSet systemFontFamilies;

    // Ordinary class vars

    /** The value of the font field. */
    private String[] vfFamily;

    /** The value of the converted font family field. */
    private String selectedFamilyFont;

    /** The value of the horizontal field */
    private boolean vfHorizontal;

    /** The value of the justify field */
    private String[] vfJustify;

    /** The internal value of the horizontal justify field */
    private int horizontalJustification;

    /** The internal value of the vertical justify field */
    private int verticalJustification;

    /** The value of the language field */
    private String vfLanguage;

    /** The value of the leftToRight field */
    private boolean vfLeftToRight;

    /** The value of the size field */
    private float vfSize;

    /** The value of the spacing field */
    private float vfSpacing;

    /** The value of the style field */
    private String vfStyle;

    /** The value of the topToBottom field */
    private boolean vfTopToBottom;

    /** The real font in use */
    private Font awtFont;

    /**
     * Static constructor to build the field representations of this node
     * once for all users.
     */
    static {
        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_FAMILY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "family");

        fieldDecl[FIELD_HORIZONTAL] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "horizontal");

        fieldDecl[FIELD_JUSTIFY] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "justify");

        fieldDecl[FIELD_LANGUAGE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "language");

        fieldDecl[FIELD_LEFTTORIGHT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "leftToRight");

        fieldDecl[FIELD_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "size");

        fieldDecl[FIELD_SPACING] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "spacing");

        fieldDecl[FIELD_STYLE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFString",
                                     "style");

        fieldDecl[FIELD_TOPTOBOTTOM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "topToBottom");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        fieldMap.put("family", new Integer(FIELD_FAMILY));
        fieldMap.put("horizontal", new Integer(FIELD_HORIZONTAL));
        fieldMap.put("justify", new Integer(FIELD_JUSTIFY));
        fieldMap.put("language", new Integer(FIELD_LANGUAGE));
        fieldMap.put("leftToRight", new Integer(FIELD_LEFTTORIGHT));
        fieldMap.put("size", new Integer(FIELD_SIZE));
        fieldMap.put("spacing", new Integer(FIELD_SPACING));
        fieldMap.put("style", new Integer(FIELD_STYLE));
        fieldMap.put("topToBottom", new Integer(FIELD_TOPTOBOTTOM));

        justifyMap = new HashMap();
        justifyMap.put("BEGIN", new Integer(BEGIN_JUSTIFY));
        justifyMap.put("END", new Integer(END_JUSTIFY));
        justifyMap.put("MIDDLE", new Integer(MIDDLE_JUSTIFY));
        justifyMap.put("FIRST", new Integer(FIRST_JUSTIFY));

        styleMap = new HashMap();
        styleMap.put("BOLD", new Integer(BOLD_STYLE));
        styleMap.put("ITALIC", new Integer(ITALIC_STYLE));
        styleMap.put("PLAIN", new Integer(PLAIN_STYLE));
        styleMap.put("BOLDITALIC", new Integer(BOLDITALIC_STYLE));

        fontMap = new HashMap();
        fontMap.put("SERIF", SERIF_FONT);
        fontMap.put("SANS", SANS_FONT);
        fontMap.put("TYPEWRITER", TYPEWRITER_FONT);

        systemFontFamilies = new HashSet();

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] all_fonts = env.getAvailableFontFamilyNames();

        for(int i = 0; i < all_fonts.length; i++)
            systemFontFamilies.add(all_fonts[i]);

        // fetch the system property defining the values
        Integer prop = (Integer)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    // privileged code goes here.
                    return Integer.getInteger(FONT_SIZE_PROP,
                                              DEFAULT_FONT_SIZE);
                }
            }
        );

        int size = prop.intValue();
        if(size <= 0)
            size = DEFAULT_FONT_SIZE;

        FONT_SIZE = size;
    }

    /**
     * Construct a default instance of the script
     */
    public BaseFontStyle() {
        super("FontStyle");
        isDEF = false;
        inSetup = true;

        String fam = (String)fontMap.get(DEFAULT_FAMILY);

        vfFamily = new String[] { DEFAULT_FAMILY };
        selectedFamilyFont = fam;
        vfHorizontal = true;
        vfJustify = new String[] { "BEGIN", null };
        vfLeftToRight = true;
        vfSize = 1;
        vfSpacing = 1;
        vfStyle = "PLAIN";
        vfTopToBottom = true;

        verticalJustification = BEGIN_JUSTIFY;
        horizontalJustification = BEGIN_JUSTIFY;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseFontStyle(VRMLNodeType node) {

        this();

        checkNodeType(node);

        try {
            VRMLFieldData field = node.getFieldValue(FIELD_FAMILY);

            // Because this class never gets modified, we can just do a
            // straight assignment of the values rather than copying them
            if(field.numElements != 0) {
                String[] tmp = field.stringArrayValue;
                if(tmp.length != vfFamily.length) {
                    vfFamily = new String[tmp.length];
                }

                for(int i = 0; i < field.numElements; i++) {
                    vfFamily[i] = tmp[i];
                }
            }

            field = node.getFieldValue(FIELD_HORIZONTAL);
            vfHorizontal = field.booleanValue;

            field = node.getFieldValue(FIELD_JUSTIFY);

            vfJustify[0] = field.stringArrayValue[0];
            vfJustify[1] = field.stringArrayValue[1];

            field = node.getFieldValue(FIELD_LEFTTORIGHT);
            vfLeftToRight = field.booleanValue;

            field = node.getFieldValue(FIELD_SIZE);
            vfSize = field.floatValue;

            field = node.getFieldValue(FIELD_SPACING);
            vfSpacing = field.floatValue;

            field = node.getFieldValue(FIELD_STYLE);
            vfStyle = field.stringValue;

            field = node.getFieldValue(FIELD_LANGUAGE);
            vfLanguage = field.stringValue;

            field = node.getFieldValue(FIELD_TOPTOBOTTOM);
            vfTopToBottom = field.booleanValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
    //----------------------------------------------------------

    /**
     * Fetch the AWT font description that matches the internal field settings.
     *
     * @return The font that is based on the fields
     */
    public Font getFont() {
        return awtFont;
    }

    /**
     * Get the horizontal justification flag. Uses the constants defined in
     * this interface. This is the real justification after it has been
     * modified according to the effects of the horizontal field
     *
     * @return The justfication value
     */
    public int getHorizontalJustification() {
        return horizontalJustification;
    }

    /**
     * Get the vertical justification flag. Uses the constants defined in
     * this interface. This is the real justification after it has been
     * modified according to the effects of the horizontal field
     *
     * @return The justfication value
     */
    public int getVerticalJustification() {
        return verticalJustification;
    }

    /**
     * Get the spacing defintion for the lines of text.
     *
     * @return The font spacing information
     */
    public float getSpacing() {
        return vfSpacing;
    }

    /**
     * Get the size information for a single line of text. Font size is
     * already incorporated into the AWT Font information, but may also be
     * needed for the inter-line spacing.
     *
     * @return The font size information
     */
    public float getSize() {
        return vfSize;
    }

    /**
     * Get the value of the topToBottom flag. Returns true if the text strings
     * should be rendered from the top first, false for bottom first.
     *
     * @return true if rendering top to bottom
     */
    public boolean isTopToBottom() {
        return vfTopToBottom;
    }

    /**
     * Get the value of the leftToRight flag. Returns true if the text strings
     * should be rendered from the left side to the right or in reverse -
     * regardless of the original character encoding.
     *
     * @return true if rendering left to right
     */
    public boolean isLeftToRight() {
        return vfLeftToRight;
    }

    //----------------------------------------------------------
    // Methods required by the VRMLNodeType interface.
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

        for(int i = 0; i < vfFamily.length; i++) {
            if(systemFontFamilies.contains(vfFamily[i])) {
                selectedFamilyFont = vfFamily[i];
                break;
            } else if(fontMap.containsKey(vfFamily[i])) {
                selectedFamilyFont = (String)fontMap.get(vfFamily[i]);
                break;
            }

            // anything else we ignore and keep looking for the first match
        }

        // Build the appropriate FONT instance
        // Not sure yet, but should be able to use the Justification attribute
        Map font_attr = new HashMap();
        font_attr.put(TextAttribute.FAMILY, selectedFamilyFont);
        font_attr.put(TextAttribute.SIZE, new Float(vfSize * FONT_SIZE));
        font_attr.put(TextAttribute.BACKGROUND, new Color(0, 0, 0, 1));

        if(vfLeftToRight)
            font_attr.put(TextAttribute.RUN_DIRECTION,
                          TextAttribute.RUN_DIRECTION_LTR);
        else
            font_attr.put(TextAttribute.RUN_DIRECTION,
                          TextAttribute.RUN_DIRECTION_RTL);

        Float posture = TextAttribute.POSTURE_REGULAR;
        Float weight = TextAttribute.WEIGHT_REGULAR;

        Integer style = (Integer)styleMap.get(vfStyle);

        if (style != null) {
            switch(style.intValue()) {
                case PLAIN_STYLE:
                    // already set by default
                    break;

                case BOLD_STYLE:
                    posture = TextAttribute.POSTURE_REGULAR;
                    weight = TextAttribute.WEIGHT_BOLD;
                    break;

                case ITALIC_STYLE:
                    posture = TextAttribute.POSTURE_OBLIQUE;
                    weight = TextAttribute.WEIGHT_REGULAR;
                    break;

                case BOLDITALIC_STYLE:
                    posture = TextAttribute.POSTURE_OBLIQUE;
                    weight = TextAttribute.WEIGHT_BOLD;
                    break;

                default:
                    // Style type is unknown, so just ignore. Probably should
                    // issue an error message here.
            }
        }

        font_attr.put(TextAttribute.POSTURE, posture);
        font_attr.put(TextAttribute.WEIGHT, weight);

        awtFont = new Font(font_attr);

        Integer tmp;

        if(vfHorizontal) {
            if(vfJustify[0] != null) {
                tmp = (Integer)justifyMap.get(vfJustify[0]);
                horizontalJustification = tmp.intValue();
            }

            if(vfJustify[1] != null) {
                tmp = (Integer)justifyMap.get(vfJustify[1]);
                verticalJustification = tmp.intValue();
            }
        } else {
            if(vfJustify[1] != null) {
                tmp = (Integer)justifyMap.get(vfJustify[1]);
                horizontalJustification = tmp.intValue();
            }

            if(vfJustify[0] != null) {
                tmp = (Integer)justifyMap.get(vfJustify[0]);
                verticalJustification = tmp.intValue();
            }
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
        if(index < 0  || index > LAST_FONTSTYLE_INDEX)
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
     * return the instance directly. An eventOut does not have field data
     * available. This is neither an exceptional condition, nor something that
     * should return valid data. Therefore the method returns null if you ask
     * for the value of an eventOut.
     *
     * @param index The index of the field to change.
     * @return The class representing the field value or null for eventOuts
     * @throws InvalidFieldException The field index is not known
     */
    public VRMLFieldData getFieldValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        switch(index) {
            case FIELD_FAMILY:
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.stringArrayValue = vfFamily;
                fieldData.numElements = vfFamily.length;
                break;

            case FIELD_HORIZONTAL:
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfHorizontal;
                break;

            case FIELD_JUSTIFY:
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.stringArrayValue = vfJustify;
                fieldData.numElements = vfJustify.length;
                break;

            case FIELD_LEFTTORIGHT:
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfLeftToRight;
                break;

            case FIELD_SIZE:
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.floatValue = vfSize;
                break;

            case FIELD_SPACING:
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.floatValue = vfSpacing;
                break;

            case FIELD_STYLE:
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.stringValue = vfStyle;
                break;

            case FIELD_LANGUAGE:
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                fieldData.stringValue = vfLanguage;
                break;

            case FIELD_TOPTOBOTTOM:
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfTopToBottom;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.FontStyleNodeType;
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

        switch(index) {
            case FIELD_STYLE:
                if (!inSetup)
                    throw new InvalidFieldAccessException("style",this);

                vfStyle = value;
                break;
            case FIELD_LANGUAGE:
                if (!inSetup)
                    throw new InvalidFieldAccessException("language",this);

                vfLanguage = value;
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
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_FAMILY:
                if (!inSetup)
                    throw new InvalidFieldAccessException("family",this);

                vfFamily = value;
                break;

            case FIELD_JUSTIFY:
                if (!inSetup)
                    throw new InvalidFieldAccessException("justify",this);

                setJustify(value);
                break;

            default:
                super.setValue(index, value, numValid);
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

        switch(index) {
            case FIELD_SIZE:
                if (!inSetup)
                    throw new InvalidFieldAccessException("size",this);

                vfSize = value;
                break;

            case FIELD_SPACING:
                if (!inSetup)
                    throw new InvalidFieldAccessException("spacing",this);

                vfSpacing = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an boolean. This would
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
            case FIELD_LEFTTORIGHT:
                if (!inSetup)
                    throw new InvalidFieldAccessException("leftToRight",this);

                vfLeftToRight = value;
                break;

            case FIELD_HORIZONTAL:
                if (!inSetup)
                    throw new InvalidFieldAccessException("horizontal",this);

                vfLeftToRight = value;
                break;

            case FIELD_TOPTOBOTTOM:
                if (!inSetup)
                    throw new InvalidFieldAccessException("topToBottom",this);

                vfTopToBottom = value;
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the justify field.
     *
     * @param The justify params
     */
    private void setJustify(String[] val)
        throws InvalidFieldValueException {

        switch(val.length) {
            case 0:
                vfJustify[0] = null;
                vfJustify[1] = null;
                break;
            case 1:
                if(justifyMap.get(val[0]) == null)
                    throw new InvalidFieldValueException(
                        MAJOR_JUSTIFY_ERR_MSG + val[0]);

                vfJustify[0] = val[0];
                vfJustify[1] = null;
                break;
            default:
                if(justifyMap.get(val[0]) == null)
                    throw new InvalidFieldValueException(
                        MAJOR_JUSTIFY_ERR_MSG + val[0]);

                if(justifyMap.get(val[1]) == null)
                    throw new InvalidFieldValueException(
                        MINOR_JUSTIFY_ERR_MSG + val[1]);

                vfJustify[0] = val[0];
                vfJustify[1] = val[1];
                break;
        }
    }
}
