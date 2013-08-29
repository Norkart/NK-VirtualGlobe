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

package org.xj3d.core.loading;

// External imports
// none

// Local imports
import org.web3d.vrml.nodes.VRMLExternalNodeType;


/**
 * A simple data holder class for information about a URL to load.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class LoadDetails {

    /** The type description. Should be one of the LoadConstants types */
    public String type;

    //----------------------------------------------------------
    // Methods defined by Object
    //----------------------------------------------------------

    /**
     * Check to see if this and the passed object are equal. They are equal if
     * the both point to the same node instance and field index.
     *
     * @param o The object to compare against
     * @return true if the node and field are identical, false otherwise
     */
    public boolean equals(Object o) {
        if(!(o instanceof LoadDetails))
            return false;

        LoadDetails ld = (LoadDetails)o;

        return (ld.type == type);
    }
}
