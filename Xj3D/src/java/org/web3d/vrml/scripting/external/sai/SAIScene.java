/***************************************************************************** 
 *                        Web3d.org Copyright (c) 2002 - 2007 
 *                               Java Source 
 * 
 * This source is licensed under the GNU LGPL v2.1 
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information 
 * 
 * This software comes with the standard NO WARRANTY disclaimer for any 
 * purpose. Use it at your own risk. If there's a problem you get to fix it. 
 * 
 ****************************************************************************/ 

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.x3d.sai.*;

import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;
import org.web3d.vrml.scripting.external.buffer.ExternalEventQueue;

import org.xj3d.core.eventmodel.RouteManager;

/**
 * A wrapper around X3DScene necessary because X3DScene and VRMLScene
 * conflict in method declarations.
 *
 * @author Brad Vender
 * @version $Revision: 1.24 $
 */
class SAIScene implements X3DScene {

    /** Empty string array for toArray calls */
    static final String[] EMPTY_STRING_ARRAY=new String[0];

    /** The event queue to post changes to */
    protected ExternalEventQueue eventQueue;

    /** The execution space*/
    protected VRMLExecutionSpace executionSpace;

    /** The underlying VRMLScene */
    protected VRMLScene theScene;

    /** The node factory for translating between SAINode and VRMLNodeType */
    protected SAINodeFactory nodeFactory;

    /** The ErrorReporter to send errors and warnings to. */
    private ErrorReporter errorReporter;

    /** Route manager for handling user added/removed routes */
    protected RouteManager routeManager;

    /** FrameState manager for creating nodes */
    protected FrameStateManager stateManager;

    /**
     * Construct a new X3DScene wrapper.
     * @param scene The scene to wrap.
     * @param aFactory The factory to map between SAINode instances and VRMLNodeType instances
     * @param queue The event queue to post queued events to
     * @param reporter Handler for error messages etc
     */
    SAIScene(VRMLScene scene,
             RouteManager rm,
             FrameStateManager fsm,
             SAINodeFactory aFactory,
             ExternalEventQueue queue,
             VRMLExecutionSpace space,
             ErrorReporter reporter) {

        if (space==null)
            throw new IllegalArgumentException("Execution space must be valid.");

        routeManager = rm;
        stateManager = fsm;
        theScene=scene;
        nodeFactory=aFactory;
        eventQueue=queue;
        executionSpace=space;
        errorReporter=reporter;
    }

    /** Provide access to the underlying scene. */
    VRMLScene getRealScene() {
        return theScene;
    }

    //-------------------------------------------------------------------
    // Methods defined by X3DScene
    //-------------------------------------------------------------------

    /**
     * Add a meta tag data item to this scene. Both name and value must be
     * non-null.
     *
     * @param name The name of the tag to add
     * @param value The value of the tag
     * @throws NullPointerException The name or value were null
     */
    public void addMetaData(String name, String value) {
        getMetaData().put(name,value);
    }

    /**
     * Get the meta data mapping from this scene. The map returned cannot
     * be changed and represents the current internal state.
     *
     * @return The current meta tag mappings
     */
    public Map getMetaData() {
        return theScene.getMetaData().getMetaData();
    }

    /**
     * Remove the named tag from the map. If tag name does not exist, the
     * request is silently ignored.
     *
     * @param name The name of the tag to remove
     */
    public void removeMetaData(String name) {
        getMetaData().remove(name);
    }

    /**
     * @see X3DExecutionContext#getExportedNode
     */
    public X3DNode getExportedNode(String name) {
        VRMLNodeType export=(VRMLNodeType)(theScene.getExports().get(name));
        if (export==null)
            throw new InvalidNodeException(name);
        return nodeFactory.getSAINode(export);
    }

