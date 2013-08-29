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

package org.web3d.vrml.renderer;

// External imports
import java.util.*;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.proto.PrototypeDecl;
import org.web3d.vrml.nodes.proto.ProtoScene;
import org.web3d.vrml.nodes.proto.ProtoFieldInfo;

/**
 * A class that is used to create real instances of protos from their
 * definitions.
 * <p>
 *
 * The creator strips the definition apart and builds a runtime node based on
 * the details and the node factory provided. The creator can handle one
 * instance at a time, athough it will correctly parse and build nested proto
 * declarations without extra effort.
 * <p>
 *
 * We have a small conundrum to deal with - if the proto definition contains
 * SF/MFNode fields, we don't know whether the values should be also generated
 * as real runtime nodes too. Maybe the usage of this node will provide values
 * that are dealt with after this class has finished. Other times, these defaul
 * values must be used. For this implementation, we have gone with the
 * safety-first approach: Always parse the definition of any SF or MFNode field
 * and turn those into runtime instances. Although this may create extra
 * garbage, there seems to be no nice way of dealing with this issue without a
 * completely different architecture for the library.
 * <p>
 *
 * Note:
 * Under the current implementation, EXTERNPROTOs are not yet catered for.
 *
 * @author Justin Couch
 * @version $Revision: 1.33 $
 */
