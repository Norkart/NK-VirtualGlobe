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
// none

// Local imports
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.VRMLExecutionSpace;
import org.web3d.vrml.nodes.VRMLNodeType;

/**
 * The manager of route information for both static and runtime systems.
 * <p>
 *
 * The implementation provides a one-shot route processing mechanism. It does
 * not continuously evaluate routes. That is left to the caller code. Once the
 * processRoutes() method is called, it will loop through all available routes
 * until none of them have any changed values to send. At this point it will
 * return and wait until the next time it is called.
 * <P>
 *
 * Routes are added to a particular execution space. Each space represents an
 * encapsulated world, such as a proto or inline. The route manager is
 * expected to be able to deal with these individual spaces, and keep them
 * separate. Thus, when the execution space is no longer referenced, the
 * associated routes and scripts are to be removed and their outputs no longer
 * processed.
 * <p>
 *
 * Implementation is not particularly efficient in this version...
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface RouteManager {

    /**
     * Register an error reporter with the manager so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter);

    /**
     * Set the factory needed to create new router instances for a new
     * execution space. If the reference is null, then this clears the current
     * factory so that we don't accept sub-space handling.
     *
     * @param fac The factory instance to use
     */
    public void setRouterFactory(RouterFactory fac);

    /**
     * Process all of the available routes until there are no more to process.
     * This method will not return until all routes have been processed.
     *
     * @param timestamp The timestamp for when these routes should be executed
     * @return false No event outs needed processing this call
     */
    public boolean processRoutes(double timestamp);

    /**
     * Add a route to the system. If the route exists in the system, this
     * silently ignores the request. If the space reference is null then
     *
     * @param space The execution space for the route
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    public void addRoute(VRMLExecutionSpace space,
                         VRMLNodeType srcNode,
                         int srcIndex,
                         VRMLNodeType destNode,
                         int destIndex);

    /**
     * Add a route object to the system. If the route exists in the system,
     * this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param route The object to add
     */
    public void addRoute(VRMLExecutionSpace space, ROUTE route);

    /**
     * Remove a route from the system. If the route does not exist in the
     * system, this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    public void removeRoute(VRMLExecutionSpace space,
                            VRMLNodeType srcNode,
                            int srcIndex,
                            VRMLNodeType destNode,
                            int destIndex);

    /**
     * Remove a route object from the system. If the route does not exist in
     * the system, this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param route The object to remove
     */
    public void removeRoute(VRMLExecutionSpace space, ROUTE route);

    /**
     * Add an execution space to the system.  This will add all its routes
     * and any contained spaces such as protos and inlines. If this space has
     * already been added, the request will be ignored.
     *
     * @param space The execution space to add
     */
    public void addSpace(VRMLExecutionSpace space);

    /**
     * Remove an execution space to the system.  This will add all its routes
     * and any contained spaces such as protos and inlines. If this request has
     * not been added it will be ignored.
     *
     * @param space The execution space to add
     */
    public void removeSpace(VRMLExecutionSpace space);

    /**
     * Notification that the route manager should now propogate all added and
     * removed spaces from this list into the core evaluatable system. It should
     * call the normal addRoute method of the space and not directly propogate
     * the route modifications immediately. They should wait for the separate
     * updateRoutes() call.
     */
    public void updateSpaces();

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