    /**
     * Get the list of exports from this file. The map is the exported name
     * to the node instance. If there are no nodes exported, the map will be
     * empty. Note that exported nodes is not a valid concept for VRML97. It
     * only exists for X3D V3.0 and above.
     *
     * @return A map of the exported nodes
     */
    public String[] getExportedNodes() {
        return (String[])theScene.getExports().keySet().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Add a named node to this scene. The node must be a valid member of
     * this scene already. If not, an error is generated. An exported name
     * may be null, in which case the local name is used as the export name
     *
     * @param localName The local DEF name in this scene
     * @param exportName The name to export the node as, or null
     */
    public void updateExportedNode(String localName, String exportName) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * Remove an exported name from the scene. If the node is not currently
     * exported this is silently ignored. The name is first checked against
     * the explicit export names and then against the DEF names that are
     * exported without using the AS keyword.
     *
     * @param name The exported name to remove
     *
     */
    public void removeExportedNode(String name) {
        theScene.getExports().remove(name);
    }

    /**
     * Add a new root node to the scene graph. This must be a valid child node
     * type or proto instance that can be a child node, as per the VRML
     * specification. If the node is already part of this scene, this is
     * treated as an implicit USE of the node at the root of the scene graph
     * and the normal rules will apply.
     *
     * @param node The node pointer to add to the scene
     */
    public void addRootNode(X3DNode node) {
        if (!(node instanceof SAINode))
            throw new IllegalArgumentException("Expected external SAI wrapper node");
        SAINode realNode=(SAINode)(node);
        VRMLWorldRootNodeType root=
            (VRMLWorldRootNodeType)(theScene.getRootNode());

        VRMLNodeType actualNode=nodeFactory.getVRMLNode(realNode);
        if (actualNode!=null)
            eventQueue.postRealizeNode(actualNode);
        eventQueue.processEvent(new AddRootNodeEvent(root,actualNode));
    }

    /**
     * Remove the root node from the scene graph. This must be a valid child node
     * type or proto instance, as per the VRML specification. If the node is
     * not a root node then an exception is generated.
     */
    public void removeRootNode(X3DNode node) {
        if (!(node instanceof SAINode))
            throw new IllegalArgumentException("Expected external SAI wrapper node");
        SAINode realNode=(SAINode)(node);
        VRMLWorldRootNodeType root=
            (VRMLWorldRootNodeType)(theScene.getRootNode());

        eventQueue.processEvent(new RemoveRootNodeEvent(root,nodeFactory.getVRMLNode(realNode)));
    }

    //-------------------------------------------------------------------
    // Methods defined by X3DExecutionContext
    //-------------------------------------------------------------------

    /**
     * Get the specification version name that was used to describe this
     * scene. The version is a string that is relative to the specification
     * used and is in the format "X.Y" where X and Y are integer values
     * describing major and minor versions, respectively.
     *
     * @return The version used for this scene
     */
    public String getSpecificationVersion() {
        return theScene.getMetaData().getVersion();
    }

    /**
     * Get the encoding of the original file type.
     *
     * @return The encoding description
     */
    public int getEncoding() {
        return theScene.getMetaData().getEncoding();
    }

    /**
     * Get the name of the profile used by this scene. If the profile is
     * not set, will return null.
     *
     * @return The name of the profile, or null
     */
    public ProfileInfo getProfile() {
        // There has got to be a better way of doing this!
        String profileName=theScene.getMetaData().getProfileName();
        org.web3d.vrml.lang.ProfileInfo[] profiles=theScene.getNodeFactory().getAvailableProfiles();
        for (int counter=0; counter<profiles.length; counter++) {
            if (profiles[counter].getName().equals(profileName))
                return new SAIProfileInfo(profiles[counter]);
        }
        throw new RuntimeException("Current profile is "+profileName+" but not found in existing profiles");
    }

    /**
     * Get the list of all the components declared in the scene. If there were
     * no components registered, this will return null.
     *
     * @return The components declared or null
     */
    public ComponentInfo[] getComponents() {
        org.web3d.vrml.lang.ComponentInfo[] components=theScene.getMetaData().getComponents();
        ComponentInfo[] results=null;
		if (components != null) {
			results=new ComponentInfo[components.length];
        	for (int counter=0; counter<results.length; counter++)
            	results[counter]=new SAIComponentInfo(components[counter]);
		}
        return results;
    }

    /** @see org.web3d.x3d.sai.X3DExecutionContext#getImportedNode */
    public X3DNode getImportedNode(String name) {
        VRMLNodeType importedNode=(VRMLNodeType) theScene.getImports().get(name);
        if (importedNode==null)
            throw new InvalidNodeException(name);
        return nodeFactory.getSAINode(importedNode);
    }

    /** @see org.web3d.x3d.sai.X3DExecutionContext#removeImportedNode */
    public void removeImportedNode(String importName) {
        theScene.getImports().remove(importName);
    }

    /** @see org.web3d.x3d.sai.X3DExecutionContext#updateImportedNode */
    public void updateImportedNode(String exportedName, String importedName, X3DNode inline) {
        VRMLNodeType realNode=nodeFactory.getVRMLNode(inline);
        while (realNode instanceof VRMLProtoInstance)
            realNode=((VRMLProtoInstance)realNode).getImplementationNode();
        if (realNode instanceof VRMLInlineNodeType) {
            VRMLInlineNodeType inlineNode=(VRMLInlineNodeType) realNode;
            VRMLScene scene=(VRMLScene) inlineNode.getContainedScene();
            VRMLNodeType foreignNode=(VRMLNodeType) scene.getExports();
            if (foreignNode==null)
                throw new InvalidNodeException(exportedName+" not found in target scene.");
            theScene.getImports().put(importedName,foreignNode);
        } else throw new IllegalArgumentException("Designated node is not an Inline node");
    }

    /**
     * Get the fully qualified URL of this scene. This returns
     * the entire URL including any possible arguments that might be associated
     * with a CGI call or similar mechanism. If the world was created
     * programmatically, this will return null.
     *
     * @return A string of the URL or null if not supported.
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public String getWorldURL() throws InvalidBrowserException {
        return theScene.getWorldRootURL();
    }


    /**
     * Get the list of ROUTEs in this scene. This gives all of the top level
     * routes in the scene in the order that they are declared.
     *
     * @return The list of ROUTE instances in this scene
     */
    public X3DRoute[] getRoutes() {
        ArrayList routes=theScene.getRoutes();
        X3DRoute[] result=new X3DRoute[routes.size()];
        int counter;
        for (counter=0; counter<result.length; counter++) {
            System.out.println(routes.get(counter).getClass().getName());
            ROUTE r=(ROUTE)(routes.get(counter));
            result[counter]=new SAIRoute(
                nodeFactory.getSAINode((VRMLNodeType)(r.getSourceNode())),
                r.getSourceNode().getFieldDeclaration(r.getSourceIndex()).getName(),
                nodeFactory.getSAINode((VRMLNodeType)(r.getDestinationNode())),
                r.getDestinationNode().getFieldDeclaration(r.getDestinationIndex()).getName()
            );
        }
        return result;
    }

    /**
     * Add a route in this scene. The route is considered to be part of this
     * scene regardless of whether the two end points are or not. The route will
     * remain valid as long as both nodes are live and this scene is live. If
     * this scene becomes invalid (eg a loadURL call is successful) then the
     * route will no longer exist and there shall be no connection between the
     * two nodes.
     *
     * @param fromX3DNode The source node for the route
     * @param readableField The readable field source of the route
     * @param toX3DNode The destination node of the route
     * @param writableField The writable field destination of the route
     * @throws InvalidReadableFieldException if the named readable field does not exist
     * @throws InvalidWritableFieldException if the named writable field does not exist.
     * @throws InvalidNodeException The nominated destination or source node
     *   has been disposed of
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public X3DRoute addRoute(X3DNode fromX3DNode,
                             String readableField,
                             X3DNode toX3DNode,
                             String writableField)
    throws InvalidBrowserException,
                InvalidReadableFieldException,
                InvalidWritableFieldException,
        InvalidNodeException {



        VRMLNodeType src_node = nodeFactory.getVRMLNode(fromX3DNode);
        VRMLNodeType dest_node = nodeFactory.getVRMLNode(toX3DNode);

        int src_idx = src_node.getFieldIndex(readableField);
        int dest_idx = dest_node.getFieldIndex(writableField);

        eventQueue.processEvent(new AddRouteEvent(routeManager,
                                                  executionSpace,
                                                  src_node,
                                                  src_idx,
                                                  dest_node,
                                                  dest_idx));

        X3DRoute r=new SAIRoute(fromX3DNode,readableField,toX3DNode,writableField);

        //ArrayList routes=theScene.getRoutes();
        //routes.add(r);

        return r;
    }

    /**
     * Delete the route from this scene. If the route is not part of this scene
     * an exception is generated.
     *
     * @param route The route reference to delete
     * @throws Invalid
     */
    public void removeRoute(X3DRoute route) throws InvalidBrowserException {

        VRMLNodeType src_node = nodeFactory.getVRMLNode(route.getSourceNode());
        VRMLNodeType dest_node = nodeFactory.getVRMLNode(route.getDestinationNode());

        int src_idx = src_node.getFieldIndex(route.getSourceField());
        int dest_idx = dest_node.getFieldIndex(route.getDestinationField());

        eventQueue.processEvent(new RemoveRouteEvent(routeManager,
                                                     executionSpace,
                                                     src_node,
                                                     src_idx,
                                                     dest_node,
                                                     dest_idx));
    }

    /**
     * Get the list of ordinary PROTO's declared in this scene. EXTERNPROTOs
     * are not included in this list.
     *
     * @return The list of proto instances in this scene
     */
    public String[] getProtosNames() {
        return (String[])theScene.getProtos().keySet().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Get the PROTO declaration representing the given name.
     *
     * @param name The name of the proto to fetch
     */
    public X3DProtoDeclaration getProtoDeclaration(String name) {
        Map proto_map = theScene.getProtos();
        VRMLProtoDeclare decl = (VRMLProtoDeclare)proto_map.get(name);

        if(decl == null)
            return null;
        else
            return new SAIProtoDeclaration(nodeFactory, decl, theScene);
    }

    /**
     * Add the PROTO declaration representing the given name.
     *
     * @param name The name of the proto to fetch
     */
    public void updateProtoDeclaration(String name, X3DProtoDeclaration proto) {
        if (!(proto instanceof SAIProtoDeclaration))
            throw new RuntimeException("PROTO declaration not compatible");
        SAIProtoDeclaration decl=(SAIProtoDeclaration)proto;
        // Don't break the old SAIProtoDeclaration instance
        theScene.getProtos().put(name,decl.getRealProtoDeclaration());
    }

    /**
     * Remove the proto declaration registered under the given name.
     *
     * @param name The name of the proto to fetch
     */
    public void removeProtoDeclaration(String name) {
        theScene.getProtos().remove(name);
    }

    /**
     * Get the list of EXTERNPROTOs declared in this scene. The instances may
     * or may not have been loaded at this point. Check with the interface
     * declaration to see if this is the case.
     *
     * @return The list of EXTERNPROTO instances in this scene
     */
    public String[] getExternProtoNames() {
        return (String[])theScene.getExternProtos().keySet().toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Get the EXTERNPROTO declaration representing the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public X3DExternProtoDeclaration getExternProtoDeclaration(String name) {
        Object o=theScene.getExternProtos().get(name);
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * Add the EXTERNPROTO declaration representing the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public void updateExternProtoDeclaration(String name,
                               X3DExternProtoDeclaration externproto) {
        throw new RuntimeException("Not yet implemented");
    }

    /**
     * Remove the externproto declaration registered under the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public void removeExternProtoDeclaration(String name) {
        theScene.getExternProtos().remove(name);
        //throw new RuntimeException("Not yet implemented");
    }

    /**
     * Get a list of the nodes that have been named with DEF or imported using
     * the IMPORT keyword in this scene. The map is keyed by the name string
     * and the values are the {@link X3DNode} instances. If there are no nodes
     * marked with DEF or IMPORT then the map will be empty.
     *
     * @return A map of the DEF'd nodes.
     */
    public String[] getNamedNodes() {
        Set keys=theScene.getDEFNodes().keySet();
        String result[]=new String[keys.size()];
        return (String[]) keys.toArray(result);
    }

    /**
     * Get the node instance represented by the given name.
     */
    public X3DNode getNamedNode(String name) {
        VRMLNodeType namedNode=(VRMLNodeType)(theScene.getDEFNodes().get(name));
        if (namedNode==null)
            throw new InvalidNodeException(name);
        return nodeFactory.getSAINode(namedNode);
    }

    /** @see X3DExecutionContext#removeNamedNode */
    public void removeNamedNode(String name) {
        theScene.getDEFNodes().remove(name);
    }

    /** @see X3DExecutionContext#updateNamedNode */
    public void updateNamedNode(String nodeName, X3DNode node) {
        VRMLNodeType realNode=nodeFactory.getVRMLNode(node);
        theScene.getDEFNodes().put(nodeName,realNode);
    }

    /**
     * Get the list of current root nodes in the scene. The array contains the
     * items in the order they were added or declared in the initial file or
     * added after the initial scene was created.
     *
     * Since there is one and only one root node in the underlying code,
     * this method is actually addressing the first layer of nodes below
     * the root node.
     *
     * @return The list of root nodes in the scene
     */
    public X3DNode[] getRootNodes() {
        VRMLWorldRootNodeType root=
            (VRMLWorldRootNodeType)(theScene.getRootNode());
        VRMLNodeType rootNodes[]=root.getChildren();
        X3DNode result[]=new X3DNode[rootNodes.length];
        for (int counter=0; counter<result.length; counter++) {
            result[counter]=nodeFactory.getSAINode(rootNodes[counter]);
        }
        return result;
    }

    /**
     * Create a new node in this scene. The node creation uses the pre-set
     * profile and component information to ensure the validity of what the
     * user wishes to create. If the named node is not in one of the defined
     * profile or components then an exception is generated. This cannot be
     * used to create nodes that are declared as protos or extern protos in
     * this scene. You must use the proto-specific mechanisms for that.
     *
     * @param name The node's name to create
     * @return The node pointer that represents the given name
     * @throws InvalidNodeException The name does not represent a node in the
     *    given list of profile and components for this scene
     */
    public X3DNode createNode(String name) {

        VRMLNodeType node =
            (VRMLNodeType)(theScene.getNodeFactory().createVRMLNode(name,
                                                                    false));

        node.setErrorReporter(errorReporter);

        if (node instanceof VRMLExternalNodeType)
            ((VRMLExternalNodeType)node).setWorldUrl(theScene.getWorldRootURL());

        node.setFrameStateManager(stateManager);
        
        return nodeFactory.getSAINode(node);
    }

    /**
     * Create a new proto instance in this scene. The node creation uses the
     * name space semantics to locate the appropriate proto. This may require
     * the browser to first look in the local proto space and then walk
     * backwards up the proto declaration spaces to find a match.
     *
     * @param name The proto's name to create
     * @return The node pointer that represents the given proto
     * @throws InvalidNodeException The name does not represent a known proto
     *    declaration in the available namespaces
     */
    public X3DProtoInstance createProto(String name) {
        X3DProtoDeclaration decl=getProtoDeclaration(name);
        if (decl==null) throw new InvalidNodeException("No proto with name "+name+" exists.");
        return decl.createInstance();
    }

    // Convenience methods
    /** Returns the VRMLNodeFactory that this scene has access to.
      * Mainly used by SAIBrowser if the user hasn't made a scene yet
      * and asks what the system can do.
      */
    VRMLNodeFactory getVRMLNodeFactory() {
        return theScene.getNodeFactory();
    }

}
