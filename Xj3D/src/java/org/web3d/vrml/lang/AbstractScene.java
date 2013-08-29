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

package org.web3d.vrml.lang;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Local imports
import org.web3d.util.HashSet;
import org.web3d.util.IntHashMap;

/**
 * Abstract implementation of the {@link BasicScene} interface.
 * <p>
 *
 *
 * @author Justin Couch
 * @version $Revision: 1.16 $
 */
public abstract class AbstractScene implements BasicScene {

    /** The meta data for the scene */
    protected SceneMetaData metaData;

    /** The root node of the scene */
    protected VRMLNode rootNode;

    /** List of all the routes stored */
    protected ArrayList routeList;

    /** All nodes currently held by this class */
    protected HashSet allNodes;

    /** List of all protos/externprotos held by this class */
    protected ArrayList templateList;

    /** The list of template declarations stored here by name (key) */
    protected HashMap templateMap;

    /** The map of all DEFs by DEF name (key) to VRMLNode (value) */
    private HashMap defMap;

    /** The map of all IMPORTs by name to String[] for src and AS names */
    private HashMap importMap;

    /**
     * Map of the primary type ID to the array list containing all the nodes
     * using that primary type.
     */
    private IntHashMap primaryTypes;

    /**
     * Map of the secondary type ID to the array list containing all the nodes
     * using that secondary type.
     */
    private IntHashMap secondaryTypes;

    /** Major version number of this scene */
    protected int majorVersion;

    /** Minor version number of this scene */
    protected int minorVersion;

    /** The node factory for this scene */
    protected VRMLNodeFactory nodeFactory;

    /** The root URL for the world location */
    protected String worldURL;

    /** The URI that created this scene */
    protected String loadedURI;

    /** The layer ID of this scene. */
    private int layerId;

    /**
     * Construct a new instance of the abstract scene. Will initialise all
     * data structures.
     *
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     */
    protected AbstractScene(int major, int minor) {
        majorVersion = major;
        minorVersion = minor;

        allNodes = new HashSet();
        routeList = new ArrayList();
        templateList = new ArrayList();
        templateMap = new HashMap();
        primaryTypes = new IntHashMap();
        secondaryTypes = new IntHashMap();
        defMap = new HashMap();
        importMap = new HashMap();
    }

    //----------------------------------------------------------
    // Methods defined by BasicScene
    //----------------------------------------------------------

    /**
     * Get the layer ID of this scene. This method is only useful when the
     * scene is not the root scene, such as being buried in an Inline.
     *
     * @return id A non-negative layer identifier
     */
    public int getLayerId() {
        return layerId;
    }

    /**
     * Set the layer ID of this scene. This method is only called when the
     * scene is not the root scene, such as being buried in an Inline. This
     * passes down the layer ID that the inline is kept in. The scene should
     * make sure that this is passed down to the children nodes of this scene,
     * but making sure to ignore any contained layers.
     *
     * @param id A non-negative layer identifier
     */
    public void setLayerId(int id) {
        layerId = id;
    }

    /**
     * Get the Root URL of the world that this scene represents. This is the
     * URL minus the file name, but including any directory information.
     *
     * @return The full URL of the world's location
     */
    public String getWorldRootURL() {
        return worldURL;
    }

    /**
     * Get the URI of the world that this scene represents.  Returns null
     * for dynamically created worlds.
     *
     * @return The full URI of the world's location
     */
    public String getLoadedURI() {
        return loadedURI;
    }

    /**
     * Get the node that forms the root of this scene.
     *
     * @return A reference to the root node of the scene
     */
    public VRMLNode getRootNode() {
        return rootNode;
    }

    /**
     * Get the meta data associated with this scene. A scene should always have
     * some amount of metadata.
     *
     * @return The current meta data
     */
    public SceneMetaData getMetaData() {
        return metaData;
    }

    /**
     * Get the specification major version that this scene belongs to.
     *
     * @return The major version number
     */
    public int getSpecificationMajorVersion() {
        return majorVersion;
    }

