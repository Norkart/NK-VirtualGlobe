/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
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
import java.util.List;
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
 * @version $Revision: 1.6 $
 */
public interface X3DExecutionContext {

    /** The browser currently does not contain a scene */
    public static final int NO_SCENE = 0;

    /** The scene was built from userland code using scripting */
    public static final int SCRIPTED_ENCODING = 1;

    /** VRML 1.0 ASCII encoding */
    public static final int ASCII_ENCODING = 2;

    /** VRML97 classic encoding (UTF8) */
    public static final int VRML_ENCODING = 3;

    /** XML encoding */
    public static final int XML_ENCODING = 4;

    /** Binary encoding */
    public static final int BINARY_ENCODING = 5;

    /** MPEG-4 BIFS encoding */
    public static final int BIFS_ENCODING = 6;

    /**
     * The index to use for the start of a custom encoding representation
     * if an end user wants to build their own custom parser.
     */
    public static final int LAST_STD_ENCODING = 127;

    /**
     * Get the specification version name that was used to describe this
     * scene. The version is a string that is relative to the specification
     * used and is in the format "X.Y" where X and Y are integer values
     * describing major and minor versions, respectively.
     *
     * @return The version used for this scene
     */
    public String getSpecificationVersion();

    /**
     * Get the encoding of the original file type.
     *
     * @return The encoding description
     */
    public int getEncoding();

    /**
     * Get the name of the profile used by this scene. If the profile is
     * not set, will return null.
     *
     * @return The name of the profile, or null
     */
    public ProfileInfo getProfile();

    /**
     * Get the list of all the components declared in the scene. If there were
     * no components registered, this will return null.
     *
     * @return The components declared or null
     */
    public ComponentInfo[] getComponents();

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
        throws InvalidBrowserException;

    /**
     * Get the list of ROUTEs in this scene. This gives all of the top level
     * routes in the scene in the order that they are declared.
     *
     * @return The list of ROUTE instances in this scene
     */
    public X3DRoute[] getRoutes();

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
               InvalidNodeException;

    /**
     * Delete the route from this scene. If the route is not part of this scene
     * an exception is generated.
     *
     * @param route The route reference to delete
     * @throws InvalidBrowserException The dispose method has been called on
     *    this browser reference.
     */
    public void removeRoute(X3DRoute route)
        throws InvalidBrowserException;

    /**
     * Get the list of ordinary PROTO's declared in this scene. EXTERNPROTOs
     * are not included in this list.
     *
     * @return The list of proto instances in this scene
     */
    public String[] getProtosNames();

    /**
     * Get the PROTO declaration representing the given name.
     *
     * @param name The name of the proto to fetch
     */
    public X3DProtoDeclaration getProtoDeclaration(String name);

    /**
     * Add the PROTO declaration representing the given name.
     * If an existing declaration for 'name' exists, it will be
     * replaced by the new declaration.
     *
     * @param name The name to use for the declaration
     * @param proto The declaration for the name
     */
    public void updateProtoDeclaration(String name, X3DProtoDeclaration proto);

    /**
     * Remove the proto declaration registered under the given name.
     *
     * @param name The name of the proto to fetch
     */
    public void removeProtoDeclaration(String name);

    /**
     * Get the list of EXTERNPROTOs declared in this scene. The instances may
     * or may not have been loaded at this point. Check with the interface
     * declaration to see if this is the case.
     *
     * @return The list of EXTERNPROTO instances in this scene
     */
    public String[] getExternProtoNames();

    /**
     * Get the EXTERNPROTO declaration representing the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public X3DExternProtoDeclaration getExternProtoDeclaration(String name);

    /**
     * Add the EXTERNPROTO declaration representing the given name.
     * If an existing declaration for 'name' exists, it will be replaced
     * by the new declaration.
     *
     * @param name The name of the declaraction
     * @param externproto The declaration for the name
     */
    public void updateExternProtoDeclaration(String name,
                               X3DExternProtoDeclaration externproto);

    /**
     * Remove the externproto declaration registered under the given name.
     *
     * @param name The name of the externproto to fetch
     */
    public void removeExternProtoDeclaration(String name);

    /**
     * Get a list of the nodes that have been named with DEF or imported using
     * the IMPORT keyword in this scene. The map is keyed by the name string
     * and the values are the {@link X3DNode} instances. If there are no nodes
     * marked with DEF or IMPORT then the map will be empty.
     *
     * @return A map of the DEF'd nodes.
     */
    public String[] getNamedNodes();

    /**
     * Get the node instance represented by the given name.
     *
     * @param name The name of the DEF to fetch
     * @return A node wrapper representing the node
     */
    public X3DNode getNamedNode(String name);

    /**
     * Get the imported node instance represented by a given name.
     *
     * @param name The name of the import to fetch
     * @return A node wrapper representing the node
     */
    public X3DNode getImportedNode(String name);

    /**
     * Remove the IMPORT statement associated with a given local import name.
     * See 19777-2 6.4.9 namedNode handling.
     *
     * @param importName The local name used in the IMPORT
     */
    public void removeImportedNode(String importName);

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
    public void updateImportedNode(String exportedName, String importedName, X3DNode inline);

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
    public void updateNamedNode(String nodeName, X3DNode node);

    /**
     * Calling this method removes any existing mapping between a
     * given literal name and any X3D nodes.
     *
     * @param name The literal name of the mapping to remove.
     */
    public void removeNamedNode(String name);

    /**
     * Get the list of current root nodes in the scene. The array contains the
     * items in the order they were added or declared in the initial file or
     * added after the initial scene was created.
     *
     * @return The list of root nodes in the scene
     */
    public X3DNode[] getRootNodes();

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
    public X3DNode createNode(String name);

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
    public X3DProtoInstance createProto(String name);
}
