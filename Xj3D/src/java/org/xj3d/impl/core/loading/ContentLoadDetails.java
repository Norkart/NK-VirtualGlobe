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

package org.xj3d.impl.core.loading;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLExternalNodeType;

import org.xj3d.core.loading.LoadDetails;

/**
 * A simple data holder class for information about a URL to load.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ContentLoadDetails extends LoadDetails {

    /**
     * The index of the field to load the URL for. If -1 then this is a
     * VRMLSingleExternalNodeType.
     */
    int fieldIndex;

    /** The node that is being loaded. Cast up to the correct type. */
    VRMLExternalNodeType node;

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Check to see if this and the passed object are equal. They are equal if
     * the both point to the same node instance and field index.
     *
     * @param obj The object to compare against
     * @return true if the node and field are identical, false otherwise
     */
    public boolean equals(Object o) {
        if(!(o instanceof ContentLoadDetails))
            return false;

        if(!super.equals(o))
            return false;

        ContentLoadDetails ld = (ContentLoadDetails)o;

        return (ld.node == node) && (ld.fieldIndex == fieldIndex);
    }
}