    /**
     * Get the specification minor version that this scene belongs to.
     *
     * @return The minor version number
     */
    public int getSpecificationMinorVersion() {
        return minorVersion;
    }

    /**
     * Get the node factory that represents the setup of this scene. Provided
     * so that a caller can make use of the factory to create nodes that are
     * only valid as part of the initial setup of this scene.
     *
     * @return The VRMLNodeFactory instance configured to represent this scene
     */
    public VRMLNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    /**
     * Set the node factory instance to be used with this scene. This is a
     * one-shot call. After it has been set, it cannot be changed. An exception
     * will be thrown.
     *
     * @param fac The factory instance to use
     * @throws IllegalArgumentException The factory instance provided was null
     * @throws IllegalStateException A factory has already been set
     */
    public void setNodeFactory(VRMLNodeFactory fac)
        throws IllegalArgumentException, IllegalStateException {

        if(nodeFactory != null)
            throw new IllegalStateException("Node factory already set");

        if(fac == null)
            throw new IllegalArgumentException("Node factory was null");

        nodeFactory = fac;
    }


    /**
     * Add a node to this scene. This is a single node instance and the
     * implementation is not required to traverse this node for all its
     * children and sort them as well. It is expected external code shall be
     * handling the tree traversal. The code will make sure that this node
     * instance is only inserted into the internal structures exactly once,
     * even if this is a duplicate call.
     *
     * @param node The node to add
     */
    public void addNode(VRMLNode node) {
        if(allNodes.contains(node))
            return;

        int p_type = node.getPrimaryType();

        ArrayList list = (ArrayList)primaryTypes.get(p_type);

        if(list == null) {
            list = new ArrayList();
            primaryTypes.put(p_type, list);
        }

        list.add(node);

        // Secondary types.
        int[] s_types = node.getSecondaryType();

        for(int i = 0; i < s_types.length; i++) {
            list = (ArrayList)secondaryTypes.get(s_types[i]);

            if(list == null) {
                list = new ArrayList();
                secondaryTypes.put(s_types[i], list);
            }

            list.add(node);
        }
    }

    /**
     * Remove a node from this scene. This is a single node instance and the
     * implementation is not required to traverse this node for all its
     * children and sort them as well. It is expected external code shall be
     * handling the tree traversal. The implementation shall silently ignore
     * a request to remove a node that does not exist in this scene.
     *
     * @param node The node to add
     */
    public void removeNode(VRMLNode node) {
        if(!allNodes.contains(node))
            return;

        int p_type = node.getPrimaryType();

        ArrayList list = (ArrayList)primaryTypes.get(p_type);
        list.remove(node);


        int[] s_types = node.getSecondaryType();

        for(int i = 0; i < s_types.length; i++) {
            list = (ArrayList)secondaryTypes.get(s_types[i]);
            list.remove(node);
        }
    }


    /**
     * Add a new template to this scene. Each template is assumed to be
     * independent of the others. If a template contains further templates,
     * the nested instances should not be added to this scene.
     *
     * @param template The new template instance to use
     */
    public void addTemplate(VRMLNodeTemplate template) {

        // Don't add the same instance twice. Causes all sorts of oddball
        // behaviour down stream.
        if(templateList.contains(template))
            return;

        templateList.add(template);
        templateMap.put(template.getVRMLNodeName(), template);
    }

    /**
     * Remove a template from this scene. The implementation shall silently
     * ignore a request to remove a template that does not exist in this scene.
     * If instances of this template already exist, they shall remain in the
     * scene.
     *
     * @param template The template instance to remove
     */
    public void removeTemplate(VRMLNodeTemplate template) {
        templateList.remove(template);
        templateMap.remove(template.getVRMLNodeName());
    }

    /**
     * Get the list of ROUTEs declared in this scene.
     *
     * @return The list of ROUTE instances in this scene
     */
    public ArrayList getRoutes() {
        return routeList;
    }

