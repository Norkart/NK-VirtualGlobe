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
import java.util.Map;
import java.util.WeakHashMap;

// Local imports
import org.web3d.vrml.nodes.*;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.util.HashSet;
import org.web3d.vrml.lang.BasicScene;
import org.web3d.vrml.lang.ROUTE;
import org.web3d.vrml.lang.TypeConstants;
import org.web3d.vrml.lang.VRMLExecutionSpace;

import org.xj3d.core.eventmodel.Router;
import org.xj3d.core.eventmodel.RouterFactory;
import org.xj3d.core.eventmodel.RouteManager;

/**
 * A manager of high-level route organisation based on execution spaces.
 * <p>
 *
 * Does not perform direct routing itself, but manages the execution space
 * and delegates the routing to individual router instances, one per
 * execution space. The idea of this is to allow multi-threaded route handlers
 * if required. Each execution space can provide a routing mechanism that
 * operates in its own thread or they all live in one thread.
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
 * @version $Revision: 1.2 $
 */
public class DefaultRouteManager implements RouteManager {

    /** The default initial size of the array of routes */
    private static final int DEFAULT_SIZE = 32;

    /** Increment of the array size when we need to increase */
    private static final int ARRAY_INC = 8;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** Map of execution spaces (key) to router component (value) */
    private Map spaceMap;

    /** The factory used to create new routers */
    private RouterFactory factory;

    /** All the spaces in a flat array to avoid GC using iterators */
    private Router[] routers;

    /** List of valid entries in the router array so far */
    private int numRouters;

    /** Flags to say if a particular router is in use currently */
    private boolean[] routerInUse;

    /**
     * Singleton flag to say if we should go looking for a router that is not
     * currently in use. Saves us iterating through the list each time.
     */
    private boolean hasUnusedRouter;

    /** Set of all spaces waiting to be added */
    private HashSet spacesToAdd;

    /** Set of all spaces waiting to be removed */
    private HashSet spacesToRemove;

    /** Temporary array used to fetch the values out of the spacesToX sets */
    private VRMLExecutionSpace[] tmpSpaceVals;

    /**
     * Create a new instance of the execution space manager to run all the
     * routing.
     */
    public DefaultRouteManager() {
        super();

        spaceMap = new WeakHashMap();
        spacesToAdd = new HashSet();
        spacesToRemove = new HashSet();

        numRouters = 0;
        hasUnusedRouter = false;
        routers = new Router[DEFAULT_SIZE];
        routerInUse = new boolean[DEFAULT_SIZE];

        tmpSpaceVals = new VRMLExecutionSpace[DEFAULT_SIZE];

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by RouteManager
    //----------------------------------------------------------

    /**
     * Register an error reporter with the manager so that any errors generated
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
     * Set the factory needed to create new router instances for a new
     * execution space. If the reference is null, then this clears the current
     * factory so that we don't accept sub-space handling.
     *
     * @param fac The factory instance to use
     */
    public void setRouterFactory(RouterFactory fac) {
        factory = fac;
    }

    /**
     * Process all of the available routes until there are no more to process.
     * This method will not return until all routes have been processed.
     *
     * @param timestamp The timestamp for when these routes should be executed
     * @return false No event outs needed processing this call
     */
    public boolean processRoutes(double timestamp) {

        boolean ret_val = false;

        for(int i = 0; i < numRouters; i++) {
            if(routerInUse[i]) {
                boolean curr = routers[i].processRoutes(timestamp);

                ret_val = ret_val || curr;
            }
        }

        return ret_val;
    }

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
                         int destIndex) {

        Router router = getRouter(space);

        if(router != null)
            router.addRoute(srcNode, srcIndex, destNode, destIndex);
    }

