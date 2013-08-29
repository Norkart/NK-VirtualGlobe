/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// External imports
import java.util.Map;

// Local imports
// none

/**
 * Abstract representation of a complete X3D scene graph.
 * <p>
 *
 * All queries to this interface return a snapshot of the current information.
 * If the scenegraph changes while the end user has a handle to an map, the map
 * shall not be updated to reflect the new internal state. If the end user adds
 * something to the maps, it shall not be representing in the underlying scene.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public interface X3DScene extends X3DExecutionContext {

    /**
     * Add a meta tag data item to this scene. Both name and value must be
     * non-null.
     *
     * @param name The name of the tag to add
     * @param value The value of the tag
     * @throws NullPointerException The name or value were null
     */
    public void addMetaData(String name, String value);

    /**
     * Get the meta data mapping from this scene. The map returned cannot
     * be changed and represents the current internal state.
     *
     * @return The current meta tag mappings
     */
    public Map getMetaData();

    /**
     * Remove the named tag from the map. If tag name does not exist, the
     * request is silently ignored.
     *
     * @param name The name of the tag to remove
     */
    public void removeMetaData(String name);

    /**
     * Get the node which is exported from the scene under a given
     *
     * @param name The export name to find
     * @return The node representing the export
     */
    public X3DNode getExportedNode(String name);

    /**
     * Get the list of exports from this file. The map is the exported name
     * to the node instance. If there are no nodes exported, the map will be
     * empty. Note that exported nodes is not a valid concept for VRML97. It
     * only exists for X3D V3.0 and above.
     *
     * @return A map of the exported nodes
     */
    public String[] getExportedNodes();

    /**
     * Add a named node to this scene. The node must be a valid member of
     * this scene already. If not, an error is generated. An exported name
     * may be null, in which case the local name is used as the export name
     *
     * @param localName The local DEF name in this scene
     * @param exportName The name to export the node as, or null
     */
    public void updateExportedNode(String localName, String exportName);

    /**
     * Remove an exported name from the scene. If the node is not currently
     * exported this is silently ignored. The name is first checked against
     * the explicit export names and then against the DEF names that are
     * exported without using the AS keyword.
     *
     * @param name The exported name to remove
     */
    public void removeExportedNode(String name);

    /**
     * Add a new root node to the scene graph. This must be a valid child node
     * type or proto instance that can be a child node, as per the VRML
     * specification. If the node is already part of this scene, this is
     * treated as an implicit USE of the node at the root of the scene graph
     * and the normal rules will apply.
     *
     * @param node The node pointer to add to the scene
     */
    public void addRootNode(X3DNode node);

    /**
     * Remove the root node from the scene graph. This must be a valid child node
     * type or proto instance, as per the VRML specification. If the node is
     * not a root node then an exception is generated.
     */
    public void removeRootNode(X3DNode node);
}
