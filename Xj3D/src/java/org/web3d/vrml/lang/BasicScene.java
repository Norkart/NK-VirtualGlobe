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
import java.util.Map;

// Local imports
// none

/**
 * Abstract representation of a contained scene graph.
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
 * @version $Revision: 1.19 $
 */
public interface BasicScene {

    /**
     * Get the layer ID of this scene. This method is only useful when the
     * scene is not the root scene, such as being buried in an Inline.
     *
     * @return id A non-negative layer identifier
     */
    public int getLayerId();

    /**
     * Set the layer ID of this scene. This method is only called when the
     * scene is not the root scene, such as being buried in an Inline. This
     * passes down the layer ID that the inline is kept in. The scene should
     * make sure that this is passed down to the children nodes of this scene,
     * but making sure to ignore any contained layers.
     *
     * @param id A non-negative layer identifier
     */
    public void setLayerId(int id);

    /**
     * Get the meta data associated with this scene. A scene should always have
     * some amount of metadata.
     *
     * @return The current meta data
     */
    public SceneMetaData getMetaData();

    /**
     * Get the node that forms the root of this proto body. The body will
     * contain the renderable scene graph as the first item of the node and
     * all other children as the non-renderable parts, as per the VRML spec.
     *
     * @return A reference to the root node of the scene
     */
    public VRMLNode getRootNode();

    /**
     * Get the Root URL of the world that this scene represents. This is the
     * URL minus the file name, but including any directory information.
     *
     * @return The full URL of the world's location
     */
    public String getWorldRootURL();

    /**
     * Get the URI of the world that this scene represents.  Returns null
     * for dynamically created worlds.
     *
     * @return The full URI of the world's location
     */
    public String getLoadedURI();

    /**
     * Get the node factory that represents the setup of this scene. Provided
     * so that a caller can make use of the factory to create nodes that are
     * only valid as part of the initial setup of this scene.
     *
     * @return The VRMLNodeFactory instance configured to represent this scene
     */
    public VRMLNodeFactory getNodeFactory();

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
        throws IllegalArgumentException, IllegalStateException;

    /**
     * Get the specification major version that this scene belongs to.
     *
     * @return The major version number
     */
    public int getSpecificationMajorVersion();

    /**
     * Get the specification minor version that this scene belongs to.
     *
     * @return The minor version number
     */
    public int getSpecificationMinorVersion();

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
    public void addNode(VRMLNode node);

    /**
     * Remove a node from this scene. The implementation shall silently
     * ignore a request to remove a node that does not exist in this scene.
     *
     * @param node The node to add
     */
    public void removeNode(VRMLNode node);

    /**
     * Add a new template to this scene. Each template is assumed to be
     * independent of the others. If a template contains further templates,
     * the nested instances should not be added to this scene.
     *
     * @param template The new template instance to use
     */
    public void addTemplate(VRMLNodeTemplate template);

    /**
     * Remove a template from this scene. The implementation shall silently
     * ignore a request to remove a template that does not exist in this scene.
     * If instances of this template already exist, they shall remain in the
     * scene.
     *
     * @param template The template instance to remove
     */
    public void removeTemplate(VRMLNodeTemplate template);

    /**
     * Get the list of ROUTEs declared in this scene.
     *
     * @return The list of ROUTE instances in this scene
     */
    public ArrayList getRoutes();

    /**
     * Get the list of top-level node templates declared in this scene.
     * The list will not be sorted into separate sets of PROTO and EXTERNPROTO
     * instances.
     *
     * @return The list of VRMLNodeTemplate instances in this scene
     */
    public ArrayList getNodeTemplates();

    /**
     * Get the list of nodes of the given primary type declared in this scene.
     * Proto instances may be fetched through this method too as they have
     * a primary type of ProtoInstance.
     *
     * @return The list of node instances in this scene.
     */
    public ArrayList getByPrimaryType(int type);

    /**
     * Get the list of nodes of the given secondary type declared in this
     * scene.
     *
     * @return The list of node instances in this scene.
     */
    public ArrayList getBySecondaryType(int type);

    /**
     * Get the proto defined by the name. If no proto is known by that name
     * then return null.
     *
     * @param name The name of the proto to fetch.
     * @return The proto known by that name or null
     */
    public VRMLNodeTemplate getNodeTemplate(String name);

    /**
     * Get a list of the nodes that have been named with DEF in this scene.
     * The map is keyed by the DEF name string and the values are the
     * <code>VRMLNodeType</code> instances. If there are no nodes marked with
     * DEF then the map will be empty.
     *
     * @return A map of the DEF'd nodes.
     */
    public Map getDEFNodes();

    /**
     * Get the IMPORT declaration information from this scene. There is no
     * validity checking of these statements. The map is the inline's DEF name
     * as the key to the value being a String[] that contains the inlined
     * file's exported name and the AS rename portion.
     *
     * @return A map of the imported nodes
     */
    public Map getImports();
}
