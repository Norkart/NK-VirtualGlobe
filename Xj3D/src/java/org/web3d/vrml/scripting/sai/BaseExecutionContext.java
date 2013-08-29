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

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.x3d.sai.*;

// listed explicitly because the ProfileInfo classes clash
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.SceneMetaData;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.VRMLFieldDeclaration;
import org.web3d.vrml.lang.VRMLNode;
import org.web3d.vrml.lang.VRMLNodeFactory;
import org.web3d.vrml.lang.VRMLNodeTemplate;
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;

import org.xj3d.core.eventmodel.RouteManager;

/**
 * X3DExecutionContext implementation that is used for Protos and the base
 * of a X3DScene.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.21 $
 */
public class BaseExecutionContext implements X3DExecutionContext {

    /** The version of the specification represented */
    protected final String specVersion;

    /** The encoding type constant */
    protected final int encoding;

    /** The list of profiles in use */
    protected final ProfileInfo profile;

    /** The list of components in use */
    protected ComponentInfo[] components;

    /** Class that represents the external reporter */
    protected ErrorReporter errorReporter;

    /** The world URL */
    protected final String url;

    /** Listing of root nodes */
    protected BaseNode[] rootNodes;

    /** The array of protos declared */
    protected X3DProtoDeclaration[] protos;

    /** The list of extern protos declared */
    protected X3DExternProtoDeclaration[] externprotos;

    /** The list of routes in use */
    protected X3DRoute[] routes;

    /** Local node factory to create node instances */
    private VRMLNodeFactory nodeFactory;

    /** The execution space that this context works in */
    protected VRMLExecutionSpace executionSpace;

    /** The basic scene type */
    protected BasicScene scene;

    /** Route manager for handling user added/removed routes */
    protected RouteManager routeManager;

    /** FrameState manager for creating nodes */
    protected FrameStateManager stateManager;

    /** Listing of all the valid X3DNode wrappers. */
    protected ArrayList nodeWrapperList;

    /** Mapping of the VRMLNodeType to its X3DNode wrapper */
    protected HashMap nodeWrapperMap;

    /** Mapping of the ROUTE to its X3DRoute wrapper */
    protected HashMap routeWrapperMap;

    /** Map of the proto name to it's SAI representation  */
    private HashMap protoObjectMap;

    /** Map of the externproto name to it's SAI representation  */
    private HashMap externObjectMap;

    /** Reference queue used for keeping track of field object instances */
    private ReferenceQueue fieldQueue;

    /** Factory used for field generation */
    private FieldFactory fieldFactory;

    /** Listener for dealing with the script wrapper for field access */
    private FieldAccessListener fieldAccessListener;

    /** The factory for node wrapper creation */
    private BaseNodeFactory baseNodeFactory;
    
