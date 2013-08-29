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
 * An abstract interface for navigation-specific user interface control of the
 * the browser.
 * <p>
 * This allows an external application to replace existing chunks of the user
 * interface controls with their own custom code, yet retain all the
 * functionality of the stock user interface. Making calls to methods on this
 * class will ensure that all requirements for synchronising with the browser's
 * internal event model will be met. Note that calls to these methods will not
 * be subject to the SAI's beginUpdate()/endUpdate() buffering strategy.
 * However, it will comply to all the rest of the event model behaviour, such
 * as
 *
 * @author Justin Couch
 * @version $Revision: 1.2 $
 */
public interface Xj3DNavigationUIManager  {

    /**
     * Change the navigation state to the new state as indicated by the
     * given string. If the state is not one of the listed types, an
     * exception is generated. A null value is treated the same as setting
     * the state to "NONE", disabling user navigation.
     *
     * @param state The state string to use or null
     */
    public void setNavigationState(String state);

    /**
     * Get the current navigation state that is active.
     */
    public String getNavigationState();


    /**
     * Activate the temporary navigation state that places the viewpoint in a
     * position such that the entire world fits within the borders of the
     * current viewspace.
     *
     * @param animated True if the movement should be animated to the final
     *   viewpoint position
     */
    public void fitToWorld(boolean animated);

    /**
     * Activate the temporary navigation state that has the viewpoint look at
     * the next object that is clicked.
     */
    public void viewpointLookAt();

    /**
     * Move the viewpoint back to the default position of the currently active
     * bound viewpoint. This is used to move the user back to that location
     * after they have navigated away from that location using an input device.
     */
    public void recenterViewpoint();

    /**
     * Correct the viewpoint's current orientation so that it is back into the
     * default orientation for the currently bound viewpoint. Used to help the
     * user out when they have strayed or got themselves completely confused
     * using a nav mode that allows them to become inverted or otherwise mixed
     * up.
     */
    public void straightenViewpoint();

    /**
     * Change to the next viewpoint in the list. The definition of next here
     * corresponds to the SAI definition of the same. In fact, this will work
     * identically to {@org.web3d.x3d.sai.Browser#nextViewpoint} call.
     */
    public void nextViewpoint();

    /**
     * Change to the previous viewpoint in the list. The definition of previous
     * here corresponds to the SAI definition of the same. In fact, this will
     * work identically to {@org.web3d.x3d.sai.Browser#previousViewpoint} call.
     */
    public void previousViewpoint();

    /**
     * Get the list of current viewpoints that are to be displayed. Current is
     * based on the active navigation layer. If there are no viewpoints for
     * this layer due to there being none defined (ie just the default
     * bindable) then the list will be null.
     *
     * @return A list of all the current viewpoints to display
     */
    public Xj3DViewpoint[] getCurrentViewpoints();

    /**
     * Select the given viewpoint as the new location to move to. If this
     * is no longer a valid viewpoint for the current list, an exception will
     * be generated. Passing in a null value is not permitted, even though
     * the listener may pass you one for when the default bindable is selected.
     *
     * @param The viewpoint instance to use
     */
    public void selectViewpoint(Xj3DViewpoint vp);

    /**
     * Add a listener for navigation UI feedback. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addNavigationUIListener(Xj3DNavigationUIListener l);

    /**
     * Remove a listener for navigation UI feedback. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeNavigationUIListener(Xj3DNavigationUIListener l);
}
