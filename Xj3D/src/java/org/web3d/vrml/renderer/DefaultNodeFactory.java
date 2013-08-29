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

package org.web3d.vrml.renderer;

// External imports
import java.io.*;
import java.util.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLProtoInstance;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;

/**
 * Singleton implementation of the {@link org.web3d.vrml.lang.VRMLNodeFactory}
 * that produces implementations of the nodes and can be customised for a
 * specific renderer.
 * <p>
 *
 * This implementation serves two purposes - generalised node factory
 * implementation and the base of a renderer-specific implementation. The
 * class is a full instance allowing it to be used directly, but the
 * constructor is marked <code>protected</code> so that you can't create
 * an instance directly. However, you can use the static factory method to
 * fetch the singleton instance for that particular renderer type.
 * <p>
 *
 * <b>Property Files</b>
 * <p>
 *
 * Yes, there are a lot used by this class. Most of them we don't know until
 * runtime because the loading of one file is used to find the definition of
 * another file to load with further property information. There is a much
 * more in-depth tutorial on how to extend Xj3D one the website, so this is
 * just an overview.
 * <p>
 *
 * At the top of the pile is <CODE>xj3d.properties</CODE>. This file is used
 * at startup of the factory. In here, is located the list of profiles
 * supported, components supported and renderers supported. The list of
 * profiles are then used to load a separate property file for each profile,
 * with the file name <CODE><i>profile_name</i>.properties</CODE>. The
 * file name is all lower case, regardless of what the initial formal profile
 * name is. In the profiile properties file is the title and the list of
 * each component and level that is required for that profile.
 * <p>
 *
 * After the profiles, there is a list of the components that may be supported.
 * This list just points to the property file that contains the definition
 * of the component. These files may be placed anywhere, however, for the
 * inbuilt components of Xj3D, you will find the property files in this
 * package (directory). Users wishing to create custom components may place
 * them anywhere, so long as they are findable in the CLASSPATH at runtime.
 * Each file contains a title, name of the implementor and a URL of that
 * implementor. In addition, it contains the definition of the maximum level
 * supported by that component and the list of nodes that are provided at each
 * level.
 *
 * @author Justin Couch
 * @version $Revision: 2.7 $
 */
