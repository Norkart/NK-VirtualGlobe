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
import java.util.Map;
import org.ietf.uri.event.ProgressListener;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.xj3d.core.eventmodel.RouteManager;

import org.xj3d.core.loading.LoadDetails;
import org.xj3d.core.loading.WorldLoaderManager;

/**
 * Independent thread used to load a world from a list of URLs and then
 * place it in the given node.
 * <p>
 *
 * This implementation is designed to work as both a loadURL() and
 * createVrmlFromUrl() call handler. The difference is defined by what data
 * is supplied to the thread. If the target node is specified, then we assume
 * that the caller wants us to put the results there. If it is null, then
 * assume that we're doing a loadURL call and replace the entire world.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
class WorldLoadDetails extends LoadDetails {

    /** Parameters to accompany a loadURL call */
    Map params;

    /** The basic browser core functionality that this script hooks to */
    BrowserCore core;

    /** Destination node for the world contents */
    VRMLNodeType node;

    /**
     * The index of the field to load the URL for. If -1 then this is a
     * VRMLSingleExternalNodeType.
     */
    int fieldIndex;

    /** The world Url */
    String worldUrl;

    /** Flag to say if we're processing a loadURL call or not */
    boolean isLoadURL;

    /** The routeManager passed to the parser */
    RouteManager routeManager;

    /** The world loader to use to process the request */
    WorldLoaderManager worldLoader;

    /** Execution space of the calling context */
    VRMLExecutionSpace space;

    /** The spec major version to load. If needed. Ignored if loadURL call */
    int majorVersion;

    /** The spec major version to load. If needed. Ignored if loadURL call */
    int minorVersion;

    /** The progress listener */
    ProgressListener progressListener;

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
        if(!(o instanceof WorldLoadDetails))
            return false;

        if(!super.equals(o))
            return false;

        WorldLoadDetails ld = (WorldLoadDetails)o;

        return (ld.node == node) && (ld.fieldIndex == fieldIndex);
    }
}
