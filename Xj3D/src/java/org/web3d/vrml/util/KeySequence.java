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

package org.web3d.vrml.util;

// External imports - NONE

// Local imports - NONE

/**
 * A sequence, much like the collections List interface, specifically
 * designed for queueing up sequences of Xj3DKeyEvents between
 * rendering frames. 
 * <p>
 * The methods involved with loading a sequence or manipulating it's
 * underlying array of events are synchronized. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class KeySequence {
    
    /** Static initial array size. */
    private static final int DEFAULT_INITIAL_CAPACITY = 128;
    
    /** Increment size */
    private static final int INCREMENT_SIZE = 16;
    
    /** Mutex object for reading/writing the key events */
    private Object mutex;
    
    /** The number of key events currently stored */
    protected int index;
    
    /** The array storing key events */
    protected Xj3DKeyEvent[] eventArray;
    
    /**
     * Construct an instance with the default capacity.
     */
    public KeySequence( ) {
        this( DEFAULT_INITIAL_CAPACITY );
    }
    
    /**
     * Construct an instance with the specified initial capacity.
     *
     * @param initialCapacity - The initial holding capacity for events.
     */
    public KeySequence( int initialCapacity ) {
        mutex = new Object( );
        index = 0;
        eventArray = new Xj3DKeyEvent[initialCapacity];
    }
    
    /**
     * Return the key event at the specified index.
     *
     * @param i - The index of the event to return
     * @return The event
     * @throws IndexOutOfBoundsException - if i is out of range 
     * ( i < 0 || i >= size( ) ).
     */
    public Xj3DKeyEvent get( int i ) {
        return( eventArray[i] );
    }
    
    /**
     * Return the number of events in the sequence
     *
     * @return the number of events in the sequence
     */
    public int size( ) {
        return( index );
    }
    
    /**
     * Add the event to the end of the sequence
     *
     * @param evt - The event
     */
    public void add( Xj3DKeyEvent evt ) {
        if ( index == eventArray.length ) {
            ensureCapacity( index + INCREMENT_SIZE );
        }   
        synchronized( mutex ) {
            eventArray[index++] = evt;
        }
    }
    
    /**
     * Clear the sequence
     */
    public void clear( ) {
        if ( index > 0 ) {
            synchronized( mutex ) {
                for ( int i = index - 1; i >= 0; i-- ) {
                    eventArray[i] = null;
                }
                index = 0;
            }
        }
    }
    
    /**
     * Move the current contents of this object into the argument sequence
     * object. Any events currently in the argument sequence will be cleared.
     * The argument sequence's capacity will be increased if it is
     * insufficent to hold the complete set of events. At the end of the
     * transfer, this object will be empty.
     *
     * @param seq - The KeySequence object to initialize
     */
    public void transfer( KeySequence seq ) {
        seq.clear( );
        if ( seq.eventArray.length < index ) {
            seq.ensureCapacity( index );
        }
        if ( index > 0 ) {
            synchronized( mutex ) {
                for ( int i = index - 1; i >= 0; i-- ) {
                    seq.eventArray[i] = eventArray[i];
                    eventArray[i] = null;
                }
                seq.index = index;
                index = 0;
            }
        }
    }
    
    /**
     * Ensure that the capacity of this sequence is sufficient to
     * contain the specified number of events.
     *
     * @param minCapacity the minimum number of events that this
     * sequence must be able to hold.
     */
    public void ensureCapacity( int minCapacity ) {
        int newCapacity = eventArray.length;
        if ( newCapacity < minCapacity ) {
            while ( newCapacity < minCapacity ) {
                newCapacity += INCREMENT_SIZE;
            }
            synchronized( mutex ) {
                Xj3DKeyEvent[] tmp = new Xj3DKeyEvent[newCapacity];
                if ( index > 0 ) {
                    System.arraycopy( eventArray, 0, tmp, 0, index );
                }
                eventArray = tmp;
            }
        }
    }
}