    /**
     * Get the list of top-level node templates declared in this scene.
     * The list will not be sorted into separate sets of PROTO and EXTERNPROTO
     * instances.
     *
     * @return The list of VRMLNodeTemplate instances in this scene
     */
    public ArrayList getNodeTemplates() {
        return templateList;
    }

    /**
     * Get the list of nodes of the given primary type declared in this scene.
     * Proto instances may be fetched through this method too as they have
     * a primary type of ProtoInstance.
     *
     * @return The list of node instances in this scene.
     */
    public ArrayList getByPrimaryType(int type) {
        ArrayList ret_val = (ArrayList)primaryTypes.get(type);

        if(ret_val == null) {
            ret_val = new ArrayList();
            primaryTypes.put(type, ret_val);
        }

        return ret_val;
    }

    /**
     * Get the list of nodes of the given secondary type declared in this
     * scene.
     *
     * @return The list of node instances in this scene.
     */
    public ArrayList getBySecondaryType(int type) {
        ArrayList ret_val = (ArrayList)secondaryTypes.get(type);

        if(ret_val == null) {
            ret_val = new ArrayList();
            secondaryTypes.put(type, ret_val);
        }

        return ret_val;
    }

    /**
     * Get the proto defined by the name. If no proto is known by that name
     * then return null.
     *
     * @param name The name of the proto to fetch.
     * @return The proto known by that name or null
     */
    public VRMLNodeTemplate getNodeTemplate(String name) {
        return (VRMLNodeTemplate)templateMap.get(name);
    }

    /**
     * Get a list of the nodes that have been named with DEF in this scene.
     * The map is keyed by the DEF name string and the values are the
     * <code>VRMLNode</code> instances. If there are no nodes marked with
     * DEF then the map will be empty.
     *
     * @return A map of the DEF'd nodes.
     */
    public Map getDEFNodes() {
        return defMap;
    }

    /**
     * Get the IMPORT declaration information from this scene. There is no
     * validity checking of these statements. The map is the AS name
     * as the key to the value being a String[] that contains the inline's
     * DEF name and exported name.
     *
     * @return A map of the imported nodes
     */
    public Map getImports() {
        return importMap;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the root URL of the world that this scenegraph represents
     *
     * @param url The full URL of the world's location
     */
    public void setWorldRootURL(String url) {
        worldURL = url;
    }

    /**
     * Set the URI of the world that this scenegraph represents
     *
     * @param uri The full URI of the world's location
     */
    public void setLoadedURI(String uri) {
        loadedURI = uri;
    }

    /**
     * Set the root node of the scene.
     *
     * @param node The root node of the scene to use
     */
    public void setRootNode(VRMLNode node) {
        rootNode = node;
    }

    /**
     * Add a new def name to the map. If there is a DEF name of this type
     * already registered, it is replace with the incoming instance (as per
     * VRML specification.
     *
     * @param name The name of the node to be replaced
     * @param node The new node instance to be registered for the node
     */
    public void addDEFNode(String name, VRMLNode node) {
        if((node != null) && (name != null))
            defMap.put(name, node);
    }

    /**
     * Remove a def name from the map.
     *
     * @param name The name of the node to be removed
     */
    public void removeDEFNode(String name) {
        defMap.remove(name);
    }

    /**
     * Add a new import name to the map. If there is a Imported name of this type
     * already registered, it is replace with the incoming instance (as per
     * VRML specification.
     *
     * @param as The name of the node in the local file (AS value)
     * @param def The DEF name of the local inline node
     * @param exported The exported name
     */
    public void addImportedNode(String as, String def, String exported) {
        if(def != null) {
            if(as != null)
                importMap.put(as, new String[] {def, exported});
            else
                importMap.put(exported, new String[] {def, exported});
        }
    }

    /**
     * Remove a import name from the map.
     *
     * @param name The name of the node to be removed
     */
    public void removeImportedNode(String name) {
        importMap.remove(name);
    }
}
