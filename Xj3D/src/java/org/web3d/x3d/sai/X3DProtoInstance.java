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
 * The base representation of any node that is constructed from a PROTO
 * declaration.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public interface X3DProtoInstance extends X3DNode {

    /**
     * Get the listing of implementatoin types for proto nodes. This performs
     * the same functionality as the getType() method in the base class. For
     * ProtoInstances, that method is required to return a single value that
     * says it is a proto instance and this method is used to determine the
     * final type.
     *
     * @return The list of types of the implemented node
     */
    public int[] getImplementationTypes();
}
