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
// None

// Local imports
import org.web3d.util.IntHashMap;

import org.web3d.vrml.nodes.VRMLScriptNodeType;
import org.xj3d.core.loading.LoadDetails;
import org.xj3d.core.loading.ScriptLoadStatusListener;

/**
 * A simple data holder class for information about a Script URL to load.
 * <p>
 *
 * The data holder contains a reference to the cache to use. This is because
 * we might have different caching regimes set up by using different load
 * managers in the same VM instance. This allows each user to specify
 * themselves what sort of caching they want done without restricting them
 * to a first come, first set cache mechanism.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class ScriptLoadDetails extends LoadDetails {

    /**
     * The index of the field to load the URL for. If -1 then this is a
     * VRMLSingleExternalNodeType.
     */
    int fieldIndex;

    /** The node that is being loaded. Cast up to the correct type. */
    VRMLScriptNodeType node;

    /**
     * The map of spec version to an internal HashMap, which then maps mime
     * types to supporting scripting engines
     */
    IntHashMap engineMap;

    /** Status listener for when the loading fails or succeeds */
    ScriptLoadStatusListener statusListener;

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
        if(!(o instanceof ScriptLoadDetails))
            return false;

        if(!super.equals(o))
            return false;

        ScriptLoadDetails ld = (ScriptLoadDetails)o;

        return (ld.node == node) && (ld.fieldIndex == fieldIndex);
    }
}
