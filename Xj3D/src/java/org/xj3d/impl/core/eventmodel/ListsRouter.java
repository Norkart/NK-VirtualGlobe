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
import java.util.HashMap;
import java.util.Set;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.nodes.VRMLNodeType;

import org.xj3d.core.eventmodel.Router;

/**
 * The manager of route information that handles routeSet by collecting together
 * all routeSet for a particular eventOut and processing it as a single handle.
 * <p>
 *
 * The implementation provides a one-shot route processing mechanism. It does
 * not continuously evaluate routeSet. That is left to the caller code. Once the
 * processRoutes() method is called, it will loop through all available routeSet
 * until none of them have any changed values to send. At this point it will
 * return and wait until the next time it is called.
 * <P>
 *
 * Implementation is not particularly efficient in this version...
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class ListsRouter implements Router {

    /** The default initial size of the array of routes */
    private static final int DEFAULT_SIZE = 32;

    /** Increment of the array size when we need to increase */
    private static final int ARRAY_INC = 8;

    /** The set of all routeSet held by this manager */
    private HashSet<RouteHolder> routeSet;

    /** Set of all routeSet waiting to be added */
    private HashSet<RouteHolder> routesToAdd;

    /** Set of all routeSet waiting to be removed */
    private HashSet<RouteHolder> routesToRemove;

    /** An internal counter for the number of routes registered */
    private int sourceCount;

    /** A map of all node + eventOut (key) and the routeSet (value) */
    private HashMap<EventOutHolder, ArrayList<RouteHolder>> sourceMap;

    /** An array of all eventOuts for processing */
    private EventOutHolder[] routeList;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Temporary array for processing routes added & removed */
    private RouteHolder[] routeTmp;

    /**
     * Create and initialise a route manager instance
     */
    public ListsRouter() {
        routeSet = new HashSet<RouteHolder>();
        routesToAdd = new HashSet<RouteHolder>();
        routesToRemove = new HashSet<RouteHolder>();

        sourceCount = 0;

        sourceMap = new HashMap<EventOutHolder, ArrayList<RouteHolder>>();

        routeList = new EventOutHolder[DEFAULT_SIZE];
        routeTmp = new RouteHolder[DEFAULT_SIZE];

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

        if(sourceCount <= 0)
            return false;

        boolean routeSetToProcess = false;

        // run through the routes
        for(int i = 0; i < sourceCount; i++) {
            if(routeList[i].needsProcessing()) {
                ArrayList<RouteHolder> l = sourceMap.get(routeList[i]);

                int size = l.size();
                for(int j = 0; j < size; j++) {
                    RouteHolder rh = l.get(j);
                    rh.sendRoute(timestamp);
                }

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
     * A request to bulk add routeSet to this router. Typically used when we're
     * bringing back online a router having previously cleared. This means
     *
     * @param routeSet List of all the ROUTE objects to add
     */
    public void addRoutes(ArrayList<ROUTE> routeSet) {
        int size = routeSet.size();

        for(int i = 0; i < size; i++) {
            ROUTE rt = routeSet.get(i);

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
     * removed routeSet from this list into the core evaluatable space.
     */
    public void updateRoutes() {

        // No point generating extra garbage if not needed
        if(routesToRemove.size() == 0 && routesToAdd.size() == 0)
            return;

        routeSet.removeAll(routesToRemove);
        routeSet.addAll(routesToAdd);

        RouteHolder rh;
        int size = routesToRemove.size();

        if(routeTmp.length < size) {
            int reqd = (size >= routeTmp.length + ARRAY_INC) ?
                       size :
                       routeTmp.length + ARRAY_INC;

            routeTmp = new RouteHolder[reqd];
        }

        routesToRemove.toArray(routeTmp);

        EventOutHolder tester = new EventOutHolder();

        for(int i = 0; i < size; i++) {
            // First need to locate the list dealing with this eventOut
            rh = (RouteHolder)routeTmp[i];
            routeTmp[i] = null;

            tester.srcNode = rh.srcNode;
            tester.srcIndex = rh.srcIndex;

            ArrayList<RouteHolder> l = sourceMap.get(tester);

            if(l == null)
                continue;

            l.remove(rh);
        }

        size = routesToAdd.size();

        if(routeTmp.length < size) {
            int reqd = (size >= routeTmp.length + ARRAY_INC) ?
                       size :
                       routeTmp.length + ARRAY_INC;

            routeTmp = new RouteHolder[reqd];
        }

        routesToAdd.toArray(routeTmp);

        for(int i = 0; i < size; i++) {
            // First need to locate the list dealing with this eventOut
            rh = (RouteHolder)routeTmp[i];
            routeTmp[i] = null;

            tester.srcNode = rh.srcNode;
            tester.srcIndex = rh.srcIndex;

            ArrayList<RouteHolder> l = sourceMap.get(tester);

            if(l == null) {
                l = new ArrayList<RouteHolder>();
                // create a new one because we end up with dodgy values
                // otherwise.
                EventOutHolder eoh = new EventOutHolder();
                eoh.srcNode = rh.srcNode;
                eoh.srcIndex = rh.srcIndex;

                sourceMap.put(eoh, l);
            }

            l.add(rh);
        }

        // Update the flat list of the source values
        sourceCount = sourceMap.size();

        if(sourceCount > routeList.length) {
            int old_size = routeList.length;
            int reqd = (sourceCount >= routeTmp.length + ARRAY_INC) ?
                       sourceCount :
                       routeTmp.length + ARRAY_INC;

            routeList = new EventOutHolder[reqd];
        }

        Set<EventOutHolder> keys = sourceMap.keySet();
        keys.toArray(routeList);

        routesToAdd.clear();
        routesToRemove.clear();
    }

    /**
     * Clear all the routeSet currently being managed here. The space this router
     * represents is being deleted.
     */
    public void clear() {
        routeSet.clear();
        routesToAdd.clear();
        routesToRemove.clear();

        sourceMap.clear();

        for(int i = 0; i < sourceCount; i++)
            routeList[i] = null;

        sourceCount = 0;
    }
}
