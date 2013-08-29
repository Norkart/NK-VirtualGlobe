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

/**
 * Representation of a ROUTE structure in X3D.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface X3DRoute {

    /**
     * Get the reference to the source node of this route.
     *
     * @return The source node reference
     */
    public X3DNode getSourceNode();

    /**
     * Get the name of the source field of this route.
     *
     * @return The source node field's name
     */
    public String getSourceField();

    /**
     * Get the reference to the destination node of this route.
     *
     * @return The destination node reference
     */
    public X3DNode getDestinationNode();

    /**
     * Get the name of the destination field of this route.
     *
     * @return The destination node field's name
     */
    public String getDestinationField();
}
