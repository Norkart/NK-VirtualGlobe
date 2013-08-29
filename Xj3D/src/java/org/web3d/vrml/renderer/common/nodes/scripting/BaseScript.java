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

package org.web3d.vrml.renderer.common.nodes.scripting;

// External imports
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

// Local imports
import org.web3d.vrml.lang.*;

import org.web3d.util.ArrayUtils;
import org.web3d.util.HashSet;
import org.web3d.util.IntHashMap;
import org.web3d.vrml.nodes.VRMLContentStateListener;
import org.web3d.vrml.nodes.VRMLFieldData;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.web3d.vrml.nodes.VRMLUrlListener;
import org.web3d.vrml.parser.FieldParserFactory;
import org.web3d.vrml.parser.VRMLFieldReader;
import org.web3d.vrml.renderer.common.nodes.AbstractDynamicFieldNode;
import org.web3d.vrml.scripting.ScriptWrapper;
import org.web3d.vrml.util.URLChecker;

/**
 * Scene graph representation of a script node.
 * <p>
 *
 * The script is different to all the other nodes. While it represents
 * a script, it doesn't have the normal content of a Java3D node. It is
 * also a bit different to the ordinary Abstract node implementation in
 * that a script can have fields added and removed on demand.
 *
 * @author Justin Couch
 * @version $Revision: 1.49 $
 */
