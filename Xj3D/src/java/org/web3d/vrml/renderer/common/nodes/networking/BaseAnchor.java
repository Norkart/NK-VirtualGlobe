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

package org.web3d.vrml.renderer.common.nodes.networking;

// Standard imports
import java.util.HashMap;
import java.util.ArrayList;
import java.io.StringReader;

// Application specific imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;
import org.web3d.vrml.util.URLChecker;

// TODO: recheck worldURL logic as a checkin when in inadventerly

/**
 * Base common implementation of an Anchor node.
 * <p>
 *
 * The anchor node represents a standard grouping node that also contains
 * URL information.
 * <p>
 *
 * For dealing with user input,
 *
 * @author Justin Couch
 * @version $Revision: 1.14 $
 */
public class BaseAnchor extends BaseGroupingNode implements VRMLLinkNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.LinkNodeType };

    // Field index decls

    /** Index of the description field */
    protected static final int FIELD_DESCRIPTION = LAST_GROUP_INDEX + 1;

    /** Index of the parameter field */
    protected static final int FIELD_PARAMETER = LAST_GROUP_INDEX + 2;

    /** Index of the URL field */
    protected static final int FIELD_URL = LAST_GROUP_INDEX + 3;

    /** The last index used by this field */
    protected static final int LAST_ANCHOR_INDEX = FIELD_URL;

    /** Total number of fields */
    protected static final int NUM_FIELDS = LAST_ANCHOR_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Indices of the fields that are MFNode or SFnode */
    private static int[] nodeFields;


    /** MFString URL list */
    protected String[] vfUrl;

    /** The world URL for correcting relative URL values */
    protected String worldURL;

    /** Flag to indicate if we've checked the URLs for relative references */
    protected boolean urlRelativeCheck;

    /** MFString parameter */
    protected String[] vfParameter;

    /** SFString description */
    protected String vfDescription;

    // VRML Field declarations

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_CHILDREN, FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFNode",
                                     "children");
        fieldDecl[FIELD_ADDCHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFNode",
                                     "addChildren");
        fieldDecl[FIELD_REMOVECHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTIN,
                                     "MFNode",
                                     "removeChildren");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_DESCRIPTION] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFString",
                                     "description");
        fieldDecl[FIELD_PARAMETER] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "parameter");
        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "url");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("set_children", idx);
        fieldMap.put("children_changed", idx);

        idx = new Integer(FIELD_ADDCHILDREN);
        fieldMap.put("addChildren", idx);
        fieldMap.put("set_addChildren", idx);

        idx = new Integer(FIELD_REMOVECHILDREN);
        fieldMap.put("removeChildren", idx);
        fieldMap.put("set_removeChildren", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        idx = new Integer(FIELD_DESCRIPTION);
        fieldMap.put("description", idx);
        fieldMap.put("set_description", idx);
        fieldMap.put("description_changed", idx);

        idx = new Integer(FIELD_PARAMETER);
        fieldMap.put("parameter", idx);
        fieldMap.put("set_parameter", idx);
        fieldMap.put("parameter_changed", idx);

        idx = new Integer(FIELD_URL);
        fieldMap.put("url", idx);
        fieldMap.put("set_url", idx);
        fieldMap.put("url_changed", idx);
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    public BaseAnchor() {
        super("Anchor");

        hasChanged = new boolean[NUM_FIELDS];
        vfUrl = FieldConstants.EMPTY_MFSTRING;
        vfParameter = FieldConstants.EMPTY_MFSTRING;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     * <P>
     * Note that the world URL has not been set by this call and will need to
     * be called separately.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    public BaseAnchor(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("description");
            VRMLFieldData field = node.getFieldValue(index);
            vfDescription = field.stringValue;

            index = node.getFieldIndex("parameter");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfParameter = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfParameter,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("url");
            field = node.getFieldValue(index);

            if(field.numElements != 0) {
                vfUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfUrl,
                                 0,
                                 field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }
    //----------------------------------------------------------
    // Methods overriding VRMLLinkNodeType class.
    //----------------------------------------------------------

    /**
     * Get the description to associate with the link. This is a line of text
     * suitable for mouseovers, status information etc. If there is no
     * description set then it returns null.
     *
     * @return The current description or null
     */
    public String getDescription() {
        return vfDescription;
    }

    /**
     * Set the description string for this link. Setting a value of null will
     * clear the current description.
     *
     * @param desc The new description to set
     */
    public void setDescription(String desc) {
        vfDescription = desc;

        if(!inSetup) {
            hasChanged[FIELD_DESCRIPTION] = true;
            fireFieldChanged(FIELD_DESCRIPTION);
        }
    }

    /**
     * Get the current list of parameters registered for this link. If there
     * are none set then this will return null. No format checking of the
     * strings are performed.
     *
     * @return The list of current parameter values or null
     */
    public String[] getParameter() {
        return vfParameter;
    }

    /**
     * Set the parameter list to the new series of values. A value of null for
     * the parameter list will clear the current list.
     *
     * @param params The new list of parameters to use
     */
    public void setParameter(String[] params) {

        if(vfParameter.length != params.length)
            vfParameter = new String[params.length];

        if(params.length != 0)
            System.arraycopy(params, 0, vfParameter, 0, params.length);

        if(!inSetup) {
            hasChanged[FIELD_PARAMETER] = true;
            fireFieldChanged(FIELD_PARAMETER);
        }
    }

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

        worldURL = url;
    }

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    public String getWorldUrl() {
        return worldURL;
    }

    /**
     * Sets the URL to a new value.  We will load only one
     * of these URL's.  The list provides alternates.
     *
     * @param newURL Array of candidate URL strings
     * @param numValid The number of valid values to copy from the array
     */
    public void setUrl(String[] newURL, int numValid) {
        if(numValid > 0) {
            if(worldURL != null)
                vfUrl = URLChecker.checkURLs(worldURL, newURL, false);
            else
                vfUrl = newURL;
        } else {
            vfUrl = FieldConstants.EMPTY_MFSTRING;
        }

        if (!inSetup) {
            hasChanged[FIELD_URL] = true;
            fireFieldChanged(FIELD_URL);
        }
    }

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    public String[] getUrl() {
        return vfUrl;
/*
        String fullURLs[] = new String[vfUrl.length];

        for(int i=0; i < vfUrl.length; i++)
            fullURLs[i] = worldURL + vfUrl[i];

        return fullURLs;
*/
    }

    //----------------------------------------------------------
    // Methods overriding VRMLNodeType
    //----------------------------------------------------------
    /**
     * Get the index of the given field name. If the name does not exist for
     * this node then return a value of -1.
     *
     * @param fieldName The name of the field we want the index from
     * @return The index of the field name or -1
     */
    public int getFieldIndex(String fieldName) {
        Integer index = (Integer) fieldMap.get(fieldName);

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
        if(index < 0  || index > LAST_ANCHOR_INDEX)
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
            case FIELD_DESCRIPTION:
                fieldData.clear();
                fieldData.stringValue = vfDescription;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FIELD_PARAMETER:
                fieldData.clear();
                fieldData.stringArrayValue = vfParameter;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                if(vfParameter != null)
                    fieldData.numElements = vfParameter.length;
                else
                    fieldData.numElements = 0;
                break;

            case FIELD_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                if(vfUrl != null)
                    fieldData.numElements = vfUrl.length;
                else
                    fieldData.numElements = 0;
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
                case FIELD_URL:
                    destNode.setValue(destIndex, vfUrl, vfUrl.length);
                    break;
                case FIELD_PARAMETER:
                    destNode.setValue(destIndex,
                                      vfParameter,
                                      vfParameter.length);
                    break;

                case FIELD_DESCRIPTION:
                    destNode.setValue(destIndex, vfDescription);
                    break;

                default:
                    super.sendRoute(time, srcIndex, destNode, destIndex);
            }
        } catch(InvalidFieldException ife) {
            System.err.println("sendRoute: No field!" + ife.getFieldName());
        } catch(InvalidFieldValueException ifve) {
            System.err.println("sendRoute: Invalid field Value: " +
                ifve.getMessage());
        }
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set SFString field "title".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_DESCRIPTION:
                setDescription(value);
                break;
            case FIELD_URL:
                setUrl(new String[] {value}, 1);
                break;

            default :
                super.setValue(index, value);
        }
    }

    /**
     * Set the value of the field at the given index as a double array of
     * floats. The orientation is to use [number of items][number of values
     * in one item]. This would be used to set MFVec2f, MFVec3f and MFRotation,
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_URL:
                setUrl(value, numValid);
                break;

            case FIELD_PARAMETER:
                setParameter(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Check the given list of URLs for relative references. If found, add the
     * base URL to it to make them all fully qualified. This will also set the
     * urlRelativeCheck flag to true.
     *
     * @param urls The list of URLs to check
     * @param numValid The number of valid values to copy from the array
     * @return The list of updated URLs.
     */
    private String[] checkURLs(String[] urls, int numValid) {

        String[] ret_val = new String[numValid];
        String tmp;

        for(int i = 0; i < numValid; i++) {
            tmp = urls[i];

            if(tmp.length() > 0 && (tmp.charAt(0) != '#') && (tmp.indexOf(':') == -1)) {
                ret_val[i] = worldURL + tmp;
            } else {
                ret_val[i] = tmp;
            }
        }

        return ret_val;
    }
}
