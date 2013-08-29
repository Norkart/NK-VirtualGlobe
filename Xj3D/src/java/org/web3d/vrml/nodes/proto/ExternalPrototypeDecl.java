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

package org.web3d.vrml.nodes.proto;

// External imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Local imports
import org.web3d.vrml.nodes.*;
import org.web3d.vrml.lang.*;

import org.web3d.vrml.util.URLChecker;


/**
 * ExternalPrototypeDecl is an implementation of the VRMLExternProtoDeclare
 * interface.
 * <p>
 *
 * The implementation does not provide any events for either type of listener.
 *
 * @author Alan Hudson
 * @version $Revision: 1.18 $
 * @see org.web3d.vrml.nodes.VRMLExternProtoDeclare
 * @see org.web3d.vrml.nodes.VRMLProtoInstance
 */
public class ExternalPrototypeDecl extends AbstractProto
    implements VRMLExternProtoDeclare {

    /**
     * Message when the root for createNewInstance is not one of
     * VRMLWorldRootNodeType or VRMLProtoInstance.
     */
    private static final String INVALID_ROOT_MSG =
        "The instance creation function has an invalid root node type ";

    /**
     * Message when the incoming proto declaration does not have a field
     * definition matching one in this interface. This is an error, and an
     * exception is generated, forcing the loader to move on to the next file.
     */
    private static final String MISMATCH_FIELD_MSG =
        "The proto declaration does not contain a field definition matching " +
        "the following field in this externproto interface: ";

    /** The state of the load */
    private int loadState;

    /** The world URL for correcting the URL names */
    private String worldURL;

    /** url field */
    private String[] vfUrl;

    /** The URI that was acutally loaded */
    protected String loadedURI;

    /** The real prototype definition, once it is loaded */
    private PrototypeDecl realProto;

    /**
     * Create a new instance of a proto that has the given name.
     *
     * @param name The name of the proto to use
     * @param majorVersion The major version number of this scene
     * @param minorVersion The minor version number of this scene
     * @param creator The node creator for generating instances of ourself
     */
    public ExternalPrototypeDecl(String name,
                         int majorVersion,
                         int minorVersion,
                         NodeTemplateToInstanceCreator creator) {
        super(name, majorVersion, minorVersion, creator);
    }

    //----------------------------------------------------------
    // Methods defined by VRMLNodeTemplate
    //----------------------------------------------------------

    /**
     * Create a new instance of a real node from this template. This will
     * ensure that all the internals are created as needed, based on the
     * current state of the node. Note that sometimes, creating an instance
     * of this template may result in an invalid node construction. Cases
     * where this could occur is when there's no node definition yet loaded
     * or that the loaded definition does not match this template.
     *
     * @param root The node that represents the root node of the
     *   VRMLExecutionSpace that we're in.
     * @param isStatic true if this is created within a StaticGroup
     * @return A new node instance from this template
     * @throws InvalidNodeTypeException The root node is not a node capable
     *    of representing a root of a scene graph
     * @see org.web3d.vrml.nodes.VRMLProtoInstance
     * @see org.web3d.vrml.nodes.VRMLWorldRootNodeType
     */
    public VRMLNode createNewInstance(VRMLNode root, boolean isStatic)
        throws InvalidNodeTypeException {

        if(realProto == null)
            return null;

        VRMLNode ret_val = null;

        if(root instanceof VRMLWorldRootNodeType) {
            ret_val = protoCreator.newInstance(realProto,
                                               (VRMLWorldRootNodeType)root,
                                               vrmlMajorVersion,
                                               vrmlMinorVersion,
                                               isStatic);
        } else if(root instanceof VRMLProtoInstance) {
            ret_val = protoCreator.newInstance(realProto,
                                               (VRMLProtoInstance)root,
                                               vrmlMajorVersion,
                                               vrmlMinorVersion,
                                               isStatic);
        } else
            throw new InvalidNodeTypeException(INVALID_ROOT_MSG + root);

        return ret_val;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExternProtoDeclare
    //----------------------------------------------------------

    /**
     * Get the real prototype information that this external reference
     * maps to. If the proto information has not been loaded yet, or it is
     * invalid this will return null.
     *
     * @return The underlying proto definition or null
     */
    public VRMLProtoDeclare getProtoDetails() {
        return realProto;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLSingleExternalNodeType
    //----------------------------------------------------------

    /**
     * Set a new value for the url list for this externproto.
     *
     * @param newUrl The new set of URLs to use
     * @param numValid number of valid items to fetch from the list
     */
    public void setUrl (String[] newUrl, int numValid) {
        vfUrl = newUrl;
    }

    /**
     * Accessor method to get current value of field <b>url</b>
     */
    public String[] getUrl() {
        return vfUrl;
    }

    //----------------------------------------------------------
    // Methods defined by VRMLExternalNodeType
    //----------------------------------------------------------

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
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype) {
        return false;
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
     * Set the content of this node to the given object. The object is then
     * cast by the internal representation to the form it needs. This assumes
     * at least some amount of intelligence on the part of the caller, but
     * we also know that we should not pass something dumb to it when we can
     * check what sort of content types it likes to handle. We assume the
     * loader thread is operating in the same context as the one that created
     * the node in the first place and thus knows the general types of items
     * to pass through.
     *
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     */
    public void setContent(String mimetype, Object content)
        throws IllegalArgumentException {
    }

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param URI The URI used to load this content
     */
    public void setLoadedURI(String URI) {
        loadedURI = URI;
    }

    /**
     * Get the primary type of this node.  Replaces the instanceof mechanism
     * for use in switch statements and when trying to determine if the node
     * has been used in the right place. If it is unknown (eg not yet loaded
     * extern proto) then return -1.
     *
     * @return The primary type
     */
    public int getPrimaryType() {
        return -1;
    }

    /**
     * Add a listener to this node instance. If the listener is already added
     * or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addUrlListener(VRMLUrlListener l) {
        // ignore
    }

    /**
     * Remove a listener from this node instance. If the listener is null or
     * not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeUrlListener(VRMLUrlListener l) {
        // ignore
    }

    /**
     * Add a listener to this node instance for the content state changes. If
     * the listener is already added or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addContentStateListener(VRMLContentStateListener l) {
    }

    /**
     * Remove a listener from this node instance for the content state changes.
     * If the listener is null or not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeContentStateListener(VRMLContentStateListener l) {
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set the real prototype information that this external reference
     * maps to. Checks that the proto interfaces match between the two
     * The externproto is allowed to declare a subset of the fields from
     * the actual proto, but no details are allowed to change. If anything
     * mismatches such as name, type or access type, then the exception is
     * thrown.
     *
     * @param proto The underlying proto definition
     * @throws IllegalArgumentException One of the field definitions does
     *    not match
     */
    protected void setProtoDetails(PrototypeDecl proto)
        throws IllegalArgumentException {

        List proto_fields = proto.getAllFields();

        Iterator itr = fieldDeclList.iterator();
        while(itr.hasNext()) {
            VRMLFieldDeclaration decl = (VRMLFieldDeclaration)itr.next();

            // TODO:
            // If they don't match, dig through the two comparing item
            // by item and generate an appropriate error message for debugging
            // purposes.
            if(!proto_fields.contains(decl)) {
                throw new IllegalArgumentException(MISMATCH_FIELD_MSG +
                                                   decl.toString(isVrml97));
            }
        }

        realProto = proto;
    }
}
