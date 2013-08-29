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

package org.web3d.vrml.scripting.external.sai;

// External imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// Local imports
import org.web3d.browser.BrowserCore;
import org.web3d.browser.Xj3DConstants;

import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;
import org.web3d.vrml.nodes.VRMLViewpointNodeType;

import org.xj3d.core.eventmodel.ViewpointManager;
import org.xj3d.core.eventmodel.ViewpointStatusListener;

import org.xj3d.core.eventmodel.NavigationManager;
import org.xj3d.core.eventmodel.NavigationStatusListener;

import org.xj3d.sai.Xj3DNavigationUIManager;
import org.xj3d.sai.Xj3DNavigationUIListener;
import org.xj3d.sai.Xj3DViewpoint;

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
 * @version $Revision: 1.4 $
 */
class NavigationUIManagerAdapter
    implements Xj3DNavigationUIManager,
               NavigationStatusListener,
               ViewpointStatusListener {

    /** Error message when the user code barfs */
    private static final String VP_REMOVE_ERROR_MSG =
        "Error sending viewpoint remove message: ";

    /** Error message when the user code barfs */
    private static final String VP_ADD_ERROR_MSG =
        "Error sending viewpoint add message: ";

    /** Error message when the user code barfs */
    private static final String VP_CHANGE_ERROR_MSG =
        "Error sending viewpoint list change message: ";

    /** Error message when the user code barfs */
    private static final String VP_SELECT_ERROR_MSG =
        "Error sending viewpoint change selection message: ";

    /** Error message when the user code barfs */
    private static final String NAV_LIST_ERROR_MSG =
        "Error sending navigation list change message: ";

    /** Error message when the user code barfs */
    private static final String NAV_SELECT_ERROR_MSG =
        "Error sending navigation change selection message: ";

    /** Default error message when sending the error messsage fails */
    private static final String DEFAULT_ERR_MSG =
        "Unknown error sending viewpoint event: ";

    /** Listener or multicaster for nav state changes */
    private Xj3DNavigationUIListener navListener;

    /** Manager of viewpoint stuff within the internals of the browser */
    private ViewpointManager viewpointManager;

    /** Manager of navigation types within the internals of the browser */
    private NavigationManager navigationManager;

    /** Core of the browser. used for viewpoint handling */
    private BrowserCore browserCore;

    /** Cache of internal node representation to their wrapper */
    private HashMap<VRMLViewpointNodeType, Xj3DViewpoint> viewpointMap;

    /** Currently available viewpoint wrappers */
    private ArrayList<Xj3DViewpoint> viewpointWrappers;

    /** Reporter instance for handing out errors */
    private ErrorReporter errorReporter;

    /**
     * Create a new instance of this adapter that works with the given
     * viewpoint management interface. The viewpoint manager interface
     * should not be null.
     *
     * @param vpm The manager of viewpoints that we have
     */
    NavigationUIManagerAdapter(ViewpointManager vpm,
                               NavigationManager nm,
                               BrowserCore core) {
        viewpointManager = vpm;
        navigationManager = nm;
        browserCore = core;

        viewpointMap = new HashMap<VRMLViewpointNodeType, Xj3DViewpoint>();
        viewpointWrappers = new ArrayList<Xj3DViewpoint>();

        viewpointManager.addViewpointListener(this);
        navigationManager.addNavigationListener(this);

        errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    //----------------------------------------------------------
    // Methods defined by Xj3DNavigationUIManager
    //----------------------------------------------------------

    /**
     * Change the navigation state to the new state as indicated by the
     * given string. If the state is not one of the listed types, an
     * exception is generated. A null value is treated the same as setting
     * the state to "NONE", disabling user navigation.
     *
     * @param state The state string to use or null
     */
    public void setNavigationState(String state) {
        List<String> internal_states =
            navigationManager.getActiveNavigationTypes();

        for(int i = 0; i < internal_states.size(); i++) {
            String s = internal_states.get(i);
            if(s.equals(state)) {
                navigationManager.setActiveNavigationIndex(i);
                break;
            }
        }
    }

    /**
     * Get the current navigation state that is active.
     *
     * @return The active state description string
     */
    public String getNavigationState() {
        List<String> internal_states =
            navigationManager.getActiveNavigationTypes();

        int active_state = navigationManager.getActiveNavgiationIndex();

        return internal_states.get(active_state);
    }

    /**
     * Activate the temporary navigation state that places the viewpoint in a
     * position such that the entire world fits within the borders of the
     * current viewspace.
     *
     * @param animated True if the movement should be animated to the final
     *   viewpoint position
     */
    public void fitToWorld(boolean animated) {
        browserCore.fitToWorld(animated);
    }

    /**
     * Activate the temporary navigation state that has the viewpoint look at
     * the next object that is clicked.
     */
    public void viewpointLookAt() {
        browserCore.setNavigationMode(Xj3DConstants.LOOKAT_NAV_MODE);
    }

    /**
     * Move the viewpoint back to the default position of the currently active
     * bound viewpoint. This is used to move the user back to that location
     * after they have navigated away from that location using an input device.
     */
    public void recenterViewpoint() {
        viewpointManager.resetViewpoint();
    }

    /**
     * Correct the viewpoint's current orientation so that it is back into the
     * default orientation for the currently bound viewpoint. Used to help the
     * user out when they have strayed or got themselves completely confused
     * using a nav mode that allows them to become inverted or otherwise mixed
     * up.
     */
    public void straightenViewpoint() {
        // JC: Not sure what to do with this one yet.
    }

    /**
     * Change to the next viewpoint in the list. The definition of next here
     * corresponds to the SAI definition of the same. In fact, this will work
     * identically to {@org.web3d.x3d.sai.Browser#nextViewpoint} call.
     */
    public void nextViewpoint() {
        viewpointManager.nextViewpoint();
    }

    /**
     * Change to the previous viewpoint in the list. The definition of previous
     * here corresponds to the SAI definition of the same. In fact, this will
     * work identically to {@org.web3d.x3d.sai.Browser#previousViewpoint} call.
     */
    public void previousViewpoint() {
        viewpointManager.previousViewpoint();
    }

    /**
     * Get the list of current viewpoints that are to be displayed. Current is
     * based on the active navigation layer. If there are no viewpoints for
     * this layer due to there being none defined (ie just the default
     * bindable) then the list will be null.
     *
     * @return A list of all the current viewpoints to display
     */
    public Xj3DViewpoint[] getCurrentViewpoints() {
        Xj3DViewpoint[] wrapper_list = null;

        if(viewpointWrappers.size() != 0)
            wrapper_list = (Xj3DViewpoint[])viewpointWrappers.toArray();

        return wrapper_list;
    }

    /**
     * Select the given viewpoint as the new location to move to. If this
     * is no longer a valid viewpoint for the current list, an exception will
     * be generated. Passing in a null value is not permitted, even though
     * the listener may pass you one for when the default bindable is selected.
     *
     * @param The viewpoint instance to use
     */
    public void selectViewpoint(Xj3DViewpoint vp) {
        VRMLViewpointNodeType node = ((ViewpointWrapper)vp).getRealViewpoint();
        viewpointManager.setViewpoint(node);
    }

    /**
     * Add a listener for navigation UI feedback. Adding the same listener
     * instance more than once will be silently ignored. Null values are
     * ignored.
     *
     * @param l The listener instance to add
     */
    public void addNavigationUIListener(Xj3DNavigationUIListener l) {
        navListener = NavigationListenerMulticaster.add(navListener, l);
    }

    /**
     * Remove a listener for navigation UI feedback. If this listener is
     * not currently registered, the request will be silently ignored.
     *
     * @param l The listener instance to remove
     */
    public void removeNavigationUIListener(Xj3DNavigationUIListener l) {
        navListener = NavigationListenerMulticaster.remove(navListener, l);
    }

    //----------------------------------------------------------
    // Methods defined by NavigationStatusListener
    //----------------------------------------------------------

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
    public void availableNavigationInfoChanged(VRMLNavigationInfoNodeType[] states) {
        // Not doing anything with this currently.
    }

    /**
     * Notification that the currently selected navigation state has changed
     * to this new value. Selection may be due to the UI interaction,
     * courtesy of a node being bound or the active navigation layer has
     * changed.
     *
     * @param state The name of the state that is now the active state
     */
    public void selectedNavigationInfoChanged(VRMLNavigationInfoNodeType state) {
        // Not doing anything with this currently.
    }

    /**
     * Notification that the navigation state has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current navModes list.
     */
    public void navigationStateChanged(int idx) {
        if(navListener == null)
            return;

        try {
            List<String> internal_states =
                navigationManager.getActiveNavigationTypes();

            String state = internal_states.get(idx);

            navListener.selectedNavigationStateChanged(state);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_SELECT_ERROR_MSG +
                                          navListener,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification that the list of valid navigation modes has changed.
     *
     * @param modes The new modes
     * @param numTypes The number of elements in the list
     */
    public void navigationListChanged(List<String> modes) {
        if(navListener == null)
            return;

        try {
            String[] nav_list = new String[modes.size()];
            modes.toArray(nav_list);

            navListener.availableNavigationStatesChanged(nav_list);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_LIST_ERROR_MSG +
                                          navListener,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------
    // Methods defined by ViewpointStatusListener
    //----------------------------------------------------------

    /**
     * The list of active viewpoints have been changed to this new list. A new
     * list can be given due to either a new world being loaded, or the active
     * navigation layer has been changed. This method is not called if a single
     * viewpoint is added or removed from the scene.
     * <p>
     *
     * If the scene contains no viewpoints at all (except the default bindable),
     * then this will be called with a null parameter.
     * <p>
     *
     * On scene or navigation layer change, it is guaranteed that this method
     * will be called before the notification of the actual bound viewpoint.
     *
     * @param An array of exactly the number of viewpoints in the list
     */
    public void availableViewpointsChanged(VRMLViewpointNodeType[] viewpoints) {
        viewpointMap.clear();
        viewpointWrappers.clear();

        if(viewpoints != null) {
            for(int i = 0; i < viewpoints.length; i++) {
                Xj3DViewpoint vp = new ViewpointWrapper(viewpoints[i]);
                viewpointMap.put(viewpoints[i], vp);
                viewpointWrappers.add(vp);
            }
        }

        if(navListener != null) {
            try {

                Xj3DViewpoint[] wrapper_list = null;

                if(viewpointWrappers.size() != 0)
                    wrapper_list = (Xj3DViewpoint[])viewpointWrappers.toArray();

                navListener.availableViewpointsChanged(wrapper_list);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(VP_CHANGE_ERROR_MSG +
                                              navListener,
                                              (Exception)th);
                else {
                    System.out.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Notification that the selected viewpoint has been changed to this
     * new instance. There are many different reasons this could happen -
     * new node bound, world changed or even active navigation layer has
     * changed and this is the default in that new layer.
     * <p>
     *
     * If the file contains no viewpoints or the default viewpoint is
     * bound (due to unbinding all real viewpoints on the stack) then this
     * will be called with a null parameter.
     * <p>
     *
     * It is guaranteed that this will always contain something from the
     * currently active viewpoint list. If all change, that callback will be
     * called before this one, to ensure consistency.
     *
     * @param vp The viewpoint instance that is now selected or null
     */
    public void selectedViewpointChanged(VRMLViewpointNodeType vp) {
        Xj3DViewpoint wrapper = viewpointMap.get(vp);

        if(navListener != null) {
            try {
                navListener.selectedViewpointChanged(wrapper);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(VP_SELECT_ERROR_MSG +
                                              navListener,
                                              (Exception)th);
                else {
                    System.out.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Notification that this viewpoint has been appended to the list of
     * available viewpoints.
     *
     * @param vp The viewpoint instance that was added
     */
    public void viewpointAdded(VRMLViewpointNodeType vp) {

        Xj3DViewpoint wrapper = new ViewpointWrapper(vp);
        viewpointMap.put(vp, wrapper);
        viewpointWrappers.add(wrapper);

        if(navListener != null) {
            try {
                navListener.viewpointAdded(wrapper);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(VP_ADD_ERROR_MSG +
                                              navListener,
                                              (Exception)th);
                else {
                    System.out.println(DEFAULT_ERR_MSG + th);
                    th.printStackTrace();
                }
            }
        }
    }

    /**
     * Notification that this viewpoint has been removed from the list of
     * available viewpoints.
     *
     * @param vp The viewpoint instance that was added
     */
    public void viewpointRemoved(VRMLViewpointNodeType vp) {
        Xj3DViewpoint wrapper = viewpointMap.remove(vp);
        viewpointWrappers.remove(wrapper);

        if(navListener != null) {
            try {
                navListener.viewpointRemoved(wrapper);
            } catch(Throwable th) {
                if(th instanceof Exception)
                    errorReporter.errorReport(VP_REMOVE_ERROR_MSG + navListener,
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
}
