/*****************************************************************************
 *                        Web3d.org Copyright (c) 2007
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
// None

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLTimeListener;

import org.xj3d.core.eventmodel.TimeSensorManager;

/**
 * A manager for time sensor nodes that allows application control
 * of the clock.
 *
 * @author Rex Melton
 * @version $Revision: 1.2 $
 */
public class VirtualTimeSensorManager implements TimeSensorManager {

    /** Default number Time listeners to create */
    private static final int DEFAULT_LISTENER_SIZE = 10;

    /** The increment amount for the listeners array size */
    private static final int LISTENER_SIZE_INC = 5;

    /** The time increment when all else fails */
    private static final int TIME_FUDGE_FACTOR = 1;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The current time that we are sending to people */
    private double currentTime = -1;

    /** The current wall clock time that we are sending to people */
    private long currentWallTime;

    /** The array holding all of the values */
    private VRMLTimeListener[] timeListeners;

    /** The number of listeners to process */
    private int numTimeListeners;

    /** The virtual clock's tick increment */
    private int tickIncrement;
    
    /**
     * Create a new time scheduler behaviour
     */
    public VirtualTimeSensorManager() {
        errorReporter = DefaultErrorReporter.getDefaultReporter();

        timeListeners = new VRMLTimeListener[DEFAULT_LISTENER_SIZE];
        numTimeListeners = 0;

        currentWallTime = System.currentTimeMillis();

        // VRML time is in seconds. Yes, type conversion long->double
        currentTime = currentWallTime * 0.001;
        tickIncrement = 0;
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
     * the next time it is enabled called to issue a clock tick.
     */
    public void resetTimeZero() {
        currentWallTime = System.currentTimeMillis();
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

		if(!(index == (--numTimeListeners))) {
			// if the listener reference is not the last on the list,
			// then shift the list to overwrite the reference
            System.arraycopy(timeListeners,
                             index + 1,
                             timeListeners,
                             index,
                             numTimeListeners - index);
			
        }
		// null out the last reference in the array
		timeListeners[numTimeListeners] = null;
    }

    //----------------------------------------------------------
    // Methods defined by TimeSensorManager
    //----------------------------------------------------------

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
     * Process the event that builds the current time.
     *
     * @param time The current clock cycle time in milliseconds
     */
    public void clockTick(long time) {
        
        for(int i = numTimeListeners; --i >= 0; ) {
            try {
                timeListeners[i].timeClick(currentWallTime);
            } catch(Exception e) {
                errorReporter.errorReport("Problem sending clock ticks", e);
            }
        }
    }

    /**
     * Force clearing all state from this manager now. This is used to indicate
     * that a new world is about to be loaded and everything should be cleaned
     * out now.
     */
    public void clear() {
        for(int i = 0; i < numTimeListeners; i++)
            timeListeners[i] = null;

        numTimeListeners = 0;
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Adjust the clock by a tick increment
     */
    public void tick( ) {
        currentWallTime += tickIncrement;

        // VRML time is in seconds. Yes, type conversion long->double
        currentTime = currentWallTime * .001;
    }
    
    /**
     * Set the increment for each virtual clock tick in milliseconds.
     *
     * @param tickIncrement The increment for each virtual clock tick.
     */
    public void setTickIncrement( int tickIncrement ) {
        this.tickIncrement = tickIncrement;
    }
    
    /**
     * Return the increment for each virtual clock tick in milliseconds.
     *
     * @return The increment for each virtual clock tick.
     */
    public int getTickIncrement( ) {
        return( tickIncrement );
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
