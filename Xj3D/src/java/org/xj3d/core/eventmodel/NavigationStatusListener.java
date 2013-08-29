/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2007
 *                               Java Source
 *
 * This source is licensed under the BSD license.
 * Please read docs/BSD.txt for the text of the license.
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.core.eventmodel;

// External imports
import java.util.List;

// Local imports
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;

/**
 * Listener for navigation user interface state change feedback from the
 * internals of the browser.
 * <p>
 *
 * These callback methods provide information for changes within an existing
 * scene. For bulk changes, such as the change of scene, please use the base
 * browser listener interface from the SAI.
 *
 * @author Justin Couch
 * @version $Revision: 1.1 $
 */
public interface NavigationStatusListener {

    /**
     * Notification that the navigation state of the currently selected
     * navigation info has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current
     *    navModes list.
     */
    public void navigationStateChanged(int idx);

    /**
     * Notification that the list of valid navigation modes of the current
     * navigation info node has changed.
     *
     * @param modes The new modes as a non-writable list
     */
    public void navigationListChanged(List<String> modes);

    /**
     * The list of available navigation states has changed to this new list.
     * The list of states corresponds to the X3D-specification defined list
     * and any additional browser-specific state names. If a state is not
     * listed in this array, then the ability to activate it should be
     * disabled (eg greying out the button that represents it). If no states
     * are currently defined, this will contain the default string "NONE",
     * which corresponds to disabling all user selection of navigation and
     * even the ability to navigate.
     *
     * @param states An array of the available state strings.
     */
    public void availableNavigationInfoChanged(VRMLNavigationInfoNodeType[] states);

    /**
     * Notification that the currently selected navigation state has changed
     * to this new value. Selection may be due to the UI interaction,
     * courtesy of a node being bound or the active navigation layer has
     * changed.
     *
     * @param state The name of the state that is now the active state
     */
    public void selectedNavigationInfoChanged(VRMLNavigationInfoNodeType state);
}
