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

// External imports
// none

// Local imports
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
 * @version $Revision: 1.6 $
 */
public interface VRMLSingleExternalNodeType extends VRMLExternalNodeType {

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @return The current load state of the node
     */
    public int getLoadState();

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param state The new state of the node
     */
    public void setLoadState(int state);

    /**
     * Replace the existing set of URLs with this new set. If the array is null
     * or zero length, it will clear the existing values.
     *
     * @param newUrl The list of new instances to use
     * @param numValid The number of valid values to copy from the array
     */
    public void setUrl(String[] newUrl, int numValid);

    /**
     * Get the list of URLs requested by this node. If there are no URLs
     * supplied in the text file then this will return a zero length array.
     *
     * @return The list of URLs to attempt to load
     */
    public String[] getUrl();

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node.
     *
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     */
    public boolean checkValidContentType(String mimetype);

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
        throws IllegalArgumentException;

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param URI The URI used to load this content
     */
    public void setLoadedURI(String URI);
}
