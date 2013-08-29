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

package org.web3d.vrml.renderer;

// Standard imports
import java.util.Enumeration;

// Application specific imports
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLTimeListener;

/**
 * A default time scheduler that deals with all time information within a
 * given scene.
 * <p>
 *
 * Currently a stubbed implementation that does no work.
 *
 * @author Alan Hudson
 * @version $Revision: 1.3 $
 */
public class TimeScheduler implements VRMLClock {

    /** Default number Time listeners to create */
    private static final int DEFAULT_LISTENER_SIZE = 10;

    /** The increment amount for the listeners array size */
    private static final int LISTENER_SIZE_INC = 5;

    /** The time increment when all else fails */
    private static final int TIME_FUDGE_FACTOR = 1;

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
        currentWallTime = lastWallTime;

        // VRML time is in seconds. Yes, type conversion long->double
        currentTime = currentWallTime * 0.001;
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

        lastWallTime = currentWallTime;

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