public class DefaultNodeFactory
    implements VRMLNodeFactory, Cloneable {

    /** Definition of the Java3D renderer */
    public static final String JAVA3D_RENDERER = "java3d";

    /** Definition of the null renderer */
    public static final String NULL_RENDERER = "null";

    /** Definition of the OpenGL renderer */
    public static final String OPENGL_RENDERER = "ogl";

    /** Definition of the Mobile renderer */
    public static final String MOBILE_RENDERER = "mobile";

    /** How big to increment the arrays each time */
    private static final int INC_SIZE = 4;

    /** The class listing for the constructor for clone copying */
    private static final Class[] NODE_CLASS = { VRMLNodeType.class };

    /** Message if the spec version number for VRML is not 2.0 */
    private static final String INVALID_VRML_SPEC_VERSION =
        "The only valid version number for VRML is 2.0. The minor version " +
        "provided is not 0: ";

    /** Message if the spec version number for X3D is not 3.0 or 3.1*/
    private static final String INVALID_X3D_SPEC_VERSION =
        "An invalid minor version number has been provided for X3D: ";

    /** Message if the spec version number is not 2.0 or 3.x */
    private static final String UNKNOWN_SPEC_VERSION =
        "An unknown specification major version number is provided, for " +
        "which we have no known configuration information: ";

    /** Message string when there is no component information defined */
    private static final String NO_COMP_DEF_MSG =
        "There is no definition information for the component ";

    /** Message when string the component definition file not found */
    private static final String NO_COMP_FILE_MSG =
        "Could not find the definition file for the component ";

    /** Message when string the profile definition file not found */
    private static final String NO_PROF_FILE_MSG =
        "Could not find the definition file for the profile ";

    /** Error message when class can't be found */
    private static final String CREATE_MSG =
        "New node instantiation exception";

    /** Error message for constructor having non-public access type */
    private static final String ACCESS_MSG =
        "New now IllegalAccess exception";

    /** Error message when missing the copy constructor. */
    private static final String COPY_CON_MSG =
        "Node without copy constructor: ";

    /** Error messsge when the copy constructor dies */
    private static final String EXCEPT_CON_MSG =
        "Exception in copy constructor: ";

    /**
     * Error message when the copy construction method is given a proto
     * instance to copy, rather than a real node. Proto instances should be
     * handled separately.
     */
    private static final String PROTO_CREATE_MSG =
        "Attempting to create a copy of a node that is a PROTO instance " +
        "already. This is not allowed here. The offending node to copy is: ";

    /**
     * Message string when there is a component, but not of high
     * enough level for the requested feature.
     */
    private static final String NO_COMP_LVL_MSG =
        "The component does not support the level requested ";

    // Global vars filled in at startup and not changed. These maps are keyed
    // by the Float representing the current specification version and contain
    // the appropriate internal map for all the information from that version.
    // Most of the times this is also a Map object too.

    /** The set of available profile definitions for fast lookup */
    private static HashMap globalAvailableProfileNames;

    /** Map of each component to its maximum supported level */
    private static HashMap globalComponentLevels;

    /** Map of node names to their component definitions. */
    private static HashMap globalNodeNameComponentMap;

    /** Map of a componentInfo to the nodes names as a String[] for it */
    private static HashMap globalComponentToNodeMap;

    /** Map of the node name to the level it belongs to (Integer) */
    private static HashMap globalNodeNameLevelMap;

    /** Mapping of node package to Class instances */
    private static HashMap globalNodeClassMap;

    /** Mapping of node name to Class instances */
    private static HashMap rendererNodeClassMap;

    /** Mapping of un-qualified node names to constructor instances */
    private static HashMap globalConstructorMap;

    /** Flat list of all profiles (ProfileInfo[]). Only set if someone asks for it */
    private static HashMap globalProfileList;

    /** Flat list of all components (ComponentInfo[]). Only set if someone asks for it */
    private static HashMap globalComponentList;

    /** Flat list of component names to their ComponentInfo. */
    private static HashMap globalComponentNameMap;

    /** The map of renderers available to their name prefix */
    private static HashMap globalRendererPrefixMap;

    /** Set of profile names that we've attempted to load but failed to find */
    private static HashMap globalInvalidProfiles;

    /** Set of component names that we've attempted to load but failed to find */
    private static HashMap globalInvalidComponents;


    // Definition information per factory instance set to the current spec version items.

    /** The map of the renderer to the instance of this factory */
    private static HashMap factoryInstances;

    // globals that are filled in during runtime

    /** The specification version this node factory deals with  */
    private Float specVersion;

    /** Map of each component to its maximum supported level */
    private HashMap componentLevels;

    /** Map of node names to their component definitions. */
    private HashMap nodeNameComponentMap;

    /** Map of component name strings to their ComponentInfo representation */
    private HashMap componentNameMap;

    /** Map of a componentInfo to the nodes names as a String[] for it */
    private HashMap componentToNodeMap;

    /** Map of the node name to the level it belongs to (Integer) */
    private HashMap nodeNameLevelMap;

    /** Mapping of node package to Class instances */
    private HashMap nodeClassMap;

    /** Mapping of un-qualified node names to constructor instances */
    private HashMap constructorMap;

    /** Flat list of all profiles. Only set if someone asks for it */
    private ProfileInfo[] profileList;

    /** Flat list of all components. Only set if someone asks for it */
    private ComponentInfo[] componentList;

    /** The list of components we should be filtering for */
    private String[] usableComponents;

    /** Matching list of usable levels for the given component */
    private int[] usableLevels;

    /** The size of the current list of usable components */
    private int numUsableComponents;

    /**
     * Collection of currently valid nodes given the provided profile and
     * component level declarations.
     */
    private HashSet currentValidNodes;

    /** Set of profile names that we've attempted to load but failed to find */
    private HashSet invalidProfiles;

    /** Set of component names that we've attempted to load but failed to find */
    private HashSet invalidComponents;

    /** A working array for setting values into the constructor */
    private Object[] constructorArgs;

    /** The prefix for class names when loading */
    private String namePrefix;

    /** The renderer in use by this instance */
    private final String renderer;

    /** The error errorReporter instance */
    private ErrorReporter errorReporter;

    /** The major version of the spec this file belongs to. */
    protected int majorVersion;

    /** The minor version of the spec this file belongs to. */
    protected int minorVersion;

    /**
     * Static constructor to create all the global static vars in one place
     */
    static {
        // First load the profile information basics.
        factoryInstances = new HashMap();

        globalAvailableProfileNames = new HashMap();
        globalRendererPrefixMap = new HashMap();
        globalComponentLevels = new HashMap();
        globalComponentNameMap = new HashMap();
        globalNodeNameComponentMap = new HashMap();
        globalComponentToNodeMap = new HashMap();
        globalNodeNameLevelMap = new HashMap();

        // These contain a second level of mapping with renderer
        globalNodeClassMap = new HashMap();
        globalConstructorMap = new HashMap();

        globalProfileList = new HashMap();
        globalComponentList = new HashMap();

        globalInvalidProfiles = new HashMap();
        globalInvalidComponents = new HashMap();
    }

    /**
     * Construct an instance of the factory for the given renderer type.
     *
     * @param rendererID The ID of the renderer to use for this factory
     */
    protected DefaultNodeFactory(String rendererID) {
        renderer = rendererID;
        constructorArgs = new Object[1];

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        currentValidNodes = new HashSet();
    }

    /**
     * Register an error errorReporter with the factory instance so that any errors
     * generated by the creation of nodes can be reported in a nice, pretty
     * fashion. Setting a value of null will clear the currently set errorReporter.
     * If one is already set, the new value replaces the old.
     *
     * @param errorReporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;

        if(errorReporter == null)
            this.errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

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
        throws UnsupportedSpecVersionException {

        specVersion = new Float(major + minor * 0.1f);

        // See if we can find an appropriate config file for this spec version.
        // This will toss an exception if we cannot find a config.
        InputStream is = checkForSpecConfig(specVersion);

        majorVersion = major;
        minorVersion = minor;
        Map prefix_map = (Map)globalRendererPrefixMap.get(specVersion);

        // If we've never loaded this profile before, as defined by the prefix not being
        // found, then pre-populate all the tables with the right maps.
        if(prefix_map == null) {
            prefix_map = new HashMap();
            globalRendererPrefixMap.put(specVersion, prefix_map);
            globalProfileList.put(specVersion, new ProfileInfo[0]);
            globalComponentList.put(specVersion, new ComponentInfo[0]);

            globalComponentToNodeMap.put(specVersion, new HashMap());
            globalComponentLevels.put(specVersion, new HashMap());
            globalComponentNameMap.put(specVersion, new HashMap());
            globalNodeNameLevelMap.put(specVersion, new HashMap());
            globalNodeClassMap.put(specVersion, new HashMap());
            globalConstructorMap.put(specVersion, new HashMap());
            globalInvalidProfiles.put(specVersion, new HashSet());
            globalInvalidComponents.put(specVersion, new HashSet());
            globalNodeNameComponentMap.put(specVersion, new HashMap());

            loadSpecDefinition(is, specVersion);
        }

        namePrefix = (String)prefix_map.get(renderer);
        profileList = (ProfileInfo[])globalProfileList.get(specVersion);

        componentList = (ComponentInfo[])globalComponentList.get(specVersion);
        componentToNodeMap = (HashMap)globalComponentToNodeMap.get(specVersion);
        componentLevels = (HashMap)globalComponentLevels.get(specVersion);
        componentNameMap = (HashMap)globalComponentNameMap.get(specVersion);
        nodeNameLevelMap = (HashMap)globalNodeNameLevelMap.get(specVersion);

        HashMap rendererMap = (HashMap)globalNodeClassMap.get(specVersion);

        nodeClassMap = (HashMap)rendererMap.get(renderer);
        if (nodeClassMap == null) {
            nodeClassMap = new HashMap();
            globalNodeClassMap.put(renderer, nodeClassMap);
        }

        rendererMap = (HashMap)globalConstructorMap.get(specVersion);

        constructorMap = (HashMap)rendererMap.get(renderer);
        if (constructorMap == null) {
            constructorMap = new HashMap();
            constructorMap.put(renderer, constructorMap);
        }

        invalidProfiles = (HashSet)globalInvalidProfiles.get(specVersion);
        invalidComponents = (HashSet)globalInvalidComponents.get(specVersion);
        nodeNameComponentMap = (HashMap)globalNodeNameComponentMap.get(specVersion);
    }

    /**
     * Get the spec version that this node factory is currently set to.
     *
     * @return An int array of major, minor version.
     */
    public int[] getSpecVersion() {
        return new int[] { majorVersion, minorVersion };
    }

    /**
     * Set the profile that is to be supported for subsequent node creation
     * calls. Calling this method will clear all previously set profile and
     * components.
     *
     * @param profile The name of the profile to use
     * @throws UnsupportedProfileException The profile is not unsupported by
     *    this implementation
     */
    public void setProfile(String profile)
        throws UnsupportedProfileException {

        numUsableComponents = 0;

        // Grab the profile and sort out what levels are needed
        ProfileInfo pi = null;
        for(int i = 0; i < profileList.length && pi == null; i++) {
            if(profileList[i].getName().equals(profile))
                pi = profileList[i];
        }

        if(pi == null)
            pi = loadProfile(profile);

        ComponentInfo[] ci = pi.getComponents();
        int num_comps = ci.length;

        if(usableComponents == null || usableComponents.length < num_comps) {
            usableComponents = new String[num_comps];
            usableLevels = new int[num_comps];
        }

        // Clear the previously set collection of valid nodes. Add back in
        // WorldRoot as this is an internal node that is the root of all scenes
        // and not part of any profile. Because of the way the system works, we
        // need to have this created before anyone even starts to set the

        currentValidNodes.clear();

        for(int i = 0; i < num_comps; i++) {
            String comp_name = ci[i].getName();
            int max_level = ci[i].getLevel();

            usableComponents[i] = comp_name;
            usableLevels[i] = max_level;
;
            String[] nodes = (String[])componentToNodeMap.get(comp_name);
            for(int j = 0; j < nodes.length; j++) {
                Integer lvl = (Integer)nodeNameLevelMap.get(nodes[j]);

                if(lvl.intValue() <= max_level)
                    currentValidNodes.add(nodes[j]);
            }
        }

        numUsableComponents = num_comps;
    }

    /**
     * Get the maximum supported spec version.
     *
     * @return An int array of major, minor version.
     */
    public int[] getMaxSupportedSpecVersion() {
        // TODO: Discover the real values for this
        return new int[] {3,1};
    }

    /**
     * Disable a component.  Typically done when something
     * finds out it cannot support the component.
     *
     * @param specVersion The spec version.  Major, Minor.
     * @param componentName The component name
     * @param level The component level and higher to disable
     */
    public void disableComponent(int[] specVersion, String componentName, int level) {
        System.out.println("Disable component not implemented");
    }

    /**
     * Add a component level requirement to the factory nodes. If that
     * component or level is not supported, an exception is thrown. If the level
     * is ANY_LEVEL then that says to find the highest supported component.
     *
     * @param name The name of the component
     * @param level The level of the component to support
     * @return The component information declaration matching the input
     * @throws UnsupportedComponentException The component or level is not
     *    unsupported by this implementation
     */
    public ComponentInfo addComponent(String name, int level)
        throws UnsupportedComponentException {

        Integer lvl = null;

        // Check to see if it is the list of definitions we might have
        if(!componentToNodeMap.containsKey(name)) {
            if(invalidComponents.contains(name))
                throw new UnsupportedComponentException(NO_COMP_DEF_MSG + name);
            else
                lvl = loadComponent(name);
        } else {
            lvl = (Integer)componentLevels.get(name);

            profileList = (ProfileInfo[])globalProfileList.get(specVersion);
            componentList = (ComponentInfo[])globalComponentList.get(specVersion);
        }

        if((level != ANY_LEVEL) && (lvl.intValue() < level))
            throw new UnsupportedComponentException(NO_COMP_LVL_MSG + level);

        // Need to add it to the usable list too!
        boolean found = false;

        for(int i = 0; i < numUsableComponents; i++) {
            if(name.equals(usableComponents[i])) {
                found = true;
                if(usableLevels[i] < level) {
                    usableLevels[i] = level;

                    String[] nodes = (String[])componentToNodeMap.get(name);
                    for(int j = 0; j < nodes.length; j++) {
                        Integer l = (Integer)nodeNameLevelMap.get(nodes[j]);

                        if(l.intValue() <= level)
                            currentValidNodes.add(nodes[j]);
                    }
                }
            }
        }

        // not found? Tack it onto the end of the list
        if(!found) {
            if(usableComponents == null) {
                usableComponents = new String[5];
                usableLevels = new int[5];
            } else if (numUsableComponents == usableComponents.length) {
                String[] ts = new String[numUsableComponents + INC_SIZE];
                System.arraycopy(usableComponents,
                                 0,
                                 ts,
                                 0,
                                 numUsableComponents);

                usableComponents = ts;
                int[] ti = new int[numUsableComponents + INC_SIZE];
                System.arraycopy(usableLevels, 0, ti, 0, numUsableComponents);
                usableLevels = ti;

            }

            usableComponents[numUsableComponents] = name;
            usableLevels[numUsableComponents] = level;
            numUsableComponents++;

            String[] nodes = (String[])componentToNodeMap.get(name);
            for(int j = 0; j < nodes.length; j++) {
                Integer l = (Integer)nodeNameLevelMap.get(nodes[j]);

                if(l.intValue() <= level)
                    currentValidNodes.add(nodes[j]);
            }
        }

        ComponentInfo ret_val = null;

        for(int i = 0; i < componentList.length && ret_val == null; i++) {
            if(componentList[i].getName().equals(name))
                ret_val = componentList[i];
        }

        if (ret_val == null) {
            System.out.println("Can't find comp: " + name + " level: " + level);
        }

        if((level != ANY_LEVEL) && (ret_val.getLevel() != level))
            ret_val = new ComponentInfo(ret_val, level);

        return ret_val;
    }

    /**
     * List the all the available profiles that this factory is capable of
     * supporting. It is not the list of set profiles to filter for.
     *
     * @return A list of the supported profiles
     */
    public ProfileInfo[] getAvailableProfiles() {
        return (ProfileInfo[])globalProfileList.get(specVersion);
    }

    /**
     * Convenience method to get just the names all the available profiles that
     * this factory is capable of supporting. It is not the list of set
     * profiles to filter for.
     *
     * @return A list of the supported profile names
     */
    public String[] getAvailableProfileNames() {
        return (String[])globalAvailableProfileNames.get(specVersion);
    }

    /**
     * List all of the available components that this factory is capable of
     * supporting.
     *
     * @return The definition of all the available components
     */
    public ComponentInfo[] getAvailableComponents() {
        return (ComponentInfo[])globalComponentList.get(specVersion);
    }

    /**
     * Create a new node instance from the given node name. A best guess is
     * made to the node's profile that it belongs to. This means that it could
     * occasionally get it wrong if there are conflicting node names. If it
     * does not understand the node type then it will return null.
     *
     * @param nodeName The name of the node instance to create
     * @param staticNode Whether this node is will be modified
     * @return An instance of the node, uninitialised or null.
     * @throws UnsupportedNodeException The node is not part of the declared
     *    profile and components
     */
    public synchronized VRMLNode createVRMLNode(String nodeName,
                                                boolean staticNode)
        throws UnsupportedNodeException {

        if(!currentValidNodes.contains(nodeName))
            throw new UnsupportedNodeException(nodeName);

        VRMLNode ret_val = null;

        try {
            Class cl = findNode(nodeName);

            if(cl != null) {
                ret_val = (VRMLNode)cl.newInstance();
                ret_val.setVersion(majorVersion, minorVersion, staticNode);
            }

        } catch(InstantiationException ie) {
            errorReporter.errorReport(CREATE_MSG, ie);
        } catch(IllegalAccessException iae) {
            errorReporter.errorReport(ACCESS_MSG, iae);
        }

        return ret_val;
    }

    /**
     * Create a new node instance of the given node that exists in the given
     * component. If the node does not exist in that component then null is
     * returned.
     *
     * @param component The name of the component to create the name for
     * @param nodeName The name of the node instance to create
     * @param staticNode Whether this node is will be modified
     * @return An instance of the node, uninitialised or null.
     * @throws UnsupportedComponentException The component is not in the list of
     *   usable profiles currently set
     * @throws UnsupportedNodeException The node is not part of the declared
     *    profile and components
     */
    public synchronized VRMLNode createVRMLNode(String component,
                                                String nodeName,
                                                boolean staticNode)
        throws UnsupportedComponentException, UnsupportedNodeException {

        if(!componentToNodeMap.containsKey(component)) {
            if(invalidComponents.contains(component))
                throw new UnsupportedComponentException(component);
            else
                loadComponent(component);
        } else {
            // Get the latest versions
            profileList = (ProfileInfo[])globalProfileList.get(specVersion);
            componentList = (ComponentInfo[])globalComponentList.get(specVersion);
        }

        VRMLNode ret_val = null;

        ComponentInfo ci = (ComponentInfo)nodeNameComponentMap.get(nodeName);

        if(!ci.getName().equals(component))
            throw new UnsupportedNodeException(nodeName);

        StringBuffer buf = new StringBuffer(ci.getPackage(renderer));

        try {
            // May want to make use of nodeClassMap here when the values are
            // defined in the map rather than always doing it by hand.
            buf.append('.');
            buf.append(namePrefix);
            buf.append(nodeName);

            Class c = Class.forName(buf.toString());
            ret_val = (VRMLNode)c.newInstance();
            ret_val.setVersion(majorVersion, minorVersion, staticNode);

        } catch(ClassNotFoundException cnfe) {
            // ignore
        } catch(InstantiationException ie) {
            errorReporter.errorReport(CREATE_MSG, ie);
        } catch(IllegalAccessException iae) {
            errorReporter.errorReport(ACCESS_MSG, iae);
        }

        return ret_val;
    }

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
    public synchronized VRMLNode createVRMLNode(VRMLNode node,
                                                boolean staticNode) {

        if(node instanceof VRMLProtoInstance) {
            errorReporter.errorReport(PROTO_CREATE_MSG + node.getVRMLNodeName(),
                                 null);
            return null;
        }

        VRMLNode ret_val = null;
        String node_name = node.getVRMLNodeName();

        ComponentInfo ci = (ComponentInfo)nodeNameComponentMap.get(node_name);

        StringBuffer buf = new StringBuffer(ci.getPackage(renderer));
        buf.append('.');
        buf.append(namePrefix);
        buf.append(node_name);

        String key = buf.toString();

        // lookup the constructor instance first
        Constructor constructor = (Constructor)constructorMap.get(key);

        try {
            if(constructor == null) {
                Class cl = findNode(node_name);
                if(cl != null) {
                    constructor = cl.getConstructor(NODE_CLASS);
                    constructorMap.put(key, constructor);
                }
            }

            if(constructor != null) {
                constructorArgs[0] = node;
                ret_val = (VRMLNode)constructor.newInstance(constructorArgs);
                ret_val.setVersion(majorVersion, minorVersion, staticNode);
                constructorArgs[0] = null;
            }
        } catch(InstantiationException ie) {
            errorReporter.errorReport(CREATE_MSG, ie);
        } catch(IllegalAccessException iae) {
            errorReporter.errorReport(ACCESS_MSG, iae);
        } catch(NoSuchMethodException nsme) {
            errorReporter.errorReport(COPY_CON_MSG + node_name, null);
        } catch(InvocationTargetException ite) {
            errorReporter.errorReport(EXCEPT_CON_MSG + node_name, ite);
        }

        return ret_val;
    }

    /**
     * Factory method to create an instance of the node factory that can be
     * used for a specific renderer.
     *
     * @param rendererID The ID of the renderer to use for this factory
     */
    public static VRMLNodeFactory createFactory(String rendererID) {
        VRMLNodeFactory ret_val =
            (VRMLNodeFactory)factoryInstances.get(rendererID);

        if(ret_val == null) {
            ret_val = new DefaultNodeFactory(rendererID);
            factoryInstances.put(rendererID, ret_val);
        }

        return ret_val;
    }

    /**
     * Factory method to create an new instance of the node factory every time
     * the method is called.
     *
     * @param rendererID The ID of the renderer to use for this factory
     */
    public static VRMLNodeFactory newInstance(String rendererID) {
        return new DefaultNodeFactory(rendererID);
    }

    /**
     * Convenience method to clear all the cached information so that we
     * start again with new information. This will clear all global
     * information, not just the info in the current instance.
     */
    public static void clearCachedInfo() {
        globalNodeClassMap.clear();
        globalConstructorMap.clear();
    }

    //----------------------------------------------------------
    // Overrridden Object methods
    //----------------------------------------------------------

    /**
     * Create a clone of this factory. The clone will have the same
     * profile and component mix already constructed.
     *
     * @return A cloned copy of this instance
     * @throws CloneNotSupportedException Was not able to clone the object
     */
    public Object clone()
        throws CloneNotSupportedException {

        DefaultNodeFactory fac = (DefaultNodeFactory)super.clone();

        fac.constructorArgs = new Object[1];

        if(usableComponents != null)
            fac.usableComponents = (String[])usableComponents.clone();

        if(usableLevels != null)
            fac.usableLevels = (int[])usableLevels.clone();

        fac.currentValidNodes = new HashSet();
        fac.currentValidNodes.addAll(currentValidNodes);

        fac.profileList = (ProfileInfo[])globalProfileList.get(specVersion);
        fac.componentList = (ComponentInfo[])globalComponentList.get(specVersion);

        fac.componentToNodeMap = (HashMap)globalComponentToNodeMap.get(specVersion);
        fac.componentLevels = (HashMap)globalComponentLevels.get(specVersion);
        fac.nodeNameLevelMap = (HashMap)globalNodeNameLevelMap.get(specVersion);
        fac.nodeClassMap = (HashMap)globalNodeClassMap.get(specVersion);
        fac.constructorMap = (HashMap)globalConstructorMap.get(specVersion);
        fac.invalidProfiles = (HashSet)globalInvalidProfiles.get(specVersion);
        fac.invalidComponents = (HashSet)globalInvalidComponents.get(specVersion);
        fac.nodeNameComponentMap = (HashMap)globalNodeNameComponentMap.get(specVersion);

        return fac;
    }

    //----------------------------------------------------------
    // Internal convenience methods.
    //----------------------------------------------------------

    /**
     * Convenience method to locate a class from all the avialable
     * profiles, given just the node name. If none can be found, it returns
     * null. It does not care what level is required for this node at this
     * point.
     *
     * @param name The name of the class/node to locate
     * @return the Class reference or null if not found
     */
    private Class findNode(String name) {
        Class ret_val = (Class)nodeClassMap.get(name);

        if(ret_val == null) {
            try {
                ComponentInfo ci = (ComponentInfo)nodeNameComponentMap.get(name);

                if(ci != null) {
                    StringBuffer buf = new StringBuffer(ci.getPackage(renderer));
                    buf.append('.');
                    buf.append(namePrefix);
                    buf.append(name);

                    String cls_name = buf.toString();
                    ret_val = Class.forName(cls_name);
                    nodeClassMap.put(name, ret_val);
                }
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }
        }

        return ret_val;
    }

    /**
     * Check to see if a spec definition file exists for the requested
     * version number.
     * <p>
     * Load the basic spec defined profiles and components for the given spec
     * version. This will look for config/spec_version/profiles.xml in the
     * classpath and return an input stream if it can find an appropriate match
     * or throw an exception if it cannot.
     *
     * @param version The version number to check for
     * @return A stream pointing to the file that we need to load
     * @throws UnsupportedSpecVersionException An exception with an appropriate
     *     error message for the version requested
     */
    private InputStream checkForSpecConfig(Float version)
        throws UnsupportedSpecVersionException {
        String file_loc = "config/" + version + "/profiles.xml";
        InputStream is = locateConfigFile(file_loc);

        if(is == null) {
            float v_float = version.floatValue();
            switch(version.intValue()) {
                case 2:
                    throw new UnsupportedSpecVersionException(INVALID_VRML_SPEC_VERSION + version);

                case 3:
                    throw new UnsupportedSpecVersionException(INVALID_X3D_SPEC_VERSION + version);

                default:
                    throw new UnsupportedSpecVersionException(UNKNOWN_SPEC_VERSION);
            }
        }

        return is;
    }

    /**
     * Load the basic spec defined profiles and components for the given spec
     * version. This will look for config/spec_version/profiles.xml and parse
     * that into a set of structures.
     *
     *
     * @param version The version number of the spec
     * @param is The stream to read the spec definition from
     */
    private void loadSpecDefinition(InputStream is, Float version) {
        Document doc_root = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(false);
            fac.setNamespaceAware(false);
            DocumentBuilder builder = fac.newDocumentBuilder();
            doc_root = builder.parse(is);
        } catch(FactoryConfigurationError fce) {
            errorReporter.errorReport("Can't create XML DOM factory: " +
                                      fce.getMessage(), null);
            return;
        } catch(ParserConfigurationException pce) {
            errorReporter.errorReport("Can't create XML DOM parser", pce);
            return;
        } catch(SAXException se) {
            Exception e = se.getException();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            if(e != null)
                e.printStackTrace(pw);
            else
                se.printStackTrace(pw);

            StringBuffer buf = new StringBuffer("SAX Error: ");
            buf.append(se.toString());
//                buf.append(sw.toString());
            throw new VRMLException(buf.toString());
        } catch(IOException ioe) {
            errorReporter.errorReport("Error reading profile.xml stream", ioe);
            return;
        }

        // Now we have a valid document, let's pull it apart looking for the
        // information we need. First start with the components and then work
        // to the profiles. This should help check sanity because the profiles
        // should only ask for components that we know about from this same
        // document and level supported.
        Element xj3d_cfg = doc_root.getDocumentElement();
        NodeList render_cfg = xj3d_cfg.getElementsByTagName("rendererConfig");
        processRendererConfig((Element)render_cfg.item(0), version);

        NodeList cfg_list = xj3d_cfg.getElementsByTagName("componentConfig");
        Element cfg_elem = (Element)cfg_list.item(0);

        if(cfg_elem != null) {
            NodeList component_list = cfg_elem.getElementsByTagName("component");
            Map global_levels = (Map)globalComponentLevels.get(version);
            Map global_components = (Map)globalComponentNameMap.get(version);

            int num = component_list.getLength();
            ComponentInfo[] tmp = new ComponentInfo[num];

            for(int i = 0; i < num; i++) {
                Element e = (Element)component_list.item(i);
                ComponentInfo ci = processComponentElement(e);

                global_levels.put(ci.getName(), new Integer(ci.getLevel()));
                global_components.put(ci.getName(), ci);
                tmp[i] = ci;
            }

            globalComponentList.put(version, tmp);
        }

        cfg_list = xj3d_cfg.getElementsByTagName("profileConfig");
        cfg_elem = (Element)cfg_list.item(0);

        if(cfg_elem != null) {
            NodeList profile_list = cfg_elem.getElementsByTagName("profile");

            int num_profiles = profile_list.getLength();
            String[] profile_names = new String[num_profiles];
            ProfileInfo[] profile_info = new ProfileInfo[num_profiles];

            for(int i = 0; i < num_profiles; i++) {
                Element e = (Element)profile_list.item(i);
                ProfileInfo pi = processProfileElement(e);
                profile_info[i] = pi;
                profile_names[i] = pi.getName();
            }

            globalAvailableProfileNames.put(version, profile_names);
            globalProfileList.put(version, profile_info);
        }
    }

    /**
     * Internal processing method to take an element describing a the renderer
     * config per profile.xml file and turn it into our internal data structures.
     *
     * @param configDef the XML definition of the component
     */
    private void processRendererConfig(Element configDef, Float version) {
        Map renderer_map = (Map)globalRendererPrefixMap.get(version);
        NodeList renderer_list = configDef.getElementsByTagName("renderer");

        int num = renderer_list.getLength();
        for(int i = 0; i < num; i++) {
            Element renderer = (Element)renderer_list.item(i);
            String type = renderer.getAttribute("type");
            String prefix = renderer.getAttribute("prefix");
            renderer_map.put(type, prefix);
        }
    }

    /**
     * Internal processing method to take an element describing a component
     * and turn it into our internal ComponentInfo object type.
     *
     * @param compDef the XML definition of the component
     * @return The internal representation of the same component
     */
    private ComponentInfo processComponentElement(Element compDef) {
        String comp_name = compDef.getAttribute("name");
        String title = compDef.getAttribute("title");
        String level_str = compDef.getAttribute("levels");
        String url = compDef.getAttribute("url");

        int levels = 0;

        try {
            levels = Integer.parseInt(level_str);
        } catch(NumberFormatException nfe) {
            errorReporter.warningReport("Component level definition is not an integer", null);
            return null;
        }

        HashMap package_map = new HashMap(5);
        NodeList renderer_list = compDef.getElementsByTagName("nodeLocation");
        int num = renderer_list.getLength();
        for(int i = 0; i < num; i++) {
            Element renderer = (Element)renderer_list.item(i);
            String type = renderer.getAttribute("type");
            String pkg = renderer.getAttribute("package");

            package_map.put(type, pkg);
        }

        ComponentInfo ret_val =
            new ComponentInfo(comp_name, levels, title, url, package_map);

        NodeList levels_list = compDef.getElementsByTagName("componentLevel");
        Map node_level_map = (Map)globalNodeNameLevelMap.get(specVersion);
        Map node_comp_map = (Map)globalNodeNameComponentMap.get(specVersion);

        num = levels_list.getLength();
        ArrayList all_nodes = new ArrayList();

        for(int i = 0; i < num; i++) {
            Element level_def = (Element)levels_list.item(i);
            level_str = level_def.getAttribute("level");

            int comp_level = 0;

            try {
                comp_level = Integer.parseInt(level_str);
            } catch(NumberFormatException nfe) {
                errorReporter.warningReport("Component level required is not an integer", null);
            }

            if(comp_level > levels) {
                errorReporter.errorReport("Component " + comp_name +
                                          "level definition specifies a level value " +
                                           comp_level + " greater than the max defined for " +
                                           " this component's max " + levels,  null);
                continue;
            }

            NodeList nodes_list = level_def.getElementsByTagName("node");
            int num_nodes = nodes_list.getLength();
            Integer level_int = new Integer(comp_level);

            for(int j = 0; j < num_nodes; j++) {
                Element node = (Element)nodes_list.item(j);
                String node_name = node.getAttribute("name");
                all_nodes.add(node_name);

                node_comp_map.put(node_name, ret_val);
                node_level_map.put(node_name, level_int);
            }
        }

        String name_strings[] = new String[all_nodes.size()];
        all_nodes.toArray(name_strings);

        Map comp_node_map = (Map)globalComponentToNodeMap.get(specVersion);
        comp_node_map.put(comp_name, name_strings);

        return ret_val;
    }

    /**
     * Internal processing method to take an element describing a profile
     * and turn it into our internal ProfileInfo object type.
     *
     * @param profileDef the XML definition of the profile
     * @return The internal representation of the same profile
     */
    private ProfileInfo processProfileElement(Element profileDef) {
        String name = profileDef.getAttribute("name");
        String title = profileDef.getAttribute("title");

        NodeList component_list = profileDef.getElementsByTagName("component");
        int num = component_list.getLength();
        Map global_components = (Map)globalComponentNameMap.get(specVersion);

        ComponentInfo[] components = new ComponentInfo[num];

        for(int i = 0; i < num; i++) {
            Element component = (Element)component_list.item(i);
            String comp_name = component.getAttribute("name");
            String level_str = component.getAttribute("level");
            int level = 0;

            try {
                level = Integer.parseInt(level_str);
            } catch(NumberFormatException nfe) {
                errorReporter.warningReport("Component level definition in profile is not an integer", null);
            }

            ComponentInfo comp_def = (ComponentInfo)global_components.get(comp_name);
// Need to check that this is not null, indicating a component that is invalid or
// not currently defined/loaded. Also, need to check the level is lower than the
// max that we currently support.
            components[i] = new ComponentInfo(comp_def, level);
        }

        return new ProfileInfo(name, title, components);
    }

    /**
     * Load a particular component's definition file. In response, return
     * the max level as an integer. Assumes that a check has been done for the
     * definition file mapping already exists and that it hasn't been loaded
     * before.
     *
     * @param name The name of the component to load
     * @return The next level value
     */
    private Integer loadComponent(String name)
        throws UnsupportedComponentException {

        if(invalidComponents.contains(name))
            throw new UnsupportedComponentException(NO_COMP_FILE_MSG + name);

        Integer ret_val = null;
        String file_loc = "config/" + specVersion + "/component/" + name + ".xml";

        InputStream is = locateConfigFile(file_loc);

        if(is == null) {
            invalidComponents.add(name);
            throw new UnsupportedComponentException(NO_COMP_FILE_MSG + name);
        }

        Document doc_root = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(false);
            fac.setNamespaceAware(false);
            DocumentBuilder builder = fac.newDocumentBuilder();
            doc_root = builder.parse(is);
        } catch(FactoryConfigurationError fce) {
            errorReporter.errorReport("Can't create XML DOM factory: " +
                                      fce.getMessage(), null);
            return ret_val;
        } catch(ParserConfigurationException pce) {
            errorReporter.errorReport("Can't create XML DOM parser", pce);
            return ret_val;
        } catch(SAXException se) {
            Exception e = se.getException();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            if(e != null)
                e.printStackTrace(pw);
            else
                se.printStackTrace(pw);

            StringBuffer buf = new StringBuffer("SAX Error: ");
            buf.append(se.toString() + " parsing file: " + file_loc);
//                buf.append(sw.toString());
            throw new VRMLException(buf.toString());
        } catch(IOException ioe) {
            errorReporter.errorReport("Error reading profile.xml stream", ioe);
            return ret_val;
        }

        Element cfg_elem = doc_root.getDocumentElement();
        NodeList comp_list = cfg_elem.getElementsByTagName("component");


        if(comp_list.getLength() > 0) {
            Element comp_def = (Element)comp_list.item(0);
            ComponentInfo ci = processComponentElement(comp_def);

            ret_val = new Integer(ci.getLevel());

            Map global_levels = (Map)globalComponentLevels.get(specVersion);
            Map global_components = (Map)globalComponentNameMap.get(specVersion);

            global_levels.put(ci.getName(), new Integer(ci.getLevel()));
            global_components.put(ci.getName(), ci);

            ComponentInfo[] comps = (ComponentInfo[])globalComponentList.get(specVersion);
            ComponentInfo[] tmp = new ComponentInfo[comps.length + 1];
            System.arraycopy(comps, 0, tmp, 0, comps.length);
            tmp[comps.length] = ci;

//System.out.println("comps: " + tmp.length);
            globalComponentList.put(specVersion, tmp);
            componentList = tmp;
        }

        return ret_val;
    }

    /**
     * Load the profile definition information. Each profile property file
     * contains the list of components and levels for that profile.
     *
     * @param name The name of the profile to load
     * @throws UnsupportedProfileException No info known about the profile
     */
    private ProfileInfo loadProfile(String name)
        throws UnsupportedProfileException {

        if(invalidProfiles.contains(name))
            throw new UnsupportedProfileException(NO_PROF_FILE_MSG + name);

        ProfileInfo ret_val = null;

        String file_loc = "config/" + specVersion + "/profile/" + name + ".xml";
        InputStream is = locateConfigFile(file_loc);

        if(is == null) {
            invalidProfiles.add(name);
            throw new UnsupportedProfileException(NO_PROF_FILE_MSG + name);
        }

        Document doc_root = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setValidating(false);
            fac.setNamespaceAware(false);
            DocumentBuilder builder = fac.newDocumentBuilder();
            doc_root = builder.parse(is);
        } catch(FactoryConfigurationError fce) {
            errorReporter.errorReport("Can't create XML DOM factory: " +
                                      fce.getMessage(), null);
            return null;
        } catch(ParserConfigurationException pce) {
            errorReporter.errorReport("Can't create XML DOM parser", pce);
            return null;
        } catch(SAXException se) {
            Exception e = se.getException();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            if(e != null)
                e.printStackTrace(pw);
            else
                se.printStackTrace(pw);

            StringBuffer buf = new StringBuffer("SAX Error: ");
            buf.append(se.toString());
//                buf.append(sw.toString());
            throw new VRMLException(buf.toString());

        } catch(IOException ioe) {
            errorReporter.errorReport("Error reading external profile stream " +
                                      file_loc, ioe);
            return null;
        }

        Element cfg_elem = doc_root.getDocumentElement();
        NodeList profile_list = cfg_elem.getElementsByTagName("profile");
        if(profile_list.getLength() > 0) {
            Element profile_def = (Element)profile_list.item(0);
            ret_val = processProfileElement(profile_def);

            ProfileInfo[] profs = (ProfileInfo[])globalProfileList.get(specVersion);
            ProfileInfo[] tmp = new ProfileInfo[profs.length + 1];
            System.arraycopy(profs, 0, tmp, 0, profs.length);
            tmp[profs.length] = ret_val;

            globalProfileList.put(specVersion, tmp);
        }

        return ret_val;
    }

    /**
     * Internal convenience method to go locate a file within the classpath and
     * return a stream to it.
     *
     * @param filename The name of the file to look for
     * @return A stream pointing to it or null if not found
     */
    private InputStream locateConfigFile(final String filename) {

        InputStream is = (InputStream)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    return ClassLoader.getSystemResourceAsStream(filename);
                }
            }
        );

        // Fallback mechanism for WebStart
        if(is == null) {
            ClassLoader cl = DefaultNodeFactory.class.getClassLoader();
            is = (InputStream)cl.getResourceAsStream(filename);
        }

        return is;
    }

}
