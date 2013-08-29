/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.swt.widgets;

// External imports
import java.util.TimerTask;

// Local imports
// None

/**
 * An alarm or timer function. This is a simple extension of a TimerTask
 * that generates an event as a result of the Timer expiring. The class 
 * was created to separate TimerTask functionality from objects servicing
 * swt ui objects.
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class Alarm extends TimerTask {
    
    /** Initial null array of AlarmListener's */
    protected static final AlarmListener[] NULL_AL_ARRAY = new AlarmListener[0];
    
    /** The working array of listeners */
    protected AlarmListener[] alarmListenerArray = NULL_AL_ARRAY;
    
    /** The one and only event object */
    protected AlarmEvent theEvent;
    
    /**
     * Default Constructor
     */
    public Alarm( ) {
        this( null );
    }
    
    /** 
     * Constructor
     * @param listener - the initial listener
     */
    public Alarm( AlarmListener listener ) {
        addAlarmListener( listener );
        theEvent = new AlarmEvent( this );
    }
    
    /**
     * The Timer has expired, notify the listeners
     */
    public void run( ) {
        fireAlarmEvent( theEvent );
    }
    
    /**
     * Add a listener for <code>AlarmEvent</code>s
     * @param listener the listener
     */
    public synchronized void addAlarmListener( AlarmListener listener ) {
        if ( listener == null ) { return; }
        //
        if ( alarmListenerArray == NULL_AL_ARRAY ) { 
            alarmListenerArray = new AlarmListener[]{ listener }; 
        }
        else {
            int length = alarmListenerArray.length;
            AlarmListener[] tmp = new AlarmListener[ length + 1 ];
            System.arraycopy( alarmListenerArray, 0, tmp, 0, length );
            tmp[length] = listener;
            alarmListenerArray = tmp;
        }
    }
    /**
     * Remove a listener for <code>AlarmEvent</code>s
     * @param listener the listener
     */
    public synchronized void removeAlarmListener( AlarmListener listener ) {
        if ( listener == null ) { return; }
        //
        // find the index of the listener to remove
        int index = -1;
        for ( int i = alarmListenerArray.length - 1; i >= 0; i-- ) {
            if ( alarmListenerArray[i].equals( listener ) ) {
                index = i;
                break;
            }
        }
        if ( index != -1 ) {
            //
            // recreate the array of listeners if necessary
            int newLength = alarmListenerArray.length - 1;
            if ( newLength == 0 ) { alarmListenerArray = NULL_AL_ARRAY; }
            else {
                AlarmListener[] tmp = new AlarmListener[ newLength ];
                System.arraycopy( alarmListenerArray, 0, tmp, 0, index );
                if ( index < newLength ) {
                    System.arraycopy( alarmListenerArray, index+1, tmp, index, newLength - index);
                }
                alarmListenerArray = tmp;
            }
        }
    }
    /**
     * Send a <code>AlarmEvent</code> to all registered listeners
     * @param ae the <code>AlarmEvent</code> to send
     */
    protected void fireAlarmEvent( AlarmEvent ae ) {
        AlarmListener[] listenerArray = alarmListenerArray;
        int length = listenerArray.length;
        for ( int i = 0; i < length; i++ ) { listenerArray[i].alarmAction( ae ); }
    }
}

