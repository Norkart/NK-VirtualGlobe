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

package org.xj3d.ui.construct.event;

// External imports
import java.util.EventObject;

// Local imports
// None

/**
 * The event object for distributing event notification. 
 *
 * @author Rex Melton
 * @version $Revision: 1.1 $
 */
public class RecorderEvent extends EventObject {
    
    /** Base identifier of events */
    private final static int EVENT_IDENTIFIER = 'R'<<24 | 'E'<<16;
    
    /** Event id indicating that recording has been initiated */
    public final static int ACTIVE = EVENT_IDENTIFIER + 0;

    /** Event id indicating that recording has completed successfully */
    public final static int COMPLETE = EVENT_IDENTIFIER + 1;

    /** The event id */
    public final int id;
    
    /** The number of frames recorded */
    public final int frames;
    
    /**
     * Construct a <code>RecorderEvent</code> object.
     *
     * @param source the object where the event originated
     * @param id The event type
     * @param frames The number of frames recorded
     */
    public RecorderEvent( Object source, int id, int frames ) {
        super( source );
        this.id = id;
        this.frames = frames;
    }
}

