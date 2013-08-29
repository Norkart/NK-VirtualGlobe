/*****************************************************************************
 *                     Web3d.org Copyright (c) 2001 - 2006
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
// None

// Local imports
import org.web3d.vrml.lang.InvalidFieldAccessException;
import org.web3d.vrml.lang.InvalidFieldValueException;

/**
 * An abstract representation of a rendering layer concept.
 *
 * @author Justin Couch
 * @version $Revision: 1.6 $
 */
public interface VRMLLayerNodeType extends VRMLNodeType {
    /**
     * Add a listener for layer state changes.  Duplicates will be ignored.
     *
     * @param listener The listener
     */
    public void addLayerListener(LayerListener listener);

    /**
     * Removed a listener for layer state changes.
     *
     * @param listener The listener
     */
    public void removeLayerListener(LayerListener listener);

    /**
     * See if this layer is currently pickable.
     *
     * @return true if the contents of this layer can be picked
     */
    public boolean isPickable();

    /**
     * Set the value of the isPickable field.
     *
     * @param enable true if the contents of this layer can be picked
     */
    public void setPickable(boolean enable);

    /**
     * Set the viewport node instance used to control the size of screen
     * real estate to use for this layer. The node type passed in should be an
     * instance of VRMLViewportNodeType or a proto wrapper thereof.
     *
     * @param node The node instance to use or null to clear
     * @throws InvalidFieldValueException The node is not a viewport node type
     * @throws InvalidFieldAccessException Attempting to write to the field
     *    after the setup is complete
     */
    public void setViewport(VRMLNodeType node)
        throws InvalidFieldValueException, InvalidFieldAccessException;

    /**
     * Fetch the viewport node instance that this layer currently has. If no
     * instance is used, returns null.
     *
     * @return The current node instance
     */
    public VRMLNodeType getViewport();

    /**
     * Get the type of viewport layout policy that the contained viewport node
     * represents. This is a shortcut to fetching the viewport instance
     * directly, walking the proto heirarchy and so forth.
     * <p>
     * This determines how the viewport is managed by the system during window
     * resizes etc. It is a fixed value that never changes for the node
     * implementation.
     * <p>
     * If no viewport node is defined, return VIEWPORT_FULLWINDOW.
     * <p>
     * If no viewport is yet referenced courtesy of an
     * externproto, this returns VIEWPORT_UNDEFINED until it is updated.
     *
     * @return One of the VIEWPORT_* constant values
     */
    public int getViewportType();

    /**
     * Set the ID of this layer. The layer should pass this down to all
     * contained geometry through the updateRefCount() method. This method
     * should only ever be called once, when just after construction.
     *
     * @param id The id of this layer
     */
    public void setLayerId(int id);

    /**
     * Get the ID of this layer. If none has been set, it should return a value
     * of -1.
     *
     * @return The ID of this layer or -1 if not yet set
     */
    public int getLayerId();
}
