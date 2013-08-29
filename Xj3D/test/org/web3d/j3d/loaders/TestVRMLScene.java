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

package org.web3d.j3d.loaders;

// Standard imports
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

// Application specific imports
import org.web3d.vrml.nodes.*;

import org.web3d.vrml.lang.ROUTE;

/**
 * Representation of a complete dummy scene in VRML for testing.
 * <p>
 *
 * The scene returns lists of nodes of the given type. This list contains
 * all of the nodes of that type in the order that they are declared in the
 * incoming stream. As the scene changes due to scripting and external
 * interactions, it will add new instances of these nodes to the end of the
 * list. If there is none of the given node types, the methods shall return
 * empty lists.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class TestVRMLScene implements VRMLScene {

    // Some constants for various standard bindable types

    /** The version of the VRML scene provided */
    private String vrmlVersion;

    /** The root URL for the world location */
    private String worldURL;

    /** The root node of the scene */
    private VRMLNodeType rootNode;

    /**
     * Map of the bindable node types. The key is the string of the known
     * node types and the value is a List of these nodes
     */
    private HashMap bindablesMap;

    /** The list of nodes with external content stored here */
    private LinkedList externalNodes;

    /** The list of scripts stored here */
    private LinkedList scripts;

    /** The list of ROUTEs stored here */
    private LinkedList routes;

    /** The list of Inlines stored here */
    private LinkedList inlines;

    /** The list of audio clips stored here */
    private LinkedList audioclips;

    /** The list of lights stored here */
    private LinkedList lights;

    /** The list of time dependent nodes */
    private LinkedList timeNodes;

    /** The list of proto instances */
    private LinkedList protoInstances;

    /** The list of PROTO declarations stored here by name (key) */
    private HashMap protos;

    /** The list of EXTERNPROTO declarations stored here by name (key) */
    private HashMap externprotos;

    /** The map of all DEFs by DEF name (key) to VRMLNodeType (value) */
    private HashMap defMap;

    /** A pointer to the first proto added to this scene */
    private VRMLPrototype firstProto;

    /**
     * Construct a new default instance of this class
     */
    public TestVRMLScene() {
        externalNodes = new LinkedList();
        bindablesMap = new HashMap();
        scripts = new LinkedList();
        routes = new LinkedList();
        inlines = new LinkedList();
        audioclips = new LinkedList();
        lights = new LinkedList();
        timeNodes = new LinkedList();
        protoInstances = new LinkedList();
        defMap = new HashMap();
        protos = new HashMap();
        externprotos = new HashMap();
    }

    /**
     * Set the VRML version information.
     *
     * @param ver A string representation of the version
     */
    public void setVersion(String ver) {
        vrmlVersion = ver;
    }

    /**
     * Get the VRML version information. This is the Version of the VRML
     * specification that was declared in the first line of the file.
     *
     * @return The version of this file
     */
    public String getVersion() {
        return vrmlVersion;
    }

    /**
     * Set the root URL of the world that this scenegraph represents
     *
     * @param url The full URL of the world's location
     */
    public void setWorldRootURL(String url) {
        worldURL = url;
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
     * Register a bindable node type with the internal map. If the type
     * already exists this is ignored.
     *
     * @param type The new type to be registered
     */
    public void registerBindableType(String type) {
        if((type == null) || (type.length() == 0))
            return;

        List l = (List)bindablesMap.get(type);
        if(l == null)
            bindablesMap.put(type, new LinkedList());
    }

    /**
     * Set a bindable node of the given type. The type must have been
     * registered beforehand or this will ignore the request.
     *
     * @param type The node type string
     * @param node The node to be registered
     */
    public void addBindableNode(String type, VRMLNodeType node) {
        if((node != null) && (type != null)) {
            List l = (List)bindablesMap.get(type);
            if(l != null)
                l.add(node);
        }
    }

    /**
     * Remove a bindable node type. The type must have been registered
     * beforehand or this will ignore the request. If the node is not know it
     * will silently ignore the request.
     *
     * @param type The node type string
     * @param node The node to be registered
     */
    public void removeBindableNode(String type, VRMLNodeType node) {
        List l = (List)bindablesMap.get(type);
        if(l != null)
            l.remove(node);
    }

    /**
     * Get the list of bindable node instances. As there are many different
     * types of bindable nodes we ask the caller to provide us a string
     * describing the type of bindable node wanted. This string should be the
     * same as the node's name, including case information. If the type is not
     * a known type this will return null rather than an empty list. If the
     * type is known, but there were none declared, then an empty list will be
     * returned.
     *
     * @param type The name of the node type to get
     * @return A list of the bindable node instances of the given type
     */
    public List getBindableNodes(String type) {
        return (List)bindablesMap.get(type);
    }

    /**
     * Add a node that defines it's content externally. This method does not
     * automatically add this node to scripts or elsewhere. If the script is
     * added to this method, the add script method must also be called, and
     * vice versa. Null references are ignored.
     *
     * @param node The new node to add.
     */
    public void addExternalDefinedNode(VRMLExternalNodeType node) {
        if(node != null)
            externalNodes.add(node);
    }

    /**
     * Remove an external defined node. If the node is not in the list it is
     * silently ignored.
     *
     * @param node The node to be removed
     */
    public void removeExternalDefinedNode(VRMLExternalNodeType node) {
        externalNodes.remove(node);
    }

    /**
     * Get the list of all nodes that define their content externally. This
     * list includes scripts, inlines and extern protos, but may include others
     * that are not known within the core implementation.
     *
     * @return The list of {@link org.web3d.vrml.nodes.VRMLExternalNodeType
     *    VRMLExternalNodeTypes}
     */
    public List getExternalDefinedNodes() {
        return externalNodes;
    }

    /**
     * Add a node that requires system time information. This method does not
     * automatically add this node to the audioclips list. If the audioclip is
     * added to this method, the add audioclip method must also be called, and
     * vice versa. Null references are ignored.
     *
     * @param node The new node to add.
     */
    public void addTimeDependentNode(VRMLTimeControlledNodeType node) {
        if(node != null)
            timeNodes.add(node);
    }

    /**
     * Remove an timed node. If the node is not in the list it is
     * silently ignored.
     *
     * @param node The node to be removed
     */
    public void removeTimeDependentNode(VRMLTimeControlledNodeType node) {
        timeNodes.remove(node);
    }

    /**
     * Get the list of all nodes that require system time information.
     *
     * @return The list of
     *    {@link org.web3d.vrml.nodes.VRMLTimeControlledNodeType}
     */
    public List getTimeDependentNodes() {
        return timeNodes;
    }

    /**
     * Add a new script instance to the internal list.
     *
     * @param node The new script instance to be added
     */
    public void addScript(VRMLScriptNodeType node) {
        if((node != null) && !scripts.contains(node))
            scripts.add(node);
    }

    /**
     * Remove a script instance from the internal list.
     *
     * @param node The new script instance to be removed
     */
    public void removeScript(VRMLScriptNodeType node) {
        scripts.remove(node);
    }

    /**
     * Get the description of the scripts that have been declared for this
     * scene. These are all scripts regardless of whether they have been
     * buried inside a PROTO or not. Scripts in EXTERNPROTOs and Inlines are
     * not considered to be part of the scene.
     *
     * @return The list of script instances in this scene
     */
    public List getScripts() {
        return scripts;
    }

    /**
     * Add a new ROUTE instance to the internal list.
     *
     * @param node The new ROUTE instance to be added
     */
    public void addRoute(ROUTE node) {
        if((node != null) && !routes.contains(node))
            routes.add(node);
    }

    /**
     * Remove a route instance from the internal list. If the list doesn't
     * know about this instance, it is quietly ignored.
     *
     * @param node The ROUTE to remove
     */
    public void removeRoute(ROUTE node) {
        routes.remove(node);
    }

    /**
     * Get the list of ROUTEs declared in this scene.
     *
     * @return The list of ROUTE instances in this scene
     */
    public List getRoutes() {
        return routes;
    }

    /**
     * Add an instance of a proto to the internal list.
     *
     * @param node The instance to be added
     */
    public void addProtoInstance(VRMLProtoInstance node) {
        if((node != null) && !protoInstances.contains(node))
            protoInstances.add(node);
    }

    /**
     * Get the list of proto instances that are instantiated in this scene.
     *
     * @return The list of protos instance in this scene
     */
    public List getProtoInstances() {
        return protoInstances;
    }

    /**
     * Add a new Inlines instance to the internal list.
     *
     * @param node The new Inlines instance to be added
     */
    public void addInline(VRMLInlineNodeType node) {
        if((node != null) && !inlines.contains(node))
            inlines.add(node);
    }

    /**
     * Remove an Inline instance from the internal list.
     *
     * @param node The Inline instance to be removed
     */
    public void removeInline(VRMLInlineNodeType node) {
        inlines.remove(node);
    }

    /**
     * Get the list of Inlines declared in this scene. The inlines may or
     * may not have been loaded at this point. Check with the interface
     * declaration to see if this is the case.
     *
     * @return The list of Inline instances in this scene
     */
    public List getInlines() {
        return inlines;
    }

    /**
     * Add a new proto instance to the internal list.
     *
     * @param node The new proto instance to be added
     */
    public void addProto(VRMLPrototype node) {
        if(node != null) {
            protos.put(node.getName(), node);

            if (firstProto == null) firstProto = node;
        }
    }

    /**
     * Remove a proto instance from the internal list.
     *
     * @param node The new proto instance to be removed
     */
    public void removeProto(VRMLPrototype node) {
        if (firstProto == node) firstProto = null;

        protos.remove(node.getName());
    }

    /**
     * Get the proto defined by the name. If no proto is known by that name
     * then return null.
     *
     * @param name The name of the proto to fetch.
     * @return The proto known by that name or null
     */
    public VRMLPrototype getProto(String name) {
        return (VRMLPrototype)protos.get(name);
    }

   /**
     * Get the first PROTO declared in this scene.  EXTERNPROTO's are not
     * included in this list. If no protos are declared it will return null.
     * If the proto is later removed then this will return null;
     *
     * @return The first proto declared or null
     */
    public VRMLPrototype getFirstProto() {
        return firstProto;
    }

    /**
     * Get the list of ordinary PROTO's declared in this scene. EXTERNPROTOs
     * are not included in this list.
     *
     * @return The list of proto instances in this scene
     */
    public Map getProtos() {
        return protos;
    }

    /**
     * Add a new EXTERNPROTO instance to the internal list.
     *
     * @param node The new EXTERNPROTO instance to be added
     */
    public void addExternProto(VRMLExternProtoDeclare node) {
        if(node != null)
            externprotos.put(node.getName(), node);
    }

    /**
     * Remove an EXTERNPROTO instance from the internal list.
     *
     * @param node The EXTERNPROTO instance to be removed
     */
    public void removeExternProto(VRMLExternProtoDeclare node) {
        externprotos.remove(node.getName());
    }

    /**
     * Get the externproto defined by the name. If no proto is known by that
     * name then return null.
     *
     * @param name The name of the proto to fetch.
     * @return The proto known by that name or null
     */
    public VRMLExternProtoDeclare getExternProto(String name) {
        return (VRMLExternProtoDeclare)externprotos.get(name);
    }

    /**
     * Get the list of EXTERNPROTOs declared in this scene. The instances may
     * or may not have been loaded at this point. Check with the interface
     * declaration to see if this is the case.
     *
     * @return The list of EXTERNPROTO instances in this scene
     */
    public Map getExternProtos() {
        return externprotos;
    }

    /**
     * Add a new AudioClip instance to the internal list.
     *
     * @param node The new AudioClip instance to be added
     */
    public void addAudioClip(VRMLAudioClipNodeType node) {
        if((node != null) && !audioclips.contains(node))
            audioclips.add(node);
    }

    /**
     * Remove an AudioClip instance from the internal list.
     *
     * @param node The AudioClip instance to be removed
     */
    public void removeAudioClip(VRMLAudioClipNodeType node) {
        audioclips.remove(node);
    }

    /**
     * Get the list of nodes in the scene that provide audio capabilities.
     * The audio streams may or may not be loaded at this time. This is both
     * the sound node and movietexture type nodes.
     *
     * @return The list of audio nodes in the scene
     */
    public List getAudioClips() {
        return audioclips;
    }

    /**
     * Add a new light instance to the internal list.
     *
     * @param node The new light instance to be added
     */
    public void addLight(VRMLLightNodeType node) {
        if((node != null) && !lights.contains(node))
            lights.add(node);
    }

    /**
     * Remove a light instance from the internal list.
     *
     * @param node The light instance to be removed
     */
    public void removeLight(VRMLLightNodeType node) {
        lights.remove(node);
    }

    /**
     * Get the list of lights declared in this scene. These are all forms of
     * lights.
     *
     * @return The list of light instances in this scene.
     */
    public List getLights() {
        return lights;
    }

    /**
     * Set the root node of the scene.
     *
     * @param node The root node of the scene to use
     */
    public void setRootNode(VRMLNodeType node) {
        rootNode = node;
    }

    /**
     * Get the node that forms the root of this scene.
     *
     * @return A reference to the root node of the scene
     */
    public VRMLNodeType getRootNode() {
        return rootNode;
    }

    /**
     * Add a new def name to the map. If there is a DEF name of this type
     * already registered, it is replace with the incoming instance (as per
     * VRML specification.
     *
     * @param name The name of the node to be replaced
     * @param node The new node instance to be registered for the node
     */
    public void addDEFNode(String name, VRMLNodeType node) {
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
}
