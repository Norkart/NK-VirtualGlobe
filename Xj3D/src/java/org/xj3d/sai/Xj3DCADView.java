/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.X3DNode;

/**
 * Wrapper interface for the internal viewpoint representation, to allow
 * abstraction of the user interface description of viewpoints from the
 * underlying node representation.
 * <p>
 *
 * This class deliberately does not give access to the SAI X3DNode that
 * represents the viewpoint.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface Xj3DCADView {

    /**
     * Get the list of layers declared in the current scene. If the scene does
     * not contain any layers, it will return null.
     */
    public X3DNode[] getCADLayers();

    /**
     * Get the list of layers declared in the current scene. If the scene does
     * not contain any layers, it will return null.
     */
    public X3DNode[] getCADAssemblies();

    /**
     * Highlight the given part or assembly using the given alternate
     * appearance attributes. This will temporarily override the appearance
     * on all sub-children until one of the following conditions are met:
     * <ul>
     * <li>The world is replaced</li>
     * <li>The parent scene graph containing this structure node is removed</li>
     * <li>Another part or structure is requested to be highlighted</li>
     * <li>This same node is requested, but with a null material parameter</li>
     * </ul>
     *
     * Once highlighting has stopped, the ordinary material values are
     * returned.
     *
     * @param structureNode A X3DProductStructureNode to be highlighted
     *    (or PROTO wrapper of one)
     * @param material A X3DmaterialNode to use as the highlight colour, or
     *    null to clear the current highlighted object
     */
    public void highlightPart(X3DNode structureNode, X3DNode material);

    /**
     * Add a listener for CAD-style changes. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addCADViewListener(Xj3DCADViewListener l);

    /**
     * Remove a listener for CAD-style changes. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeCADViewListener(Xj3DCADViewListener l);
}