    /**
     * Construct an execution context descriptor for the given information.
     *
     * @param space The space to source information for this scene
     * @param rm A route manager for users creating/removing routes
     * @param fsm state manager for dealing with dynamic scene graph changes
     * @param profile Instance of the ProfileInfo that describes this scene
     * @param components The component definitions for this scene
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    public BaseExecutionContext(VRMLExecutionSpace space,
                                RouteManager rm,
                                FrameStateManager fsm,
                                ProfileInfo profile,
                                ComponentInfo[] components,
                                ReferenceQueue refQueue,
                                FieldFactory fac,
                                FieldAccessListener fal,
                                BaseNodeFactory bnf) {

        this.profile = profile;
        routeManager = rm;
        executionSpace = space;
        fieldQueue = refQueue;
        fieldFactory = fac;
        stateManager = fsm;
        fieldAccessListener = fal;
        baseNodeFactory = bnf;

        this.components = components;

        scene = executionSpace.getContainedScene();
        nodeFactory = scene.getNodeFactory();
        
        nodeWrapperList = new ArrayList();
        nodeWrapperMap = new HashMap();
        routeWrapperMap = new HashMap();
        protoObjectMap = new HashMap();
        externObjectMap = new HashMap();

        SceneMetaData md = scene.getMetaData();
        specVersion = md.getVersion();
        this.url = scene.getWorldRootURL();

        errorReporter = DefaultErrorReporter.getDefaultReporter();


        switch(md.getEncoding()) {
            case SceneMetaData.SCRIPTED_ENCODING:
                encoding = SCRIPTED_ENCODING;
                break;

            case SceneMetaData.ASCII_ENCODING:
                encoding = ASCII_ENCODING;
                break;

            case SceneMetaData.VRML_ENCODING:
                encoding = VRML_ENCODING;
                break;

            case SceneMetaData.XML_ENCODING:
                encoding = XML_ENCODING;
                break;

            case SceneMetaData.BINARY_ENCODING:
                encoding = BINARY_ENCODING;
                break;

            case SceneMetaData.BIFS_ENCODING:
                encoding = BIFS_ENCODING;
                break;

            default:
                encoding = NO_SCENE;
        }
    }

    /**
     * Construct an execution context descriptor for the given information.
     *
     * @param space The space to source information for this scene
     * @param rm A route manager for users creating/removing routes
     * @param profile Instance of the ProfileInfo that describes this scene
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    public BaseExecutionContext(VRMLExecutionSpace space,
                                RouteManager rm,
                                FrameStateManager fsm,
                                ProfileInfo profile,
                                ReferenceQueue refQueue,
                                FieldFactory fac,
                                FieldAccessListener fal,
                                BaseNodeFactory bnf) {

        this(space, rm, fsm, profile, null, refQueue, fac, fal, bnf);

        SceneMetaData md = scene.getMetaData();
        org.web3d.vrml.lang.ComponentInfo[] c_list = md.getComponents();

        if(c_list != null) {
            components = new ComponentInfo[c_list.length];

            for(int i = 0; i < c_list.length; i++)
                components[i] = new SAIComponentInfo(c_list[i]);
        } else {
            components = null;
        }

        // Now process the list of node templates.
        ArrayList templates = scene.getNodeTemplates();
        int num_proto = 0;
        int num_ep = 0;
        int size = templates.size();

        for(int i = 0; i < size; i++) {
            if(templates.get(i) instanceof VRMLProtoDeclare)
                num_proto++;
            else
                num_ep++;
        }

        protos = new X3DProtoDeclaration[num_proto];
        externprotos = new X3DExternProtoDeclaration[num_ep];

        num_proto = 0;
        num_ep = 0;

        for(int i = 0; i < size; i++) {
            VRMLNodeTemplate t = (VRMLNodeTemplate)templates.get(i);
            if(t instanceof VRMLProtoDeclare) {
                protos[num_proto] =
                    new SAIProtoDeclaration(((VRMLProtoDeclare)t),
                                            executionSpace,
                                            fieldQueue,
                                            fieldFactory,
                                            fieldAccessListener,
                                            baseNodeFactory);

                protoObjectMap.put(t.getVRMLNodeName(), protos[num_proto]);
                num_proto++;
            } else {
                externprotos[num_ep] =
                    new SAIExternProtoDeclaration((VRMLExternProtoDeclare)t,
                                                  executionSpace,
                                                  fieldQueue,
                                                  fieldFactory,
                                                  fieldAccessListener,
                                                  baseNodeFactory);

                externObjectMap.put(t.getVRMLNodeName(), externprotos[num_ep]);
                num_ep++;
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by X3DExecutionContext
    //----------------------------------------------------------

    /**
     * Get the specification version name that was used to describe this
     * scene. The version is a string that is relative to the specification
     * used and is in the format "X.Y" where X and Y are integer values
     * describing major and minor versions, respectively.
     *
     * @return The version used for this scene
     */
    public String getSpecificationVersion() {
        return specVersion;
    }

    /**
     * Get the encoding of the original file type.
     *
     * @return The encoding description
     */
    public int getEncoding() {
        return encoding;
    }

    /**
     * Get the name of the profile used by this scene. If the profile is
     * not set, will return null.
     *
     * @return The name of the profile, or null
     */
    public ProfileInfo getProfile() {
        return profile;
    }

