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

package org.web3d.vrml.renderer.j3d.input;

// External imports
import java.util.Enumeration;

import javax.media.j3d.*;

import javax.vecmath.Point3d;

// Local imports
import org.web3d.vrml.nodes.VRMLClock;
import org.xj3d.core.eventmodel.RouteManager;

/**
 * A Java3D behavior that is triggered when we need to do routing.
 * <p>
 *
 * This behaviour will run the route manager exactly once any time that some
 * piece of code needs to do routing. It should <i>never</i> be called by
 * anything other than the behaviors in this package.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public class RouteBehavior extends Behavior {

    /** The criteria used to wake up for mouse events */
    private WakeupOnBehaviorPost criteria1;
    private WakeupOnElapsedTime criteria2;

    /** The navigation processor we are delegating to do the work */
    private RouteManager routeManager;

    /** The current clock for processing */
    private VRMLClock clock;

    private WakeupOr wup;

    /**
     * Create a new behavior with default settings.
     */
    public RouteBehavior() {

        criteria1 =
            new WakeupOnBehaviorPost(null,
                                     BehaviorIDConstants.ROUTE_REQUIRED_ID);
        criteria2 =
            new WakeupOnElapsedTime(100);

        Point3d center = new Point3d();
        BoundingSphere bounds =
            new BoundingSphere(center, Double.POSITIVE_INFINITY);

        setSchedulingBounds(bounds);
    }

    //----------------------------------------------------------
    // Methods defined by Behavior
    //----------------------------------------------------------

    /**
     * Initialise the behavior to start running. This will register the
     * first criteria. Don't wake until we have arrived in the activation
     * area of the viewpoint and then run every frame.
     */
    public void initialize() {
        WakeupCriterion wc[] = new WakeupCriterion[2];
        wc[0] = criteria1;
        wc[1] = criteria2;
        wup = new WakeupOr(wc);
        wakeupOn(wup);
    }

    /**
     * Process the event that builds the current time.
     *
     * @param why The list of conditions why this was woken
     */
    public void processStimulus(Enumeration why) {
        if((routeManager != null) && (clock != null))
            routeManager.processRoutes(clock.getTime());

        wakeupOn(wup);
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Set up the system as needed. Passing values of null will remove the
     * current items.
     *
     * @param clk The new clock to use for time info
     * @param router The class to hanlde routing information
     */
    public void setup(VRMLClock clk, RouteManager router) {
        clock = clk;
        routeManager = router;
    }
}