    /**
     * Add a route object to the system. If the route exists in the system,
     * this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param route The object to add
     */
    public void addRoute(VRMLExecutionSpace space, ROUTE route) {
        Router router = getRouter(space);
        if(router != null)
            router.addRoute((VRMLNodeType)route.getSourceNode(),
                            route.getSourceIndex(),
                            (VRMLNodeType)route.getDestinationNode(),
                            route.getDestinationIndex());
    }

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
                            int destIndex) {

        Router router = (Router)spaceMap.get(space);

        if(router != null)
            router.removeRoute(srcNode, srcIndex, destNode, destIndex);
        else
            errorReporter.warningReport("Attempting to remove route from a " +
                                        "space that doesn't exist",
                                        null);
    }

    /**
     * Remove a route object from the system. If the route does not exist in
     * the system, this silently ignores the request.
     *
     * @param space The execution space for the route
     * @param route The object to remove
     */
    public void removeRoute(VRMLExecutionSpace space, ROUTE route) {
        Router router = (Router)spaceMap.get(space);

        if(router != null) {
            router.removeRoute((VRMLNodeType)route.getSourceNode(),
                               route.getSourceIndex(),
                               (VRMLNodeType)route.getDestinationNode(),
                               route.getDestinationIndex());
        } else
            errorReporter.warningReport("Attempting to remove route (object) " +
                                        "from a space that doesn't exist",
                                        null);
    }

    /**
     * Add an execution space to the system.  This will add all its routes
     * and any contained spaces such as protos and inlines. If this space has
     * already been added, the request will be ignored.
     *
     * @param space The execution space to add
     */
    public synchronized void addSpace(VRMLExecutionSpace space) {
        if(spaceMap.containsKey(space) || spacesToAdd.contains(space)) {
            errorReporter.warningReport("Trying to add duplicate space!",
                                        null);

            return;
        }
        spacesToAdd.add(space);
    }

    /**
     * Remove an execution space to the system.  This will add all its routes
     * and any contained spaces such as protos and inlines. If this request has
     * not been added it will be ignored.
     *
     * @param space The execution space to add
     */
    public synchronized void removeSpace(VRMLExecutionSpace space) {
        if(!spaceMap.containsKey(space)) {
            errorReporter.warningReport("Trying to remove non-existent space!",
                                        null);
            return;
        } else if(spacesToRemove.contains(space)) {
            errorReporter.warningReport("Trying to duplicate remove space!!",
                                        null);
            return;
        }

        spacesToRemove.add(space);
    }

    /**
     * Notification that the route manager should now propogate all added and
     * removed routes and spaces from this list into the core evaluatable
     * system.
     */
    public synchronized void updateSpaces() {

        int i;
        int reqd_size =
            spaceMap.size() + spacesToAdd.size() - spacesToRemove.size();

        checkSize(reqd_size);

        // First iterate through all the ones being cleared so that we might be
        // able to immediately reuse it for the ones being added.
        reqd_size = spacesToRemove.size();

        if(reqd_size != 0) {
            if(tmpSpaceVals.length < reqd_size)
                tmpSpaceVals = new VRMLExecutionSpace[reqd_size];

            spacesToRemove.toArray(tmpSpaceVals);

            for(i = 0; i < reqd_size; i++) {
                BasicScene scene = tmpSpaceVals[i].getContainedScene();
                delayedRemoveSpace(tmpSpaceVals[i]);
                tmpSpaceVals[i] = null;
            }
        }


        reqd_size = spacesToAdd.size();

        if(reqd_size != 0) {
            if(tmpSpaceVals.length < reqd_size)
                tmpSpaceVals = new VRMLExecutionSpace[reqd_size];

            spacesToAdd.toArray(tmpSpaceVals);

            for(i = 0; i < reqd_size; i++) {
                delayedAddSpace(tmpSpaceVals[i]);
                tmpSpaceVals[i] = null;
            }
        }

        spacesToAdd.clear();
        spacesToRemove.clear();
    }

    /**
     * Notification that the route manager should now propogate all added and
     * removed routes from this list into the core evaluatable space.
     */
    public void updateRoutes() {
        for(int i = 0; i < numRouters; i++) {
            if(routerInUse[i]) {
                routers[i].updateRoutes();
            }
        }
    }

    /**
     * Clear all the routes currently being managed here. The space this router
     * represents is being deleted.
     */
    public void clear() {
        for(int i = 0; i < numRouters; i++) {
            if(routerInUse[i]) {
                routers[i].clear();
                routerInUse[i] = false;
            }
        }

        spaceMap.clear();
        spacesToAdd.clear();
        spacesToRemove.clear();
    }

    //----------------------------------------------------------
    // Local methods
    //----------------------------------------------------------

    /**
     * Add an execution space to the system.  This will add all its routes
     * and any contained spaces such as protos and inlines.  Any duplicated
     * routes will be silently ignored.
     *
     * @param space The execution space to add
     */
    private void delayedAddSpace(VRMLExecutionSpace space) {
        if(space == null)
            return;

        BasicScene scene = space.getContainedScene();

        if(scene == null)
            return;

        Router router = getRouter(space);

        // Add routes defined in top level
        int i;
        int size;
        ArrayList node_list = scene.getRoutes();

        router.addRoutes(node_list);

        // Recursively add contained spaces

        // Handle Prototypes
        node_list = scene.getByPrimaryType(TypeConstants.ProtoInstance);
        size = node_list.size();

        for(i = 0; i < size; i++) {
            VRMLExecutionSpace nested_space =
                (VRMLExecutionSpace)node_list.get(i);

            if (((VRMLProtoInstance)nested_space).getImplementationNode() == null) {
            }
            else {
                delayedAddSpace(nested_space);
            }
        }

        // Handle Inlines. If the inline is not loaded yet, just ignore it.
        // We'll pick it up next time around when the content is loaded. That
        // will cause the addSpace method to be called again and eventually
        // make its way back into here.
        node_list = scene.getByPrimaryType(TypeConstants.InlineNodeType);
        size = node_list.size();
        VRMLInlineNodeType inline;

        for(i = 0; i < size; i++) {
            inline = (VRMLInlineNodeType)node_list.get(i);

            if (inline.getLoadState() == VRMLExternalNodeType.LOAD_COMPLETE) {
                delayedAddSpace(inline);
            }
        }
    }

    /**
     * Remove an execution space to the system.  This will remove all its
     * routes and any contained spaces such as protos and inlines.
     *
     * @param space The execution space to add
     */
    private void delayedRemoveSpace(VRMLExecutionSpace space) {
        if(space == null)
            return;

        BasicScene scene = space.getContainedScene();

        if(scene == null)
            return;

        Router router = (Router)spaceMap.remove(space);

        if(router == null)
            return;

        int i;
        int size;

        router.clear();

        // find this router in the list of all and mark it available
        for(i = 0; i < numRouters; i++) {
            if(routers[i] == router) {
                routerInUse[i] = false;
                break;
            }
        }

        // Recursively add contained spaces

        // Handle Prototypes
        ArrayList node_list =
            scene.getByPrimaryType(TypeConstants.ProtoInstance);
        size = node_list.size();

        for(i = 0; i < size; i++) {
            VRMLExecutionSpace nested_space =
                (VRMLExecutionSpace)node_list.get(i);

            delayedRemoveSpace(nested_space);
        }

        // Handle Inlines. If the inline is not loaded yet, just ignore it.
        // We'll pick it up next time around when the content is loaded. That
        // will cause the addSpace method to be called again and eventually
        // make its way back into here.
        node_list = scene.getByPrimaryType(TypeConstants.InlineNodeType);
        size = node_list.size();
        VRMLInlineNodeType inline;

        for(i = 0; i < size; i++) {
            inline = (VRMLInlineNodeType)node_list.get(i);

            if (inline.getLoadState() == VRMLExternalNodeType.LOAD_COMPLETE) {
                delayedRemoveSpace(inline);
            }
        }
    }

    /**
     * Convenience method to locate a Router instance to for the given space.
     * If one is not already assigned, it will look in the spares list or
     * create a new one.
     *
     * @param space The execution space to work with
     */
    private Router getRouter(VRMLExecutionSpace space) {
        Router router = (Router)spaceMap.get(space);

        if((router == null) && (factory != null)) {
            // do a look through the list for a spare space that is not in
            // use currently.
            if(hasUnusedRouter) {
                int i;

                for(i = 0; i < numRouters; i++) {
                    if(!routerInUse[i]) {
                        router = routers[i];
                        routerInUse[i] = true;
                        break;
                    }
                }

                // Did we find the last item in the array? If so that
                // means we probably don't have any unused ones left.
                // The current logic is a little dodgy in that we could
                // probably optimise further by doing a sweep through
                // the rest of the array after the found one and check
                // to see if there were any unused ones left.
                if(i == (numRouters - 1))
                    hasUnusedRouter = false;
                else if((i == numRouters) && (router == null)) {
                    checkSize(-1);
                    routers[numRouters] = router;
                    routerInUse[numRouters] = true;
                    numRouters++;
                    hasUnusedRouter = false;
                }
            } else {
                checkSize(-1);
                router = factory.newRouter();
                routers[numRouters] = router;
                routerInUse[numRouters] = true;
                numRouters++;
            }

            spaceMap.put(space, router);
        }

        return router;
    }

    /**
     * Convenience method to resize the array if needed. A value of -1 as the
     * minimum size will indicate to just check on the current size versus the
     * number of routes and just do a stock increment.
     *
     * @param minReqd The minimum size we're going to need the array to be
     */
    private void checkSize(int minReqd) {

        if(minReqd == -1) {
            if(routers.length == numRouters) {
                int old_size = routers.length;

                Router[] tmp = new Router[old_size + ARRAY_INC];
                System.arraycopy(routers, 0, tmp, 0, old_size);
                routers = tmp;

                boolean[] tmp2 = new boolean[old_size + ARRAY_INC];
                System.arraycopy(routerInUse, 0, tmp2, 0, old_size);
                routerInUse = tmp2;
            }
        } else {
            if(routers.length < minReqd) {
                int old_size = routers.length;

                Router[] tmp = new Router[minReqd];
                System.arraycopy(routers, 0, tmp, 0, old_size);
                routers = tmp;

                boolean[] tmp2 = new boolean[minReqd];
                System.arraycopy(routerInUse, 0, tmp2, 0, old_size);
                routerInUse = tmp2;
            }
        }
    }
}
