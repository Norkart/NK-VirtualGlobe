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

package org.web3d.vrml.renderer.common.nodes.networking;

// External imports
import java.util.*;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.renderer.common.nodes.AbstractNode;
import org.web3d.vrml.util.FieldValidator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.util.URLChecker;

/**
 * A node that can handle inlined content from other VRML worlds.
 * <p>
 *
 * This implementation does not care whether the source world came from a
 * UTF8 or XML encoded file.
 * <p>
 *
 * While the node is awaiting content to be downloaded, it will put a wireframe
 * box around the suggested bounds of the content. If no bounds are set then
 * a 1x1x1 box is placed at the local origin. If the URL given is null, then
 * the outline box will not be shown.
 * <p>
 * TODO:<br>
 * - Implement a scheme to allow the updating of the contents at runtime when
 *   the URL changes. It currently removes the old content, but does not
 *   inform any ContentLoadManager to fetch it's new values.
 * - Load is ignored
 *
 * @author Justin Couch
 * @version $Revision: 1.23 $
 */
public class BaseInline extends AbstractNode
    implements VRMLInlineNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE =
        { TypeConstants.SingleExternalNodeType };

    /** Index of the url field */
    protected static final int FIELD_URL = LAST_NODE_INDEX + 1;

    /** Index of the Bounding box center bboxCenter field */
    protected static final int FIELD_BBOX_CENTER = LAST_NODE_INDEX + 2;

    /** Index of the Bounding box size bboxSize field */
    protected static final int FIELD_BBOX_SIZE = LAST_NODE_INDEX + 3;

    /** Index of the Load field */
    protected static final int FIELD_LOAD = LAST_NODE_INDEX + 4;

    /** The last field index used by this class */
    protected static final int LAST_INLINE_INDEX = FIELD_LOAD;

    /** Array of VRMLFieldDeclarations */
    protected static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    protected static final HashMap fieldMap;

    /** Listing of field indexes that have nodes */
    private static final int[] nodeFields;

    /** Valid set of mime types for VRML97 support */
    private static final HashSet validVrmlTypes;

    /** Valid set of mime types for X3D support */
    private static final HashSet validX3DTypes;

    /** SFVec3f bboxCenter NULL */
    protected float[] vfBboxCenter;

    /** SFVec3f bboxSize NULL */
    protected float[] vfBboxSize;

    /** The URL list */
    protected String[] vfUrl;

    /** The load field */
    protected boolean vfLoad;

    /** The world URL for correcting relative URL values */
    protected String worldURL;

    /** The URI of the content that was actually loaded */
    protected String loadedURI;

    /** Flag to indicate if we've checked the URLs for relative references */
    protected boolean urlRelativeCheck;

    /** The state of the load */
    protected int loadState;

    /** Scene containing the nested info */
    protected VRMLScene scene;

    /** List of those who want to know about Url changes. Likely 1 */
    private ArrayList urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private ArrayList contentListeners;

    /** The parent execution space of this inline */
    private VRMLExecutionSpace execSpace;

    /** Map of the exported names to the ImportProxyNode instances */
    private Map exportProxyNodes;

    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] { FIELD_METADATA };

        fieldDecl = new VRMLFieldDeclaration[LAST_INLINE_INDEX + 1];
        fieldMap = new HashMap(LAST_INLINE_INDEX + 1);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_URL] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "MFString",
                                     "url");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_LOAD] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFBool",
                                     "load");

        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_URL);
        fieldMap.put("url", idx);
        fieldMap.put("set_url", idx);
        fieldMap.put("url_changed", idx);

        idx = new Integer(FIELD_LOAD);
        fieldMap.put("load", idx);
        fieldMap.put("set_load", idx);
        fieldMap.put("load_changed", idx);

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        validVrmlTypes = new HashSet();
        validX3DTypes = new HashSet();

        validVrmlTypes.add("model/vrml");
        validVrmlTypes.add("x-world/x-vrml");

        validX3DTypes.add("model/x3d+vrml");
        validX3DTypes.add("model/x3d+xml");
        validX3DTypes.add("model/x3d+binary");
    }


    /**
     * Create a new, default instance of this class.
     */
    public BaseInline() {
        super("Inline");

        inSetup = true;
        hasChanged = new boolean[LAST_INLINE_INDEX + 1];

        urlRelativeCheck = false;

        vfBboxSize = new float[] {-1, -1, -1};
        vfBboxCenter = new float[] {0, 0, 0};
        vfLoad = true;
        vfUrl = FieldConstants.EMPTY_MFSTRING;

        urlListeners = new ArrayList(1);
        contentListeners = new ArrayList(1);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not the same type, an exception will be
     * thrown.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException Incorrect Node Type
     */
    public BaseInline(VRMLNodeType node) {
        this();

        checkNodeType(node);

        try {
            int index = node.getFieldIndex("bboxCenter");
            VRMLFieldData field = node.getFieldValue(index);
            vfBboxCenter[0] = field.floatArrayValue[0];
            vfBboxCenter[1] = field.floatArrayValue[1];
            vfBboxCenter[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("bboxSize");
            field = node.getFieldValue(index);
            vfBboxSize[0] = field.floatArrayValue[0];
            vfBboxSize[1] = field.floatArrayValue[1];
            vfBboxSize[2] = field.floatArrayValue[2];

            index = node.getFieldIndex("url");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfUrl, 0,
                    field.numElements);
            }

            if(vrmlMajorVersion > 2) {
                index = node.getFieldIndex("load");
                field = node.getFieldValue(index);
                vfLoad = field.booleanValue;
            }

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLBoundedNodeType
    //-------------------------------------------------------------

    /**
     * Accessor method to get current value of field <b>bboxCenter</b>
     * default value is <code>0 0 0</code>.
     *
     * @return Value of bboxCenter(SFVec3f)
     */
    public float[] getBboxCenter () {
        return vfBboxCenter;
    }

    /**
     * Accessor method to get current value of field <b>bboxSize</b>
     * default value is <code>-1 -1 -1</code>.
     *
     * @return The size of the bounding box(SFVec3f)
     */
    public float[] getBboxSize () {
        return vfBboxSize;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExecutionSpace
    //----------------------------------------------------------

    /**
     * Get the contained scene graph that this instance has. This represents
     * everything about the internal scene that the node declaration wraps.
     * This is a real-time representation so that if it the nodes contains a
     * script that changes the internal representation then this instance will
     * be updated to reflect and changes made.
     *
     * @return The scene contained by this node instance
     */
    public BasicScene getContainedScene() {
        return scene;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLInlineNodeType
    //----------------------------------------------------------

    /**
     * Get the parent execution space of this Inline.  This is for internal usage, you cannot
     * use this to walk back up the scenegraph because of protos.
     */
    public VRMLExecutionSpace getParentSpace() {
        return execSpace;
    }

    /**
     * Set the parent execution space of this Inline.  This is for internal usage, you cannot
     * use this to walk back up the scenegraph because of protos.
     *
     * @param space The parent space or null for the world root space
     */
    public void setParentSpace(VRMLExecutionSpace space) {
        execSpace = space;
    }

    /**
     * Set the mapping of import names to their proxy node implementations. The
     * mapping is from the exported name to the {@link ImportNodeProxy}
     * instance that it represents.
     *
     * @param imports The map of export names to proxy instances
     */
    public void setImportNodes(Map imports) {
        exportProxyNodes = imports;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType
    //----------------------------------------------------------

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype) {
        if(vrmlMajorVersion == 2)
            return validVrmlTypes.contains(mimetype);
        else
            return validX3DTypes.contains(mimetype);
    }

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
        loadState = state;

        fireContentStateChanged();
    }

    /**
     * Set the URL to a new value. If the value is null, it removes the old
     * contents (if set) and treats it as though there is no content.
     *
     * @param url The list of urls to set or null
     */
    public void setUrl(String[] newUrl, int numValid) {

        if(worldURL != null) {
            vfUrl = URLChecker.checkURLs(worldURL, newUrl, false);

        } else {
            vfUrl = newUrl;
        }

        if(!inSetup) {
            hasChanged[FIELD_URL] = true;
            fireFieldChanged(FIELD_URL);
            fireUrlChanged();
        }
    }

    /**
     * Get the list of URI's currently registered with this node.
     */
    public String[] getUrl() {
        return vfUrl;
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

        if(!(content instanceof VRMLScene)) {
            throw new IllegalArgumentException(
                "Invalid content type for inline. Not VRMLScene");
        }

// TODO:
// What should be do about clearing a scene when the URL is set back to empty.
// We really need to register a removed scene with the state manager as well
// as clearing the import mappings.

        if (scene != null) {
            // Clear up previous ref counts

            VRMLNodeType root = (VRMLNodeType) scene.getRootNode();

            for(int i = 0; i < layerIds.length; i++) {
                root.updateRefCount(i, false);
            }
        }

        scene = (VRMLScene)content;

        stateManager.registerAddedScene(this);

        VRMLNodeType root = (VRMLNodeType) scene.getRootNode();

        for(int i = 0; i < layerIds.length; i++) {
            root.updateRefCount(i, true);
        }

        // Map through the exports if we already have a scene set.
        if(exportProxyNodes == null)
            return;

        Map export_map = scene.getExports();
        Map def_map = scene.getDEFNodes();

        Set set = exportProxyNodes.keySet();
        Iterator itr = set.iterator();

        while(itr.hasNext()) {
            String import_name = (String)itr.next();
            String export_name = (String)export_map.get(import_name);

            VRMLNodeType export_node = (VRMLNodeType)def_map.get(export_name);

            if(export_node == null) {
                errorReporter.warningReport("Unable to map import of " +
                                            import_name + " to an exported " +
                                            " name in " + loadedURI, null);
                continue;
            }

            ImportNodeProxy proxy =
                (ImportNodeProxy)exportProxyNodes.get(import_name);
            proxy.setErrorReporter(errorReporter);

            proxy.setRealNode(export_node);
        }
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param URI The URI used to load this content
     */
    public void setLoadedURI(String URI) {
        loadedURI = URI;
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
        if(url.charAt(url.length() - 1) != '/')
            worldURL = url + '/';
        else
            worldURL = url;

        if(vfUrl != null)
            URLChecker.checkURLsInPlace(worldURL, vfUrl, false);

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

        // if the load flag is set to false, mark ourselves as having load
        // completed before we've started. Should prevent the load manager from
        // looking at us before it should.
        if(!vfLoad)
            loadState = LOAD_COMPLETE;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
    //----------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the node's internals can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        super.setErrorReporter(reporter);

        // Make sure our proxies pick up the new reporter, if we have any
        if(exportProxyNodes != null) {
            Set set = exportProxyNodes.keySet();
            Iterator itr = set.iterator();

            while(itr.hasNext()) {
                String name = (String)itr.next();
                ImportNodeProxy proxy =
                    (ImportNodeProxy)exportProxyNodes.get(name);
                proxy.setErrorReporter(errorReporter);
            }
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
        if(index < 0  || index > LAST_INLINE_INDEX)
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
        return TypeConstants.InlineNodeType;
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
            case FIELD_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfUrl.length;
                break;

            case FIELD_BBOX_SIZE:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxSize;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_BBOX_CENTER:
                fieldData.clear();
                fieldData.floatArrayValue = vfBboxCenter;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_LOAD:
                if(vrmlMajorVersion == 2)
                    throw new InvalidFieldException("load field not defined for VRML97");

                fieldData.clear();
                fieldData.booleanValue = vfLoad;
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
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
                case FIELD_URL:
                    destNode.setValue(destIndex, vfUrl, vfUrl.length);
                    break;

                case FIELD_LOAD:
                    destNode.setValue(destIndex, vfLoad);
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
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, float[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_BBOX_CENTER:
                setBboxCenter(value);
                break;

            case FIELD_BBOX_SIZE:
                setBboxSize(value);
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
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_URL:
                setUrl(value, numValid);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the load field.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     */
    public void setValue(int index, boolean value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_LOAD:
                setLoad(value);
                break;

            default:
                super.setValue(index, value);
        }
    }

    //----------------------------------------------------------
    // Internal convenience methods
    //----------------------------------------------------------

    /**
     * Convenience method to set a new value the vfBboxCenter fields
     *
     * @param newBboxCenter The new center of the bounding box
     */
    private void setBboxCenter(float[] newBboxCenter) {

        if(!inSetup)
            throw new InvalidFieldAccessException("bboxCenter is initializeOnly");

        vfBboxCenter[0] = newBboxCenter[0];
        vfBboxCenter[1] = newBboxCenter[1];
        vfBboxCenter[2] = newBboxCenter[2];
    }

    /**
     * Convenience method to set a new value for the vfBboxSize field.
     *
     * @param newBboxSize The new size for the bounding box
     */
    private void setBboxSize(float[] newBboxSize) {
        if(!inSetup)
            throw new InvalidFieldAccessException("bboxSize is initializeOnly");

        FieldValidator.checkBBoxSize("BaseShape.bboxSize", newBboxSize);

        vfBboxSize[0] = newBboxSize[0];
        vfBboxSize[1] = newBboxSize[1];
        vfBboxSize[2] = newBboxSize[2];
    }

    /**
     * Convenience method to handle the load field value. When value is
     * <code>true</code> replace the new URL with the old value by placing it
     * immediately on the load queue. If the value is <code>false</code> then
     * it should immediately remove the content. The derived classes should
     * make sure they override this method and handle the content removal.
     * The derived class should do all it's work before calling this method.
     *
     * @param value The new load field state
     */
    protected void setLoad(boolean value)
        throws InvalidFieldException
    {
        if(vrmlMajorVersion == 2)
            throw new InvalidFieldException("load field not defined for VRML97");

        // are we just removing everything?
        if(!inSetup && !value)
            stateManager.registerRemovedScene(this);

        boolean send_load = !vfLoad && value;

        vfLoad = value;

        if(!inSetup) {
            fireFieldChanged(FIELD_LOAD);
            hasChanged[FIELD_LOAD] = true;
        }

        if(send_load)
            fireUrlChanged();
    }

    /**
     * Send a notification to the registered listeners that a field has been
     * changed. If no listeners have been registered, then this does nothing,
     * so always call it regardless.
     *
     * @param index The index of the field that changed
     */
    protected void fireUrlChanged() {
        // Notify listeners of new value
        int num_listeners = urlListeners.size();
        VRMLUrlListener ul;

        for(int i = 0; i < num_listeners; i++) {
            ul = (VRMLUrlListener)urlListeners.get(i);
            ul.urlChanged(this, FIELD_URL);
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
