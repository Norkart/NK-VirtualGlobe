/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004-2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.nodes.geospatial;

// External imports
import java.util.HashMap;
import java.util.ArrayList;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.HashSet;
import org.web3d.vrml.renderer.common.nodes.BaseGroupingNode;
import org.web3d.vrml.renderer.common.geospatial.GTTransformUtils;
import org.web3d.vrml.util.URLChecker;

/**
 * Common base implementation of a GeoLOD node.
 * <p>
 *
 * Internally the LOD keeps both the basic range and the values squared. This
 * makes computation much faster, eliminating the need to take expensive
 * square-roots each frame.
 *
 * The basic (X3D) definition of GeoLOD is:
 * <pre>
 *  GeoLOD : X3DGroupingNode {
 *    SFNode   [in,out] metadata       NULL       [X3DMetadataObject]
 *    MFNode   [out]    children       []         [X3DChildNode]
 *    SFInt32  [out]    level_changed
 *    SFVec3d  []       center         0 0 0      (-inf,inf)
 *    MFString []       child1Url      []         [<i>urn</i>]
 *    MFString []       child2Url      []         [<i>urn</i>]
 *    MFString []       child3Url      []         [<i>urn</i>]
 *    MFString []       child4Url      []         [<i>urn</i>]
 *    MFNode   []       geoOrigin      NULL       [GeoOrigin]
 *    MFString []       geoSystem      ["GD",WE"]
 *    SFFloat  []       range          10         [0,inf)
 *    MFString []       rootUrl        []         [<i>urn</i>]
 *    MFNode   []       rootNode       NULL       [X3DChildNode]
 *    SFVec3f  []       bboxCenter     0 0 0      (-inf,inf)
 *    SFVec3f  []       bboxSize       -1 -1 -1   [0,inf) or -1 -1 -1
 * }
 * </pre>
 *
 * @author Justin Couch
 * @version $Revision: 1.15 $
 */
