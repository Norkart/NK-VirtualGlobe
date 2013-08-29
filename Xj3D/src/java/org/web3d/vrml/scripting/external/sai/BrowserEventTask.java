/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.scripting.external.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.BrowserEvent;
import org.web3d.x3d.sai.BrowserListener;

/**
 * A Runnable task that gets registered with an executor service that will send
 * out event notifications.
 * <p>
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
class BrowserEventTask implements Runnable {

    /**
     * The initial listener instance to send the event to. Really this is
     * the multicaster, so all listener intances will get this event.
     */
    private BrowserListener listener;

    /** The local reporter for internal usage when no external reporter */
    private BrowserEvent event;

    /**
     * Creates a instance of the adapter that will interface with the
     * given internal error reporter instance.
     */
    BrowserEventTask(BrowserListener l, BrowserEvent evt) {
        listener = l;
        event = evt;
    }

    //-------------------------------------------------------------------
    // Methods defined by Runnable
    //-------------------------------------------------------------------

    /**
     * Execute the task now.
     */
    public void run() {
        listener.browserChanged(event);
    }
}
