/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
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
import org.web3d.vrml.lang.InvalidFieldException;

/**
 * Representation of a node that defines more than one piece of its content at
 * a list of URLs separate to this file that will also need loading.
 * <p>
 * This is the same system as used by the
 * {@link org.web3d.vrml.nodes.VRMLExternalNodeType} except expanded to provide
 * more than one field with external content.
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
 * @version $Revision: 1.7 $
 */
public interface VRMLMultiExternalNodeType extends VRMLExternalNodeType {

    /**
     * Get the list of field index values that require external content.
     * These will be used to query for the URL values later. The field must
     * be non-null and non-zero in length.
     *
     * @return A list of field indexes requiring textures
     */
    public int[] getUrlFieldIndexes();

    /**
     * Ask the state of the load of this node. The value will be one of the
     * constants defined above.
     *
     * @param index The field index we want to set the state for
     * @return The current load state of the node
     */
    public int getLoadState(int index);

    /**
     * Set the load state of the node. The value must be one of the constants
     * defined above.
     *
     * @param index The field index we want to set the state for.
     * @param state The new state of the node
     */
    public void setLoadState(int index, int state);

    /**
     * Get the list of URLs for the given field index requested by this node.
     * If there are no URLs supplied in the text file then this will return a
     * zero length array.
     *
     * @param index The field index we want URL values for
     * @return The list of URLs to attempt to load
     * @throws InvalidFieldException The field index is not valid for the query
     */
    public String[] getUrl(int index)
        throws InvalidFieldException;

    /**
     * Check to see if the given MIME type is one that would be supported as
     * content coming into this node at the given field index.
     *
     * @param index The field index we want to check content type for.
     * @param mimetype The type to check for
     * @return true if this is OK, false if not
     * @throws InvalidFieldException The field index is not valid for the query
     */
    public boolean checkValidContentType(int index, String mimetype)
        throws InvalidFieldException;

    /**
     * Get the list of preferred content class types in order of preference.
     * This allows the code to ask for data in an order of preference. For
     * example, for images it might want a BufferedImage preferance to an
     * Image. This is passed to the URI loading code. If there is no preference
     * this may return a null list.
     *
     * @param index THe field index for the prefered types.
     * @return A list of prefered class types
     * @throws InvalidFieldException The field index is not valid for the query
     */
    public Class[] getPreferredClassTypes(int index)
        throws InvalidFieldException;

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
     * @param index The field index to set the content for
     * @param mimetype The mime type of this object if known
     * @param content The content of the object
     * @throws IllegalArguementException The content object is not supported
     * @throws InvalidFieldException The field index is not valid for the query
     */
    public void setContent(int index, String mimetype, Object content)
        throws IllegalArgumentException, InvalidFieldException;

    /**
     * Notify the node which URL was used to load the content.  It will be the
     * complete URI with path, query and references parts.  This method will
     * be called before setContent.
     *
     * @param fieldIdx The field index that was loaded
     * @param URI The URI used to load this content
     */
    public void setLoadedURI(int fieldIdx, String URI);
}