public abstract class BaseGeoLOD extends BaseGroupingNode
    implements VRMLViewDependentNodeType, VRMLMultiExternalNodeType {

    /** Secondary type constant */
    private static final int[] SECONDARY_TYPE = {
        TypeConstants.MultiExternalNodeType,
        TypeConstants.ViewDependentNodeType
    };

    /** Index of the rootNode field */
    protected static final int FIELD_ROOT_NODE = LAST_GROUP_INDEX + 1;

    /** Index of the rootUrl field */
    protected static final int FIELD_ROOT_URL = LAST_GROUP_INDEX + 2;

    /** Index of the child1Url field */
    protected static final int FIELD_CHILD1_URL = LAST_GROUP_INDEX + 3;

    /** Index of the child2Url field */
    protected static final int FIELD_CHILD2_URL = LAST_GROUP_INDEX + 4;

    /** Index of the child3Url field */
    protected static final int FIELD_CHILD3_URL = LAST_GROUP_INDEX + 5;

    /** Index of the child4Url field */
    protected static final int FIELD_CHILD4_URL = LAST_GROUP_INDEX + 6;

    /** Index of the geoOrigin field */
    protected static final int FIELD_GEO_ORIGIN = LAST_GROUP_INDEX + 7;

    /** Index of the geoSystem field */
    protected static final int FIELD_GEO_SYSTEM = LAST_GROUP_INDEX + 8;

    /** Index of the center field */
    protected static final int FIELD_CENTER = LAST_GROUP_INDEX + 9;

    /** The index of the range field */
    protected static final int FIELD_RANGE = LAST_GROUP_INDEX + 10;

    /** The index of the level_changed field */
    protected static final int FIELD_LEVEL_CHANGED = LAST_GROUP_INDEX + 11;

    /** The last field index used by this class */
    protected static final int LAST_LOD_INDEX = FIELD_LEVEL_CHANGED;

    /** Number of fields constant */
    protected static final int NUM_FIELDS = LAST_LOD_INDEX + 1;

    /** Message for when the proto is not a GeoOrigin */
    private static final String GEO_ORIGIN_PROTO_MSG =
        "Proto does not describe a GeoOrigin object";

    /** Message for when the node in setValue() is not a GeoOrigin */
    private static final String GEO_ORIGIN_NODE_MSG =
        "Node does not describe a GeoOrigin object";

    /** Message during setupFinished() when geotools issues an error */
    private static final String FACTORY_ERR_MSG =
        "Unable to create an appropriate set of operations for the defined " +
        "geoSystem setup. May be either user or tools setup error";

    /** Message when the mathTransform.transform() fails */
    private static final String TRANSFORM_ERR_MSG =
        "Unable to transform the coordinate values for some reason.";

    /** Message when the range provided is negative */
    private static final String NEG_RANGE_MSG =
        "GeoLOD range value provided is negative. Must be [0,oo): ";

    /** Array of VRMLFieldDeclarations */
    private static final VRMLFieldDeclaration[] fieldDecl;

    /** Hashmap between a field name and its index */
    private static final HashMap fieldMap;

    /** Indices of the fields that are MFNode or SFnode */
    private static final int[] nodeFields;

    /** The array of fields that need URL content */
    private static final int[] urlFieldIndexList;
    private static final int[] urlFieldChildrenIndexList;
    private static final int[] urlFieldRootIndexList;

    /** Valid set of mime types for X3D support */
    private static final HashSet validX3DTypes;

    // VRML Field declarations

    /** field SFVec3d center */
    protected double[] vfCenter;

    /** field SFFloat range */
    protected float vfRange;

    /** field MFString child1Url */
    protected String[] vfChild1Url;

    /** field MFString child2Url */
    protected String[] vfChild2Url;

    /** field MFString child3Url */
    protected String[] vfChild3Url;

    /** field MFString child4Url */
    protected String[] vfChild4Url;

    /** field MFString rootUrl */
    protected String[] vfRootUrl;

    /** field MFString geoSystem ["GD","WE"] */
    protected String[] vfGeoSystem;

    /** Proto version of the geoOrigin */
    protected VRMLProtoInstance pGeoOrigin;

    /** field SFNode geoOrigin */
    protected VRMLNodeType vfGeoOrigin;

    /** field MFNode rootNode */
    protected ArrayList vfRootNode;

    /** The value of the outputOnly field level_changed */
    protected int vfLevelChanged;

    /** Internal scratch var for dealing with added/removed children */
    protected VRMLNodeType[] nodeTmp;

    /** Local version of the center position post coord conversion */
    protected double[] localCenter;

    /** The world URL for correcting relative URL values */
    protected String worldURL;

    /** Flag to indicate if we've checked the URLs for relative references */
    protected boolean urlRelativeCheck;

    /** The state of the load for the various fields */
    protected int[] loadState;

    /** The index of the currently active object */
    protected boolean childrenShown;

    /** List of loaded URI strings */
    protected String[] loadedUri;

    /** List of those who want to know about Url changes. Likely 1 */
    private ArrayList urlListeners;

    /** List of those who want to know about content state changes. Likely 1 */
    private ArrayList contentListeners;

    /** The scenes that represents the childXUrl after loading. */
    protected VRMLScene[] childScenes;

    /** The scene wrappers for loaded scenes */
    protected SceneWrapper[] loadedScenes;

    /** The main scene that represents the rootUrl after loading. */
    protected VRMLScene rootScene;

    /** The scene wrapper for the root */
    protected SceneWrapper rootSceneWrapper;


    /**
     * Static constructor builds the type lists for use by all instances as
     * well as the field handling.
     */
    static {
        nodeFields = new int[] {
            FIELD_CHILDREN,
            FIELD_METADATA,
            FIELD_GEO_ORIGIN,
            FIELD_ROOT_NODE
        };

        fieldDecl = new VRMLFieldDeclaration[NUM_FIELDS];
        fieldMap = new HashMap(NUM_FIELDS);

        fieldDecl[FIELD_METADATA] =
            new VRMLFieldDeclaration(FieldConstants.EXPOSEDFIELD,
                                     "SFNode",
                                     "metadata");
        fieldDecl[FIELD_CHILDREN] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "MFNode",
                                     "children");
		fieldDecl[FIELD_LEVEL_CHANGED] =
            new VRMLFieldDeclaration(FieldConstants.EVENTOUT,
                                     "SFInt32",
                                     "level_changed");
        fieldDecl[FIELD_BBOX_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxCenter");
        fieldDecl[FIELD_BBOX_SIZE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3f",
                                     "bboxSize");
        fieldDecl[FIELD_CENTER] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFVec3d",
                                     "center");
        fieldDecl[FIELD_RANGE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFFloat",
                                     "range");
        fieldDecl[FIELD_GEO_SYSTEM] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "geoSystem");
        fieldDecl[FIELD_GEO_ORIGIN] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "SFNode",
                                     "geoOrigin");
        fieldDecl[FIELD_ROOT_URL] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "rootUrl");
        fieldDecl[FIELD_CHILD1_URL] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "child1Url");
        fieldDecl[FIELD_CHILD2_URL] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "child2Url");
        fieldDecl[FIELD_CHILD3_URL] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "child3Url");
        fieldDecl[FIELD_CHILD4_URL] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFString",
                                     "child4Url");
        fieldDecl[FIELD_ROOT_NODE] =
            new VRMLFieldDeclaration(FieldConstants.FIELD,
                                     "MFNode",
                                     "rootNode");


        Integer idx = new Integer(FIELD_METADATA);
        fieldMap.put("metadata", idx);
        fieldMap.put("set_metadata", idx);
        fieldMap.put("metadata_changed", idx);

        idx = new Integer(FIELD_CHILDREN);
        fieldMap.put("children", idx);
        fieldMap.put("children_changed", idx);

        fieldMap.put("level_changed",new Integer(FIELD_LEVEL_CHANGED));

        fieldMap.put("bboxCenter",new Integer(FIELD_BBOX_CENTER));
        fieldMap.put("bboxSize",new Integer(FIELD_BBOX_SIZE));

        fieldMap.put("center",new Integer(FIELD_CENTER));
        fieldMap.put("range",new Integer(FIELD_RANGE));
        fieldMap.put("geoSystem", new Integer(FIELD_GEO_SYSTEM));
        fieldMap.put("geoOrigin", new Integer(FIELD_GEO_ORIGIN));
        fieldMap.put("rootNode",new Integer(FIELD_ROOT_NODE));
        fieldMap.put("rootUrl",new Integer(FIELD_ROOT_URL));
        fieldMap.put("child1Url",new Integer(FIELD_CHILD1_URL));
        fieldMap.put("child2Url",new Integer(FIELD_CHILD2_URL));
        fieldMap.put("child3Url",new Integer(FIELD_CHILD3_URL));
        fieldMap.put("child4Url",new Integer(FIELD_CHILD4_URL));

        urlFieldIndexList = new int[5];
        urlFieldIndexList[0] = FIELD_ROOT_URL;
        urlFieldIndexList[1] = FIELD_CHILD1_URL;
        urlFieldIndexList[2] = FIELD_CHILD2_URL;
        urlFieldIndexList[3] = FIELD_CHILD3_URL;
        urlFieldIndexList[4] = FIELD_CHILD4_URL;


        urlFieldChildrenIndexList = new int[4];
        urlFieldChildrenIndexList[0] = FIELD_CHILD1_URL;
        urlFieldChildrenIndexList[1] = FIELD_CHILD2_URL;
        urlFieldChildrenIndexList[2] = FIELD_CHILD3_URL;
        urlFieldChildrenIndexList[3] = FIELD_CHILD4_URL;

        urlFieldRootIndexList = new int[1];
        urlFieldRootIndexList[0] = FIELD_ROOT_URL;

        validX3DTypes = new HashSet();

        validX3DTypes.add("model/x3d+vrml");
        validX3DTypes.add("model/x3d+xml");
        validX3DTypes.add("model/x3d+binary");
    }

    /**
     * Construct a default instance of this node. The defaults are set by the
     * VRML specification.
     */
    protected BaseGeoLOD() {
        super("GeoLOD");

        hasChanged = new boolean[NUM_FIELDS];

        vfCenter = new double[3];
        vfRange = 10;
        vfChild1Url = FieldConstants.EMPTY_MFSTRING;
        vfChild2Url = FieldConstants.EMPTY_MFSTRING;
        vfChild3Url = FieldConstants.EMPTY_MFSTRING;
        vfChild4Url = FieldConstants.EMPTY_MFSTRING;
        vfRootUrl = FieldConstants.EMPTY_MFSTRING;

        vfGeoSystem = new String[] {"GD","WE"};
        vfRootNode = new ArrayList();
        localCenter = new double[3];
        urlRelativeCheck = false;
        loadState = new int[NUM_FIELDS];
        loadedUri = new String[NUM_FIELDS];
        urlListeners = new ArrayList(1);
        contentListeners = new ArrayList(1);
    }

    /**
     * Construct a new instance of this node based on the details from the
     * given node. If the node is not a group node, an exception will be
     * thrown. It does not copy the children nodes, just this node.
     *
     * @param node The node to copy
     * @throws IllegalArgumentException The node is not a Group node
     */
    protected BaseGeoLOD(VRMLNodeType node) {
        this();

        checkNodeType(node);

        copy((VRMLGroupingNodeType)node);

        try {
            int index = node.getFieldIndex("center");
            VRMLFieldData field = node.getFieldValue(index);

            vfCenter[0] = field.doubleArrayValue[0];
            vfCenter[1] = field.doubleArrayValue[1];
            vfCenter[2] = field.doubleArrayValue[2];

            index = node.getFieldIndex("range");
            field = node.getFieldValue(index);
            vfRange = field.floatValue;

            index = node.getFieldIndex("geoSystem");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfGeoSystem = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfGeoSystem, 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("child1Url");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfChild1Url = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfChild1Url, 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("child2Url");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfChild2Url = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfChild2Url, 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("child3Url");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfChild3Url = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfChild3Url, 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("child4Url");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfChild4Url = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfChild4Url, 0,
                                 field.numElements);
            }

            index = node.getFieldIndex("rootUrl");
            field = node.getFieldValue(index);
            if(field.numElements != 0) {
                vfRootUrl = new String[field.numElements];
                System.arraycopy(field.stringArrayValue, 0, vfRootUrl, 0,
                                 field.numElements);
            }
        } catch(VRMLException ve) {
            throw new IllegalArgumentException(ve.getMessage());
        }
    }

    //-------------------------------------------------------------
    // Methods defined by VRMLGroupingNodeType
    //-------------------------------------------------------------

    /**
     * Adjust the sharing count up or down one increment depending on the flag.
     *
     * @param used true if this is about to have another reference added
     */
    public void setShared(boolean used) {
        super.setShared(used);


        int size = vfRootNode.size();

        VRMLNodeType kid;

        for(int i = 0; i < size; i++) {
            kid = (VRMLNodeType)vfRootNode.get(i);

            if(kid instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)kid).setShared(used);
        }
    }

    /**
     * Change the reference count up or down by one for a given layer ID.
     * If there is no reference to the given layer ID previously, add one
     * now. A listing of the layer IDs that reference this node can be
     * retrieved through {@link #getLayerIds()}.
     *
     * @param layer The id of the layer to modify the ref count on
     * @param add true to increment the reference count, false to decrement
     */
    public void updateRefCount(int layer, boolean add) {
        super.updateRefCount(layer, add);

        if(layerIds == null)
            return;

        int size = vfRootNode.size();

        VRMLNodeType kid;

        for(int i = 0; i < size; i++) {
            kid = (VRMLNodeType)vfRootNode.get(i);

            updateRefs(kid, add);
        }
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
//System.out.println("***Getting url list: " + this + " childrenShown: " + childrenShown);

//        return urlFieldIndexList;

        if (childrenShown)
            return urlFieldChildrenIndexList;
        else
            return urlFieldRootIndexList;
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
            case FIELD_ROOT_URL:
                ret_val = vfRootUrl;
                break;

            case FIELD_CHILD1_URL:
                ret_val = vfChild1Url;
                break;

            case FIELD_CHILD2_URL:
                ret_val = vfChild2Url;
                break;

            case FIELD_CHILD3_URL:
                ret_val = vfChild3Url;
                break;

            case FIELD_CHILD4_URL:
                ret_val = vfChild4Url;
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
		switch (index) {
		case FIELD_CHILD1_URL:
		case FIELD_CHILD2_URL:
		case FIELD_CHILD3_URL:
		case FIELD_CHILD4_URL:
			if (childrenShown) {
        		return(loadState[index]);
			} else {
				return(VRMLExternalNodeType.LOAD_COMPLETE);
			}
		default:
			return(loadState[index]);
		}
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
        // Only accept content that is X3D.
        return validX3DTypes.contains(mimetype);
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

        // Is this used anywhere?
        return null;
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
     * Notification that the construction phase of this node has finished.
     * If the node would like to do any internal processing, such as setting
     * up geometry, then go for it now.
     */
    public void setupFinished() {
        if(!inSetup)
            return;

        super.setupFinished();

        if(pGeoOrigin != null)
            pGeoOrigin.setupFinished();
        else if(vfGeoOrigin != null)
            vfGeoOrigin.setupFinished();

        checkURLs();

        // Fetch the geo transform and shift the first set of points
        try {
            GTTransformUtils gtu = GTTransformUtils.getInstance();
            boolean[] swap = new boolean[1];

            MathTransform transform  = gtu.createSystemTransform(vfGeoSystem, swap);

            if(swap[0]) {
                double tmp = vfCenter[0];
                vfCenter[0] = vfCenter[1];
                vfCenter[1] = tmp;
                transform.transform(vfCenter, 0, localCenter, 0, 1);

                tmp = vfCenter[0];
                vfCenter[0] = vfCenter[1];
                vfCenter[1] = tmp;
			} else {
                transform.transform(vfCenter, 0, localCenter, 0, 1);
			}

            if(vfGeoOrigin != null) {
                double[] pos = ((BaseGeoOrigin)vfGeoOrigin).getConvertedCoordRef();
                localCenter[0] -= pos[0];
                localCenter[1] -= pos[1];
                localCenter[2] -= pos[2];
            }
        } catch(FactoryException fe) {
            errorReporter.errorReport(FACTORY_ERR_MSG, fe);
        } catch(TransformException te) {
            errorReporter.warningReport(TRANSFORM_ERR_MSG, te);
        }

        int num_kids = vfRootNode.size();
        VRMLNodeType kid;

        for(int i = 0; i < num_kids; i++) {
            kid = (VRMLNodeType)vfRootNode.get(i);

            // Make sure the child is finished first.
            kid.setupFinished();
        }
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNode
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
        if(index < 0  || index > LAST_LOD_INDEX)
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
            case FIELD_CENTER:
                fieldData.clear();
                fieldData.doubleArrayValue = vfCenter;
                fieldData.dataType = VRMLFieldData.DOUBLE_ARRAY_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_RANGE:
                fieldData.clear();
                fieldData.floatValue = vfRange;
                fieldData.dataType = VRMLFieldData.FLOAT_DATA;
                break;

            case FIELD_LEVEL_CHANGED:
                fieldData.clear();
                fieldData.intValue = vfLevelChanged;
                fieldData.dataType = VRMLFieldData.INT_DATA;
                fieldData.numElements = 1;
                break;

            case FIELD_GEO_ORIGIN:
                fieldData.clear();
                if (pGeoOrigin != null)
                    fieldData.nodeValue = pGeoOrigin;
                else
                    fieldData.nodeValue = vfGeoOrigin;

                fieldData.dataType = VRMLFieldData.NODE_DATA;
                break;

            case FIELD_GEO_SYSTEM:
                fieldData.clear();
                fieldData.stringArrayValue = vfGeoSystem;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfGeoSystem.length;
                break;

            case FIELD_ROOT_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfRootUrl;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfRootUrl.length;
                break;

            case FIELD_CHILD1_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfChild1Url;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfChild1Url.length;
                break;

            case FIELD_CHILD2_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfChild2Url;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfChild2Url.length;
                break;

            case FIELD_CHILD3_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfChild3Url;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfChild3Url.length;
                break;

            case FIELD_CHILD4_URL:
                fieldData.clear();
                fieldData.stringArrayValue = vfChild4Url;
                fieldData.dataType = VRMLFieldData.STRING_ARRAY_DATA;
                fieldData.numElements = vfChild4Url.length;
                break;

            case FIELD_ROOT_NODE:
                int num_kids = vfRootNode.size();

                if((nodeTmp == null) || (nodeTmp.length < num_kids))
                    nodeTmp = new VRMLNodeType[num_kids];
                vfRootNode.toArray(nodeTmp);
                fieldData.clear();
                fieldData.nodeArrayValue = nodeTmp;
                fieldData.dataType = VRMLFieldData.NODE_ARRAY_DATA;
                fieldData.numElements = num_kids;
                break;

            default:
                super.getFieldValue(index);
        }

        return fieldData;
    }

    /**
     * Set the value of the field at the given index as an array of floats.
     * This would be used to set MFFloat, SFVec2f, SFVec3f and SFRotation
     * field types.
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @param numValid The number of valid values to copy from the array
     */
    public void setValue(int index, float value)
        throws InvalidFieldException, InvalidFieldValueException {

        switch(index) {
            case FIELD_RANGE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                         "range");

                if(value < 0)
                    throw new InvalidFieldValueException(NEG_RANGE_MSG + value);

                vfRange = value;

                break;

            default:
                super.setValue(index, value);
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
     */
    public void setValue(int index, double[] value, int numValid)
        throws InvalidFieldException {

        switch(index) {
            case FIELD_CENTER:
                setCenter(value);
                break;

            default:
                super.setValue(index, value, numValid);
        }
    }

    /**
     * Set the value of the field at the given index as an array of strings.
     * This would be used to set the MFString field type "type".
     *
     * @param index The index of destination field to set
     * @param value The new value to use for the node
     * @throws InvalidFieldException The field index is not know
     */
    public void setValue(int index, String[] value, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_GEO_SYSTEM:
               if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "geoSystem");

                if(vfGeoSystem.length != numValid)
                    vfGeoSystem = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfGeoSystem[i] = value[i];
                break;

            case FIELD_ROOT_URL:
               if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "rootUrl");

                if(vfRootUrl.length != numValid)
                    vfRootUrl= new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfRootUrl[i] = value[i];
                break;

            case FIELD_CHILD1_URL:
               if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "child1Url");

                if(vfChild1Url.length != numValid)
                    vfChild1Url = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfChild1Url[i] = value[i];
                break;

            case FIELD_CHILD2_URL:
               if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "child2Url");

                if(vfChild2Url.length != numValid)
                    vfChild2Url = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfChild2Url[i] = value[i];
                break;

            case FIELD_CHILD3_URL:
               if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "child3Url");

                if(vfChild3Url.length != numValid)
                    vfChild3Url = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfChild3Url[i] = value[i];
                break;

            case FIELD_CHILD4_URL:
               if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "child4Url");

                if(vfChild4Url.length != numValid)
                    vfChild4Url = new String[numValid];

                for(int i = 0; i < numValid; i++)
                    vfChild4Url[i] = value[i];
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
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    public void setValue(int index, VRMLNodeType child)
        throws InvalidFieldException, InvalidFieldValueException {

        VRMLNodeType node = child;

        switch(index) {
            case FIELD_GEO_ORIGIN:
                setGeoOrigin(child);
                break;

            case FIELD_ROOT_NODE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "rootNode");

                if(child != null)
                    addRootNode(child);

                break;

            default:
                super.setValue(index, child);
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
     * @throws InvalidFieldAccessException The call is attempting to write to
     *    a field that does not permit writing now
     */
    public void setValue(int index, VRMLNodeType[] children, int numValid)
        throws InvalidFieldException, InvalidFieldValueException,
               InvalidFieldAccessException {

        switch(index) {
            case FIELD_ROOT_NODE:
                if(!inSetup)
                    throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG +
                                                          "rootNode");
                if(!inSetup)
                    clearChildren();

                for(int i = 0; i < numValid; i++ )
                    addRootNode(children[i]);

                break;

            default:
                super.setValue(index, children, numValid);
        }
    }

    //----------------------------------------------------------
    // Internal methods
    //----------------------------------------------------------

    /**
     * Clear the child node list of all children in the VRML node. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     */
    protected void clearRootNodes() {
        int num_kids = vfRootNode.size();

        if((nodeTmp == null) || (nodeTmp.length < num_kids))
            nodeTmp = new VRMLNodeType[num_kids];

        vfRootNode.toArray(nodeTmp);

        for(int i = 0; i < num_kids; i++) {
            if(nodeTmp[i] instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)nodeTmp[i]).setShared(false);

            updateRefs(nodeTmp[i], false);
        }

        if (num_kids > 0)
            stateManager.registerRemovedNodes(nodeTmp);

        vfRootNode.clear();
        hasBindables = false;
    }

    /**
     * Set the center component of the of transform. Setting a value
     * of null is an error
     *
     * @param center The new center component
     * @throws InvalidFieldValueException The center was null
     */
    private void setCenter(double[] center)
        throws InvalidFieldValueException {

        if(!inSetup)
            throw new InvalidFieldValueException(INIT_ONLY_WRITE_MSG + "center");

        vfCenter[0] = center[0];
        vfCenter[1] = center[1];
        vfCenter[2] = center[2];
    }

    /**
     * Set node content for the geoOrigin node.
     *
     * @param geo The new geoOrigin
     * @throws InvalidFieldValueException The node does not match the required
     *    type.
     */
    private void setGeoOrigin(VRMLNodeType geo)
        throws InvalidFieldValueException, InvalidFieldAccessException {

        if(!inSetup)
            throw new InvalidFieldAccessException(INIT_ONLY_WRITE_MSG + "geoOrigin");

        BaseGeoOrigin node;
        VRMLNodeType old_node;

        if(pGeoOrigin != null)
            old_node = pGeoOrigin;
        else
            old_node = vfGeoOrigin;

        if(geo instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)geo).getImplementationNode();

            // Walk down the proto impl looking for the real node to check it
            // is the right type.
            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if((impl != null) && !(impl instanceof BaseGeoOrigin))
                throw new InvalidFieldValueException(GEO_ORIGIN_PROTO_MSG);

            node = (BaseGeoOrigin)impl;
            pGeoOrigin = (VRMLProtoInstance)geo;

        } else if(geo != null && !(geo instanceof BaseGeoOrigin)) {
            throw new InvalidFieldValueException(GEO_ORIGIN_NODE_MSG);
        } else {
            pGeoOrigin = null;
            node = (BaseGeoOrigin)geo;
        }

        vfGeoOrigin = node;
        if(geo != null)
            updateRefs(geo, true);

        if(old_node != null)
            updateRefs(old_node, true);
    }

    /**
     * Add a single child node to the list of available children. Override
     * to provide.renderer-specific behaviour, but remember to also call this
     * implementation too.
     *
     * @param node The node to add
     * @throws InvalidFieldValueException This is a bindable node shared
     */
    protected void addRootNode(VRMLNodeType node)
        throws InvalidFieldValueException {

        boolean new_bindable =
            ((node instanceof VRMLBindableNodeType) ||
             ((node instanceof VRMLGroupingNodeType) &&
              ((VRMLGroupingNodeType)node).containsBindableNodes()));

        if((shareCount > 1)  && new_bindable)
            throw new InvalidFieldValueException(USE_BIND_MSG);

        if(node instanceof VRMLGroupingNodeType) {
            ((VRMLGroupingNodeType)node).setShared(true);
        } else if(node instanceof VRMLProtoInstance) {
            VRMLNodeType impl =
                ((VRMLProtoInstance)node).getImplementationNode();

            while((impl != null) && (impl instanceof VRMLProtoInstance))
                impl = ((VRMLProtoInstance)impl).getImplementationNode();

            if(impl instanceof VRMLGroupingNodeType)
                ((VRMLGroupingNodeType)impl).setShared(true);
        }

        if(new_bindable)
            hasBindables = true;

        vfRootNode.add(node);
        updateRefs(node, true);
    }

    /**
     * Check the URL arrays for relative references. If found, add the
     * base URL to it to make them all fully qualified. This will also set the
     * urlRelativeCheck flag to true.
     */
    private void checkURLs() {
        URLChecker.checkURLsInPlace(worldURL, vfRootUrl, false);
        URLChecker.checkURLsInPlace(worldURL, vfChild1Url, false);
        URLChecker.checkURLsInPlace(worldURL, vfChild2Url, false);
        URLChecker.checkURLsInPlace(worldURL, vfChild3Url, false);
        URLChecker.checkURLsInPlace(worldURL, vfChild4Url, false);
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

    /**
     * Load a scene into memory.  This will either be the root or the children.
     *
     * @param children True if loading the children, false for the root.
     *
     */
    protected void loadScene(boolean children)
        throws IllegalArgumentException {

        if (childrenShown) {

			if((vfChild1Url.length != 0)&&(loadedScenes[0] == null)) {
            	fireUrlChanged(FIELD_CHILD1_URL);
			}
			if((vfChild1Url.length != 0)&&(loadedScenes[1] == null)) {
            	fireUrlChanged(FIELD_CHILD2_URL);
			}
			if((vfChild1Url.length != 0)&&(loadedScenes[2] == null)) {
            	fireUrlChanged(FIELD_CHILD3_URL);
			}
			if((vfChild1Url.length != 0)&&(loadedScenes[3] == null)) {
            	fireUrlChanged(FIELD_CHILD4_URL);
			}
            
        } else if (vfRootNode.size() == 0) {
			// using the root url
			if (rootSceneWrapper == null) {
            	fireUrlChanged(FIELD_ROOT_URL);
			}
		}
    }
}