public abstract class BaseScript extends AbstractDynamicFieldNode
    implements VRMLScriptNodeType {

    /** Constant indicating a field of size 0 */
    private static final Integer ZERO_SIZE = new Integer(0);

    /** Constant indicating a field of size 1 */
    private static final Integer SINGLE_SIZE = new Integer(1);

    /** Message when the field type is not valid in VRML97 */
    private static final String VRML97_FIELD_MSG =
        "Field type not supported in VRML97: ";

    private static final String VRML97_EXPOSED_MSG =
        "VRML97 Does not allow script fields to be declared as exposedField";

    /** The set of MIME types this engine will support */
    private static final String[] VRML97_MIME_TYPES = {
        "application/x-javascript",
        "application/javascript",
        "application/x-java",
        "application/java"
    };

    /** The set of MIME types this engine will support */
    private static final String[] X3D_MIME_TYPES = {
        "application/x-ecmascript",
        "application/ecmascript",
        "application/x-java",
        "application/java"
    };

    private static final int[] SECONDARY_TYPE =
        { TypeConstants.SingleExternalNodeType };

    /** List of valid content types for all VRML97 scripts */
    private static final HashSet validVrml97Types;

    /** List of valid content types for all X3D scripts */
    private static final HashSet validX3DTypes;

    /** List of fields that are not allowed to be defined elsewhere */
    private static final HashSet forbiddenFields;

    /** Index of the metadata field */
    private final int FIELD_METADATA;

    /** Index of the URL field */
    private final int FIELD_URL;

    /** Index of the mustEvaluate field */
    private final int FIELD_MUST_EVALUATE;

    /** Index of teh directOutput field */
    private final int FIELD_DIRECT_OUTPUT;

    /** The last of the fixed index items */
    private final int LAST_FIXED_INDEX;

    /**
     * Mapping of field index to field value. Field values are always kept in
     * primitive class form or as the raw array. All arrays should be single
     * dimensional, rather than 2D arrays. If one of the setValue methods is
     * called with 2D array, flatten it into a 1D array on the spot and store
     * that.
     */
    private IntHashMap fieldValueMap;

    /** The list of sizes for each field */
    private IntHashMap fieldSizeMap;

    /** Map of whether the field index has been changed since last check */
    private IntHashMap fieldChangedMap;

    /** Mapping of field index to user data object */
    private IntHashMap userData;

    /** The world URL for correcting relative URLS */
    private String worldURL;

    /** The state of the load */
    private int loadState;

    /** Flag indicating this is a DEF node */
    private boolean isDEF;

    /** Flag for the node being static */
    private boolean isStatic;

    /** Flag to say we're processing eventOuts right now */
    private boolean eventOutProcessingInProgress;

    /** The major version of the spec this instance belongs to. */
    protected int majorVersion;

    /** The minor version of the spec this instance belongs to. */
    protected int minorVersion;

    /** Flags for the local, pre-defined fields */
    private boolean[] hasChanged;

    /** Change time for the local, pre-defined fields */
    private double[] lastChangedTime;

    /** MFString url field */
    private String[] vfUrl;

    /** SFBool mustEvaluate field */
    private boolean vfMustEvaluate;

    /** SFBool directOutput field */
    private boolean vfDirectOutput;

    /** The actual executable code */
    private ScriptWrapper realScript;

    /** Execution space that this script belongs to */
    private VRMLExecutionSpace execSpace;

    /** List of those who want to know about Url changes. Likely 1 */
    private ArrayList urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private ArrayList contentListeners;

    /**
     * Static initializer to set up table of valid content types.
     */
    static {
        validVrml97Types = new HashSet();

        for(int i = 0; i < VRML97_MIME_TYPES.length; i++)
            validVrml97Types.add(VRML97_MIME_TYPES[i]);

        validX3DTypes = new HashSet();

        for(int i = 0; i < X3D_MIME_TYPES.length; i++)
            validX3DTypes.add(X3D_MIME_TYPES[i]);

        forbiddenFields = new HashSet();
        forbiddenFields.add("metadata");
        forbiddenFields.add("url");
        forbiddenFields.add("mustEvaluate");
        forbiddenFields.add("directOutput");

    }

    /**
     * Construct a default instance of the script
     */
    protected BaseScript() {
        super("Script");

        isDEF = false;
        inSetup = true;

        vfMustEvaluate = false;
        vfDirectOutput = false;

        urlListeners = new ArrayList(1);
        contentListeners = new ArrayList(1);

        fieldSizeMap = new IntHashMap();
        fieldValueMap = new IntHashMap();
        fieldChangedMap = new IntHashMap();
        userData = new IntHashMap();

        VRMLFieldDeclaration field;
        int field_index = 0;

        loadState = NOT_LOADED;

        // Now, register the 4 basic fields with the base class
        field = new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                         "SFNode",
                                         "metadata");
        try {
            field_index = appendField(field);
        } catch(FieldExistsException fee) {
            // Grrrr!!!!!
        }

        FIELD_METADATA = field_index;

        field = new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                         "MFString",
                                         "url");
        try {
            field_index = appendField(field);
        } catch(FieldExistsException fee) {
            // Bloody hope not!!!!!
        }

        FIELD_URL = field_index;

        field = new VRMLFieldDeclaration(FieldConstants.FIELD,
                                         "SFBool",
                                         "mustEvaluate");
        try {
            field_index = appendField(field);
        } catch(FieldExistsException fee) {
            // Useless piece of code...
        }

        FIELD_MUST_EVALUATE = field_index;

        field = new VRMLFieldDeclaration(FieldConstants.FIELD,
                                         "SFBool",
                                         "directOutput");
        try {
            field_index = appendField(field);
        } catch(FieldExistsException fee) {
            // Farkenell
        }

        FIELD_DIRECT_OUTPUT = field_index;

        LAST_FIXED_INDEX = FIELD_DIRECT_OUTPUT;
        fieldChangedMap = new IntHashMap();

        hasChanged = new boolean[LAST_FIXED_INDEX  + 1];

        vfUrl = FieldConstants.EMPTY_MFSTRING;

        eventOutProcessingInProgress = false;
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseScript(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((BaseScript)node);

        try {
            int index = node.getFieldIndex("url");
            VRMLFieldData field = node.getFieldValue(index);

            // Only copy if the field has values set
            if(field.numElements != 0) {
                vfUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue,
                                 0,
                                 vfUrl,
                                 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("mustEvaluate");
            field = node.getFieldValue(index);

            vfMustEvaluate = field.booleanValue;

            index = node.getFieldIndex("directOutput");
            field = node.getFieldValue(index);

            vfDirectOutput = field.booleanValue;

        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    /**
     * Notify a node that an ExternProto has resolved.  This will verify the objects
     * type and add it to the render sceneGraph.
     *
     * @param index The field index
     * @throws InvalidFieldValueException If the proto contains he wrong type
     */
     public synchronized void notifyExternProtoLoaded(int index, VRMLNodeType node)
        throws InvalidFieldValueException {

System.out.println("Script needs to handle notifyExternProtoLoaded");
     }

    //----------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType
    //----------------------------------------------------------

    /**
     * Replace the existing set of URLs with this new set. If the array is null
     * or zero length, it will clear the existing values.
     *
     * @param newUrl The list of new instances to use
     * @param numValid number of valid items to use from the size array
     */
    public void setUrl(String[] newUrl, int numValid) {
        if(worldURL != null) {
            vfUrl = URLChecker.checkURLs(worldURL, newUrl, false);
        } else {
            vfUrl = newUrl;
        }

        hasChanged[FIELD_URL] = true;
        fireFieldChanged(FIELD_URL);
        fireUrlChanged();
    }

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    public String[] getUrl() {
        return vfUrl;
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

        if(!(content instanceof ScriptWrapper))
            throw new IllegalArgumentException("Not a script wrapper");

        realScript = (ScriptWrapper)content;
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param uri The URI used to load this content
     */
    public void setLoadedURI(String uri) {
        // Do nothing for now.
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
    // Methods defined by VRMLScriptNodeType
    //----------------------------------------------------------

    /**
     * Set the execution space to be this new value. Usually set at some time
     * after the initial loading of the class, but before content is set. A
     * value of null will clear the current space.
     *
     * @param space The space that this script is operating in
     */
    public void setExecutionSpace(VRMLExecutionSpace space) {
        execSpace = space;
    }

    /**
     * Get the execution space that this script is operating under. If there
     * is none, this will return null.
     *
     * @return The current execution space or null
     */
    public VRMLExecutionSpace getExecutionSpace() {
        return execSpace;
    }

    /**
     * Notification that the script can now call the initialize() method on the
     * user script code. If this is called accidentally when there is no user
     * code set, silently ignore the request.
     *
     * @param timestamp The VRML time that the initialisation occured at
     */
    public void initialize(double timestamp) {
        try {
            if(realScript != null) {
                realScript.setTimestamp(timestamp);
                realScript.initialize(this);

                eventOutProcessingInProgress = true;
                realScript.updateEventOuts();
                eventOutProcessingInProgress = false;
            }
        } catch(Exception e) {
           System.out.println("Script failed to initialize: " + vfUrl[0]);
           e.printStackTrace();
        }
    }

    /**
     * Notification to call the prepare-events for scripts at the start of the
     * timestamp. For scripts that are in an X3D world, this will also call the
     * prepareEvents SAI service. This method will be called every frame.
     *
     * @param timestamp The time of the current frame in VRML time
     */
    public void prepareEvents(double timestamp) {
        if(realScript != null) {
            realScript.setTimestamp(timestamp);
            if(realScript.prepareEvents() && realScript.sendEvents()) {
                eventOutProcessingInProgress = true;
                realScript.updateEventOuts();
                eventOutProcessingInProgress = false;
            }
        }
    }

    /**
     * Notification by the route manager that an event cascade is complete.
     * This should allow the underlying scripting engine to call
     * <code>processEvents()</code> (or equivalent) on the script code.
     */
    public void processEvents() {
        if((realScript != null) && realScript.sendEvents()) {
            eventOutProcessingInProgress = true;
            realScript.updateEventOuts();
            eventOutProcessingInProgress = false;
        }
    }

    /**
     * Notification by the route manager that an event cascade is complete.
     * This should allow the underlying scripting engine to call
     * <code>eventsProcessed()</code> on the script code.
     */
    public void eventsProcessed() {
        if(realScript != null) {
            realScript.eventsProcessed();

            eventOutProcessingInProgress = true;
            realScript.updateEventOuts();
            eventOutProcessingInProgress = false;
        }
    }

    /**
     * Call shutdown on the user content now. It will no longer be needed. This
     * does not shut down the entire node. It is assumed that content will be
     * forcoming shortly.
     */
    public void shutdown() {
        if(realScript != null) {
            realScript.shutdown();

            if(realScript.sendEvents()) {
                eventOutProcessingInProgress = true;
                realScript.updateEventOuts();
                eventOutProcessingInProgress = false;
            }

            realScript = null;
        }
    }

    /**
     * Completely shutdown this script node. There's no life left in this one
     * so might as well clean up everything. The user code shutdown will be
     * guaranteed to be called before this method.
     */
    public void shutdownAll() {
        // Do nothing for now.
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeType
    //----------------------------------------------------------

    /**
     * Check to see if setupFinished() has already been called on this node.
     *
     * @return true if setupFinished() has been called
     */
    public boolean isSetupFinished() {
        return !inSetup;
    }

    /**
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        super.setupFinished();

        VRMLFieldDeclaration decl;
        int access_type;
        int field_type;
        int num_fields = getFieldCount();

        for(int i = 0; i < num_fields; i++) {
            decl = (VRMLFieldDeclaration)fieldDeclList.get(i);
            fieldChangedMap.put(i, Boolean.FALSE);

            access_type = decl.getAccessType();

            if((access_type == FieldConstants.EVENTIN) ||
               (access_type == FieldConstants.EVENTOUT))
                continue;

            field_type = decl.getFieldType();

            if((field_type != FieldConstants.MFNODE) &&
               (field_type != FieldConstants.SFNODE))
                continue;

            Object value = fieldValueMap.get(i);

            if(value instanceof VRMLNodeType) {
                VRMLNodeType s_node = (VRMLNodeType)value;
                if(s_node != this)
                    s_node.setupFinished();
            } else if (value instanceof VRMLNodeType[]) {
                // It may have been null too, so always check....
                VRMLNodeType[] m_node = (VRMLNodeType[])value;
                int num_nodes = ((Integer)fieldSizeMap.get(i)).intValue();
                for(int j = 0; j < num_nodes; j++) {
                    if((m_node[j] != this) && (m_node[j] != null))
                        m_node[j].setupFinished();
                }
            }
        }
    }

    /**
     * Notify this node that is has been DEFd. This method shall only be
     * called before setupFinished(). It is an error to call it any other
     * time. It is also guaranteed that this call will be made after
     * construction, but before any of the setValue() methods have been called.
     *
     * @throws IllegalStateException The setup is finished.
     */
    public void setDEF() {
        if(!inSetup)
            throw new IllegalStateException("Can't set DEF now");

        isDEF = true;
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

        if(index >= 0 && index <= LAST_FIXED_INDEX) {

            if(index == FIELD_URL) {
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.stringArrayValue = vfUrl;
                fieldData.numElements = (vfUrl == null) ? 0 : vfUrl.length;
            }  else if(index == FIELD_DIRECT_OUTPUT) {
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfDirectOutput;
            } else if(index == FIELD_MUST_EVALUATE) {
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                fieldData.booleanValue = vfMustEvaluate;
            } else if(index == FIELD_METADATA) {
                fieldData.nodeValue = vfMetadata;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
            }
        } else {

            if(!fetchValue(index))
                return null;
        }


        return fieldData;
    }

    /**
     * Check to see if the given field has changed since we last checked.
     * Calling this method will set the flag back to "not changed" so that two
     * consective reads after a changed value would result in a true and then
     * false being returned. If the field number is not recognized for this
     * node then this returns false.
     *
     * @param index The index of the field to change.
     * @return true if the field has changed since last read
     */
    public boolean hasFieldChanged(int index) {

        boolean ret_val = false;
        Boolean bool = (Boolean)fieldChangedMap.get(index);
        ret_val = bool.booleanValue();
        fieldChangedMap.put(index, Boolean.FALSE);

        return ret_val;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return TypeConstants.ScriptNodeType;
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

        Object value = fieldValueMap.get(srcIndex);

        // Shouldn't happen, but is a saftey valve just in case
        if(value == null)
            return;

        Integer size = (Integer)fieldSizeMap.get(srcIndex);

        try {
            sendValue(destNode, destIndex, value, size.intValue());
        } catch(InvalidFieldException ife) {
            System.err.println("Script route to invalid event: " +
                               ife.getFieldName());
            ife.printStackTrace();
        } catch(InvalidFieldValueException ifve) {
            System.err.println("Script route sending out of range values " +
                               ifve.getFieldName());
            ifve.printStackTrace();
            System.out.println("URL: " + vfUrl[0]);
        }
    }

    /**
     * Set the value of the field at the given index as an integer. This would
     * be used to set SFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int value)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(int): Invalid Index: " +
                                            index);

        fieldValueMap.put(index, new Integer(value));

        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(FieldConstants.SFINT32, decl.getName(), value);

        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
    }

    /**
     * Set the value of the field at the given index as an array of integers.
     * This would be used to set MFInt32 field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, int[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(int[]): Invalid Index: " +
                                            index);

        fieldSizeMap.put(index, new Integer(numValid));

        if(numValid != 0) {
            int[] cur_val = (int[])(fieldValueMap.get(index));
            if(cur_val == null || cur_val.length < numValid) {
                cur_val = new int[numValid];
                fieldValueMap.put(index, cur_val);
            }

            System.arraycopy(value, 0, cur_val, 0, numValid);
        }

        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(decl.getFieldType(),
                                  decl.getName(),
                                  value,
                                  numValid);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
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

        if(index >= 0 && index <= LAST_FIXED_INDEX) {
            if(index == FIELD_URL) {
                throw new InvalidFieldException("setValue(boolean): " +
                                                "URL field not a boolean");
            } else if(!inSetup)
                throw new InvalidFieldException("Cannot change initialise " +
                                                " only fields");
            if(index == FIELD_DIRECT_OUTPUT) {
                vfDirectOutput = value;
            } else if (index == FIELD_MUST_EVALUATE) {
                vfMustEvaluate = value;
            } else {
                throw new InvalidFieldException("Unknown fixed field");
            }
        } else {

            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if(decl == null)
                throw new InvalidFieldException("setValue(int): Invalid Index: " +
                                                index);

            fieldValueMap.put(index, new Boolean(value));

            if((realScript != null) && shouldUpdate(index, decl))
                realScript.queueEvent(FieldConstants.SFBOOL, decl.getName(), value);

            fieldChangedMap.put(index, Boolean.TRUE);
            fireFieldChanged(index);
        }
    }

    /**
     * Set the value of the field at the given index as an array of boolean.
     * This would be used to set MFBool field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, boolean[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(boolean[]): Invalid Index: " +
                                            index);

        fieldSizeMap.put(index, new Integer(numValid));

        if(numValid != 0) {
            boolean[] cur_val = (boolean[])(fieldValueMap.get(index));
            if(cur_val == null || cur_val.length < numValid) {
                cur_val = new boolean[numValid];
                fieldValueMap.put(index, cur_val);
            }

            System.arraycopy(value, 0, cur_val, 0, numValid);
        }

        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(FieldConstants.MFBOOL,
                                  decl.getName(),
                                  value,
                                  numValid);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
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

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(float): Invalid Index: " +
                                            index);

        fieldValueMap.put(index, new Float(value));
        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(decl.getFieldType(), decl.getName(), value);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
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

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(float[]): Invalid Index: " +
                                            index);

        fieldSizeMap.put(index, new Integer(numValid));

        if(numValid != 0) {
            float[]  cur_val = (float[])(fieldValueMap.get(index));
            if(cur_val == null || cur_val.length < numValid) {
                cur_val = new float[numValid];
                fieldValueMap.put(index, cur_val);
            }

            System.arraycopy(value, 0, cur_val, 0, numValid);
        }

        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(decl.getFieldType(),
                                  decl.getName(),
                                  value,
                                  numValid);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
    }

    /**
     * Set the value of the field at the given index as an long. This would
     * be used to set SFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, long value)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(long): Invalid Index: " +
                                            index);

        fieldValueMap.put(index, new Long(value));
        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(decl.getFieldType(), decl.getName(), value);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
    }

    /**
     * Set the value of the field at the given index as an array of longs.
     * This would be used to set MFTime field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, long[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(long): Invalid Index: " +
                                            index);

        fieldSizeMap.put(index, new Integer(numValid));

        if(numValid != 0) {
            long[] cur_val = (long[])(fieldValueMap.get(index));
            if(cur_val == null || cur_val.length < numValid) {
                cur_val = new long[numValid];
                fieldValueMap.put(index, cur_val);
            }

            System.arraycopy(value, 0, cur_val, 0, numValid);
        }

        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(decl.getFieldType(),
                                  decl.getName(),
                                  value,
                                  numValid);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
    }

    /**
     * Set the value of the field at the given index as an double. This would
     * be used to set SFDouble field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double value)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(double): Invalid Index: " +
                                            index);

        fieldValueMap.put(index, new Double(value));
        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(decl.getFieldType(), decl.getName(), value);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
    }

    /**
     * Set the value of the field at the given index as an array of doubles.
     * This would be used to set MFDouble, SFVec2d and SFVec3d field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(double): Invalid Index: " +
                                            index);

        fieldSizeMap.put(index, new Integer(numValid));

        if(numValid != 0) {
            double[] cur_val = (double[])(fieldValueMap.get(index));
            if(cur_val == null || cur_val.length < numValid) {
                cur_val = new double[numValid];
                fieldValueMap.put(index, cur_val);
            }
            System.arraycopy(value, 0, cur_val, 0, numValid);
        }

        if((realScript != null) && shouldUpdate(index, decl))
            realScript.queueEvent(decl.getFieldType(),
                                  decl.getName(),
                                  value,
                                  numValid);
        fieldChangedMap.put(index, Boolean.TRUE);
        fireFieldChanged(index);
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

        if(index == FIELD_URL) {
            setUrl(new String[] { value }, 1);
        } else {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if(decl == null)
                throw new InvalidFieldException("setValue(String): Invalid Index: " +
                                                index);

            fieldValueMap.put(index, value);
            if((realScript != null) && shouldUpdate(index, decl))
                realScript.queueEvent(FieldConstants.SFSTRING, decl.getName(), value);
            fieldChangedMap.put(index, Boolean.TRUE);
            fireFieldChanged(index);
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

        if(index == FIELD_URL) {
            setUrl(value, numValid);
        } else {
            VRMLFieldDeclaration decl = getFieldDeclaration(index);

            if(decl == null)
                throw new InvalidFieldException("setValue(String[]): Invalid Index: " +
                                                index);

            fieldSizeMap.put(index, new Integer(numValid));

            if(numValid != 0) {
                String[] cur_val = (String[])(fieldValueMap.get(index));
                if(cur_val == null || cur_val.length < numValid) {
                    cur_val = new String[numValid];
                    fieldValueMap.put(index, cur_val);
                }

                System.arraycopy(value, 0, cur_val, 0, numValid);
            }

            if((realScript != null) && shouldUpdate(index, decl))
                realScript.queueEvent(FieldConstants.MFSTRING,
                                      decl.getName(),
                                      value,
                                      numValid);

            fieldChangedMap.put(index, Boolean.TRUE);
            fireFieldChanged(index);
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

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(node): Invalid Index: " +
                                            index);
        // I don't think this will handle MFNode fields properly during the
        // setup phase. Should probably be appending the values here.
        if(inSetup && decl.getFieldType() == FieldConstants.MFNODE) {
            Object cur_node = fieldValueMap.get(index);
            if(cur_node == null) {
                fieldValueMap.put(index, child);
                fieldSizeMap.put(index, new Integer(1));
            } else if(cur_node instanceof VRMLNodeType) {
                VRMLNodeType[] na = new VRMLNodeType[2];
                na[0] = (VRMLNodeType)cur_node;
                na[1] = child;
                fieldValueMap.put(index, na);
                fieldSizeMap.put(index, new Integer(2));
            } else {
                // already existing array so just elongate
                Integer cs = (Integer)fieldSizeMap.get(index);
                int cur_size = cs.intValue();

                VRMLNodeType[] cur_vals = (VRMLNodeType[])cur_node;

                if(cur_vals.length == cur_size) {
                    VRMLNodeType[] tmp = new VRMLNodeType[cur_size + 4];
                    System.arraycopy(cur_vals, 0, tmp, 0, cur_size);
                    cur_vals = tmp;
                    fieldValueMap.put(index, tmp);
                }

                cur_vals[cur_size] = child;
                fieldSizeMap.put(index, new Integer(cur_size + 1));
            }
        } else {
            if (index == FIELD_METADATA) {
                vfMetadata = child;
            } else {
                fieldValueMap.put(index, child);
            }
            if((realScript != null) && shouldUpdate(index, decl)) {
                if(decl.getFieldType() == FieldConstants.SFNODE)
                    realScript.queueEvent(FieldConstants.SFNODE,
                                          decl.getName(),
                                          child);
                else
                    realScript.queueEvent(FieldConstants.MFNODE,
                                          decl.getName(),
                                          child);
            }

            fieldChangedMap.put(index, Boolean.TRUE);
            fireFieldChanged(index);
            fieldSizeMap.put(index, SINGLE_SIZE);
        }
    }

    /**
     * Set the value of the field at the given index as an array of nodes.
     * This would be used to set MFNode field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not known
     * @throws InvalidFieldValueException The value provided is out of range
     *    for the field type.
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException {
        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException("setValue(node[]): Invalid Index: " +
                                            index);

        fieldSizeMap.put(index, new Integer(numValid));

        if(inSetup) {
            Object cur_node = fieldValueMap.get(index);
            if(cur_node == null) {
                fieldValueMap.put(index, children);
            } else if(cur_node instanceof VRMLNodeType) {
                VRMLNodeType[] na = new VRMLNodeType[children.length + 1];
                na[0] = (VRMLNodeType)cur_node;
                System.arraycopy(children, 0, na, 1, children.length);
                fieldValueMap.put(index, na);
            } else {
                // already existing array so just elongate
                VRMLNodeType[] cur_vals = (VRMLNodeType[])cur_node;
                int new_size = cur_vals.length + children.length;

                VRMLNodeType[] na = new VRMLNodeType[new_size];

                System.arraycopy(cur_vals, 0, na, 0, cur_vals.length);
                System.arraycopy(children,
                                 0,
                                 na,
                                 cur_vals.length,
                                 children.length);
                fieldValueMap.put(index, na);
            }
        } else {
            if(numValid != 0) {
                VRMLNodeType[]  cur_val = (VRMLNodeType[])fieldValueMap.get(index);
                if(cur_val == null || cur_val.length < numValid) {
                    cur_val = new VRMLNodeType[numValid];
                    fieldValueMap.put(index, cur_val);
                }

                System.arraycopy(children, 0, cur_val, 0, numValid);
            }

            if((realScript != null) && shouldUpdate(index, decl))
                realScript.queueEvent(FieldConstants.MFNODE,
                                      decl.getName(),
                                      children,
                                      numValid);

            fieldChangedMap.put(index, Boolean.TRUE);
            fireFieldChanged(index);
        }
    }

    /**
     * Check to see if this node has been DEFd. Returns true if it has and
     * the user should ask for the shared representation rather than the
     * normal one.
     *
     * @return true if this node has been DEFd
     */
    public boolean isDEF() {
        return isDEF;
    }

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node. This shouldn't be called because it is
     * handled by the script engines, but make a reasonable-pass guess at this
     * anyway based on the set of all types supported.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype) {
        if(majorVersion == 2)
            return validVrml97Types.contains(mimetype);
        else
            return validX3DTypes.contains(mimetype);
    }

    //----------------------------------------------------------
    // Methods defined by AbstractDynamicFieldNode
    //----------------------------------------------------------

    /**
     * Append a field declaration to this node. Overrides the base class to
     * check for eventIns and eventOuts and making sure they have a valid
     * default value.
     *
     * @param field The new field to add
     * @return The index that this field was added at
     * @throws FieldExistsException A conflicting field of the same name
     *   already exists for this node
     */
    public int appendField(VRMLFieldDeclaration field)
        throws FieldExistsException, InvalidFieldException {

        // Sanity check to prevent VRML97 content from declaring bogus fields.
        if(majorVersion == 2) {
            if(field.getAccessType() == FieldConstants.EXPOSEDFIELD) {
                throw new InvalidFieldException(VRML97_EXPOSED_MSG +
                                                field.getName());
            }

            switch(field.getFieldType()) {
                case FieldConstants.SFDOUBLE:
                case FieldConstants.MFDOUBLE:
                case FieldConstants.SFLONG:
                case FieldConstants.MFLONG:
                case FieldConstants.MFBOOL:
                case FieldConstants.SFCOLORRGBA:
                case FieldConstants.MFCOLORRGBA:
                case FieldConstants.SFVEC3D:
                case FieldConstants.MFVEC3D:
                case FieldConstants.SFVEC4F:
                case FieldConstants.MFVEC4F:
                case FieldConstants.SFVEC4D:
                case FieldConstants.MFVEC4D:
                case FieldConstants.SFMATRIX3F:
                case FieldConstants.MFMATRIX3F:
                case FieldConstants.SFMATRIX4F:
                case FieldConstants.MFMATRIX4F:
                case FieldConstants.SFMATRIX3D:
                case FieldConstants.MFMATRIX3D:
                case FieldConstants.SFMATRIX4D:
                case FieldConstants.MFMATRIX4D:
                case FieldConstants.MFIMAGE:
                    throw new InvalidFieldException(VRML97_FIELD_MSG +
                                                    field.getFieldTypeString() +
                                                    " field name " +
                                                    field.getName());
                default:
                    // Do nothing.
            }
        }

        int field_index = super.appendField(field);
        fieldChangedMap.put(field_index, Boolean.FALSE);

        // now, what is the type? Set up default values just in case we have
        // something like an IS or script attempting to read a value
        switch(field.getFieldType()) {
            case FieldConstants.SFINT32:
                fieldValueMap.put(field_index, new Integer(0));
                fieldSizeMap.put(field_index, SINGLE_SIZE);
                break;

            case FieldConstants.MFINT32:
                fieldValueMap.put(field_index, FieldConstants.EMPTY_MFINT32);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.SFFLOAT:
                fieldValueMap.put(field_index, new Float(0));
                fieldSizeMap.put(field_index, SINGLE_SIZE);
                break;

            case FieldConstants.SFDOUBLE:
                fieldValueMap.put(field_index, new Double(0));
                fieldSizeMap.put(field_index, SINGLE_SIZE);
                break;

            case FieldConstants.SFLONG:
                fieldValueMap.put(field_index, new Long(0));
                fieldSizeMap.put(field_index, SINGLE_SIZE);
                break;

            case FieldConstants.MFLONG:
                fieldValueMap.put(field_index,  FieldConstants.EMPTY_MFLONG);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.SFBOOL:
                fieldValueMap.put(field_index, Boolean.TRUE);
                fieldSizeMap.put(field_index, SINGLE_SIZE);
                break;

            case FieldConstants.MFBOOL:
                fieldValueMap.put(field_index,  FieldConstants.EMPTY_MFBOOL);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.SFTIME:
                fieldValueMap.put(field_index, new Double(0));
                fieldSizeMap.put(field_index, SINGLE_SIZE);
                break;

            case FieldConstants.SFNODE:
                fieldValueMap.put(field_index, null);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.MFNODE:
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

// TODO:
// Not sure that this is a correct assumption. We probably should set the size
// to be 3, 4 or whatever as these sorts of fields always have some value
// defined, even if it is a default.
            case FieldConstants.SFCOLOR:
                fieldValueMap.put(field_index, new float[3]);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.SFCOLORRGBA:
                fieldValueMap.put(field_index, new float[4]);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.SFSTRING:
                fieldValueMap.put(field_index, "");
                fieldSizeMap.put(field_index, SINGLE_SIZE);
                break;

            case FieldConstants.MFSTRING:
                fieldValueMap.put(field_index,  FieldConstants.EMPTY_MFSTRING);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.MFFLOAT:
            case FieldConstants.SFVEC2F:
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFVEC4F:
            case FieldConstants.SFROTATION:
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFVEC2F:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFVEC4F:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFMATRIX3F:
            case FieldConstants.MFMATRIX4F:
                fieldValueMap.put(field_index, new float[0]);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.MFDOUBLE:
            case FieldConstants.MFTIME:
            case FieldConstants.SFVEC3D:
            case FieldConstants.MFVEC3D:
            case FieldConstants.SFMATRIX3D:
            case FieldConstants.MFMATRIX3D:
            case FieldConstants.SFMATRIX4D:
            case FieldConstants.MFMATRIX4D:
                fieldValueMap.put(field_index, new double[0]);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.SFIMAGE:
                fieldValueMap.put(field_index, new int[3]);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;

            case FieldConstants.MFIMAGE:
                fieldValueMap.put(field_index,  FieldConstants.EMPTY_MFIMAGE);
                fieldSizeMap.put(field_index, ZERO_SIZE);
                break;
        }

        return field_index;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
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
    public void setVersion(int major, int minor, boolean isStatic) {
        majorVersion = major;
        minorVersion = minor;

        this.isStatic = isStatic;

        isVrml97 = (major == 2);
    }

    /**
     * Set arbitrary data for a given field. Provided primarily to help the
     * EAI fullfil its requirements, but may be useful elsewhere.
     *
     * @param index The index of destination field to set
     * @param data The item to store for the field
     * @throws InvalidFieldException The field index is not known
     */
    public void setUserData(int index, Object data)
        throws InvalidFieldException {

        if(index < 0 || index > hasChanged.length + fieldCount)
            throw new InvalidFieldException("Invalid index in setUserData");

        userData.put(index, data);
    }

    /**
     * Fetch the stored user data for a given field index. If nothing is
     * registered, null is returned.
     *
     * @param index The index of destination field to set
     * @return The item stored for the field or null
     * @throws InvalidFieldException The field index is not known
     */
    public Object getUserData(int index) throws InvalidFieldException {
        if(index < 0 || index > hasChanged.length + fieldCount)
            throw new InvalidFieldException("Invalid index in getUserData");

        return userData.get(index);
    }

    //----------------------------------------------------------
    // Internal convenience  methods
    //----------------------------------------------------------

    /**
     * Copy this node into the given node. Used as the basis of a copy
     * constructor. A set of field names to ignore is included so that scripts
     * can make use of this method without extra work.
     *
     * @param node The node that is being copied
     */
    protected void copy(BaseScript node) {

        List fields = node.getAllFields();
        int size = fields.size();

        for(int i = 0; i < size; i++) {
            VRMLFieldDeclaration decl =
                (VRMLFieldDeclaration)fields.get(i);

            if((decl == null) || forbiddenFields.contains(decl.getName()))
                continue;

            try {
                int dest_index = appendField(decl);

                // only copy the values of fields and exposedFields
                int access_type = decl.getAccessType();

                if((access_type == FieldConstants.EVENTIN) ||
                   (access_type == FieldConstants.EVENTOUT))
                    continue;

                VRMLFieldData src_data = node.getFieldValue(i);
                copyField(i, src_data);
            } catch(FieldException fe) {
                System.out.println("Error copying field in abstract node");
                fe.printStackTrace();
            }
        }
    }

    /**
     * Set the value in the given node.
     *
     * @param destNode The destination node to call setValue() on
     * @param index The field index to call
     * @param value The value representation
     * @throws InvalidFieldException The node does not have that field index
     */
    private void sendValue(VRMLNodeType destNode,
                           int index,
                           Object value,
                           int numValid)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLFieldDeclaration decl = destNode.getFieldDeclaration(index);

        switch(decl.getFieldType()) {
            case FieldConstants.SFINT32:
                destNode.setValue(index, ((Integer)value).intValue());
                break;

            case FieldConstants.MFINT32:
            case FieldConstants.SFIMAGE:
            case FieldConstants.MFIMAGE:
                destNode.setValue(index, (int[])value, numValid);
                break;

            case FieldConstants.SFFLOAT:
                destNode.setValue(index, ((Float)value).floatValue());
                break;

            case FieldConstants.MFFLOAT:
            case FieldConstants.SFCOLOR:
            case FieldConstants.SFVEC3F:
            case FieldConstants.SFVEC2F:
            case FieldConstants.SFVEC4F:
            case FieldConstants.SFROTATION:
            case FieldConstants.SFMATRIX3F:
            case FieldConstants.SFMATRIX4F:
                destNode.setValue(index, (float[])value, numValid);
                break;

            case FieldConstants.SFDOUBLE:
                destNode.setValue(index, ((Double)value).doubleValue());
                break;

            case FieldConstants.SFVEC3D:
            case FieldConstants.SFVEC4D:
            case FieldConstants.SFMATRIX3D:
            case FieldConstants.SFMATRIX4D:
            case FieldConstants.MFDOUBLE:
                destNode.setValue(index, (double[])value, numValid);
                break;

            case FieldConstants.SFLONG:
                destNode.setValue(index, ((Long)value).longValue());
                break;

            case FieldConstants.MFLONG:
                destNode.setValue(index, (long[])value, numValid);
                break;

            case FieldConstants.SFBOOL:
                destNode.setValue(index, ((Boolean)value).booleanValue());
                break;

            case FieldConstants.MFBOOL:
                destNode.setValue(index, (boolean[])value, numValid);
                break;

            case FieldConstants.SFSTRING:
                destNode.setValue(index, (String)value);
                break;

            case FieldConstants.MFSTRING:
                destNode.setValue(index, (String[])value, numValid);
                break;

            case FieldConstants.SFTIME:
                destNode.setValue(index, ((Double)value).doubleValue());
                break;

            case FieldConstants.MFTIME:
                destNode.setValue(index, (double[])value, numValid);
                break;

            case FieldConstants.SFNODE:
                destNode.setValue(index, (VRMLNodeType)value);
                break;

            case FieldConstants.MFNODE:
                destNode.setValue(index, (VRMLNodeType[])value, numValid);
                break;

            case FieldConstants.MFVEC2F:
            case FieldConstants.MFVEC3F:
            case FieldConstants.MFVEC4F:
            case FieldConstants.MFCOLOR:
            case FieldConstants.MFCOLORRGBA:
            case FieldConstants.MFROTATION:
            case FieldConstants.MFMATRIX3F:
            case FieldConstants.MFMATRIX4F:
                destNode.setValue(index, (float[])value, numValid);
                break;

            case FieldConstants.MFVEC3D:
            case FieldConstants.MFVEC4D:
            case FieldConstants.MFMATRIX3D:
            case FieldConstants.MFMATRIX4D:
                destNode.setValue(index, (double[])value, numValid);
                break;

            default:
                System.out.println("Unhandled case in Script.sendValue" +
                                    decl.getFieldTypeString());
        }
    }

    /**
     * Fetch the value of the named field and build a data description of it.
     *
     * @param index The field index to call
     * @return true if this is a valid value, false for an eventOut
     * @throws InvalidFieldException The node does not have that field index
     */
    private boolean fetchValue(int index) throws InvalidFieldException {
        VRMLFieldData fieldData = fieldLocalData.get();

        VRMLFieldDeclaration decl = getFieldDeclaration(index);

        if(decl == null)
            throw new InvalidFieldException(nodeName, index);

        Object value = fieldValueMap.get(index);
        Integer i_size = (Integer)fieldSizeMap.get(index);
        int num_elements = i_size == null ? 0 : i_size.intValue();

        switch(decl.getFieldType()) {
            case FieldConstants.SFINT32:
                fieldData.intValue = ((Integer)value).intValue();
                fieldData.dataType = VRMLFieldData.INT_DATA;
                break;

            case FieldConstants.MFINT32:
                fieldData.intArrayValue = (int[])value;
                fieldData.numElements = num_elements;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                break;

            case FieldConstants.SFFLOAT:
                fieldData.floatValue = ((Float)value).floatValue();
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FieldConstants.MFFLOAT:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = num_elements;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.SFDOUBLE:
                fieldData.doubleValue = ((Double)value).doubleValue();
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FieldConstants.MFDOUBLE:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = num_elements;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.SFLONG:
                fieldData.longValue = ((Long)value).longValue();
                fieldData.dataType = VRMLFieldData.LONG_DATA;
                break;

            case FieldConstants.MFLONG:
                fieldData.longArrayValue = (long[])value;
                fieldData.numElements = num_elements;
                fieldData.dataType = VRMLFieldData.LONG_ARRAY_DATA;
                break;

            case FieldConstants.SFBOOL:
                fieldData.booleanValue = ((Boolean)value).booleanValue();
                fieldData.dataType = VRMLFieldData.BOOLEAN_DATA;
                break;

            case FieldConstants.MFBOOL:
                fieldData.booleanArrayValue = (boolean[])value;
                fieldData.numElements = num_elements;
                fieldData.dataType = VRMLFieldData.BOOLEAN_ARRAY_DATA;
                break;

            case FieldConstants.SFTIME:
                fieldData.doubleValue = ((Double)value).doubleValue();
                fieldData.dataType = VRMLFieldData.DOUBLE_DATA;
                break;

            case FieldConstants.MFTIME:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = num_elements;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.SFSTRING:
                fieldData.stringValue = (String)value;
                fieldData.dataType = VRMLFieldData.STRING_DATA;
                break;

            case FieldConstants.MFSTRING:
                if (value instanceof String[]) {
                    fieldData.stringArrayValue = (String[])value;
                    fieldData.numElements = num_elements;
                    fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                }
                else {
                    fieldData.stringValue = (String)value;
                    fieldData.dataType = VRMLFieldData.STRING_DATA;
                }
                break;

            case FieldConstants.SFNODE:
                fieldData.nodeValue = (VRMLNodeType)value;
                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FieldConstants.MFNODE:
                // An mfnode may be a single or array object
                if(value instanceof VRMLNodeType) {
                    fieldData.nodeArrayValue = new VRMLNodeType[1];
                    fieldData.nodeArrayValue[0] = (VRMLNodeType)value;
                } else {
                    fieldData.nodeArrayValue = (VRMLNodeType[])value;
                }
                fieldData.numElements = num_elements;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                break;

            case FieldConstants.SFVEC2F:
                fieldData.floatArrayValue = (float[])value;
                if((value == null) || fieldData.floatArrayValue.length < 2)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.MFVEC2F:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = num_elements / 2;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.SFCOLOR:
            case FieldConstants.SFVEC3F:
                fieldData.floatArrayValue = (float[])value;
                if((value == null) || fieldData.floatArrayValue.length < 3)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.SFROTATION:
            case FieldConstants.SFCOLORRGBA:
            case FieldConstants.SFVEC4F:
                fieldData.floatArrayValue = (float[])value;
                if((value == null) || fieldData.floatArrayValue.length < 4)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.MFCOLOR:
            case FieldConstants.MFVEC3F:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = num_elements / 3;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.MFROTATION:
            case FieldConstants.MFVEC4F:
            case FieldConstants.MFCOLORRGBA:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = num_elements / 4;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.SFVEC2D:
                fieldData.doubleArrayValue = (double[])value;
                if((value == null) || fieldData.doubleArrayValue.length < 2)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.SFVEC3D:
                fieldData.doubleArrayValue = (double[])value;

                if((value == null) || fieldData.doubleArrayValue.length < 3)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = 1;

                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.SFVEC4D:
                fieldData.doubleArrayValue = (double[])value;
                if((value == null) || fieldData.doubleArrayValue.length < 4)
                    fieldData.numElements = 0;
                else
                    fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.MFVEC2D:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = num_elements / 2;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.MFVEC3D:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = num_elements / 3;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;


            case FieldConstants.MFVEC4D:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = num_elements / 4;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.SFMATRIX3F:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;


            case FieldConstants.SFMATRIX4F:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.SFMATRIX3D:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;


            case FieldConstants.SFMATRIX4D:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = 1;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.MFMATRIX3F:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = num_elements / 9;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;


            case FieldConstants.MFMATRIX4F:
                fieldData.floatArrayValue = (float[])value;
                fieldData.numElements = num_elements / 16;
                fieldData.dataType = VRMLFieldData.FLOAT_ARRAY_DATA;
                break;

            case FieldConstants.MFMATRIX3D:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = num_elements / 9;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;


            case FieldConstants.MFMATRIX4D:
                fieldData.doubleArrayValue = (double[])value;
                fieldData.numElements = num_elements / 16;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                break;

            case FieldConstants.SFIMAGE:
            case FieldConstants.MFIMAGE:
                fieldData.intArrayValue = (int[])value;
                fieldData.numElements = (value != null) ?
                    fieldData.intArrayValue.length :
                    0;
                fieldData.dataType = VRMLFieldData.INT_ARRAY_DATA;
                break;
        }

        return true;
    }

    /**
     * Process a single field for copying data from this node to the
     * destination node.
     *
     * @param node The node reference we are sending the value to
     * @param field The index of the field to set the data for
     * @param data The source data used to set the field
     * @throws FieldException Any one of the normal field exceptions
     */
    private void copyField(int field, VRMLFieldData data)
        throws FieldException {

        int num_items = 0;

        switch(data.dataType) {
            case VRMLFieldData.BOOLEAN_DATA:
                setValue(field, data.booleanValue);
                break;

            case VRMLFieldData.BOOLEAN_ARRAY_DATA:
                boolean[] bools = null;

                if(data.numElements != 0) {
                    bools = new boolean[data.numElements];
                    System.arraycopy(data.booleanArrayValue,
                                     0,
                                     bools,
                                     0,
                                     data.numElements);
                }
                setValue(field, bools, data.numElements);
                break;

            case VRMLFieldData.INT_DATA:
                setValue(field, data.intValue);
                break;

            case VRMLFieldData.INT_ARRAY_DATA:
                int[] ints = null;

                if(data.numElements != 0) {
                    num_items = data.intArrayValue.length;
                    ints = new int[num_items];
                    System.arraycopy(data.intArrayValue,
                                     0,
                                     ints,
                                     0,
                                     num_items);
                }
                setValue(field, ints, num_items);
                break;

            case VRMLFieldData.LONG_DATA:
                setValue(field, data.longValue);
                break;

            case VRMLFieldData.LONG_ARRAY_DATA:
                long[] longs = null;

                if(data.numElements != 0) {
                    num_items = data.longArrayValue.length;
                    longs = new long[num_items];
                    System.arraycopy(data.longArrayValue,
                                     0,
                                     longs,
                                     0,
                                     num_items);
                }
                setValue(field, longs, num_items);
                break;

            case VRMLFieldData.FLOAT_DATA:
                setValue(field, data.floatValue);
                break;

// TODO:
// Not quite right on num_items. Should be multiplied by the underlying data size,
// for the numElements - eg 3 for vec3f, 9 for matrix3f etc.
            case VRMLFieldData.FLOAT_ARRAY_DATA:
                float[] floats = null;

                if(data.numElements != 0) {
                    num_items = data.floatArrayValue.length;
                    floats = new float[num_items];
                    System.arraycopy(data.floatArrayValue,
                                     0,
                                     floats,
                                     0,
                                     num_items);
                }
                setValue(field, floats, num_items);
                break;

            case VRMLFieldData.DOUBLE_DATA:
                setValue(field, data.doubleValue);
                break;

// TODO:
// Not quite right on num_items. Should be multiplied by the underlying data size,
// for the numElements - eg 3 for vec3d, 9 for matrix3d etc.
            case VRMLFieldData.DOUBLE_ARRAY_DATA:
                double[] dbles = null;

                if(data.numElements != 0) {
                    num_items = data.doubleArrayValue.length;
                    dbles = new double[num_items];
                    System.arraycopy(data.doubleArrayValue,
                                     0,
                                     dbles,
                                     0,
                                     num_items);
                }
                setValue(field, dbles, num_items);
                break;

            case VRMLFieldData.STRING_DATA:
                setValue(field, data.stringValue);
                break;

            case VRMLFieldData.STRING_ARRAY_DATA:
                String[] strs = null;

                if(data.numElements != 0) {
                    strs = new String[data.numElements];
                    System.arraycopy(data.stringArrayValue,
                                     0,
                                     strs,
                                     0,
                                     data.numElements);
                }
                setValue(field, strs, data.numElements);
                break;

            // for these two, we don't do anything. The assumption is that
            // the code wanting to make the copy is doing the scenegraph
            // traversal for us. That is, it will query the source node for
            // SF/MFNode fields and deal with those separately. This matches
            // with the other nodes like Group that don't copy their children.
            case VRMLFieldData.NODE_DATA:
            case VRMLFieldData.NODE_ARRAY_DATA:
                break;
        }
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

    /**
     * Convenience method to see if this is a field that should be sent along
     * to the script wrapper. Used to work out if it was an
     * eventOut/exposedField in the script.
     *
     * @param index The field index this belongs to
     * @param decl The field declaration for the field
     * @return true if this should allow an update to happen
     */
    private boolean shouldUpdate(int index, VRMLFieldDeclaration decl) {

        boolean ret_val = true;

        if(eventOutProcessingInProgress) {
            switch(decl.getAccessType()) {
                case FieldConstants.EVENTIN:
                case FieldConstants.EXPOSEDFIELD:
                    Boolean bool = (Boolean)fieldChangedMap.get(index);
                    ret_val = !bool.booleanValue();
                    break;

                case FieldConstants.FIELD:
                case FieldConstants.EVENTOUT:
                    ret_val = false;
                    break;
            }
        }

        return ret_val;
    }
}
