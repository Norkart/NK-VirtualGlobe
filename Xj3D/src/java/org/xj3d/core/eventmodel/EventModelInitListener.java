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

package org.xj3d.core.eventmodel;

// External imports
// none

// Local imports
// None

/**
 * A listener for internal state information about the event model being
 * started up.
 * <p>
 *
 * The listener is for the VRML97 semantics of requiring the script to
 * call the initialize() method on scripts of the main world before it
 * is being shown.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface EventModelInitListener {

    /**
     * Notification that the initialization phase is now complete.
     */
    public void worldInitComplete();

    /**
     * Notification that its safe to clear the world.  The underlying
     * rendering layer should now be cleared and loaded with the
     * world.
     */
    public void changeWorld();
}