    /**
     * Get the list of all the components declared in the scene. If there were
     * no components registered, this will return null.
     *
     * @return The components declared or null
     */
    public ComponentInfo[] getComponents() {
        return components;
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
     * @throws ConnectionException An error occurred in the connection to the
     *    browser.
     */
    public String getWorldURL()
        throws InvalidBrowserException {

        return url;
    }

    /**
     * Get the list of ROUTEs in this scene. This gives all of the top level
     * routes in the scene in the order that they are declared.
     *
     * @return The list of ROUTE instances in this scene
     */
    public X3DRoute[] getRoutes() {
        ArrayList src_routes = scene.getRoutes();
        int num_routes = src_routes.size();
        X3DRoute[] ret_val = new X3DRoute[num_routes];

        for(int i = 0; i < num_routes; i++) {
            ROUTE r = (ROUTE)src_routes.get(i);
            ret_val[i] = (X3DRoute)routeWrapperMap.get(r);

            if(ret_val[i] == null) {
                VRMLNodeType sn = (VRMLNodeType)r.getSourceNode();
                int idx = r.getSourceIndex();
                VRMLFieldDeclaration sf = sn.getFieldDeclaration(idx);
                BaseNode s_xn = (BaseNode)nodeWrapperMap.get(sn);

                if(s_xn == null) {
                    //s_xn = new BaseNode(sn,
                    //                    fieldQueue,
                    //                    fieldFactory,
                    //                    fieldAccessListener);
                    s_xn = (BaseNode)baseNodeFactory.getBaseNode( sn );
                    s_xn.setAccessValid(true);
                    nodeWrapperMap.put(sn, s_xn);
                }

                VRMLNodeType dn = (VRMLNodeType)r.getDestinationNode();
                idx = r.getDestinationIndex();
                VRMLFieldDeclaration df = dn.getFieldDeclaration(idx);
                BaseNode d_xn = (BaseNode)nodeWrapperMap.get(dn);

                if(d_xn == null) {
                    //d_xn = new BaseNode(sn,
                    //                    fieldQueue,
                    //                    fieldFactory,
                    //                    fieldAccessListener);
                    d_xn = (BaseNode)baseNodeFactory.getBaseNode( sn );
                    d_xn.setAccessValid(true);
                    nodeWrapperMap.put(sn, d_xn);
                }

                ret_val[i] =
                    new SAIRoute(s_xn, sf.getName(), d_xn, df.getName());
                routeWrapperMap.put(r, ret_val);
            }
        }

        return ret_val;
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

        BaseNode f_node = ((BaseNode)fromX3DNode);
        BaseNode t_node = ((BaseNode)toX3DNode);

        VRMLNodeType src_node = f_node.getImplNode();
        VRMLNodeType dest_node = t_node.getImplNode();

        int src_idx = src_node.getFieldIndex(readableField);
        int dest_idx = dest_node.getFieldIndex(writableField);

        X3DRoute ret_val = null;

        try {
            routeManager.addRoute(executionSpace,
                                  src_node,
                                  src_idx,
                                  dest_node,
                                  dest_idx);

            ret_val = new SAIRoute(f_node,
                                   readableField,
                                   t_node,
                                   writableField);

            // Need to register the new route with the scene object
            // Also need the ROUTE object back from the routeManager
//                routeWrapperMap.put(r, ret_val);

        } catch(InvalidFieldException ife) {
            errorReporter.errorReport(null, ife);
        }

        return ret_val;
    }

    /**
     * Delete the route from this scene. If the route is not part of this scene
     * an exception is generated.
     *
     * @param route The route reference to delete
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void removeRoute(X3DRoute route)
        throws InvalidBrowserException {

        SAIRoute sr = (SAIRoute)route;
        VRMLNodeType src_node = sr.srcNode.getImplNode();
        VRMLNodeType dest_node = sr.destNode.getImplNode();

        int src_idx = src_node.getFieldIndex(sr.destField);
        int dest_idx = dest_node.getFieldIndex(sr.srcField);

        routeManager.removeRoute(executionSpace,
                                 src_node,
                                 src_idx,
                                 dest_node,
                                 dest_idx);

        // Need to remove the new route from the scene object
        // And the node wrapper map.
    }

    /**
     * Get the list of ordinary PROTO's declared in this scene. EXTERNPROTOs
     * are not included in this list.
     *
     * @return The list of proto instances in this scene
     */
    public String[] getProtosNames() {
        String[] ret_val = null;

        if(!protoObjectMap.isEmpty()) {
            Set s = protoObjectMap.keySet();
            ret_val = new String[s.size()];
            s.toArray(ret_val);
        }

        return ret_val;
    }

    /**
     * Get the PROTO declaration representing the given name.
     *
     * @param name The name of the proto to fetch
     */
    public X3DProtoDeclaration getProtoDeclaration(String name) {
        return (X3DProtoDeclaration)protoObjectMap.get(name);
    }

    /**
     * Add the PROTO declaration representing the given name.
     *
     * @param name The name of the proto to fetch
     */
    public void updateProtoDeclaration(String name, X3DProtoDeclaration proto) {
        SAIProtoDeclaration old_proto =
            (SAIProtoDeclaration)protoObjectMap.put(name, proto);

        if(old_proto != null) {
            VRMLNodeTemplate old_internal = old_proto.getInternalDecl();
            scene.removeTemplate(old_internal);
        }

        VRMLNodeTemplate internal =
            ((SAIProtoDeclaration)proto).getInternalDecl();
        scene.addTemplate(internal);
    }

    /**
     * Remove the proto declaration registered under the given name.
     *
     * @param name The name of the proto to fetch
     */
    public void removeProtoDeclaration(String name) {
        SAIProtoDeclaration proto =
            (SAIProtoDeclaration)protoObjectMap.remove(name);

        // should attempt to remove from the underlying scene
        if(proto != null) {
            VRMLNodeTemplate internal = proto.getInternalDecl();
            scene.removeTemplate(internal);
        }
    }

    /**
     * Get the list of EXTERNPROTOs declared in this scene. The instances may
     * or may not have been loaded at this point. Check with the interface
     * declaration to see if this is the case.
     *
     * @return The list of EXTERNPROTO instances in this scene
     */
    public String[] getExternProtoNames() {

        String[] ret_val = null;

        if(!externObjectMap.isEmpty()) {
            Set s = externObjectMap.keySet();
            ret_val = new String[s.size()];
            s.toArray(ret_val);
        }

        return ret_val;
    }

    /**
     * Get the EXTERNPROTO declaration representing the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public X3DExternProtoDeclaration getExternProtoDeclaration(String name) {
        return (X3DExternProtoDeclaration)externObjectMap.get(name);
    }

    /**
     * Add the EXTERNPROTO declaration representing the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public void updateExternProtoDeclaration(String name,
                               X3DExternProtoDeclaration externproto) {
        SAIProtoDeclaration old_proto =
            (SAIProtoDeclaration)externObjectMap.put(name, externproto);

        if(old_proto != null) {
            VRMLNodeTemplate old_internal = old_proto.getInternalDecl();
            scene.removeTemplate(old_internal);
        }

        VRMLNodeTemplate internal =
            ((SAIProtoDeclaration)externproto).getInternalDecl();
        scene.addTemplate(internal);
    }

    /**
     * Remove the externproto declaration registered under the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public void removeExternProtoDeclaration(String name) {
        SAIProtoDeclaration proto = (SAIProtoDeclaration)externObjectMap.remove(name);

        // Attempt to remove from the underlying scene
        if(proto != null) {
            VRMLNodeTemplate internal = proto.getInternalDecl();
            scene.removeTemplate(internal);
        }
    }

    /**
     * Get a list of the nodes that have been named with DEF or keyword in this
     * scene. The map is keyed by the name string and the values are the
     * {@link X3DNode} instances. If there are no nodes marked with DEF then
     * the map will be empty.
     *
     * @return A map of the DEF'd nodes.
     */
    public String[] getNamedNodes() {
        Map defs = scene.getDEFNodes();
        Set names = defs.keySet();

        String[] ret_val = new String[names.size()];
        names.toArray(ret_val);
        return ret_val;
    }

    /**
     * Get the node instance represented by the given name.
     *
     * @param name The name of the node to fetch the definition for
     * @return The node instance or null if not known
     */
    public X3DNode getNamedNode(String name) {

        Map defs = scene.getDEFNodes();
        if(name == null || !defs.containsKey(name))
            return null;

        VRMLNodeType v_node = (VRMLNodeType)defs.get(name);
        // Look up to see if we have a cached wrapper of this node already.
        X3DNode ret_val = (X3DNode)nodeWrapperMap.get(v_node);

        if(ret_val == null) {
            //ret_val = new BaseNode(v_node,
            //                       fieldQueue,
            //                       fieldFactory,
            //                       fieldAccessListener);
            ret_val = (BaseNode)baseNodeFactory.getBaseNode( v_node );
            ((BaseNode)ret_val).setAccessValid(true);
            nodeWrapperMap.put(v_node, ret_val);
            nodeWrapperList.add(ret_val);
        }

        return ret_val;
    }

    /**
     * Calling this method removes any existing mapping between a
     * given literal name and any X3D nodes.
     *
     * @param name The literal name of the mapping to remove.
     */
    public void removeNamedNode(String name) {
        scene.getDEFNodes().remove(name);
    }

    /**
     * Calling this method creates an association between
     * a literal name and a node.  If there is already a mapping
     * from a given literal name to some value, that mapping will
     * be replaced by the new mapping.  If a mapping for that literal
     * name did not exist, one will be created.  There may exist multiple
     * literal names which map to a given X3D node.
     *
     * @param nodeName The literal name to change.
     * @param node The node to map to.
     */
    public void updateNamedNode(String nodeName, X3DNode node) {
        BaseNode newNode=(BaseNode)node;
        scene.getDEFNodes().put(nodeName,newNode);
    }

    /**
     * Get the imported node instance represented by a given name.
     *
     * @param name The name of the import to fetch
     * @return A node wrapper representing the node
     */
    public X3DNode getImportedNode(String name) {
        VRMLNodeType importedNode=(VRMLNodeType) scene.getDEFNodes().get(name);

        if (importedNode==null)
            throw new InvalidNodeException(name);

        // The following code was copy-pasted from getNamedNode above.
        // Look up to see if we have a cached wrapper of this node already.
        X3DNode ret_val = (X3DNode)nodeWrapperMap.get(importedNode);

        if(ret_val == null) {
            //ret_val = new BaseNode(importedNode,
            //                       fieldQueue,
            //                       fieldFactory,
            //                       fieldAccessListener);
            ret_val = (BaseNode)baseNodeFactory.getBaseNode( importedNode );
            ((BaseNode)ret_val).setAccessValid(true);
            nodeWrapperMap.put(importedNode, ret_val);
            nodeWrapperList.add(ret_val);
        }

        return ret_val;

    }

    /**
     * Remove the IMPORT statement associated with a given local import name.
     * See 19777-2 6.4.9 namedNode handling.
     *
     * @param importName The local name used in the IMPORT
     */
    public void removeImportedNode(String importName) {
        scene.getImports().remove(importName);
    }

    /**
     * Create or modify an IMPORT from the specified inline node to
     * a given import name.  If 'importName' has already been used
     * as the local name for an import, the old value will be replaced
     * by the new value.  Otherwise the appropriate new IMPORT will
     * be constructed. See 19777-2 6.4.9 namedNode handling
     *
     * @param exportedName The node name as exported from the inline
     * @param importedName The name to use locally for the imported node
     * @param inline The "DEF'd Inline" of the IMPORT (ISO 19775:2005).
     */
    public void updateImportedNode(String exportedName,
                                   String importedName,
                                   X3DNode inline) {

        // Copied from the external SAI implimentation.
        VRMLNodeType realNode=((BaseNode)inline).getImplNode();

        while (realNode instanceof VRMLProtoInstance)
            realNode=((VRMLProtoInstance)realNode).getImplementationNode();

        if (realNode instanceof VRMLInlineNodeType) {
            VRMLInlineNodeType inlineNode=(VRMLInlineNodeType) realNode;
            VRMLScene scene=(VRMLScene) inlineNode.getContainedScene();
            VRMLNodeType foreignNode=(VRMLNodeType) scene.getExports();

            if (foreignNode==null)
                throw new InvalidNodeException(exportedName+" not found in target scene.");

            scene.getImports().put(importedName,foreignNode);
        } else
            throw new IllegalArgumentException("Designated node is not an Inline node");
    }

    /**
     * Get the list of current root nodes in the scene. The array contains the
     * items in the order they were added or declared in the initial file or
     * added after the initial scene was created.
     *
     * @return The list of root nodes in the scene
     */
    public X3DNode[] getRootNodes() {
        VRMLNode n = scene.getRootNode();
        int n_kids;
        VRMLNodeType[] kids;

        if(n instanceof VRMLGroupingNodeType) {
            VRMLGroupingNodeType root = (VRMLGroupingNodeType)n;
            n_kids = root.getChildrenSize();
            kids = root.getChildren();
        } else {
            VRMLWorldRootNodeType root = (VRMLWorldRootNodeType)n;
            n_kids = root.getChildrenSize();
            kids = root.getChildren();
        }

        X3DNode[] ret_val = new X3DNode[n_kids];
        for(int i = 0; i < n_kids; i++) {
            ret_val[i] = (X3DNode)nodeWrapperMap.get(kids[i]);

            if(ret_val[i] == null) {
                //ret_val[i] = new BaseNode(kids[i],
                //                          fieldQueue,
                //                          fieldFactory,
                //                          fieldAccessListener);
                ret_val[i] = (BaseNode)baseNodeFactory.getBaseNode( kids[i] );
                ((BaseNode)ret_val[i]).setAccessValid(true);
                nodeWrapperMap.put(kids[i], ret_val[i]);
            }
        }

        return ret_val;
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
            (VRMLNodeType)nodeFactory.createVRMLNode(name, false);

        if(node == null)
            throw new InvalidNodeException("The node \"" + name +"\" is not " +
                "valid for X3D, given the declared profile and components");

        node.setErrorReporter(errorReporter);

        if (node instanceof VRMLExternalNodeType)
            ((VRMLExternalNodeType)node).setWorldUrl(url);

        node.setFrameStateManager(stateManager);
        //BaseNode n = new BaseNode(node,
        //                          fieldQueue,
        //                          fieldFactory,
        //                          fieldAccessListener);
        BaseNode n = (BaseNode)baseNodeFactory.getBaseNode( node );
        n.setAccessValid(true);

        return n;
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
        VRMLNodeTemplate tmpl = scene.getNodeTemplate(name);

        if(tmpl == null)
            throw new InvalidNodeException("The PROTO declaration \"" + name +
                "\"is not found in this scene");

        VRMLNodeType node =
            (VRMLNodeType)tmpl.createNewInstance(scene.getRootNode(), false);

        if(node == null)
            return null;

        node.setFrameStateManager(stateManager);

        SAIProtoInstanceImpl impl =
            new SAIProtoInstanceImpl(node,
                                     fieldQueue,
                                     fieldFactory,
                                     fieldAccessListener,
                                     baseNodeFactory);
        impl.setAccessValid(true);

        return impl;
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Get the scene object that this execution context represents
     *
     * @return The scene valid for this execution context
     */
    BasicScene getInternalScene() {
        return scene;
    }

    /**
     * Process any node wrappers that have been generated, at the end of the
     * frame. This makes sure that if the user has accessed and changed one of
     * these fields that any changes made are propogated through to the
     * internals.
     */
    void updateEventOuts() {
        int num_nodes = nodeWrapperList.size();

        for(int i = 0; i < num_nodes; i++) {
            BaseNode n = (BaseNode)nodeWrapperList.get(i);
            n.updateFields();
        }
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.  Also changes the ErrorReporter
     * used by the CommonBrowser instance.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }
}
