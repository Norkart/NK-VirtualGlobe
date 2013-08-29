/*****************************************************************************
 *                        Web3d.org Copyright (c) 2004 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
// None

// Local imports
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLWorldRootNodeType;
import org.web3d.vrml.scripting.external.buffer.ExternalEvent;

import org.xj3d.core.eventmodel.RouteManager;

/**
 * Simple queue element for posting addRoute calls
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
class AddRouteEvent implements ExternalEvent {

    /** The routeManager to use */
    private RouteManager routeManager;

    /** The space to add to */
    private VRMLExecutionSpace space;

    /** The source node for the route */
    private VRMLNodeType srcNode;

    /** The readable field source of the route */
    private int srcIdx;

    /** The destination node of the route */
    private VRMLNodeType destNode;

    /** The writable field destination of the route */
    private int destIdx;

    /**
     * Create a new route.
     *
     * @param fromX3DNode The source node for the route
     * @param readableField The readable field source of the route
     * @param toX3DNode The destination node of the route
     * @param writableField The writable field destination of the route
     */
    AddRouteEvent(RouteManager rm,
                  VRMLExecutionSpace space,
                  VRMLNodeType srcNode,
                  int srcIdx,
                  VRMLNodeType destNode,
                  int destIdx) {

        routeManager = rm;
        this.space = space;
        this.srcNode = srcNode;
        this.srcIdx = srcIdx;
        this.destNode = destNode;
        this.destIdx = destIdx;
    }

    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#doEvent()
     */
    public void doEvent() {
        routeManager.addRoute(space,
                              srcNode,
                              srcIdx,
                              destNode,
                              destIdx);
    }
    /**
     * @see org.web3d.vrml.scripting.external.buffer.ExternalEvent#isConglomerating()
     */
    public boolean isConglomerating() {
        return false;
    }

}
