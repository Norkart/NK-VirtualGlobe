/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
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
import java.io.Reader;
import java.io.StringReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ietf.uri.URIUtils;

// Local imports
import org.web3d.vrml.lang.*;
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.nodes.proto.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.util.URLChecker;

/**
 * A CR External Prototype Declaration.
 * <p>
 *
 * This node will be placed on the content loader.  Instances of this EP will
 * go into the scenegraph as stubs.  When this EP is loaded it will finish
 * creating those instances.
 *
 * @author Alan Hudson
 * @version $Revision: 1.21 $
 */
public abstract class CRExternPrototypeDecl extends ExternalPrototypeDecl {

    /** Error message when someone attempts to change the URL at runtime */
    private static final String CHANGE_URL_MSG =
        "ExternProto's cannot change URL at runtime";

    /** Valid set of mime types for VRML97 support */
    private static HashSet validVrmlTypes;

    /** Valid set of mime types for X3D support */
    private static HashSet validX3DTypes;

    /** The prototype declaration that we always use */
    protected VRMLNodeTemplate proto;

    /** Flag indicating we are in setup mode currently */
    protected boolean inSetup;

    /** url field */
    protected String[] vfUrl;

    /** The world URL for correcting relative URL values */
    protected String worldURL;

    /** The state of the load */
    protected int loadState;

    /** The node factory used to create real node instances */
    protected VRMLNodeFactory nodeFactory;

    /** List of EP instances to fill in. */
    protected ArrayList instanceList;

    /** List of EP instances.  Stored as a list of WeakReferences */
    protected ArrayList wrInstances;

    /** The reference part of the URI loaded */
    protected String uriRef;

    /** Frame state manager for this event model instance */
    protected FrameStateManager stateManager;

    /** Reporter instance for handing out errors */
    protected ErrorReporter errorReporter;

    static {
        validVrmlTypes = new HashSet();
        validX3DTypes = new HashSet();

        validVrmlTypes.add("model/vrml");
        validVrmlTypes.add("x-world/x-vrml");

        validX3DTypes.add("model/x3d+vrml");
        validX3DTypes.add("model/x3d+xml");
        validX3DTypes.add("model/x3d+binary");
    }

    /**
     * Create a place holder that represents the given extern proto
     * declaration.
     *
     * @param name The name of the declaration to base this placeholder on
     * @param fac The factory to use for creating the instances
     * @param majorVersion The major version number of this scene
     * @param minorVersion The minor version number of this scene
     * @param creator The node creator for generating instances of ourself
     */
    public CRExternPrototypeDecl(String name,
                                 VRMLNodeFactory fac,
                                 int majorVersion,
                                 int minorVersion,
                                 NodeTemplateToInstanceCreator creator) {

        super(name, majorVersion, minorVersion, creator);

        inSetup = true;
        uriRef = null;
        nodeFactory = fac;

        instanceList = new ArrayList();
        wrInstances = new ArrayList();
        errorReporter = DefaultErrorReporter.getDefaultReporter();
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
    }

    /**
     * Add an instance of a EXTERNPROTO to be filled out when loaded
     *
     * @param parent The parent node for this EP
     * @param idx The field index
     * @param node The instance
     */
    public void addInstance(VRMLNodeType parent, int idx, VRMLNodeType node) {
        instanceList.add(new EPDInstanceEntry(parent, idx, node));
    }

    /**
     * Set the frame state manager to be used by this proto instance.
     * used to pass in information about the contained scene for extern
     * proto handler.
     *
     * @param mgr The manager instance to use
     */
    public void setFrameStateManager(FrameStateManager mgr) {
        stateManager = mgr;
    }

    /**
     * Get the PROTO/EXTERNPROTO definition used by this place holder.
     *
     * @return The proto definition used by the node
     */
    public VRMLNodeTemplate getProtoDefinition() {
        return proto;
    }

    //--------------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType .
    //--------------------------------------------------------------

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    public int getLoadState() {
        return loadState;
    }

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    public void setLoadState(int state) {
        loadState = state;
    }

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    public void setWorldUrl(String url) {
        if(url == null)
            return;

        // check for a trailing slash. If it doesn't have one, append it.
        if(url.charAt(url.length() - 1) != '/') {
            worldURL = url + '/';
        } else {
            worldURL = url;
        }

        if(vfUrl != null) {
            URLChecker.checkURLsInPlace(worldURL, vfUrl, false);
        }
    }

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    public String getWorldUrl() {
        return worldURL;
    }

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype) {
        if(isVrml97)
            return validVrmlTypes.contains(mimetype);
        else
            return validX3DTypes.contains(mimetype);
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param URI The URI used to load this content
     */
    public void setLoadedURI(String URI) {
        String parts[] = URIUtils.stripFile(URI);
        uriRef = parts[2];

        // An extern proto defines its own relative URL anchor
        int pos = parts[0].lastIndexOf("/");
        worldURL = parts[0].substring(0,pos);
    }

    //----------------------------------------------------------------
    // Methods defined by VRMLExternProtoDeclare
    //----------------------------------------------------------------

    /**
     * Set the URL list that describes the sources for this externproto.
     *
     * @param newUrl The list of URLs to use
     * @param numValid The number of valid items to use from the array
     * @throws RuntimeException Attempting to change the URL at runtime
     */
    public void setUrl (String[] newUrl, int numValid) {
        // TODO: What todo with numValid

        if(!inSetup)
            throw new RuntimeException(CHANGE_URL_MSG);

        vfUrl = newUrl;
    }

    /**
     * Fetch the set values for the URL(s) of the externproto definition.
     *
     * @return A list of URL strings, guaranteed to be at least 1 in length
     */
    public String[] getUrl() {
        return vfUrl;
    }

    /**
     * Finish off the creation process of the generic externproto handling.
     *
     * @param protoDef The declaration to build stuff from
     * @param protoCreator The creator used to copy the node
     * @param rootSpace Parent execution space that this instance belongs to
     */
    protected void finishSetContent(PrototypeDecl protoDef,
                                    CRProtoCreator protoCreator,
                                    VRMLExecutionSpace rootSpace) {

        proto = protoDef;

        EPDInstanceEntry[] instances =
            new EPDInstanceEntry[instanceList.size()];
        instanceList.toArray(instances);
        CRProtoInstance instance;
        VRMLNodeType parent;

        for(int i=0; i < instances.length; i++) {
            instance = (CRProtoInstance)instances[i].instance;
            parent = instances[i].parent;
            protoCreator.fillinInstance(protoDef, instance, rootSpace);
            instance.setupFinished();

            if(parent != null)
                parent.notifyExternProtoLoaded(instances[i].idx, instance);

            wrInstances.add(new WeakReference(instance));
        }

        instanceList.clear();
    }
}
