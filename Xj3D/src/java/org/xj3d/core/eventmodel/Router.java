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

package org.xj3d.core.eventmodel;

// External imports
import java.util.ArrayList;

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;

/**
 * A runtime evaluator of routes for a single execution space.
 * <p>
 *
 * The implementation provides a one-shot route processing mechanism. It does
 * not continuously evaluate routes. That is left to the caller code. Once the
 * processRoutes() method is called, it will loop through all available routes
 * exactly once and return to the caller. This is to conform with the
 * requirements of the event cascade processing defined in the VRML spec.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface Router {

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Process all of the available routes once, now. This method will not
     * return until all routes have been processed. Implied in this behaviour
     * is the loop-breaking rule handling. There is no explicit direction that
     * the timestamp has changed, but the implementation should also keep track
     * of which eventOuts have already sent an event for this frame and make
     * sure they do not send any further values/routes. It should only
     * perform the routing processing at this point. Calling eventsProcessed()
     * on scripts is left to a later call.
     *
     * @param timestamp The timestamp for when these routes should be executed
     * @return false No event outs needed processing this call
     */
    public boolean processRoutes(double timestamp);

    /**
     * Add a route to the system. If the route exists in the system, this
     * silently ignores the request. IAW the spec, this should buffer the
     * actual add operation until the updateRoutes() method is called. If the
     * destination of the route is a script, it should automatically add it to
     * the internal queue for dealing with eventsProcessed methods.
     *
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    public void addRoute(VRMLNodeType srcNode,
                         int srcIndex,
                         VRMLNodeType destNode,
                         int destIndex);

    /**
     * A request to bulk add routes to this router. Typically used when we're
     * bringing back online a router having previously cleared.
     *
     * @param routes List of all the ROUTE objects to add
     */
    public void addRoutes(ArrayList<ROUTE> routes);

    /**
     * Remove a route from the system. If the route does not exist in the
     * system, this silently ignores the request. IAW the spec, this should
     * buffer the actual add operation until the updateRoutes() method is
     * called.
     *
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    public void removeRoute(VRMLNodeType srcNode,
                            int srcIndex,
                            VRMLNodeType destNode,
                            int destIndex);

    /**
     * Notification that the route manager should now propogate all added and
     * removed routes from this list into the core evaluatable space.
     */
    public void updateRoutes();

    /**
     * Clear all the routes currently being managed here. The space this router
     * represents is being deleted.
     */
    public void clear();
}
