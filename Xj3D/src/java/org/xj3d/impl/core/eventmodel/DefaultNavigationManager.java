/*****************************************************************************
 *                        Web3d.org Copyright (c) 2005 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.impl.core.eventmodel;

// External imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.NavigationStateListener;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;

import org.xj3d.core.eventmodel.NavigationManager;
import org.xj3d.core.eventmodel.NavigationStatusListener;
import org.xj3d.core.eventmodel.NavigationStatusListenerMulticaster;

/**
 * An abstract representation of a class that would be responsible for
 * performing Viewpoint management.
 * <p>
 *
 * This interface represents a further abstracted view of viewpoint management
 * handling beyond the {@link org.web3d.browser.BrowserCore}. This gives you
 * all the handling that is normally seen at a user interface level. You should
 * use one or the other, but not both as implementations of this class will
 * also interact with BrowserCore.
 * <p>
 *
 * <b>Note: This code does not current handle the navigation info nodes. It
 * only deals with the string type list as that is all we currently have
 * available from the browser internals.
 *
 * @author Justin Couch
 * @version $Revision: 1.3 $
 */
public class DefaultNavigationManager
    implements NavigationManager, NavigationStateListener {

    /** Error message when the user code barfs */
    private static final String REMOVE_ERROR_MSG =
        "Error sending viewpoint remove message: ";

    /** Error message when the user code barfs */
    private static final String NAV_SELECT_ERROR_MSG =
        "Error sending navigation info selection message: ";

    /** Error message when the user code barfs */
    private static final String NAV_CHANGE_ERROR_MSG =
        "Error sending navigation info change message: ";

    /** Error message when the user code barfs */
    private static final String NAV_STATE_ERROR_MSG =
        "Error sending navigation state change message: ";

    /** Error message when the user code barfs */
    private static final String NAV_LIST_ERROR_MSG =
        "Error sending navigation type listing message: ";

    /** Default error message when sending the error messsage fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending navigation state change event: ";

    /** Message when the user sets an invalid navigation type index */
    private static final String ACTIVE_IDX_SIZE_MSG =
        "The navigation type index selected is out of range for " +
        "the currently available types in the system: ";

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /** The listener(s) for navigation status events */
    private NavigationStatusListener listeners;

    /** List of current navigation types */
    private List<String> navigationTypes;

    /** The active index in navigationTypes */
    private int activeNavigationType;

    /** The current active navigation info node */
    private VRMLNavigationInfoNodeType activeNavInfo;

    /** The list of active navinfo nodes */
    private List<VRMLNavigationInfoNodeType> activeNavInfoList;

    /** The browser core reference for VP management */
    private BrowserCore browserCore;

    /**
     * Create a new instance of this manager that works with the given
     * browser core representation.
     *
     * @param core The browser core to work with
     */
    public DefaultNavigationManager(BrowserCore core) {
        navigationTypes = new ArrayList<String>();
        activeNavInfoList = new ArrayList<VRMLNavigationInfoNodeType>();
        activeNavigationType = -1;

        errorReporter = DefaultErrorReporter.getDefaultReporter();
        browserCore = core;
        browserCore.addNavigationStateListener(this);
    }

    //----------------------------------------------------------
    // Methods defined by NavigationManager
    //----------------------------------------------------------

    /**
     * Set the current navigation info.
     *
     * @param nav The new current navigation info.
     */
    public void setNavigationInfo(VRMLNavigationInfoNodeType nav) {
        activeNavInfo = nav;
    }

    /**
     * Get the current active navigation info node. If there isn't one
     * (eg there is no world loaded at all). Active is defined for the
     * current active navigation layer.
     *
     * @return The current active navigation info node or null
     */
    public VRMLNavigationInfoNodeType getNavigationInfo() {
        return activeNavInfo;
    }

    /**
     * Get the list of active navigation modes for the current active
     * navigation layer. This is a list of all the available options, not
     * the single type that is currently being used right now. To find
     * out which one of these modes is active, use
     * {@link #getActiveNavgiationIndex()}. This is a read-only list.
     *
     * @return The list of all the available types
     */
    public List<String> getActiveNavigationTypes() {
        return Collections.unmodifiableList(navigationTypes);
    }

    /**
     * Set the active navigation index of the current list. If the index is -1
     * then all navigation is disabled. An index of out bounds for the current
     * list throws an exception.
     *
     * @param idx The index to set as the active type
     * @throws IllegalArgumentException The index is greater than the
     *    available list size
     */
    public void setActiveNavigationIndex(int idx)
        throws IllegalArgumentException {

        if(idx >= navigationTypes.size() || idx < -1)
            throw new IllegalArgumentException(ACTIVE_IDX_SIZE_MSG + idx);

        activeNavigationType = idx;

        sendNavTypeIndex(idx);
    }

    /**
     * Fetch the index into the navigation type array of the actual type
     * of navigation being used by the system. If no type is active or the
     * type list includes "NONE" then this will return -1.
     *
     * @return The index of the active type
     */
    public int getActiveNavgiationIndex() {
        return activeNavigationType;
    }

    /**
     * Force clearing all currently managed nodes from this manager now. This
     * is used to indicate that a new world is about to be loaded and
     * everything should be cleaned out now.
     */
    public void clear() {
    }

    /**
     * Shutdown the node manager now. If this is using any external resources
     * it should remove those now as the entire application is about to die
     */
    public void shutdown() {
        browserCore.removeNavigationStateListener(this);
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Gets the viewpoints for the currently active layer.
     *
     * @return A list of the viewpoint nodes
     */
    public List<VRMLNavigationInfoNodeType> getActiveNavInfos() {
        return Collections.unmodifiableList(activeNavInfoList);
    }

    /**
     * Add a listener for viewpoint status messages. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addNavigationListener(NavigationStatusListener l) {
        listeners = NavigationStatusListenerMulticaster.add(listeners, l);
    }

    /**
     * Remove a listener for viewpoint status messages. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeNavigationListener(NavigationStatusListener l) {
        listeners = NavigationStatusListenerMulticaster.remove(listeners, l);
    }

    //----------------------------------------------------------
    // Methods defined by NavigationStateListener
    //----------------------------------------------------------

    /**
     * Notification that the navigation state has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current navModes list.
     */
    public void navigationStateChanged(int idx) {
        activeNavigationType = idx;

        sendNavTypeIndex(idx);
    }

    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numTypes The number of elements in the list
     */
    public void navigationListChanged(String[] modes, int numTypes) {
        navigationTypes.clear();

        for(int i = 0; i < numTypes; i++)
            navigationTypes.add(modes[i]);

        // Send it on down the pipe.
        if(listeners != null) {
            List<String> mode_list =
                Collections.unmodifiableList(navigationTypes);

            try {
                listeners.navigationListChanged(mode_list);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(NAV_LIST_ERROR_MSG + listeners,
                                                  (Exception)th);
                else {
                    System.out.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace();
                }
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Send out to the listeners the active navigation type list.
     */
    private void sendNavTypeIndex(int idx) {
        if(listeners == null)
            return;

        try {
            listeners.navigationStateChanged(idx);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_STATE_ERROR_MSG + listeners,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }
}

