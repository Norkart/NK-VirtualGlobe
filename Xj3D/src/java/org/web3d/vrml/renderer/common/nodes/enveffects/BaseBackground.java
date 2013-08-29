/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.enveffects;

// External imports
import java.util.ArrayList;
import java.util.HashMap;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.HashSet;

import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseBindableNode;

import org.web3d.vrml.util.URLChecker;
import org.web3d.vrml.util.FieldValidator;

/**
 * Common base implementation of a Background node.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.22 $
 */
public abstract class BaseBackground extends BaseBindableNode
    implements VRMLBackgroundNodeType, VRMLMultiExternalNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.BindableNodeType,
        TypeConstants.MultiExternalNodeType
    };

    /** Index of the groundAngle field */
    protected static final int FIELD_GROUND_ANGLE = LAST_BINDABLE_INDEX + 1;

    /** Index of the groundColor field */
    protected static final int FIELD_GROUND_COLOR = LAST_BINDABLE_INDEX + 2;

    /** Index of the skyAngle field */
    protected static final int FIELD_SKY_ANGLE = LAST_BINDABLE_INDEX + 3;

    /** Index of the skyColor field */
    protected static final int FIELD_SKY_COLOR = LAST_BINDABLE_INDEX + 4;

    /** Index of the backUrl field */
    protected static final int FIELD_BACK_URL = LAST_BINDABLE_INDEX + 5;

    /** Index of the frontUrl field */
    protected static final int FIELD_FRONT_URL = LAST_BINDABLE_INDEX + 6;

    /** Index of the leftUrl field */
    protected static final int FIELD_LEFT_URL = LAST_BINDABLE_INDEX + 7;

    /** Index of the rightUrl field */
    protected static final int FIELD_RIGHT_URL = LAST_BINDABLE_INDEX + 8;

    /** Index of the bottomUrl field */
    protected static final int FIELD_BOTTOM_URL = LAST_BINDABLE_INDEX + 9;

    /** Index of the topUrl field */
    protected static final int FIELD_TOP_URL = LAST_BINDABLE_INDEX + 10;

    /** Index of the topUrl field */
    protected static final int FIELD_TRANSPARENCY = LAST_BINDABLE_INDEX + 11;

    // Local working constants

    /** The last field index used by this class */
    protected static final int LAST_BACKGROUND_INDEX = FIELD_TRANSPARENCY;

    /** The number of fields implemented */
    protected static final int NUM_FIELDS = LAST_BACKGROUND_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;


    /** An empty list of URL fields for initialisation */
    protected static final String[] EMPTY_LIST = {};

    // Side constants for readability when generating the background box.
    // Also used to control the shown SwitchGroup as children
    protected static final int BACK   = 0;
    protected static final int FRONT  = 1;
    protected static final int LEFT   = 2;
    protected static final int RIGHT  = 3;
    protected static final int TOP    = 4;
    protected static final int BOTTOM = 5;
    protected static final int SKY_SPHERE = 6;
    protected static final int GROUND_SPHERE = 7;
    protected static final int NUM_BG_OBJECTS = 8;

    // Common for all instances

    /** The array of fields that need URL content */
    protected static final int[] urlFieldIndexList;

    /** The class types that we want for our images to load */
    private static final Class[] requiredImageTypes;

    // Field declarations

    /** The world URL for correcting relative URL values */
    protected String worldURL;

    /** Flag to indicate if we've checked the URLs for relative references */
    protected boolean urlRelativeCheck;

    /** The state of the load for the various fields */
    protected int[] loadState;

    /** List of loaded URI strings */
    protected String[] loadedUri;

    /** MFString backUrl list */
    protected String[] vfBackUrl;

    /** MFString frontUrl list */
    protected String[] vfFrontUrl;

    /** MFString leftUrl list */
    protected String[] vfLeftUrl;

    /** MFString rightUrl list */
    protected String[] vfRightUrl;

    /** MFString topUrl list */
    protected String[] vfTopUrl;

    /** MFString bottomUrl list */
    protected String[] vfBottomUrl;

    /** MFFloat groundAngle */
    protected float[] vfGroundAngle;

    /** MFColor groundColor */
    protected float[] vfGroundColor;

    /** MFFloat skyAngle */
    protected float[] vfSkyAngle;

    /** MFColor skyColor */
    protected float[] vfSkyColor;

    /** SFFloat transparency */
    protected float vfTransparency;

    /** Number of valid values in vfGroundAngle */
    protected int numGroundAngle;

    /** Number of valid values in vfGroundColor */
    protected int numGroundColor;

    /** Number of valid values in vfSkyAngle */
    protected int numSkyAngle;

    /** Number of valid values in vfSkyColor */
    protected int numSkyColor;

    /** Flag indicating a spec version threshold */
    protected boolean isVersionPost_3_2;
    
    /** List of those who want to know about Url changes. Likely 1 */
    private ArrayList urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private ArrayList contentListeners;

    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_BIND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "SFBool",
                                     "set_bind");
        fieldDecl[FIELD_IS_BOUND] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFBool",
                                     "isBound");
        fieldDecl[FIELD_BIND_TIME] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFTime",
                                     "bindTime");
        fieldDecl[FIELD_GROUND_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "groundAngle");
        fieldDecl[FIELD_GROUND_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFColor",
                                     "groundColor");
        fieldDecl[FIELD_SKY_ANGLE] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFFloat",
                                     "skyAngle");
        fieldDecl[FIELD_SKY_COLOR] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFColor",
                                     "skyColor");
        fieldDecl[FIELD_BACK_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "backUrl");
        fieldDecl[FIELD_FRONT_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "frontUrl");
        fieldDecl[FIELD_LEFT_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "leftUrl");
        fieldDecl[FIELD_RIGHT_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "rightUrl");
        fieldDecl[FIELD_TOP_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "topUrl");
        fieldDecl[FIELD_BOTTOM_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "bottomUrl");
        fieldDecl[FIELD_TRANSPARENCY] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFFloat",
                                     "transparency");

        fieldMap.put("set_bind",new Integer(FIELD_BIND));
        fieldMap.put("isBound",new Integer(FIELD_IS_BOUND));

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_BIND_TIME);
        fieldMap.put("bindTime", idx);
        fieldMap.put("bindTime_changed", idx);

        idx = new Integer(FIELD_BACK_URL);
        fieldMap.put("backUrl", idx);
        fieldMap.put("set_backUrl", idx);
        fieldMap.put("backUrl_changed", idx);

        idx = new Integer(FIELD_FRONT_URL);
        fieldMap.put("frontUrl", idx);
        fieldMap.put("set_frontUrl", idx);
        fieldMap.put("frontUrl_changed", idx);

        idx = new Integer(FIELD_LEFT_URL);
        fieldMap.put("leftUrl", idx);
        fieldMap.put("set_leftUrl", idx);
        fieldMap.put("leftUrl_changed", idx);

        idx = new Integer(FIELD_RIGHT_URL);
        fieldMap.put("rightUrl", idx);
        fieldMap.put("set_rightUrl", idx);
        fieldMap.put("rightUrl_changed", idx);

        idx = new Integer(FIELD_TOP_URL);
        fieldMap.put("topUrl", idx);
        fieldMap.put("set_topUrl", idx);
        fieldMap.put("topUrl_changed", idx);

        idx = new Integer(FIELD_BOTTOM_URL);
        fieldMap.put("bottomUrl", idx);
        fieldMap.put("set_bottomUrl", idx);
        fieldMap.put("bottomUrl_changed", idx);

        idx = new Integer(FIELD_GROUND_ANGLE);
        fieldMap.put("groundAngle", idx);
        fieldMap.put("set_groundAngle", idx);
        fieldMap.put("groundAngle_changed", idx);

        idx = new Integer(FIELD_GROUND_COLOR);
        fieldMap.put("groundColor", idx);
        fieldMap.put("set_groundColor", idx);
        fieldMap.put("groundColor_changed", idx);

        idx = new Integer(FIELD_SKY_ANGLE);
        fieldMap.put("skyAngle", idx);
        fieldMap.put("set_skyAngle", idx);
        fieldMap.put("skyAngle_changed", idx);

        idx = new Integer(FIELD_SKY_COLOR);
        fieldMap.put("skyColor", idx);
        fieldMap.put("set_skyColor", idx);
        fieldMap.put("skyColor_changed", idx);

        idx = new Integer(FIELD_TRANSPARENCY);
        fieldMap.put("transparency", idx);
        fieldMap.put("set_transparency", idx);
        fieldMap.put("transparency_changed", idx);
        
        urlFieldIndexList = new int[6];
        urlFieldIndexList[0] = FIELD_BACK_URL;
        urlFieldIndexList[1] = FIELD_FRONT_URL;
        urlFieldIndexList[2] = FIELD_LEFT_URL;
        urlFieldIndexList[3] = FIELD_RIGHT_URL;
        urlFieldIndexList[4] = FIELD_TOP_URL;
        urlFieldIndexList[5] = FIELD_BOTTOM_URL;

        requiredImageTypes = null;
    }

    /**
     * Create a new, default instance of this class.
     */
    protected BaseBackground() {
        super("Background");

        urlListeners = new ArrayList(1);
        contentListeners = new ArrayList(1);

        hasChanged = new boolean[NUM_FIELDS];
        loadState = new int[NUM_FIELDS];
        loadedUri = new String[NUM_FIELDS];

        vfBackUrl = FieldConstants.EMPTY_MFSTRING;
        vfFrontUrl = FieldConstants.EMPTY_MFSTRING;
        vfLeftUrl = FieldConstants.EMPTY_MFSTRING;
        vfRightUrl = FieldConstants.EMPTY_MFSTRING;
        vfTopUrl = FieldConstants.EMPTY_MFSTRING;
        vfBottomUrl = FieldConstants.EMPTY_MFSTRING;

        vfSkyColor = new float[] {0, 0, 0};

        vfGroundAngle = FieldConstants.EMPTY_MFFLOAT;
        vfGroundColor = FieldConstants.EMPTY_MFFLOAT;
        vfSkyAngle = FieldConstants.EMPTY_MFFLOAT;
        vfTransparency = 0;
        
        numGroundAngle = 0;
        numGroundColor = 0;
        numSkyAngle = 0;
        numSkyColor = 3;

        urlRelativeCheck = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node.
     * <P>
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not the right type.
     */
    protected BaseBackground(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index;
            VRMLFieldData field;

            index = node.getFieldIndex("transparency");
            field = node.getFieldValue(index);

            vfTransparency = field.floatValue;

            index = node.getFieldIndex("backUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfBackUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfBackUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("frontUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfFrontUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfFrontUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("leftUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfLeftUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfLeftUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("rightUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfRightUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfRightUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("topUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfTopUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfTopUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("bottomUrl");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfBottomUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfBottomUrl, 0,
                    field.numElements);
            }

            index = node.getFieldIndex("groundAngle");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfGroundAngle = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,0,vfGroundAngle,0,
                    field.numElements);

                numGroundAngle = field.numElements;
            }

            index = node.getFieldIndex("groundColor");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfGroundColor = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValue,0,vfGroundColor,0,
                    field.numElements * 3);

                numGroundColor = field.numElements * 3;
            }

            index = node.getFieldIndex("skyAngle");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSkyAngle = new float[field.numElements];
                System.arraycopy(field.floatArrayValue,0,vfSkyAngle,0,
                    field.numElements);

                numSkyAngle = field.numElements;
            }

            index = node.getFieldIndex("skyColor");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfSkyColor = new float[field.numElements * 3];
                System.arraycopy(field.floatArrayValue,0,vfSkyColor,0,
                    field.numElements * 3);

                numSkyColor = field.numElements * 3;
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLBackgroundNodeType
    //----------------------------------------------------------

    /**
     * Get the transparency of the background.
     */
    public float getTransparency() {
        return( vfTransparency );
    }

    /**
     * Set the transparency of the background.
     *
     * @param val The transparency value
     */
    public void setTransparency(float val) {
        vfTransparency = val;

        if(!inSetup) {
            hasChanged[FIELD_TRANSPARENCY] = true;
            fireFieldChanged(FIELD_TRANSPARENCY);
        }
    }

    /**
     * Get the number of valid sky color values that are currently defined.
     *
     * @return The number of values
     */
    public int getNumSkyColors() {
        return numSkyColor / 3;
    }

    /**
     * Get the number of valid ground color values that are currently defined.
     *
     * @return The number of values
     */
    public int getNumGroundColors() {
        return numGroundColor / 3;
    }

    /**
     * Fetch the color and angles for the sky values. Assumes that the sky
     * color size is at least 1.
     *
     * @param color The array to return the color values in
     * @param angle The array to return the angle values in
     */
    public void getSkyValues(float[] color, float[] angle) {
        System.arraycopy(vfSkyColor, 0, color, 0, numSkyColor);

        if(numSkyAngle != 0)
            System.arraycopy(vfSkyAngle, 0, angle, 0, numSkyAngle);
    }

    /**
     * Fetch the color and angles for the ground values. Assumes that the ground
     * color size is at least 1.
     *
     * @param color The array to return the color values in
     * @param angle The array to return the angle values in
     */
    public void getGroundValues(float[] color, float[] angle) {
        System.arraycopy(vfGroundColor, 0, color, 0, numGroundColor);

        if(numGroundAngle != 0)
            System.arraycopy(vfGroundAngle, 0, angle, 0, numGroundAngle);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLMultiExternalNodeType
    //----------------------------------------------------------

    /**
     * Get the list of field index values that require external content.
     * These will be used to query for the URL values later.
     *
     * @return A list of field indexes requiring textures
     */
    public int[] getUrlFieldIndexes() {
        return urlFieldIndexList;
    }

    /**
     * Get the list of URI's currently registered with this node for the given
     * field index.
     *
     * @param index The field index we want the URLs for
     * @return The list of URLs at that index
     * @throws InvalidFieldException The index does not match something
     *   with URLs.
     */
    public String[] getUrl(int index) throws InvalidFieldException {

        String[] ret_val = null;

        switch(index) {
            case FIELD_BACK_URL:
                ret_val = vfBackUrl;
                break;

            case FIELD_FRONT_URL:
                ret_val = vfFrontUrl;
                break;

            case FIELD_LEFT_URL:
                ret_val = vfLeftUrl;
                break;

            case FIELD_RIGHT_URL:
                ret_val = vfRightUrl;
                break;

            case FIELD_TOP_URL:
                ret_val = vfTopUrl;
                break;

            case FIELD_BOTTOM_URL:
                ret_val = vfBottomUrl;
                break;

            default:
                throw new InvalidFieldException("getURL invalid index");
        }

        return ret_val;
    }


    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    public int getLoadState(int index) {
        return loadState[index];
    }

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    public void setLoadState(int index, int state) {
        loadState[index] = state;

        fireContentStateChanged(index);
    }

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node. As all external types are images, we
     * always report the same type here.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(int index, String mimetype) {
        // Only accept content that is images.
        return mimetype.startsWith("image/");
    }

    /**
     * Get the list of preferred content class types in order of preference.
     * As all types are images, this always returns the image class types.
     *
     * @param index THe field index for the prefered types.
     * @return A list of prefered class types
     * @throws InvalidFieldException The field index is not valid for the query
     */
    public Class[] getPreferredClassTypes(int index)
        throws InvalidFieldException {

        return requiredImageTypes;
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param fieldIdx The field index that was loaded
     * @param uri The URI used to load this content
     */
    public void setLoadedURI(int fieldIdx, String uri) {
        loadedUri[fieldIdx] = uri;
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param ul The listener instance to add
     */
    public void addUrlListener(VRMLUrlListener ul) {
        if(!urlListeners.contains(ul))
            urlListeners.add(ul);
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param ul The listener to be removed
     */
    public void removeUrlListener(VRMLUrlListener ul) {
        urlListeners.remove(ul);
    }

    /**
     * Add a listener to this node instance for the content state changes. If
     * the listener is already added or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addContentStateListener(VRMLContentStateListener l) {
        if(!contentListeners.contains(l))
            contentListeners.add(l);
    }

    /**
     * Remove a listener from this node instance for the content state changes.
     * If the listener is null or not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeContentStateListener(VRMLContentStateListener l) {
        contentListeners.remove(l);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExternalNodeType
    //----------------------------------------------------------

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    public void setWorldUrl(String url) {

        if((url == null) || (url.length() == 0))
            return;

        // check for a trailing slash. If it doesn't have one, append it.
        if(url.charAt(url.length() - 1) != '/') {
            worldURL = url + '/';
        } else {
            worldURL = url;
        }

        checkURLs();
    }

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    public String getWorldUrl() {
        return worldURL;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
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
    public void setVersion( int major, int minor, boolean isStatic ) {
        super.setVersion( major, minor, isStatic );
        
        isVersionPost_3_2 = 
            ( vrmlMajorVersion > 3 ) | (( vrmlMajorVersion == 3 ) && ( vrmlMinorVersion >= 2 ));
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
        checkURLs();
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
        int idxValue = (index == null) ? -1 : index.intValue();
        if ( idxValue == FIELD_TRANSPARENCY ) {
            // the transparency field was added to the background
            // node as of spec version 3.2
            if ( !isVersionPost_3_2 ) {
                // profess ignorance of this field if an earlier
                // version of the spec is in use
                idxValue = -1;
            }
        } 
        return( idxValue );
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
        if (index < 0  || index > LAST_BACKGROUND_INDEX)
            return null;

        return fieldDecl[index];
    }

    /**
     * Get the number of fields.
     *
     * @param The number of fields.
     */
    public int getNumFields() {
        int numFields = fieldDecl.length;
        if ( !isVersionPost_3_2 ) {
            // adjust to account for the transparency field NOT
            // being present prior to version 3.2 of the spec
            numFields--;
        }
        return( numFields );
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.BackgroundNodeType;
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
            case FIELD_BACK_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfBackUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfBackUrl.length;
                break;

            case FIELD_FRONT_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfFrontUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfFrontUrl.length;
                break;

            case FIELD_TOP_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfTopUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfTopUrl.length;
                break;

            case FIELD_BOTTOM_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfBottomUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfBottomUrl.length;
                break;

            case FIELD_LEFT_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfLeftUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfLeftUrl.length;
                break;

            case FIELD_RIGHT_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfRightUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfRightUrl.length;
                break;

            case FIELD_SKY_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfSkyColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numSkyColor / 3;
                break;

            case FIELD_SKY_ANGLE:
                fieldData.clear();
                fieldData.floatArrayValue = vfSkyAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numSkyAngle;
                break;

            case FIELD_GROUND_COLOR:
                fieldData.clear();
                fieldData.floatArrayValue = vfGroundColor;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numGroundColor / 3;
                break;

            case FIELD_GROUND_ANGLE:
                fieldData.clear();
                fieldData.floatArrayValue = vfGroundAngle;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = numGroundAngle;
                break;

            case FIELD_TRANSPARENCY:
                fieldData.clear();
                fieldData.floatValue = vfTransparency;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                fieldData.numElements = 1;
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
                case FIELD_BACK_URL:
                    destNode.setValue(destIndex, vfBackUrl, vfBackUrl.length);
                    break;

                case FIELD_FRONT_URL:
                    destNode.setValue(destIndex, vfFrontUrl, vfFrontUrl.length);
                    break;

                case FIELD_LEFT_URL:
                    destNode.setValue(destIndex, vfLeftUrl, vfLeftUrl.length);
                    break;

                case FIELD_RIGHT_URL:
                    destNode.setValue(destIndex, vfRightUrl, vfRightUrl.length);
                    break;

                case FIELD_TOP_URL:
                    destNode.setValue(destIndex, vfTopUrl, vfTopUrl.length);
                    break;

                case FIELD_BOTTOM_URL:
                    destNode.setValue(destIndex, vfBottomUrl, vfBottomUrl.length);
                    break;

                case FIELD_GROUND_ANGLE:
                    destNode.setValue(destIndex, vfGroundAngle, numGroundAngle);
                    break;

                case FIELD_GROUND_COLOR:
                    destNode.setValue(destIndex, vfGroundColor, numGroundColor);
                    break;

                case FIELD_SKY_ANGLE:
                    destNode.setValue(destIndex, vfSkyAngle, numSkyAngle);
                    break;

                case FIELD_SKY_COLOR:
                    destNode.setValue(destIndex, vfSkyColor, numSkyColor);
                    break;

                case FIELD_TRANSPARENCY:
                    destNode.setValue(destIndex, vfTransparency);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFFloat fields.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_TRANSPARENCY:
               setTransparency(value);
               break;

            default:
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFVec3f field types bboxCenter and bboxSize.
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
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_GROUND_ANGLE:
                if(numValid > vfGroundAngle.length)
                    vfGroundAngle = new float[numValid];

                System.arraycopy(value, 0, vfGroundAngle, 0, numValid);
                numGroundAngle = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_GROUND_ANGLE] = true;
                    fireFieldChanged(FIELD_GROUND_ANGLE);
                }
                break;

            case FIELD_GROUND_COLOR:
                FieldValidator.checkColorArray("Background.GroundColor", value);

                if(numValid > vfGroundColor.length)
                    vfGroundColor = new float[numValid];

                System.arraycopy(value, 0, vfGroundColor, 0, numValid);
                numGroundColor = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_GROUND_COLOR] = true;
                    fireFieldChanged(FIELD_GROUND_COLOR);
                }
                break;

            case FIELD_SKY_ANGLE:
                if(value.length > vfSkyAngle.length)
                    vfSkyAngle = new float[numValid];

                System.arraycopy(value, 0, vfSkyAngle, 0, numValid);
                numSkyAngle = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_SKY_ANGLE] = true;
                    fireFieldChanged(FIELD_SKY_ANGLE);
                }
                break;

            case FIELD_SKY_COLOR:
                FieldValidator.checkColorArray("Background.SkyColor", value);
                if(numValid > vfSkyColor.length)
                    vfSkyColor = new float[numValid];

                System.arraycopy(value, 0, vfSkyColor, 0, numValid);
                numSkyColor = numValid;

                if(!inSetup) {
                    hasChanged[FIELD_SKY_COLOR] = true;
                    fireFieldChanged(FIELD_SKY_COLOR);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set MFString field type url.
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
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BACK_URL:
                if(vfBackUrl.length != numValid)
                    vfBackUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfBackUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_BACK_URL] = true;
                    fireFieldChanged(FIELD_BACK_URL);
                }
                break;

            case FIELD_FRONT_URL:
                if(vfFrontUrl.length != numValid)
                    vfFrontUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfFrontUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_FRONT_URL] = true;
                    fireFieldChanged(FIELD_FRONT_URL);
                }
                break;

            case FIELD_LEFT_URL:
                if(vfLeftUrl.length != numValid)
                    vfLeftUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfLeftUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_LEFT_URL] = true;
                    fireFieldChanged(FIELD_LEFT_URL);
                }
                break;

            case FIELD_RIGHT_URL:
                if(vfRightUrl.length != numValid)
                    vfRightUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfRightUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_RIGHT_URL] = true;
                    fireFieldChanged(FIELD_RIGHT_URL);
                }
                break;

            case FIELD_TOP_URL:
                if(vfTopUrl.length != numValid)
                    vfTopUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfTopUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_TOP_URL] = true;
                    fireFieldChanged(FIELD_TOP_URL);
                }
                break;

            case FIELD_BOTTOM_URL:
                if(vfBottomUrl.length != numValid)
                    vfBottomUrl = new String[numValid];

                if(numValid != 0)
                    System.arraycopy(value, 0, vfBottomUrl, 0, numValid);

                if(!inSetup) {
                    hasChanged[FIELD_BOTTOM_URL] = true;
                    fireFieldChanged(FIELD_BOTTOM_URL);
                }
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Check the URL arrays for relative references. If found, add the
     * base URL to it to make them all fully qualified. This will also set the
     * urlRelativeCheck flag to true.
     */
    private void checkURLs() {
        URLChecker.checkURLsInPlace(worldURL, vfBackUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfFrontUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfLeftUrl, false);
        URLChecker.checkURLsInPlace(worldURL,  vfRightUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfTopUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfBottomUrl, false);
    }

    /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireUrlChanged(int index) {
        // Notify listeners of new value
        int num_listeners = urlListeners.size();
        VRMLUrlListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = (VRMLUrlListener)urlListeners.get(i);
            ul.urlChanged(this, index);
        }
    }

    /**
     * Send a notification to the registered listeners that the content state
     * has been changed. If no listeners have been registered, then this does
     * nothing, so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireContentStateChanged(int index) {
        // Notify listeners of new value
        int num_listeners = contentListeners.size();
        VRMLContentStateListener csl;

        for(int i = 0; i < num_listeners; i++) {
            csl = (VRMLContentStateListener)contentListeners.get(i);
            csl.contentStateChanged(this, index, loadState[index]);
        }
    }
}
