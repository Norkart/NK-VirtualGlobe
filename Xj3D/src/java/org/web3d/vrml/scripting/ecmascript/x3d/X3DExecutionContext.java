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

package org.web3d.vrml.scripting.ecmascript.x3d;

// External imports
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.FunctionObject;
import org.mozilla.javascript.Scriptable;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.scripting.ecmascript.builtin.NodeImplSource;
import org.web3d.vrml.scripting.ecmascript.builtin.MFNode;
import org.web3d.vrml.scripting.ecmascript.builtin.SFNode;

import org.xj3d.core.eventmodel.RouteManager;

import org.web3d.vrml.scripting.ecmascript.builtin.AbstractScriptableObject;

/**
 * X3DExecutionContext prototype object.
 * <P>
 *
 * @author Justin Couch
 * @version $Revision: 1.20 $
 */
public class X3DExecutionContext extends AbstractScriptableObject {

    /**
     * Error message for when we were expecting a Route instance and got
     * something else in the deleteRoute method.
     */
    private static final String INVALID_ROUTE_TYPE_MSG =
        "deleteRoute() was expecting an object of type Route, but got ";

    /**
     * Error message for when we were expecting a SFNode instance and got
     * something else in the addRoute method's first argument.
     */
    private static final String INVALID_ROUTE_SRC_MSG =
        "addRoute() fromNode argument was expecting an object of type SFNode " +
        "but got ";

    /**
     * Error message for when we were expecting a SFNode instance and got
     * something else in the addRoute method's third argument.
     */
    private static final String INVALID_ROUTE_DEST_MSG =
        "addRoute() toNode argument was expecting an object of type SFNode " +
        "but got ";

    /**
     * Message when the user attempts to write to rootNodes in the case where
     * it is not writable.
     */
    private static final String READ_ONLY_ROOT_MSG =
        "Attempting to write to the rootNodes variable inside a proto " +
        "instance is not permitted by the specification. See 19777-1 7.3.3" +
        "for more information";

    /** The encoding is ASCII */
    private static final String ASCII_ENCODING = "ASCII";

    /** The encoding is VRML */
    private static final String VRML_ENCODING = "VRML";

    /** The encoding is XML */
    private static final String XML_ENCODING = "XML";

    /** The encoding is BINARY */
    private static final String BINARY_ENCODING = "BINARY";

    /** The encoding is SCRIPTED */
    private static final String SCRIPTED_ENCODING = "SCRIPTED";

    /** The encoding is BIFS */
    private static final String BIFS_ENCODING = "BIFS";

    /** The encoding is NONE */
    private static final String NONE_ENCODING = "NONE";

    /** Default specification version */
    private static final String DEFAULT_SPEC_VERSION = "3.0";

    /** Set of the valid property names for this object */
    protected static HashSet propertyNames;

    /** Set of the valid function names for this object */
    protected static HashSet functionNames;

    /** The function objects to maintain */
    private HashMap functionObjects;

    /** The version of the specification represented */
    protected final String specVersion;

    /** The encoding type constant */
    protected final String encoding;

    /** The list of profiles in use */
    protected final ProfileInfo profile;

    /** The list of components in use */
    protected final ComponentInfoArray components;

    /** Class that represents the external reporter */
    protected ErrorReporter errorReporter;

    /** The world URL */
    protected final String url;

    /** Listing of root nodes */
    protected MFNode rootNodes;

    /** The array of protos declared */
    protected ProtoDeclarationArray protos;

    /** The list of extern protos declared */
    protected ExternProtoDeclarationArray externprotos;

    /** The list of routes in use */
    protected RouteArray routes;

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

    /** map containing imports info */
    protected Map importMap;

    /** Flag to indicate part of the scene infrastructure has changed */
    protected boolean sceneChanged;

    /** Flag indicating the rootNodes itself has changed */
    private boolean rootChanged;

    /** Flag indicating that the contents of the rootNodes has changed */
    private boolean rootContentsChanged;

