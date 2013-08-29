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
import java.util.List;

// Local imports
import org.web3d.util.DefaultErrorReporter;
import org.web3d.util.ErrorReporter;

import org.web3d.vrml.nodes.VRMLNavigationInfoNodeType;

/**
 * A class which implements efficient and thread-safe multi-cast event
 * dispatching for the events defined in this package.
 * <p>
 *
 * This class will manage an immutable structure consisting of a chain of
 * event listeners and will dispatch events to those listeners.  Because
 * the structure is immutable, it is safe to use this API to add/remove
 * listeners during the process of an event dispatch operation.
 * <p>
 *
 * An example of how this class could be used to implement a new
 * component which fires "action" events:
 *
 * <pre><code>
 * public myComponent extends Component {
 *   NavigationStatusListener listener = null;
 *
 *   public void addNodeListener(NavigationStatusListener l) {
 *     listener = NavigationStatusListenerMulticaster.add(listener, l);
 *   }
 *
 *   public void removeNodeListener(NavigationStatusListener l) {
 *     listener = NavigationStatusListenerMulticaster.remove(listener, l);
 *   }
 *
 *   public void browerChanged(NavigationStatusEvent evt) {
 *     if(listener != null) {
 *       listener.browserChanged(evt);
 *   }
 * }
 * </code></pre>
 *
 * @author  Justin Couch
 * @version $Revision: 1.2 $
 */
public class NavigationStatusListenerMulticaster
    implements NavigationStatusListener {

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
        "Unknown error sending viewpoint event: ";

    /** The node listeners in use by this class */
    private final NavigationStatusListener a;

    /** The node listeners in use by this class */
    private final NavigationStatusListener b;

    /** Reporter instance for handing out errors */
    private static ErrorReporter errorReporter =
        DefaultErrorReporter.getDefaultReporter();

    /**
     * Creates an event multicaster instance which chains listener-a
     * with listener-b. Input parameters <code>a</code> and <code>b</code>
     * should not be <code>null</code>, though implementations may vary in
     * choosing whether or not to throw <code>NullPointerException</code>
     * in that case.
     * @param a listener-a
     * @param b listener-b
     */
    NavigationStatusListenerMulticaster(NavigationStatusListener a,
                                        NavigationStatusListener b) {
        this.a = a;
        this.b = b;
    }

    /**
     * Removes a listener from this multicaster and returns the
     * resulting multicast listener.
     * @param oldl the listener to be removed
     */
    NavigationStatusListener remove(NavigationStatusListener oldl) {

        if(oldl == a)
            return b;

        if(oldl == b)
            return a;

        NavigationStatusListener a2 = removeInternal(a, oldl);
        NavigationStatusListener b2 = removeInternal(b, oldl);

        if (a2 == a && b2 == b) {
            return this;  // it's not here
        }

        return addInternal(a2, b2);
    }

    /**
     * Register an error reporter with the engine so that any errors generated
     * by the loading of script code can be reported in a nice, pretty fashion.
     * Setting a value of null will clear the currently set reporter. If one
     * is already set, the new value replaces the old.
     *
     * @param reporter The instance to use or null
     */
    public static void setErrorReporter(ErrorReporter reporter) {
        errorReporter = reporter;

        // Reset the default only if we are not shutting down the system.
        if(reporter == null)
            errorReporter = DefaultErrorReporter.getDefaultReporter();
    }

    /**
     * Adds input-method-listener-a with input-method-listener-b and
     * returns the resulting multicast listener.
     * @param a input-method-listener-a
     * @param b input-method-listener-b
     */
    public static NavigationStatusListener add(NavigationStatusListener a,
                                              NavigationStatusListener b) {
        return (NavigationStatusListener)addInternal(a, b);
    }

    /**
     * Removes the old component-listener from component-listener-l and
     * returns the resulting multicast listener.
     * @param l component-listener-l
     * @param oldl the component-listener being removed
     */
    public static NavigationStatusListener remove(NavigationStatusListener l,
                                                 NavigationStatusListener oldl) {
        return (NavigationStatusListener)removeInternal(l, oldl);
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
        try {
            a.availableNavigationInfoChanged(states);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_CHANGE_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.availableNavigationInfoChanged(states);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_CHANGE_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
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
        try {
            a.selectedNavigationInfoChanged(state);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_SELECT_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.selectedNavigationInfoChanged(state);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_SELECT_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification that the navigation state of the currently selected
     * navigation info has changed to the new state.
     *
     * @param idx The new state expressed as an index into the current
     *     navModes list.
     */
    public void navigationStateChanged(int idx) {
        try {
            a.navigationStateChanged(idx);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_STATE_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.navigationStateChanged(idx);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_STATE_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    /**
     * Notification that the list of valid navigation modes of the current
     * navigation info node has changed.
     *
     * @param modes The new modes
     */
    public void navigationListChanged(List<String> modes) {
        try {
            a.navigationListChanged(modes);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_LIST_ERROR_MSG + a,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }

        try {
            b.navigationListChanged(modes);
        } catch(Throwable th) {
            if(th instanceof Exception)
                errorReporter.errorReport(NAV_LIST_ERROR_MSG + b,
                                          (Exception)th);
            else {
                System.out.println(DEFAULT_ERR_MSG + th);
                th.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Returns the resulting multicast listener from adding listener-a
     * and listener-b together.
     * If listener-a is null, it returns listener-b;
     * If listener-b is null, it returns listener-a
     * If neither are null, then it creates and returns
     *     a new NavigationStatusMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    private static NavigationStatusListener addInternal(NavigationStatusListener a,
                                                        NavigationStatusListener b) {
        if(a == null)
            return b;

        if(b == null)
            return a;

        return new NavigationStatusListenerMulticaster(a, b);
    }

    /**
     * Returns the resulting multicast listener after removing the
     * old listener from listener-l.
     * If listener-l equals the old listener OR listener-l is null,
     * returns null.
     * Else if listener-l is an instance of NavigationStatusMulticaster,
     * then it removes the old listener from it.
     * Else, returns listener l.
     * @param l the listener being removed from
     * @param oldl the listener being removed
     */
    private static NavigationStatusListener removeInternal(NavigationStatusListener l,
                                                          NavigationStatusListener oldl) {
        if (l == oldl || l == null) {
            return null;
        } else if (l instanceof NavigationStatusListenerMulticaster) {
            return ((NavigationStatusListenerMulticaster)l).remove(oldl);
        } else {
            return l;   // it's not here
        }
    }
}
