/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001-2005
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.x3d.sai;

// External imports
import java.util.EventObject;

// Local imports
// None

 /**
  * Class representing events that happen to the VRML browser.
  * <P>
  * This event notifies listener classes about changes to the browser that
  * may be of interest to the client. VRML events cannot be consumed in the
  * way that java AWT events can be.
  *
  * @version 1.1 3 August 1998
  */
public class BrowserEvent extends EventObject {

    /**
     * The browser has completed the initial loading of the world. Event is
     * generated just after the scene has been loaed and just before the first
     * event has been sent
     */
    public static final int INITIALIZED = 0;

    /**
     * The currently loaded world is about to be unloaded. Called just before
     * the scene is about to be unloaded. If another world is to replace this,
     * then an initialize event will be generated following this one.
     */
    public static final int SHUTDOWN = 1;

    /**
     * An error occurred in loading VRML from a URL call. Source could be either
     * a createVrmlFromURL call or loadURL.
     */
    public static final int URL_ERROR = 2;

    /**
     * An error has occured that has caused the connection between the browser
     * and the external application to fail. This may be the VRML browser
     * crashing, or a network connection falling over.
     */
    public static final int CONNECTION_ERROR = 10;

    /**
     * The number of reserved identifier numbers for event conditions. Any
     * value below this is considered to be a general VRML defined event as
     * specified in the External Authoring Interface specification. Any values
     * above this are browser specific messages.
     */
    public static final int LAST_IDENTIFIER = 100;

    /** The id of the event that this class instance represents */
    private int id;

    /**
     * Create a new browser event.
     *
     * @param browser The source of the browser that generated this event
     * @param action The event type to create
     * @exception IllegalArgumentException if the action or browser id are not
     *   legal values
     */
    public BrowserEvent(Object browser, int action) {

        super(browser);

        if(browser == null)
            throw new IllegalArgumentException("Null browser reference");

        if(action < 0)
            throw new IllegalArgumentException("Invalid event action type");

        id = action;
    }

    /**
     * Get the type of event that has occurred.
     *
     * @return The type of event as defined by the types
     * @see #INITIALIZED
     * @see #SHUTDOWN
     */
    public int getID()
    {
        return id;
    }
}