    static {
        propertyNames = new HashSet();
        propertyNames.add("specificationVersion");
        propertyNames.add("encoding");
        propertyNames.add("profile");
        propertyNames.add("components");
        propertyNames.add("worldURL");
        propertyNames.add("rootNodes");
        propertyNames.add("protos");
        propertyNames.add("externprotos");
        propertyNames.add("routes");

        functionNames = new HashSet();
        functionNames.add("addRoute");
        functionNames.add("deleteRoute");
        functionNames.add("createNode");
        functionNames.add("createProto");
        functionNames.add("getImportedNode");
        functionNames.add("updateImportedNode");
        functionNames.add("removeImportedNode");
        functionNames.add("getNamedNode");
        functionNames.add("updateNamedNode");
        functionNames.add("removeNamedNode");
    }

    /**
     * Construct an execution context descriptor for the given information.
     *
     * @param space The space to source information for this scene
     * @param rm A route manager for users creating/removing routes
     * @param profile Instance of the ProfileInfo that describes this scene
     */
    public X3DExecutionContext(VRMLExecutionSpace space,
                               RouteManager rm,
                               FrameStateManager fsm,
                               ProfileInfo profile) {
        super((space.getContainedScene() instanceof VRMLScene) ?
              "Scene" : "X3DExecutionContext");

        sceneChanged = false;
        rootChanged = false;
        rootContentsChanged = false;

        functionObjects = new HashMap();
        nodeWrapperList = new ArrayList();
        nodeWrapperMap = new HashMap();

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        routeManager = rm;
        executionSpace = space;
        stateManager = fsm;
        scene = executionSpace.getContainedScene();
        nodeFactory = scene.getNodeFactory();

        SceneMetaData md = scene.getMetaData();

        specVersion = md.getVersion();
        this.profile = profile;

        BasicScene internalScene = space.getContainedScene();

        // Set up the profile and component listings. Note name class for
        // the internal and Spec-required objects.
        VRMLNodeFactory node_fac = internalScene.getNodeFactory();

        org.web3d.vrml.lang.ComponentInfo[] c_list =
            node_fac.getAvailableComponents();

        ComponentInfo[] ecma_comps = new ComponentInfo[c_list.length];

        for(int i = 0; i < c_list.length; i++)
            ecma_comps[i] = new ComponentInfo(c_list[i]);

        components = new ComponentInfoArray(ecma_comps);

        this.url = scene.getWorldRootURL();

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
                encoding = NONE_ENCODING;
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

        ProtoDeclaration[] proto_list = new ProtoDeclaration[num_proto];
        ExternProtoDeclaration[] ep_list = new ExternProtoDeclaration[num_ep];

        num_proto = 0;
        num_ep = 0;

        for(int i = 0; i < size; i++) {
            Object t = templates.get(i);
            if(t instanceof VRMLProtoDeclare) {
                proto_list[num_proto] =
                    new ProtoDeclaration((VRMLProtoDeclare)t,
                                         internalScene);
                num_proto++;
            } else {
                ep_list[num_ep] =
                    new ExternProtoDeclaration((VRMLExternProtoDeclare)t,
                                               internalScene);
                num_ep++;
            }
        }

        // TODO:
        // None of these items currently handle dynamic changes to the listing.
        // They're going to need a lot more work to deal with that - probably
        // by also passing in the current Scene instance so that we can
        // dynamically query for changes in the lists.

        protos = new ProtoDeclarationArray(proto_list);
        externprotos = new ExternProtoDeclarationArray(ep_list);

        // Route listing.
        ArrayList src_routes = scene.getRoutes();

        Route[] route_list = new Route[src_routes.size()];

        for(int i = 0; i < src_routes.size(); i++) {
            ROUTE r = (ROUTE)src_routes.get(i);

            VRMLNodeType src_node = (VRMLNodeType)r.getSourceNode();
            int src_idx = r.getSourceIndex();
            VRMLNodeType dest_node = (VRMLNodeType)r.getDestinationNode();
            int dest_idx = r.getDestinationIndex();

            // now find the field names for the two index values
            VRMLFieldDeclaration decl = src_node.getFieldDeclaration(src_idx);
            String src_field = decl.getName();

            decl = dest_node.getFieldDeclaration(dest_idx);
            String dest_field = decl.getName();

            SFNode src = new SFNode(src_node);
            src.setParentScope(this);

            SFNode dest = new SFNode(dest_node);
            dest.setParentScope(this);

            route_list[i] = new Route(src, src_field, dest, dest_field);
        }

        routes = new RouteArray(route_list);


        // Setup rootNodes property as a MFNode
        if(space instanceof VRMLWorldRootNodeType) {
            VRMLWorldRootNodeType node = (VRMLWorldRootNodeType)space;
            int field_idx = node.getFieldIndex("children");
            VRMLNodeType[] children = node.getChildren();
            rootNodes = new MFNode(node, field_idx, children, children.length);
            rootNodes.setParentScope(this);
        } else if(space instanceof VRMLProtoInstance) {
            VRMLProtoInstance proto = (VRMLProtoInstance)space;
            VRMLNodeType[] root_nodes = proto.getBodyNodes();
            rootNodes = new MFNode(proto, 0, root_nodes, root_nodes.length);
            rootNodes.setReadOnly();
            rootNodes.setParentScope(this);
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

    /**
     * Check for the named property presence.
     *
     * @return true if it is a defined eventOut or field
     */
    public boolean has(String name, Scriptable start) {
        return (propertyNames.contains(name) || functionNames.contains(name));
    }

    /**
     * Get the value of the named function. If no function object is
     * registex for this name, the method will return null.
     *
     * @param name The variable name
     * @param start The object where the lookup began
     * @return the corresponding function object or null
     */
    public Object get(String name, Scriptable start) {
        Object ret_val = NOT_FOUND;

        if(propertyNames.contains(name)) {
            char prop = name.charAt(0);
            char ch;

            switch(prop) {
                case 's':
                    ret_val = specVersion;
                    break;

                case 'c':
                    ret_val = components;
                    break;

                case 'w':
                    ret_val = url;
                    break;

                case 'r':  // root node or routes
                    ch = name.charAt(2);
                    if(ch == 'o') {
                        ret_val = rootNodes;
                    } else
                        ret_val = routes;
                    break;

                case 'e':  // encoding or externprotos
                    ch = name.charAt(name.length() - 1);
                    if(ch == 'g')
                        ret_val = encoding;
                    else
                        ret_val = externprotos;
                    break;

                case 'p':   // profile or protos
                    ch = name.charAt(name.length() - 1);
                    if(ch == 'e')
                        ret_val = profile;
                    else
                        ret_val = protos;
                    break;
            }
        } else if(functionNames.contains(name)) {
            ret_val = locateFunction(name);
        }

        return ret_val;
    }

    /**
     * Sets the named property with a new value. A put usually means changing
     * the entire property. So, if the property has changed using an operation
     * like <code> e = new SFColor(0, 1, 0);</code> then a whole new object is
     * passed to us.
     *
     * @param name The name of the property to define
     * @param start The object who's property is being set
     * @param value The value being requested
     */
    public void put(String name, Scriptable start, Object value) {
        if(value instanceof Function) {
            functionObjects.put(name, value);
        } else if(name.equals("rootNodes")) {
            // Note that in section 7.3.3 of the ECMAScript spec that it
            // states that rootNodes is only writable when this is the main
            // scene. Inside a proto this is readonly, so an error should
            // be generated.
            if (executionSpace instanceof VRMLWorldRootNodeType) {
               rootNodes = (MFNode)value;
               rootNodes.setParentScope(this);
               rootNodes.realize();

               sceneChanged = true;
               rootChanged = true;

            } else {
                errorReporter.warningReport(READ_ONLY_ROOT_MSG, null);
            }
        }
    }

    //----------------------------------------------------------
    // Local methods used by ECMAScript
    //----------------------------------------------------------

    /**
     * addRoute() function defined by From table 6.6.
     */
    public Route jsFunction_addRoute(Scriptable fn,
                                     String fromField,
                                     Scriptable tn,
                                     String toField) {
        if(!(fn instanceof NodeImplSource))
            Context.reportRuntimeError(INVALID_ROUTE_SRC_MSG + fn.getClass());

        if(!(tn instanceof NodeImplSource))
            Context.reportRuntimeError(INVALID_ROUTE_DEST_MSG + tn.getClass());


        NodeImplSource from_source = (NodeImplSource)fn;
        NodeImplSource to_source = (NodeImplSource)tn;

        VRMLNodeType src_node = from_source.getImplNode();
        VRMLNodeType dest_node = to_source.getImplNode();

        SFNode from_node = (fn instanceof SFNode) ?
                            (SFNode)fn : new SFNode(src_node);
        SFNode to_node = (tn instanceof SFNode) ?
                            (SFNode)tn : new SFNode(dest_node);

        int src_idx = src_node.getFieldIndex(fromField);
        int dest_idx = dest_node.getFieldIndex(toField);

        Route ret_val = null;

        try {
            routeManager.addRoute(executionSpace,
                                  src_node,
                                  src_idx,
                                  dest_node,
                                  dest_idx);

            ret_val = new Route(from_node, fromField, to_node, toField);
        } catch(InvalidFieldException ife) {
            errorReporter.errorReport(null, ife);
        }

        return ret_val;
    }

    /**
     * deleteRoute() function defined by From table 6.6
     */
    public void jsFunction_deleteRoute(Scriptable r) {

        if(!(r instanceof Route))
            Context.reportRuntimeError(INVALID_ROUTE_TYPE_MSG + r.getClass());

        Route route = (Route)r;

        VRMLNodeType src_node = route.srcNode.getImplNode();
        VRMLNodeType dest_node = route.destNode.getImplNode();

        int src_idx = src_node.getFieldIndex(route.srcField);
        int dest_idx = dest_node.getFieldIndex(route.destField);

        routeManager.removeRoute(executionSpace,
                                 src_node,
                                 src_idx,
                                 dest_node,
                                 dest_idx);
    }

    /**
     * createNode() function defined by From table 6.6.
     */
    public SFNode jsFunction_createNode(String name) {

        VRMLNodeType node =
            (VRMLNodeType)nodeFactory.createVRMLNode(name, false);
        node.setErrorReporter(errorReporter);

        if (node instanceof VRMLExternalNodeType)
            ((VRMLExternalNodeType)node).setWorldUrl(url);

        node.setFrameStateManager(stateManager);

        SFNode ret_val = new SFNode(node);
        ret_val.setParentScope(this);

        return ret_val;
    }

    /**
     * createProto() function defined by From table 6.6.
     */
    public SFNode jsFunction_createProto(String name) {
        VRMLNodeTemplate tmpl = scene.getNodeTemplate(name);

        VRMLNodeType node =
            (VRMLNodeType)tmpl.createNewInstance(scene.getRootNode(), false);
        node.setFrameStateManager(stateManager);

        SFNode ret_val = new SFNode(node);
        ret_val.setParentScope(this);

        return ret_val;
    }

    /**
     * getNamedNode() function defined by From table 6.6.
     */
    public SFNode jsFunction_getNamedNode(String name) {
        Map defs = scene.getDEFNodes();
        if(name == null || !defs.containsKey(name))
            return null;

        VRMLNodeType v_node = (VRMLNodeType)defs.get(name);
        // Look up to see if we have a cached wrapper of this node already.
        SFNode ret_val = (SFNode)nodeWrapperMap.get(v_node);

        if(ret_val == null) {
            ret_val = new SFNode(v_node);
            ret_val.setParentScope(this);
            nodeWrapperMap.put(v_node, ret_val);
            nodeWrapperList.add(ret_val);
        }

        return ret_val;
    }

    /**
     * updateNamedNode() function defined by From table 6.6.
     */
    public void jsFunction_updateNamedNode(String name, Scriptable node) {
errorReporter.messageReport("updateNamedNode() not implemented yet");
    }

    /**
     * removeNamedNode() function defined by From table 6.6.
     */
    public void jsFunction_removeNamedNode(String name) {
        Map defs = scene.getDEFNodes();
        if(name == null || !defs.containsKey(name))
            return;

        VRMLNodeType v_node = (VRMLNodeType)defs.remove(name);
        // Look up to see if we have a cached wrapper of this node already.
        SFNode e_node = (SFNode)nodeWrapperMap.get(v_node);

        if(e_node != null) {
            nodeWrapperMap.remove(v_node);
            nodeWrapperList.remove(e_node);
        }
    }

    /**
     * getImportedNode() function defined by From table 7.8.
     */
    public SFNode jsFunction_getImportedNode(String defName, String asName) {
errorReporter.messageReport("X3DExecutionContext does not implement getImportedNode() yet");
        return null;

/*
        Map defs = scene.getDEFNodes();

        if(importMap == null)
            importMap = scene.getImports();

        String def_name = (String)importMap.get(name);

        SFNode ret_val = null;

        if(def_name != null)
            ret_val = jsFunction_getNamedNode(name);

        return ret_val;
*/
    }

    /**
     * updateImportedNode() function defined by From table 7.8.
     */
    public void jsFunction_updateImportedNode(String name, String newName) {

errorReporter.messageReport("X3DExecutionContext does not implement updateImportedNode() yet");

        if(importMap == null)
            importMap = scene.getImports();
    }

    /**
     * getImportedNode() function defined by From table 7.8.
     */
    public void jsFunction_removeImportedNode(String name) {
        if(importMap == null)
            importMap = scene.getImports();

        // Just remove the export name from the global list.
        importMap.remove(name);
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Check to see if any of the scene variables have been changed, such as
     * the root nodes, proto definitions etc. This will reset the change
     * flag back to false.
     *
     * @return true if any of the scene structures have changed
     */
    boolean hasSceneChanged() {
        boolean ret_val = sceneChanged;

        rootChanged |= rootNodes.hasLocalChanges();
        rootContentsChanged = rootNodes.hasChanged();
        sceneChanged = false;

        return ret_val || rootContentsChanged;
    }

    /**
     * Get the list of fields that have changed. The return value may be
     * either a single {@link NodeFieldData} instance or an
     * {@link java.util.ArrayList} of field data instances if more than one
     * has changed. When called, this is recursive so that all fields and
     * nodes referenced by this node field will be included. If no fields have
     * changed, this will return null. However, that should never happen as the
     * user should always check {@link FieldScriptableObject#hasChanged()} which
     * would return false before calling this method.
     *
     * @return A single {@link NodeFieldData}, {@link java.util.ArrayList}
     *   or null
     */
    public Object getChangedData() {

        if (rootChanged && executionSpace instanceof VRMLWorldRootNodeType) {
           VRMLWorldRootNodeType node = (VRMLWorldRootNodeType)executionSpace;

           VRMLNodeType[] children = rootNodes.getRawData();
           node.setChildren(children);
           rootChanged = false;
       }

        Object ret_val = null;

        if(rootContentsChanged) {
            ret_val = rootNodes.getChangedFields();
            rootContentsChanged = false;
        }

        return ret_val;

    }

    /**
     * Get the scene object that this execution context represents
     *
     * @return The scene valid for this execution context
     */
    BasicScene getInternalScene() {
        return scene;
    }

    /**
     * Register a function object with this field type
     *
     * @param name The name to associate it with
     * @param value The object to keep this as
     */
    protected void registerFunction(String name, Object value) {
        functionObjects.put(name, value);
    }

    /**
     * Convenience method to locate a function name for this object and
     * create an appriate Function instance to represent it. It assumes that
     * the name you give it is the normal name and will add a "jsFunction_"
     * prefix to locate that from the method details. There is also the
     * implicit assumption that you have made a check for this name being a
     * valid function for this object before you call this method. If a
     * function object is found for this method, it will automatically be
     * registered and you can also have a copy of it returned to use.
     *
     * @param name The real method name to look for
     * @return The function object corresponding to the munged method name
     */
    protected FunctionObject locateFunction(String name) {
        String real_name = JS_FUNCTION_PREFIX + name;
        Method[] methods = FunctionObject.findMethods(getClass(), real_name);

        if (methods == null) {
            errorReporter.warningReport("Unknown function: " + real_name +
                                        " on: " + getClass(), null);
            return null;
        }

        FunctionObject function = new FunctionObject(name, methods[0], this);

        functionObjects.put(name, function);

        return function;
    }
}
