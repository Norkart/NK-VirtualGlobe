/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.texture;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.renderer.common.nodes.BaseTexture3DNode;
import org.web3d.vrml.util.URLChecker;

/**
 * Common implementation of a 3D ImageTexture node.
 * <p>
 *
 * No specific image types are directly handled by this node.
 *
 * @author Justin Couch
 * @version $Revision: 1.4 $
 */
public class BaseImageTexture3D extends BaseTexture3DNode
    implements VRMLSingleExternalNodeType {

    /** Field Index for the Image field */
    protected static final int FIELD_URL = LAST_3DTEXTURE_INDEX + 1;

    /** Last valid index for this texture */
    private static final int LAST_PIXELTEXTURE_INDEX = FIELD_URL;

    /** Number of fields constant */
    private static final int NUM_FIELDS = LAST_PIXELTEXTURE_INDEX + 1;

    /** Array of VRMLFieldDeclarations */
    private static VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static int[] nodeFields;

    // VRML Field declarations

    /* Internally the arrays are kept as max of whatever was sent to us in
     * a setValue.  So a length variable is needed to know the "real" length
     * This will speed setValue calls.  It will cost on sendRoute
     */

    /** exposedField MFString url [] */
    private String[] vfURL;

    /** URL Load State */
    private int loadState = NOT_LOADED;

    /** Class vars for performance */
    private String worldURL;

    /** List of those who want to know about Url changes.  Likely 1 */
    private ArrayList urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private ArrayList contentListeners;

    // Static constructor
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS*3);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_REPEATS] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatS");
        fieldDecl[FIELD_REPEATT] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatT");
        fieldDecl[FIELD_REPEATR] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFBool",
                                     "repeatR");
        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "url");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_URL);
        fieldMap.put("url", idx);
        fieldMap.put("set_url", idx);
        fieldMap.put("url_changed", idx);

        fieldMap.put("repeatS",new Integer(FIELD_REPEATS));
        fieldMap.put("repeatT",new Integer(FIELD_REPEATT));
        fieldMap.put("repeatR",new Integer(FIELD_REPEATR));
    }

    /**
     * Construct a default instance of this node.
     */
    protected BaseImageTexture3D() {
        super("ImageTexture3D");

        contentListeners = new ArrayList();
        urlListeners = new ArrayList(1);

        hasChanged = new boolean[NUM_FIELDS];
        vfURL = FieldConstants.EMPTY_MFSTRING;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a Box node, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseImageTexture3D(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLTexture3DNodeType)node);

        try {
            int index = node.getFieldIndex("image");
            VRMLFieldData field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfURL = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfURL,
                                 0,
                                 field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType
    //----------------------------------------------------------

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    public int getLoadState() {
        return loadState;
    }

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    public void setLoadState(int state) {
        switch(state) {
            case 2 :
                System.out.println("Loading: " + vfURL[0]);
                break;
            case 3 :
                System.out.println("Loading complete: " + vfURL[0]);
                break;
            case 4 :
                System.out.println("Loading failed: " + vfURL[0]);
                break;
            default :
                System.out.println("Unknown state: " + state);
        }

        loadState = state;
        fireContentStateChanged();
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
                vfURL = URLChecker.checkURLs(worldURL, newURL, false);
            else
                vfURL = newURL;
        } else {
            vfURL = FieldConstants.EMPTY_MFSTRING;
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
        return vfURL;
    }

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype) {
        // TODO: Need to implement
        return true;
    }

    /**
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param URI The URI used to load this content
     */
    public void setLoadedURI(String URI) {
        // Ignore
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param ul The listener instance to add
     */
    public void addUrlListener(VRMLUrlListener ul) {
        if (!urlListeners.contains(ul))
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
     * Get the declaration of the field at the given index. This allows for
     * reverse lookup if needed. If the field does not exist, this will give
     * a value of null.
     *
     * @param index The index of the field to get information
     * @return A representation of this field's information
     */
    public VRMLFieldDeclaration getFieldDeclaration(int index) {
        if(index < 0  || index > LAST_PIXELTEXTURE_INDEX)
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
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.TextureNodeType;
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
            case FIELD_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfURL;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfURL.length;
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
                case FIELD_URL :
                    destNode.setValue(destIndex, vfURL, vfURL.length);
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
     * Set the value of the field at the given index as a string. This would
     * be used to set SFString field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL:
                vfURL = new String[1];
                vfURL[0] = value;

                URLChecker.checkURLsInPlace(worldURL, vfURL, false);

                if (!inSetup)
                    fireUrlChanged(index);
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
     * @param numValid The number of valid values to copy from the array
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL :
                vfURL = new String[numValid];

                System.arraycopy(value, 0, vfURL, 0, numValid);

                URLChecker.checkURLsInPlace(worldURL, vfURL, false);

                if (!inSetup)
                    fireUrlChanged(index);

                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

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
     */
    protected void fireContentStateChanged() {
        // Notify listeners of new value
        int num_listeners = contentListeners.size();
        VRMLContentStateListener csl;

        for(int i = 0; i < num_listeners; i++) {
            csl = (VRMLContentStateListener)contentListeners.get(i);
            csl.contentStateChanged(this, FIELD_URL, loadState);
        }
    }
}
