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

package org.web3d.vrml.nodes;

// Standard imports
// none

// Application specific imports
// none

/**
 * Representation of a node that defines it's content at a URL separate to
 * this file that will also need loading.
 * <p>
 * The idea behind this node interface is that we are going to allow a
 * separate thread from the one that created the initial node to simultaneously
 * load the contents of this node. As we really don't know all the possible
 * types to load, particularly with profiles, a common base class will make
 * life much easier for us to deal with in a generic fashion.
 * <p>
 * The operation of this interface is to get the loader thread to set the
 * load state as it is going. Once it has loaded the content it then tells the
 * implementation about what it has found.
 * <p>
 * When fetching the URL list, it is expected that all URLs will be fully
 * qualified. It will be up to the implementor to process the set URLs to
 * contain only fully qualified URLs. For this, we will ensure that the world
 * URL is set on every instance through the setWorldUrl() method.
 *
 * @author Justin Couch
 * @version $Revision: 1.11 $
 */
public interface VRMLExternalNodeType {

    /** The node has not started loading yet. */
    public int NOT_LOADED = 1;

    /** The node is in the middle of the loading process. */
    public int LOADING = 2;

    /** The node loaded sucessfully and has it's content available */
    public int LOAD_COMPLETE = 3;

    /**
     * The node completed loading but none of the available content was
     * successfully loaded.
     */
    public int LOAD_FAILED = 4;

    /**
     * Set the world URL so that any relative URLs may be corrected to the
     * fully qualified version. Guaranteed to be non-null.
     *
     * @param url The world URL.
     */
    public void setWorldUrl(String url);

    /**
     * Get the world URL so set for this node.
     *
     * @return url The world URL.
     */
    public String getWorldUrl();

    /**
     * Add a listener to this node instance for URL changes. If the
     * listener is already added or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addUrlListener(VRMLUrlListener l);

    /**
     * Remove a listener from this node instance for URL changes. If the
     * listener is null or not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeUrlListener(VRMLUrlListener l);

    /**
     * Add a listener to this node instance for the content state changes. If
     * the listener is already added or null the request is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void addContentStateListener(VRMLContentStateListener l);

    /**
     * Remove a listener from this node instance for the content state changes.
     * If the listener is null or not registered, the request is silently ignored.
     *
     * @param l The listener to be removed
     */
    public void removeContentStateListener(VRMLContentStateListener l);
}
