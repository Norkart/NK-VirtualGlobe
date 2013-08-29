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
// none

// Local imports
import org.web3d.util.ErrorReporter;

/**
 * Representation of a factory that can produce instances of VRMLNode
 * implementations.
 * <p>
 *
 * This interface is primarily provided to allow an independence of the
 * factory implementation from the factory functionality. Classes that need
 * to create instances of VRMLNodes would just be passed this interface
 * rather than the full factory. That then allows a single parser to be
 * passed different factories generating different output as needed. For
 * example one factory might use it to write info to a serialised network
 * stream while another uses it as a layer over a rendering API such as Java3D
 * or OpenGL.
 * <p>
 * The factory has a form of filtering mechanism in built for profile support.
 * It acts as a form of filtering when requesting nodes. If the requested node
 * is not part of the set profile list then the create calls would return null.
 * <p>
 *
 * <b>Node Creation Behaviour</b>
 * <p>
 *
 * In order to create a node, the factory has to be told what profile and any
 * optional components the nodes are to come from. Then, the createVRMLNode()
 * calls work on this information to determine whether it is legal or not to
 * create the node. If it is not, then an {@link UnsupportedNodeException} is
 * generated naming the node that is in error.
 * <p>
 *
 * Because Xj3D is also permanently "in development", we have to also recognise
 * that not all components will be completely implemented. For these, the
 * loaded file should not fail with an undefined node message when the node
 * really is valid for that component but we haven't implemented it yet. In that
 * case, the createVRMLNode() methods will return a null. Users should check for
 * this and make sure to understand the difference between this case and that of
 * the invalid node, which generates the exception. In this later case, the user
 * should continue to process the rest of the stream, while issuing a notification
 * message about the missing node implementation.
 *
 * @author Justin Couch
 * @version $Revision: 1.7 $
 */
public interface VRMLNodeFactory {

    /** Flag to say that the component level can be whatever is available */
    public static final int ANY_LEVEL = -1;

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Set the spec version that this node factory should be setting it's profile
     * information for.
     *
     * @param major The major version of the VRML/X3D spec to use
     * @param minor The minor version of the VRML/X3D spec to use
     * @throws UnsupportedSpecVersionException The profile is not unsupported by
     *    this implementation
     */
    public void setSpecVersion(int major, int minor)
        throws UnsupportedSpecVersionException;

    /**
     * Get the spec version that this node factory is currently set to.
     *
     * @return An int array of major, minor version.
     */
    public int[] getSpecVersion();

    /**
     * Get the maximum supported spec version.
     *
     * @return An int array of major, minor version.
     */
    public int[] getMaxSupportedSpecVersion();

    /**
     * Disable a component.  Typically done when something
     * finds out it cannot support the component.
     *
     * @param specVersion The spec version.  Major, Minor.
     * @param componentName The component name
     * @param level The component level and higher to disable
     */
    public void disableComponent(int[] specVersion, String componentName, int level);

    /**
     * Set the profile that is to be supported for the following nodes. Calling
     * this method will clear all previously set profile and components.
     *
     * @param profile The profile name to use
     * @throws UnsupportedProfileException The profile is not unsupported by
     *    this implementation
     */
    public void setProfile(String profile)
        throws UnsupportedProfileException;

    /**
     * Add a component level requirement to the factory nodes. If that
     * component or level is not supported, an exception is thrown. If the level
     * is ANY_LEVEL then that says to find the highest supported component.
     *
     * @param name The name of the component
     * @param level The level of the component to support
     * @return The component information declaration matching the input
     * @throws UnsupportedComponentException The component is not unsupported by
     *    this implementation
     */
    public ComponentInfo addComponent(String name, int level)
        throws UnsupportedComponentException;

    /**
     * List the all the available profiles that this factory is capable of
     * supporting. It is not the list of set profiles to filter for.
     *
     * @return A list of the supported profiles
     */
    public ProfileInfo[] getAvailableProfiles();

    /**
     * Convenience method to get just the names all the available profiles that
     * this factory is capable of supporting. It is not the list of set
     * profiles to filter for.
     *
     * @return A list of the supported profile names
     */
    public String[] getAvailableProfileNames();

    /**
     * List all of the available components that this factory is capable of
     * supporting.
     *
     * @return The definition of all the available components
     */
    public ComponentInfo[] getAvailableComponents();

    /**
     * Create a new node instance from the given node name. A best guess is
     * made to the node's profile that it belongs to. This means that it could
     * occasionally get it wrong if there are conflicting node names.
     * If the node does not exist in the set profile + component then an
     * exception is thrown. If it is part of the profile, but has not yet been
     * implemented then null is returned.
     *
     * @param nodeName The name of the node instance to create
     * @param staticNode Whether this node is will be modified
     * @return An instance of the node, uninitialised or null.
     * @throws UnsupportedNodeException The node is not part of the declared
     *    profile and components
     */
    public VRMLNode createVRMLNode(String nodeName,
                                   boolean staticNode)
        throws UnsupportedNodeException;

    /**
     * Create a new node instance of the given node that exists in the given
     * profile. If the node does not exist in that profile then an exception is
     * thrown. If it is part of the profile, but has not yet been implemented
     * then null is returned.
     *
     * @param component The name of the component to create the name for
     * @param nodeName The name of the node instance to create
     * @param staticNode Whether this node is will be modified
     * @return An instance of the node, uninitialised or null.
     * @throws UnsupportedComponentException The component is not in the list of
     *   usable components currently set
     * @throws UnsupportedNodeException The node is not part of the declared
     *    profile and components
     */
    public VRMLNode createVRMLNode(String component,
                                   String nodeName,
                                   boolean staticNode)
        throws UnsupportedComponentException, UnsupportedNodeException;

    /**
     * Create a new node instance that is a cloned copy of the given node.
     * The copy shall be a shallow copy. All of the node's direct field data
     * has full copies of the values made, but any referenced child nodes are
     * not included in the copy.
     * <p>
     * The primary use of this method is in generating proto instances from
     * a set of template nodes. The parser of the proto will be responsible
     * for making sure the children scene graphs are correctly built by
     * sucessive calls to this method. Note that this implies the source node
     * may well be from a different renderer implementation to the output
     * form.
     *
     * @param node The node instance to create a copy of
     * @param staticNode Whether this node is will be modified
     * @return An instance of the node initialised to the values or null.
     */
    public VRMLNode createVRMLNode(VRMLNode node, boolean staticNode);

    /**
     * Create a clone of this factory. The clone will have the same
     * profile and component mix already constructed.
     *
     * @return A cloned copy of this instance
     * @throws CloneNotSupportedException Was not able to clone the object
     */
    public Object clone() throws CloneNotSupportedException;
}
