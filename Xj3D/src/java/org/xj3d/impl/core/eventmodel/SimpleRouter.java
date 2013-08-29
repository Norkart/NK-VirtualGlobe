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

package org.xj3d.impl.core.eventmodel;

// External imports
import java.util.ArrayList;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLScriptNodeType;

import org.xj3d.core.eventmodel.Router;

/**
 * The manager of route information for both static and runtime systems.
 * <p>
 *
 * The implementation provides a one-shot route processing mechanism. It does
 * not continuously evaluate routeSet. That is left to the caller code. Once the
 * processRoutes() method is called, it will loop through all available routeSet
 * until none of them have any changed values to send. At this point it will
 * return and wait until the next time it is called.
 * <P>
 *
 * This implementation does not deal correctly with fan-out of events. This is
 * a simple manager for this reason. The reason for this is that we've decided
 * to optimise for speed for scenes that you know only have a single ROUTE from
 * any given eventOut to another eventIn, which is the majority of scenes.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class SimpleRouter implements Router {

    /** The default initial size of the array of routes */
    private static final int DEFAULT_SIZE = 32;

    /** Increment of the array size when we need to increase */
    private static final int ARRAY_INC = 8;

    /** The set of all routes held by this manager */
    private HashSet<RouteHolder> routeSet;

    /** Set of all routes waiting to be added */
    private HashSet<RouteHolder> routesToAdd;

    /** Set of all routes waiting to be removed */
    private HashSet<RouteHolder> routesToRemove;

    /** An internal counter for the number of routes registered */
    private int routeCount;

    /** An array of all eventOuts for processing */
    private RouteHolder[] routeList;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Create and initialise a route manager instance
     */
    public SimpleRouter() {
        routeSet = new HashSet<RouteHolder>();
        routesToAdd = new HashSet<RouteHolder>();
        routesToRemove = new HashSet<RouteHolder>();

        routeCount = 0;
        routeList = new RouteHolder[DEFAULT_SIZE];

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //-------------------------------------------------------------
    // Methods defined by Router
    //-------------------------------------------------------------

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Process all of the available routeSet until there are no more to process.
     * This method will not return until all routeSet have been processed.
     *
     * @param timestamp The timestamp for when these routeSet should be executed
     */
    public boolean processRoutes(double timestamp) {

        if(routeCount <= 0)
            return false;

        boolean routeSetToProcess = false;

        // run through the routes
        for(int i = 0; i < routeCount; i++) {
            if(routeList[i].needsProcessing()) {
                routeList[i].sendRoute(timestamp);
                routeSetToProcess = true;
            }
        }

        return routeSetToProcess;
    }

    /**
     * Add a route to the system. If the route exists in the system, this
     * silently ignores the request.
     *
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    public void addRoute(VRMLNodeType srcNode,
                         int srcIndex,
                         VRMLNodeType destNode,
                         int destIndex) {

        RouteHolder holder = new RouteHolder();
        holder.srcNode = srcNode;
        holder.srcIndex = srcIndex;
        holder.destNode = destNode;
        holder.destIndex = destIndex;

        if(routeSet.contains(holder) || routesToAdd.contains(holder))
            return;

        if(routesToRemove.contains(holder)) {
            routesToRemove.remove(holder);
            return;
        }

        routesToAdd.add(holder);
    }

    /**
     * A request to bulk add routes to this router. Typically used when we're
     * bringing back online a router having previously cleared. This means
     *
     * @param routes List of all the ROUTE objects to add
     */
    public void addRoutes(ArrayList<ROUTE> routes) {
        int size = routes.size();

        for(int i = 0; i < size; i++) {
            ROUTE rt = routes.get(i);

            addRoute((VRMLNodeType)rt.getSourceNode(),
                     rt.getSourceIndex(),
                     (VRMLNodeType)rt.getDestinationNode(),
                     rt.getDestinationIndex());
        }
    }

    /**
     * Remove a route from the system. If the route does not exist in the
     * system, this silently ignores the request.
     *
     * @param srcNode The source node of the route
     * @param srcIndex The index of the source field
     * @param destNode The destination node of the route
     * @param destIndex The index of the destination field
     */
    public void removeRoute(VRMLNodeType srcNode,
                            int srcIndex,
                            VRMLNodeType destNode,
                            int destIndex) {

        // Yuck, but simple to do for the first round impl...
        RouteHolder holder = new RouteHolder();
        holder.srcNode = srcNode;
        holder.srcIndex = srcIndex;
        holder.destNode = destNode;
        holder.destIndex = destIndex;

        if(!routeSet.contains(holder) || routesToRemove.contains(holder))
            return;

        if(routesToAdd.contains(holder)) {
            routesToAdd.remove(holder);
            return;
        }

        routesToRemove.add(holder);
    }

    /**
     * Notification that the route manager should now propogate all added and
     * removed routes from this list into the core evaluatable space.
     */
    public void updateRoutes() {
        // No point generating extra garbage if not needed
        if(routesToRemove.size() == 0 && routesToAdd.size() == 0)
            return;

        routeSet.removeAll(routesToRemove);
        routeSet.addAll(routesToAdd);

        routeCount = routeSet.size();

        if(routeCount > routeList.length) {
            int old_size = routeList.length;
            int reqd = (routeCount >= routeList.length + ARRAY_INC) ?
                       routeCount:
                       routeList.length + ARRAY_INC;

            routeList = new RouteHolder[reqd];
        }

        routeSet.toArray(routeList);

        routesToAdd.clear();
        routesToRemove.clear();
    }

    /**
     * Clear all the routes currently being managed here. The space this router
     * represents is being deleted.
     */
    public void clear() {
        routeSet.clear();
        routesToAdd.clear();
        routesToRemove.clear();

        for(int i = 0; i < routeCount; i++)
            routeList[i] = null;

        routeCount = 0;
    }
}