public abstract class CRProtoCreator
    implements NodeTemplateToInstanceCreator,
               SceneGraphTraversalSimpleObserver {

    /** The factory that is used to generate the runtime version */
    protected VRMLNodeFactory factory;

    /** Traverser used to examine SF/MFNode fields and the proto body */
    protected SceneGraphTraverser traverser;

    /** The scene as we are building it up during traversal */
    protected ProtoScene scene;

    /** The state manager to currently use */
    protected FrameStateManager stateManager;

    /** Mapping of declaration node to the J3D version */
    protected HashMap nodeMap;

    /** Mapping of import names to the copy proxy instance */
    protected HashMap importMap;

    /** The proto instance we're building up */
    protected CRProtoInstance protoInstance;

    /**
     * The parent executionSpace used when working with instances of
     * nodes inside the field declarations.
     */
    protected VRMLExecutionSpace rootSpace;

    /** Class used to copy SF/MFNode field instances */
    protected NodeCopier nodeCopier;

    /** Class used to create the proto body nodes */
    protected ProtoCopier protoCopier;

    /** The current observer. May be node or proto copier instance */
    protected SceneGraphTraversalSimpleObserver currentObserver;

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    /** Flag to say if the current proto instance is VRML97 capable */
    protected boolean isVRML97;

    /** The major version of the spec this instance belongs to. */
    protected int majorVersion;

    /** The minor version of the spec this instance belongs to. */
    protected int minorVersion;


    /** The current world's root URL */
    protected String worldURL;

    /**
     * Create a new instance of a generic proto creator.
     *
     * @param fac The factory to use (Must generate VRMLNodeType instances)
     * @param worldURL the current world's root URL
     * @param major The major version number of this scene
     * @param minor The minor version number of this scene
     */
    protected CRProtoCreator(VRMLNodeFactory fac,
                             String worldURL,
                             int major,
                             int minor) {

        if(fac == null)
          throw new NullPointerException("No node factory supplied");

        this.worldURL = worldURL;
        factory = fac;
        majorVersion = major;
        minorVersion = minor;

        errorReporter = DefaultErrorReporter.getDefaultReporter();

        traverser = new SceneGraphTraverser();
        traverser.setObserver(this);
        traverser.setErrorReporter(errorReporter);

        nodeMap = new HashMap();
        importMap = new HashMap();

        protoCopier = new ProtoCopier(factory, worldURL);
        protoCopier.setErrorReporter(errorReporter);
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the script code can be reported in a nice, pretty fashion. Setting a
     * value of null will clear the currently set reporter. If one is already
     * set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
        else
            errorReporter = reporter;

        protoCopier.setErrorReporter(errorReporter);
        traverser.setErrorReporter(errorReporter);
        if(nodeCopier != null)
            nodeCopier.setErrorReporter(errorReporter);
    }

    /**
     * Given a stubbed instance, fill in the rest of the details.
     * This is used for extern protos who create a light instance for the
     * scenegraph and then after the EP is loaded they fill in the details.
     *
     * @param template The proto definition loaded from the EP
     * @param space The execution space this node belongs in
     * @param nodeInstance The instance to fill out
     */
    public void fillinInstance(VRMLNodeTemplate template,
                               VRMLNode nodeInstance,
                               VRMLExecutionSpace space) {

        this.protoInstance = (CRProtoInstance)nodeInstance;
        rootSpace = space;

        PrototypeDecl proto = (PrototypeDecl)template;

        List field_list = proto.getAllFields();

        VRMLFieldDeclaration[] field_decls =
            new VRMLFieldDeclaration[field_list.size()];
        field_list.toArray(field_decls);
        int idx;

        for(int i = field_decls.length - 1; i >= 0; i--) {
            idx = protoInstance.getFieldIndex(field_decls[i].getName());
            if (idx == -1) {
                try {
                    protoInstance.appendField(field_decls[i]);
                } catch(FieldExistsException fee) {
                    // Should never happen
                    errorReporter.messageReport("CRProtoCreator: Field exists?");
                }
            }
        }
        VRMLGroupingNodeType src_body = proto.getBodyGroup();

        protoInstance.setNumBodyNodes(src_body.getChildrenSize());
        protoInstance.setBackFill(true);

        finishCreate(proto);

        protoInstance.setImports(importMap);
        protoInstance.setBackFill(false);
        protoInstance.resendIS();
        protoInstance.setComplete();
        protoInstance.propagateSetupFinished();
        protoInstance.sendUpdateMessage();
    }

    //----------------------------------------------------------
    // Methods required by SceneGraphTraversalSimpleObserver
    //----------------------------------------------------------

    /**
     * Notification of a child node.
     *
     * @param parent The parent node of this node
     * @param child The child node that is being observed
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    public void observedNode(VRMLNodeType parent,
                             VRMLNodeType child,
                             int field,
                             boolean used) {

        if(child instanceof VRMLProtoInstance)
            protoNode(parent, (VRMLProtoInstance)child, field, used);
        else
            currentObserver.observedNode(parent, child, field, used);
    }

    //----------------------------------------------------------
    // Internal convenience  methods
    //----------------------------------------------------------

    /**
     * Set the frame state manager to use for the builder after
     * this point. Set a value of null it will clear the currently set items.
     *
     * @param fsm The state manager to use
     */
    public void setFrameStateManager(FrameStateManager fsm) {
        stateManager = fsm;
        protoCopier.setFrameStateManager(fsm);
    }

    /**
     * Separate handler for the proto node, which needs to be renderer-specific
     *
     * @param parent The parent node of this node
     * @param proto The proto node that has been found
     * @param field The index of the child field in its parent node
     * @param used true if the node reference is actually a USE
     */
    protected abstract void protoNode(VRMLNodeType parent,
                                      VRMLProtoInstance proto,
                                      int field,
                                      boolean used);

    /**
     * Internal method to group code together for finishing proto creation
     *
     * @param proto The decl of the proto
     */
    protected void finishCreate(PrototypeDecl proto) {
        VRMLGroupingNodeType src_body = proto.getBodyGroup();

        BasicScene parent_scene = rootSpace.getContainedScene();
        scene = new ProtoScene(majorVersion, minorVersion);
        scene.mergeScene(parent_scene);
        scene.setRootNode(protoInstance);

        protoCopier.copyNode(protoInstance,
                             scene,
                             nodeMap,
                             majorVersion,
                             minorVersion,
                             false);

        currentObserver = protoCopier;

        // Traverse the body of the proto. We can't just pass the body node in
        // because the traverser will give us the root node back, where we
        // want to know about it's children. So we do the first part of the
        // process here
        VRMLNodeType[] children = src_body.getChildren();

        traverser.reset();

        for(int i = 0; i < children.length; i++)
            traverser.traverseGraph(protoInstance, children[i]);

        traverser.reset();

        // If there is an SF/MFNode field in the decl, we need to traverse that
        // and turn it into something real.

        // process the DEFs and IMPORTs first so that the clones are ready
        // to use by the other updates
        processProtoDecls(proto.getPrototypeDecls());
        processDEFs(proto.getDEFMap());
        processImports(proto.getImportDecls());

        // Look at all the routes and build those from the nodeMap.
        // then process the IS mapping information
        processRoutes(proto.getRouteDecls());

        processIS(proto.getISMaps(), proto);

        // Now all the setup is done, grab the field values and assign those
        // to the proto interface. Because of the IS mapping now in place, that
        // should ensure all the contained nodes get set with the right values.
        processFields(proto);

        // All done now!
        protoInstance.setContainedScene(scene);

        // Clean up temporary handlers
        nodeMap.clear();
        importMap.clear();
    }

    /**
     * Process the collection of prototypes declarations that this proto represents
     */
    private void processProtoDecls(List<VRMLNodeTemplate> protos) {
        for(int i = 0; i < protos.size(); i++) {
            scene.addTemplate(protos.get(i));
        }
    }

    /**
     * Process the DEF map to create another map that now contains the copied,
     * live versions of the nodes, rather than the internal versions.
     */
    private void processDEFs(Map defMappings) {
        Set def_set = defMappings.entrySet();
        Iterator itr = def_set.iterator();

        while(itr.hasNext()) {
            Map.Entry e = (Map.Entry)itr.next();
            String name = (String)e.getKey();
            Object src_node = e.getValue();
            Object dest_node = nodeMap.get(src_node);

            scene.addDEFNode(name, (VRMLNodeType)dest_node);
        }
    }

    /**
     * Process the IMPORT representations to create cloned versions ready for
     * the route processing.
     *
     * @param importDecls The list of import declarations to process
     * @param srcProto The declaration to take the field vlaues from
     */
    private void processImports(Map importDecls) {

        // clone each of the imports and then look up the real versions of
        // each inline and set that as the real node instance.
        Set entries = importDecls.entrySet();
        Iterator itr = entries.iterator();
        Map def_map = scene.getDEFNodes();
        HashMap export_map = new HashMap();

        while(itr.hasNext()) {
            Map.Entry e = (Map.Entry)itr.next();
            try {
                ImportNodeProxy value = (ImportNodeProxy)e.getValue();
                Object key = e.getKey();

                ImportNodeProxy proxy = (ImportNodeProxy)value.clone();

                String inline = value.getInlineDEFName();
                Map exports = (Map)export_map.get(inline);
                if(exports == null) {
                    exports = new HashMap();
                    export_map.put(inline, exports);

                    VRMLInlineNodeType i_node = (VRMLInlineNodeType)def_map.get(inline);
                    i_node.setImportNodes(exports);
                }

                exports.put(proxy.getExportedName(), proxy);
                importMap.put(key, proxy);

                // Update the node map so that routes can find the new output
                nodeMap.put(value, proxy);
            } catch(CloneNotSupportedException cnse) {
                errorReporter.warningReport("Error during clone of an import " +
                                            "proxy during proto creation", cnse);
            }
        }
    }

    /**
     * Process the routes from the declared versions and place those in the
     * proto's scene info.
     *
     * @param declRoutes The routes from the declaration
     */
    private void processRoutes(Set declRoutes) {
        if(declRoutes.size() == 0)
            return;

        Iterator itr = declRoutes.iterator();

        ROUTE src_route;
        ROUTE dest_route;
        VRMLFieldDeclaration field_decl;
        VRMLNodeType src_node;
        VRMLNodeType dest_node;
        VRMLNode tmp_node;

        int src_index;
        int dest_index;

        while(itr.hasNext()) {
            src_route = (ROUTE)itr.next();

            src_node = (VRMLNodeType)nodeMap.get(src_route.getSourceNode());

            if(src_node == null) {
                errorReporter.warningReport(
                    "Unable to find matching source node copy for route " +
                     src_route, null);
                continue;
            }

            dest_node =
                (VRMLNodeType)nodeMap.get(src_route.getDestinationNode());

            if(dest_node == null) {
                errorReporter.warningReport(
                    "Unable to find matching destination node copy for route "+
                     src_route, null);
                continue;
            }

            // now work out the field index, by going the long way around.
            tmp_node = src_route.getSourceNode();
            src_index = src_route.getSourceIndex();

            field_decl = tmp_node.getFieldDeclaration(src_index);
            src_index = src_node.getFieldIndex(field_decl.getName());

            tmp_node = src_route.getDestinationNode();
            dest_index = src_route.getDestinationIndex();
            field_decl = tmp_node.getFieldDeclaration(dest_index);
            dest_index = dest_node.getFieldIndex(field_decl.getName());

            dest_route =
                new CRROUTE(src_node, src_index, dest_node, dest_index);
            scene.addRoute(dest_route);
        }
    }

    /**
     * Process the IS representations to create real versions.
     *
     * @param isDecls The list of IS declarations to process
     * @param srcProto The declaration to take the field vlaues from
     */
    private void processIS(Map isDecls, PrototypeDecl srcProto) {
        if(isDecls.size() == 0)
            return;

        Set key_set = isDecls.keySet();
        Iterator itr = key_set.iterator();
        Integer src_int;
        VRMLFieldDeclaration field_decl;
        int is_index;
        int src_index;
        int dest_index;
        List src_list;
        ArrayList dest_list;
        ProtoFieldInfo src_info;
        ProtoFieldInfo dest_info;
        VRMLNodeType dest_node;

        while(itr.hasNext()) {
            src_int = (Integer)itr.next();
            src_index = src_int.intValue();
            field_decl =
                (VRMLFieldDeclaration)srcProto.getFieldDeclaration(src_index);

            is_index = protoInstance.getFieldIndex(field_decl.getName());

            src_list = (List)isDecls.get(src_int);
            Iterator field_itr = src_list.iterator();
            dest_list = new ArrayList(src_list.size());

            // loop through all of the field info decls in the list and
            // replicate versions of these based on the runtime node info
            while(field_itr.hasNext()) {
                src_info = (ProtoFieldInfo)field_itr.next();

                dest_node = (VRMLNodeType)nodeMap.get(src_info.node);

                if(dest_node == null) {
                    errorReporter.messageReport(
                        "Unable to find IS dest node copy: " + src_info +
                        "\nSource for IS " + field_decl);
                    errorReporter.messageReport("IS ignored");
                    continue;
                }

                field_decl = src_info.node.getFieldDeclaration(src_info.field);

                if(field_decl == null) {
                    errorReporter.messageReport("Invalid source field for: " +
                                                src_info.node);
                    errorReporter.messageReport("IS ignored");
                    continue;
                }

                dest_index = dest_node.getFieldIndex(field_decl.getName());
                dest_info = new ProtoFieldInfo(dest_node, dest_index);

                dest_list.add(dest_info);
            }

            protoInstance.setIsMapping(is_index, dest_list);
        }
    }

    /**
     * Take the field values from the declaration node and set them in the
     * real instance. Assumes the IS values have been already set.
     *
     * @param srcProto The declaration to take the field values from
     */
    private void processFields(PrototypeDecl srcProto) {
        List src_fields = srcProto.getAllFields();
        Iterator itr = src_fields.iterator();
        VRMLFieldDeclaration field;
        int src_index, dest_index;
        String name;
        int access;

        while(itr.hasNext()) {
            field = (VRMLFieldDeclaration)itr.next();

            access = field.getAccessType();

            // no point trying to set fields that don't have values
            if((access == FieldConstants.EVENTIN) ||
               (access == FieldConstants.EVENTOUT))
               continue;

            try {
                name = field.getName();
                src_index = srcProto.getFieldIndex(name);
                dest_index = protoInstance.getFieldIndex(name);
                VRMLFieldData raw_data = srcProto.getFieldValue(src_index);

                if(raw_data != null)
                    setProtoField(protoInstance, dest_index, field.getFieldSize(), raw_data);
            } catch(FieldException fe) {
                // We should _never_ get this!
                errorReporter.errorReport("Proto create field types don't match!", fe);
            }
        }
    }

    /**
     * Process a single field of a contained, nested proto.
     *
     * @param node The node reference we are sending the value to
     * @param field The index of the field to set the data for
     * @param fieldSize The size of field to set the data for
     * @param data The source data used to set the field
     * @throws FieldException Any one of the normal field exceptions
     */
    protected void setProtoField(VRMLNodeType node,
                                 int field,
                                 int fieldSize,
                                 VRMLFieldData data)
        throws FieldException {

        switch(data.dataType) {
            case VRMLFieldData.BOOLEAN_DATA:
                node.setValue(field, data.booleanValue);
                break;

            case VRMLFieldData.BOOLEAN_ARRAY_DATA:
                node.setValue(field, data.booleanArrayValue, data.numElements * fieldSize);
                break;

            case VRMLFieldData.INT_DATA:
                node.setValue(field, data.intValue);
                break;

            case VRMLFieldData.INT_ARRAY_DATA:
                node.setValue(field, data.intArrayValue, data.numElements * fieldSize);
                break;

            case VRMLFieldData.LONG_DATA:
                node.setValue(field, data.longValue);
                break;

            case VRMLFieldData.LONG_ARRAY_DATA:
                node.setValue(field, data.longArrayValue, data.numElements * fieldSize);
                break;

            case VRMLFieldData.FLOAT_DATA:
                node.setValue(field, data.floatValue);
                break;

            case VRMLFieldData.FLOAT_ARRAY_DATA:
                node.setValue(field, data.floatArrayValue, data.numElements * fieldSize);
                break;

            case VRMLFieldData.DOUBLE_DATA:
                node.setValue(field, data.doubleValue);
                break;

            case VRMLFieldData.DOUBLE_ARRAY_DATA:
                node.setValue(field, data.doubleArrayValue, data.numElements * fieldSize);
                break;

            case VRMLFieldData.STRING_DATA:
                node.setValue(field, data.stringValue);
                break;

            case VRMLFieldData.STRING_ARRAY_DATA:
                node.setValue(field, data.stringArrayValue, data.numElements * fieldSize);
                break;

            case VRMLFieldData.NODE_DATA:
                if(data.nodeValue != null) {
                    node.setValue(field,
                        processNodeField((VRMLNodeType)data.nodeValue));
                }
                break;

            case VRMLFieldData.NODE_ARRAY_DATA:
                if((data.nodeArrayValue != null) && (data.numElements * fieldSize != 0))
                    node.setValue(field,
                            processNodeField((VRMLNodeType[])data.nodeArrayValue,
                                             data.numElements * fieldSize),
                                  data.numElements * fieldSize);
                break;
        }
    }

    /**
     * Read and process an field of the proto declaration into the runtime
     * form. The field is an SFNode and returns the Java3D equivalent.
     *
     * @param src The source node to copy
     * @return The Java3D equivalent of it the source
     */
    private VRMLNodeType processNodeField(VRMLNodeType src) {
        if(nodeCopier == null) {
            nodeCopier = new NodeCopier(factory, worldURL);
            nodeCopier.setErrorReporter(errorReporter);
        }

        currentObserver = nodeCopier;

System.out.println("Proto contains nodes in the declaration. " +
                   "We must do something about processing the " +
                   "scene generated");

        ProtoScene scene = new ProtoScene(majorVersion, minorVersion);
        VRMLNodeType root =
            nodeCopier.copyNode(src,
                                scene,
                                rootSpace,
                                majorVersion,
                                minorVersion,
                                false);

        // Do not call this here because this is covered when the final proto
        // instance has the method called on itself. The instance will iterate
        // through all its fields and call this then.
        // root.setupFinished();

        return root;
    }

    /**
     * Read and process an array field of the proto declaration into the
     * runtime form. The field is a MFNode and returns the Java3D equivalent.
     *
     * @param src The source nodes to copy
     * @param num The number of valid entries to process
     * @return The Java3D equivalent of it the source
     */
    private VRMLNodeType[] processNodeField(VRMLNodeType[] src, int num) {

        if(nodeCopier == null)
            nodeCopier = new NodeCopier(factory, worldURL);

        currentObserver = nodeCopier;

        ProtoScene scene = new ProtoScene(majorVersion, minorVersion);
        VRMLNodeType[] dest = new VRMLNodeType[num];

System.out.println("Proto contains nodes in the declaration. " +
                   "We must do something about processing the " +
                   "scene generated");

        for(int i = 0; i < num; i++) {
            if(src[i] != null)
                dest[i] = nodeCopier.copyNode(src[i],
                                              scene,
                                              rootSpace,
                                              majorVersion,
                                              minorVersion,
                                              false);
        }

        return dest;
    }
}
