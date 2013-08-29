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

package org.web3d.vrml.scripting.sai;

// External imports
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.Map;

// Local imports
import org.web3d.x3d.sai.*;

import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.lang.WriteableSceneMetaData;
import org.web3d.vrml.nodes.FrameStateManager;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;

import org.xj3d.core.eventmodel.RouteManager;

/**
 * Representation of a complete X3D scene graph.
 * <p>
 *
 * All queries to this interface return a snapshot of the current information.
 * If the scenegraph changes while the end user has a handle to an map, the map
 * shall not be updated to reflect the new internal state. If the end user adds
 * something to the maps, it shall not be representing in the underlying scene.
 *
 * @author Justin Couch
 * @version $Revision: 1.10 $
 */
class WorldScene extends BaseExecutionContext
    implements X3DScene {

    /** Metadata relating to this scene. */
    private WriteableSceneMetaData metadata;

    /** map containing metadata info */
    private Map metaMap;

    /** Root node of the scene */
    private VRMLWorldRootNodeType rootNode;

    /**
     * Construct an execution context descriptor for the given information.
     *
     * @param space The space to source information for this scene
     * @param rm A route manager for users creating/removing routes
     * @param fsm state manager for dealing with dynamic scene graph changes
     * @param profile Instance of the ProfileInfo that describes this scene
     * @param refQueue The queue used for dealing with field references
     * @param fac Factory used to create field wrappers
     * @param fal The access listener for propogating s2 requests
     * @param bnf The factory used to create node wrappers
     */
    public WorldScene(VRMLExecutionSpace space,
                      RouteManager rm,
                      FrameStateManager fsm,
                      ProfileInfo profile,
                      ReferenceQueue refQueue,
                      FieldFactory fac,
                      FieldAccessListener fal,
                      BaseNodeFactory bnf) {

        super(space, rm, fsm, profile, refQueue, fac, fal, bnf);

        metadata = (WriteableSceneMetaData)scene.getMetaData();

        if(space instanceof VRMLWorldRootNodeType)
            rootNode = (VRMLWorldRootNodeType)space;
        else
            System.out.println("Uh oh, world root not a world root!");
    }

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
    public WorldScene(VRMLExecutionSpace space,
                      RouteManager rm,
                      FrameStateManager fsm,
                      ProfileInfo profile,
                      ComponentInfo[] components,
                      ReferenceQueue refQueue,
                      FieldFactory fac,
                      FieldAccessListener fal,
                      BaseNodeFactory bnf) {

        super(space, rm, fsm, profile, refQueue, fac, fal, bnf);

        metadata = (WriteableSceneMetaData)scene.getMetaData();

        if(space instanceof VRMLWorldRootNodeType)
            rootNode = (VRMLWorldRootNodeType)space;
        else
            System.out.println("Uh oh, world root not a world root!");
    }

    /**
     * Add a meta tag data item to this scene. Both name and value must be
     * non-null.
     *
     * @param name The name of the tag to add
     * @param value The value of the tag
     * @throws NullPointerException The name or value were null
     */
    public void addMetaData(String name, String value) {
        if(value == null)
            throw new NullPointerException("Value was null");

        if(name == null)
            throw new NullPointerException("Name was null");

        metadata.addMetaData(name, value);
    }

    /**
     * Get the meta data mapping from this scene. The map returned cannot
     * be changed and represents the current internal state.
     *
     * @return The current meta tag mappings
     */
    public Map getMetaData() {
        if(metaMap == null)
            metaMap = metadata.getMetaData();

        return Collections.unmodifiableMap(metaMap);
    }

    /**
     * Remove the named tag from the map. If tag name does not exist, the
     * request is silently ignored.
     *
     * @param name The name of the tag to remove
     */
    public void removeMetaData(String name) {
        metadata.removeMetaData(name);
    }

    /** @see X3DExecutionContext#getExportedNode */
    public X3DNode getExportedNode(String name) {
        throw new RuntimeException("getExportedNode not yet implemented.");
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
System.out.println("Scene.getExportedNode() not implemented yet");
        return null;
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
System.out.println("Scene.addExportedNode() not implemented yet");
    }

    /**
     * Remove an exported name from the scene. If the node is not currently
     * exported this is silently ignored. The name is first checked against
     * the explicit export names and then against the DEF names that are
     * exported without using the AS keyword.
     *
     * @param name The exported name to remove
     */
    public void removeExportedNode(String name) {
System.out.println("Scene.removeExportedNode() not implemented yet");
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
        BaseNode b_node = (BaseNode)node;

        if(!b_node.isRealized())
            b_node.realize();

        VRMLNodeType real_node = b_node.getImplNode();

        ((VRMLWorldRootNodeType)rootNode).addChild(real_node);
    }

    /**
     * Remove the root node from the scene graph. This must be a valid child node
     * type or proto instance, as per the VRML specification. If the node is
     * not a root node then an exception is generated.
     */
    public void removeRootNode(X3DNode node) {
        BaseNode b_node = (BaseNode)node;
        VRMLNodeType real_node = b_node.getImplNode();

        ((VRMLWorldRootNodeType)rootNode).removeChild(real_node);
    }
}
