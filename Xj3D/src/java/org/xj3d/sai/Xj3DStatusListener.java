/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.sai;

// External imports
// None

// Local imports
import org.web3d.x3d.sai.ExternalBrowser;

/**
 * Listener for basic status information from the browser internals.
 * <p>
 *
 * Status information is not the same as messages that are reported using
 * the error reporter interface. These are for simple single-line messages.
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface Xj3DStatusListener {

    /**
     * Notification that a single line status message has changed to the new
     * string. A null string means to clear the currently displayed message.
     *
     * @param msg The new message string to display for the status
     */
    public void updateStatusMessage(String msg);

    /**
     * Notification that the calculated frames per second has changed to this
     * new value. It is expected that this is called frequently.
     */
    public void updateFramesPerSecond(float fps);

    /**
     * Notification of a progress update. There may be several items in
     * progression at once (eg multithreaded texture and scripting loading)
     * so implementers should work appropriately for this situation. To keep
     * this aligned, each item that is reporting progress will have a unique
     * ID string (for this session) associated with it so you can keep track
     * of the multiples. Once 100% has been reached you can assume that the
     * tracking is complete for that object.
     *
     * @param id A unique ID string for the given item
     * @param msg A message to accompany the update
     * @param perc A percentage from 0-100 of the progress completion
     */
    public void progressUpdate(String id, String msg, float perc);
}
