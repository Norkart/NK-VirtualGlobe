/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
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

// Standard imports
import java.util.Enumeration;
import javax.media.j3d.Behavior;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.media.j3d.WakeupOnElapsedFrames;
import javax.vecmath.Point3d;

// Application specific imports
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLTimeListener;

/**
 * The Java3D time scheduler that deals with all time information within a
 * given scene.
 * <p>
 *
 * The time schedular works as a standard Java3D Behaviour that requires being
 * woken up on each frame. During the startup phase, it waits for the viewpoint
 * to activate the scene before starting to generate the time clicks. In this
 * way, no behaviours or VRML code is executed before anything becomes visible
 * on the screen.
 * <p>
 *
 * The current implementation is not particularly efficient. It will always
 * run regardless of whether any listeners are registered or not. Thus it will
 * always take maximum CPU time. Not good. Need some way of registering that
 * we have no listeners and therefore not to run until a listener has been
 * been added.
 * <p>
 *
 * Another problem is dealing with Java time. On Win32 boxes the accuracy is
 * pretty fucked - 10ms at best on an NT/2K box. The result is that we get this
 * behaviour called every frame and we have no way of dealing with the fact that
 * time has not advanced at all according to the system clock. One way of doing
 * this is looking for the last time, checking the difference and then
 * incrementing by a very small amount (say 1ms). This should give accuracy up
 * to about 100Hz frame rate. How we go when it runs faster than that, I have
 * no idea. Jump up and down and bitch to Sun about it.
 * <p>
 *
 * During each time click we first call the listeners for time information with
 * the current time. After all of these have been called, the RouteManager is
 * then called to propogate all of the routes and other information. We do
 * this so that we can have something to process. Usually an event cascade is
 * created when a time sensor fires. To do this we must first set the time so
 * that we can generate the resulting cascade.
 * <p>
 *
 * In order for time to always run, we internally create a schedule bounds that
 * is infinite.
 * <p>
 *
 * Time management exists on two levels. The timesensor can be enabled and
 * disabled like any other behavior. This allows the user to pause the play
 * back or just halt in mid-stream. The other part allows the user to reset
 * the time clock so that we can start at time zero again. This allows the
 * replaying of a world from the time zero independently of the enable - for
 * example, loading a new world into a browser and keeping the same clock
 * instance.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class TimeScheduler extends Behavior implements VRMLClock {

    /** Default number Time listeners to create */
    private static final int DEFAULT_LISTENER_SIZE = 10;

    /** The increment amount for the listeners array size */
    private static final int LISTENER_SIZE_INC = 5;

    /** The time increment when all else fails */
    private static final int TIME_FUDGE_FACTOR = 1;

    /** The conditions that we always use for time clicks */
    private WakeupOnElapsedFrames tick_critter;

    /** Condition to wake up a sleeping behaviour */
    private WakeupOnBehaviorPost sleep_critter;

    /** The current time that we are sending to people */
    private double currentTime = -1;

    /** The current wall clock time that we are sending to people */
    private long currentWallTime;

    /** The last clock tick. We don't bother sending if the diff is zero */
    private long lastWallTime;

    /** The array holding all of the values */
    private VRMLTimeListener[] timeListeners;

    /** The number of listeners to process */
    private int numTimeListeners;

    /**
     * Create a new time scheduler behaviour
     */
    public TimeScheduler() {
        tick_critter = new WakeupOnElapsedFrames(0);
        sleep_critter =
            new WakeupOnBehaviorPost(this, BehaviorIDConstants.VRML_CLOCK_ID);

        Point3d center = new Point3d();
        BoundingSphere bounds =
            new BoundingSphere(center, Double.POSITIVE_INFINITY);

        setSchedulingBounds(bounds);

        timeListeners = new VRMLTimeListener[DEFAULT_LISTENER_SIZE];
        numTimeListeners = 0;
    }

    //----------------------------------------------------------
    // Methods required by the Behavior interface.
    //----------------------------------------------------------

    /**
     * Initialise the behavior to start running. This will register the
     * first criteria. Don't wake until we have arrived in the activation
     * area of the viewpoint and then run every frame.
     */
    public void initialize() {

        lastWallTime = System.currentTimeMillis();

        currentWallTime = System.currentTimeMillis();

        // This is to deal with the fucked Win32 system clock.
        if((currentWallTime - lastWallTime) <= 0)
            currentWallTime += TIME_FUDGE_FACTOR;

        // VRML time is in seconds. Yes, type conversion long->double
        currentTime = currentWallTime * 0.001;

        if(numTimeListeners == 0)
            wakeupOn(sleep_critter);
        else
            wakeupOn(tick_critter);
    }

    /**
     * Process the event that builds the current time.
     *
     * @param why The list of conditions why this was woken
     */
    public void processStimulus(Enumeration why) {

        currentWallTime = System.currentTimeMillis();

        // This is to deal with the fucked Win32 system clock.
        if((currentWallTime - lastWallTime) <= 0)
            currentWallTime += TIME_FUDGE_FACTOR;

        // VRML time is in seconds. Yes, type conversion long->double
        currentTime = currentWallTime * 0.001;

        for(int i = numTimeListeners; --i >= 0; ) {
            try {
                timeListeners[i].timeClick(currentWallTime);
            } catch(Exception e) {
                System.out.println("Error sending time " + e);
                e.printStackTrace();
            }
        }

        postId(BehaviorIDConstants.ROUTE_REQUIRED_ID);

        if(numTimeListeners == 0) {
            wakeupOn(sleep_critter);
        } else {
            wakeupOn(tick_critter);
        }
    }

    //----------------------------------------------------------
    // Methods required by the VRMLClock interface.
    //----------------------------------------------------------

    /**
     * Request the current time from the system clock. The time returned
     * is the VRML scene time.
     *
     * @return The current time in VRML coodinates
     */
    public double getTime() {
        return currentTime;
    }

    /**
     * Request the time in wall-clock coordinates. This is standard Unix
     * epoch time.
     *
     * @return The current wall-clock time
     */
    public long getWallTime() {
        return currentWallTime;
    }

    /**
     * Reset the clock to the current time as time zero. This can only be
     * called when the timer is not enabled. The last wall clock time is set
     * to now so that fraction information will be correctly oriented for
     * the next time it is enabled. So, ideally it should be called just before
     * setEnabled to get the right time setups working.
     */
    public void resetTimeZero() {
        if(getEnable())
            return;

        lastWallTime = System.currentTimeMillis();
        currentWallTime = lastWallTime;
    }

    /**
     * Add a time listener to this clock. Only one instance of each listener
     * will be registered and null references are ignored.
     *
     * @param l The listener instance to add
     */
    public void addTimeListener(VRMLTimeListener l) {

        if(findListener(l) != -1)
            return;

        if(numTimeListeners == timeListeners.length) {
            int length = timeListeners.length;
            VRMLTimeListener[] tmp = new VRMLTimeListener[length + LISTENER_SIZE_INC];
            System.arraycopy(timeListeners, 0, tmp, 0, length);
            timeListeners = tmp;
        }

        timeListeners[numTimeListeners++] = l;

        if(numTimeListeners == 1) {
            postId(BehaviorIDConstants.VRML_CLOCK_ID);
        }
    }

    /**
     * Remove a time listener to this clock. If the listener is not known to
     * this implementation, it is silently ignored.
     *
     * @param l The listener instance to add
     */
    public void removeTimeListener(VRMLTimeListener l) {
        int index = findListener(l);

        if(index == -1)
            return;

        if(index == (numTimeListeners - 1)) {
            timeListeners[index] = null;
        } else {
            System.arraycopy(timeListeners,
                             index + 1,
                             timeListeners,
                             index,
                             numTimeListeners - index - 1);
        }

        numTimeListeners--;
    }

    /**
     * Internal convenience method to find a listener instance in the list
     *
     * @param l The listener to locate
     * @return the listener index or -1 if not found
     */
    private int findListener(VRMLTimeListener l) {
        int ret_val = -1;

        for(int i = numTimeListeners; --i >= 0; ) {
            if(timeListeners[i] == l) {
                ret_val = i;
                break;
            }
        }

        return ret_val;
    }
}
