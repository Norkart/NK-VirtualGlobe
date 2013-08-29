/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2005
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.browser;

// External imports
// none

// Local imports
// none

/**
 * A listener interface used to communicate changes in the navigation state
 * from one handler to another.
 * <p>
 *
 * @author Justin Couch, Alan Hudson
 * @version $Revision: 1.3 $
 */
public interface NavigationStateListener {
    /** The navigation state is Walking */
    public static int WALK_STATE = 1;

    /** The navigation state is Tilt */
    public static int TILT_STATE = 2;

    /** The navigation state is Panning */
    public static int PAN_STATE = 3;

    /** The navigation state is Flying */
    public static int FLY_STATE = 4;

    /** The navigation state is Examine */
    public static int EXAMINE_STATE = 5;

    /** The navigation state is such that there is no navigation */
    public static int NO_STATE = 0;

    /**
     * Notification that the navigation state has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current navModes list.
     */
    public void navigationStateChanged(int idx);

    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numTypes The number of elements in the list
     */
    public void navigationListChanged(String[] modes, int numTypes);
}
